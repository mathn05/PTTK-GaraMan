package garaman.filter;

import garaman.dao.DAO6;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

public class AuthFilter6 implements Filter {
    @Override public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws ServletException,IOException {
        var req=(HttpServletRequest)request;var resp=(HttpServletResponse)response;var session=req.getSession(false);
        if(session==null||session.getAttribute("currentUser")==null){
            String route=req.getServletPath();if(Set.of("/booking","/vehicle","/revenue").contains(route)&&"GET".equals(req.getMethod()))req.getSession().setAttribute("returnRoute",route);
            resp.sendRedirect(req.getContextPath()+"/login");return;
        }
        String required=Set.of("/booking","/appointment","/vehicle").contains(req.getServletPath())?"CUSTOMER":"MANAGER";
        if(!required.equals(session.getAttribute("currentRole"))){error(req,resp,403,"Bạn không có quyền truy cập chức năng này.");return;}
        chain.doFilter(req,resp);
    }

    private static void error(HttpServletRequest req,HttpServletResponse resp,int status,String message) throws ServletException,IOException {
        resp.setStatus(status);req.setAttribute("message",message);req.setAttribute("errorStatus",status);view(req,resp,"ErrorView6");
    }
    private static void view(HttpServletRequest req,HttpServletResponse resp,String name) throws ServletException,IOException {
        req.setAttribute("ctx",req.getContextPath());req.setAttribute("businessZone",DAO6.zone().getId());
        req.getRequestDispatcher("/WEB-INF/views/"+name+".jsp").forward(req,resp);
    }
}
