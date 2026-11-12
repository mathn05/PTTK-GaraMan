"""Regression of V3 schema, server-side JSP packaging, old D049-D053 and ordered report drilldown."""
from pathlib import Path
from datetime import datetime,timedelta,timezone
from decimal import Decimal
from urllib.parse import urlencode
import json, os, traceback
from playwright.sync_api import sync_playwright
import browser_test as flow
from test_support import GuardedTest
from schema_tools import ROOT

OUT=ROOT/'docs/final/evidence/regression';OUT.mkdir(parents=True,exist_ok=True)
checks=[];T=None
def check(name,ok,actual=None):
    checks.append({'name':name,'pass':bool(ok),'actual':actual})
    print(('PASS ' if ok else 'FAIL ')+name,flush=True)
    if not ok:raise AssertionError(name+': '+str(actual))
def main():
    global T
    T=GuardedTest(flow.BASE);flow.T=T
    with sync_playwright() as pw:
        browser=pw.chromium.launch(executable_path=os.environ.get('GARAMAN_CHROME',r'C:\Program Files\Google\Chrome\Application\chrome.exe'),headless=True)
        customer=browser.new_context(viewport={'width':1440,'height':1000});page=customer.new_page()
        flow.login(page,'customer6');page.goto(flow.BASE+'/booking')
        context=flow.field(page,'context');csrf=flow.field(page,'csrfToken')
        check('Actual application and SQL use V3 test schema',T.db=='garaman6_v3_it',{'database':T.db,'schemaHash':T.schema_hash})
        forbidden=T.query("SELECT table_name FROM information_schema.tables WHERE table_schema=DATABASE() AND LOWER(table_name) IN ('tblpayment6','tblcodesequence6','tblschemaversion6')")
        check('No Payment/CodeSequence/SchemaVersion physical table',not forbidden)
        vehicle_page=flow.BASE+'/vehicle?'+urlencode({'context':context})
        page.goto(vehicle_page)
        for brand in ['abc','','-1','0','999999','1 OR 1=1']:
            form={'context':context,'csrfToken':csrf,'vehicleBrand':brand,'vehicleModel':'Model <b>safe</b>','licensePlate':'INVALIDCASE','manufactureYear':''}
            response=flow.post(customer,flow.BASE+'/vehicle',form)
            check('Invalid brand '+repr(brand)+' returns 400 with escaped retained input',response.status==400 and 'Model &lt;b&gt;safe&lt;/b&gt;' in response.text(),response.status)
        before_vehicles=T.query('SELECT COUNT(*) FROM tblVehicle6')[0][0]
        response=flow.post(customer,flow.BASE+'/vehicle',{'context':context,'csrfToken':csrf,'action':'forged','id':'999'})
        check('Unsupported vehicle action is rejected',response.status==400 and T.query('SELECT COUNT(*) FROM tblVehicle6')[0][0]==before_vehicles,response.status)
        response=flow.post(customer,flow.BASE+'/booking',{'context':context,'csrfToken':csrf,'action':'forged'})
        check('Unsupported appointment action is rejected',response.status==400,response.status)
        check('JSP only renders and has no entity packaging or DAO access',all(not any(marker in p.read_text(encoding='utf-8') for marker in ['jsp:useBean','jsp:setProperty','garaman.dao.','<% ']) for p in (ROOT/'src/main/webapp').rglob('*.jsp')))
        plate='V3'+str(int(datetime.now().timestamp()*1000))[-12:]
        response=flow.post(customer,flow.BASE+'/vehicle',{'context':context,'csrfToken':csrf,'vehicleBrand':'1','vehicleModel':'Regression','licensePlate':plate,'manufactureYear':'','currentMileage':'9999','customerId':'5','id':'9999','vehicleCode':'FORGED'})
        check('Servlet-packaged new vehicle persists and redirects to booking',response.status==200 and 'bookingForm' in response.text(),response.status)
        page.goto(flow.BASE+'/booking?'+urlencode({'context':context}))
        vehicle_id=int(flow.field(page,'vehicle'))
        data=T.query('SELECT currentMileage,manufactureYear,tblCustomer6tblMember6id FROM tblVehicle6 WHERE id=%s',(vehicle_id,))[0]
        check('Unknown mileage/year remain NULL and customer is server-owned',data==(None,None,1),str(data))
        check('Opening-hours caption has no hardcoded schedule','08:00–17:00' not in page.locator('main').inner_text())
        day=(datetime.now(timezone(timedelta(hours=7))).date()+timedelta(days=2)).isoformat()
        with page.expect_response(lambda r:'/booking?' in r.url and 'action=slots' in r.url,timeout=5000) as loaded:
            page.locator('#appointmentDate').fill(day)
        import re
        ids=re.findall(r'name="appointmentSlot" value="(\d+)"',loaded.value.text())
        if not ids:raise AssertionError('No available slot in loaded date response')
        page.locator('[name="appointmentSlot"][value="'+ids[0]+'"]').wait_for(state='attached')
        slot_date=T.query('SELECT DATE(startTime) FROM tblAppointmentSlot6 WHERE id=%s',(int(ids[0]),))[0][0]
        check('Changing date actually fetches and renders slots of the selected day',str(slot_date)==day and page.locator('#slotError').count()==0,str(slot_date))
        page.locator('#vehicle').select_option(str(vehicle_id));page.locator('#requestDescription').fill('Clock skew regression')
        selected=page.locator('[name="appointmentSlot"][value="'+ids[0]+'"]');slot=int(selected.get_attribute('value'));selected.check()
        flow.click(page,page.get_by_role('button',name='XÁC NHẬN →',exact=True))
        timer=page.locator('#holdTimer');ttl=timer.get_attribute('data-remaining') if timer.count() else None
        page.screenshot(path=str(OUT/'confirmation-ttl.png'),full_page=True)
        check('Servlet-packaged draft reaches confirmation with DB TTL',timer.count()==1 and 0<int(ttl)<=180000,{'ttl':ttl,'url':page.url,'slot':slot,'body':page.locator('main').inner_text()[:400]})
        confirm_url=page.url
        # Hold POST forwards, so retain an explicit GET URL for reload of the same server context.
        confirm_url=flow.BASE+'/booking?'+urlencode({'action':'confirm','context':context})
        page.add_init_script('const originalNow=Date.now;Date.now=()=>originalNow()+600000;');page.goto(confirm_url)
        ahead=page.evaluate('Date.now()-new Date().getTime()')
        check('Client clock +10 minutes does not disable a valid hold',ahead>599000 and page.locator('#confirmBooking').is_enabled(),ahead)
        second=customer.new_page();second.add_init_script('const originalNow=Date.now;Date.now=()=>originalNow()-600000;');second.goto(confirm_url)
        behind=second.evaluate('Date.now()-new Date().getTime()')
        check('Client clock -10 minutes does not disable a valid hold',behind< -599000 and second.locator('#confirmBooking').is_enabled(),behind)
        T.execute("UPDATE tblAppointmentSlot6 SET holdExpiresAt=NOW(6)+INTERVAL 1 SECOND WHERE id=%s AND status='HELD'",(slot,))
        second.reload();second.wait_for_timeout(1400)
        check('Real server expiry disables elapsed TTL',second.locator('#confirmBooking').is_disabled())
        before=T.count_appointments()
        response=flow.post(customer,flow.BASE+'/appointment',{'context':context,'csrfToken':csrf})
        check('Server rejects expired confirmation with 409 and no INSERT',response.status==409 and T.count_appointments()==before,response.status)
        flow.post(customer,flow.BASE+'/booking',{'context':context,'csrfToken':csrf,'action':'edit'})
        manager=browser.new_context(viewport={'width':1440,'height':1000});m=manager.new_page();flow.login(m,'manager6')
        flow.click(m,m.locator('a[href*="action=menu"]'));flow.click(m,m.locator('a[href*="action=filter"]'))
        m.locator('#startDate').fill('2026-10-01');m.locator('#endDate').fill('2026-10-31');m.locator('#statisticType').select_option('ALL')
        flow.click(m,m.locator('main form button[type="submit"]'))
        flow.click(m,m.locator('tr[data-item-type="SERVICE"][data-item-code="DV0001"] a'))
        check('Ordered SERVICE first: lifetime5 and filtered2',m.locator('#lifetimeUsageCount').inner_text()=='5 lần' and m.locator('#usageTable tbody tr').count()==2)
        m.screenshot(path=str(OUT/'service-first.png'),full_page=True)
        flow.click(m,m.locator('main a[href*="/revenue"][href*="action=list"]'))
        check('Back keeps ALL and original report page','statisticType=ALL' in m.url and 'page=1' in m.url)
        flow.click(m,m.locator('tr[data-item-type="PART"][data-item-code="PT0005"] a'))
        check('Ordered PART second: lifetime3 and filtered2',m.locator('#lifetimeUsageCount').inner_text()=='3 lần' and m.locator('#usageTable tbody tr').count()==2)
        detail=m.url;m.screenshot(path=str(OUT/'part-second.png'),full_page=True)
        flow.click(m,m.locator('a[href$="usageId=1"]'))
        check('Common invoice is HD0001 with four complete lines','invoiceCode=HD0001' in m.url and m.locator('#invoiceLines tbody tr').count()==4)
        check('Salesperson and cashier remain different roles','Lê Thị Lan' in m.locator('#saleStaff').inner_text() and 'Vũ Thu Ngân' in m.locator('#cashier').inner_text())
        m.screenshot(path=str(OUT/'invoice-full.png'),full_page=True)
        filters={'action':'invoice','itemType':'PART','itemCode':'PT0005','startDate':'2026-10-01','endDate':'2026-10-31','statisticType':'ALL','page':'1','usagePage':'1'}
        for usage,expected in [('',400),('abc',400),('0',400),('-1',400),('999999',404),('2',404)]:
            response=manager.request.get(flow.BASE+'/service-part?'+urlencode(dict(filters,usageId=usage)))
            check('Invoice usageId '+repr(usage)+' has status '+str(expected),response.status==expected,response.status)
        response=manager.request.get(flow.BASE+'/service-part?'+urlencode(dict(filters,usageId='1',startDate='2026-09-01',endDate='2026-09-30')))
        check('Invoice lookup still enforces selected period',response.status==404,response.status)
        T.execute("UPDATE tblServiceUsage6 SET saleUnitPrice=%s WHERE id=1",(Decimal('9007199254740991.01'),))
        T.execute("UPDATE tblInvoice6 SET paymentAmount=%s WHERE id=1",(Decimal('9007199257840991.01'),))
        m.goto(flow.BASE+'/invoice?invoiceCode=HD0001')
        value=m.locator('#invoiceTotal').get_attribute('data-value');text=m.locator('#invoiceTotal').inner_text()
        check('Large invoice preserves exact cents in model and rendered text',Decimal(value)==Decimal('9007199257840991.01') and '9.007.199.257.840.991,01' in text,{'dataValue':value,'text':text})
        m.screenshot(path=str(OUT/'large-money-fixed.png'),full_page=True)
        m.goto(flow.BASE+'/revenue?'+urlencode({'action':'list','startDate':'2026-10-01','endDate':'2026-10-31','statisticType':'ALL'}))
        check('Large whole-report total has no double conversion',Decimal(m.locator('#totalRevenue').get_attribute('data-value'))==Decimal('9007199259981991.01'),m.locator('#totalRevenue').get_attribute('data-value'))
        m.goto(flow.BASE+'/invoice?invoiceCode=HD0004')
        check('Unpaid invoice has no fabricated Payment/cashier',m.locator('#cashier').count()==0 and 'Hóa đơn chưa thanh toán.' in m.locator('main').inner_text())
        # Deliberately exceed Integer's cache for both IDs being compared.
        large=200000+int(datetime.now().timestamp())%100000
        user='large_boxed_'+str(large)
        T.execute("INSERT INTO tblMember6(id,fullName,phone,username,password,status) SELECT %s,'Large ID customer','0900000000',%s,password,'ACTIVE' FROM tblMember6 WHERE id=1",(large,user))
        T.execute("INSERT INTO tblCustomer6(tblMember6id,customerCode) VALUES(%s,%s)",(large,'BOX'+str(large)))
        T.execute("INSERT INTO tblVehicle6(id,licensePlate,vehicleModel,tblCustomer6tblMember6id,tblVehicleBrand6id) VALUES(%s,%s,'Large IDs',%s,1)",(large,'BOX'+str(large),large))
        large_context=browser.new_context();large_page=large_context.new_page();flow.login(large_page,user);large_page.goto(flow.BASE+'/booking')
        lc=flow.field(large_page,'context');token=flow.field(large_page,'csrfToken')
        available=T.query("SELECT id,DATE(startTime) FROM tblAppointmentSlot6 WHERE id>127 AND startTime>NOW(6) AND DATE(startTime)<=CURDATE()+INTERVAL 29 DAY AND status='AVAILABLE' AND id NOT IN (SELECT activeSlotId FROM tblAppointment6 WHERE activeSlotId IS NOT NULL) ORDER BY startTime LIMIT 1")
        if not available:raise AssertionError('No high-ID future slot fixture')
        sid,day=available[0]
        held=flow.post(large_context,flow.BASE+'/booking',{'context':lc,'csrfToken':token,'action':'hold','vehicle':str(large),'appointmentSlot':str(sid),'appointmentDate':str(day),'requestDescription':'Boxed identity equality'})
        check('Customer and vehicle IDs above 127 pass ownership by value',held.status==200 and 'holdTimer' in held.text(),held.status)
        saved=flow.post(large_context,flow.BASE+'/appointment',{'context':lc,'csrfToken':token})
        check('Large-ID draft confirms without recreation and with correct owner',saved.status==200 and T.query("SELECT COUNT(*) FROM tblAppointment6 WHERE tblCustomer6tblMember6id=%s AND tblVehicle6id=%s AND tblAppointmentSlot6id=%s",(large,large,sid))[0][0]==1,saved.status)
        T.execute("INSERT INTO tblPartUsage6(id,usedQuantity,usageTime,saleUnitPrice,tblSparePart6id,tblRepairOrder6id,tblEmployee6tblMember6id) VALUES(%s,1,NOW(6),0,1,1,4)",(large,))
        response=manager.request.get(flow.BASE+'/service-part?'+urlencode(dict(filters,usageId=str(large))))
        check('Usage ID above 127 passes membership comparison by value',response.status==200 and 'invoiceCode=HD0001' in response.url,response.status)
        browser.close()
if __name__=='__main__':
    failure=None
    try:main()
    except Exception:failure=traceback.format_exc();print(failure)
    finally:
        cleanup=None
        if T is not None:
            try:cleanup=T.cleanup()
            except Exception:failure=(failure or '')+'\nCleanup: '+traceback.format_exc()
        report={'target':flow.BASE,'checks':checks,'cleanup':cleanup,'error':failure,'timestamp':datetime.now().isoformat()}
        (OUT/'results.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    if failure:raise SystemExit(1)
