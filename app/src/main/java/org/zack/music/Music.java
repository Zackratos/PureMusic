package org.zack.music;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/2.
 */
public class Music {
    private String title;
    private String name;
    private String album;
    private String artist;
    private String path;
    private byte[] model;
//    private Bitmap image;
    private long duration;

    private Music(MusicBuilder builder) {
        title = builder.title;
        name = builder.name;
        album = builder.album;
        artist = builder.artist;
        path = builder.path;
        model = builder.model;
//        image = builder.image;
        duration = builder.duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getModel() {
        return model;
    }

    public void setModel(byte[] model) {
        this.model = model;
    }

    /*    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }*/

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


    public static byte[] getAlbumByte(String filePath) {
        if (filePath != null) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(filePath);
                return retriever.getEmbeddedPicture();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    retriever.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }


    public static Bitmap createAlbumArt(String filePath) {
        Bitmap bitmap = null;
        //能够获取多媒体文件元数据的类
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath); //设置数据源
            byte[] art = retriever.getEmbeddedPicture(); //得到字节型数据
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length); //转换为图片
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static List<Music> getMusicList(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<Music> musics = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    long duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    if (duration >= 20 * 1000) {
                        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        Music music = new MusicBuilder()
                                .title(title)
                                .album(album)
                                .path(path)
                                .name(name)
                                .artist(artist)
                                .duration(duration)
                                .builder();
                        musics.add(music);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return musics;
    }




    public static class MusicBuilder {
        private String title;
        private String name;
        private String album;
        private String artist;
        private String path;
        private byte[] model;
//        private Bitmap image;
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


        public MusicBuilder model(byte[] model) {
            this.model = model;
            return this;
        }
/*        public MusicBuilder image(Bitmap image) {
            this.image = image;
            return this;
        }*/

        public MusicBuilder duration(long duration) {
            this.duration = duration;
            return this;
        }

        public Music builder() {
            return new Music(this);
        }
    }
}
