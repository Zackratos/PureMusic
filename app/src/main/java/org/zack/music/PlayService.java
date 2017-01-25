package org.zack.music;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayService extends Service {
    MediaPlayer mp;
    private int current;
    private List<Music> musics;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        return intent;
    }

    public PlayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        PlayBinder playBinder = new PlayBinder();

        return playBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                musics = getMusicList();
            }
        }).start();
        mp = new MediaPlayer();
        current = PreferenceUtil.getCurrent(this);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("sjlf")
                .setContentText("sjldfkj")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
        PreferenceUtil.putCurrent(this, current);
    }


    private List<Music> getMusicList() {
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<Music> musics = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                while (cursor.moveToNext()) {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    long duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    Music music = new Music.MusicBuilder()
                            .title(title)
                            .album(album)
                            .path(path)
                            .name(name)
                            .duration(duration)
                            .builder();
                    musics.add(music);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return musics;
    }


    private Bitmap createAlbumArt(String filePath) {
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


    class PlayBinder extends Binder {

        private boolean isPause;


        public PlayService getPlayService() {
            return PlayService.this;
        }

        public void setDataSource(String path) {
            try {
                mp.reset();
                mp.setDataSource(path);
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void play() {
            mp.start();
        }

        public void play(Music music) {
            play(music, 0);
        }

        public void play(Music music, int position) {
            try {
                mp.setDataSource(music.getPath());
                mp.prepare();
                mp.seekTo(position);
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isPlaying() {
            return mp.isPlaying();
        }

        public void pause() {
            if (mp.isPlaying()) {
                mp.pause();
                mp.getCurrentPosition();
                isPause = true;
            }
        }

        public boolean isPause() {
            return isPause;
        }

        public void stop() {

        }

        public void next() {
            boolean isPlaying = isPlaying();
            if (current < musics.size() - 1) {
                setDataSource(musics.get(++current).getPath());
            }
            if (isPlaying) {
                play();
            }
        }

        public void previous() {
            boolean isPlaying = isPlaying();
            setDataSource(musics.get(--current).getPath());
            if (isPlaying) {
                play();
            }
        }
    }

}
