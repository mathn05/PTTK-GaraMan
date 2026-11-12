package garaman.util;

import jakarta.servlet.annotation.WebListener;
import garaman.servlet.BookingServlet6;
import jakarta.servlet.http.*;
import java.util.logging.*;

@WebListener
public class BookingSessionListener6 implements HttpSessionListener {
    @Override public void sessionDestroyed(HttpSessionEvent event){
        try{BookingServlet6.releaseAll(event.getSession());}catch(RuntimeException e){Logger.getLogger(getClass().getName()).log(Level.WARNING,"Could not release session hold; DB expiry remains authoritative",e);}
    }
}
