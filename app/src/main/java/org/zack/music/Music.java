package org.zack.music;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2017/1/2.
 */
public class Music {
    private String title;
    private String name;
    private String album;
    private String artist;
    private String path;
    private Bitmap image;
    private long duration;

    private Music(MusicBuilder builder) {
        title = builder.title;
        name = builder.name;
        album = builder.album;
        artist = builder.artist;
        path = builder.path;
        image = builder.image;
        duration = builder.duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
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

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static class MusicBuilder {
        private String title;
        private String name;
        private String album;
        private String artist;
        private String path;
        private Bitmap image;
        private long duration;

        public MusicBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MusicBuilder name(String name) {
            this.name = name;
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

        public MusicBuilder path(String path) {
            this.path = path;
            return this;
        }

        public MusicBuilder image(Bitmap image) {
            this.image = image;
            return this;
        }

        public MusicBuilder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public Music builder() {
            return new Music(this);
        }
    }
}
