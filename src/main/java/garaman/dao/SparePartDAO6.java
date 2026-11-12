package garaman.dao;

import garaman.entity.Category6;
import garaman.entity.PartBrand6;
import garaman.entity.SparePart6;
import garaman.exception.DataAccessException6;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SparePartDAO6 extends DAO6 {
    private static final String SELECT = """
            SELECT p.*, b.name AS brandName, c.categoryName
            FROM vwSparePart6 p
            JOIN tblPartBrand6 b ON b.id = p.tblPartBrand6id
            JOIN tblCategory6 c ON c.id = p.tblCategory6id
            """;

    public SparePartDAO6() {
        super();
    }

    public SparePartDAO6(Connection con) {
        super(con);
    }

    public SparePart6 getPartInfo(String partCode) {
        String sql = SELECT + " WHERE p.partCode = ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, partCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return readPart(resultSet);
            }
        } catch (SQLException e) {
            throw new DataAccessException6("Không tải được phụ tùng", e);
        }
    }

    SparePart6 findById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            return null;
        }
        String sql = SELECT + " WHERE p.id = ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return readPart(resultSet);
            }
        }
    }

    private SparePart6 readPart(ResultSet rs) throws SQLException {
        SparePart6 part = new SparePart6();
        part.setId(rs.getObject("id", Integer.class));
        part.setPartCode(rs.getString("partCode"));
        part.setPartName(rs.getString("partName"));
        part.setDescription(rs.getString("description"));
        part.setUnit(rs.getString("unit"));
        part.setTechnicalSpecifications(rs.getString("technicalSpec"));
        part.setUnitPrice(rs.getBigDecimal("unitPrice"));
        part.setStockQuantity(rs.getInt("stockQuantity"));
        part.setImage(rs.getString("image"));

        PartBrand6 brand = new PartBrand6();
        brand.setId(rs.getObject("tblPartBrand6id", Integer.class));
        brand.setName(rs.getString("brandName"));
        part.setBrand(brand);
        Category6 category = new Category6();
        category.setId(rs.getObject("tblCategory6id", Integer.class));
        category.setCategoryName(rs.getString("categoryName"));
        part.setCat(category);
        return part;
    }
}
