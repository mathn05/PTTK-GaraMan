"""Read-only DDL/constraint evidence from the actual project instance; excludes all row data."""
from pathlib import Path
import json,sys
sys.path.insert(0,str(Path(__file__).parent))
from local_runtime import connect

root=Path(__file__).resolve().parents[1]
cfg=json.loads((root/'.local/runtime.json').read_text());con=connect(cfg['rootPassword'],cfg['port'])
data={}
with con.cursor() as cur:
    cur.execute('USE garaman6')
    cur.execute('SELECT VERSION(),@@port,@@session.time_zone,@@datadir')
    data['runtime']=dict(zip(['serverVersion','port','sessionTimeZone','dataDirectory'],cur.fetchone()))
    cur.execute('SHOW FULL TABLES');tables=cur.fetchall();data['objects']=[]
    for name,kind in tables:
        cur.execute('SHOW CREATE TABLE `'+name+'`');ddl=cur.fetchone()[1]
        data['objects'].append({'name':name,'kind':kind,'ddl':ddl})
con.close()
(root/'docs/schema-metadata.json').write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
print('Exported actual DDL for',len(tables),'tables/views; no data rows or credentials exported.')
