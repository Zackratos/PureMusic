package org.zack.music;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

public class MainActivity extends BaseActivity {

    private DrawerLayout dl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        immersionBar();
        setContentView(R.layout.activity_main);
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void immersionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            View decorView = window.getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            window.setNavigationBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
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
        final MainMiddleFragment mmFragment = MainMiddleFragment.newInstance();
        MainLeftFragment mlFragment = MainLeftFragment.newInstance();
        mmFragment.setMainMiddleListener(new MainMiddleFragment.MainMiddleListener() {

        });

        mlFragment.setMainLeftListener(new MainLeftFragment.MainLeftListener() {
            @Override
            public List<Music> getMusicList() {
                return null;
            }

            @Override
            public void putMusicList(List<Music> musics) {
                mmFragment.setMusicList(musics);
            }
        });

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.main_middle, mmFragment);
        ft.add(R.id.main_left, mlFragment);
        ft.commit();

        ActionBarDrawerToggle abdt = new ActionBarDrawerToggle(this, dl, toolbar, 0, 0);
        dl.addDrawerListener(abdt);
        abdt.syncState();
//        dl.openDrawer(Gravity.LEFT);
    }
}
