package com.pharmacy.dto;

import java.util.List;

public class CheckoutRequest {
    private List<CartItemRequest> items;
    private String paymentMode; // CASH, UPI, CARD, CREDIT
    private String borrowerId;  // required if paymentMode is CREDIT
    private String customerName;

    public CheckoutRequest() {}

    public CheckoutRequest(List<CartItemRequest> items, String paymentMode, String borrowerId, String customerName) {
        this.items = items;
        this.paymentMode = paymentMode;
        this.borrowerId = borrowerId;
        this.customerName = customerName;
    }

    public List<CartItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CartItemRequest> items) {
        this.items = items;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
