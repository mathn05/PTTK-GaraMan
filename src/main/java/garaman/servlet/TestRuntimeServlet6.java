package garaman.servlet;

import garaman.dao.*;
import garaman.entity.*;
import java.util.*;
import java.time.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import garaman.dao.DAO6;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/** Read-only test guard. Disabled on the main configuration; no credentials are returned. */
@WebServlet("/test-runtime")
public class TestRuntimeServlet6 extends HttpServlet {
    @Override public void doGet(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        if(!"true".equals(DAO6.setting("GARAMAN_TEST_MODE","false"))
           ||!java.net.InetAddress.getByName(req.getRemoteAddr()).isLoopbackAddress()){resp.sendError(404);return;}
        try(var dao=new DAO6()){
            var info=dao.runtimeInfo();String db=info.get("database");
            if(!db.matches("garaman6[a-zA-Z0-9_]*_it")||!db.equals(DAO6.setting("GARAMAN_TEST_DATABASE","garaman6_v3_it"))){resp.sendError(409);return;}
            resp.setContentType("application/json");
            resp.getWriter().write("{\"database\":\""+db+"\",\"timeZone\":\""+info.get("timeZone")+"\",\"serverVersion\":\""+info.get("serverVersion")+"\"}");
        }
    }
}
