package org.zack.music;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class PlayService extends Service {


    private MainCallBack mainCallBack;
    private SetupCallBack setupCallBack;

    private MediaPlayer mp;
    private int current;
    private int last;
    private List<Music> musics;
    private boolean random;
    private int cycle;
    private int background;
    private boolean showLyric;

    private Handler handler;
    private Runnable runnable;


//    private RemoteViews rv;

//    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver notificationReceiver;

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

        initBroadcastReceiver(playBinder);
        return playBinder;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        current = PreferenceUtil.getCurrent(this);
        last = current;
        random = PreferenceUtil.isRandom(this);
        cycle = PreferenceUtil.getCycle(this);
        background = PreferenceUtil.getBackground(this);
        showLyric = PreferenceUtil.getShowLyric(this);

        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (musics != null && current < musics.size()) {
//                    changeMusic(musics.get(++current));
                    if (cycle == PreferenceUtil.SINGLE_CYCLE) {
                        changeMusic(current);
                    } else {
                        if (random) {
                            setRandom();
                        } else {
                            setNext();
                        }
                    }
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
                    changeMusic(musics.get(current));
                }

            }
        }.execute();*/
/*        musics = getMusicList();
        if (musics.size() > 0)
        changeMusic(musics.get(current));*/





        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mainCallBack != null) {
                    mainCallBack.updateTime(mp.getCurrentPosition());
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
        PreferenceUtil.putBackground(this, background);
        PreferenceUtil.putShowLyric(this, showLyric);
        handler.removeCallbacks(runnable);

        unregisterReceiver(notificationReceiver);
    }


    private void initBroadcastReceiver(final PlayBinder playBinder) {
//        broadcastManager = LocalBroadcastManager.getInstance(this);
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TAG", "onReceive");
                if (intent != null) {
                    if (intent.getAction().equals(getPackageName() + "PLAY")) {
                        Log.d("TAG", "play");
                        playBinder.clickPlay();
                    } else if (intent.getAction().equals(getPackageName() + "NEXT")) {
                        playBinder.clickNext();
                    } else if (intent.getAction().equals(getPackageName() + "PREVIOUS")) {
                        playBinder.clickPrevious();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName() + "PLAY");
        filter.addAction(getPackageName() + "NEXT");
        filter.addAction(getPackageName() + "PREVIOUS");

        registerReceiver(notificationReceiver, filter);
    }


    private void popNotification(final Music music) {
//        if (musics != null && musics.size() > current) {
//            Music music = musics.get(current);
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {

                Intent intent = MainActivity.newIntent(PlayService.this);
                PendingIntent pi = PendingIntent.getActivity(PlayService.this, 0, intent, 0);
                RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification_layout);

                rv.setImageViewResource(R.id.notification_play, mp.isPlaying() ? R.drawable.pause_icon : R.drawable.play_icon);
                rv.setTextViewText(R.id.notification_title, music.getTitle() == null ? music.getName() : music.getTitle());
                rv.setTextViewText(R.id.notification_artist, music.getArtist());
                rv.setImageViewBitmap(R.id.notification_cover, (Bitmap) message.obj);

                Intent playIntent = new Intent(getPackageName() + "PLAY");
                PendingIntent playPi = PendingIntent.getBroadcast(PlayService.this, 0, playIntent, 0);
                Intent nextIntent = new Intent(getPackageName() + "NEXT");
                PendingIntent nextPi = PendingIntent.getBroadcast(PlayService.this, 0, nextIntent, 0);
                Intent previousIntent = new Intent(getPackageName() + "PREVIOUS");
                PendingIntent previousPi = PendingIntent.getBroadcast(PlayService.this, 0, previousIntent, 0);

                rv.setOnClickPendingIntent(R.id.notification_play, playPi);
                rv.setOnClickPendingIntent(R.id.notification_next, nextPi);
                rv.setOnClickPendingIntent(R.id.notification_previous, previousPi);


                Notification notification = new NotificationCompat.Builder(PlayService.this)
                        .setContentTitle(music.getTitle() == null ? music.getName() : music.getTitle())
                        .setContentText(music.getArtist())
                        .setLargeIcon((Bitmap) message.obj)
                        .setContentIntent(pi)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setCustomBigContentView(rv)
                        .build();

                startForeground(1, notification);
                return false;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Music.createAlbumArt(music.getPath());
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.album_icon);
                }

                Message msg = handler.obtainMessage();
                msg.obj = bitmap;
                handler.sendMessage(msg);
            }
        }).start();


//        }
    }


    private void changeMusic(int current) {
        Music music = musics.get(current);
        Log.d("TAG", "musicPath = " + music.getPath());
        if (mainCallBack != null) {
            mainCallBack.onMusicChange(music);
            Log.d("TAG", "mainCallBack != null");
        }
        try {
            if (mp == null) {
                Log.d("TAG", "mp == null");
            }
            mp.reset();
            Log.d("TAG", "mp.reset");
            mp.setDataSource(music.getPath());
            Log.d("TAG", "mp.changeMusic");
            mp.prepare();
            Log.d("TAG", "mp.prepare");
        } catch (IOException e) {
            e.printStackTrace();
        }

        popNotification(music);
    }


    private void startPlay() {
        mp.start();


        handler.removeCallbacks(runnable);
        handler.post(runnable);
    }

    private void pausePlay() {
        mp.pause();

        handler.removeCallbacks(runnable);
    }

    private void setNext() {
        if (cycle == PreferenceUtil.ALL_CYCLE) {
            last = current;
            if (musics.size() - 1 > current) {
                current++;
            } else {
                current = 0;
            }
            changeMusic(current);
        } else {
            if (musics.size() - 1 > current) {
                last = current;
                changeMusic(++current);
            }
        }
    }

    private void setRandom() {
        last = current;
        current = new Random().nextInt(musics.size());
        changeMusic(current);
    }


    class PlayBinder extends Binder {

        public PlayService getPlayService() {
            return PlayService.this;
        }

        public boolean isPlaying() {
            return mp.isPlaying();
        }

        public boolean isRandom() {
            return random;
        }

        public int getCycle() {
            return cycle;
        }

        public boolean getShowLyric() {
            return showLyric;
        }

        public void clickPlay() {
            if (musics != null && musics.size() > current) {
                if (mp.isPlaying()) {
                    pausePlay();
                } else {
                    startPlay();
                }
            }

            if (mainCallBack != null) {
                mainCallBack.initPlayView(mp.isPlaying());
            }

            popNotification(musics.get(current));

        }

        public void clickNext() {
            boolean isPlaying = mp.isPlaying();
            if (musics != null && musics.size() > current) {
                if (random) {
                    setRandom();
                } else {
                    setNext();
                }
            }
            if (isPlaying) {
                startPlay();
            }
        }

        public void clickPrevious() {
            boolean isPlaying = mp.isPlaying();
            if (musics != null && musics.size() > current) {
//                changeMusic(musics.get(--current));
                if (random) {
                    setRandom();
                } else {
                    if (cycle == PreferenceUtil.ALL_CYCLE) {
                        last = current;
                        if (current > 0) {
                            current--;
                        } else {
                            current = musics.size() - 1;
                        }
                        changeMusic(current);
                    } else {
                        if (current > 0) {
                            last = current;
                            changeMusic(--current);
                        }
                    }
                }
            }
            if (isPlaying) {
                startPlay();
            }
        }

        public void clickRandom() {
            random = !random;
            if (mainCallBack != null) {
                mainCallBack.initRandomView(random);
            }
        }

        public void clickCycle() {
            if (cycle == PreferenceUtil.NO_CYCLE) {
                cycle = PreferenceUtil.ALL_CYCLE;
            } else if (cycle == PreferenceUtil.ALL_CYCLE) {
                cycle = PreferenceUtil.SINGLE_CYCLE;
            } else {
                cycle = PreferenceUtil.NO_CYCLE;
            }

            if (mainCallBack != null) {
                mainCallBack.initCycleView(cycle);
            }
        }

        public void clickLyric() {
            showLyric = !showLyric;
            if (mainCallBack != null) {
                mainCallBack.initShowLyric(showLyric);
            }
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
                last = current;
                current = position;
                changeMusic(current);
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
                        if (mainCallBack != null) {
                            mainCallBack.setMusics(musics);
                        }
                        changeMusic(current);
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

        public int getCurrent() {
            return current;
        }

        public int getLast() {
            return last;
        }

        public List<Music> getMusicList() {
            return musics;
        }

        public void setMainCallBack(MainCallBack mainCallBack) {
            PlayService.this.mainCallBack = mainCallBack;
        }

        public MainCallBack getCallBack() {
            return PlayService.this.mainCallBack;
        }

        public int getBackground() {
            return background;
        }

        public void popServiceNotification() {
            popNotification(musics.get(current));
        }

        public void setBackground(int background) {
            PlayService.this.background = background;
            if (setupCallBack != null) {
                setupCallBack.onBackgroundChange(PlayService.this.background);
            }
            if (mainCallBack != null) {
                mainCallBack.onBackgroundTypeChange(PlayService.this.background);
            }

        }

        public void setSetupCallBack(SetupCallBack setupCallBack) {
            PlayService.this.setupCallBack = setupCallBack;
        }
    }


    public interface MainCallBack {
        void onMusicChange(Music music);
        void initPlayView(boolean isPlaying);
        void initCycleView(int cycle);
        void initRandomView(boolean random);
        void initShowLyric(boolean showLyric);
        void updateTime(int time);
        void setMusics(List<Music> musics);
        void onBackgroundTypeChange(int background);
    }

    public interface SetupCallBack {
        void onBackgroundChange(int background);
    }

/*    public void setCallBack(CallBack mainCallBack) {
        this.mainCallBack = mainCallBack;
    }*/

}
