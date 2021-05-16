package com.wt.cloudmedia.ui;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wt.cloudmedia.R;
import com.wt.cloudmedia.vo.Movie;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    public static final String TAG = "AdapterRecyclerView";
    private final List<Movie> mediaItems = new ArrayList<>();

    public void addItems(List<Movie> movies) {
        mediaItems.addAll(movies);
        notifyDataSetChanged();
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
        Glide.with(holder.jzvdStd.getContext()).load(
                mediaItems.get(position).getThumbnail()).into(holder.jzvdStd.posterImageView);
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        JzvdStd jzvdStd;

        public MyViewHolder(View itemView) {
            super(itemView);
            jzvdStd = itemView.findViewById(R.id.videoplayer);
        }
    }

}
