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
import java.time.Year;

@WebServlet("/vehicle")
public class VehicleServlet6 extends HttpServlet {
    @Override public void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        Map<String,Object> c=BookingServlet6.context(req,true);synchronized(c){form(req,resp,c);}
    }
    @Override public void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        Map<String,Object> c=BookingServlet6.context(req,false);synchronized(c){
            try {
                if(c.get("completed")!=null||c.get("holdCode")!=null)throw new IllegalArgumentException("Hãy quay lại và chỉnh sửa lịch hẹn trước khi thêm xe.");
                if(!text(req,"action").isEmpty())throw new IllegalArgumentException("Thao tác không hợp lệ.");
                Integer brandId=integer(req,"vehicleBrand");
                if(brandId<=0)throw new IllegalArgumentException("Vui lòng chọn hãng xe hợp lệ.");
                VehicleBrand6 brand;
                try(var dao=new VehicleBrandDAO6()){brand=dao.findById(brandId);}
                if(brand==null)throw new IllegalArgumentException("Hãng xe không tồn tại.");
                String plate=text(req,"licensePlate"),model=text(req,"vehicleModel");
                Integer year=text(req,"manufactureYear").isEmpty()?null:integer(req,"manufactureYear");
                if(!plate.toUpperCase(Locale.ROOT).replaceAll("[\\s.\\-]","").matches("[A-Z0-9]{1,20}")||model.isBlank()||model.length()>255
                   ||(year!=null&&(year<1886||year>Year.now(DAO6.zone()).getValue()+1)))throw new IllegalArgumentException("Thông tin xe không hợp lệ.");
                Customer6 customer=customer(req);
                Vehicle6 v=new Vehicle6();
                v.setCus(customer);v.setBrand(brand);v.setLicensePlate(plate);v.setVehicleModel(model);v.setManufactureYear(year);
                try(var dao=new VehicleDAO6()){Vehicle6 saved=dao.saveVehicle(v,customer.getId());c.put("vehicleId",saved.getId());}
                resp.sendRedirect(url(req,"/booking",Map.of("context",c.get("id"))));
            }catch(IllegalArgumentException e){resp.setStatus(400);req.setAttribute("message",e.getMessage());form(req,resp,c);}
        }
    }

    private void form(HttpServletRequest req,HttpServletResponse resp,Map<String,Object> c) throws ServletException,IOException {
        req.setAttribute("context",c.get("id"));req.setAttribute("cus",customer(req));req.setAttribute("maxYear",Year.now(DAO6.zone()).getValue()+1);
        for(String name:new String[]{"licensePlate","vehicleModel","manufactureYear"})req.setAttribute(name,text(req,name));
        Integer selected=null;try{Integer id=integer(req,"vehicleBrand");if(id>0)selected=id;}catch(IllegalArgumentException ignored){}
        req.setAttribute("selectedBrandId",selected);
        try(var dao=new VehicleBrandDAO6()){req.setAttribute("vehicleBrandList",dao.getVehicleBrands());}
        view(req,resp,"customer/AddNewVehicleView6");
    }

    private static Customer6 customer(HttpServletRequest req){
        Member6 u=(Member6)req.getSession().getAttribute("currentUser");
        if(!(u instanceof Customer6 c))throw new IllegalArgumentException("Phiên khách hàng không hợp lệ.");
        try(var dao=new CustomerDAO6()){Customer6 fresh=dao.getCustomerInfo(c.getCustomerCode());if(fresh==null)throw new IllegalArgumentException("Không tìm thấy hồ sơ khách hàng.");return fresh;}
    }
    private static String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
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
