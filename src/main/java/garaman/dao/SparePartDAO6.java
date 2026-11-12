package garaman.dao;

import garaman.entity.*;
import garaman.exception.DataAccessException6;
import java.sql.*;

public class SparePartDAO6 extends DAO6 {
    private static final String SELECT="SELECT p.*,b.name AS brandName,c.categoryName FROM vwSparePart6 p JOIN tblPartBrand6 b ON b.id=p.tblPartBrand6id JOIN tblCategory6 c ON c.id=p.tblCategory6id";
    public SparePartDAO6(){super();}
    public SparePartDAO6(Connection con){super(con);}
    public SparePart6 getPartInfo(String partCode){
        try(var ps=con.prepareStatement(SELECT+" WHERE p.partCode=?")) {
            ps.setString(1,partCode);try(var rs=ps.executeQuery()){return rs.next()?readPart(rs):null;}
        }catch(SQLException e){throw new DataAccessException6("Không tải được phụ tùng",e);}
    }
    SparePart6 findById(Integer id) throws SQLException {
        if(id==null||id<=0)return null;
        try(var ps=con.prepareStatement(SELECT+" WHERE p.id=?")){ps.setInt(1,id);try(var rs=ps.executeQuery()){return rs.next()?readPart(rs):null;}}
    }

    private SparePart6 readPart(ResultSet rs) throws SQLException {
        SparePart6 p=new SparePart6();p.setId(rs.getObject("id",Integer.class));p.setPartCode(rs.getString("partCode"));p.setPartName(rs.getString("partName"));
        p.setDescription(rs.getString("description"));p.setUnit(rs.getString("unit"));p.setTechnicalSpecifications(rs.getString("technicalSpec"));
        p.setUnitPrice(rs.getBigDecimal("unitPrice"));p.setStockQuantity(rs.getInt("stockQuantity"));p.setImage(rs.getString("image"));
        PartBrand6 b=new PartBrand6();b.setId(rs.getObject("tblPartBrand6id",Integer.class));b.setName(rs.getString("brandName"));p.setBrand(b);
        Category6 c=new Category6();c.setId(rs.getObject("tblCategory6id",Integer.class));c.setCategoryName(rs.getString("categoryName"));p.setCat(c);return p;
    }
}
