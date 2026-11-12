"""Run the project WAR on native Windows Tomcat without requiring an IDE plugin."""
from pathlib import Path
import argparse, hashlib, json, os, shutil, socket, subprocess, time, urllib.request

ROOT=Path(__file__).resolve().parents[1]
HOME=ROOT/'.runtime/apache-tomcat-10.1.61'
BASE=ROOT/'.runtime/tomcat-base'
STATE=ROOT/'.local/tomcat.json'
TEST_MODE=False

def stop():
    if not STATE.exists():return
    state=json.loads(STATE.read_text());pid=int(state['pid'])
    literal=str(BASE).replace("'","''")
    code=f"$p = Get-CimInstance Win32_Process -Filter 'ProcessId={pid}'; if ($p -and $p.CommandLine.Contains('{literal}')) {{ Stop-Process -Id {pid}; Write-Output 'Stopped project Tomcat' }}"
    subprocess.run(['powershell.exe','-NoProfile','-Command',code],check=True,creationflags=subprocess.CREATE_NO_WINDOW)
    for _ in range(20):
        try:
            with socket.create_connection(('127.0.0.1',state['port']),timeout=.3):time.sleep(.3)
        except OSError:break

def start():
    if os.name!='nt':raise RuntimeError('Windows runtime only')
    if not HOME.exists():raise RuntimeError('Tomcat distribution is missing; run Setup-GaraMan.ps1')
    if not (ROOT/'target/garaman6.war').exists():raise RuntimeError('Build the WAR first')
    stop()
    BASE.mkdir(exist_ok=True)
    if not (BASE/'conf').exists():shutil.copytree(HOME/'conf',BASE/'conf')
    for d in ['webapps','logs','temp','work']:(BASE/d).mkdir(exist_ok=True)
    # Reproducible WARs use stable ZIP timestamps. Clear only this application's
    # generated JSP cache after stopping its JVM, so old bytecode cannot survive.
    cache=(BASE/'work/Catalina/localhost/garaman6').resolve()
    allowed=(BASE/'work').resolve()
    if not cache.is_relative_to(ROOT.resolve()) or not cache.is_relative_to(allowed) or cache.name!='garaman6':
        raise RuntimeError('JSP cache resolved outside the intended project directory')
    if cache.exists():shutil.rmtree(cache)
    port=8082 if TEST_MODE else 8080
    with socket.socket() as s:
        if s.connect_ex(('127.0.0.1',port))==0:
            for port in range(8081,8091):
                with socket.socket() as other:
                    if other.connect_ex(('127.0.0.1',port))!=0:break
            else:raise RuntimeError('No free HTTP port')
    source=(HOME/'conf/server.xml').read_text(encoding='utf-8')
    source=source.replace('<Server port="8005"','<Server port="-1"').replace('<Connector port="8080"',f'<Connector address="127.0.0.1" port="{port}"')
    (BASE/'conf/server.xml').write_text(source,encoding='utf-8')
    shutil.copy2(ROOT/'target/garaman6.war',BASE/'webapps/garaman6.war')
    # Tomcat expands a changed WAR and replaces its own old exploded application.
    java=shutil.which('java')
    config=ROOT/('.local/test.properties' if TEST_MODE else '.local/garaman.properties')
    args=[java,f'-Dgaraman.config={config}',f'-Dcatalina.home={HOME}',f'-Dcatalina.base={BASE}',f'-Djava.io.tmpdir={BASE / "temp"}','-Duser.timezone=Asia/Ho_Chi_Minh','-Dfile.encoding=UTF-8','-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager',f'-Djava.util.logging.config.file={BASE / "conf/logging.properties"}','-classpath',str(HOME/'bin/bootstrap.jar')+';'+str(HOME/'bin/tomcat-juli.jar'),'org.apache.catalina.startup.Bootstrap','start']
    if TEST_MODE:args[1:1]=['-DGARAMAN_TEST_MODE=true','-DGARAMAN_TEST_DATABASE=garaman6_v3_it']
    with (BASE/'logs/stdout.log').open('ab') as out,(BASE/'logs/stderr.log').open('ab') as err:
        p=subprocess.Popen(args,cwd=ROOT,stdout=out,stderr=err,creationflags=subprocess.CREATE_NO_WINDOW)
    STATE.write_text(json.dumps({'pid':p.pid,'port':port,'url':f'http://localhost:{port}/garaman6/login','mode':'test' if TEST_MODE else 'main'}),encoding='utf-8')
    for _ in range(80):
        if p.poll() is not None:raise RuntimeError('Tomcat exited; inspect logs')
        try:
            response=urllib.request.urlopen(f'http://127.0.0.1:{port}/garaman6/login',timeout=2)
            if response.status==200:print(f'Tomcat ready: http://localhost:{port}/garaman6/login');return
        except Exception:time.sleep(.5)
    raise RuntimeError('Tomcat did not serve login; inspect .runtime/tomcat-base/logs')

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('action',choices=['start','stop','status']);p.add_argument('--test',action='store_true');a=p.parse_args()
    if a.test:
        TEST_MODE=True;BASE=ROOT/'.runtime/tomcat-test-base';STATE=ROOT/'.local/tomcat-test.json'
    if a.action=='start':start()
    elif a.action=='stop':stop()
    else:print(STATE.read_text() if STATE.exists() else 'Not started')
