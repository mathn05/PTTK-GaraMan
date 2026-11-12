package garaman.servlet;

import garaman.dao.*;
import garaman.entity.*;
import java.util.*;
import java.time.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import garaman.dao.InvoiceDAO6;
import garaman.entity.Invoice6;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

@WebServlet("/invoice")
public class InvoiceServlet6 extends HttpServlet {
    @Override public void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        String code=text(req,"invoiceCode");if(code.isEmpty()||code.length()>30)throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        Invoice6 invoice;try(var dao=new InvoiceDAO6()){invoice=dao.getInvoiceDetail(code);}
        if(invoice==null){error(req,resp,404,"Không tìm thấy hóa đơn.");return;}
        req.setAttribute("invoice",invoice);
        if(!text(req,"itemCode").isEmpty()){
            reportFilter(req);String type=text(req,"itemType");if(!Set.of("SERVICE","PART").contains(type))throw new IllegalArgumentException("Loại đối tượng không hợp lệ.");
            var p=reportParams(req);p.put("itemType",type);p.put("itemCode",text(req,"itemCode"));p.put("usagePage",page(req,"usagePage"));req.setAttribute("backUrl",url(req,"/service-part",p));
        }else req.setAttribute("backUrl",req.getContextPath()+"/revenue?action=filter");
        view(req,resp,"manager/InvoiceDetailView6");
    }

    private static LocalDate date(HttpServletRequest req,String name){try{return LocalDate.parse(text(req,name));}catch(java.time.DateTimeException e){throw new IllegalArgumentException("Ngày không hợp lệ.");}}
    private static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
    private static void error(HttpServletRequest req,HttpServletResponse resp,int status,String message) throws ServletException,IOException {
        resp.setStatus(status);req.setAttribute("message",message);req.setAttribute("errorStatus",status);view(req,resp,"ErrorView6");
    }
    private static Integer page(HttpServletRequest req,String name){String v=text(req,name);if(v.isEmpty())return 1;try{return Math.max(1,Integer.parseInt(v));}catch(NumberFormatException e){return 1;}}
    private static void reportFilter(HttpServletRequest req){
        LocalDate start=date(req,"startDate"),end=date(req,"endDate");
        if(start.isAfter(end)||start.getYear()<1900||end.getYear()>9998)throw new IllegalArgumentException("Khoảng ngày thống kê không hợp lệ.");
        String type=text(req,"statisticType");if(!Set.of("SERVICE","PART","ALL").contains(type))throw new IllegalArgumentException("Loại thống kê không hợp lệ.");
        req.setAttribute("startDate",start.toString());req.setAttribute("endDate",end.toString());req.setAttribute("statisticType",type);
    }
    private static Map<String,Object> reportParams(HttpServletRequest req){
        var p=new LinkedHashMap<String,Object>();for(String key:List.of("startDate","endDate","statisticType"))p.put(key,req.getAttribute(key));p.put("page",page(req,"page"));return p;
    }
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
