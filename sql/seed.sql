-- Demo fixtures for a NEW local/test database only. Generated 2026-11-12
INSERT INTO tblMember6(id,fullName,phone,email,address,username,password,status) VALUES
(1,'Nguyễn Văn An','0912345678','customer6@example.com','Hà Nội','customer6','pbkdf2-sha256$v1$600000$yZIJfNX7RoTvftrPLfIHLw==$X3+q3Bw95W/rEWUKNwT9Zq3SrXRNec6FV4WHtVoeSVI=','ACTIVE'),
(2,'Trần Minh Quản','0901234567','manager6@example.com','Hà Nội','manager6','pbkdf2-sha256$v1$600000$MMVC2O/BzmLAaq1aiZ/RRw==$iqbq4ppfzj5LEC363h2snTrQ6AJ4xAIUvR/pd0m8N2s=','ACTIVE'),
(3,'Lê Thị Lan','0902345678','member3@example.com','Hà Nội',NULL,NULL,NULL),
(4,'Phạm Văn Kỹ','0903456789','member4@example.com','Hà Nội',NULL,NULL,NULL),
(5,'Nguyễn Văn Bình','0904567890','customer_other6@example.com','Hà Nội','customer_other6','pbkdf2-sha256$v1$600000$ohsohrvkDAxRXewjQUz+eg==$MOz5eOoxVoWgGG62IkOpX5Gw3e37/fp0X5SMfqn+CJc=','ACTIVE'),
(6,'Nhân viên kho','0905678901','member6@example.com','Hà Nội',NULL,NULL,NULL),
(7,'Tài khoản tạm khóa','0906789012','inactive6@example.com','Hà Nội','inactive6','pbkdf2-sha256$v1$600000$rCZ4WU7omnBinlNYN6llbA==$XuEKwE+iZvQkPeIm8GFcnQPZEtF/YS3Nk7Jt5+PbghE=','INACTIVE'),
(8,'Vũ Thu Ngân','0907890123','member8@example.com','Hà Nội',NULL,NULL,NULL);
INSERT INTO tblCustomer6(tblMember6id,customerCode) VALUES
(1,'KH0001'),
(5,'KH0002'),
(7,'KH0003');
INSERT INTO tblEmployee6(tblMember6id,employeeCode,jobTitle) VALUES
(2,'NV0001','MANAGER'),
(3,'NV0002','SALE_STAFF'),
(4,'NV0003','TECHNICIAN'),
(6,'NV0004','WAREHOUSE_STAFF'),
(8,'NV0005','SALE_STAFF');
INSERT INTO tblVehicleBrand6(id,name) VALUES
(1,'Toyota'),
(2,'Honda'),
(3,'Hyundai');
INSERT INTO tblVehicle6(id,licensePlate,vehicleModel,currentMileage,manufactureYear,tblCustomer6tblMember6id,tblVehicleBrand6id) VALUES
(1,'30A12345','Vios',35000,2022,1,1),
(2,'51G98765','Civic',NULL,NULL,5,2);
INSERT INTO tblPartBrand6(id,name) VALUES
(1,'Michelin'),
(2,'Bosch');
INSERT INTO tblCategory6(id,categoryName) VALUES
(1,'Lốp xe'),
(2,'Lọc dầu'),
(3,'Vật tư');
INSERT INTO tblService6(id,serviceCode,serviceName,description,unitPrice) VALUES
(1,'DV0001','Thay nhớt động cơ','Thay dầu và kiểm tra động cơ.',650000),
(2,'DV0002','Bảo dưỡng phanh xe','Kiểm tra, vệ sinh và bảo dưỡng hệ thống phanh.',900000),
(3,'DV0003','Cân chỉnh thước lái','Kiểm tra góc đặt bánh xe.',400000),
(100,'DV0100','Kiểm tra hạng mục 01','Hạng mục kiểm tra định kỳ.',50000),
(101,'DV0101','Kiểm tra hạng mục 02','Hạng mục kiểm tra định kỳ.',50001),
(102,'DV0102','Kiểm tra hạng mục 03','Hạng mục kiểm tra định kỳ.',50002),
(103,'DV0103','Kiểm tra hạng mục 04','Hạng mục kiểm tra định kỳ.',50003),
(104,'DV0104','Kiểm tra hạng mục 05','Hạng mục kiểm tra định kỳ.',50004),
(105,'DV0105','Kiểm tra hạng mục 06','Hạng mục kiểm tra định kỳ.',50005),
(106,'DV0106','Kiểm tra hạng mục 07','Hạng mục kiểm tra định kỳ.',50006),
(107,'DV0107','Kiểm tra hạng mục 08','Hạng mục kiểm tra định kỳ.',50007),
(108,'DV0108','Kiểm tra hạng mục 09','Hạng mục kiểm tra định kỳ.',50008),
(109,'DV0109','Kiểm tra hạng mục 10','Hạng mục kiểm tra định kỳ.',50009),
(110,'DV0110','Kiểm tra hạng mục 11','Hạng mục kiểm tra định kỳ.',50010),
(111,'DV0111','Kiểm tra hạng mục 12','Hạng mục kiểm tra định kỳ.',50011),
(112,'DV0112','Kiểm tra hạng mục 13','Hạng mục kiểm tra định kỳ.',50012),
(113,'DV0113','Kiểm tra hạng mục 14','Hạng mục kiểm tra định kỳ.',50013),
(114,'DV0114','Kiểm tra hạng mục 15','Hạng mục kiểm tra định kỳ.',50014),
(115,'DV0115','Kiểm tra hạng mục 16','Hạng mục kiểm tra định kỳ.',50015),
(116,'DV0116','Kiểm tra hạng mục 17','Hạng mục kiểm tra định kỳ.',50016),
(117,'DV0117','Kiểm tra hạng mục 18','Hạng mục kiểm tra định kỳ.',50017),
(118,'DV0118','Kiểm tra hạng mục 19','Hạng mục kiểm tra định kỳ.',50018),
(119,'DV0119','Kiểm tra hạng mục 20','Hạng mục kiểm tra định kỳ.',50019),
(120,'DV0120','Kiểm tra hạng mục 21','Hạng mục kiểm tra định kỳ.',50020),
(121,'DV0121','Kiểm tra hạng mục 22','Hạng mục kiểm tra định kỳ.',50021);
INSERT INTO tblSparePart6(id,partCode,partName,description,unit,technicalSpec,unitPrice,image,tblPartBrand6id,tblCategory6id) VALUES
(1,'PT0005','Lốp Michelin Energy XM2+','Lốp du lịch, bám đường tốt.','Chiếc','185/65R15; tải 88H',1500000,'/assets/part-tyre.svg',1,1),
(2,'PT0001','Lọc dầu Bosch','Lọc dầu động cơ.','Chiếc','Tương thích Toyota Vios',220000,'/assets/part-filter.svg',2,2),
(3,'DV0001','Vòng đệm xả dầu','Mã trùng chuỗi dịch vụ để kiểm tra phân loại.','Chiếc','14 mm',30000,NULL,2,3);
INSERT INTO tblRepairOrder6(id,repairOrderCode,createTime,completionTime,vehicleCondition,repairDescription,repairResult,repairStatus,tblVehicle6id,tblCustomer6tblMember6id,tblEmployee6tblMember6id) VALUES
(1,'PSC0001','2026-10-01 00:00:00','2026-10-01 00:00:00','Kiểm tra theo yêu cầu','Bảo dưỡng định kỳ','Đã hoàn tất','COMPLETED',1,1,3),
(2,'PSC0002','2026-10-31 23:59:59.999999','2026-10-31 23:59:59.999999','Kiểm tra theo yêu cầu','Bảo dưỡng định kỳ','Đã hoàn tất','COMPLETED',2,5,3),
(3,'PSC0003','2026-09-30 23:59:59.999999','2026-09-30 23:59:59.999999','Kiểm tra theo yêu cầu','Bảo dưỡng định kỳ','Đã hoàn tất','COMPLETED',1,1,3),
(4,'PSC0004','2026-10-15 12:00:00','2026-10-15 12:00:00','Kiểm tra theo yêu cầu','Bảo dưỡng định kỳ','Đã hoàn tất','COMPLETED',1,1,3),
(5,'PSC0005','2026-11-01 00:00:00','2026-11-01 00:00:00','Kiểm tra theo yêu cầu','Bảo dưỡng định kỳ','Đã hoàn tất','COMPLETED',1,1,3),
(6,'PSC0006','2026-10-10 10:00:00','2026-10-10 10:00:00','Kiểm tra theo yêu cầu','Bảo dưỡng định kỳ','Đã hoàn tất','COMPLETED',1,1,3),
(7,'PSC0007','2026-10-12 10:00:00','2026-10-12 10:00:00','Kiểm tra theo yêu cầu','Bảo dưỡng định kỳ','Đã hoàn tất','COMPLETED',1,1,3),
(8,'PSC0008','2026-10-13 10:00:00','2026-10-13 10:00:00','Kiểm tra theo yêu cầu','Bảo dưỡng định kỳ','Đã hoàn tất','COMPLETED',1,1,3);
INSERT INTO tblServiceUsage6(id,tblService6id,tblRepairOrder6id,saleUnitPrice,usageTime,note,tblEmployee6tblMember6id) VALUES
(1,1,1,500000,'2026-10-01 00:00:00','Giá tại thời điểm sửa chữa',4),
(2,2,1,800000,'2026-10-01 00:00:00','Giá tại thời điểm sửa chữa',4),
(3,1,2,550000,'2026-11-01 01:00:00','Giá tại thời điểm sửa chữa',4),
(4,1,3,450000,'2026-09-30 23:59:59.999999','Giá tại thời điểm sửa chữa',4),
(5,1,4,700000,'2026-10-15 12:00:00','Giá tại thời điểm sửa chữa',4),
(6,1,5,600000,'2026-11-01 00:00:00','Giá tại thời điểm sửa chữa',4),
(7,3,7,350000,'2026-10-12 10:00:00','Giá tại thời điểm sửa chữa',4),
(100,100,6,10000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(101,101,6,11000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(102,102,6,12000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(103,103,6,13000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(104,104,6,14000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(105,105,6,15000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(106,106,6,16000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(107,107,6,17000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(108,108,6,18000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(109,109,6,19000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(110,110,6,20000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(111,111,6,21000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(112,112,6,22000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(113,113,6,23000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(114,114,6,24000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(115,115,6,25000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(116,116,6,26000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(117,117,6,27000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(118,118,6,28000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(119,119,6,29000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(120,120,6,30000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4),
(121,121,6,31000,'2026-10-10 10:00:00','Giá tại thời điểm sửa chữa',4);
INSERT INTO tblPartUsage6(id,tblSparePart6id,tblRepairOrder6id,saleUnitPrice,usedQuantity,usageTime,note,tblEmployee6tblMember6id) VALUES
(1,1,1,1000000,2,'2026-10-01 00:00:00','Giá bán lịch sử',4),
(2,2,1,100000,3,'2026-10-01 00:00:00','Giá bán lịch sử',4),
(3,1,2,1100000,1,'2026-11-01 01:00:00','Giá bán lịch sử',4),
(4,1,3,900000,1,'2026-09-30 23:59:59.999999','Giá bán lịch sử',4),
(5,2,4,110000,4,'2026-10-15 12:00:00','Giá bán lịch sử',4),
(6,2,5,120000,1,'2026-11-01 00:00:00','Giá bán lịch sử',4),
(7,3,6,20000,2,'2026-10-10 10:00:00','Giá bán lịch sử',4),
(8,2,8,125000,1,'2026-10-13 10:00:00','Giá bán lịch sử',4);
INSERT INTO tblInvoice6(id,invoiceCode,issueDate,tblRepairOrder6id,paymentAmount,paymentTime,paymentMethod,paymentStatus,tblEmployee6tblMember6id,transactionReference) VALUES
(1,'HD0001','2026-10-01 00:00:00',1,3600000,'2026-10-01 00:00:00','BANK_TRANSFER','PAID',8,'DEMO-1'),
(2,'HD0002','2026-10-31 23:59:59.999999',2,1650000,'2026-10-31 23:59:59.999999','BANK_TRANSFER','PAID',8,'DEMO-2'),
(3,'HD0003','2026-09-30 23:59:59.999999',3,1350000,'2026-09-30 23:59:59.999999','BANK_TRANSFER','PAID',8,'DEMO-3'),
(4,'HD0004','2026-10-15 12:00:00',4,NULL,NULL,NULL,'UNPAID',NULL,NULL),
(5,'HD0005','2026-11-01 00:00:00',5,720000,'2026-11-01 00:00:00','BANK_TRANSFER','PAID',8,'DEMO-5'),
(6,'HD0006','2026-10-10 10:00:00',6,491000,'2026-10-10 10:00:00','BANK_TRANSFER','PAID',8,'DEMO-6'),
(8,'HD0008','2026-10-13 10:00:00',8,NULL,NULL,NULL,'UNPAID',NULL,NULL);
INSERT INTO tblSupplier6(id,supplierName,phone) VALUES
(1,'Nhà cung cấp demo','0241234567');
INSERT INTO tblPartReceipt6(id,receiptDate,tblSupplier6id,tblEmployee6tblMember6id) VALUES
(1,'2026-09-30 08:00:00',1,6);
INSERT INTO tblReceiptDetail6(receiptQuantity,purchaseUnitPrice,tblPartReceipt6id,tblSparePart6id) VALUES
(100,800000,1,1),
(100,80000,1,2),
(100,10000,1,3);
