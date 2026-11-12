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

@WebServlet("/service-part")
public class ServicePartServlet6 extends HttpServlet {
    @Override public void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        reportFilter(req);
        String type=text(req,"itemType"),code=text(req,"itemCode");
        if(!Set.of("SERVICE","PART").contains(type)||code.isEmpty()||code.length()>30)throw new IllegalArgumentException("Đối tượng thống kê không hợp lệ.");
        java.util.Date start=date(LocalDate.parse((String)req.getAttribute("startDate"))),end=date(LocalDate.parse((String)req.getAttribute("endDate")));
        req.setAttribute("itemType",type);req.setAttribute("itemCode",code);
        String action=text(req,"action");if(!Set.of("","invoice").contains(action))throw new IllegalArgumentException("Thao tác chi tiết không hợp lệ.");
        boolean invoiceAction="invoice".equals(action);
        Integer rows,usagePage;boolean containsUsage=false;Integer target=invoiceAction?integer(req,"usageId"):0;
        if(invoiceAction&&target<=0)throw new IllegalArgumentException("Mã lần sử dụng phải lớn hơn 0.");
        if(type.equals("SERVICE")){
            try(var services=new ServiceDAO6();var usages=new ServiceUsageDAO6()){
                Service6 service=services.getServiceInfo(code);if(service==null){error(req,resp,404,"Không tìm thấy dịch vụ.");return;}
                ServiceUsage6[] all=usages.getAllServiceUsageOfService(code,start,end);rows=all.length;usagePage=clampPage(page(req,"usagePage"),rows);
                req.setAttribute("service",service);req.setAttribute("serviceUsageList",slice(all,usagePage));req.setAttribute("lifetimeUsageCount",usages.getTotalUsageCount(code));
                for(var u:all)if(Objects.equals(u.getId(),target))containsUsage=true;
            }
        }else{
            try(var parts=new SparePartDAO6();var usages=new PartUsageDAO6()){
                SparePart6 part=parts.getPartInfo(code);if(part==null){error(req,resp,404,"Không tìm thấy phụ tùng.");return;}
                PartUsage6[] all=usages.getAllPartUsageOfPart(code,start,end);rows=all.length;usagePage=clampPage(page(req,"usagePage"),rows);
                req.setAttribute("sparePart",part);req.setAttribute("partUsageList",slice(all,usagePage));req.setAttribute("lifetimeUsageCount",usages.getTotalUsageCount(code));
                String image=part.getImage();if(image!=null&&image.matches("/assets/[a-zA-Z0-9_.-]+\\.(svg|png|jpg|webp)"))req.setAttribute("partImageUrl",req.getContextPath()+image);
                for(var u:all)if(Objects.equals(u.getId(),target))containsUsage=true;
            }
        }
        req.setAttribute("currentPage",usagePage);req.setAttribute("totalPages",(rows+19)/20);req.setAttribute("totalRows",rows);req.setAttribute("reportPage",page(req,"page"));
        var p=reportParams(req);p.put("action","list");req.setAttribute("backUrl",url(req,"/revenue",p));p.remove("action");p.put("itemType",type);p.put("itemCode",code);p.put("usagePage",usagePage);
        if(invoiceAction){
            if(!containsUsage){error(req,resp,404,"Lần sử dụng không thuộc đối tượng và khoảng thống kê đang xem.");return;}
            String invoiceCode;try(var invoices=new InvoiceDAO6()){invoiceCode=invoices.getInvoiceCodeByUsage(type,target);}
            if(invoiceCode==null){error(req,resp,404,"Lần sử dụng chưa có hóa đơn.");return;}
            p.put("invoiceCode",invoiceCode);resp.sendRedirect(url(req,"/invoice",p));return;
        }
        p.put("usagePage",usagePage-1);req.setAttribute("previousUrl",url(req,"/service-part",p));p.put("usagePage",usagePage+1);req.setAttribute("nextUrl",url(req,"/service-part",p));p.put("usagePage",usagePage);p.put("action","invoice");req.setAttribute("invoiceBaseUrl",url(req,"/service-part",p));
        view(req,resp,"manager/ServicePartDetailView6");
    }

    private static Integer clampPage(Integer page,Integer length){int pages=(length+19)/20;return pages==0?1:Math.min(page,pages);}
    private static LocalDate date(HttpServletRequest req,String name){try{return LocalDate.parse(text(req,name));}catch(java.time.DateTimeException e){throw new IllegalArgumentException("Ngày không hợp lệ.");}}
    private static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
    private static void error(HttpServletRequest req,HttpServletResponse resp,int status,String message) throws ServletException,IOException {
        resp.setStatus(status);req.setAttribute("message",message);req.setAttribute("errorStatus",status);view(req,resp,"ErrorView6");
    }
    private static Integer integer(HttpServletRequest req,String name){try{return Integer.parseInt(text(req,name));}catch(NumberFormatException e){throw new IllegalArgumentException("Giá trị "+name+" không hợp lệ.");}}
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
    private static <T>T[] slice(T[] all,Integer page){int from=Math.min(all.length,(page-1)*20);return Arrays.copyOfRange(all,from,Math.min(all.length,from+20));}
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
    private static Date date(LocalDate date) { return Date.from(date.atStartOfDay(DAO6.zone()).toInstant()); }
    private static LocalDate localDate(Date date) { return date.toInstant().atZone(DAO6.zone()).toLocalDate(); }
}
