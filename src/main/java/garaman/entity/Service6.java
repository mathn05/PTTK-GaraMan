package garaman.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Service6 {
    private Integer id;
    private String serviceCode;
    private String serviceName;
    private String description;
    private BigDecimal unitPrice;

    public Service6() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice == null ? null : unitPrice.setScale(2, RoundingMode.UNNECESSARY); }
}
