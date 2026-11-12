"""V3 copy/verify/publish migration. Source tables are archived, never dropped/reset.
MySQL DDL implicitly commits; recovery is a verified reverse table swap, not a promised DDL rollback.
"""
from pathlib import Path
from decimal import Decimal
import argparse, datetime, json, re, socket
from schema_tools import *

def preflight(con,db):
    names=set(base_tables(con,db))
    allowed={t.lower() for t in TABLES}|{'tblpayment6','tblcodesequence6','tblschemaversion6'}
    if names!=allowed:raise RuntimeError('Unknown legacy table set; refuse automatic migration')
    with con.cursor() as q:
        q.execute("SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema=%s",(db,))
        if q.fetchone()[0]:raise RuntimeError('Triggers require a separate migration plan')
    members=rows(con,db,'tblMember6');employees={r['memberId']:r for r in rows(con,db,'tblEmployee6')}
    for m in members:
        if m['username'] is None:
            if m['password'] is not None or m['profileImage'] is not None:raise RuntimeError('Incomplete account cannot be normalized safely')
        elif not m['username'].strip() or not m['password'] or m['status'] not in ('ACTIVE','INACTIVE'):
            raise RuntimeError('Invalid existing account')
    invoices=rows(con,db,'tblInvoice6');payments={r['id']:r for r in rows(con,db,'tblPayment6')}
    linked=[r['paymentId'] for r in invoices if r['paymentId'] is not None]
    if len(linked)!=len(set(linked)) or set(linked)!=set(payments):raise RuntimeError('Shared, missing, or orphan legacy payment; resolve first')
    orders=rows(con,db,'tblRepairOrder6');a=[r['appointmentId'] for r in orders if r['appointmentId'] is not None]
    if len(a)!=len(set(a)):raise RuntimeError('Multiple repair orders reference one appointment')
    totals={}
    for r in rows(con,db,'tblServiceUsage6'):totals[r['repairOrderId']]=totals.get(r['repairOrderId'],Decimal(0))+r['saleUnitPrice']
    for r in rows(con,db,'tblPartUsage6'):totals[r['repairOrderId']]=totals.get(r['repairOrderId'],Decimal(0))+r['saleUnitPrice']*r['usedQuantity']
    status_counts={}
    for i in invoices:
        p=payments.get(i['paymentId'])
        if p is None:continue
        status_counts[p['status']]=status_counts.get(p['status'],0)+1
        if p['status'] not in ('PAID','PENDING','FAILED','CANCELLED'):raise RuntimeError('Unknown payment status')
        if p['status']=='PAID':
            if p['paymentAmount'] is None or p['paymentAmount']<0 or p['paymentTime'] is None or not p['paymentMethod'] or not p['paymentMethod'].strip() or p['saleStaffId'] is None:
                raise RuntimeError('Incomplete paid payment')
            if employees.get(p['saleStaffId'],{}).get('jobTitle')!='SALE_STAFF':raise RuntimeError('Wrong cashier role')
            if p['paymentAmount']!=totals.get(i['repairOrderId'],Decimal(0)):raise RuntimeError('Paid amount differs from invoice total')
    if any(len(r['name'])>25 for r in rows(con,db,'tblVehicleBrand6')):raise RuntimeError('Brand name exceeds image VARCHAR(25)')
    return {'payments':status_counts,'orphans':0,'sharedPayments':0,'duplicateOrderAppointment':0,'nonAccountStatusNormalized':sum(r['username'] is None for r in members)}

def canonical(con,source,stage):
    payments={r['id']:r for r in rows(con,source,'tblPayment6')};result={}
    for table in TABLES:
        fields=[r['COLUMN_NAME'] for r in columns(con,stage,table)]
        reverse={new:old for old,new in RENAMES[table].items()};data=[]
        for old in rows(con,source,table):
            row={f:old.get(reverse.get(f,f)) for f in fields}
            if table=='tblMember6' and row['username'] is None:row['status']=None
            if table=='tblInvoice6':
                p=payments.get(old['paymentId']);paid=p is not None and p['status']=='PAID'
                row.update(paymentStatus='PAID' if paid else 'UNPAID',paymentAmount=p['paymentAmount'] if paid else None,
                  paymentTime=p['paymentTime'] if paid else None,paymentMethod=p['paymentMethod'] if paid else None,
                  transactionReference=p['transactionReference'] if paid else None,tblEmployee6tblMember6id=p['saleStaffId'] if paid else None)
            data.append(row)
        result[table]=data
    return result

def publish(con,db,stage,archive,old_views):
    old=base_tables(con,db);new=base_tables(con,stage);new_database(con,archive)
    pairs=[ident(db)+'.'+ident(t)+' TO '+ident(archive)+'.'+ident(t) for t in old]
    pairs += [ident(stage)+'.'+ident(t)+' TO '+ident(db)+'.'+ident(t) for t in new]
    drop_views(con,db)
    try:
        with con.cursor() as q:q.execute('RENAME TABLE '+','.join(pairs))
    except Exception:
        restore_views(con,db,old_views);raise
    try:create_views(con,db)
    except Exception:
        # The receipt/stage/archive still hold every table. Never hide a partial DDL failure.
        raise RuntimeError('Tables published; view recreation failed. Use the private recovery receipt.')
    return {'oldTables':old,'newTables':new}

def migrate(con,db,folder):
    folder=Path(folder);checks=preflight(con,db);before=fingerprints(con,db);views=view_ddls(con,db)
    tag=stamp();stage='garaman6_build_'+tag+'_it';archive='garaman6_archive_'+tag+('_it' if db.endswith('_it') else '_main')
    receipt={'database':db,'stage':stage,'archive':archive,'before':before,'views':views,'preflight':checks,'phase':'staging'}
    folder.mkdir(parents=True,exist_ok=True);write_json(folder/'receipt.json',receipt)
    new_database(con,stage);create_fresh(con,stage)
    expected=canonical(con,db,stage)
    con.begin()
    try:
        with con.cursor() as q:
            for table in TABLES:
                fields=[r['COLUMN_NAME'] for r in columns(con,stage,table) if 'GENERATED' not in r['EXTRA']]
                sql='INSERT INTO '+ident(stage)+'.'+ident(table)+'('+','.join(map(ident,fields))+') VALUES('+','.join(['%s']*len(fields))+')'
                if expected[table]:q.executemany(sql,[tuple(r[f] for f in fields) for r in expected[table]])
        con.commit()
    except Exception:
        con.rollback();raise
    for table in TABLES:
        if rows(con,stage,table)!=expected[table]:raise RuntimeError('Staging copy differs: '+table)
    # One-time ID high-watermark initialization, not runtime MAX+1.
    sequence={r['prefix']:r['lastValue'] for r in rows(con,db,'tblCodeSequence6')}
    appts=expected['tblAppointment6'];suffixes=[int(r['appointmentCode'][2:]) for r in appts if re.fullmatch(r'LH[0-9]+',r['appointmentCode'],re.I)]
    next_appointment=max([0,sequence.get('LH',0),*[r['id'] for r in appts],*suffixes])+1
    next_vehicle=max([0,sequence.get('X',0),*[r['id'] for r in expected['tblVehicle6']]])+1
    if next_appointment>2147483647 or next_vehicle>2147483647:raise RuntimeError('INT identity exhausted')
    with con.cursor() as q:
        q.execute('ALTER TABLE '+ident(stage)+'.tblAppointment6 AUTO_INCREMENT='+str(next_appointment))
        q.execute('ALTER TABLE '+ident(stage)+'.tblVehicle6 AUTO_INCREMENT='+str(next_vehicle))
    signature=verify_schema(con,stage)
    if fingerprints(con,db)!=before:raise RuntimeError('Source changed while staging; retry in maintenance')
    receipt.update(phase='verified',canonicalHashes={t:hash_value(r) for t,r in expected.items()},schemaHash=signature,
                   nextAppointmentId=next_appointment,nextVehicleId=next_vehicle)
    write_json(folder/'receipt.json',receipt)
    receipt.update(publish(con,db,stage,archive,views));receipt['phase']='published'
    write_json(folder/'receipt.json',receipt)
    verify_schema(con,db)
    for table in TABLES:
        if rows(con,db,table)!=expected[table]:raise RuntimeError('Published data differs: '+table)
    if fingerprints(con,archive)!=before:raise RuntimeError('Archived source differs')
    receipt.update(success=True,phase='complete',after=fingerprints(con,db),schemaHash=verify_schema(con,db))
    write_json(folder/'receipt.json',receipt)
    state_path=LOCAL/'schema-state.json';state=json.loads(state_path.read_text(encoding='utf-8')) if state_path.exists() else {}
    state[db]={'version':'V3','schemaHash':receipt['schemaHash'],'receipt':str(folder/'receipt.json'),'verifiedAt':datetime.datetime.now().isoformat()}
    write_json(state_path,state);return receipt

def rollback(con,receipt):
    db=receipt['database'];archive=receipt['archive'];quarantine='garaman6_reverted_'+stamp()+'_it'
    new_database(con,quarantine);current=base_tables(con,db);old=base_tables(con,archive)
    if set(current)!=set(receipt['newTables']) or set(old)!=set(receipt['oldTables']):raise RuntimeError('Recovery table set changed')
    drop_views(con,db)
    pairs=[ident(db)+'.'+ident(t)+' TO '+ident(quarantine)+'.'+ident(t) for t in current]
    pairs += [ident(archive)+'.'+ident(t)+' TO '+ident(db)+'.'+ident(t) for t in old]
    with con.cursor() as q:q.execute('RENAME TABLE '+','.join(pairs))
    restore_views(con,db,receipt['views'])
    if fingerprints(con,db)!=receipt['before']:raise RuntimeError('Rollback fingerprint mismatch')
    return {'restoredDatabase':db,'allLegacyTablesEqual':True,'newDataPreservedIn':quarantine}

def main():
    p=argparse.ArgumentParser();p.add_argument('action',choices=['test','main','rollback']);p.add_argument('--database');p.add_argument('--receipt');a=p.parse_args()
    con=connect();out=ROOT/'docs/final/evidence';tag=stamp()
    try:
        if a.action=='test':
            test_db=a.database or 'garaman6_v3_it'
            if not re.fullmatch(r'garaman6[a-zA-Z0-9_]*_it',test_db):raise RuntimeError('Migration rehearsal requires a new _it schema')
            folder=LOCAL/'backups'/('v3-test-'+tag);backup(con,'garaman6',folder)
            restored=restore_dump(con,test_db,folder)
            first=migrate(con,test_db,folder/'first')
            recovery=rollback(con,first)
            final=migrate(con,test_db,folder/'final')
            proof={'success':True,'backupRestore':restored,'rollback':recovery,'migration':{k:final[k] for k in ['database','archive','schemaHash','preflight','nextAppointmentId','nextVehicleId','success']},'privateBackup':str(folder),'ddlSha256':hashlib.sha256((ROOT/'sql/schema.sql').read_bytes()).hexdigest()}
            write_json(out/'migration-test.json',proof);print(json.dumps(proof,ensure_ascii=True))
        elif a.action=='main':
            proof=json.loads((out/'migration-test.json').read_text(encoding='utf-8'))
            if not proof['success'] or proof['ddlSha256']!=hashlib.sha256((ROOT/'sql/schema.sql').read_bytes()).hexdigest():raise RuntimeError('Current DDL has no tested restore/migration/rollback proof')
            state=json.loads((LOCAL/'tomcat.json').read_text(encoding='utf-8'))
            with socket.socket() as sock:
                if sock.connect_ex(('127.0.0.1',state['port']))==0:raise RuntimeError('Stop only the project Tomcat before main migration')
            folder=LOCAL/'backups'/('v3-main-'+tag);backup(con,'garaman6',folder)
            restored=restore_dump(con,'garaman6_restore_'+tag+'_it',folder)
            receipt=migrate(con,'garaman6',folder/'migration')
            result={'success':True,'backupRestore':restored,'migration':{k:receipt[k] for k in ['database','archive','schemaHash','preflight','nextAppointmentId','nextVehicleId','success']},'privateBackup':str(folder)}
            write_json(out/'migration-main.json',result);print(json.dumps(result,ensure_ascii=True))
        else:
            if not a.receipt:raise RuntimeError('--receipt required')
            receipt=json.loads(Path(a.receipt).read_text(encoding='utf-8'))
            if not receipt['database'].endswith('_it'):raise RuntimeError('Main recovery requires reviewed maintenance instructions; not an automatic test command')
            print(json.dumps(rollback(con,receipt)))
    finally:con.close()
if __name__=='__main__':main()
