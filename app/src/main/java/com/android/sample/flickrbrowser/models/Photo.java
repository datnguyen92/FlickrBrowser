package com.android.sample.flickrbrowser.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by datnguyen on 28/3/16.
 */
public class Photo {
    String title, mUrl, lUrl, sUrl, zUrl, nUrl, cUrl, oUrl, description, tags, owner, postedAt;
    HashMap<String, String> mSize, sSize, lSize, zSize, nSize, cSize;
    int views;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(String postedAt) {
        this.postedAt = postedAt;
    }

    public Photo(JSONObject json) {

        try {
            this.title = json.getString("title");
            this.mUrl = json.has("url_m")?json.getString("url_m"):"";
            this.sUrl = json.has("url_s") ? json.getString("url_s"):"";
            this.zUrl = json.has("url_z")?json.getString("url_z"):"";
            this.nUrl = json.has("url_n")?json.getString("url_n"):"";
            this.cUrl = json.has("url_c")?json.getString("url_c"):"";
            this.lUrl = json.has("url_l")?json.getString("url_l"):"";
            this.oUrl = json.has("url_o")?json.getString("url_o"):"";
            this.owner = json.has("ownername")?json.getString("ownername"):"";
            this.postedAt = json.has("dateupload")?json.getString("dateupload"):"";
            this.tags = json.has("tags")?json.getString("tags"):"";
            JSONObject descriptionJson = json.has("description")?json.getJSONObject("description"):null;
            this.description = descriptionJson!=null||descriptionJson.has("_content")?descriptionJson.getString("_content"):"";
        } catch (JSONException e) {
            Log.d("Parse photo", e.getMessage());
        }
    }

    public String getoUrl() {
        return oUrl;
    }

    public void setoUrl(String oUrl) {
        this.oUrl = oUrl;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getlUrl() {
        return lUrl;
    }

    public void setlUrl(String lUrl) {
        this.lUrl = lUrl;
    }

    public String getsUrl() {
        return sUrl;
    }

    public void setsUrl(String sUrl) {
        this.sUrl = sUrl;
    }

    public String getzUrl() {
        return zUrl;
    }

    public void setzUrl(String zUrl) {
        this.zUrl = zUrl;
    }

    public String getnUrl() {
        return nUrl;
    }

    public void setnUrl(String nUrl) {
        this.nUrl = nUrl;
    }

    public String getcUrl() {
        return cUrl;
    }

    public void setcUrl(String cUrl) {
        this.cUrl = cUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public HashMap<String, String> getmSize() {
        return mSize;
    }

    public void setmSize(HashMap<String, String> mSize) {
        this.mSize = mSize;
    }

    public HashMap<String, String> getsSize() {
        return sSize;
    }

    public void setsSize(HashMap<String, String> sSize) {
        this.sSize = sSize;
    }

    public HashMap<String, String> getlSize() {
        return lSize;
    }

    public void setlSize(HashMap<String, String> lSize) {
        this.lSize = lSize;
    }

    public HashMap<String, String> getzSize() {
        return zSize;
    }

    public void setzSize(HashMap<String, String> zSize) {
        this.zSize = zSize;
    }

    public HashMap<String, String> getnSize() {
        return nSize;
    }

    public void setnSize(HashMap<String, String> nSize) {
        this.nSize = nSize;
    }

    public HashMap<String, String> getcSize() {
        return cSize;
    }

    public void setcSize(HashMap<String, String> cSize) {
        this.cSize = cSize;
    }
}
