package org.zack.music;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupFragment extends Fragment implements View.OnClickListener {


    private View tranWhole, girlWhole, innWhole;
    private SwitchCompat tranSwitch, girlSwitch, innSwitch;

    public SetupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SetupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetupFragment newInstance() {
        SetupFragment fragment = new SetupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        return view;
    }

    private void initView(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.setup_toolbar).findViewById(R.id.toolbar);
        final AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
        parentActivity.setSupportActionBar(toolbar);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tranWhole = view.findViewById(R.id.transParent_background_whole);
        girlWhole = view.findViewById(R.id.girl_background_whole);
        innWhole = view.findViewById(R.id.inn_background_whole);

        tranSwitch = (SwitchCompat) view.findViewById(R.id.transParent_switch);
        girlSwitch = (SwitchCompat) view.findViewById(R.id.girl_switch);
        innSwitch = (SwitchCompat) view.findViewById(R.id.inn_switch);

        tranWhole.setOnClickListener(this);
        girlWhole.setOnClickListener(this);
        innWhole.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.transParent_background_whole:
                tranSwitch.setChecked(!tranSwitch.isChecked());
                break;
            case R.id.girl_background_whole:
                girlSwitch.setChecked(!girlSwitch.isChecked());
                break;
            case R.id.inn_background_whole:
                innSwitch.setChecked(!innSwitch.isChecked());
                break;
            default:
                break;
        }
    }


    private void initTranSwitch() {

    }

    private void initGirlSwitch() {

    }

    private void initInnSwitch() {

    }
}
