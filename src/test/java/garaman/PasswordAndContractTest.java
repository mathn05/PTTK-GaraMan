package garaman;

import garaman.dao.*;
import garaman.entity.*;

import org.junit.jupiter.api.Test;
import java.util.Date;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PasswordAndContractTest {
    @Test void passwordHashUsesSaltAndRejectsWrongOrMalformedPassword() throws Exception {
        String a=MemberDAO6.hashPassword("GaraMan6@Demo"),b=MemberDAO6.hashPassword("GaraMan6@Demo");
        assertNotEquals(a,b);assertTrue(a.startsWith("pbkdf2-sha256$v1$600000$"));
        assertTrue(verifyHash("GaraMan6@Demo",a));assertFalse(verifyHash("wrong",a));
        assertFalse(verifyHash("secret","plaintext"));assertFalse(verifyHash("secret",null));
    }
    @Test void publicContractsPreserveFieldsArraysAndParameterNames() throws Exception {
        assertEquals(Member6.class,Customer6.class.getSuperclass());assertEquals(Employee6.class,Manager6.class.getSuperclass());
        assertEquals(Integer.class,Vehicle6.class.getDeclaredField("manufactureYear").getType());
        assertEquals(RepairOrder6.class,ServiceUsage6.class.getDeclaredField("order").getType());
        assertEquals(RepairOrder6.class,PartUsage6.class.getDeclaredField("order").getType());
        assertThrows(NoSuchFieldException.class,()->RevenueStats6.class.getDeclaredField("id"));
        for(var pair:java.util.Map.of(RevenueStats6.class,"totalRevenue",Service6.class,"unitPrice",SparePart6.class,"unitPrice",ServiceUsage6.class,"saleUnitPrice",PartUsage6.class,"saleUnitPrice",Payment6.class,"paymentAmount",Invoice6.class,"totalAmount").entrySet())assertEquals(BigDecimal.class,pair.getKey().getDeclaredField(pair.getValue()).getType());
        assertEquals(Integer.class,Vehicle6.class.getDeclaredField("currentMileage").getType());
        for(Class<?> c:java.util.List.of(Member6.class,Account6.class,Vehicle6.class,Appointment6.class,AppointmentSlot6.class,VehicleBrand6.class,Service6.class,SparePart6.class,ServiceUsage6.class,PartUsage6.class,RepairOrder6.class,Invoice6.class,Payment6.class,Category6.class,PartBrand6.class))
            assertEquals(Integer.class,c.getDeclaredField("id").getType(),c.getName());
        assertEquals(Integer.class,RevenueStats6.class.getDeclaredField("usageCount").getType());
        assertEquals(Integer.class,PartUsage6.class.getDeclaredField("usedQuantity").getType());
        assertEquals(Integer.class,SparePart6.class.getDeclaredField("stockQuantity").getType());
        assertEquals(VehicleBrand6[].class,VehicleBrandDAO6.class.getMethod("getVehicleBrands").getReturnType());
        assertEquals(Integer.class,ServiceUsageDAO6.class.getMethod("getTotalUsageCount",String.class).getReturnType());
        assertThrows(NoSuchFieldException.class,()->Vehicle6.class.getDeclaredField("vehicleCode"));
        Vehicle6 vehicle=new Vehicle6();vehicle.setId(10001);assertEquals("X10001",vehicle.getVehicleCode());
        vehicle.setId(null);assertNull(vehicle.getVehicleCode());
        Service6 money=new Service6();money.setUnitPrice(new BigDecimal("1.000"));assertEquals(new BigDecimal("1.00"),money.getUnitPrice());
        assertThrows(ArithmeticException.class,()->money.setUnitPrice(new BigDecimal("1.001")));
        assertEquals(Date.class,Appointment6.class.getDeclaredField("createAt").getType());
        assertEquals(Vehicle6[].class,VehicleDAO6.class.getMethod("getVehiclesOfCustomer",Integer.class).getReturnType());
        assertEquals(AppointmentSlot6[].class,SlotDAO6.class.getMethod("getAvailableSlots",LocalDate.class).getReturnType());
        var report=RevenueDAO6.class.getMethod("getRevenueStatistics",Date.class,Date.class,String.class);
        assertEquals(RevenueStats6[].class,report.getReturnType());assertEquals("startDate",report.getParameters()[0].getName());
        assertEquals("endDate",report.getParameters()[1].getName());assertEquals("statisticType",report.getParameters()[2].getName());
        assertEquals(ServiceUsage6[].class,ServiceUsageDAO6.class.getMethod("getAllServiceUsageOfService",String.class,Date.class,Date.class).getReturnType());
        assertEquals(PartUsage6[].class,PartUsageDAO6.class.getMethod("getAllPartUsageOfPart",String.class,Date.class,Date.class).getReturnType());
    }

    private static boolean verifyHash(String password,String hash) throws Exception {
        var method=MemberDAO6.class.getDeclaredMethod("verifyPassword",String.class,String.class);method.setAccessible(true);return (boolean)method.invoke(null,password,hash);
    }
}
