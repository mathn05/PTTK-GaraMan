"""Windows-only isolated MySQL runtime; never opens the existing service's data directory."""
from pathlib import Path
from schema_tools import verify_schema, write_json
import argparse, json, os, secrets, socket, subprocess, sys, time

ROOT=Path(__file__).resolve().parents[1]
LOCAL=ROOT/'.local'
RUNTIME=ROOT/'.runtime'
BIN=Path(os.environ.get('GARAMAN_MYSQL_BIN',r'C:\Program Files\MySQL\MySQL Server 9.4\bin'))

def wait_port(port):
    for _ in range(60):
        try:
            with socket.create_connection(('127.0.0.1',port),timeout=1):return
        except OSError:time.sleep(.5)
    raise RuntimeError('MySQL did not start; inspect .runtime/mysql/server.log')

def connect(password,port):
    import pymysql
    return pymysql.connect(host='127.0.0.1',port=port,user='root',password=password,charset='utf8mb4',autocommit=True)

def sql_statements(filename):
    lines=[line for line in Path(filename).read_text(encoding='utf-8').splitlines() if not line.lstrip().startswith('--')]
    source='\n'.join(lines);quoted=False;escaped=False;buffer=[]
    for ch in source:
        if ch=="'" and not escaped:quoted=not quoted
        if ch==';' and not quoted:
            if ''.join(buffer).strip():yield ''.join(buffer)
            buffer=[]
        else:buffer.append(ch)
        escaped=(ch=='\\' and not escaped)
    if ''.join(buffer).strip():yield ''.join(buffer)

def run_sql(con,filename):
    with con.cursor() as cur:
        for statement in sql_statements(filename):cur.execute(statement)

def main():
    if os.name!='nt':raise RuntimeError('This script targets Windows; do not report cloud/WSL as the target machine.')
    parser=argparse.ArgumentParser();parser.add_argument('action',choices=['setup','start','stop','status']);args=parser.parse_args()
    LOCAL.mkdir(exist_ok=True);data=RUNTIME/'mysql'/'data';cfg=LOCAL/'runtime.json'
    if not cfg.exists():
        if args.action!='setup':raise RuntimeError('Run setup first')
        for port in [3306,3307,3308]:
            with socket.socket() as s:
                if s.connect_ex(('127.0.0.1',port))!=0:break
        cfg.write_text(json.dumps({'port':port,'rootPassword':secrets.token_urlsafe(30),'appPassword':secrets.token_urlsafe(30)}),encoding='utf-8')
    settings=json.loads(cfg.read_text(encoding='utf-8'));port=settings['port']
    if args.action in ('stop','status'):
        con=connect(settings['rootPassword'],port)
        with con.cursor() as cur:
            if args.action=='stop':cur.execute('SHUTDOWN');print('Stopped project MySQL only.')
            else:
                cur.execute('SELECT VERSION(),@@port,@@session.time_zone,@@datadir');print(cur.fetchone())
        con.close();return
    initial=not (data/'mysql').exists()
    if initial:
        data.mkdir(parents=True,exist_ok=True)
        subprocess.run([str(BIN/'mysqld.exe'),'--no-defaults','--initialize-insecure',f'--basedir={BIN.parent}',f'--datadir={data}'],check=True,creationflags=subprocess.CREATE_NO_WINDOW)
    try:con=connect('' if initial else settings['rootPassword'],port)
    except Exception:
        with socket.socket() as s:
            if s.connect_ex(('127.0.0.1',port))==0:raise RuntimeError('Port occupied by a different instance; refusing to change it.')
        log=RUNTIME/'mysql'/'server.log'
        with log.open('ab') as stream:
            proc=subprocess.Popen([str(BIN/'mysqld.exe'),'--no-defaults',f'--basedir={BIN.parent}',f'--datadir={data}',f'--port={port}','--bind-address=127.0.0.1','--mysqlx=OFF','--default-time-zone=+07:00','--character-set-server=utf8mb4','--collation-server=utf8mb4_0900_ai_ci',f'--pid-file={RUNTIME / "mysql" / "mysql.pid"}'],stdout=stream,stderr=stream,creationflags=subprocess.CREATE_NO_WINDOW)
        wait_port(port);con=connect('' if initial else settings['rootPassword'],port)
    if initial:
        with con.cursor() as cur:cur.execute("ALTER USER 'root'@'localhost' IDENTIFIED BY %s",(settings['rootPassword'],))
    if args.action=='setup':
        with con.cursor() as cur:
            cur.execute('CREATE DATABASE IF NOT EXISTS garaman6 CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci')
            cur.execute('CREATE DATABASE IF NOT EXISTS garaman6_v3_it CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci')
            cur.execute("CREATE USER IF NOT EXISTS 'garaman_app'@'127.0.0.1' IDENTIFIED BY %s",(settings['appPassword'],))
            cur.execute("CREATE USER IF NOT EXISTS 'garaman_test'@'127.0.0.1' IDENTIFIED BY %s",(settings['appPassword'],))
            cur.execute("GRANT SELECT,INSERT,UPDATE ON garaman6.* TO 'garaman_app'@'127.0.0.1'")
            cur.execute("GRANT SELECT,INSERT,UPDATE,DELETE ON garaman6_v3_it.* TO 'garaman_test'@'127.0.0.1'")
        for db in ['garaman6','garaman6_v3_it']:
            with con.cursor() as cur:
                cur.execute('USE '+db);cur.execute('SHOW TABLES');existing=cur.fetchall()
            if existing:
                signature=verify_schema(con,db)
                print(db,'V3 columns/types/null/default/generated/index/FK/CHECK/view dependencies verified; data preserved.')
            else:
                run_sql(con,ROOT/'sql/schema.sql')
                con.begin()
                try:
                    run_sql(con,ROOT/'sql/seed.sql');run_sql(con,ROOT/'sql/seed-slots.sql');con.commit()
                except Exception:
                    con.rollback();raise
                signature=verify_schema(con,db)
                print(db,'fresh V3 schema and seed applied.')
            ledger=LOCAL/'schema-state.json'
            state=json.loads(ledger.read_text(encoding='utf-8')) if ledger.exists() else {}
            state[db]={'version':'V3','schemaHash':signature,'verifiedAt':time.time()};write_json(ledger,state)
        url=f'jdbc:mysql://127.0.0.1:{port}/garaman6?connectionTimeZone=Asia/Ho_Chi_Minh&forceConnectionTimeZoneToSession=false&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&sslMode=DISABLED'
        content=f'GARAMAN_DB_URL={url}\nGARAMAN_DB_USER=garaman_app\nGARAMAN_DB_PASSWORD={settings["appPassword"]}\nGARAMAN_TIME_ZONE=Asia/Ho_Chi_Minh\n'
        (LOCAL/'garaman.properties').write_text(content,encoding='utf-8')
        (LOCAL/'test.properties').write_text(content.replace('/garaman6?','/garaman6_v3_it?').replace('=garaman_app','=garaman_test')+'GARAMAN_TEST_MODE=true\nGARAMAN_TEST_DATABASE=garaman6_v3_it\n',encoding='utf-8')
    with con.cursor() as cur:
        cur.execute('SELECT VERSION(),@@port,@@session.time_zone');print('Actual Windows MySQL server:',cur.fetchone())
    con.close()

if __name__=='__main__':
    main()
