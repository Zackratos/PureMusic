package org.zack.music;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.File;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainMiddleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainMiddleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMiddleFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {


    private MainMiddleListener listener;


    private ImageView playView;
    private ImageView previousView;
    private ImageView nextView;
    private ImageView cycleView, randomView;
    private TextView durationView, bottomDurationView;
    private TextView currentView, bottomCurrentView;
    private LyricView lyricView;

    private DiscreteSeekBar lineSeekBar;
    private CircularSeekBar circleSeekBar;


    public static MainMiddleFragment newInstance() {
        MainMiddleFragment fragment = new MainMiddleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return initView(inflater, container);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.middle_play:
                if (listener != null) {
                    listener.clickPlay();
                }
                break;

            case R.id.middle_next:
                if (listener != null) {
                    listener.clickNext();
                }
                break;

            case R.id.middle_previous:
                if (listener != null) {
                    listener.clickPrevious();
                }
                break;

            case R.id.middle_cycle:
                if (listener != null) {
                    listener.clickCycle();
                }
                break;

            case R.id.middle_random:
                if (listener != null) {
                    listener.clickRandom();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_main_middle, container, false);
        playView = (ImageView) view.findViewById(R.id.middle_play);
        previousView = (ImageView) view.findViewById(R.id.middle_previous);
        nextView = (ImageView) view.findViewById(R.id.middle_next);
        cycleView = (ImageView) view.findViewById(R.id.middle_cycle);
        randomView = (ImageView) view.findViewById(R.id.middle_random);
        durationView = (TextView) view.findViewById(R.id.middle_duration);
        bottomDurationView = (TextView) view.findViewById(R.id.middle_bottom_duration);
        currentView = (TextView) view.findViewById(R.id.middle_current_time);
        bottomCurrentView = (TextView) view.findViewById(R.id.middle_bottom_current_time);
        lyricView = (LyricView) view.findViewById(R.id.middle_lyric);


        playView.setOnClickListener(this);
        previousView.setOnClickListener(this);
        nextView.setOnClickListener(this);
        cycleView.setOnClickListener(this);
        randomView.setOnClickListener(this);

        lineSeekBar = (DiscreteSeekBar) view.findViewById(R.id.middle_line_seekBar);
        circleSeekBar = (CircularSeekBar) view.findViewById(R.id.middle_circle_seekBar);
        lineSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    circleSeekBar.setProgress(value);
                    String durationText = getDurationText(value);
                    currentView.setText(durationText);
                    bottomCurrentView.setText(durationText);
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                listener.onStartTrackingTouch();
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                listener.onStopTrackingTouch(seekBar.getProgress());
            }
        });

        circleSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    lineSeekBar.setProgress(progress);
                    String durationText = getDurationText(progress);
                    currentView.setText(durationText);
                    bottomCurrentView.setText(durationText);
                }
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                listener.onStopTrackingTouch(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
                listener.onStartTrackingTouch();
            }
        });


        return view;
    }

    interface MainMiddleListener {

        void clickPlay();

        void clickNext();

        void clickPrevious();

        void clickRandom();

        void clickCycle();

        void onStartTrackingTouch();

        void onStopTrackingTouch(int progress);

    }

    public void setMainMiddleListener(MainMiddleListener listener) {
        this.listener = listener;
    }


    public void setDuration(long duration) {
        int durationSecond = getDurationSecond(duration);
        String durationText = getDurationText(durationSecond);

        durationView.setText(durationText);
        bottomDurationView.setText(durationText);

        lineSeekBar.setMax(durationSecond);
        circleSeekBar.setMax(durationSecond);

        lineSeekBar.setProgress(0);
        circleSeekBar.setProgress(0);

        currentView.setText("00:00");
        bottomCurrentView.setText("00:00");
    }


    public void initPlayView(boolean isPlaying) {
        if (isPlaying) {
            playView.setImageResource(R.drawable.pause_icon);
        } else {
            playView.setImageResource(R.drawable.play_icon);
        }
    }

    public void initCycleView(int cycle) {
        if (cycle == PreferenceUtil.NO_CYCLE) {
            cycleView.setImageResource(R.drawable.cycle_icon_off);
        } else if (cycle == PreferenceUtil.ALL_CYCLE) {
            cycleView.setImageResource(R.drawable.cycle_icon_on);
        } else {
            cycleView.setImageResource(R.drawable.cycle_icon_single);
        }
    }

    public void initRandomView(boolean random) {
        randomView.setImageResource(random ? R.drawable.random_icon_on : R.drawable.random_icon_off);
    }

    public void initLyricView(String path) {
        try {
            lyricView.setLyricFile(new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTime(int time) {
        int durationSecond = getDurationSecond(time);
        String durationText = getDurationText(durationSecond);
        currentView.setText(durationText);
        bottomCurrentView.setText(durationText);
        lineSeekBar.setProgress(durationSecond);
        circleSeekBar.setProgress(durationSecond);

        lyricView.setCurrentTimeMillis(time);
    }



    private int getDurationSecond(long duration) {
        return Math.round(duration / 1000);
    }

    private String getDurationText(int durationSecond) {

        long minute = durationSecond / 60;
        long second = durationSecond % 60;
        String minuteText;
        String secondText;
        if (minute < 10) {
            minuteText = "0" + String.valueOf(minute);
        } else {
            minuteText = String.valueOf(minute);
        }
        if (second < 10) {
            secondText = "0" + String.valueOf(second);
        } else {
            secondText = String.valueOf(second);
        }
        return new StringBuilder()
                .append(minuteText)
                .append(":")
                .append(secondText)
                .toString();
    }
}
