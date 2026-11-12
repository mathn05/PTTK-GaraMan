-- Read-only checks before considering V2 against a COPY of an original V1 database.
-- Both result sets must be empty. Never resolve conflicts by deleting appointments.
SELECT startTime,COUNT(*) duplicateCount FROM tblAppointmentSlot6 GROUP BY startTime HAVING COUNT(*)>1;
SELECT UPPER(REPLACE(REPLACE(REPLACE(REPLACE(TRIM(licensePlate),' ',''),'.',''),'-',''),CHAR(9),'')) normalizedPlate,
 COUNT(*) duplicateCount FROM tblVehicle6 GROUP BY normalizedPlate HAVING COUNT(*)>1;
