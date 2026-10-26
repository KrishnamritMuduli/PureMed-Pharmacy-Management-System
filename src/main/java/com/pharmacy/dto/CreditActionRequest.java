package com.pharmacy.dto;

import java.math.BigDecimal;

public class CreditActionRequest {
    private BigDecimal amount;
    private String note;
    private String paymentMethod; // CASH, UPI, CARD, OTHER

    public CreditActionRequest() {}

    public CreditActionRequest(BigDecimal amount, String note, String paymentMethod) {
        this.amount = amount;
        this.note = note;
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
}
