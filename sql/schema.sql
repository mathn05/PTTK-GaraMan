-- Effective V3 schema: physical names from input/grm_db.jpg. Fresh/empty schema only.
-- No technical tables; migration history is external to the application database.
SET time_zone = '+07:00';
CREATE TABLE tblMember6 (
 id INT PRIMARY KEY AUTO_INCREMENT,
 fullName VARCHAR(255) NOT NULL, phone VARCHAR(20) NOT NULL,
 email VARCHAR(255) NULL, address VARCHAR(255) NULL,
 username VARCHAR(50) NULL UNIQUE, password VARCHAR(255) NULL,
 profileImage VARCHAR(255) NULL, status VARCHAR(50) NULL DEFAULT NULL,
 CONSTRAINT ck_member_account CHECK (
  (username IS NULL AND password IS NULL AND status IS NULL AND profileImage IS NULL)
  OR (username IS NOT NULL AND CHAR_LENGTH(TRIM(username))>0 AND password IS NOT NULL AND CHAR_LENGTH(password)>0
      AND status IS NOT NULL AND CAST(status AS BINARY) IN (_binary'ACTIVE',_binary'INACTIVE')))
) ENGINE=InnoDB;
CREATE TABLE tblCustomer6 (
 tblMember6id INT PRIMARY KEY, customerCode VARCHAR(50) NOT NULL UNIQUE,
 FOREIGN KEY (tblMember6id) REFERENCES tblMember6(id)
) ENGINE=InnoDB;
CREATE TABLE tblEmployee6 (
 tblMember6id INT PRIMARY KEY, employeeCode VARCHAR(50) NOT NULL UNIQUE,
 jobTitle VARCHAR(50) NOT NULL,
 FOREIGN KEY (tblMember6id) REFERENCES tblMember6(id),
 CHECK (CAST(jobTitle AS BINARY) IN (_binary'MANAGER',_binary'SALE_STAFF',_binary'TECHNICIAN',_binary'WAREHOUSE_STAFF'))
) ENGINE=InnoDB;
CREATE TABLE tblVehicleBrand6 (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(25) NOT NULL UNIQUE) ENGINE=InnoDB;
CREATE TABLE tblVehicle6 (
 id INT PRIMARY KEY AUTO_INCREMENT, licensePlate VARCHAR(20) NOT NULL UNIQUE,
 vehicleModel VARCHAR(255) NOT NULL, currentMileage INT NULL DEFAULT NULL,
 manufactureYear INT NULL, tblCustomer6tblMember6id INT NOT NULL, tblVehicleBrand6id INT NOT NULL,
 FOREIGN KEY (tblCustomer6tblMember6id) REFERENCES tblCustomer6(tblMember6id),
 FOREIGN KEY (tblVehicleBrand6id) REFERENCES tblVehicleBrand6(id),
 CHECK (currentMileage IS NULL OR currentMileage >= 0), CHECK (manufactureYear IS NULL OR manufactureYear >= 1886)
) ENGINE=InnoDB;
CREATE TABLE tblAppointmentSlot6 (
 id INT PRIMARY KEY AUTO_INCREMENT, startTime TIMESTAMP(6) NOT NULL,
 holdCode VARCHAR(100) NULL, holdExpiresAt TIMESTAMP(6) NULL,
 status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
 UNIQUE KEY uk_slot_start (startTime),
 CHECK (CAST(status AS BINARY) IN (_binary'AVAILABLE',_binary'HELD',_binary'BOOKED',_binary'CLOSED')),
 CHECK ((status='HELD' AND holdCode IS NOT NULL AND holdExpiresAt IS NOT NULL)
     OR (status<>'HELD' AND holdCode IS NULL AND holdExpiresAt IS NULL)),
 INDEX ix_slot_date (startTime, status)
) ENGINE=InnoDB;
CREATE TABLE tblAppointment6 (
 id INT PRIMARY KEY AUTO_INCREMENT, appointmentCode VARCHAR(30) NOT NULL UNIQUE,
 requestDescription VARCHAR(255) NOT NULL, createAt TIMESTAMP(6) NOT NULL,
 status VARCHAR(255) NOT NULL DEFAULT 'PENDING', tblEmployee6tblMember6id INT NULL,
 tblVehicle6id INT NOT NULL, tblAppointmentSlot6id INT NOT NULL, tblCustomer6tblMember6id INT NOT NULL,
 activeSlotId INT GENERATED ALWAYS AS
   (CASE WHEN status IN ('PENDING','CONFIRMED','RECEIVED') THEN tblAppointmentSlot6id ELSE NULL END) STORED,
 UNIQUE KEY uk_active_slot (activeSlotId),
 FOREIGN KEY (tblEmployee6tblMember6id) REFERENCES tblEmployee6(tblMember6id),
 FOREIGN KEY (tblVehicle6id) REFERENCES tblVehicle6(id),
 FOREIGN KEY (tblAppointmentSlot6id) REFERENCES tblAppointmentSlot6(id),
 FOREIGN KEY (tblCustomer6tblMember6id) REFERENCES tblCustomer6(tblMember6id),
 CHECK (CAST(status AS BINARY) IN (_binary'PENDING',_binary'CONFIRMED',_binary'RECEIVED',_binary'COMPLETED',_binary'CANCELLED'))
) ENGINE=InnoDB;
CREATE TABLE tblPartBrand6 (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL UNIQUE) ENGINE=InnoDB;
CREATE TABLE tblCategory6 (id INT PRIMARY KEY AUTO_INCREMENT, categoryName VARCHAR(255) NOT NULL UNIQUE) ENGINE=InnoDB;
CREATE TABLE tblService6 (
 id INT PRIMARY KEY AUTO_INCREMENT, serviceCode VARCHAR(30) NOT NULL UNIQUE,
 serviceName VARCHAR(255) NOT NULL, description VARCHAR(255) NULL,
 unitPrice DECIMAL(18,2) NOT NULL, CHECK (unitPrice>=0)
) ENGINE=InnoDB;
CREATE TABLE tblSparePart6 (
 id INT PRIMARY KEY AUTO_INCREMENT, partCode VARCHAR(30) NOT NULL UNIQUE,
 partName VARCHAR(255) NOT NULL, description VARCHAR(255) NULL, unit VARCHAR(50) NOT NULL,
 technicalSpec VARCHAR(255) NULL, unitPrice DECIMAL(18,2) NOT NULL,
 image VARCHAR(255) NULL, tblPartBrand6id INT NOT NULL, tblCategory6id INT NOT NULL,
 FOREIGN KEY (tblPartBrand6id) REFERENCES tblPartBrand6(id),
 FOREIGN KEY (tblCategory6id) REFERENCES tblCategory6(id), CHECK (unitPrice>=0)
) ENGINE=InnoDB;
CREATE TABLE tblRepairOrder6 (
 id INT PRIMARY KEY AUTO_INCREMENT, repairOrderCode VARCHAR(30) NOT NULL UNIQUE,
 createTime TIMESTAMP(6) NOT NULL, completionTime TIMESTAMP(6) NULL,
 vehicleCondition VARCHAR(255) NULL, repairDescription VARCHAR(255) NULL,
 repairResult VARCHAR(255) NULL, repairStatus VARCHAR(255) NOT NULL,
 tblVehicle6id INT NOT NULL, tblCustomer6tblMember6id INT NOT NULL, tblEmployee6tblMember6id INT NOT NULL, tblAppointment6id INT NULL,
 UNIQUE KEY uk_order_appointment (tblAppointment6id),
 FOREIGN KEY (tblVehicle6id) REFERENCES tblVehicle6(id), FOREIGN KEY (tblCustomer6tblMember6id) REFERENCES tblCustomer6(tblMember6id),
 FOREIGN KEY (tblEmployee6tblMember6id) REFERENCES tblEmployee6(tblMember6id), FOREIGN KEY (tblAppointment6id) REFERENCES tblAppointment6(id)
) ENGINE=InnoDB;
CREATE TABLE tblServiceUsage6 (
 id INT PRIMARY KEY AUTO_INCREMENT, usageTime TIMESTAMP(6) NOT NULL,
 saleUnitPrice DECIMAL(18,2) NOT NULL, note VARCHAR(255) NULL,
 tblService6id INT NOT NULL, tblRepairOrder6id INT NOT NULL, tblEmployee6tblMember6id INT NOT NULL,
 FOREIGN KEY (tblService6id) REFERENCES tblService6(id), FOREIGN KEY (tblRepairOrder6id) REFERENCES tblRepairOrder6(id),
 FOREIGN KEY (tblEmployee6tblMember6id) REFERENCES tblEmployee6(tblMember6id), CHECK (saleUnitPrice>=0),
 INDEX ix_service_usage (tblService6id, tblRepairOrder6id)
) ENGINE=InnoDB;
CREATE TABLE tblPartUsage6 (
 id INT PRIMARY KEY AUTO_INCREMENT, usedQuantity INT NOT NULL,
 usageTime TIMESTAMP(6) NOT NULL, saleUnitPrice DECIMAL(18,2) NOT NULL, note VARCHAR(255) NULL,
 tblSparePart6id INT NOT NULL, tblRepairOrder6id INT NOT NULL, tblEmployee6tblMember6id INT NOT NULL,
 FOREIGN KEY (tblSparePart6id) REFERENCES tblSparePart6(id), FOREIGN KEY (tblRepairOrder6id) REFERENCES tblRepairOrder6(id),
 FOREIGN KEY (tblEmployee6tblMember6id) REFERENCES tblEmployee6(tblMember6id),
 CHECK (usedQuantity>0), CHECK (saleUnitPrice>=0), INDEX ix_part_usage (tblSparePart6id, tblRepairOrder6id)
) ENGINE=InnoDB;
CREATE TABLE tblInvoice6 (
 id INT PRIMARY KEY AUTO_INCREMENT, invoiceCode VARCHAR(30) NOT NULL UNIQUE,
 issueDate TIMESTAMP(6) NOT NULL, tblRepairOrder6id INT NOT NULL,
 paymentAmount DECIMAL(18,2) NULL, paymentTime TIMESTAMP(6) NULL,
 paymentMethod VARCHAR(255) NULL, paymentStatus VARCHAR(255) NOT NULL DEFAULT 'UNPAID',
 transactionReference VARCHAR(100) NULL, tblEmployee6tblMember6id INT NULL,
 UNIQUE KEY uk_invoice_order (tblRepairOrder6id), INDEX ix_invoice_date (issueDate),
 FOREIGN KEY (tblRepairOrder6id) REFERENCES tblRepairOrder6(id),
 FOREIGN KEY (tblEmployee6tblMember6id) REFERENCES tblEmployee6(tblMember6id),
 CONSTRAINT ck_invoice_payment CHECK (
  (CAST(paymentStatus AS BINARY)=_binary'UNPAID' AND paymentAmount IS NULL AND paymentTime IS NULL AND paymentMethod IS NULL
     AND transactionReference IS NULL AND tblEmployee6tblMember6id IS NULL)
  OR (CAST(paymentStatus AS BINARY)=_binary'PAID' AND paymentAmount IS NOT NULL AND paymentAmount>=0 AND paymentTime IS NOT NULL
     AND paymentMethod IS NOT NULL AND CHAR_LENGTH(TRIM(paymentMethod))>0 AND tblEmployee6tblMember6id IS NOT NULL))
) ENGINE=InnoDB;
CREATE TABLE tblSupplier6 (
 id INT PRIMARY KEY AUTO_INCREMENT, supplierName VARCHAR(255) NOT NULL,
 phone VARCHAR(20) NOT NULL, email VARCHAR(255) NULL, address VARCHAR(255) NULL
) ENGINE=InnoDB;
CREATE TABLE tblPartReceipt6 (
 id INT PRIMARY KEY AUTO_INCREMENT, receiptDate TIMESTAMP(6) NOT NULL,
 tblSupplier6id INT NOT NULL, tblEmployee6tblMember6id INT NOT NULL,
 FOREIGN KEY (tblSupplier6id) REFERENCES tblSupplier6(id), FOREIGN KEY (tblEmployee6tblMember6id) REFERENCES tblEmployee6(tblMember6id)
) ENGINE=InnoDB;
CREATE TABLE tblReceiptDetail6 (
 id INT PRIMARY KEY AUTO_INCREMENT, receiptQuantity INT NOT NULL,
 purchaseUnitPrice DECIMAL(18,2) NOT NULL, tblPartReceipt6id INT NOT NULL, tblSparePart6id INT NOT NULL,
 FOREIGN KEY (tblPartReceipt6id) REFERENCES tblPartReceipt6(id), FOREIGN KEY (tblSparePart6id) REFERENCES tblSparePart6(id),
 CHECK (receiptQuantity>0), CHECK (purchaseUnitPrice>=0)
) ENGINE=InnoDB;
CREATE VIEW vwInvoice6 AS
 SELECT i.*, COALESCE(s.total,0)+COALESCE(p.total,0) AS totalAmount
 FROM tblInvoice6 i
 LEFT JOIN (SELECT tblRepairOrder6id,SUM(saleUnitPrice) total FROM tblServiceUsage6 GROUP BY tblRepairOrder6id) s ON s.tblRepairOrder6id=i.tblRepairOrder6id
 LEFT JOIN (SELECT tblRepairOrder6id,SUM(usedQuantity*saleUnitPrice) total FROM tblPartUsage6 GROUP BY tblRepairOrder6id) p ON p.tblRepairOrder6id=i.tblRepairOrder6id;
CREATE VIEW vwSparePart6 AS
 SELECT p.*, COALESCE(r.qty,0)-COALESCE(u.qty,0) AS stockQuantity
 FROM tblSparePart6 p
 LEFT JOIN (SELECT tblSparePart6id,SUM(receiptQuantity) qty FROM tblReceiptDetail6 GROUP BY tblSparePart6id) r ON r.tblSparePart6id=p.id
 LEFT JOIN (SELECT tblSparePart6id,SUM(usedQuantity) qty FROM tblPartUsage6 GROUP BY tblSparePart6id) u ON u.tblSparePart6id=p.id;
