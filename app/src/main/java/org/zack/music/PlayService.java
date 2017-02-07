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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
    private int cycle;

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
        PlayBinder playBinder = new PlayBinder();

        return playBinder;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        current = PreferenceUtil.getCurrent(this);
        random = PreferenceUtil.isRandom(this);
        cycle = PreferenceUtil.getCycle(this);

        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (musics != null && current < musics.size() -1) {
                    setDataSource(musics.get(++current));
                    startPlay();
                }
            }
        });

/*        new AsyncTask<Void, Void, Boolean>() {
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
        }.execute();*/
/*        musics = getMusicList();
        if (musics.size() > 0)
        setDataSource(musics.get(current));*/



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
                if (callBack != null) {
                    callBack.updateTime(mp.getCurrentPosition());
                    handler.postDelayed(this, 500);
                }
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
        PreferenceUtil.putRandom(this, random);
        PreferenceUtil.putCycle(this, cycle);
        handler.removeCallbacks(runnable);
    }


/*    private List<Music> getMusicList() {
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
    }*/


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

        public PlayService getPlayService() {
            return PlayService.this;
        }

        public boolean isPlaying() {
            return mp.isPlaying();
        }

        public void clickPlay() {
            if (musics != null && musics.size() > current) {
                if (mp.isPlaying()) {
                    pausePlay();
                } else {
                    startPlay();
                }
            }
        }

        public void clickNext() {
            boolean isPlaying = mp.isPlaying();
            if (musics != null && current < musics.size() - 1) {
                setDataSource(musics.get(++current));
            }
            if (isPlaying) {
                startPlay();
            }
        }

        public void clickPrevious() {
            boolean isPlaying = mp.isPlaying();
            if (musics != null && current > 0) {
                setDataSource(musics.get(--current));
            }
            if (isPlaying) {
                startPlay();
            }
        }

        public void clickRandom() {
            random = !random;
        }

        public void clickCycle() {

        }

        public void onStartTrackingTouch() {
            if (musics != null && musics.size() > current) {
                handler.removeCallbacks(runnable);
            }
        }

        public void onStopTrackingTouch(int progress) {
            if (musics != null && musics.size() > current) {
                mp.seekTo(progress * 1000);
                if (mp.isPlaying()) {
                    handler.post(runnable);
                }
            }
        }

        public void clickPosition(int position) {
            if (musics != null && musics.size() > position) {
                boolean isPlaying = mp.isPlaying();
                setDataSource(musics.get(position));
                current = position;
                if (isPlaying) {
                    startPlay();
                }
            }
        }


        public void initMusicList() {

            final Handler handler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == 0 && musics != null && musics.size() > current) {
                        setDataSource(musics.get(current));
                        if (callBack != null) {
                            callBack.setMusics(musics);
                        }
                    }
                    return false;
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (musics == null) {
                        musics = Music.getMusicList(PlayService.this);
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }
                }
            }).start();
        }

        public Music getCurrentMusic() {
            if (musics != null && musics.size() > current) {
                return musics.get(current);
            }
            return null;
        }

        public List<Music> getMusicList() {
            return musics;
        }

        public void setCallBack(CallBack callBack) {
            PlayService.this.callBack = callBack;
        }

        public CallBack getCallBack() {
            return PlayService.this.callBack;
        }
    }


    public interface CallBack {
        void onMusicChange(Music music);
        void initPlayView(boolean isPlaying);
        void updateTime(int time);
        void setMusics(List<Music> musics);
    }

/*    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }*/

}
