package com.android.sample.flickrbrowser.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.sample.flickrbrowser.R;
import com.android.sample.flickrbrowser.models.Photo;
import com.android.sample.flickrbrowser.utils.CustomImageLoader;
import com.android.sample.flickrbrowser.utils.Utils;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

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
        holder.tvTags.setText("#tags: " + photo.getTags());
        holder.tvDescription.setText(photo.getDescription());
        holder.tvUsername.setText(photo.getOwner());
        if (photo.getPostedAt()!=null && !photo.getPostedAt().equals("")) {
            holder.tvTimeAgo.setText(Utils.timeAgoFrom(Long.parseLong(photo.getPostedAt())));
        }

        imageLoader = new CustomImageLoader(context);
        imageLoader.DisplayImage(photo.getoUrl(), 0, holder.ivPhoto, holder.loader);

       /* if (photo.getDescription().equals("")) {
            holder.lnDescriptionContainer.setVisibility(View.GONE);
        }
        if (photo.getTags().equals("")) {
            holder.tvTags.setVisibility(View.GONE);
        }*/

        //Animate holder display
        animate(holder);
    }

    @Override
    public void onViewRecycled(CustomViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    // Insert a new photo to the list on a specific position
    public void insert(int position, Photo photo) {
        photoList.add(position, photo);
        notifyItemInserted(position);
    }

    public void setData(List<Photo> newList) {
        this.photoList=newList;
    }

    // Animate holder display
    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.anticipateovershoot_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }

    // Provide a reference to the views for each data item via a ViewHolder
    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDescription, tvUsername, tvTimeAgo, tvTags;
        public ImageView ivPhoto;
        public DilatingDotsProgressBar loader;
        public LinearLayout lnDescriptionContainer;

        public CustomViewHolder(View v) {
            super(v);
            tvDescription = (TextView) v.findViewById(R.id.tvDescription);
            tvUsername = (TextView) v.findViewById(R.id.tvUsername);
            tvTimeAgo = (TextView) v.findViewById(R.id.tvTimeAgo);
            ivPhoto = (ImageView) v.findViewById(R.id.ivPhoto);
            tvTags = (TextView) v.findViewById(R.id.tvTags);
            loader = (DilatingDotsProgressBar) v.findViewById(R.id.progress);
            lnDescriptionContainer = (LinearLayout) v.findViewById(R.id.lnDescriptionWrap);
        }
    }
}
