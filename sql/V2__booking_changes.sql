-- Preflight duplicate normalized plates/startTime in setup before this migration.
ALTER TABLE tblVehicle6 ADD COLUMN vehicleCode VARCHAR(30) NULL;
UPDATE tblVehicle6 SET vehicleCode=CONCAT('X',LPAD(id,GREATEST(4,LENGTH(id)),'0')) WHERE vehicleCode IS NULL;
ALTER TABLE tblVehicle6 MODIFY vehicleCode VARCHAR(30) NOT NULL, ADD UNIQUE KEY uk_vehicle_code (vehicleCode);
UPDATE tblVehicle6 SET licensePlate=UPPER(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(licensePlate),' ',''),'.',''),'-',''),CHAR(9),''));
ALTER TABLE tblAppointmentSlot6 ADD UNIQUE KEY uk_slot_start (startTime);
CREATE TABLE tblCodeSequence6 (
 prefix VARCHAR(10) PRIMARY KEY, lastValue BIGINT NOT NULL, CHECK (lastValue>=0)
) ENGINE=InnoDB;
INSERT INTO tblCodeSequence6(prefix,lastValue)
 SELECT 'X',COALESCE(MAX(CAST(SUBSTRING(vehicleCode,2) AS UNSIGNED)),0) FROM tblVehicle6;
INSERT INTO tblCodeSequence6(prefix,lastValue)
 SELECT 'LH',COALESCE(MAX(CASE WHEN appointmentCode REGEXP '^LH[0-9]+$' THEN CAST(SUBSTRING(appointmentCode,3) AS UNSIGNED) ELSE 0 END),0) FROM tblAppointment6;
