package com.ws.womansafety.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ws.womansafety.Guardian.GuardianMainActivity;
import com.ws.womansafety.R;
import com.ws.womansafety.luncher.LauncherActivity;
import com.ws.womansafety.model.User;
import com.ws.womansafety.user.UserMainActivity;
import com.ws.womansafety.utils.SharedPrefConst;
import com.ws.womansafety.utils.SharedPrefUtils;


public class SplashActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setFlags();

        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(SharedPrefUtils.getStringData(SharedPrefConst.USER_TYPE).isEmpty())
               gotoLaunchActivity();
                else if(SharedPrefUtils.getStringData(SharedPrefConst.USER_TYPE).equalsIgnoreCase(getString(R.string.str_user))){
                    gotoUserActivity();
                }else {
                    gotoGuardianActivity();
                }

            }
        }, 500);


    }

    private void gotoLaunchActivity() {
        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
        finish();
    }
    private void gotoUserActivity() {
        Intent intent = new Intent(this, UserMainActivity.class);
        startActivity(intent);
        finish();
    }
    private void gotoGuardianActivity() {
        Intent intent = new Intent(this, GuardianMainActivity.class);
        startActivity(intent);
        finish();
    }

}
