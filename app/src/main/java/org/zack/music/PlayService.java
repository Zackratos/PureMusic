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
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayService extends Service {

    private CallBack callBack;

    private MediaPlayer mp;
    private int current;
    private List<Music> musics;
    private boolean random;

    private Handler handler;
    private Runnable runnable;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        return intent;
    }

    public PlayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("TAG", "onBind");
        PlayBinder playBinder = new PlayBinder();

        return playBinder;
    }

    @Override
    public void onCreate() {
        Log.d("TAG", "onCreate");
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                musics = getMusicList();
            }
        }).start();
        current = PreferenceUtil.getCurrent(this);

        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (musics != null && current < musics.size() -1) {
                    setDataSource(musics.get(++current));
//                    mp.start();
                    startPlay();
                }
            }
        });

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                musics = getMusicList();
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean && musics.size() > 0) {
                    setDataSource(musics.get(current));
                }

            }
        }.execute();




        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("sjlf")
                .setContentText("sjldfkj")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("TAG", "updateTime");
                callBack.updateTime(mp.getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        };
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
        handler.removeCallbacks(runnable);
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


    private void setDataSource(Music music) {
        if (callBack != null) {
            callBack.onMusicChange(music);
            Log.d("TAG", "callBack != null");
        }
        try {
            mp.reset();
            mp.setDataSource(music.getPath());
            mp.prepare();
            Log.d("TAG", "mp.prepare");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startPlay() {
        mp.start();
        if (callBack != null) {
            callBack.initPlayView(true);
        }

        handler.removeCallbacks(runnable);
        handler.post(runnable);
    }

    private void pausePlay() {
        mp.pause();
        if (callBack != null) {
            callBack.initPlayView(false);
        }
        handler.removeCallbacks(runnable);
    }


    class PlayBinder extends Binder {

//        private boolean isPause;


        public PlayService getPlayService() {
            return PlayService.this;
        }

        public boolean isPlaying() {
            return mp.isPlaying();
        }

        public void clickPlay() {
            if (musics.size() > 0) {
                if (mp.isPlaying()) {
                    pausePlay();
                } else {
                    startPlay();
                }
            }
        }

        public void clickNext() {
            boolean isPlaying = mp.isPlaying();
            if (current < musics.size() - 1) {
                setDataSource(musics.get(++current));
            }
            if (isPlaying) {
//                mp.start();
                startPlay();
            }
        }

        public void clickPrevious() {
            boolean isPlaying = mp.isPlaying();
            if (current > 0) {
                setDataSource(musics.get(--current));
            }
            if (isPlaying) {
//                mp.start();
                startPlay();
            }
        }

        public void clickRandom() {

        }

        public void clickCycle() {

        }

        public void progressChange(int progress) {
            mp.seekTo(progress * 1000);
        }

        public void clickPosition(int position) {
            if (musics != null && musics.size() > 0) {
                boolean isPlaying = mp.isPlaying();
                setDataSource(musics.get(position));
                current = position;
                if (isPlaying) {
//                    mp.start();
                    startPlay();
                }
            }
        }

        public Music getMusic() {
            if (musics != null && musics.size() > 0) {
                return musics.get(current);
            }
            return null;
        }

    }

    public interface CallBack {
//        void setDuration(long duration);
        void onMusicChange(Music music);
        void initPlayView(boolean isPlaying);
        void updateTime(int time);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

}
