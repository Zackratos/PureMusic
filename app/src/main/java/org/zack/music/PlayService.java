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
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class PlayService extends Service {


    private boolean running;
    private boolean hadLoadMusics;

    private MainCallBack mainCallBack;
//    private SetupCallBack setupCallBack;

    private MediaPlayer mp;
    private int current;
    private int last;
    private List<Music> musics;
    private boolean random;
    private int cycle;
    private int backgroundType;
    private boolean showLyric;

    private Handler updateUIHandler;
    private Runnable updateUIRunnable;


    private BroadcastReceiver notificationReceiver;

    public static Intent newIntent(Context context) {
        return new Intent(context, PlayService.class);
    }

    public PlayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.d("TAG", "onBind");
        PlayBinder playBinder = new PlayBinder();

//        initBroadcastReceiver(playBinder);
        return playBinder;
    }




    @Override
    public void onCreate() {

        super.onCreate();

        current = PreferenceUtil.getCurrent(this);
        last = current;
        random = PreferenceUtil.isRandom(this);
        cycle = PreferenceUtil.getCycle(this);
        backgroundType = PreferenceUtil.getBackgroundType(this);
        showLyric = PreferenceUtil.getShowLyric(this);

        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (cycle == PreferenceUtil.SINGLE_CYCLE) {
                    changeMusic();
                } else {
                    if (random) {
                        changeMusicRandom();
                    } else {
                        changeMusicNext();
                    }
                }
                startPlay();
/*                if (musics != null && current < musics.size()) {
                    if (cycle == PreferenceUtil.SINGLE_CYCLE) {
                        changeMusic(current);
                    } else {
                        if (random) {
                            changeMusicRandom();
                        } else {
                            changeMusicNext();
                        }
                    }
                    startPlay();
                }*/
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


        initBroadcastReceiver();


        updateUIHandler = new Handler();
        updateUIRunnable = new Runnable() {
            @Override
            public void run() {
                if (mainCallBack != null) {
                    mainCallBack.updateUI(mp.getCurrentPosition());
                    updateUIHandler.postDelayed(this, 500);
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
        PreferenceUtil.putBackgroundType(this, backgroundType);
        PreferenceUtil.putShowLyric(this, showLyric);

        updateUIHandler.removeCallbacks(updateUIRunnable);

        unregisterReceiver(notificationReceiver);
    }


    private void initMusicList() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                musics = Music.getMusicList(PlayService.this);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (musics != null && musics.size() > current) {
                            hadLoadMusics = true;
                            if (mainCallBack != null) {
                                mainCallBack.setMusics(musics);
                            }
                        }
                        changeMusic();
                        popNotification(musics.get(current));
                    }
                });
            }
        }).start();
    }


    private void initBroadcastReceiver() {
//        broadcastManager = LocalBroadcastManager.getInstance(this);
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TAG", "onReceive");
                if (intent != null) {
                    if (intent.getAction().equals(getPackageName() + "PLAY")) {
                        clickPlay();
//                        playBinder.onClickPlay();
                    } else if (intent.getAction().equals(getPackageName() + "NEXT")) {
                        clickNext();
//                        playBinder.onClickNext();
                    } else if (intent.getAction().equals(getPackageName() + "PREVIOUS")) {
                        clickPrevious();
//                        playBinder.onClickPrevious();
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
        Intent intent = MainActivity.newIntent(PlayService.this);
        PendingIntent pi = PendingIntent.getActivity(PlayService.this, 0, intent, 0);
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification_layout);

        rv.setImageViewResource(R.id.notification_play, mp.isPlaying() ? R.drawable.pause_icon : R.drawable.play_icon);
        rv.setTextViewText(R.id.notification_title, music.getTitle() == null ? music.getName() : music.getTitle());
        rv.setTextViewText(R.id.notification_artist, music.getArtist());

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
//                .setLargeIcon((Bitmap) message.obj)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.notification_icon)
                .setCustomBigContentView(rv)
                .build();

        startForeground(1, notification);

        final NotificationTarget bigTarget = new NotificationTarget(
                this, rv, R.id.notification_cover, notification, 1
        );

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final byte[] model = Music.getAlbumByte(music.getPath());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(PlayService.this)
                                .load(model)
                                .asBitmap()
                                .error(R.drawable.album_icon)
                                .into(bigTarget);
                    }
                });

            }
        }).start();


/*        final Handler handler = new Handler(new Handler.Callback() {
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
        }).start();*/


//        }
    }


    private void changeMusic() {
        if (hadLoadMusics) {
            Music music = musics.get(current);
            Log.d("TAG", "musicPath = " + music.getPath());
            if (mainCallBack != null) {
                mainCallBack.onMusicChange(current, music, last, backgroundType);
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



//            hadLoadMusics = true;

        }

//        hadLoadMusics = false;
    }


    private void startPlay() {

        mp.start();
        updateUIHandler.removeCallbacks(updateUIRunnable);
        updateUIHandler.post(updateUIRunnable);

        popNotification(musics.get(current));
    }

    private void pausePlay() {


        mp.pause();

        updateUIHandler.removeCallbacks(updateUIRunnable);

        popNotification(musics.get(current));
    }

    private void changeMusicNext() {
        if (cycle == PreferenceUtil.ALL_CYCLE) {
            last = current;
            if (musics.size() - 1 > current) {
                current++;
            } else {
                current = 0;
            }
            changeMusic();
        } else {
            if (musics.size() - 1 > current) {
                last = current;
                current++;
                changeMusic();
            }
        }
    }

    private void changeMusicPrevious() {
        if (cycle == PreferenceUtil.ALL_CYCLE) {
            last = current;
            if (current > 0) {
                current--;
            } else {
                current = musics.size() - 1;
            }
            changeMusic();
        } else {
            if (current > 0) {
                last = current;
                current--;
                changeMusic();
            }
        }
/*        int temp = current;
        current = last;
        last = temp;
        changeMusic();*/
    }

    private void changeMusicRandom() {
        last = current;
        current = new Random().nextInt(musics.size());
        changeMusic();
    }


    private void clickPlay() {
        if (hadLoadMusics) {
            if (mp.isPlaying()) {
                pausePlay();
            } else {
                startPlay();
            }


            if (mainCallBack != null) {
                mainCallBack.initPlayView(mp.isPlaying());
            }



        }
    }


    private void clickNext() {

//        startPlay();
        boolean isPlaying = mp.isPlaying();
        if (random) {
            changeMusicRandom();
        } else {
            changeMusicNext();
        }
        if (isPlaying) {
            startPlay();
        }
    }

    private void clickPrevious() {
        boolean isPlaying = mp.isPlaying();
        if (random) {
            changeMusicRandom();
        } else {
            changeMusicPrevious();
        }
        if (isPlaying) {
            startPlay();
        }
/*        changeMusicPrevious();
        if (isPlaying) {
            startPlay();
        }*/
    }


    class PlayBinder extends Binder {

        public PlayService getPlayService() {
            return PlayService.this;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            PlayService.this.running = running;
        }

        public boolean hadLoadMusics() {
            return hadLoadMusics;
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

        public boolean isShowLyric() {
            return showLyric;
        }

        public void onClickPlay() {
            clickPlay();

        }



        public void onClickNext() {
            clickNext();
        }

        public void onClickPrevious() {
            clickPrevious();
/*            boolean isPlaying = mp.isPlaying();
            if (musics != null && musics.size() > current) {
                if (random) {
                    changeMusicRandom();
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
            }*/
        }

        public void onClickRandom() {
            random = !random;
            if (mainCallBack != null) {
                mainCallBack.initRandomView(random);
            }
        }

        public void onClickCycle() {
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

        public void onClickLyric() {
            showLyric = !showLyric;
            if (mainCallBack != null) {
                mainCallBack.initShowLyric(showLyric);
            }
        }

        public void onStartTrackingTouch() {
            if (musics != null && musics.size() > current) {
                updateUIHandler.removeCallbacks(updateUIRunnable);
            }
        }

        public void onStopTrackingTouch(int progress) {
            if (musics != null && musics.size() > current) {
                mp.seekTo(progress * 1000);
                if (mp.isPlaying()) {
                    updateUIHandler.post(updateUIRunnable);
                }
            }
        }

        public void onItemClickPosition(int position) {
            if (musics != null && musics.size() > position) {
                boolean isPlaying = mp.isPlaying();
                last = current;
                current = position;
                changeMusic();
                if (isPlaying) {
                    startPlay();
                }
            }
        }


        public void onInitMusicList() {
            PlayService.this.initMusicList();
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

        public int getBackgroundType() {
            return backgroundType;
        }

        public void popServiceNotification() {
            popNotification(musics.get(current));
        }

        public void setBackgroundType(int background) {
            PlayService.this.backgroundType = background;
/*            if (setupCallBack != null) {
                setupCallBack.onBackgroundChange(PlayService.this.backgroundType);
            }*/
            if (mainCallBack != null) {
                String path = null;
                if (musics != null && musics.size() > current) {
                    path = musics.get(current).getPath();
                }
                mainCallBack.onBackgroundTypeChange(PlayService.this.backgroundType, path);
            }

        }

/*        public void setSetupCallBack(SetupCallBack setupCallBack) {
            PlayService.this.setupCallBack = setupCallBack;
        }*/
    }


    public interface MainCallBack {
        void onMusicChange(int position, Music music, int last, int backgroundType);
        void initPlayView(boolean isPlaying);
        void initCycleView(int cycle);
        void initRandomView(boolean random);
        void initShowLyric(boolean showLyric);
        void updateUI(int time);
        void setMusics(List<Music> musics);
        void onBackgroundTypeChange(int backgroundType, String path);
    }

/*    public interface SetupCallBack {
        void onBackgroundChange(int background);
    }*/

/*    public void setCallBack(CallBack mainCallBack) {
        this.mainCallBack = mainCallBack;
    }*/

}
