package com.google.firebase.example.datn.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Chat {
    private String owner;
    private String message;

    private @ServerTimestamp Date timestamp;

    public Chat(){}

    public Chat(String owner, String message) {
        this.owner = owner;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getOwner() {
        return owner;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
