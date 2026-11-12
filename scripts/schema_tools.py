"""Schema validation/private backups; no history tables in application schemas."""
from pathlib import Path
import datetime, hashlib, json, os, re, subprocess, tempfile
import pymysql
ROOT=Path(__file__).resolve().parents[1]
LOCAL=ROOT/'.local'
BIN=Path(os.environ.get('GARAMAN_MYSQL_BIN',r'C:\Program Files\MySQL\MySQL Server 9.4\bin'))
TABLES=['tblMember6','tblCustomer6','tblEmployee6','tblVehicleBrand6','tblVehicle6','tblAppointmentSlot6','tblAppointment6','tblPartBrand6','tblCategory6','tblService6','tblSparePart6','tblRepairOrder6','tblServiceUsage6','tblPartUsage6','tblInvoice6','tblSupplier6','tblPartReceipt6','tblReceiptDetail6']
COMMON={'memberId':'tblMember6id','customerId':'tblCustomer6tblMember6id','vehicleBrandId':'tblVehicleBrand6id','vehicleId':'tblVehicle6id','appointmentSlotId':'tblAppointmentSlot6id','appointmentId':'tblAppointment6id','repairOrderId':'tblRepairOrder6id','serviceId':'tblService6id','sparePartId':'tblSparePart6id','partBrandId':'tblPartBrand6id','categoryId':'tblCategory6id','partReceiptId':'tblPartReceipt6id','supplierId':'tblSupplier6id','technicalSpecifications':'technicalSpec'}
RENAMES={t:dict(COMMON) for t in TABLES}
for t in TABLES:
    employee='technicianId' if t in ('tblServiceUsage6','tblPartUsage6') else 'warehouseStaffId' if t=='tblPartReceipt6' else 'saleStaffId'
    RENAMES[t][employee]='tblEmployee6tblMember6id'

def ident(name):
    if not re.fullmatch(r'[A-Za-z][A-Za-z0-9_]{0,63}',name):raise ValueError('Invalid SQL identifier')
    return chr(96)+name+chr(96)

def connect(database=None):
    settings=json.loads((LOCAL/'runtime.json').read_text(encoding='utf-8'))
    con=pymysql.connect(host='127.0.0.1',port=settings['port'],user='root',password=settings['rootPassword'],database=database,charset='utf8mb4',autocommit=True)
    with con.cursor() as q:q.execute("SET time_zone='+07:00'")
    return con

def statements(path):
    source='\n'.join(x for x in Path(path).read_text(encoding='utf-8').splitlines() if not x.lstrip().startswith('--'))
    quoted=False;escaped=False;buf=[]
    for ch in source:
        if ch=="'" and not escaped:quoted=not quoted
        if ch==';' and not quoted:
            if ''.join(buf).strip():yield ''.join(buf)
            buf=[]
        else:buf.append(ch)
        escaped=(ch=='\\' and not escaped)
    if ''.join(buf).strip():yield ''.join(buf)

def run_sql(con,path):
    with con.cursor() as q:
        for sql in statements(path):q.execute(sql)
def stamp():return datetime.datetime.now().strftime('%Y%m%d_%H%M%S_%f')
def write_json(path,value):
    Path(path).parent.mkdir(parents=True,exist_ok=True)
    Path(path).write_text(json.dumps(value,ensure_ascii=False,indent=2,default=str),encoding='utf-8')
def hash_value(value):return hashlib.sha256(json.dumps(value,sort_keys=True,default=str,ensure_ascii=False,separators=(',',':')).encode()).hexdigest()
def base_tables(con,db):
    with con.cursor() as q:
        q.execute("SELECT table_name FROM information_schema.tables WHERE table_schema=%s AND table_type='BASE TABLE' ORDER BY table_name",(db,))
        return [r[0] for r in q.fetchall()]
def columns(con,db,table):
    with con.cursor(pymysql.cursors.DictCursor) as q:
        q.execute("SELECT COLUMN_NAME,EXTRA FROM information_schema.columns WHERE table_schema=%s AND table_name=%s ORDER BY ordinal_position",(db,table.lower()))
        return q.fetchall()
def rows(con,db,table):
    with con.cursor(pymysql.cursors.DictCursor) as q:
        q.execute('SELECT * FROM '+ident(db)+'.'+ident(table)+' ORDER BY 1');return list(q.fetchall())
def fingerprints(con,db):
    return {t:{'rows':len(data),'sha256':hash_value(data)} for t in base_tables(con,db) for data in [rows(con,db,t)]}

def shape(con,db):
    with con.cursor() as q:
        q.execute("SELECT table_name,table_type FROM information_schema.tables WHERE table_schema=%s ORDER BY table_name",(db,))
        objects=[(a.lower(),b) for a,b in q.fetchall()]
        q.execute("SELECT table_name,ordinal_position,column_name,column_type,is_nullable,column_default,extra,generation_expression,character_maximum_length,datetime_precision,numeric_precision,numeric_scale FROM information_schema.columns WHERE table_schema=%s ORDER BY table_name,ordinal_position",(db,))
        cols=[tuple([r[0].lower(),*r[1:]]) for r in q.fetchall()]
        q.execute("SELECT table_name,index_name,non_unique,seq_in_index,column_name,sub_part FROM information_schema.statistics WHERE table_schema=%s ORDER BY table_name,index_name,seq_in_index",(db,))
        groups={}
        for t,n,nu,pos,col,sub in q.fetchall():groups.setdefault((t.lower(),n,nu),[]).append((pos,col,sub))
        indexes=sorted([(t,nu,c) for (t,n,nu),c in groups.items()],key=str)
        q.execute("SELECT k.table_name,k.constraint_name,k.column_name,k.referenced_table_schema,k.referenced_table_name,k.referenced_column_name,r.update_rule,r.delete_rule,k.ordinal_position FROM information_schema.key_column_usage k JOIN information_schema.referential_constraints r ON r.constraint_schema=k.constraint_schema AND r.constraint_name=k.constraint_name WHERE k.table_schema=%s AND k.referenced_table_name IS NOT NULL ORDER BY k.table_name,k.constraint_name,k.ordinal_position",(db,))
        fks=[(t.lower(),col,'SELF' if schema==db else schema,parent.lower(),pcol,up,delete,pos) for t,n,col,schema,parent,pcol,up,delete,pos in q.fetchall()]
        q.execute("SELECT t.table_name,c.check_clause FROM information_schema.table_constraints t JOIN information_schema.check_constraints c ON c.constraint_schema=t.constraint_schema AND c.constraint_name=t.constraint_name WHERE t.table_schema=%s AND t.constraint_type='CHECK'",(db,))
        checks=sorted([(t.lower(),re.sub(r'\s+',' ',expr).strip()) for t,expr in q.fetchall()])
        q.execute("SELECT view_name,table_schema FROM information_schema.view_table_usage WHERE view_schema=%s",(db,))
        if any(schema!=db for _,schema in q.fetchall()):raise RuntimeError('View references another schema')
    return json.loads(json.dumps({'objects':objects,'columns':cols,'indexes':indexes,'foreignKeys':sorted(fks,key=str),'checks':checks},default=str))
def verify_schema(con,db):
    expected=json.loads((ROOT/'sql/schema-contract.json').read_text(encoding='utf-8'));actual=shape(con,db)
    if actual!=expected:raise RuntimeError('Schema is not V3: '+','.join(k for k in expected if actual.get(k)!=expected[k]))
    return hash_value(actual)
def new_database(con,db):
    if not db.startswith('garaman6_'):raise RuntimeError('Only isolated project schemas can be created')
    with con.cursor() as q:
        q.execute('SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name=%s',(db,))
        if q.fetchone()[0]:raise RuntimeError('Refuse to overwrite '+db)
        q.execute('CREATE DATABASE '+ident(db)+' CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci')
def create_fresh(con,db):
    with con.cursor() as q:
        q.execute('USE '+ident(db));q.execute('SHOW TABLES')
        if q.fetchall():raise RuntimeError('Fresh DDL requires an empty schema')
    run_sql(con,ROOT/'sql/schema.sql')
def create_views(con,db):
    with con.cursor() as q:
        q.execute('USE '+ident(db))
        for sql in statements(ROOT/'sql/schema.sql'):
            if sql.lstrip().upper().startswith('CREATE VIEW'):q.execute(sql)
def view_ddls(con,db):
    with con.cursor() as q:
        q.execute("SELECT table_name FROM information_schema.views WHERE table_schema=%s",(db,))
        names=[r[0] for r in q.fetchall()];result={}
        for n in names:
            q.execute('SHOW CREATE VIEW '+ident(db)+'.'+ident(n));result[n]=q.fetchone()[1]
        return result
def drop_views(con,db):
    with con.cursor() as q:
        for name in view_ddls(con,db):q.execute('DROP VIEW '+ident(db)+'.'+ident(name))
def restore_views(con,db,ddls,original=None):
    with con.cursor() as q:
        q.execute('USE '+ident(db))
        for sql in ddls.values():
            if original:sql=sql.replace(ident(original)+'.',ident(db)+'.')
            q.execute(sql.replace('CREATE ALGORITHM','CREATE OR REPLACE ALGORITHM',1))

def client_config():
    settings=json.loads((LOCAL/'runtime.json').read_text(encoding='utf-8'))
    with tempfile.NamedTemporaryFile(mode='w',encoding='utf-8',dir=LOCAL,suffix='.cnf',delete=False) as f:
        f.write('[client]\nuser=root\npassword="'+settings['rootPassword']+'"\nhost=127.0.0.1\nport='+str(settings['port'])+'\ndefault-character-set=utf8mb4\n')
        return Path(f.name)
def backup(con,db,folder):
    folder=Path(folder);folder.mkdir(parents=True,exist_ok=False);config=client_config();dump=folder/'database.sql'
    try:
        args=[str(BIN/'mysqldump.exe'),'--defaults-extra-file='+str(config),'--single-transaction','--hex-blob','--set-gtid-purged=OFF','--no-tablespaces','--column-statistics=0','--result-file='+str(dump),db]
        r=subprocess.run(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        if r.returncode:raise RuntimeError('mysqldump failed: '+r.stderr.decode('utf-8',errors='replace')[:300])
    finally:config.unlink(missing_ok=True)
    data={'database':db,'time':datetime.datetime.now().isoformat(),'dumpSha256':hashlib.sha256(dump.read_bytes()).hexdigest(),'tables':fingerprints(con,db),'views':view_ddls(con,db)}
    write_json(folder/'manifest.json',data);return data
def restore_dump(con,db,folder):
    folder=Path(folder);manifest=json.loads((folder/'manifest.json').read_text(encoding='utf-8'));dump=folder/'database.sql'
    if hashlib.sha256(dump.read_bytes()).hexdigest()!=manifest['dumpSha256']:raise RuntimeError('Backup hash mismatch')
    new_database(con,db);config=client_config()
    try:
        with dump.open('rb') as stream:r=subprocess.run([str(BIN/'mysql.exe'),'--defaults-extra-file='+str(config),db],stdin=stream,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        if r.returncode:raise RuntimeError('Restore failed: '+r.stderr.decode('utf-8',errors='replace')[:500])
    finally:config.unlink(missing_ok=True)
    restore_views(con,db,manifest['views'],manifest['database'])
    if fingerprints(con,db)!=manifest['tables']:raise RuntimeError('Restored data differs from backup')
    return {'database':db,'allTablesEqual':True,'tableCount':len(manifest['tables']),'dumpSha256':manifest['dumpSha256']}

