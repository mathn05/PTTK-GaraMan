"""End-to-end checks against the actual local Windows Tomcat, using installed Chrome."""
from pathlib import Path
from datetime import datetime,timedelta,timezone
from urllib.parse import urlencode,urlparse,parse_qs
import json, os, re, time, traceback
from decimal import Decimal
from test_support import GuardedTest
from concurrent.futures import ThreadPoolExecutor
from playwright.sync_api import sync_playwright

ROOT=Path(__file__).resolve().parents[1]
OUT=Path(os.environ.get('GARAMAN_TEST_OUT',str(ROOT/'docs/final/evidence/browser')))
OUT.mkdir(parents=True,exist_ok=True)
state=json.loads((ROOT/'.local/tomcat-test.json').read_text())
if state.get('mode')!='test':raise RuntimeError('Refuse the main Tomcat; start run_tomcat.py start --test')
BASE=os.environ.get('GARAMAN_BASE_URL',f'http://127.0.0.1:{state["port"]}/garaman6')
T=None
expected=json.loads((ROOT/'sql/seed-expected.json').read_text(encoding='utf-8'))
results=[]

def check(name,condition,expected_value=True,actual=None):
    results.append({'name':name,'pass':bool(condition),'expected':expected_value,'actual':bool(condition) if actual is None else actual})
    print(('PASS ' if condition else 'FAIL ')+name,flush=True)
    if not condition:raise AssertionError(name+': '+str(actual))

def screenshot(page,name):page.screenshot(path=str(OUT/(name+'.png')),full_page=True)
def click(page,locator):
    T.guard()
    with page.expect_navigation(wait_until='load'):locator.click()
def login(page,user):
    page.goto(BASE+'/login');page.locator('#username').fill(user);page.locator('#password').fill('GaraMan6@Demo');click(page,page.get_by_role('button',name='Đăng nhập',exact=False))
def field(page,name):return page.locator(f'[name="{name}"]').first.input_value()
def post(context,url,form):
    T.guard();return context.request.post(url,form=form)
def count_appointments():return T.count_appointments()

def main():
    global T
    T=GuardedTest(BASE)
    with sync_playwright() as pw:
        chrome=os.environ.get('GARAMAN_CHROME',r'C:\Program Files\Google\Chrome\Application\chrome.exe')
        browser=pw.chromium.launch(executable_path=chrome,headless=True)
        customer=browser.new_context(viewport={'width':1280,'height':950});page=customer.new_page()
        errors=[];page.on('pageerror',lambda e:errors.append(str(e)))
        page.goto(BASE+'/booking');check('Unauthenticated booking redirects to login',page.url.endswith('/login'))
        screenshot(page,'01-login')
        page.locator('#username').fill('customer6');page.locator('#password').fill('wrong');click(page,page.get_by_role('button',name='Đăng nhập',exact=False))
        check('Wrong password receives generic login error',page.get_by_role('alert').count()==1)
        login(page,'customer6');check('Login restores previously requested protected booking route',page.locator('#bookingForm').count()==1)
        click(page,page.get_by_role('link',name='QUAY LẠI',exact=False));check('Customer home renders after successful login','CHĂM SÓC CHIẾC XE' in page.locator('main').inner_text())
        check('Customer blocked from manager reports',customer.request.get(BASE+'/revenue').status==403)
        check('POST without CSRF is blocked',post(customer,BASE+'/booking',form={'action':'hold'}).status==403)
        click(page,page.get_by_role('link',name='ĐẶT LỊCH HẸN NGAY'))
        check('Profile fields are readonly',all(page.locator('#'+x).get_attribute('readonly') is not None for x in ['fullName','phone','email']))
        check('Future slots loaded from database',page.locator('[name="appointmentSlot"]').count()>0)
        date=field(page,'appointmentDate');context=field(page,'context');csrf=field(page,'csrfToken')
        day=(datetime.now(timezone(timedelta(hours=7))).date()+timedelta(days=2)).isoformat()
        page.locator('#requestDescription').fill('Kiểm tra phanh, thay dầu động cơ')
        page.locator('#vehicle').select_option('1')
        page.locator('#appointmentDate').fill(day);page.locator('#appointmentDate').dispatch_event('change');page.wait_for_timeout(500)
        check('Changing date keeps entered request and vehicle',field(page,'vehicle')=='1' and field(page,'requestDescription')=='Kiểm tra phanh, thay dầu động cơ')
        click(page,page.get_by_role('button',name='THÊM XE MỚI',exact=False))
        plate='E2E'+str(int(time.time()*1000))[-11:]
        page.locator('#vehicleBrand').select_option('1');page.locator('#vehicleModel').fill('Corolla Cross');page.locator('#licensePlate').fill(plate[:3]+'-'+plate[3:]+'.')
        click(page,page.get_by_role('button',name='Lưu thông tin xe'))
        new_vehicle=field(page,'vehicle')
        if os.environ.get('GARAMAN_TEST_FAIL_AFTER_VEHICLE')=='1':raise RuntimeError('Intentional cleanup verification failure')
        check('Add vehicle returns to booking with new vehicle selected',new_vehicle not in ['', '1'])
        check('Add vehicle preserves date and request',field(page,'appointmentDate')==day and field(page,'requestDescription')=='Kiểm tra phanh, thay dầu động cơ')
        screenshot(page,'02-booking')
        slot=page.locator('[name="appointmentSlot"]').first.get_attribute('value')
        # Server must ignore tampered personal fields and customerId.
        xss='Kiểm tra <script>window.garamanXss=1</script>'
        form={'context':context,'csrfToken':csrf,'action':'hold','vehicle':new_vehicle,'appointmentSlot':slot,'appointmentDate':day,'requestDescription':xss,'fullName':'HACKED','phone':'000','email':'evil@example.com','customerId':'5'}
        response=post(customer,BASE+'/booking',form=form)
        check('Hold form accepts valid ownership and ignores tampered profile',response.status==200 and 'Nguyễn Văn An' in response.text() and 'HACKED' not in response.text())
        page.goto(BASE+'/booking?'+urlencode({'action':'confirm','context':context}));page.wait_for_load_state()
        check('Confirmation escapes user text',page.locator('.multiline').inner_text()==xss and page.evaluate('window.garamanXss') is None)
        check('Confirmation displays hold expiry',page.locator('#holdTimer').count()==1)
        screenshot(page,'03-confirm')
        # Edit releases token; another session can immediately hold the same slot.
        click(page,page.get_by_role('button',name='CHỈNH SỬA',exact=False))
        check('Edit restores selected vehicle and input',field(page,'vehicle')==new_vehicle and field(page,'requestDescription')==xss)
        check('Edit releases chosen slot',page.locator(f'[name="appointmentSlot"][value="{slot}"]').count()==1)
        # Vehicle ownership must be enforced before holding.
        bad=dict(form,vehicle='2',requestDescription='Test quyền sở hữu')
        check('Forged foreign vehicle receives 403',post(customer,BASE+'/booking',form=bad).status==403)
        # Rehold through the browser, then submit the same context concurrently.
        page.goto(BASE+'/booking?'+urlencode({'context':context}));page.locator('#vehicle').select_option(new_vehicle);page.locator('#requestDescription').fill('Kiểm tra phanh, thay dầu động cơ');page.locator(f'[name="appointmentSlot"][value="{slot}"]').check()
        click(page,page.get_by_role('button',name='XÁC NHẬN →',exact=True))
        before=count_appointments();confirmation={'context':context,'csrfToken':field(page,'csrfToken')}
        # Browser fetch issues simultaneous requests with the same authenticated cookie jar.
        statuses=page.evaluate('''async ({url,data,guardUrl,db}) => { const body=new URLSearchParams(data);return await Promise.all(Array.from({length:5},async()=>{const identity=await (await fetch(guardUrl)).json();if(identity.database!==db)throw new Error('Wrong test DB');const r=await fetch(url,{method:'POST',body});return r.status;}));}''',{'url':BASE+'/appointment','data':confirmation,'guardUrl':BASE+'/test-runtime','db':T.db})
        page.goto(BASE+'/booking?'+urlencode({'action':'confirm','context':context}));check('Concurrent double-submit returns successful recorded result',all(s==200 for s in statuses) and page.locator('#appointmentCode').count()==1)
        code=page.locator('#appointmentCode').inner_text();check('Only one appointment persisted after five submissions',count_appointments()==before+1,before+1,count_appointments())
        page.reload();check('Refreshing success retains same appointment code',page.locator('#appointmentCode').inner_text()==code)
        screenshot(page,'04-booking-success')
        # Separate tab/context within the same session cannot consume a different draft.
        another=customer.new_page();another.goto(BASE+'/booking');check('New tab receives a different booking context',field(another,'context')!=context)
        unknown=post(customer,BASE+'/appointment',form={'csrfToken':field(another,'csrfToken'),'context':'not-in-session'})
        check('Unknown booking context is rejected',unknown.status==400)
        # Responsive booking layout.
        another.set_viewport_size({'width':390,'height':844});screenshot(another,'05-booking-mobile')
        check('Mobile booking has no document overflow',another.evaluate('document.documentElement.scrollWidth <= window.innerWidth'))
        another.close()
        # Real manager/report workflow.
        manager=browser.new_context(viewport={'width':1280,'height':950});m=manager.new_page();m.on('pageerror',lambda e:errors.append(str(e)))
        login(m,'manager6');check('Manager login opens manager home','TRANG CHỦ QUẢN LÝ' in m.locator('main').inner_text())
        check('Manager blocked from customer booking',manager.request.get(BASE+'/booking').status==403)
        click(m,m.get_by_role('link',name='XEM BÁO CÁO'));check('Out-of-scope customer statistics is disabled',m.get_by_role('button',name='Thống kê khách hàng',exact=False).is_disabled())
        click(m,m.get_by_role('link',name='Thống kê dịch vụ / phụ tùng',exact=False))
        m.locator('#startDate').fill(expected['startDate']);m.locator('#endDate').fill(expected['endDate']);m.locator('#statisticType').select_option('ALL');screenshot(m,'06-revenue-filter')
        click(m,m.get_by_role('button',name='THỐNG KÊ',exact=False))
        check('ALL report matches independent expected total',Decimal(m.locator('#totalRevenue').get_attribute('data-value'))==Decimal('5741000'),'5741000.00',m.locator('#totalRevenue').get_attribute('data-value'))
        check('Report displays twenty rows and two pages',m.locator('#revenueTable tbody tr[data-item-code]').count()==20 and m.locator('#totalPages').inner_text()=='2')
        check('Revenue sorted with tyre first',m.locator('#revenueTable tbody tr[data-item-code]').first.get_attribute('data-item-code')=='PT0005')
        check('No customer rating displayed on report','Đánh giá' not in m.locator('main').inner_text())
        screenshot(m,'07-revenue-list')
        list_url=m.url;click(m,m.get_by_role('link',name='Trang sau',exact=False));check('Second page has remaining seven rows and full total',m.locator('#revenueTable tbody tr[data-item-code]').count()==7 and Decimal(m.locator('#totalRevenue').get_attribute('data-value'))==Decimal('5741000'));screenshot(m,'08-revenue-page2')
        m.goto(list_url)
        # Open PART before SERVICE; no forced scenario ordering.
        click(m,m.locator('tr[data-item-type="PART"][data-item-code="PT0005"] a'));check('Shared detail renders part branch first',m.get_by_role('heading',name='THÔNG TIN CHI TIẾT PHỤ TÙNG').count()==1)
        check('Part lifetime count differs from filtered history',m.locator('#lifetimeUsageCount').inner_text()=='3 lần' and m.locator('#usageTable tbody tr').count()==2)
        check('Part detail has category brand stock specification image',all(x in m.locator('main').inner_text() for x in ['Michelin','Lốp xe','96 Chiếc','185/65R15']) and m.locator('.part-image').count()==1)
        screenshot(m,'09-part-detail')
        part_url=m.url;click(m,m.locator('#usageTable tbody tr').last.get_by_role('link',name='Xem hóa đơn'));check('Invoice lookup redirects to correct invoice code','invoiceCode=HD0001' in m.url)
        check('Invoice displays all service and part lines',m.locator('#invoiceLines tbody tr[data-type="SERVICE"]').count()==2 and m.locator('#invoiceLines tbody tr[data-type="PART"]').count()==2)
        check('Invoice has salesperson and cashier from different real employees','Lê Thị Lan' in m.locator('#saleStaff').inner_text() and 'Vũ Thu Ngân' in m.locator('#cashier').inner_text())
        check('Invoice total uses all historical lines',Decimal(m.locator('#invoiceTotal').get_attribute('data-value'))==3600000)
        screenshot(m,'10-invoice')
        click(m,m.get_by_role('link',name='Quay lại chi tiết',exact=False));check('Invoice Back preserves part and report context',parse_qs(urlparse(m.url).query).get('itemCode')==['PT0005'] and parse_qs(urlparse(m.url).query).get('startDate')==[expected['startDate']])
        click(m,m.get_by_role('link',name='Quay lại danh sách',exact=False));click(m,m.locator('tr[data-item-type="SERVICE"][data-item-code="DV0001"] a'))
        check('Shared detail renders service with no rating',m.get_by_role('heading',name='THÔNG TIN CHI TIẾT DỊCH VỤ').count()==1 and 'Đánh giá' not in m.locator('main').inner_text())
        check('Service lifetime versus filtered history is correct',m.locator('#lifetimeUsageCount').inner_text()=='5 lần' and m.locator('#usageTable tbody tr').count()==2)
        screenshot(m,'11-service-detail')
        click(m,m.locator('#usageTable tbody tr').last.get_by_role('link',name='Xem hóa đơn'));check('Service usage also opens full same invoice','invoiceCode=HD0001' in m.url and m.locator('#invoiceLines tbody tr').count()==4)
        # Filters, empty result, invalid dates and page clamping all through HTTP/JSP.
        for kind,total in [('SERVICE',2301000),('PART',3440000)]:
            m.goto(BASE+'/revenue?'+urlencode({'action':'list','startDate':expected['startDate'],'endDate':expected['endDate'],'statisticType':kind,'page':999999}))
            check(kind+' total and page clamping',Decimal(m.locator('#totalRevenue').get_attribute('data-value'))==total and int(m.locator('#currentPage').inner_text())==int(m.locator('#totalPages').inner_text()))
        m.goto(BASE+'/revenue?'+urlencode({'action':'list','startDate':'2080-01-01','endDate':'2080-01-31','statisticType':'ALL'}))
        check('Empty report has page 1 / 0 and zero total',m.locator('#currentPage').inner_text()=='1' and m.locator('#totalPages').inner_text()=='0' and Decimal(m.locator('#totalRevenue').get_attribute('data-value'))==0)
        bad=manager.request.get(BASE+'/revenue?'+urlencode({'action':'list','startDate':'2026-10-31','endDate':'2026-10-01','statisticType':'ALL'}));check('Invalid date range receives 400 filter form',bad.status==400 and 'BỘ LỌC THỐNG KÊ' in bad.text())
        # Logout invalidates old access.
        click(page,page.get_by_role('button',name='Đăng xuất'));check('Logout returns to login',page.url.endswith('/login'));r=customer.request.get(BASE+'/booking');check('Logged-out session cannot access booking','/login' in r.url)
        check('Browser JavaScript has no uncaught errors',len(errors)==0,[],errors)
        browser.close()

if __name__=='__main__':
    failure=None
    try:main()
    except Exception:
        failure=traceback.format_exc();results.append({'name':'Unhandled test failure','pass':False,'actual':failure})
    finally:
        cleanup=None
        if T is not None:
            try:cleanup=T.cleanup()
            except Exception:failure=(failure or '')+'\nCleanup: '+traceback.format_exc()
        report={'target':BASE,'platform':'native Windows Chrome','timestamp':datetime.now().isoformat(),'checks':results,'cleanup':cleanup,'error':failure}
        (OUT/'browser-results.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    if failure:print(failure);raise SystemExit(1)
