package com.example.mindcheckdatacollectionapp.ui.theme;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mindcheckdatacollectionapp.R;

import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {
    private List<VideoItem> videos;
    private Context context;

    public VideosAdapter(List<VideoItem> videos, Context context) {
        this.videos = videos;
        this.context = context;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        VideoItem video = videos.get(position);
        holder.titleTextView.setText(video.getTitle());
        Glide.with(holder.thumbnailImageView.getContext())
                .load(video.getThumbnailUrl())
                .into(holder.thumbnailImageView);

        holder.thumbnailImageView.setOnClickListener(v -> {
            //TODO: handle click event, play video
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("video_url", video.getVideoUrl());
            context.startActivity(intent);
        });
        // Bind data to your views
        // For example: holder.videoThumbnail.setImage(video.getThumbnailUrl());
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        // Define your ViewHolder with views from item_video.xml
        ImageView thumbnailImageView;
        TextView titleTextView;
        public VideoViewHolder(View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
        }
    }
}

