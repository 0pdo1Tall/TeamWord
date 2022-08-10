package com.google.firebase.example.datn.model;

public class WordListDetail {

    private String name;
    private String meaning;
    private String category;
    private String photo;
    private String owner;

    public WordListDetail() {}

    public WordListDetail(String name, String meaning, String category, String photo, String owner) {
        this.name = name;
        this.meaning = meaning;
        this.category = category;
        this.photo = photo;
        this.owner = owner;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }


}
