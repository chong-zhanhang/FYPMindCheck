package com.example.mindcheckdatacollectionapp.ui.theme;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class VideoItem {
    private String videoUrl;
    private String thumbnailUrl;
    private String title;

    public VideoItem(String videoUrl, String thumbnailUrl, String title) {
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void loadThumbnail(ImageView imageView, String thumbnailUrl) {
        Glide.with(imageView.getContext())
                .load(thumbnailUrl)
                .into(imageView);
    }
}
