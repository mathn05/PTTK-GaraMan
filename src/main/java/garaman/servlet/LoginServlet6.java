package garaman.servlet;

import garaman.dao.*;
import garaman.entity.*;
import java.util.*;
import java.time.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import garaman.dao.MemberDAO6;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet6 extends HttpServlet {
    @Override public void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        if(req.getSession().getAttribute("currentUser")!=null){home(req,resp);return;}view(req,resp,"user/LoginView6");
    }
    @Override public void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        if("logout".equals(text(req,"action"))){BookingServlet6.releaseAll(req.getSession());req.getSession().invalidate();resp.sendRedirect(req.getContextPath()+"/login");return;}
        String username=text(req,"username"),password=req.getParameter("password");
        if(username.isEmpty()||username.length()>50||password==null||password.length()>1024){invalid(req,resp);return;}
        try(var dao=new MemberDAO6()){
            Member6 user=dao.authenticate(username,password);
            if(user==null){invalid(req,resp);return;}
            String role=user instanceof Customer6?"CUSTOMER":user instanceof Manager6?"MANAGER":null;
            if(role==null){error(req,resp,403,"Tài khoản không được cấp quyền sử dụng hai chức năng này.");return;}
            BookingServlet6.releaseAll(req.getSession());req.getSession().removeAttribute("bookingContexts");req.changeSessionId();
            req.getSession().setAttribute("currentUser",user);req.getSession().setAttribute("currentRole",role);
            req.getSession().setAttribute("csrfToken",java.util.UUID.randomUUID().toString());
            String route=(String)req.getSession().getAttribute("returnRoute");req.getSession().removeAttribute("returnRoute");
            if(("CUSTOMER".equals(role)&&("/booking".equals(route)||"/vehicle".equals(route)))||("MANAGER".equals(role)&&"/revenue".equals(route))){resp.sendRedirect(req.getContextPath()+route);return;}
            home(req,resp);
        }
    }
    private void invalid(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {resp.setStatus(401);req.setAttribute("message","Tên đăng nhập hoặc mật khẩu không đúng, hoặc tài khoản chưa hoạt động.");view(req,resp,"user/LoginView6");}
    private void home(HttpServletRequest req,HttpServletResponse resp) throws IOException {resp.sendRedirect(req.getContextPath()+("CUSTOMER".equals(req.getSession().getAttribute("currentRole"))?"/booking?action=home":"/revenue?action=home"));}

    private static void error(HttpServletRequest req,HttpServletResponse resp,int status,String message) throws ServletException,IOException {
        resp.setStatus(status);req.setAttribute("message",message);req.setAttribute("errorStatus",status);view(req,resp,"ErrorView6");
    }
    private static String text(HttpServletRequest req,String name){String v=req.getParameter(name);return v==null?"":v.trim();}
    private static void view(HttpServletRequest req,HttpServletResponse resp,String name) throws ServletException,IOException {
        req.setAttribute("ctx",req.getContextPath());req.setAttribute("businessZone",DAO6.zone().getId());
        req.getRequestDispatcher("/WEB-INF/views/"+name+".jsp").forward(req,resp);
    }
}
