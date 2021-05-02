package com.ws.womansafety;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ws.womansafety.Guardian.GuardianMainActivity;
import com.ws.womansafety.databinding.ActivityMainBinding;
import com.ws.womansafety.luncher.LauncherActivity;
import com.ws.womansafety.model.Guardian;
import com.ws.womansafety.model.User;
import com.ws.womansafety.user.UserMainActivity;
import com.ws.womansafety.utils.KeyBoardUtils;
import com.ws.womansafety.utils.SharedPrefConst;
import com.ws.womansafety.utils.SharedPrefUtils;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    ActivityMainBinding mBinding;
    private FirebaseAuth mAuth;
    String TAG = "MainActivity";
    String mVerificationId;
    int userType;
    String name, mobiile;
    DatabaseReference mFireBaseData;
    View parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        userType = getIntent().getExtras().getInt("type");
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(mBinding.toolbarSingUp);

        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (userType == 2) {
            String str_gu = getString(R.string.str_guardian);
            getSupportActionBar().setTitle(str_gu + " " + getString(R.string.str_register));
            mBinding.tvTitle.setText(getString(R.string.str_guardian_name));
            mBinding.tvMobile.setText(getString(R.string.str_guardian_no));
        } else {
            String str_user = getString(R.string.str_user);
            getSupportActionBar().setTitle(str_user + " " + getString(R.string.str_register));
            mBinding.tvTitle.setText(getString(R.string.str_user_name));
            mBinding.tvMobile.setText(getString(R.string.str_user_no));
        }


        mBinding.btnSendOTP.setOnClickListener(v -> {
            KeyBoardUtils.hideKeyBoard(this);
            name = mBinding.editTextName.getText().toString();
            mobiile = mBinding.editTextMobileNo.getText().toString();

            if (name.isEmpty() || mobiile.isEmpty())
                displaySnackBar(getString(R.string.str_error_all_field), false);
            else {
                if (mobiile.length() < 10) {
                    displaySnackBar(getString(R.string.str_error_mobile_length), false);
                } else {
                    mBinding.progressBar.setVisibility(View.VISIBLE);
                    sendOTP();
                    // sendVerificationCode("+91" +mobiile);
                }
            }
        });
        mBinding.btnVerifyOTP.setOnClickListener(v -> {
            KeyBoardUtils.hideKeyBoard(this);
            mBinding.progressBar.setVisibility(View.VISIBLE);
            verifyCode(mBinding.editTextOtp.getText().toString());
        });

        parentLayout = findViewById(android.R.id.content);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void sendVerificationCode(String mobileNo) {

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(mobileNo)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Instant verification is applied and a credential is directly returned.
                        // ...
                        Log.d(TAG, "onVerificationCompleted:" + credential);
                        if (credential.getSmsCode() != null) {
                            mBinding.tvOtp.setVisibility(View.VISIBLE);
                            mBinding.editTextOtp.setVisibility(View.VISIBLE);
                            mBinding.editTextOtp.setText(credential.getSmsCode());
                            verifyCode(credential.getSmsCode());
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                        mBinding.progressBar.setVisibility(View.INVISIBLE);
                        Log.w(TAG, "onVerificationFailed", e);

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            displaySnackBar("Invalid request", false);
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            displaySnackBar("The SMS quota for the project has been exceeded", false);
                        } else
                            displaySnackBar("" + e.getMessage(), false);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        Log.d(TAG, "onCodeSent:" + verificationId);

                        // Save verification ID and resending token so we can use them later
                        mVerificationId = verificationId;
                        mBinding.tvOtp.setVisibility(View.VISIBLE);
                        mBinding.editTextOtp.setVisibility(View.VISIBLE);
                        mBinding.btnVerifyOTP.setVisibility(View.VISIBLE);
                        mBinding.progressBar.setVisibility(View.INVISIBLE);
                        mBinding.btnSendOTP.setVisibility(View.GONE);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(
                mVerificationId,
                code
        );
        checkVerificationCode(credential);
    }

    private void checkVerificationCode(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addUser();
            } else {
                mBinding.progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addUser() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        if (userType == 2) {
            String token = SharedPrefUtils.getStringData(SharedPrefConst.AUTH_KEY);
            Guardian guardian = new Guardian(token, name, mobiile);
            mDatabase.child(getString(R.string.str_guardian)).child(mobiile).setValue(guardian);
            // Toast.makeText(this, "Guradian Added Successfully", Toast.LENGTH_SHORT).show();
            displaySnackBar(getString(R.string.str_guardian_add), true);
            SharedPrefUtils.saveStringData(SharedPrefConst.USER_TYPE, getString(R.string.str_guardian));
            SharedPrefUtils.saveStringData(SharedPrefConst.USER_NAME, name);
            SharedPrefUtils.saveStringData(SharedPrefConst.USER_NO, mobiile);
            gotoGuardianActivity();
        } else {
            User user = new User(name, mobiile);
            mDatabase.child(getString(R.string.str_user)).child(mobiile).setValue(user);
            displaySnackBar(getString(R.string.str_user_add), true);

            SharedPrefUtils.saveStringData(SharedPrefConst.USER_TYPE, getString(R.string.str_user));
            SharedPrefUtils.saveStringData(SharedPrefConst.USER_NAME, name);
            SharedPrefUtils.saveStringData(SharedPrefConst.USER_NO, mobiile);
            gotoUserActivity();
        }
    }

    private void navigateToLauncherActivity() {
        Intent intent = new Intent(this, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void gotoGuardianActivity() {
        Intent intent = new Intent(this, GuardianMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void gotoUserActivity() {
        Intent intent = new Intent(this, UserMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToLauncherActivity();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToLauncherActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void sendOTP() {
        if (userType == 2)
            mFireBaseData = FirebaseDatabase.getInstance().getReference(getString(R.string.str_guardian)).child(mobiile);
        else
            mFireBaseData = FirebaseDatabase.getInstance().getReference(getString(R.string.str_user)).child(mobiile);

        mFireBaseData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    mBinding.progressBar.setVisibility(View.INVISIBLE);
                    displaySnackBar(getString(R.string.str_error_mob_already_register), false);
                } else {
                    displaySnackBar(getString(R.string.str_code_send) + mobiile.substring(6), true);
                    sendVerificationCode("+91" + mobiile);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Toast.makeText(RegisterActivity.this, "FirebaseDatabase Error ", Toast.LENGTH_SHORT).show();
                displaySnackBar("FirebaseDatabase Error ", false);
                Log.e(TAG, error.getMessage());
            }
        });

    }

    public void displaySnackBar(String message, boolean isSuccess) {
        int color;
        if (isSuccess)
            color = getColor(R.color.success);
        else
            color = getColor(R.color.error);

        try {
            if (message == null || message.isEmpty())
                return;

            final Snackbar mSnackbar = Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG);
            mSnackbar.getView().setBackgroundColor(color);
            mSnackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}