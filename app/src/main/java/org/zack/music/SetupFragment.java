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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupFragment extends Fragment {


//    private View tranWhole, girlWhole, innWhole;
//    private SwitchCompat tranSwitch, girlSwitch, innSwitch;

    @BindView(R.id.setup_background_group)
    RadioGroup backgroundGroup;

    @BindView(R.id.setup_trans)
    RadioButton transButton;

    @BindView(R.id.setup_girl)
    RadioButton girlButton;

    @BindView(R.id.setup_inn)
    RadioButton innButton;


/*    @OnCheckedChanged(R.id.setup_background_group)
    void onBackgroundChange(int i) {
        if (i == 0) {
            playBinder.setBackgroundType(PreferenceUtil.TRAN_BACKGROUND);
        } else if (i == 1) {
            playBinder.setBackgroundType(PreferenceUtil.GIRL_BACKGROUND);
        } else {
            playBinder.setBackgroundType(PreferenceUtil.INN_BACKGROUND);
        }
    }*/

    private boolean fromUser;

    private ServiceConnection connection;
    private PlayService.PlayBinder playBinder;

    private int backgroundType;

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
        ButterKnife.bind(this, view);
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

        backgroundGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.setup_girl) {
                    backgroundType = PreferenceUtil.GIRL_BACKGROUND;
                } else if (i == R.id.setup_inn) {
                    backgroundType = PreferenceUtil.INN_BACKGROUND;
                } else {
                    backgroundType = PreferenceUtil.TRAN_BACKGROUND;
                }
                if (fromUser) {
                    playBinder.setBackgroundType(backgroundType);
                }
//                initBackgroundType();
            }
        });
//        girlButton.setChecked(true);
/*        tranWhole = view.findViewById(R.id.tran_background_whole);
        girlWhole = view.findViewById(R.id.girl_background_whole);
        innWhole = view.findViewById(R.id.inn_background_whole);

        tranSwitch = (SwitchCompat) view.findViewById(R.id.tran_switch);
        girlSwitch = (SwitchCompat) view.findViewById(R.id.girl_switch);
        innSwitch = (SwitchCompat) view.findViewById(R.id.inn_switch);

        tranWhole.setOnClickListener(this);
        girlWhole.setOnClickListener(this);
        innWhole.setOnClickListener(this);*/



    }

/*    @Override
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
    }*/


    private void initService() {
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playBinder = (PlayService.PlayBinder) service;
                backgroundType = playBinder.getBackgroundType();
                initBackgroundType();
//                initBackgroundSwitch();


//                tranSwitch.setOnCheckedChangeListener(SetupFragment.this);
//                girlSwitch.setOnCheckedChangeListener(SetupFragment.this);
//                innSwitch.setOnCheckedChangeListener(SetupFragment.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }


/*    private void initTranSwitch() {
        tranSwitch.setChecked(background == PreferenceUtil.TRAN_BACKGROUND);
    }

    private void initGirlSwitch() {
        girlSwitch.setChecked(background == PreferenceUtil.GIRL_BACKGROUND);
    }

    private void initInnSwitch() {
        innSwitch.setChecked(background == PreferenceUtil.INN_BACKGROUND);
    }*/

/*    private void initBackgroundSwitch() {
        initTranSwitch();
        initGirlSwitch();
        initInnSwitch();
    }*/

    private void initBackgroundType() {
        if (backgroundType == PreferenceUtil.GIRL_BACKGROUND) {
            backgroundGroup.check(R.id.setup_girl);
        } else if (backgroundType == PreferenceUtil.INN_BACKGROUND) {
            backgroundGroup.check(R.id.setup_inn);
        } else {
            backgroundGroup.check(R.id.setup_trans);
        }

        fromUser = true;
    }

}
