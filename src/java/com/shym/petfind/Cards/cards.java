package com.shym.petfind.Cards;

import android.util.Log;

public class cards {
    private String userId;
    private String name;
    private String description;
    private String city;
    private String color;
    private String pet;
    private String[] profileImageUrl;
    private int currentPhoto;
    private boolean isClicked;
    public cards (String userId, String name, String[] profileImageUrl, int index){
        this.userId = userId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.currentPhoto = index;
        this.isClicked = false;
    }

    public String getUserId(){
        return userId;
    }
    public void setUserID(String userID){
        this.userId = userId;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getProfileImageUrl(){
        return profileImageUrl[currentPhoto];
    }

    public void setProfileImageUrl(String[] profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }

    public void nextPhoto(){
        this.currentPhoto = (this.currentPhoto + 1) % this.profileImageUrl.length;
        Log.d("cur", Integer.toString(this.currentPhoto));
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setPet(String pet) {
        this.pet = pet;
    }

    public String getPet() {
        return pet;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setClicked() {
        isClicked = true;
    }

    public boolean isClicked() {
        return isClicked;
    }
}
