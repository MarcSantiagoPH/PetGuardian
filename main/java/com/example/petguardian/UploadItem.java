package com.example.petguardian;

public class UploadItem {
    private final String name;
    private final String uri;
    private final boolean isImage;
    private final boolean isVideo;
    private final String mimeType;

    public UploadItem(String name, String uri, boolean isImage, boolean isVideo, String mimeType) {
        this.name = name;
        this.uri = uri;
        this.isImage = isImage;
        this.isVideo = isVideo;
        this.mimeType = mimeType;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public boolean isImage() {
        return isImage;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public String getMimeType() {
        return mimeType;
    }
}

