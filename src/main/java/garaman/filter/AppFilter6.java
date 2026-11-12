package garaman.filter;

import garaman.exception.*;
import garaman.dao.DAO6;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppFilter6 implements Filter {
    private static final Logger LOG=Logger.getLogger(AppFilter6.class.getName());
    @Override public void init(FilterConfig config){TimeZone.setDefault(TimeZone.getTimeZone(DAO6.zone()));}
    @Override public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException,ServletException {
        var req=(HttpServletRequest)request;var resp=(HttpServletResponse)response;
        req.setCharacterEncoding("UTF-8");resp.setCharacterEncoding("UTF-8");
        resp.setHeader("X-Content-Type-Options","nosniff");resp.setHeader("X-Frame-Options","SAMEORIGIN");
        if(req.getServletPath().startsWith("/assets/")){chain.doFilter(req,resp);return;}
        resp.setHeader("Cache-Control","no-store");
        HttpSession session=req.getSession();synchronized(session){if(session.getAttribute("csrfToken")==null)session.setAttribute("csrfToken",UUID.randomUUID().toString());}
        if("POST".equals(req.getMethod())){
            String expected=(String)session.getAttribute("csrfToken"),actual=req.getParameter("csrfToken");
            if(actual==null||!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),actual.getBytes(StandardCharsets.UTF_8))){error(req,resp,403,"Phiên biểu mẫu không hợp lệ. Vui lòng tải lại trang.");return;}
        }
        try{chain.doFilter(req,resp);}
        catch(BookingConflictException6 e){if(!resp.isCommitted())error(req,resp,409,e.getMessage());}
        catch(IllegalArgumentException e){if(!resp.isCommitted())error(req,resp,400,e.getMessage());}
        catch(DataAccessException6 e){LOG.log(Level.SEVERE,"Database operation failed",e);if(!resp.isCommitted())error(req,resp,500,"Hệ thống chưa thể xử lý yêu cầu. Vui lòng thử lại sau.");}
        catch(ServletException|RuntimeException e){LOG.log(Level.SEVERE,"Request failed",e);if(!resp.isCommitted())error(req,resp,500,"Hệ thống chưa thể xử lý yêu cầu. Vui lòng thử lại sau.");}
    }

    private static void error(HttpServletRequest req,HttpServletResponse resp,int status,String message) throws ServletException,IOException {
        resp.setStatus(status);req.setAttribute("message",message);req.setAttribute("errorStatus",status);view(req,resp,"ErrorView6");
    }
    private static void view(HttpServletRequest req,HttpServletResponse resp,String name) throws ServletException,IOException {
        req.setAttribute("ctx",req.getContextPath());req.setAttribute("businessZone",DAO6.zone().getId());
        req.getRequestDispatcher("/WEB-INF/views/"+name+".jsp").forward(req,resp);
    }
}
