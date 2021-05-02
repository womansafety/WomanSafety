package com.ws.womansafety.Guardian;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.ws.womansafety.R;
import com.ws.womansafety.databinding.ActivityGuardianMainBinding;
import com.ws.womansafety.utils.SharedPrefConst;
import com.ws.womansafety.utils.SharedPrefUtils;

public class GuardianMainActivity extends AppCompatActivity {

    ActivityGuardianMainBinding mBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_guardian_main);
        setSupportActionBar(mBinding.toolbarGuardian);
        getSupportActionBar().setTitle("Hi, " + SharedPrefUtils.getStringData(SharedPrefConst.USER_NAME));


        Intent intent = new Intent(this, MapsActivity.class);
        //startActivity(intent);

        mBinding.btnNextActivity.setOnClickListener(v -> {
            startActivity(intent);
        });
    }


}