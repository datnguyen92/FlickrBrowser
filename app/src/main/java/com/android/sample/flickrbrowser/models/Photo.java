package com.android.sample.flickrbrowser.models;

/**
 * Created by datnguyen on 28/3/16.
 */
public class Photo {
    String title;
    String url;
    String description;
    String author;
    String iamge;

    public Photo(String title, String url, String description, String author, String iamge) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.author = author;
        this.iamge = iamge;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIamge() {
        return iamge;
    }

    public void setIamge(String iamge) {
        this.iamge = iamge;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", iamge='" + iamge + '\'' +
                '}';
    }
}
