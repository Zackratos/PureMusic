package org.zack.music;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2017/1/2.
 */
public class Music {
    private String title;
    private String album;
    private String artist;
    private String path;
    private Bitmap background;
    private int duration;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private Music(MusicBuilder builder) {
        title = builder.title;
        album = builder.album;
        artist = builder.artist;
        background = builder.background;
        duration = builder.duration;

    }

    public Bitmap getBackground() {
        return background;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



    public static class MusicBuilder {
        private String title;
        private String album;
        private String artist;
        private Bitmap background;
        private int duration;
        private long size;

        public MusicBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MusicBuilder album(String album) {
            this.album = album;
            return this;
        }

        public MusicBuilder artist(String artist) {
            this.artist = artist;
            return this;
        }

        public MusicBuilder background(Bitmap background) {
            this.background = background;
            return this;
        }

        public MusicBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Music builder() {
            return new Music(this);
        }
    }
}
