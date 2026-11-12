"""Explicit post-migration main verification: business data is read-only; login changes session only."""
import argparse,hashlib,json,os,re,shutil,subprocess
from pathlib import Path
from urllib.parse import urlencode,urlparse
from playwright.sync_api import sync_playwright
from schema_tools import ROOT,connect,fingerprints,shape,verify_schema,write_json
from test_support import properties

def main():
    p=argparse.ArgumentParser();p.add_argument('--main',action='store_true',required=True);p.parse_args()
    state=json.loads((ROOT/'.local/tomcat.json').read_text(encoding='utf-8'))
    config=ROOT/'.local/garaman.properties';cfg=properties(config)
    if urlparse(cfg['GARAMAN_DB_URL'][5:]).path!='/garaman6':raise RuntimeError('Unexpected main config')
    base='http://127.0.0.1:'+str(state['port'])+'/garaman6';out=ROOT/'docs/final/evidence/main';out.mkdir(exist_ok=True)
    helper=ROOT/'.runtime/tools/InspectRuntime6.java'
    helper.write_text('import garaman.dao.DAO6; class InspectRuntime6 { public static void main(String[] a) { try(var d=new DAO6()){var m=d.runtimeInfo();System.out.println(m.get("database")+"|"+m.get("timeZone")+"|"+m.get("serverVersion"));}}}',encoding='utf-8')
    cp=os.pathsep.join([str(ROOT/'target/classes'),str(ROOT/'target/garaman6/WEB-INF/lib/mysql-connector-j-9.4.0.jar')])
    r=subprocess.run([shutil.which('java'),'-Dgaraman.config='+str(config),'-Duser.timezone=Asia/Ho_Chi_Minh','--class-path',cp,str(helper)],capture_output=True,text=True,creationflags=subprocess.CREATE_NO_WINDOW,check=True)
    database,zone,version=r.stdout.strip().split('|')
    if database!='garaman6' or zone!='+07:00':raise RuntimeError('Actual Java DAO target mismatch')
    command=subprocess.run(['powershell.exe','-NoProfile','-Command',f"(Get-CimInstance Win32_Process -Filter 'ProcessId={int(state['pid'])}').CommandLine"],capture_output=True,text=True,creationflags=subprocess.CREATE_NO_WINDOW,check=True).stdout
    if str(config) not in command or 'tomcat-base' not in command:raise RuntimeError('Main JVM does not use this config/base')
    expected=json.loads((ROOT/'sql/seed-expected.json').read_text(encoding='utf-8'));checks=[]
    def check(name,value):
        checks.append({'name':name,'pass':bool(value)})
        if not value:raise AssertionError(name)
    with connect('garaman6') as con:
        before=fingerprints(con,'garaman6');signature=verify_schema(con,'garaman6');metadata=shape(con,'garaman6')
    check('WAR hash matches deployed main WAR',hashlib.sha256((ROOT/'target/garaman6.war').read_bytes()).digest()==hashlib.sha256((ROOT/'.runtime/tomcat-base/webapps/garaman6.war').read_bytes()).digest())
    try:
        with sync_playwright() as pw:
            browser=pw.chromium.launch(executable_path=os.environ.get('GARAMAN_CHROME',r'C:\Program Files\Google\Chrome\Application\chrome.exe'),headless=True)
            for user in ['customer6','manager6']:
                context=browser.new_context(viewport={'width':1440,'height':1000});page=context.new_page()
                # Only POST permitted here is session login. No booking/vehicle/appointment POST.
                page.route('**/*',lambda route:route.abort() if route.request.method=='POST' and urlparse(route.request.url).path!='/garaman6/login' else route.continue_())
                check(user+' login GET',page.goto(base+'/login').status==200)
                page.locator('#username').fill(user);page.locator('#password').fill('GaraMan6@Demo')
                with page.expect_navigation():page.get_by_role('button',name='Đăng nhập',exact=False).click()
                if user=='customer6':
                    check('Customer booking GET on migrated main',page.goto(base+'/booking').status==200 and page.locator('#bookingForm').count()==1)
                    page.screenshot(path=str(out/'booking.png'),full_page=True)
                else:
                    params={'action':'list','statisticType':'ALL','startDate':expected['startDate'],'endDate':expected['endDate'],'page':1}
                    check('Revenue ALL on migrated main',page.goto(base+'/revenue?'+urlencode(params)).status==200 and page.locator('#revenueTable tbody tr').count()==20)
                    page.screenshot(path=str(out/'revenue.png'),full_page=True)
                    for typ,code,lifetime in [('SERVICE','DV0001',5),('PART','PT0005',3)]:
                        params.update(itemType=typ,itemCode=code);params.pop('action',None)
                        check(typ+' detail on migrated main',page.goto(base+'/service-part?'+urlencode(params)).status==200 and str(lifetime) in page.locator('#lifetimeUsageCount').inner_text())
                    params.update(action='invoice',usageId=1)
                    response=page.goto(base+'/service-part?'+urlencode(params))
                    check('Invoice redirect and complete detail on main',response.status==200 and '/invoice?' in page.url)
                    body=page.locator('main').inner_text()
                    check('Main invoice shows salesperson and separate cashier','Lê Thị Lan' in body and 'Vũ Thu Ngân' in body and 'Nhân viên bán hàng' in body and 'Nhân viên thu tiền' in body)
                    check('Main invoice has no rating','đánh giá' not in body.lower())
                    page.screenshot(path=str(out/'invoice.png'),full_page=True)
                    check('Main test-runtime is disabled',context.request.get(base+'/test-runtime').status==404)
                context.close()
            browser.close()
    finally:
        with connect('garaman6') as con:after=fingerprints(con,'garaman6')
        result={'actualJavaDatabase':database,'sessionTimeZone':zone,'serverVersion':version,'mainJvmUsesMainConfig':True,'url':base+'/login','schemaHash':signature,'schema':metadata,'checks':checks,'businessTablesUnchanged':before==after,'before':before,'after':after}
        write_json(out/'results.json',result)
        if before!=after:raise RuntimeError('Read-only verification changed business rows')
    print(json.dumps({'checks':len(checks),'allPass':all(c['pass'] for c in checks),'mainDatabase':database,'businessTablesUnchanged':True,'schemaHash':signature},ensure_ascii=True))
if __name__=='__main__':main()
