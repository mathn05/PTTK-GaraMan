"""Per-write test guards, actual application DB identity, and cleanup even after a failed check."""
from pathlib import Path
from urllib.parse import urlparse
from urllib.request import urlopen
import json, os, re
import pymysql
from schema_tools import ROOT, connect, verify_schema, ident, rows

def properties(path):
    return {k.strip():v.strip() for line in Path(path).read_text(encoding='utf-8-sig').splitlines()
            if line.strip() and not line.lstrip().startswith('#') and '=' in line for k,v in [line.split('=',1)]}

class GuardedTest:
    def __init__(self,base):
        if os.name!='nt':raise RuntimeError('Run tests on native Windows, not cloud/WSL')
        self.base=base;u=urlparse(base)
        if u.scheme!='http' or u.hostname not in ('127.0.0.1','localhost'):raise RuntimeError('Only local test HTTP targets are allowed')
        cfg=properties(os.environ.get('GARAMAN_TEST_CONFIG',str(ROOT/'.local/test.properties')))
        match=re.match(r'jdbc:mysql://(127\.0\.0\.1|localhost):(\d+)/([A-Za-z0-9_]+)(?:\?|$)',cfg.get('GARAMAN_DB_URL',''))
        if not match:raise RuntimeError('Refuse unknown test JDBC target')
        self.db=match[3]
        if not re.fullmatch(r'garaman6[a-zA-Z0-9_]*_it',self.db) or self.db!=cfg.get('GARAMAN_TEST_DATABASE') or cfg.get('GARAMAN_TEST_MODE')!='true':
            raise RuntimeError('Refuse writes outside explicitly configured test schema')
        self.con=pymysql.connect(host=match[1],port=int(match[2]),database=self.db,user=cfg['GARAMAN_DB_USER'],password=cfg['GARAMAN_DB_PASSWORD'],charset='utf8mb4',autocommit=True)
        with self.con.cursor() as q:q.execute("SET time_zone='+07:00'")
        try:self.guard()
        except Exception:self.con.close();raise
        with connect(self.db) as metadata:self.schema_hash=verify_schema(metadata,self.db)
        self.original={t:rows(self.con,self.db,t) for t in ['tblAppointment6','tblVehicle6','tblAppointmentSlot6','tblServiceUsage6','tblPartUsage6','tblInvoice6','tblCustomer6','tblMember6']}
        self.cleanup_result=None

    def guard(self,http=True):
        with self.con.cursor() as q:
            q.execute('SELECT DATABASE(),@@session.time_zone');db,zone=q.fetchone()
            if db!=self.db or not db.endswith('_it') or zone!='+07:00':raise RuntimeError('Test connection guard failed')
        if http:
            info=json.load(urlopen(self.base+'/test-runtime',timeout=10))
            if info.get('database')!=self.db or info.get('timeZone')!='+07:00':raise RuntimeError('Actual application does not use the guarded test DB')

    def execute(self,sql,args=(),http=True):
        self.guard(http=http)
        with self.con.cursor() as q:q.execute(sql,args);return q.rowcount
    def query(self,sql,args=()):
        self.guard(http=False)
        with self.con.cursor() as q:q.execute(sql,args);return q.fetchall()
    def count_appointments(self):return self.query('SELECT COUNT(*) FROM tblAppointment6')[0][0]

    def cleanup(self):
        # HTTP may be unavailable after a failing browser test; SQL still verifies the dedicated test schema before every write.
        self.guard(http=False);deleted={}
        for table in ['tblAppointment6','tblServiceUsage6','tblPartUsage6','tblVehicle6','tblAppointmentSlot6','tblCustomer6','tblMember6']:
            key='tblMember6id' if table=='tblCustomer6' else 'id'
            before={r[key] for r in self.original[table]}
            current={r[0] for r in self.query('SELECT '+ident(key)+' FROM '+ident(table))}
            ids=sorted(current-before);deleted[table]=len(ids)
            for id in ids:self.execute('DELETE FROM '+ident(table)+' WHERE '+ident(key)+'=%s',(id,),http=False)
        for r in self.original['tblAppointmentSlot6']:
            self.execute('UPDATE tblAppointmentSlot6 SET status=%s,holdCode=%s,holdExpiresAt=%s,startTime=%s WHERE id=%s',
                (r['status'],r['holdCode'],r['holdExpiresAt'],r['startTime'],r['id']),http=False)
        for table in ['tblServiceUsage6','tblPartUsage6']:
            for r in self.original[table]:self.execute('UPDATE '+ident(table)+' SET saleUnitPrice=%s WHERE id=%s',(r['saleUnitPrice'],r['id']),http=False)
        for r in self.original['tblInvoice6']:
            fields=['paymentAmount','paymentTime','paymentMethod','paymentStatus','transactionReference','tblEmployee6tblMember6id']
            self.execute('UPDATE tblInvoice6 SET '+','.join(ident(f)+'=%s' for f in fields)+' WHERE id=%s',tuple(r[f] for f in fields)+(r['id'],),http=False)
        equal={t:rows(self.con,self.db,t)==original for t,original in self.original.items()}
        self.cleanup_result={'deleted':deleted,'restoredTables':equal,'allRestored':all(equal.values()),'database':self.db}
        if not all(equal.values()):raise RuntimeError('Cleanup data mismatch')
        self.con.close();return self.cleanup_result

