"""Generate auditable SQL fixtures and independent expected totals; no DB writes here."""
from pathlib import Path
from datetime import datetime, timezone, timedelta
from decimal import Decimal
from collections import defaultdict
import hashlib, secrets, base64, json, calendar

def password_hash(password):
    salt=secrets.token_bytes(16)
    key=hashlib.pbkdf2_hmac('sha256',password.encode(),salt,600000,32)
    return 'pbkdf2-sha256$v1$600000$'+base64.b64encode(salt).decode()+'$'+base64.b64encode(key).decode()

def generate():
    today=datetime.now(timezone(timedelta(hours=7))).date()
    end=today.replace(day=1)-timedelta(days=1)
    start=end.replace(day=1)
    before=start-timedelta(days=1)
    after=end+timedelta(days=1)
    def q(v):
        if v is None:return 'NULL'
        if isinstance(v,(int,Decimal)):return str(v)
        return "'"+str(v).replace('\\','\\\\').replace("'","''")+"'"
    sql=['-- Demo fixtures for a NEW local/test database only. Generated '+today.isoformat()]
    def ins(table,cols,rows):
        sql.append('INSERT INTO '+table+'('+cols+') VALUES\n'+',\n'.join('('+','.join(q(v) for v in row)+')' for row in rows)+';')
    members=[]
    for id,name,phone,user,status in [(1,'Nguyễn Văn An','0912345678','customer6','ACTIVE'),(2,'Trần Minh Quản','0901234567','manager6','ACTIVE'),(3,'Lê Thị Lan','0902345678',None,'ACTIVE'),(4,'Phạm Văn Kỹ','0903456789',None,'ACTIVE'),(5,'Nguyễn Văn Bình','0904567890','customer_other6','ACTIVE'),(6,'Nhân viên kho','0905678901',None,'ACTIVE'),(7,'Tài khoản tạm khóa','0906789012','inactive6','INACTIVE'),(8,'Vũ Thu Ngân','0907890123',None,'ACTIVE')]:
        members.append((id,name,phone,(user or f'member{id}')+'@example.com','Hà Nội',user,password_hash('GaraMan6@Demo') if user else None,status if user else None))
    ins('tblMember6','id,fullName,phone,email,address,username,password,status',members)
    ins('tblCustomer6','tblMember6id,customerCode',[(1,'KH0001'),(5,'KH0002'),(7,'KH0003')])
    ins('tblEmployee6','tblMember6id,employeeCode,jobTitle',[(2,'NV0001','MANAGER'),(3,'NV0002','SALE_STAFF'),(4,'NV0003','TECHNICIAN'),(6,'NV0004','WAREHOUSE_STAFF'),(8,'NV0005','SALE_STAFF')])
    ins('tblVehicleBrand6','id,name',[(1,'Toyota'),(2,'Honda'),(3,'Hyundai')])
    ins('tblVehicle6','id,licensePlate,vehicleModel,currentMileage,manufactureYear,tblCustomer6tblMember6id,tblVehicleBrand6id',[(1,'30A12345','Vios',35000,2022,1,1),(2,'51G98765','Civic',None,None,5,2)])
    ins('tblPartBrand6','id,name',[(1,'Michelin'),(2,'Bosch')])
    ins('tblCategory6','id,categoryName',[(1,'Lốp xe'),(2,'Lọc dầu'),(3,'Vật tư')])
    services=[(1,'DV0001','Thay nhớt động cơ','Thay dầu và kiểm tra động cơ.',650000),(2,'DV0002','Bảo dưỡng phanh xe','Kiểm tra, vệ sinh và bảo dưỡng hệ thống phanh.',900000),(3,'DV0003','Cân chỉnh thước lái','Kiểm tra góc đặt bánh xe.',400000)]
    services += [(100+i,f'DV{100+i:04d}',f'Kiểm tra hạng mục {i+1:02d}','Hạng mục kiểm tra định kỳ.',50000+i) for i in range(22)]
    ins('tblService6','id,serviceCode,serviceName,description,unitPrice',services)
    parts=[(1,'PT0005','Lốp Michelin Energy XM2+','Lốp du lịch, bám đường tốt.','Chiếc','185/65R15; tải 88H',1500000,'/assets/part-tyre.svg',1,1),(2,'PT0001','Lọc dầu Bosch','Lọc dầu động cơ.','Chiếc','Tương thích Toyota Vios',220000,'/assets/part-filter.svg',2,2),(3,'DV0001','Vòng đệm xả dầu','Mã trùng chuỗi dịch vụ để kiểm tra phân loại.','Chiếc','14 mm',30000,None,2,3)]
    ins('tblSparePart6','id,partCode,partName,description,unit,technicalSpec,unitPrice,image,tblPartBrand6id,tblCategory6id',parts)
    times={1:f'{start} 00:00:00',2:f'{end} 23:59:59.999999',3:f'{before} 23:59:59.999999',4:f'{start.replace(day=15)} 12:00:00',5:f'{after} 00:00:00',6:f'{start.replace(day=10)} 10:00:00',7:f'{start.replace(day=12)} 10:00:00',8:f'{start.replace(day=13)} 10:00:00'}
    ins('tblRepairOrder6','id,repairOrderCode,createTime,completionTime,vehicleCondition,repairDescription,repairResult,repairStatus,tblVehicle6id,tblCustomer6tblMember6id,tblEmployee6tblMember6id',[(i,f'PSC{i:04d}',t,t,'Kiểm tra theo yêu cầu','Bảo dưỡng định kỳ','Đã hoàn tất','COMPLETED',2 if i==2 else 1,5 if i==2 else 1,3) for i,t in times.items()])
    # Tuples (id, service/part id, order id, historical unit price [, quantity]).
    su=[(1,1,1,500000),(2,2,1,800000),(3,1,2,550000),(4,1,3,450000),(5,1,4,700000),(6,1,5,600000),(7,3,7,350000)]
    su += [(100+i,100+i,6,10000+i*1000) for i in range(22)]
    pu=[(1,1,1,1000000,2),(2,2,1,100000,3),(3,1,2,1100000,1),(4,1,3,900000,1),(5,2,4,110000,4),(6,2,5,120000,1),(7,3,6,20000,2),(8,2,8,125000,1)]
    # usageTime intentionally differs from issueDate for last-day invoice.
    usage_time=lambda oid: f'{after} 01:00:00' if oid==2 else times[oid]
    ins('tblServiceUsage6','id,tblService6id,tblRepairOrder6id,saleUnitPrice,usageTime,note,tblEmployee6tblMember6id',[(i,s,o,p,usage_time(o),'Giá tại thời điểm sửa chữa',4) for i,s,o,p in su])
    ins('tblPartUsage6','id,tblSparePart6id,tblRepairOrder6id,saleUnitPrice,usedQuantity,usageTime,note,tblEmployee6tblMember6id',[(i,s,o,p,n,usage_time(o),'Giá bán lịch sử',4) for i,s,o,p,n in pu])
    totals=defaultdict(int)
    for _,s,o,p in su:totals[o]+=p
    for _,s,o,p,n in pu:totals[o]+=p*n
    # UNPAID has no payment group. Historical PENDING/FAILED are archived during migration.
    paid_orders={1,2,3,5,6}
    ins('tblInvoice6','id,invoiceCode,issueDate,tblRepairOrder6id,paymentAmount,paymentTime,paymentMethod,paymentStatus,tblEmployee6tblMember6id,transactionReference',[(o,f'HD{o:04d}',times[o],o,totals[o] if o in paid_orders else None,times[o] if o in paid_orders else None,'BANK_TRANSFER' if o in paid_orders else None,'PAID' if o in paid_orders else 'UNPAID',8 if o in paid_orders else None,f'DEMO-{o}' if o in paid_orders else None) for o in times if o!=7])
    ins('tblSupplier6','id,supplierName,phone',[(1,'Nhà cung cấp demo','0241234567')])
    ins('tblPartReceipt6','id,receiptDate,tblSupplier6id,tblEmployee6tblMember6id',[(1,f'{before} 08:00:00',1,6)])
    ins('tblReceiptDetail6','receiptQuantity,purchaseUnitPrice,tblPartReceipt6id,tblSparePart6id',[(100,800000,1,1),(100,80000,1,2),(100,10000,1,3)])
    # Fixed hand-checked core: tyre 3.1m/2 events, oil 1.05m/2, brake .8m/1,
    # filter .3m/1, washer .04m/1; extras sum 451000 over 22 service events.
    expected={'startDate':str(start),'endDate':str(end),'core':{'PART:PT0005':{'usageCount':2,'totalRevenue':3100000},'SERVICE:DV0001':{'usageCount':2,'totalRevenue':1050000},'SERVICE:DV0002':{'usageCount':1,'totalRevenue':800000},'PART:PT0001':{'usageCount':1,'totalRevenue':300000},'PART:DV0001':{'usageCount':1,'totalRevenue':40000}},'SERVICE':{'rows':24,'usageCount':25,'totalRevenue':2301000},'PART':{'rows':3,'usageCount':4,'totalRevenue':3440000},'ALL':{'rows':27,'usageCount':29,'totalRevenue':5741000},'lifetime':{'SERVICE:DV0001':6,'PART:PT0005':4,'PART:PT0001':5},'HD0001':{'totalAmount':3600000,'serviceLines':2,'partLines':2,'saleStaff':'Lê Thị Lan','cashier':'Vũ Thu Ngân'}}
    # Correct lifetime values independently checked against fixture rows, not SQL.
    expected['lifetime']['SERVICE:DV0001']=5
    expected['lifetime']['PART:PT0005']=3
    expected['lifetime']['PART:PT0001']=4
    Path('sql/seed.sql').write_text('\n'.join(sql)+'\n',encoding='utf-8')
    Path('sql/seed-expected.json').write_text(json.dumps(expected,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('Seed prepared:',start,'to',end,'expected ALL=5,741,000 VND (27 rows).')
if __name__=='__main__':generate()
