package org.zack.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    @BindView(R.id.main_drawer)
    DrawerLayout dl;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private boolean hadBind;
    private ServiceConnection connection;
    private PlayService.PlayBinder playBinder;

    private PlayFragment playFragment;
    private MusicListFragment mlFragment;

    private ImageView backgroundView;

    private MenuItem lyricItem;


    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }




    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initService();
        initView();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            requestRunTimePermission(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }


    @Override
    protected void grantedPermission() {
        hadBind = bindService(PlayService.newIntent(this), connection, BIND_AUTO_CREATE);
        startService(PlayService.newIntent(this));
    }

    @Override
    protected void deniedPermission(List<String> deniedPermissions) {

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        lyricItem = menu.findItem(R.id.menu_lyric);
        if (playBinder != null) {
            initLyricItem(playBinder.isShowLyric());
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
            playBinder.onClickLyric();
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
        if (hadBind) {
            unbindService(connection);
        }
        if (playBinder != null && !playBinder.isPlaying()) {
            stopService(PlayService.newIntent(this));
        }
    }


    private void initView() {
        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        backgroundView = new ImageView(this);

        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        backgroundView.setLayoutParams(params);
        backgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        decorView.addView(backgroundView, 0);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar).findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



//        dl = (DrawerLayout) findViewById(R.id.main_drawer);
        FragmentManager fm = getSupportFragmentManager();
        playFragment = PlayFragment.newInstance();
        mlFragment = MusicListFragment.newInstance();
        playFragment.setPlayCallBack(new PlayFragment.PlayCallBack() {

            @Override
            public void onClickPlay() {
                playBinder.onClickPlay();
            }

            @Override
            public void onClickNext() {
                playBinder.onClickNext();
            }

            @Override
            public void onClickPrevious() {
                playBinder.onClickPrevious();
            }

            @Override
            public void onClickRandom() {
                playBinder.onClickRandom();
            }

            @Override
            public void onClickCycle() {
                playBinder.onClickCycle();
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
                playBinder.onItemClickPosition(position);
                dl.closeDrawer(Gravity.LEFT, true);
//                playFragment.onItemClickPosition(position);
            }
        });

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.main_middle, playFragment);
        ft.add(R.id.main_left, mlFragment);
        ft.commit();

        ActionBarDrawerToggle abdt = new ActionBarDrawerToggle(this, dl, toolbar, 0, 0);
        dl.addDrawerListener(abdt);
        abdt.syncState();
    }

    private void setBackgroundInn(final String path) {
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final byte[] model = Music.getAlbumByte(path);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(MainActivity.this)
                                    .load(model)
                                    .into(backgroundView);
                        }
                    });
                }
            }).start();
/*            final Handler handler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    backgroundView.setImageBitmap((Bitmap) message.obj);
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
            }).start();*/
    }

    private void setBackgroundGirl(final int random) {

        Glide.with(MainActivity.this)
                .load(getResources().getIdentifier
                        ("background_" + random, "drawable", getPackageName()))
                .into(backgroundView);

/*        final Handler handler = new Handler(new Handler.Callback() {
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
        }).start();*/

    }

    private void setBackgroundTran() {
        backgroundView.setImageDrawable(null);
    }


    private void setBackground(int backgroundType, String path) {
        if (backgroundType == PreferenceUtil.TRAN_BACKGROUND) {
            setBackgroundTran();
        } else if (backgroundType == PreferenceUtil.GIRL_BACKGROUND) {
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
                    public void onMusicChange(int current, Music music, int last, int backgroundType) {
                        playFragment.setDuration(music.getDuration());
                        setTitle(TextUtils.isEmpty(music.getTitle()) ? music.getName() : music.getTitle());
                        setBackground(backgroundType, music.getPath());
                        playFragment.initLyricView(music.getPath().replace(".mp3", ".lrc").replace(".wma", ".lrc"));
                        mlFragment.initRecyclerViewPosition(current);
                        mlFragment.initRecyclerViewItemDisplay(current, last);
                    }

                    @Override
                    public void initPlayView(boolean isPlaying) {
                        playFragment.initPlayView(isPlaying);
                    }

                    @Override
                    public void initCycleView(int cycle) {
                        playFragment.initCycleView(cycle);
                    }

                    @Override
                    public void initRandomView(boolean random) {
                        playFragment.initRandomView(random);
                    }

                    @Override
                    public void initShowLyric(boolean showLyric) {
                        playFragment.initShowLyric(showLyric);
//                        lyricItem.setIcon(showLyric ? R.drawable.lyric_icon_on : R.drawable.lyric_icon_off);
                        initLyricItem(showLyric);
                    }

                    @Override
                    public void updateUI(int time) {
                        playFragment.updateUI(time);
                    }

                    @Override
                    public void setMusics(List<Music> musics) {
                        mlFragment.setMusics(musics);
                    }

                    @Override
                    public void onBackgroundTypeChange(int backgroundType, String path) {
                        setBackground(backgroundType, path);
/*                        if (playBinder.getCurrentMusic() != null) {
                            setBackground(backgroundType, playBinder.getCurrentMusic().getPath());
                        }*/
                    }
                });

                if (playBinder.isRunning()) {
                    Music music = playBinder.getCurrentMusic();
                    if (music != null) {
                        playFragment.setDuration(music.getDuration());
                        setTitle(TextUtils.isEmpty(music.getTitle()) ? music.getName() : music.getTitle());
                        setBackground(playBinder.getBackgroundType(), music.getPath());
                        playFragment.initLyricView(music.getPath().replace(".mp3", ".lrc").replace(".wma", ".lrc"));
                    }
                    playFragment.initPlayView(playBinder.isPlaying());
                    initInterface();
//                    playFragment.initRandomView(playBinder.isRandom());
//                    playFragment.initCycleView(playBinder.getCycle());
//                    playFragment.initShowLyric(playBinder.isShowLyric());
//                    initLyricItem(playBinder.isShowLyric());

                    mlFragment.setMusics(playBinder.getMusicList());
                    mlFragment.initRecyclerViewPosition(playBinder.getCurrent());
                    mlFragment.initRecyclerViewItemDisplay(playBinder.getCurrent(), playBinder.getLast());
                } else {
                    playBinder.setRunning(true);
                    playBinder.onInitMusicList();
                    initInterface();
//                    playFragment.initRandomView(playBinder.isRandom());
//                    playFragment.initCycleView(playBinder.getCycle());
//                    playFragment.initShowLyric(playBinder.isShowLyric());
//                    initLyricItem(playBinder.isShowLyric());
                }

/*                List<Music> musics = playBinder.getMusicList();

                if (musics == null) {
                    playBinder.onInitMusicList();
                    playFragment.initRandomView(playBinder.isRandom());
                    playFragment.initCycleView(playBinder.getCycle());
                    playFragment.initShowLyric(playBinder.isShowLyric());
                    initLyricItem(playBinder.isShowLyric());
                } else {
                    Music music = playBinder.getCurrentMusic();
                    playFragment.setDuration(music.getDuration());
                    setTitle(TextUtils.isEmpty(music.getTitle()) ? music.getName() : music.getTitle());
                    setBackground(playBinder.getBackgroundType(), music.getPath());

                    playFragment.initPlayView(playBinder.isPlaying());
                    playFragment.initRandomView(playBinder.isRandom());
                    playFragment.initCycleView(playBinder.getCycle());
                    playFragment.initShowLyric(playBinder.isShowLyric());
                    initLyricItem(playBinder.isShowLyric());
                    playFragment.initLyricView(music.getPath().replace(".mp3", ".lrc").replace(".wma", ".lrc"));
                    mlFragment.setMusics(musics);
                    mlFragment.initRecyclerViewPosition(playBinder.getCurrent());
                    mlFragment.initRecyclerViewItemDisplay(playBinder.getCurrent(), playBinder.getLast());
                }*/

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                playBinder.setMainCallBack(null);
            }
        };

    }



    private void initInterface() {
        playFragment.initRandomView(playBinder.isRandom());
        playFragment.initCycleView(playBinder.getCycle());
        playFragment.initShowLyric(playBinder.isShowLyric());
        initLyricItem(playBinder.isShowLyric());
    }
}
