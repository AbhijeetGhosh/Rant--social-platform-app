package com.apps.abhijeet.rant;

public class Comments {
    public String comment, date, time, username;

    public Comments(){

    }

    public Comments(String comment, String date, String time, String username) {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
