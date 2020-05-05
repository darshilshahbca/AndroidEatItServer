package com.example.androideatitserver.Model;

import com.google.firebase.database.PropertyName;

public class Category {
    private String Name;
    private String Image;

    public Category() {
    }


    public Category(String name, String image) {
        Name = name;
        Image = image;

    }
    @PropertyName("Name")
    public void setName(String name) {
        Name = name;
    }

    @PropertyName("Image")
    public void setImage(String image) {
        Image = image;
    }

    @PropertyName("Name")
    public String getName() {
        return Name;
    }

    @PropertyName("Image")
    public String getImage() {
        return Image;
    }
}
