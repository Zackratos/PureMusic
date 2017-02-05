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

//    private boolean playing;
//    private int current;
    private int position = 0;
    private boolean random;
    private int cycle;

    private ImageView playView;
    private ImageView previousView;
    private ImageView nextView;
    private TextView durationView, bottomDurationView;


    public static MainMiddleFragment newInstance() {
        MainMiddleFragment fragment = new MainMiddleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cycle = PreferenceUtil.getCyclePlay(getActivity());
        random = PreferenceUtil.isRandomPlay(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
                    playView.setImageResource(listener.isPlaying() ? R.drawable.pause_icon : R.drawable.play_icon);
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
        durationView = (TextView) view.findViewById(R.id.middle_duration);
        bottomDurationView = (TextView) view.findViewById(R.id.middle_bottom_duration);
//        durationView.setText(getDurationText(listener.getDuration()));
        playView.setOnClickListener(this);
        previousView.setOnClickListener(this);
        nextView.setOnClickListener(this);
        final DiscreteSeekBar lineSeekBar = (DiscreteSeekBar) view.findViewById(R.id.middle_line_seekBar);
        final CircularSeekBar circleSeekBar = (CircularSeekBar) view.findViewById(R.id.middle_circle_seekBar);
        lineSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
//                circleSeekBar.setProgress(value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        circleSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }
        });


        return view;
    }

    interface MainMiddleListener {
        boolean isPlaying();
        void clickPlay();
        void clickNext();
        void clickPrevious();
        void clickRandom();
        void clickCycle();

        long getDuration(int position);
    }

    public void setMainMiddleListener(MainMiddleListener listener) {
        this.listener = listener;
    }

    public void clickPosition(int position) {
        playView.setImageResource(R.drawable.pause_icon);
//        String durationText = getDurationText(listener.getDuration(position));
//        durationView.setText(durationText);
//        bottomDurationView.setText(durationText);
    }

    public void setDuration(long duration) {
        String durationText = getDurationText(duration);
        durationView.setText(durationText);
        bottomDurationView.setText(durationText);
    }


    private long getDurationSecond(long duration) {
        return Math.round(duration / 1000);
    }

    private String getDurationText(long duration) {

        long secondCount = getDurationSecond(duration);
        long minute = secondCount / 60;
        long second = secondCount % 60;
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
