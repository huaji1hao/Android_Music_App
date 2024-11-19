package com.example.cwk_mwe.models;

import java.io.Serializable;

public class MusicCard implements Serializable {
    public String title;
    public String artist;
    public String album;
    public String duration;
    public String path;
    public int progress;

    public MusicCard(String title, String artist, String album, String duration, String path, int progress) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.path = path;
        this.progress = progress;
    }

    public MusicCard() {
    }
}
