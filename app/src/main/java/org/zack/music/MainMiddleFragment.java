package org.zack.music;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MainMiddleListener listener;
    private List<Music> musics;

    private boolean playing;
    private int current;
    private boolean random;
    private int cycle;
    private ServiceConnection connection;
    private PlayService.PlayBinder playBinder;

    private ImageView playView;
    private ImageView forwardView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainMiddleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainMiddleFragment newInstance() {
        MainMiddleFragment fragment = new MainMiddleFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        current = PreferenceUtil.getCurrent(getActivity());
        cycle = PreferenceUtil.getCyclePlay(getActivity());
        random = PreferenceUtil.isRandomPlay(getActivity());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        playing = false;
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                playBinder = (PlayService.PlayBinder) iBinder;
                PlayService service = playBinder.getPlayService();
                current = service.current;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        getActivity().bindService(PlayService.newIntent(getActivity()), connection, Context.BIND_AUTO_CREATE);
//        getActivity().startService(PlayService.newIntent(getActivity()));
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
        getActivity().unbindService(connection);
        if (!playBinder.isPlaying()) {
            getActivity().stopService(PlayService.newIntent(getActivity()));
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.middle_play:
                if (playBinder.isPlaying()) {
                    playBinder.pause();
                } else {
//                    playBinder.play();
                    if (musics != null) {
                        playBinder.play(musics.get(current));
                    }
                }
                playing = !playing;
                playView.setImageResource(playBinder.isPlaying() ? R.drawable.pause_icon : R.drawable.play_icon);
                break;

            case R.id.middle_forward:
                if (musics != null && playBinder.isPlaying()) {
                    if (random) {

                    } else {
                        switch (cycle) {
                            case PreferenceUtil.NO_CYCLE:
                                if (current < musics.size() - 1) {
                                    playBinder.play(musics.get(++current));
                                } else {
                                    playBinder.play(musics.get(0));
                                }
                                break;

                            case PreferenceUtil.ALL_CYCLE:
                                break;
                            case PreferenceUtil.SINGER_CYCLE:
                                break;
                            default:
                                break;
                        }

                    }
                }
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
        forwardView = (ImageView) view.findViewById(R.id.middle_forward);
        playView.setOnClickListener(this);
        forwardView.setOnClickListener(this);
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


    }

    public void setMainMiddleListener(MainMiddleListener listener) {
        this.listener = listener;
    }


    public void setMusicList(List<Music> musics) {
        this.musics = musics;
    }
}
