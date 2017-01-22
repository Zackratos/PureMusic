package org.zack.music;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.preference.Preference;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;

public class PlayService extends Service {
    MediaPlayer mp;
    public int current;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        return intent;
    }

    public PlayService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        current = PreferenceUtil.getCurrent(this);
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

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

    class PlayBinder extends Binder {

        public PlayService getPlayService() {
            return PlayService.this;
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
            }
        }

        public void stop() {

        }

        public void next() {

        }

        public void previous() {

        }
    }

}
