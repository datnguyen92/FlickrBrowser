package com.android.sample.flickrbrowser.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Gallery {
    String author, name;
    int pageCount;
    ArrayList<Photo> photoList;

    public Gallery() {
        author = "";
        name = "";
        pageCount =0;
        photoList = new ArrayList<>();
    }

    public Gallery(JSONObject data) {
        try {
            JSONObject json;
            json = (JSONObject) data.get("photos");
            JSONArray photos = json.getJSONArray("photo");
            photoList = new ArrayList<>();
            for (int i = 0; i<photos.length();i++) {
                JSONObject photoJson = (JSONObject) photos.get(i);
                Photo photo = new Photo(photoJson);
                photoList.add(photo);
            }

            pageCount = json.getInt("pages");
            name="My Photos";
            return;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        author = "";
        name = "";
        pageCount = 0;
        photoList = new ArrayList<>();
    }

    public void join(JSONObject data) {
        try {
            JSONObject json;
            json = (JSONObject) data.get("photos");
            JSONArray photos = json.getJSONArray("photo");
            for (int i = 0; i<photos.length();i++) {
                JSONObject photoJson = (JSONObject) photos.get(i);
                Photo photo = new Photo(photoJson);
                this.photoList.add(photo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public ArrayList<Photo> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(ArrayList<Photo> photoList) {
        this.photoList = photoList;
    }
}
