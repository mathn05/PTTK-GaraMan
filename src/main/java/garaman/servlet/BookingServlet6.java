package garaman.servlet;

import garaman.dao.*;
import garaman.entity.*;
import java.util.*;
import java.time.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/booking")
public class BookingServlet6 extends HttpServlet {
    @Override public void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        if("home".equals(text(req,"action"))){req.setAttribute("cus",customer(req));view(req,resp,"customer/CustomerHomeView6");return;}
        Map<String,Object> c=context(req,true);
        synchronized(c){
            if("confirm".equals(text(req,"action"))||c.get("holdCode")!=null||c.get("completed")!=null){confirm(req,resp,c);return;}
            if(!text(req,"appointmentDate").isEmpty())c.put("appointmentDate",date(req,"appointmentDate"));
            form(req,resp,c);
        }
    }
    @Override public void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        Map<String,Object> c=context(req,false);
        synchronized(c){
            String action=text(req,"action");
            if(!Set.of("edit","hold","addVehicle").contains(action))throw new IllegalArgumentException("Thao tác không hợp lệ.");
            if("edit".equals(action)){
                if(c.get("completed")==null)release(c);
                resp.sendRedirect(url(req,"/booking",c.get("completed")==null?Map.of("context",c.get("id")):Map.of("action","confirm","context",c.get("id"))));return;
            }
            if(c.get("completed")!=null||c.get("holdCode")!=null){confirm(req,resp,c);return;}
            try {
                LocalDate day=date(req,"appointmentDate");String description=text(req,"requestDescription");
                Integer vehicleId=text(req,"vehicle").isEmpty()?null:integer(req,"vehicle");
                Integer slotId=text(req,"appointmentSlot").isEmpty()?null:integer(req,"appointmentSlot");
                c.put("appointmentDate",day);c.put("requestDescription",description);c.put("vehicleId",vehicleId);c.put("slotId",slotId);
                if("addVehicle".equals(action)){resp.sendRedirect(url(req,"/vehicle",Map.of("context",c.get("id"))));return;}
                LocalDate today=LocalDate.now(DAO6.zone());
                if(day.isBefore(today)||day.isAfter(today.plusDays(DAO6.number("GARAMAN_SLOT_DAYS",30)-1)))throw new IllegalArgumentException("Ngày hẹn nằm ngoài lịch làm việc có thể đặt.");
                if(description.isBlank()||description.length()>255||vehicleId==null||slotId==null)throw new IllegalArgumentException("Vui lòng chọn xe, khung giờ và nhập nội dung yêu cầu (tối đa 255 ký tự).");
                Customer6 customer=customer(req);Vehicle6 selected;
                try(var vehicles=new VehicleDAO6()){selected=vehicles.findById(vehicleId);}
                if(selected==null||!Objects.equals(selected.getCus().getId(),customer.getId())){error(req,resp,403,"Xe không thuộc khách hàng hiện tại.");return;}
                AppointmentSlot6 held;
                try(var slots=new SlotDAO6()){
                    AppointmentSlot6 info=slots.findById(slotId);
                    if(info==null||!info.getStartTime().toInstant().atZone(DAO6.zone()).toLocalDate().equals(day))throw new IllegalArgumentException("Khung giờ không thuộc ngày đã chọn.");
                    held=slots.holdSlot(slotId);
                }
                if(held==null){resp.setStatus(409);req.setAttribute("message","Khung giờ vừa được giữ hoặc đã đặt. Vui lòng chọn khung giờ khác.");form(req,resp,c);return;}
                c.put("holdCode",held.getHoldCode());c.put("holdExpiresAt",held.getHoldExpiresAt());
                try {
                    Appointment6 draft=new Appointment6();
                    draft.setCus(customer);draft.setVehicle(selected);draft.setSlot(held);draft.setRequestDescription(description);
                    c.put("draft",draft);
                    confirm(req,resp,c);
                }catch(ServletException|IOException|RuntimeException e){release(c);throw e;}
            }catch(IllegalArgumentException e){resp.setStatus(400);req.setAttribute("message",e.getMessage());form(req,resp,c);}
        }
    }
    private void form(HttpServletRequest req,HttpServletResponse resp,Map<String,Object> c) throws ServletException,IOException {
        req.setAttribute("context",c.get("id"));req.setAttribute("cus",customer(req));req.setAttribute("appointmentDate",c.get("appointmentDate").toString());
        req.setAttribute("selectedVehicle",c.get("vehicleId"));req.setAttribute("selectedSlot",c.get("slotId"));req.setAttribute("requestDescription",c.get("requestDescription"));req.setAttribute("draftAppointment",c.get("draft"));
        req.setAttribute("minDate",LocalDate.now(DAO6.zone()).toString());req.setAttribute("maxDate",LocalDate.now(DAO6.zone()).plusDays(DAO6.number("GARAMAN_SLOT_DAYS",30)-1).toString());
        try(var vehicles=new VehicleDAO6();var slots=new SlotDAO6()){
            req.setAttribute("vehicleList",vehicles.getVehiclesOfCustomer(((Customer6)req.getAttribute("cus")).getId()));
            req.setAttribute("availableSlots",slots.getAvailableSlots((LocalDate)c.get("appointmentDate")));
        }
        view(req,resp,"customer/BookAppointmentView6");
    }
    static void confirm(HttpServletRequest req,HttpServletResponse resp,Map<String,Object> c) throws ServletException,IOException {
        if(c.get("draft")==null&&c.get("completed")==null)throw new IllegalArgumentException("Chưa có lịch hẹn cần xác nhận. Vui lòng chọn lại thời gian.");
        req.setAttribute("context",c.get("id"));req.setAttribute("appointment",c.get("completed")!=null?c.get("completed"):c.get("draft"));req.setAttribute("completed",c.get("completed")!=null);
        req.setAttribute("holdCode",c.get("holdCode"));req.setAttribute("holdExpiresAt",c.get("holdExpiresAt"));
        if(c.get("holdCode")!=null&&c.get("slotId")!=null)try(var slots=new SlotDAO6()){req.setAttribute("holdRemainingMillis",slots.remainingHoldMillis((Integer)c.get("slotId"),(String)c.get("holdCode")));}
        view(req,resp,"customer/ConfirmView6");
    }
    @SuppressWarnings("unchecked") private static Map<String,Map<String,Object>> contexts(HttpSession session){
        synchronized(session){
            var all=(Map<String,Map<String,Object>>)session.getAttribute("bookingContexts");
            if(all==null){all=new ConcurrentHashMap<>();session.setAttribute("bookingContexts",all);}return all;
        }
    }
    static Map<String,Object> context(HttpServletRequest req,boolean create){
        var all=contexts(req.getSession());String id=text(req,"context");
        if(id.isEmpty()&&create)synchronized(all){
            if(all.size()>=64)throw new IllegalArgumentException("Đã mở quá nhiều biểu mẫu trong phiên. Hãy đăng nhập lại.");
            id=UUID.randomUUID().toString();Map<String,Object> c=new HashMap<>();c.put("id",id);c.put("appointmentDate",LocalDate.now(DAO6.zone()).plusDays(1));c.put("requestDescription","");all.put(id,c);return c;
        }
        var c=all.get(id);if(c==null)throw new IllegalArgumentException("Biểu mẫu không thuộc phiên hiện tại. Vui lòng mở lại đặt lịch.");return c;
    }
    private static void release(Map<String,Object> c){
        synchronized(c){
            if(c.get("holdCode")!=null&&c.get("slotId")!=null)try(var dao=new SlotDAO6()){dao.releaseSlot((Integer)c.get("slotId"),(String)c.get("holdCode"));}
            c.remove("holdCode");c.remove("holdExpiresAt");c.remove("draft");
        }
    }
    public static void releaseAll(HttpSession session){for(var c:contexts(session).values())release(c);}

    private static Customer6 customer(HttpServletRequest req){
        Member6 u=(Member6)req.getSession().getAttribute("currentUser");
        if(!(u instanceof Customer6 c))throw new IllegalArgumentException("Phiên khách hàng không hợp lệ.");
        try(var dao=new CustomerDAO6()){Customer6 fresh=dao.getCustomerInfo(c.getCustomerCode());if(fresh==null)throw new IllegalArgumentException("Không tìm thấy hồ sơ khách hàng.");return fresh;}
    }
    private static LocalDate date(HttpServletRequest req,String name){try{return LocalDate.parse(text(req,name));}catch(java.time.DateTimeException e){throw new IllegalArgumentException("Ngày không hợp lệ.");}}
    private static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
    private static void error(HttpServletRequest req,HttpServletResponse resp,int status,String message) throws ServletException,IOException {
        resp.setStatus(status);req.setAttribute("message",message);req.setAttribute("errorStatus",status);view(req,resp,"ErrorView6");
    }
    private static Integer integer(HttpServletRequest req,String name){try{return Integer.parseInt(text(req,name));}catch(NumberFormatException e){throw new IllegalArgumentException("Giá trị "+name+" không hợp lệ.");}}
    private static String text(HttpServletRequest req,String name){String v=req.getParameter(name);return v==null?"":v.trim();}
    private static String url(HttpServletRequest req,String route,Map<String,?> params){
        var out=new StringBuilder(req.getContextPath()).append(route);String sep="?";
        for(var e:params.entrySet()){if(e.getValue()==null)continue;out.append(sep).append(encode(e.getKey())).append('=').append(encode(String.valueOf(e.getValue())));sep="&";}
        return out.toString();
    }
    private static void view(HttpServletRequest req,HttpServletResponse resp,String name) throws ServletException,IOException {
        req.setAttribute("ctx",req.getContextPath());req.setAttribute("businessZone",DAO6.zone().getId());
        req.getRequestDispatcher("/WEB-INF/views/"+name+".jsp").forward(req,resp);
    }
}
