package com.pharmacy.dto;

import com.pharmacy.model.Sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceReceipt {
    private String invoiceNumber;
    private LocalDateTime invoiceDate;
    private String paymentMode;
    private String customerName;
    private String borrowerId;
    private List<Sale> items;
    private BigDecimal grandTotal;
    private BigDecimal totalGrossProfit;
    private BigDecimal borrowerNewBalance;

    public InvoiceReceipt() {}

    public InvoiceReceipt(String invoiceNumber, LocalDateTime invoiceDate, String paymentMode,
                          String customerName, String borrowerId, List<Sale> items,
                          BigDecimal grandTotal, BigDecimal totalGrossProfit, BigDecimal borrowerNewBalance) {
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.paymentMode = paymentMode;
        this.customerName = customerName;
        this.borrowerId = borrowerId;
        this.items = items;
        this.grandTotal = grandTotal;
        this.totalGrossProfit = totalGrossProfit;
        this.borrowerNewBalance = borrowerNewBalance;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public List<Sale> getItems() {
        return items;
    }

    public void setItems(List<Sale> items) {
        this.items = items;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public BigDecimal getTotalGrossProfit() {
        return totalGrossProfit;
    }

    public void setTotalGrossProfit(BigDecimal totalGrossProfit) {
        this.totalGrossProfit = totalGrossProfit;
    }

    public BigDecimal getBorrowerNewBalance() {
        return borrowerNewBalance;
    }

    public void setBorrowerNewBalance(BigDecimal borrowerNewBalance) {
        this.borrowerNewBalance = borrowerNewBalance;
    }
}
