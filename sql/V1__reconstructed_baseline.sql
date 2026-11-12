-- Reconstructed V1 from supplied design + V2 decisions. Original V1 was not supplied.
-- Fresh database only. Never use this to overwrite an existing application database.
CREATE TABLE tblMember6 (
 id INT PRIMARY KEY AUTO_INCREMENT,
 fullName VARCHAR(255) NOT NULL, phone VARCHAR(20) NOT NULL,
 email VARCHAR(255) NULL, address VARCHAR(255) NULL,
 username VARCHAR(50) NULL UNIQUE, password VARCHAR(255) NULL,
 profileImage VARCHAR(255) NULL, status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
) ENGINE=InnoDB;
CREATE TABLE tblCustomer6 (
 memberId INT PRIMARY KEY, customerCode VARCHAR(50) NOT NULL UNIQUE,
 FOREIGN KEY (memberId) REFERENCES tblMember6(id)
) ENGINE=InnoDB;
CREATE TABLE tblEmployee6 (
 memberId INT PRIMARY KEY, employeeCode VARCHAR(50) NOT NULL UNIQUE,
 jobTitle VARCHAR(30) NOT NULL,
 FOREIGN KEY (memberId) REFERENCES tblMember6(id),
 CHECK (jobTitle IN ('MANAGER','SALE_STAFF','TECHNICIAN','WAREHOUSE_STAFF'))
) ENGINE=InnoDB;
CREATE TABLE tblVehicleBrand6 (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL UNIQUE) ENGINE=InnoDB;
CREATE TABLE tblVehicle6 (
 id INT PRIMARY KEY AUTO_INCREMENT, licensePlate VARCHAR(20) NOT NULL UNIQUE,
 vehicleModel VARCHAR(255) NOT NULL, currentMileage INT NOT NULL DEFAULT 0,
 manufactureYear INT NULL, customerId INT NOT NULL, vehicleBrandId INT NOT NULL,
 FOREIGN KEY (customerId) REFERENCES tblCustomer6(memberId),
 FOREIGN KEY (vehicleBrandId) REFERENCES tblVehicleBrand6(id),
 CHECK (currentMileage >= 0), CHECK (manufactureYear IS NULL OR manufactureYear >= 1886)
) ENGINE=InnoDB;
CREATE TABLE tblAppointmentSlot6 (
 id INT PRIMARY KEY AUTO_INCREMENT, startTime DATETIME(6) NOT NULL,
 holdCode VARCHAR(100) NULL, holdExpiresAt DATETIME(6) NULL,
 status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
 CHECK (status IN ('AVAILABLE','HELD','BOOKED','CLOSED')),
 CHECK ((status='HELD' AND holdCode IS NOT NULL AND holdExpiresAt IS NOT NULL)
     OR (status<>'HELD' AND holdCode IS NULL AND holdExpiresAt IS NULL)),
 INDEX ix_slot_date (startTime, status)
) ENGINE=InnoDB;
CREATE TABLE tblAppointment6 (
 id INT PRIMARY KEY AUTO_INCREMENT, appointmentCode VARCHAR(30) NOT NULL UNIQUE,
 requestDescription VARCHAR(255) NOT NULL, createAt DATETIME(6) NOT NULL,
 status VARCHAR(30) NOT NULL DEFAULT 'PENDING', saleStaffId INT NULL,
 vehicleId INT NOT NULL, appointmentSlotId INT NOT NULL, customerId INT NOT NULL,
 activeSlotId INT GENERATED ALWAYS AS
   (CASE WHEN status IN ('PENDING','CONFIRMED','RECEIVED') THEN appointmentSlotId ELSE NULL END) STORED,
 UNIQUE KEY uk_active_slot (activeSlotId),
 FOREIGN KEY (saleStaffId) REFERENCES tblEmployee6(memberId),
 FOREIGN KEY (vehicleId) REFERENCES tblVehicle6(id),
 FOREIGN KEY (appointmentSlotId) REFERENCES tblAppointmentSlot6(id),
 FOREIGN KEY (customerId) REFERENCES tblCustomer6(memberId),
 CHECK (status IN ('PENDING','CONFIRMED','RECEIVED','COMPLETED','CANCELLED'))
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
 partName VARCHAR(255) NOT NULL, description VARCHAR(255) NULL, unit VARCHAR(30) NOT NULL,
 technicalSpecifications VARCHAR(255) NULL, unitPrice DECIMAL(18,2) NOT NULL,
 image VARCHAR(255) NULL, partBrandId INT NOT NULL, categoryId INT NOT NULL,
 FOREIGN KEY (partBrandId) REFERENCES tblPartBrand6(id),
 FOREIGN KEY (categoryId) REFERENCES tblCategory6(id), CHECK (unitPrice>=0)
) ENGINE=InnoDB;
CREATE TABLE tblRepairOrder6 (
 id INT PRIMARY KEY AUTO_INCREMENT, repairOrderCode VARCHAR(30) NOT NULL UNIQUE,
 createTime DATETIME(6) NOT NULL, completionTime DATETIME(6) NULL,
 vehicleCondition VARCHAR(255) NULL, repairDescription VARCHAR(255) NULL,
 repairResult VARCHAR(255) NULL, repairStatus VARCHAR(30) NOT NULL,
 vehicleId INT NOT NULL, customerId INT NOT NULL, saleStaffId INT NOT NULL, appointmentId INT NULL,
 FOREIGN KEY (vehicleId) REFERENCES tblVehicle6(id), FOREIGN KEY (customerId) REFERENCES tblCustomer6(memberId),
 FOREIGN KEY (saleStaffId) REFERENCES tblEmployee6(memberId), FOREIGN KEY (appointmentId) REFERENCES tblAppointment6(id)
) ENGINE=InnoDB;
CREATE TABLE tblServiceUsage6 (
 id INT PRIMARY KEY AUTO_INCREMENT, usageTime DATETIME(6) NOT NULL,
 saleUnitPrice DECIMAL(18,2) NOT NULL, note VARCHAR(255) NULL,
 serviceId INT NOT NULL, repairOrderId INT NOT NULL, technicianId INT NOT NULL,
 FOREIGN KEY (serviceId) REFERENCES tblService6(id), FOREIGN KEY (repairOrderId) REFERENCES tblRepairOrder6(id),
 FOREIGN KEY (technicianId) REFERENCES tblEmployee6(memberId), CHECK (saleUnitPrice>=0),
 INDEX ix_service_usage (serviceId, repairOrderId)
) ENGINE=InnoDB;
CREATE TABLE tblPartUsage6 (
 id INT PRIMARY KEY AUTO_INCREMENT, usedQuantity INT NOT NULL,
 usageTime DATETIME(6) NOT NULL, saleUnitPrice DECIMAL(18,2) NOT NULL, note VARCHAR(255) NULL,
 sparePartId INT NOT NULL, repairOrderId INT NOT NULL, technicianId INT NOT NULL,
 FOREIGN KEY (sparePartId) REFERENCES tblSparePart6(id), FOREIGN KEY (repairOrderId) REFERENCES tblRepairOrder6(id),
 FOREIGN KEY (technicianId) REFERENCES tblEmployee6(memberId),
 CHECK (usedQuantity>0), CHECK (saleUnitPrice>=0), INDEX ix_part_usage (sparePartId, repairOrderId)
) ENGINE=InnoDB;
CREATE TABLE tblPayment6 (
 id INT PRIMARY KEY AUTO_INCREMENT, paymentAmount DECIMAL(18,2) NOT NULL,
 paymentTime DATETIME(6) NULL, paymentMethod VARCHAR(30) NULL,
 status VARCHAR(30) NOT NULL, saleStaffId INT NOT NULL, transactionReference VARCHAR(100) NULL,
 FOREIGN KEY (saleStaffId) REFERENCES tblEmployee6(memberId), CHECK (paymentAmount>=0),
 CHECK (status IN ('PAID','PENDING','FAILED','CANCELLED'))
) ENGINE=InnoDB;
CREATE TABLE tblInvoice6 (
 id INT PRIMARY KEY AUTO_INCREMENT, invoiceCode VARCHAR(30) NOT NULL UNIQUE,
 issueDate DATETIME(6) NOT NULL, repairOrderId INT NOT NULL UNIQUE, paymentId INT NULL UNIQUE,
 FOREIGN KEY (repairOrderId) REFERENCES tblRepairOrder6(id), FOREIGN KEY (paymentId) REFERENCES tblPayment6(id),
 INDEX ix_invoice_date (issueDate)
) ENGINE=InnoDB;
-- Only persistence/seed for derived stock; no inventory management UI.
CREATE TABLE tblSupplier6 (
 id INT PRIMARY KEY AUTO_INCREMENT, supplierName VARCHAR(255) NOT NULL,
 phone VARCHAR(20) NOT NULL, email VARCHAR(255) NULL, address VARCHAR(255) NULL
) ENGINE=InnoDB;
CREATE TABLE tblPartReceipt6 (
 id INT PRIMARY KEY AUTO_INCREMENT, receiptDate DATETIME(6) NOT NULL,
 supplierId INT NOT NULL, warehouseStaffId INT NOT NULL,
 FOREIGN KEY (supplierId) REFERENCES tblSupplier6(id), FOREIGN KEY (warehouseStaffId) REFERENCES tblEmployee6(memberId)
) ENGINE=InnoDB;
CREATE TABLE tblReceiptDetail6 (
 id INT PRIMARY KEY AUTO_INCREMENT, receiptQuantity INT NOT NULL,
 purchaseUnitPrice DECIMAL(18,2) NOT NULL, partReceiptId INT NOT NULL, sparePartId INT NOT NULL,
 FOREIGN KEY (partReceiptId) REFERENCES tblPartReceipt6(id), FOREIGN KEY (sparePartId) REFERENCES tblSparePart6(id),
 CHECK (receiptQuantity>0), CHECK (purchaseUnitPrice>=0)
) ENGINE=InnoDB;
CREATE VIEW vwInvoice6 AS
 SELECT i.*, COALESCE(s.total,0)+COALESCE(p.total,0) AS totalAmount
 FROM tblInvoice6 i
 LEFT JOIN (SELECT repairOrderId,SUM(saleUnitPrice) total FROM tblServiceUsage6 GROUP BY repairOrderId) s ON s.repairOrderId=i.repairOrderId
 LEFT JOIN (SELECT repairOrderId,SUM(usedQuantity*saleUnitPrice) total FROM tblPartUsage6 GROUP BY repairOrderId) p ON p.repairOrderId=i.repairOrderId;
CREATE VIEW vwSparePart6 AS
 SELECT p.*, COALESCE(r.qty,0)-COALESCE(u.qty,0) AS stockQuantity
 FROM tblSparePart6 p
 LEFT JOIN (SELECT sparePartId,SUM(receiptQuantity) qty FROM tblReceiptDetail6 GROUP BY sparePartId) r ON r.sparePartId=p.id
 LEFT JOIN (SELECT sparePartId,SUM(usedQuantity) qty FROM tblPartUsage6 GROUP BY sparePartId) u ON u.sparePartId=p.id;
