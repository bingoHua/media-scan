package com.wt.cloudmedia.ui.main;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wt.cloudmedia.R;
import com.wt.cloudmedia.db.movie.Movie;
import com.wt.cloudmedia.player.JzvdStd2;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    public static final String TAG = "AdapterRecyclerView";
    private List<Movie> mediaItems = null;
    private ItemClicked itemClicked = null;

    public void addItems(List<Movie> movies) {
        if (mediaItems == null) {
            mediaItems = new ArrayList<>();
            mediaItems.addAll(movies);
            notifyItemRangeInserted(0, mediaItems.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mediaItems.size();
                }

                @Override
                public int getNewListSize() {
                    return movies.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return TextUtils.equals(movies.get(newItemPosition).getId(), mediaItems.get(oldItemPosition).getId());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Movie newProduct = movies.get(newItemPosition);
                    Movie oldProduct = mediaItems.get(oldItemPosition);
                    return TextUtils.equals(newProduct.getId(), (oldProduct.getId()))
                            && TextUtils.equals(newProduct.getUrl(), oldProduct.getUrl());
                }
            });
            mediaItems = movies;
            result.dispatchUpdatesTo(this);
        }
    }

    public void addItem(Movie mediaItem) {
        mediaItems.add(mediaItem);
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.item_videoview, parent,
                false));
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder [" + holder.jzvdStd.hashCode() + "] position=" + position);
        holder.jzvdStd.setUp(
                mediaItems.get(position).getUrl(),
                mediaItems.get(position).getName(), Jzvd.SCREEN_NORMAL);
        holder.jzvdStd.posterImageView.setScaleType(ImageView.ScaleType.CENTER);
        Glide.with(holder.jzvdStd.getContext()).load(
                mediaItems.get(position).getThumbnail()).into(holder.jzvdStd.posterImageView);
    }

    @Override
    public int getItemCount() {
        return mediaItems == null ? 0 : mediaItems.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        JzvdStd2 jzvdStd;

        public MyViewHolder(View itemView) {
            super(itemView);
            jzvdStd = itemView.findViewById(R.id.videoplayer);
        }
    }

    public void setItemClicked(ItemClicked itemClicked) {
        this.itemClicked = itemClicked;
    }

    interface ItemClicked {
        void itemClicked(Movie movie);
    }
}
