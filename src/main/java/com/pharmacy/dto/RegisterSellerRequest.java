package com.pharmacy.dto;

public class RegisterSellerRequest {
    private String pharmacyName;
    private String username;
    private String password;
    private String contactPhone;
    private String email;
    private String address;

    public RegisterSellerRequest() {}

    public RegisterSellerRequest(String pharmacyName, String username, String password, String contactPhone, String email, String address) {
        this.pharmacyName = pharmacyName;
        this.username = username;
        this.password = password;
        this.contactPhone = contactPhone;
        this.email = email;
        this.address = address;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
