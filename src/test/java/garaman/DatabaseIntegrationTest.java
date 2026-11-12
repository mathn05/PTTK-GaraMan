package garaman;

import garaman.dao.*;
import garaman.entity.*;
import garaman.exception.*;

import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Real MySQL only. Guarded test schema; never truncates/recreates application tables. */
class DatabaseIntegrationTest {
    static class Probe extends DAO6 {Connection connection(){return con;}}
    static LocalDate start,end;
    final List<Integer> slots=new ArrayList<>();
    final List<Integer> vehicles=new ArrayList<>();
    final List<Integer> appointments=new ArrayList<>();
    @BeforeAll static void targetTestSchema() throws Exception {
        assertTrue(Files.isRegularFile(Path.of(".local/test.properties")),"Run Windows setup to enable real MySQL integration tests.");
        System.setProperty("garaman.config", ".local/test.properties");
        System.setProperty("GARAMAN_TEST_MODE","true");
        System.setProperty("GARAMAN_TEST_DATABASE","garaman6_v3_it");
        try(var d=new Probe();var s=d.connection().createStatement();var rs=s.executeQuery("SELECT DATABASE(),@@session.time_zone")){
            assertTrue(rs.next());assertEquals("garaman6_v3_it",rs.getString(1),"Refuse writes outside test schema");assertEquals("+07:00",rs.getString(2));
        }
        String expected=Files.readString(Path.of("sql/seed-expected.json"));
        var a=java.util.regex.Pattern.compile("\"startDate\":\\s*\"([^\"]+)\"").matcher(expected);assertTrue(a.find());start=LocalDate.parse(a.group(1));
        var b=java.util.regex.Pattern.compile("\"endDate\":\\s*\"([^\"]+)\"").matcher(expected);assertTrue(b.find());end=LocalDate.parse(b.group(1));
    }
    @AfterEach void cleanup() throws Exception {
        try(var d=new Probe()){
            for(int id:slots)try(var s=d.connection().prepareStatement("DELETE FROM tblAppointment6 WHERE tblAppointmentSlot6id=?")){s.setInt(1,id);s.executeUpdate();}
            for(int id:appointments)try(var s=d.connection().prepareStatement("DELETE FROM tblAppointment6 WHERE id=?")){s.setInt(1,id);s.executeUpdate();}
            for(int id:slots)try(var s=d.connection().prepareStatement("DELETE FROM tblAppointmentSlot6 WHERE id=?")){s.setInt(1,id);s.executeUpdate();}
            for(int id:vehicles)try(var s=d.connection().prepareStatement("DELETE FROM tblVehicle6 WHERE id=?")){s.setInt(1,id);s.executeUpdate();}
        }
    }
    static void assertMoney(String expected,BigDecimal actual){assertEquals(0,new BigDecimal(expected).compareTo(actual),actual.toPlainString());}
    static BigDecimal sum(RevenueStats6[] rows){return Arrays.stream(rows).map(RevenueStats6::getTotalRevenue).reduce(BigDecimal.ZERO,BigDecimal::add);}
    int newSlot() throws Exception {
        try(var d=new Probe();var s=d.connection().prepareStatement("INSERT INTO tblAppointmentSlot6(startTime,status) VALUES(?,'AVAILABLE')",Statement.RETURN_GENERATED_KEYS)){
            var day=LocalDateTime.of(2030,1,1,8,0).plusNanos(ThreadLocalRandom.current().nextLong(300L*24*3600)*1_000_000_000L);
            s.setTimestamp(1,Timestamp.valueOf(day));s.executeUpdate();try(var rs=s.getGeneratedKeys()){rs.next();int id=rs.getInt(1);slots.add(id);return id;}
        }
    }
    Appointment6 draft(AppointmentSlot6 slot){
        try(var c=new CustomerDAO6();var v=new VehicleDAO6()){
            Appointment6 a=new Appointment6();a.setCus(c.getCustomerInfo("KH0001"));a.setVehicle(v.findById(1));a.setSlot(slot);a.setRequestDescription("Kiểm tra phanh và thay dầu");return a;
        }
    }
    void expire(int id) throws Exception {try(var d=new Probe();var ps=d.connection().prepareStatement("UPDATE tblAppointmentSlot6 SET holdExpiresAt=NOW(6)-INTERVAL 1 SECOND WHERE id=?")){ps.setInt(1,id);ps.executeUpdate();}}
    @Test void authenticateRealCustomerManagerWrongAndInactive(){
        try(var dao=new MemberDAO6()){
            assertInstanceOf(Customer6.class,dao.authenticate("customer6","GaraMan6@Demo"));assertInstanceOf(Manager6.class,dao.authenticate("manager6","GaraMan6@Demo"));
            assertNull(dao.authenticate("customer6","wrong"));assertNull(dao.authenticate("inactive6","GaraMan6@Demo"));assertNull(dao.authenticate("missing","GaraMan6@Demo"));
        }
    }
    @Test void mappingUsesSharedMemberIdAndNullableYear(){
        try(var c=new CustomerDAO6();var v=new VehicleDAO6()){
            assertEquals(1,c.getCustomerInfo("KH0001").getId());assertEquals("Nguyễn Văn An",c.getCustomerInfo("KH0001").getFullName());
            assertNull(v.findById(2).getManufactureYear());assertEquals(5,v.findById(2).getCus().getId());assertEquals("Toyota",v.findById(1).getBrand().getName());
            assertEquals(0,v.getVehiclesOfCustomer(999999).length);assertNull(c.getCustomerInfo("none"));assertNull(v.findById(-1));
        }
    }
    @Test void concurrentHoldOnlyOneConnectionWins() throws Exception {
        int id=newSlot();var gate=new CountDownLatch(1);
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()){
            var jobs=new ArrayList<Future<AppointmentSlot6>>();for(int i=0;i<12;i++)jobs.add(pool.submit(()->{gate.await();try(var d=new SlotDAO6()){return d.holdSlot(id);}}));
            gate.countDown();int wins=0;for(var f:jobs)if(f.get(20,TimeUnit.SECONDS)!=null)wins++;assertEquals(1,wins);
        }
    }
    @Test void wrongTokenCannotReleaseOrConsumeAndBorrowedConnectionStaysOpen() throws Exception {
        int id=newSlot();try(var d=new SlotDAO6();var p=new Probe()){
            var held=d.holdSlot(id);assertFalse(d.releaseSlot(id,"wrong"));assertFalse(d.validateHold(id,"wrong"));assertTrue(d.validateHold(id,held.getHoldCode()));
            assertThrows(IllegalStateException.class,()->d.consumeHold(id,held.getHoldCode()));
            p.connection().setAutoCommit(false);try(var borrowed=new SlotDAO6(p.connection())){assertFalse(borrowed.consumeHold(id,"wrong"));}
            assertFalse(p.connection().isClosed());p.connection().rollback();p.connection().setAutoCommit(true);
            assertTrue(d.releaseSlot(id,held.getHoldCode()));assertFalse(d.validateHold(id,held.getHoldCode()));var renewed=d.holdSlot(id);assertNotEquals(held.getHoldCode(),renewed.getHoldCode());
        }
    }
    @Test void expiredHoldCannotPersistButCanBeReclaimed() throws Exception {
        int id=newSlot();try(var s=new SlotDAO6();var a=new AppointmentDAO6()){
            var old=s.holdSlot(id);expire(id);assertFalse(s.validateHold(id,old.getHoldCode()));
            assertThrows(BookingConflictException6.class,()->a.createAppointment(draft(old),old.getHoldCode()));
            var renewed=s.holdSlot(id);assertNotNull(renewed);assertNotEquals(old.getHoldCode(),renewed.getHoldCode());assertFalse(s.releaseSlot(id,old.getHoldCode()));assertTrue(s.validateHold(id,renewed.getHoldCode()));
        }
    }
    @Test void expiredHoldAppearsAvailableWithoutGetMutatingDatabase() throws Exception {
        int id=newSlot();try(var s=new SlotDAO6()){
            var old=s.holdSlot(id);expire(id);var date=old.getStartTime().toInstant().atZone(DAO6.zone()).toLocalDate();
            assertTrue(Arrays.stream(s.getAvailableSlots(date)).anyMatch(x->x.getId()==id));
            assertEquals("HELD",s.findById(id).getStatus());assertEquals(old.getHoldCode(),s.findById(id).getHoldCode());
        }
    }
    @Test void concurrentFinalConfirmationCreatesExactlyOneAppointment() throws Exception {
        int id=newSlot();AppointmentSlot6 held;try(var s=new SlotDAO6()){held=s.holdSlot(id);}var draft=draft(held);var gate=new CountDownLatch(1);
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()){
            var jobs=new ArrayList<Future<Boolean>>();for(int i=0;i<8;i++)jobs.add(pool.submit(()->{gate.await();try(var dao=new AppointmentDAO6()){var a=dao.createAppointment(draft,held.getHoldCode());assertNotNull(a.getCreateAt());assertEquals("PENDING",a.getStatus());assertNull(a.getSs());assertEquals("LH"+String.format(java.util.Locale.ROOT,"%04d",a.getId()),a.getAppointmentCode());return true;}catch(BookingConflictException6 e){return false;}}));
            gate.countDown();int wins=0;for(var f:jobs)if(f.get(20,TimeUnit.SECONDS))wins++;assertEquals(1,wins);
        }
        try(var p=new Probe();var s=p.connection().prepareStatement("SELECT COUNT(*) FROM tblAppointment6 WHERE tblAppointmentSlot6id=?")){
            s.setInt(1,id);try(var rs=s.executeQuery()){rs.next();assertEquals(1,rs.getInt(1));}
        }
        try(var s=new SlotDAO6()){assertEquals("BOOKED",s.findById(id).getStatus());assertNull(s.findById(id).getHoldCode());assertNull(s.findById(id).getHoldExpiresAt());}
    }
    @Test void failedOwnershipRollsBackConsumedSlot() throws Exception {
        int id=newSlot();try(var s=new SlotDAO6();var a=new AppointmentDAO6();var v=new VehicleDAO6()){
            var held=s.holdSlot(id);var draft=draft(held);draft.setVehicle(v.findById(2));
            assertThrows(BookingConflictException6.class,()->a.createAppointment(draft,held.getHoldCode()));assertTrue(s.validateHold(id,held.getHoldCode()));
        }
    }
    @Test void codeFinalizationFailureRollsBackSlotAndTemporaryInsert() throws Exception {
        int blocked=newSlot(),target=newSlot();int existing;
        // Use the actual allocated key; INFORMATION_SCHEMA AUTO_INCREMENT statistics may be cached.
        try(var p=new Probe();var q=p.connection().prepareStatement("INSERT INTO tblAppointment6(appointmentCode,requestDescription,createAt,status,tblVehicle6id,tblAppointmentSlot6id,tblCustomer6tblMember6id) VALUES(?,'collision fixture',NOW(6),'PENDING',1,?,1)",Statement.RETURN_GENERATED_KEYS)){
            q.setString(1,"COLLISION"+System.nanoTime());q.setInt(2,blocked);q.executeUpdate();
            try(var rs=q.getGeneratedKeys()){rs.next();existing=rs.getInt(1);}
            try(var update=p.connection().prepareStatement("UPDATE tblAppointment6 SET appointmentCode=? WHERE id=?")){update.setString(1,"LH"+String.format(java.util.Locale.ROOT,"%04d",existing+1));update.setInt(2,existing);update.executeUpdate();}
        }
        try(var slots=new SlotDAO6();var dao=new AppointmentDAO6()){
            var held=slots.holdSlot(target);assertThrows(BookingConflictException6.class,()->dao.createAppointment(draft(held),held.getHoldCode()));
            assertTrue(slots.validateHold(target,held.getHoldCode()));
        }
        try(var p=new Probe();var q=p.connection().createStatement();var rs=q.executeQuery("SELECT COUNT(*) FROM tblAppointment6 WHERE appointmentCode LIKE '~%' OR tblAppointmentSlot6id="+target)){
            rs.next();assertEquals(0,rs.getInt(1));
        }
    }
    @Test void vehicleNormalizationNullableYearNullableMileageAndDuplicate() {
        String plate="T"+Long.toUnsignedString(System.nanoTime(),36).toUpperCase(Locale.ROOT);
        try(var dao=new VehicleDAO6()){
            Vehicle6 v=new Vehicle6();v.setLicensePlate(" "+plate.substring(0,2)+"-"+plate.substring(2)+". ");v.setVehicleModel("Corolla");VehicleBrand6 b=new VehicleBrand6();b.setId(1);v.setBrand(b);
            var saved=dao.saveVehicle(v,1);vehicles.add(saved.getId());assertEquals(plate,saved.getLicensePlate());assertNull(saved.getCurrentMileage());assertNull(saved.getManufactureYear());assertEquals(1,saved.getCus().getId());assertTrue(saved.getVehicleCode().matches("X[0-9]{4,}"));
            assertThrows(IllegalArgumentException.class,()->dao.saveVehicle(v,5));v.setManufactureYear(1800);assertThrows(IllegalArgumentException.class,()->dao.saveVehicle(v,1));
        }
    }
    @Test void concurrentVehicleCodesAreDistinct() throws Exception {
        var gate=new CountDownLatch(1);try(var pool=Executors.newVirtualThreadPerTaskExecutor()){
            var jobs=new ArrayList<Future<Vehicle6>>();for(int i=0;i<6;i++){final int n=i;jobs.add(pool.submit(()->{gate.await();try(var dao=new VehicleDAO6()){Vehicle6 v=new Vehicle6();v.setLicensePlate("C"+Long.toUnsignedString(System.nanoTime(),36)+n);v.setVehicleModel("Concurrent");VehicleBrand6 b=new VehicleBrand6();b.setId(1);v.setBrand(b);return dao.saveVehicle(v,1);}}));}
            gate.countDown();var codes=new HashSet<String>();for(var f:jobs){var v=f.get(20,TimeUnit.SECONDS);vehicles.add(v.getId());codes.add(v.getVehicleCode());}assertEquals(6,codes.size());
        }
    }
    @Test void servicePartAllRevenueMatchIndependentHandCalculation(){
        try(var dao=new RevenueDAO6()){
            for(var expected:Map.of("SERVICE",new int[]{24,25,2301000},"PART",new int[]{3,4,3440000},"ALL",new int[]{27,29,5741000}).entrySet()){
                var rows=dao.getRevenueStatistics(date(start),date(end),expected.getKey());assertEquals(expected.getValue()[0],rows.length);
                assertEquals(expected.getValue()[1],Arrays.stream(rows).mapToInt(RevenueStats6::getUsageCount).sum());
                BigDecimal total=Arrays.stream(rows).map(r->r.getTotalRevenue()).reduce(BigDecimal.ZERO,BigDecimal::add);assertEquals(0,total.compareTo(BigDecimal.valueOf(expected.getValue()[2])));
                for(int i=1;i<rows.length;i++){var prev=rows[i-1];var cur=rows[i];assertTrue(prev.getTotalRevenue().compareTo(cur.getTotalRevenue())>=0);}
            }
            var rows=dao.getRevenueStatistics(date(start),date(end),"ALL");assertEquals("PT0005",rows[0].getItemCode());assertEquals(2,rows[0].getUsageCount());assertMoney("3100000",rows[0].getTotalRevenue());
            assertEquals(2,Arrays.stream(rows).filter(r->r.getItemCode().equals("DV0001")).count());
        }
    }
    @Test void dateBoundariesUseInvoiceNotUsageOrPaymentTime(){
        try(var dao=new RevenueDAO6()){
            var first=dao.getRevenueStatistics(date(start),date(start),"ALL");assertMoney("3600000",sum(first));
            var last=dao.getRevenueStatistics(date(end),date(end),"ALL");assertMoney("1650000",sum(last));
            assertEquals(0,dao.getRevenueStatistics(date(start.plusYears(5)),date(end.plusYears(5)),"ALL").length);
            assertThrows(IllegalArgumentException.class,()->dao.getRevenueStatistics(date(end),date(start),"ALL"));
        }
    }
    @Test void lifetimeDiffersFromPaidHistoryAndHydratesCustomerVehicleOrder(){
        try(var s=new ServiceUsageDAO6();var p=new PartUsageDAO6();var part=new SparePartDAO6()){
            assertEquals(5,s.getTotalUsageCount("DV0001"));var su=s.getAllServiceUsageOfService("DV0001",date(start),date(end));assertEquals(2,su.length);assertEquals("PSC0002",su[0].getOrder().getRepairOrderCode());assertEquals("Nguyễn Văn Bình",su[0].getOrder().getCus().getFullName());assertEquals("51G98765",su[0].getOrder().getVehicle().getLicensePlate());
            assertEquals(3,p.getTotalUsageCount("PT0005"));assertEquals(4,p.getTotalUsageCount("PT0001"));assertEquals(2,p.getAllPartUsageOfPart("PT0005",date(start),date(end)).length);
            var tyre=part.getPartInfo("PT0005");assertEquals(96,tyre.getStockQuantity());assertEquals("Michelin",tyre.getBrand().getName());assertEquals("Lốp xe",tyre.getCat().getCategoryName());
        }
    }
    @Test void bothUsageTypesResolveFullInvoiceWithSeparateSalespersonAndCashier(){
        try(var d=new InvoiceDAO6()){
            assertEquals("HD0001",d.getInvoiceCodeByUsage("SERVICE",1));assertEquals("HD0001",d.getInvoiceCodeByUsage("PART",2));assertNull(d.getInvoiceCodeByUsage("SERVICE",7));assertNull(d.getInvoiceDetail("missing"));
            var i=d.getInvoiceDetail("HD0001");assertEquals(2,i.getOrder().getServiceUsage().length);assertEquals(2,i.getOrder().getPartUsage().length);assertMoney("3600000",i.getTotalAmount());assertEquals(i.getTotalAmount(),i.getPayment().getPaymentAmount());assertEquals("Nguyễn Văn An",i.getOrder().getCus().getFullName());assertEquals("Lê Thị Lan",i.getOrder().getSs().getFullName());assertEquals("Vũ Thu Ngân",i.getPayment().getSs().getFullName());
            assertThrows(IllegalArgumentException.class,()->d.getInvoiceCodeByUsage("tblMember6",1));
        }
    }
    @Test void paidInvoiceMismatchAndSqlFailureAreExceptionsNotEmptySuccess() throws Exception {
        try(var p=new Probe()){
            p.connection().setAutoCommit(false);
            try(var s=p.connection().createStatement()){s.executeUpdate("UPDATE tblInvoice6 SET paymentAmount=1 WHERE id=1");}
            try(var d=new InvoiceDAO6(p.connection())){assertThrows(DataAccessException6.class,()->d.getInvoiceDetail("HD0001"));}
            p.connection().rollback();p.connection().setAutoCommit(true);
            var d=new CustomerDAO6(p.connection());p.connection().close();assertThrows(DataAccessException6.class,()->d.getCustomerInfo("KH0001"));
        }
    }
    @Test void boxedIdentitiesAndZeroCountsHaveExplicitContracts(){
        Vehicle6 vehicle=new Vehicle6();assertNull(vehicle.getId());assertNull(vehicle.getVehicleCode());
        vehicle.setId(Integer.valueOf("10001"));assertEquals("X10001",vehicle.getVehicleCode());
        try(var service=new ServiceUsageDAO6();var parts=new PartUsageDAO6();var brands=new VehicleBrandDAO6()){
            assertEquals(Integer.valueOf(0),service.getTotalUsageCount("MISSING"));
            assertEquals(Integer.valueOf(0),parts.getTotalUsageCount("MISSING"));
            assertNotNull(brands.getVehicleBrands());assertNull(brands.findById(null));
        }
    }
    @Test void monetaryFractionsSurviveAggregationAndInvoiceMapping() throws Exception {
        try(var p=new Probe()){
            p.connection().setAutoCommit(false);
            try(var s=p.connection().createStatement()){
                s.executeUpdate("UPDATE tblServiceUsage6 SET saleUnitPrice=500000.25 WHERE id=1");
                s.executeUpdate("UPDATE tblInvoice6 SET paymentAmount=3600000.25 WHERE id=1");
            }
            try(var r=new RevenueDAO6(p.connection());var i=new InvoiceDAO6(p.connection())){
                var rows=r.getRevenueStatistics(date(start),date(end),"ALL");
                var total=Arrays.stream(rows).map(x->x.getTotalRevenue()).reduce(BigDecimal.ZERO,BigDecimal::add);
                assertEquals(0,new BigDecimal("5741000.25").compareTo(total));assertMoney("3600000.25",i.getInvoiceDetail("HD0001").getTotalAmount());
            }finally{p.connection().rollback();p.connection().setAutoCommit(true);}
        }
    }
    @Test void equalRevenueAndSameCodeAcrossTypesSortDeterministically() throws Exception {
        try(var p=new Probe()){
            p.connection().setAutoCommit(false);
            try(var s=p.connection().createStatement()){s.executeUpdate("UPDATE tblPartUsage6 SET saleUnitPrice=525000 WHERE id=7");}
            try(var r=new RevenueDAO6(p.connection())){
                var rows=r.getRevenueStatistics(date(start),date(end),"ALL");
                assertEquals("PT0005",rows[0].getItemCode());assertEquals("DV0001",rows[1].getItemCode());assertEquals("PART",rows[1].getItemType());
                assertEquals("DV0001",rows[2].getItemCode());assertEquals("SERVICE",rows[2].getItemType());assertEquals(rows[1].getTotalRevenue(),rows[2].getTotalRevenue());
            }finally{p.connection().rollback();p.connection().setAutoCommit(true);}
        }
    }
    @Test void mileageNullZeroAndKnownValuesRoundTrip() throws Exception {
        try(var dao=new VehicleDAO6()){
            for(Integer mileage:new Integer[]{null,0,12345}){
                Vehicle6 v=new Vehicle6();v.setLicensePlate("N"+Long.toUnsignedString(System.nanoTime(),36));v.setVehicleModel("Mileage");
                VehicleBrand6 brand=new VehicleBrand6();brand.setId(1);v.setBrand(brand);v.setCurrentMileage(mileage);
                var saved=dao.saveVehicle(v,1);vehicles.add(saved.getId());assertEquals(mileage,saved.getCurrentMileage());
                assertEquals("X"+String.format(java.util.Locale.ROOT,"%04d",saved.getId()),saved.getVehicleCode());
                v.setLicensePlate("NEG"+Long.toUnsignedString(System.nanoTime(),36));v.setCurrentMileage(-1);
                assertThrows(IllegalArgumentException.class,()->dao.saveVehicle(v,1));
            }
        }
    }
    @Test void unpaidPaymentGroupAndExplicitNullChecks() throws Exception {
        try(var p=new Probe()){
            p.connection().setAutoCommit(false);
            try(var dao=new InvoiceDAO6(p.connection())){
                assertNull(dao.getInvoiceDetail("HD0004").getPayment());
                assertThrows(SQLException.class,()->{try(var q=p.connection().createStatement()){q.executeUpdate("UPDATE tblInvoice6 SET paymentStatus='paid' WHERE id=1");}});
                for(String missing:List.of("paymentAmount","paymentTime","paymentMethod","tblEmployee6tblMember6id")){
                    String sql="UPDATE tblInvoice6 SET paymentStatus='PAID',paymentAmount=1140000,paymentTime=NOW(6),paymentMethod='CASH',tblEmployee6tblMember6id=8,"+missing+"=NULL WHERE id=4";
                    assertThrows(SQLException.class,()->{try(var q=p.connection().createStatement()){q.executeUpdate(sql);}});
                }
                assertThrows(SQLException.class,()->{try(var q=p.connection().createStatement()){q.executeUpdate("UPDATE tblInvoice6 SET paymentAmount=0 WHERE id=4");}});
                try(var q=p.connection().createStatement()){q.executeUpdate("UPDATE tblInvoice6 SET paymentStatus='PAID',paymentAmount=1140000,paymentTime=NOW(6),paymentMethod='CASH',tblEmployee6tblMember6id=8,transactionReference=NULL WHERE id=4");}
                var paid=dao.getInvoiceDetail("HD0004").getPayment();assertNotNull(paid);assertMoney("1140000",paid.getPaymentAmount());assertNull(paid.getTransactionReference());
                assertEquals(4,paid.getId());assertEquals("PAID",paid.getStatus());
            }finally{p.connection().rollback();p.connection().setAutoCommit(true);}
        }
    }
    @Test void optionalAccountIsAllNullAndPartialAccountIsRejected() throws Exception {
        try(var p=new Probe()){
            p.connection().setAutoCommit(false);
            try(var invoices=new InvoiceDAO6(p.connection())){
                assertNull(invoices.getInvoiceDetail("HD0001").getOrder().getSs().getAcc());
                assertThrows(SQLException.class,()->{try(var q=p.connection().createStatement()){q.executeUpdate("UPDATE tblMember6 SET status='active' WHERE id=1");}});
                assertThrows(SQLException.class,()->{try(var q=p.connection().createStatement()){q.executeUpdate("UPDATE tblEmployee6 SET jobTitle='sale_staff' WHERE tblMember6id=3");}});
                for(String set:List.of("status='ACTIVE'","username='partial'","profileImage='orphan.svg'")){
                    assertThrows(SQLException.class,()->{try(var q=p.connection().createStatement()){q.executeUpdate("UPDATE tblMember6 SET "+set+" WHERE id=3");}});
                }
                try(var q=p.connection().prepareStatement("UPDATE tblMember6 SET username='sales_test',password=?,status='ACTIVE',profileImage=NULL WHERE id=3")){
                    q.setString(1,MemberDAO6.hashPassword("Account@Case"));q.executeUpdate();
                }
                try(var member=new MemberDAO6(p.connection())){assertInstanceOf(SaleStaff6.class,member.authenticate("sales_test","Account@Case"));}
            }finally{p.connection().rollback();p.connection().setAutoCommit(true);}
        }
    }
    @Test void generatedSlotAndOneToOneFksProtectEverySqlWriter() throws Exception {
        int slot=newSlot();int appointment;
        try(var p=new Probe()){
            p.connection().setAutoCommit(false);
            try{
                try(var q=p.connection().prepareStatement("INSERT INTO tblAppointment6(appointmentCode,requestDescription,createAt,status,tblVehicle6id,tblAppointmentSlot6id,tblCustomer6tblMember6id) VALUES(?,'generated test',NOW(6),'PENDING',1,?,1)",Statement.RETURN_GENERATED_KEYS)){
                    q.setString(1,"G"+System.nanoTime());q.setInt(2,slot);q.executeUpdate();try(var rs=q.getGeneratedKeys()){rs.next();appointment=rs.getInt(1);}
                }
                for(String status:List.of("PENDING","CONFIRMED","RECEIVED","COMPLETED","CANCELLED")){
                    try(var q=p.connection().prepareStatement("UPDATE tblAppointment6 SET status=? WHERE id=?")){q.setString(1,status);q.setInt(2,appointment);q.executeUpdate();}
                    try(var q=p.connection().prepareStatement("SELECT activeSlotId FROM tblAppointment6 WHERE id=?")){
                        q.setInt(1,appointment);try(var rs=q.executeQuery()){rs.next();assertEquals(Set.of("PENDING","CONFIRMED","RECEIVED").contains(status)?slot:null,rs.getObject(1,Integer.class));}
                    }
                }
                assertThrows(SQLException.class,()->{try(var q=p.connection().createStatement()){q.executeUpdate("UPDATE tblAppointment6 SET activeSlotId=NULL WHERE id="+appointment);}});
                assertThrows(SQLException.class,()->{try(var q=p.connection().createStatement()){q.executeUpdate("UPDATE tblAppointment6 SET status='pending' WHERE id="+appointment);}});
                try(var q=p.connection().createStatement()){
                    q.executeUpdate("INSERT INTO tblAppointment6(appointmentCode,requestDescription,createAt,status,tblVehicle6id,tblAppointmentSlot6id,tblCustomer6tblMember6id) VALUES('SECOND"+slot+"','second',NOW(6),'PENDING',1,"+slot+",1)");
                }
                assertThrows(SQLException.class,()->{try(var q=p.connection().createStatement()){q.executeUpdate("UPDATE tblAppointment6 SET status='PENDING' WHERE id="+appointment);}});
                String ro="INSERT INTO tblRepairOrder6(repairOrderCode,createTime,repairStatus,tblVehicle6id,tblCustomer6tblMember6id,tblEmployee6tblMember6id,tblAppointment6id) VALUES(?,NOW(6),'OPEN',1,1,3,?)";
                try(var q=p.connection().prepareStatement(ro)){q.setString(1,"ONE"+slot);q.setInt(2,appointment);q.executeUpdate();}
                assertThrows(SQLException.class,()->{try(var q=p.connection().prepareStatement(ro)){q.setString(1,"TWO"+slot);q.setInt(2,appointment);q.executeUpdate();}});
                assertThrows(SQLException.class,()->{try(var q=p.connection().createStatement()){q.executeUpdate("INSERT INTO tblInvoice6(invoiceCode,issueDate,tblRepairOrder6id) VALUES('DUPLICATE',NOW(6),1)");}});
            }finally{p.connection().rollback();p.connection().setAutoCommit(true);}
        }
    }
    @Test void timestampPreservesInstantMicrosecondsAndVietnamDate() throws Exception {
        int slot=newSlot();TimeZone prior=TimeZone.getDefault();TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try(var p=new Probe()){
            Instant instant=Instant.parse("2031-06-01T01:02:03.123456Z");
            try(var q=p.connection().prepareStatement("UPDATE tblAppointmentSlot6 SET startTime=? WHERE id=?")){q.setTimestamp(1,Timestamp.from(instant));q.setInt(2,slot);q.executeUpdate();}
            try(var q=p.connection().prepareStatement("SELECT startTime,DATE_FORMAT(startTime,'%Y-%m-%d %H:%i:%s.%f'),@@session.time_zone FROM tblAppointmentSlot6 WHERE id=?")){
                q.setInt(1,slot);try(var rs=q.executeQuery()){rs.next();assertEquals(instant,rs.getTimestamp(1).toInstant());assertEquals("2031-06-01 08:02:03.123456",rs.getString(2));assertEquals("+07:00",rs.getString(3));}
            }
        }finally{TimeZone.setDefault(prior);}
    }
    @Test void largeDecimalAmountsAndPartMultiplicationAreExact() throws Exception {
        try(var p=new Probe()){
            p.connection().setAutoCommit(false);
            try{
                try(var q=p.connection().createStatement()){
                    q.executeUpdate("UPDATE tblServiceUsage6 SET saleUnitPrice=9007199254740991.01 WHERE id=1");
                    q.executeUpdate("UPDATE tblInvoice6 SET paymentAmount=9007199257840991.01 WHERE id=1");
                }
                try(var invoices=new InvoiceDAO6(p.connection());var revenue=new RevenueDAO6(p.connection())){
                    var invoice=invoices.getInvoiceDetail("HD0001");assertMoney("9007199257840991.01",invoice.getTotalAmount());
                    assertMoney("9007199257840991.01",invoice.getPayment().getPaymentAmount());
                    // Independent total: 5,741,000 - 500,000 + 9,007,199,254,740,991.01.
                    assertMoney("9007199259981991.01",sum(revenue.getRevenueStatistics(date(start),date(end),"ALL")));
                    assertMoney("9007199254740991.01",invoice.getOrder().getServiceUsage()[0].getSaleUnitPrice());
                    var part=new PartUsage6();part.setSaleUnitPrice(new BigDecimal("0.01"));part.setUsedQuantity(3);assertMoney("0.03",part.getLineAmount());
                }
            }finally{p.connection().rollback();p.connection().setAutoCommit(true);}
        }
    }


    private static java.util.Date date(LocalDate day){return java.util.Date.from(day.atStartOfDay(DAO6.zone()).toInstant());}
}
