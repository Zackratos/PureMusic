package org.zack.music;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    private List<Music> musics;

//    private boolean playing;
//    private int current;
    private int position = 0;
    private boolean random;
    private int cycle;
//    private ServiceConnection connection;
//    private PlayService.PlayBinder playBinder;

    private ImageView playView;
    private ImageView previousView;
    private ImageView nextView;


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
                    if (listener.isPlaying()) {
                        listener.pause();
                    } else {
                        if (!listener.isPause() && musics != null && musics.size() > 0) {
//                            listener.setDataSource(musics.get(current).getPath());
                        }
                        listener.play();
                    }
                    playView.setImageResource(listener.isPlaying() ? R.drawable.pause_icon : R.drawable.play_icon);
                }
                break;

            case R.id.middle_next:
                listener.next();
                break;

            case R.id.middle_previous:
                listener.previous();
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
        boolean isPause();
        void setDataSource(String path);
        void play();
        void pause();
        void next();
        void previous();
    }

    public void setMainMiddleListener(MainMiddleListener listener) {
        this.listener = listener;
    }


    public void setMusicList(List<Music> musics) {
        this.musics = musics;
    }
}
