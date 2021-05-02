package com.ws.womansafety.Guardian;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ws.womansafety.R;
import com.ws.womansafety.databinding.ActivityMapsBinding;
import com.ws.womansafety.model.UserLatLng;
import com.ws.womansafety.utils.KeyBoardUtils;
import com.ws.womansafety.utils.NetworkUtils;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    ActivityMapsBinding mBinding;
    private GoogleMap mMap;
    DatabaseReference mFireBaseData;
    private View parentLayout;
    String mobNO;

    ArrayList<UserLatLng> locationList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_maps);
        setSupportActionBar(mBinding.toolbarMapActivity);
        getSupportActionBar().setTitle("Child Tracking ");

        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        parentLayout = findViewById(android.R.id.content);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mBinding.btnGetData.setOnClickListener(v -> {
            KeyBoardUtils.hideKeyBoard(this);
            mobNO = mBinding.editTextTm.getText().toString();

            mMap.clear();

            if (NetworkUtils.isNetworkAvailable(this)) {
                if (mobNO.length() == 10) {
                    displaySnackBar(getString(R.string.str_info_start_fetching), true);
                    mBinding.progressbarMap.setVisibility(View.VISIBLE);
                    getRecords(mobNO);
                } else
                    displaySnackBar(getString(R.string.str_error_mobile_length), false);
            } else
                displaySnackBar(getString(R.string.str_internet_connection), false);

        });

        mBinding.btnClear.setOnClickListener(v -> {

            KeyBoardUtils.hideKeyBoard(this);
            mobNO = mBinding.editTextTm.getText().toString();

            if (NetworkUtils.isNetworkAvailable(this)) {
                if (mobNO.length() == 10) {
                    mBinding.progressbarMap.setVisibility(View.VISIBLE);
                    displaySnackBarWithButton();

                } else
                    displaySnackBar(getString(R.string.str_error_mobile_length), false);
            } else
                displaySnackBar(getString(R.string.str_internet_connection), false);


        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(17.971005, 75.577634);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setMaxZoomPreference(15);
        Log.e("info ", "onMapReady");
    }


    private void addMarkers() {

        mMap.clear();

        for (int i = 0; i < locationList.size(); i++) {

            LatLng ulatlng = new LatLng(Double.parseDouble(locationList.get(i).lat), Double.parseDouble(locationList.get(i).lng));
            // below line is use to add marker to each location of our array list.
            mMap.addMarker(new MarkerOptions().position(ulatlng).title(ulatlng.latitude + "  " + ulatlng.longitude));
            // below lin is use to zoom our camera on map.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));
            // below line is use to move our camera to the specific location.
            mMap.moveCamera(CameraUpdateFactory.newLatLng(ulatlng));
        }


        int middelPos = locationList.size() / 2;
        LatLng mountainView = new LatLng(Double.parseDouble(locationList.get(middelPos).lat), Double.parseDouble(locationList.get(middelPos).lng));


// Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());

// Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

// Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mountainView)      // Sets the center of the map to Mountain View
                .zoom(12)                   // Sets the zoom
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private void getRecords(String mobiile) {


        mFireBaseData = FirebaseDatabase.getInstance().getReference(getString(R.string.str_user)).child(mobiile).child(getString(R.string.str_tracking));


        mFireBaseData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mBinding.progressbarMap.setVisibility(View.INVISIBLE);
                if (snapshot.exists()) {

                    locationList = new ArrayList<>();
                    locationList.clear();
                    for (DataSnapshot pt : snapshot.getChildren()) {

                        UserLatLng userLatLng = pt.getValue(UserLatLng.class);
                        locationList.add(userLatLng);
                    }

                    displaySnackBar(getString(R.string.str_info_stop_fetching), true);
                    addMarkers();
                } else {
                    displaySnackBar(getString(R.string.str_error_no_record), false);
                    mMap.clear();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                displaySnackBar(getString(R.string.str_error_database), false);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMap.clear();
    }

    public void displaySnackBarWithButton() {
        try {

            final Snackbar mSnackbar = Snackbar.make(mBinding.parentLayout, getString(R.string.str_clear_track), Snackbar.LENGTH_LONG)
                    .setAction("Yes", v -> {
                        mFireBaseData = FirebaseDatabase.getInstance().getReference(getString(R.string.str_user)).child(mobNO).child(getString(R.string.str_tracking));

                        mFireBaseData.removeValue().addOnSuccessListener(aVoid -> {
                            displaySnackBar(getString(R.string.str_delete_track), true);
                            mBinding.progressbarMap.setVisibility(View.INVISIBLE);
                        });

                    });
            mSnackbar.getView().setBackgroundColor(ContextCompat.getColor(MapsActivity.this, R.color.info_color));


            mSnackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}