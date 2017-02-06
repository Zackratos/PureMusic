package org.zack.music;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

public class MainActivity extends BaseActivity {

    private DrawerLayout dl;
    private ServiceConnection connection;
    private PlayService.PlayBinder playBinder;

    private MainMiddleFragment mmFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initService();
        initView();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_setup) {
            startActivity(SetupActivity.newIntent(this));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (dl.isDrawerOpen(Gravity.LEFT)) {
            dl.closeDrawer(Gravity.LEFT, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        if (!playBinder.isPlaying()) {
            stopService(PlayService.newIntent(this));
        }
    }

    private void initView() {
        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        ImageView backgroundView = new ImageView(this);
        backgroundView.setImageResource(R.drawable.background_1);
        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        backgroundView.setLayoutParams(params);
        backgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        decorView.addView(backgroundView, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar).findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dl = (DrawerLayout) findViewById(R.id.main_drawer);
        FragmentManager fm = getSupportFragmentManager();
        mmFragment = MainMiddleFragment.newInstance();
        MainLeftFragment mlFragment = MainLeftFragment.newInstance();
        mmFragment.setMainMiddleListener(new MainMiddleFragment.MainMiddleListener() {

            @Override
            public void clickPlay() {
                playBinder.clickPlay();
            }

            @Override
            public void clickNext() {
                playBinder.clickNext();
            }

            @Override
            public void clickPrevious() {
                playBinder.clickPrevious();
            }

            @Override
            public void clickRandom() {
                playBinder.clickRandom();
            }

            @Override
            public void clickCycle() {
                playBinder.clickCycle();
            }

            @Override
            public void progressChange(int progress) {
                playBinder.progressChange(progress);
            }
        });

        mlFragment.setMainLeftListener(new MainLeftFragment.MainLeftListener() {
            @Override
            public void clickPosition(int position) {
                playBinder.clickPosition(position);
                dl.closeDrawer(Gravity.LEFT, true);
//                mmFragment.clickPosition(position);
            }
        });

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.main_middle, mmFragment);
        ft.add(R.id.main_left, mlFragment);
        ft.commit();

        ActionBarDrawerToggle abdt = new ActionBarDrawerToggle(this, dl, toolbar, 0, 0);
        dl.addDrawerListener(abdt);
        abdt.syncState();
    }


    private void initService() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d("TAG", "onServiceConnected");
                playBinder = (PlayService.PlayBinder) iBinder;
                Music music = playBinder.getMusic();
                if (music != null) {
                    mmFragment.setDuration(music.getDuration());
                    setTitle(TextUtils.isEmpty(music.getTitle()) ? music.getName() : music.getTitle());
                    mmFragment.initPlayView(playBinder.isPlaying());
                }


                PlayService service = playBinder.getPlayService();

                service.setCallBack(new PlayService.CallBack() {
                    @Override
                    public void onMusicChange(Music music) {
                        mmFragment.setDuration(music.getDuration());
                        setTitle(TextUtils.isEmpty(music.getTitle()) ? music.getName() : music.getTitle());
                    }

                    @Override
                    public void initPlayView(boolean isPlaying) {
                        mmFragment.initPlayView(isPlaying);
                    }

                    @Override
                    public void updateTime(int time) {
                        mmFragment.updateTime(time);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(PlayService.newIntent(this), connection, BIND_AUTO_CREATE);
        startService(PlayService.newIntent(this));
    }
}
