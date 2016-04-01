package com.android.sample.flickrbrowser.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.sample.flickrbrowser.models.Photo;

import java.util.ArrayList;

/**
 * A custom adapter for recycler view
 * Custom layout will be applied for items in the list
 */
public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.CustomViewHolder> {

    ArrayList<Photo> photoList;

    public PhotoListAdapter(ArrayList<Photo> photoList) {
        this.photoList = photoList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    // Provide a reference to the views for each data item via a ViewHolder
    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public CustomViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }
}
