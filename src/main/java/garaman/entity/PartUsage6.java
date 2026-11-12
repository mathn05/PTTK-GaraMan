package garaman.entity;

import java.util.Date;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PartUsage6 {
    private Integer id;
    private Integer usedQuantity;
    private Date usageTime;
    private BigDecimal saleUnitPrice;
    private String note;
    private SparePart6 sparePart;
    private Technician6 tech;
    private RepairOrder6 order;

    public PartUsage6() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUsedQuantity() { return usedQuantity; }
    public void setUsedQuantity(Integer usedQuantity) { this.usedQuantity = usedQuantity; }
    public Date getUsageTime() { return usageTime; }
    public void setUsageTime(Date usageTime) { this.usageTime = usageTime; }
    public BigDecimal getSaleUnitPrice() { return saleUnitPrice; }
    public void setSaleUnitPrice(BigDecimal saleUnitPrice) { this.saleUnitPrice = saleUnitPrice == null ? null : saleUnitPrice.setScale(2, RoundingMode.UNNECESSARY); }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public SparePart6 getSparePart() { return sparePart; }
    public void setSparePart(SparePart6 sparePart) { this.sparePart = sparePart; }
    public Technician6 getTech() { return tech; }
    public void setTech(Technician6 tech) { this.tech = tech; }
    public RepairOrder6 getOrder() { return order; }
    public void setOrder(RepairOrder6 order) { this.order = order; }
    public BigDecimal getLineAmount() { return saleUnitPrice.multiply(BigDecimal.valueOf(usedQuantity)); }
}
