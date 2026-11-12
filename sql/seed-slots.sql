-- Fresh demo database only. Occupied/held/expired examples on a future business day.
INSERT INTO tblAppointmentSlot6(id,startTime,status,holdCode,holdExpiresAt) VALUES
 (100001,TIMESTAMP(CURRENT_DATE+INTERVAL 3 DAY,'08:00:00'),'HELD',UUID(),NOW(6)+INTERVAL 3 MINUTE),
 (100002,TIMESTAMP(CURRENT_DATE+INTERVAL 3 DAY,'09:00:00'),'HELD',UUID(),NOW(6)-INTERVAL 1 MINUTE),
 (100003,TIMESTAMP(CURRENT_DATE+INTERVAL 3 DAY,'10:00:00'),'BOOKED',NULL,NULL);
INSERT INTO tblAppointment6(appointmentCode,requestDescription,createAt,status,tblEmployee6tblMember6id,tblVehicle6id,tblAppointmentSlot6id,tblCustomer6tblMember6id)
 VALUES('LH0001','Lịch hẹn mẫu đã đặt để kiểm tra slot bận',NOW(6),'PENDING',NULL,2,100003,5);
