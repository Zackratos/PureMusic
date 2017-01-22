package org.zack.music;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SetupActivity extends SingleFragmentActivity {


    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, SetupActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return SetupFragment.newInstance();
    }

}
