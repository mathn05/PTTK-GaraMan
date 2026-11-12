package garaman.entity;

import java.util.Date;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Payment6 {
    private Integer id;
    private BigDecimal paymentAmount;
    private Date paymentTime;
    private String paymentMethod;
    private String status;
    private SaleStaff6 ss;
    private String transactionReference;

    public Payment6() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount == null ? null : paymentAmount.setScale(2, RoundingMode.UNNECESSARY); }
    public Date getPaymentTime() { return paymentTime; }
    public void setPaymentTime(Date paymentTime) { this.paymentTime = paymentTime; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public SaleStaff6 getSs() { return ss; }
    public void setSs(SaleStaff6 ss) { this.ss = ss; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
}
