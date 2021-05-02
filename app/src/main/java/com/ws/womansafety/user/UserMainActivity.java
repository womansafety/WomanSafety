package com.ws.womansafety.user;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ws.womansafety.R;
import com.ws.womansafety.databinding.ActivityUserMainBinding;
import com.ws.womansafety.model.Guardian;
import com.ws.womansafety.user.SendNotificationPack.APIService;
import com.ws.womansafety.user.SendNotificationPack.Client;
import com.ws.womansafety.user.SendNotificationPack.Data;
import com.ws.womansafety.user.SendNotificationPack.MyResponse;
import com.ws.womansafety.user.SendNotificationPack.NotificationSender;
import com.ws.womansafety.utils.NetworkUtils;
import com.ws.womansafety.utils.SharedPrefConst;
import com.ws.womansafety.utils.SharedPrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class UserMainActivity extends AppCompatActivity {

    ActivityUserMainBinding userBinding;
    DatabaseReference mFireBaseData;
    View parentLayout;
    boolean is_1_GuardianAdded, is_2_GuardianAdded, isNetworkAvailable;
    String notification_send_user;
    int which_Guardian_Added;


    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_main);
        setSupportActionBar(userBinding.toolbarUser);
        getSupportActionBar().setTitle("Hi, " + SharedPrefUtils.getStringData(SharedPrefConst.USER_NAME));
        parentLayout = findViewById(android.R.id.content);

        is_1_GuardianAdded = SharedPrefUtils.getBooleanData(SharedPrefConst.GUA_1_ADD);
        is_2_GuardianAdded = SharedPrefUtils.getBooleanData(SharedPrefConst.GUA_2_ADD);

        isNetworkAvailable = NetworkUtils.isNetworkAvailable(this);

        if (is_1_GuardianAdded) {
            String title = "Connect to " + SharedPrefUtils.getStringData(SharedPrefConst.GUA_1_name);
            userBinding.btnGuardian1.setText(title);
        } else {
            userBinding.btnGuardian1.setText(getString(R.string.str_add_guardian1));
        }

        if (is_2_GuardianAdded) {
            String title = "Connect to " + SharedPrefUtils.getStringData(SharedPrefConst.GUA_2_name);
            userBinding.btnGuardian2.setText(title);
        } else {
            userBinding.btnGuardian2.setText(getString(R.string.str_add_guardian2));
        }


        userBinding.btnGuardian1.setOnClickListener(v -> {
            if (isNetworkAvailable) {
                is_1_GuardianAdded = SharedPrefUtils.getBooleanData(SharedPrefConst.GUA_1_ADD);
                which_Guardian_Added = 1;
                if (is_1_GuardianAdded) {
                    userBinding.pbUser.setVisibility(View.VISIBLE);
                    checkGuardianAvailable(SharedPrefUtils.getStringData(SharedPrefConst.GUA_1_mobNo));
                } else
                    showMobileDialogBox(1);
            } else
                displaySnackBar(getString(R.string.str_internet_connection), false);
        });

        userBinding.btnGuardian2.setOnClickListener(v -> {
            if (isNetworkAvailable) {
                is_2_GuardianAdded = SharedPrefUtils.getBooleanData(SharedPrefConst.GUA_2_ADD);
                which_Guardian_Added = 2;
                if (is_2_GuardianAdded) {
                    userBinding.pbUser.setVisibility(View.VISIBLE);
                    checkGuardianAvailable(SharedPrefUtils.getStringData(SharedPrefConst.GUA_2_mobNo));
                } else
                    showMobileDialogBox(2);
            } else
                displaySnackBar(getString(R.string.str_internet_connection), false);
        });

        userBinding.btnStartTracking.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkPermission()) {
                    onPermissionGranted();
                } else
                    requestPermissions();
            } else
                onPermissionGranted();
        });

        userBinding.btnStopTracking.setOnClickListener(v -> {
            stopLocationService();
        });

        if (isLocationServiceRunning()) {
            userBinding.btnStopTracking.setVisibility(View.VISIBLE);
            userBinding.btnStartTracking.setVisibility(View.GONE);
        } else {
            userBinding.btnStopTracking.setVisibility(View.GONE);
            userBinding.btnStartTracking.setVisibility(View.VISIBLE);
        }

    }

    private void showMobileDialogBox(int no) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserMainActivity.this);
        builder.setTitle("Add Guardian - " + no);
        builder.setCancelable(false);

        final AppCompatEditText input = new AppCompatEditText(UserMainActivity.this);
        input.setHint("Enter Guardian Mobile No");
        input.setMaxLines(1);
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(10);

        input.setFilters(FilterArray);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mob = input.getText().toString();
                if (!mob.isEmpty()) {
                    if (mob.length() == 10) {
                        userBinding.pbUser.setVisibility(View.VISIBLE);
                        checkGuardianAvailable(input.getText().toString());
                    } else
                        displaySnackBar("Enter 10 digit Mobile No ", false);
                } else
                    displaySnackBar("Enter Mobile No ", false);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void checkGuardianAvailable(String gMobNo) {


        mFireBaseData = FirebaseDatabase.getInstance().getReference(getString(R.string.str_guardian)).child(gMobNo);

        mFireBaseData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    {
                        displaySnackBar("Guardian is available", true);
                        Guardian guardian = snapshot.getValue(Guardian.class);
                        Log.e("firebase ->  ", guardian.toString() + " \n " + guardian.gName + " " + guardian.gToken);

                        sendNotifications(guardian.gToken, "Hi " + guardian.gName + ",", SharedPrefUtils.getStringData(SharedPrefConst.USER_NAME) + " give the access to track her location.");
                        notification_send_user = guardian.gName;
                        if (which_Guardian_Added == 1) {
                            if (!is_1_GuardianAdded) {
                                SharedPrefUtils.saveBooleanData(SharedPrefConst.GUA_1_ADD, true);
                                SharedPrefUtils.saveStringData(SharedPrefConst.GUA_1_name, guardian.gName);
                                SharedPrefUtils.saveStringData(SharedPrefConst.GUA_1_mobNo, guardian.gMobileNo);
                                String title = "Connect to " + guardian.gName;
                                userBinding.btnGuardian1.setText(title);

                            }
                        } else if (which_Guardian_Added == 2) {
                            if (!is_2_GuardianAdded) {
                                SharedPrefUtils.saveBooleanData(SharedPrefConst.GUA_2_ADD, true);
                                SharedPrefUtils.saveStringData(SharedPrefConst.GUA_2_name, guardian.gName);
                                SharedPrefUtils.saveStringData(SharedPrefConst.GUA_2_mobNo, guardian.gMobileNo);
                                String title = "Connect to " + guardian.gName;
                                userBinding.btnGuardian2.setText(title);
                            }
                        }
                    }
                } else {
                    userBinding.pbUser.setVisibility(View.INVISIBLE);
                    displaySnackBar("Guardian is not available", false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userBinding.pbUser.setVisibility(View.VISIBLE);
                displaySnackBar("FirebaseDatabase Error ", false);
            }
        });
    }

    public void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);

        APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                userBinding.pbUser.setVisibility(View.INVISIBLE);
                if (response.code() == 200) {
                    if (response.body().success == 1) {
                        displaySnackBar("Notification send to " + notification_send_user, true);
                        userBinding.btnStartTracking.setVisibility(View.VISIBLE);
                    } else {
                        displaySnackBar("Guardian Notification Failed ", false);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                userBinding.pbUser.setVisibility(View.INVISIBLE);
                displaySnackBar("Notification Failed ", false);
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


    private void onPermissionGranted() {
        locationSetting();
    }

    private void locationSetting() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse ->
                        startLocationService()
                //stopLocationService()

        );

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(UserMainActivity.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationService();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Location Disable ", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
                break;
        }
    }


    private boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);

        int result3 = 0;  // result3 is used for only 10 or higher devices
        //   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        //  result3= ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_BACKGROUND_LOCATION);

        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION,ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_CODE);
        else*/
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted();
            } else
                Toast.makeText(this, "Permission was not Granted", Toast.LENGTH_SHORT).show();
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {

            for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(serviceInfo.service.getClassName())) {
                    if (serviceInfo.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }

        return false;
    }

    private void startLocationService() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            if (!isLocationServiceRunning()) {
                Intent intent = new Intent(getApplicationContext(), LocationService.class);
                intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
                startService(intent);
                userBinding.btnStopTracking.setVisibility(View.VISIBLE);
                userBinding.btnStartTracking.setVisibility(View.GONE);

            }
        } else
            displaySnackBar(getString(R.string.str_internet_connection), false);
    }


    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            userBinding.btnStopTracking.setVisibility(View.GONE);
            userBinding.btnStartTracking.setVisibility(View.VISIBLE);
        }
    }

}
