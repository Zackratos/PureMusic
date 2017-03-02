package org.zack.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;
import java.util.Random;

public class MainActivity extends BaseActivity {

    private DrawerLayout dl;
    private ServiceConnection connection;
    private PlayService.PlayBinder playBinder;

    private PlayFragment mmFragment;
    private MusicListFragment mlFragment;

    private ImageView backgroundView;

    private MenuItem lyricItem;

    private boolean hasBindService;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return intent;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initService();
        initView();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.
                READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            hasBindService = bindService(PlayService.newIntent(this), connection, BIND_AUTO_CREATE);
            startService(PlayService.newIntent(this));
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        lyricItem = menu.findItem(R.id.menu_lyric);
        if (playBinder != null) {
            initLyricItem(playBinder.getShowLyric());
        }
        return super.onPrepareOptionsMenu(menu);
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
        } else if (item.getItemId() == R.id.menu_lyric) {
            playBinder.clickLyric();
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
        if (hasBindService) {
            unbindService(connection);
        }
        if (playBinder != null && !playBinder.isPlaying()) {
            stopService(PlayService.newIntent(this));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasBindService = bindService(PlayService.newIntent(this), connection, BIND_AUTO_CREATE);
                startService(PlayService.newIntent(this));
            } else {
                finish();
            }
        }
    }

    private void initView() {
        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        backgroundView = new ImageView(this);
//        backgroundView.setImageResource(R.drawable.background_1);
        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        backgroundView.setLayoutParams(params);
        backgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        decorView.addView(backgroundView, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar).findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        lyricItem = (MenuItem) findViewById(R.id.menu_lyric);


        dl = (DrawerLayout) findViewById(R.id.main_drawer);
        FragmentManager fm = getSupportFragmentManager();
        mmFragment = PlayFragment.newInstance();
        mlFragment = MusicListFragment.newInstance();
        mmFragment.setMainMiddleListener(new PlayFragment.MainMiddleListener() {

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
            public void onStartTrackingTouch() {
                playBinder.onStartTrackingTouch();
            }

            @Override
            public void onStopTrackingTouch(int progress) {
                playBinder.onStopTrackingTouch(progress);
            }

        });

        mlFragment.setMainLeftListener(new MusicListFragment.MainLeftListener() {
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

    private void setBackgroundInn(final String path) {
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                backgroundView.setImageBitmap((Bitmap)message.obj);
                return false;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Music.createAlbumArt(path);
                Message msg = handler.obtainMessage();
                msg.obj = bitmap;
                handler.sendMessage(msg);
            }
        }).start();

    }

    private void setBackgroundGirl(final int random) {
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                backgroundView.setImageDrawable((Drawable) msg.obj);
                return false;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Drawable drawable = getResources().getDrawable(getResources()
                        .getIdentifier("background_" + random, "drawable", getPackageName()));
                Message msg = handler.obtainMessage();
                msg.obj = drawable;
                handler.sendMessage(msg);
            }
        }).start();
/*        switch (random) {
            case 0:
//                backgroundView.setImageResource(R.drawable.background_0);
                backgroundView.setImageDrawable(getResources().getDrawable(R.drawable.background_0));
                break;
            case 1:
//                backgroundView.setImageResource(R.drawable.background_1);
                backgroundView.setImageDrawable(getResources().getDrawable(R.drawable.background_1));
                break;
            case 2:
//                backgroundView.setImageResource(R.drawable.background_2);
                backgroundView.setImageDrawable(getResources().getDrawable(R.drawable.background_2));
                break;
            case 3:
//                backgroundView.setImageResource(R.drawable.background_3);
                backgroundView.setImageDrawable(getResources().getDrawable(R.drawable.background_3));
                break;
            case 4:
//                backgroundView.setImageResource(R.drawable.background_4);
                backgroundView.setImageDrawable(getResources().getDrawable(R.drawable.background_4));
                break;
            case 5:
//                backgroundView.setImageResource(R.drawable.background_5);
                backgroundView.setImageDrawable(getResources().getDrawable(R.drawable.background_5));
                break;
            case 6:
//                backgroundView.setImageResource(R.drawable.background_6);
                backgroundView.setImageDrawable(getResources().getDrawable(R.drawable.background_6));
                break;
            case 7:
//                backgroundView.setImageResource(R.drawable.background_7);
                backgroundView.setImageDrawable(getResources().getDrawable(R.drawable.background_7));
                break;
            case 8:
//                backgroundView.setImageResource(R.drawable.background_8);
                backgroundView.setImageDrawable(getResources().getDrawable(R.drawable.background_8));
                break;
            case 9:
//                backgroundView.setImageResource(R.drawable.background_9);
                backgroundView.setImageDrawable(getResources().getDrawable(R.drawable.background_9));
                break;
            default:
                break;
        }*/
    }

    private void setBackgroundTran() {
        backgroundView.setImageDrawable(null);
    }


    private void setBackground(int background, String path) {
        if (background == PreferenceUtil.TRAN_BACKGROUND) {
            setBackgroundTran();
        } else if (background == PreferenceUtil.GIRL_BACKGROUND) {
            setBackgroundGirl(new Random().nextInt(10));
        } else {
            setBackgroundInn(path);
        }
    }

    private void initLyricItem(boolean showLyric) {
        if (lyricItem !=  null) {
            lyricItem.setIcon(showLyric ? R.drawable.lyric_icon_on : R.drawable.lyric_icon_off);
        }
    }


    private void initService() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                playBinder = (PlayService.PlayBinder) iBinder;

                playBinder.setMainCallBack(new PlayService.MainCallBack() {
                    @Override
                    public void onMusicChange(Music music) {
                        mmFragment.setDuration(music.getDuration());
                        setTitle(TextUtils.isEmpty(music.getTitle()) ? music.getName() : music.getTitle());
                        setBackground(playBinder.getBackground(), music.getPath());
                        mmFragment.initLyricView(music.getPath().replace(".mp3", ".lrc").replace(".wma", ".lrc"));
                        mlFragment.initRecyclerViewPosition(playBinder.getCurrent());
                        mlFragment.initRecyclerViewItemDisplay(playBinder.getCurrent(), playBinder.getLast());
                    }

                    @Override
                    public void initPlayView(boolean isPlaying) {
                        mmFragment.initPlayView(isPlaying);
                    }

                    @Override
                    public void initCycleView(int cycle) {
                        mmFragment.initCycleView(cycle);
                    }

                    @Override
                    public void initRandomView(boolean random) {
                        mmFragment.initRandomView(random);
                    }

                    @Override
                    public void initShowLyric(boolean showLyric) {
                        mmFragment.initShowLyric(showLyric);
//                        lyricItem.setIcon(showLyric ? R.drawable.lyric_icon_on : R.drawable.lyric_icon_off);
                        initLyricItem(showLyric);
                    }

                    @Override
                    public void updateTime(int time) {
                        mmFragment.updateTime(time);
                    }

                    @Override
                    public void setMusics(List<Music> musics) {
                        mlFragment.setMusics(musics);
                    }

                    @Override
                    public void onBackgroundTypeChange(int background) {
                        if (playBinder.getCurrentMusic() != null) {
                            setBackground(background, playBinder.getCurrentMusic().getPath());
                        }
                    }
                });

                List<Music> musics = playBinder.getMusicList();

                if (musics == null) {
                    playBinder.initMusicList();
                    mmFragment.initRandomView(playBinder.isRandom());
                    mmFragment.initCycleView(playBinder.getCycle());
                    mmFragment.initShowLyric(playBinder.getShowLyric());
                    initLyricItem(playBinder.getShowLyric());
                } else {
                    Music music = playBinder.getCurrentMusic();
                    mmFragment.setDuration(music.getDuration());
                    setTitle(TextUtils.isEmpty(music.getTitle()) ? music.getName() : music.getTitle());
                    setBackground(playBinder.getBackground(), music.getPath());

                    mmFragment.initPlayView(playBinder.isPlaying());
                    mmFragment.initRandomView(playBinder.isRandom());
                    mmFragment.initCycleView(playBinder.getCycle());
                    mmFragment.initShowLyric(playBinder.getShowLyric());
                    initLyricItem(playBinder.getShowLyric());
                    mmFragment.initLyricView(music.getPath().replace(".mp3", ".lrc").replace(".wma", ".lrc"));
                    mlFragment.setMusics(musics);
                    mlFragment.initRecyclerViewPosition(playBinder.getCurrent());
                    mlFragment.initRecyclerViewItemDisplay(playBinder.getCurrent(), playBinder.getLast());
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                playBinder.setMainCallBack(null);
            }
        };

    }
}
