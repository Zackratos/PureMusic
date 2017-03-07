package org.zack.music;


import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private View tranWhole, girlWhole, innWhole;
    private SwitchCompat tranSwitch, girlSwitch, innSwitch;

    private ServiceConnection connection;
    private PlayService.PlayBinder playBinder;

    private int background;

    public SetupFragment() {
        // Required empty public constructor
    }

    public static SetupFragment newInstance() {
        SetupFragment fragment = new SetupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initService();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup, container, false);
        initView(view);
/*        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.finish();
            }
        });*/
        getActivity().bindService(PlayService.newIntent(getActivity()), connection, Context.BIND_AUTO_CREATE);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(connection);
    }

    private void initView(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.setup_toolbar).findViewById(R.id.toolbar);
        final AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
        parentActivity.setSupportActionBar(toolbar);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tranWhole = view.findViewById(R.id.tran_background_whole);
        girlWhole = view.findViewById(R.id.girl_background_whole);
        innWhole = view.findViewById(R.id.inn_background_whole);

        tranSwitch = (SwitchCompat) view.findViewById(R.id.tran_switch);
        girlSwitch = (SwitchCompat) view.findViewById(R.id.girl_switch);
        innSwitch = (SwitchCompat) view.findViewById(R.id.inn_switch);

        tranWhole.setOnClickListener(this);
        girlWhole.setOnClickListener(this);
        innWhole.setOnClickListener(this);



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tran_background_whole:
                background = PreferenceUtil.TRAN_BACKGROUND;
                playBinder.setBackgroundType(background);
                break;
            case R.id.girl_background_whole:
                background = PreferenceUtil.GIRL_BACKGROUND;
                playBinder.setBackgroundType(background);
                break;
            case R.id.inn_background_whole:
                background = PreferenceUtil.INN_BACKGROUND;
                playBinder.setBackgroundType(background);
                break;
            default:
                break;
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.tran_switch:
                if (isChecked) {
                    background = PreferenceUtil.TRAN_BACKGROUND;
                    playBinder.setBackgroundType(background);
                }
                break;
            case R.id.girl_switch:
                if (isChecked) {
                    background = PreferenceUtil.GIRL_BACKGROUND;
                    playBinder.setBackgroundType(background);
                }
                break;
            case R.id.inn_switch:
                if (isChecked) {
                    background = PreferenceUtil.INN_BACKGROUND;
                    playBinder.setBackgroundType(background);
                }
                break;
            default:
                break;
        }
    }


    private void initService() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playBinder = (PlayService.PlayBinder) service;
                background = playBinder.getBackgroundType();
                initBackgroundSwitch();
                playBinder.setSetupCallBack(new PlayService.SetupCallBack() {
                    @Override
                    public void onBackgroundChange(int background) {
                        initBackgroundSwitch();
                    }
                });

                tranSwitch.setOnCheckedChangeListener(SetupFragment.this);
                girlSwitch.setOnCheckedChangeListener(SetupFragment.this);
                innSwitch.setOnCheckedChangeListener(SetupFragment.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                playBinder.setSetupCallBack(null);
            }
        };
    }


    private void initTranSwitch() {
        tranSwitch.setChecked(background == PreferenceUtil.TRAN_BACKGROUND);
    }

    private void initGirlSwitch() {
        girlSwitch.setChecked(background == PreferenceUtil.GIRL_BACKGROUND);
    }

    private void initInnSwitch() {
        innSwitch.setChecked(background == PreferenceUtil.INN_BACKGROUND);
    }

    private void initBackgroundSwitch() {
        initTranSwitch();
        initGirlSwitch();
        initInnSwitch();
    }

}
