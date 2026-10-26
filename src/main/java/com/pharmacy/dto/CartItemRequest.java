package com.pharmacy.dto;

public class CartItemRequest {
    private String medicineId;
    private int quantity;

    public CartItemRequest() {}

    public CartItemRequest(String medicineId, int quantity) {
        this.medicineId = medicineId;
        this.quantity = quantity;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
