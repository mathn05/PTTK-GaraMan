package garaman.entity;

import java.util.Date;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Invoice6 {
    private Integer id;
    private String invoiceCode;
    private BigDecimal totalAmount;
    private Date issueDate;
    private RepairOrder6 order;
    private Payment6 payment;

    public Invoice6() {}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount == null ? null : totalAmount.setScale(2, RoundingMode.UNNECESSARY); }
    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    public RepairOrder6 getOrder() { return order; }
    public void setOrder(RepairOrder6 order) { this.order = order; }
    public Payment6 getPayment() { return payment; }
    public void setPayment(Payment6 payment) { this.payment = payment; }
}
