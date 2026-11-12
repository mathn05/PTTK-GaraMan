package garaman.servlet;

import garaman.dao.*;
import garaman.entity.*;
import java.util.*;
import java.time.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import garaman.dao.RevenueDAO6;
import garaman.entity.RevenueStats6;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@WebServlet("/revenue")
public class RevenueServlet6 extends HttpServlet {
    @Override public void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        String action=text(req,"action");
        switch(action){
            case "home" -> view(req,resp,"manager/ManagerHomeView6");
            case "", "menu" -> view(req,resp,"manager/ReportMenuView6");
            case "filter" -> filter(req,resp);
            case "list" -> {
                try{reportFilter(req);list(req,resp);}
                catch(IllegalArgumentException e){resp.setStatus(400);req.setAttribute("message",e.getMessage());filter(req,resp);}
            }
            default -> error(req,resp,400,"Thao tác báo cáo không hợp lệ.");
        }
    }
    private void filter(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        LocalDate last=LocalDate.now(DAO6.zone()).withDayOfMonth(1).minusDays(1);
        req.setAttribute("startDate",text(req,"startDate").isEmpty()?last.withDayOfMonth(1).toString():text(req,"startDate"));
        req.setAttribute("endDate",text(req,"endDate").isEmpty()?last.toString():text(req,"endDate"));
        String type=text(req,"statisticType");req.setAttribute("statisticType",type.isEmpty()?"ALL":type);
        view(req,resp,"manager/RevenueStatisticsFilterView6");
    }
    private void list(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        RevenueStats6[] all;
        try(var dao=new RevenueDAO6()){all=dao.getRevenueStatistics(date(LocalDate.parse((String)req.getAttribute("startDate"))),date(LocalDate.parse((String)req.getAttribute("endDate"))),(String)req.getAttribute("statisticType"));}
        int page=clampPage(page(req,"page"),all.length);
        BigDecimal total=BigDecimal.ZERO;for(var row:all)total=total.add(row.getTotalRevenue());
        req.setAttribute("revenueStats",slice(all,page));req.setAttribute("totalRevenue",total);req.setAttribute("currentPage",page);req.setAttribute("totalPages",(all.length+19)/20);req.setAttribute("totalRows",all.length);
        Map<String,Object> p=reportParams(req);p.put("action","list");p.put("page",page-1);req.setAttribute("previousUrl",url(req,"/revenue",p));p.put("page",page+1);req.setAttribute("nextUrl",url(req,"/revenue",p));p.put("action","filter");p.remove("page");req.setAttribute("backUrl",url(req,"/revenue",p));
        view(req,resp,"manager/RevenueStatisticsListView6");
    }

    private static Integer clampPage(Integer page,Integer length){int pages=(length+19)/20;return pages==0?1:Math.min(page,pages);}
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
