package garaman.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RevenueStats6 {
    private String itemCode;
    private String itemName;
    private String itemType;
    private Integer usageCount;
    private BigDecimal totalRevenue;

    public RevenueStats6() {}
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue == null ? null : totalRevenue.setScale(2, RoundingMode.UNNECESSARY); }
}
