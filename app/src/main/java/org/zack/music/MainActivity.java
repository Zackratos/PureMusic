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
            public boolean isPlaying() {
                return playBinder.isPlaying();
            }

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
                playBinder = (PlayService.PlayBinder) iBinder;
                PlayService service = playBinder.getPlayService();
                service.setCallBack(new PlayService.CallBack() {
                    @Override
                    public void setDuration(long duration) {
                        mmFragment.setDuration(duration);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(PlayService.newIntent(this), connection, BIND_AUTO_CREATE);
    }
}
