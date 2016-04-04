package com.android.sample.flickrbrowser.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.sample.flickrbrowser.R;
import com.android.sample.flickrbrowser.models.Photo;
import com.android.sample.flickrbrowser.utils.CustomImageLoader;
import com.android.sample.flickrbrowser.utils.Utils;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom adapter for recycler view
 * Custom layout will be applied for items in the list
 * Implement custom volley request to load image from url
 */
public class PhotoListAdapter extends RecyclerView.Adapter<PhotoListAdapter.CustomViewHolder> {

    List<Photo> photoList;
    CustomImageLoader imageLoader;
    Context context;

    public PhotoListAdapter(List<Photo> photoList, Context context) {
        this.photoList = photoList;
        this.context = context;
        //setHasStableIds(true);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_list_row, parent, false);
        return new CustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        // Display photo and related information
        Photo photo = photoList.get(position);
        /*if (photo.getDescription().equals("")) {
            holder.tvDescription.setVisibility(View.GONE);
        }
        if (photo.getTags().equals("")) {
            holder.tvTags.setVisibility(View.GONE);
        }*/
        holder.tvTags.setText("#tags: " + photo.getTags());
        holder.tvDescription.setText(photo.getDescription());
        holder.tvUsername.setText(photo.getOwner());
        if (photo.getPostedAt()!=null && !photo.getPostedAt().equals("")) {
            holder.tvTimeAgo.setText(Utils.timeAgoFrom(Long.parseLong(photo.getPostedAt())));
        }

        imageLoader = new CustomImageLoader(context);
        imageLoader.DisplayImage(photo.getoUrl(), 0, holder.ivPhoto, holder.loader);
    }

    @Override
    public void onViewRecycled(CustomViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public void updatePhotoList(ArrayList<Photo> photos) {
        photoList = photos;
    }

    // Provide a reference to the views for each data item via a ViewHolder
    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDescription, tvUsername, tvTimeAgo, tvTags;
        public CardView cvContainer;
        public ImageView ivPhoto;
        public DilatingDotsProgressBar loader;

        public CustomViewHolder(View v) {
            super(v);
            tvDescription = (TextView) v.findViewById(R.id.tvDescription);
            tvUsername = (TextView) v.findViewById(R.id.tvUsername);
            tvTimeAgo = (TextView) v.findViewById(R.id.tvTimeAgo);
            ivPhoto = (ImageView) v.findViewById(R.id.ivPhoto);
            cvContainer = (CardView) v.findViewById(R.id.cvContainer);
            tvTags = (TextView) v.findViewById(R.id.tvTags);
            loader = (DilatingDotsProgressBar) v.findViewById(R.id.progress);
        }
    }
}
