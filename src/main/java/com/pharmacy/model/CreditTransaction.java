package com.pharmacy.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreditTransaction implements Serializable {
    public enum Type {
        CREDIT_TAKEN,
        PAYMENT_MADE
    }

    private String transactionId;
    private String borrowerId;
    private BigDecimal amount;
    private Type type;
    private LocalDateTime timestamp;
    private String note;
    private String paymentMethod; // e.g. CASH, UPI, CARD, PHARMACY_SALE
    private BigDecimal runningBalance;

    public CreditTransaction() {
        this.timestamp = LocalDateTime.now();
    }

    public CreditTransaction(String transactionId, String borrowerId, BigDecimal amount, Type type,
                             LocalDateTime timestamp, String note, String paymentMethod, BigDecimal runningBalance) {
        this.transactionId = transactionId;
        this.borrowerId = borrowerId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.runningBalance = runningBalance;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }
}
