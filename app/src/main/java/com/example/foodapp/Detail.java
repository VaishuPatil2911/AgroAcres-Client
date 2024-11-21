//Kushwanth23
package com.example.foodapp;

//import java.util.SplittableRandom;

public class Detail {
    public String name,address,description,price,image;
    Boolean sold;

    public Detail(){

    }

    public String getName(){ return name; }

    public void setName(String name) { this.name = name; }

    public String getAddress(){ return address; }

    public void setAddress(String address){ this.address = address; }

    public  String getDescription(){ return description; }

    public void setDescription(String description){ this.description = description; }

    public String getPrice(){ return price; }

    public void setPrice(String price){ this.price = price; }

    public String getImage(){ return image; }

    public void setImage(String image){ this.image = image; }

    public Boolean getSold() {
        return sold;
    }

    public void setSold(Boolean sold) {
        this.sold = sold;
    }
}
