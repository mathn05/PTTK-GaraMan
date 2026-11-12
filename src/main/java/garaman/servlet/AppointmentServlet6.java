package garaman.servlet;

import garaman.dao.*;
import garaman.entity.*;
import java.util.*;
import java.time.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import garaman.exception.BookingConflictException6;
import garaman.entity.Appointment6;
import java.util.Objects;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/appointment")
public class AppointmentServlet6 extends HttpServlet {
    @Override public void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        Map<String,Object> c=BookingServlet6.context(req,false);
        synchronized(c){
            if(c.get("completed")==null){
                Appointment6 draft=(Appointment6)c.get("draft");String holdCode=(String)c.get("holdCode");Integer slotId=(Integer)c.get("slotId");
                if(draft==null||holdCode==null||slotId==null)throw new BookingConflictException6("Giữ chỗ không còn hiệu lực. Vui lòng đặt lại lịch.");
                if(!Objects.equals(draft.getCus().getId(),customer(req).getId())){error(req,resp,403,"Lịch hẹn không thuộc phiên hiện tại.");return;}
                try(var slots=new SlotDAO6()){
                    if(!slots.validateHold(slotId,holdCode)){resp.setStatus(409);req.setAttribute("message","Giữ chỗ đã hết hạn. Chọn Chỉnh sửa để chọn lại khung giờ.");BookingServlet6.confirm(req,resp,c);return;}
                }
                try(var dao=new AppointmentDAO6()){c.put("completed",dao.createAppointment(draft,holdCode));}
                c.remove("holdCode");c.remove("holdExpiresAt");c.remove("draft");
            }
            resp.sendRedirect(url(req,"/booking",Map.of("action","confirm","context",c.get("id"))));
        }
    }

    private static Customer6 customer(HttpServletRequest req){
        Member6 u=(Member6)req.getSession().getAttribute("currentUser");
        if(!(u instanceof Customer6 c))throw new IllegalArgumentException("Phiên khách hàng không hợp lệ.");
        try(var dao=new CustomerDAO6()){Customer6 fresh=dao.getCustomerInfo(c.getCustomerCode());if(fresh==null)throw new IllegalArgumentException("Không tìm thấy hồ sơ khách hàng.");return fresh;}
    }
    private static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
    private static void error(HttpServletRequest req,HttpServletResponse resp,int status,String message) throws ServletException,IOException {
        resp.setStatus(status);req.setAttribute("message",message);req.setAttribute("errorStatus",status);view(req,resp,"ErrorView6");
    }
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
