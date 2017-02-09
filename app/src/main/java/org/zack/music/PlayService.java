package org.zack.music;

import android.app.Notification;
import android.app.PendingIntent;
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
//                    setDataSource(musics.get(++current));
                    if (cycle == PreferenceUtil.SINGLE_CYCLE) {
                        setDataSource(current);
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
                    setDataSource(musics.get(current));
                }

            }
        }.execute();*/
/*        musics = getMusicList();
        if (musics.size() > 0)
        setDataSource(musics.get(current));*/





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
    }



    private void popNotification(final Music music) {
//        if (musics != null && musics.size() > current) {
//            Music music = musics.get(current);
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                Intent intent = MainActivity.newIntent(PlayService.this);
                PendingIntent pi = PendingIntent.getActivity(PlayService.this, 0, intent, 0);
                Notification notification = new NotificationCompat.Builder(PlayService.this)
                        .setContentTitle(music.getTitle() == null ? music.getName() : music.getTitle())
                        .setContentText(music.getArtist())
                        .setLargeIcon((Bitmap) message.obj)
                        .setContentIntent(pi)
                        .setSmallIcon(R.drawable.notification_icon)
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


    private void setDataSource(int current) {
        Music music = musics.get(current);
        if (mainCallBack != null) {
            mainCallBack.onMusicChange(music);
            Log.d("TAG", "mainCallBack != null");
        }
        try {
            mp.reset();
            mp.setDataSource(music.getPath());
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
            setDataSource(current);
        } else {
            if (musics.size() - 1 > current) {
                last = current;
                setDataSource(++current);
            }
        }
    }

    private void setRandom() {
        last = current;
        current = new Random().nextInt(musics.size());
        setDataSource(current);
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
//                setDataSource(musics.get(--current));
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
                        setDataSource(current);
                    } else {
                        if (current > 0) {
                            last = current;
                            setDataSource(--current);
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
                setDataSource(current);
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
                        setDataSource(current);
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
