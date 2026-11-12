package garaman.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SparePart6 {
    private Integer id;
    private String partCode;
    private String partName;
    private String description;
    private String unit;
    private String technicalSpecifications;
    private BigDecimal unitPrice;
    private Integer stockQuantity;
    private String image;
    private PartBrand6 brand;
    private Category6 cat;

    public SparePart6() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPartCode() { return partCode; }
    public void setPartCode(String partCode) { this.partCode = partCode; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getTechnicalSpecifications() { return technicalSpecifications; }
    public void setTechnicalSpecifications(String technicalSpecifications) { this.technicalSpecifications = technicalSpecifications; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice == null ? null : unitPrice.setScale(2, RoundingMode.UNNECESSARY); }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public PartBrand6 getBrand() { return brand; }
    public void setBrand(PartBrand6 brand) { this.brand = brand; }
    public Category6 getCat() { return cat; }
    public void setCat(Category6 cat) { this.cat = cat; }
}
