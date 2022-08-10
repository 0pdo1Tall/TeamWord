package com.google.firebase.example.datn.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

//x
@IgnoreExtraProperties
public class Word { // 1

    public static final String FIELD_MEANING = "MEANING"; // name
    public static final String FIELD_CATEGORY = "category"; // category
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_POPULARITY = "numRatings";
    public static final String FIELD_AVG_RATING = "avgRating";

    private String name; // name
    private String meaning; //meaning
    private String category; //category
    private String photo;
    private int price; // remove
    private int numRatings;
    private double avgRating;
    private @ServerTimestamp Date timestamp;

    //
    private String owner;

    public Word() {}

    public Word(String name, String meaning, String category, String photo,
                      int price, int numRatings, double avgRating) {
        this.name = name;
        this.meaning = meaning;
        this.category = category;
        this.price = price;
        this.numRatings = numRatings;
        this.avgRating = avgRating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    //
    public String getOwner(){return owner;}

    public void setOwner(String owner){this.owner = owner;}

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
