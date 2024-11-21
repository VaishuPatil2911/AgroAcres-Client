package com.example.foodapp.Model;

public class Address {
    String area,landmark,city,country,pinCode,userId,houseNo;

    public Address(String address, String landmark, String city, String country, String pinCode, String userId, String houseNo) {
        this.area = address;
        this.landmark = landmark;
        this.city = city;
        this.country = country;
        this.pinCode = pinCode;
        this.userId = userId;
        this.houseNo = houseNo;
    }

    public Address(){}

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHouseNo() {
        return houseNo;
    }

    public void setHouseNo(String houseNo) {
        this.houseNo = houseNo;
    }
}
