package com.example.mindcheckdatacollectionapp.ui.theme;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.mindcheckdatacollectionapp.R;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.HomeFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.NotificationsFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.TherapistAppointmentFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.TherapistHomeFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.TherapistPatientListFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.TherapistProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class TherapistMainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private double longitude, latitude;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapist_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null){
                    String therapistID = currentUser.getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Query query = db.collection("therapist").whereEqualTo("therapistID", therapistID);
                    query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                            document.getReference()
                                    .update("latitude", latitude, "longitude", longitude)
                                    .addOnSuccessListener(aVoid -> Log.d("DEBUG", "Location updated successfully"))
                                    .addOnFailureListener(e -> Log.e("DEBUG", "Error querying documents", e));
                        }
                    }).addOnFailureListener(e -> Log.e("DEBUG", "Error querying documents", e));
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates(); //Listen to location updates
        }

        // Start listening for location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new TherapistHomeFragment()).commit();

        bottomNavigationView = findViewById(R.id.bottom_navigation);;
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectorFragment = new TherapistHomeFragment();
                } else if(itemId == R.id.nav_profile) {
                    selectorFragment = new TherapistProfileFragment();
                } else if (itemId == R.id.nav_appointment) {
                    selectorFragment = new TherapistAppointmentFragment();
                } else if (itemId == R.id.nav_noti) {
                    selectorFragment = new NotificationsFragment();
                    //selectorFragment = new TherapistNotificationFragment();
                } else if(itemId == R.id.nav_graph) {
                    selectorFragment = new TherapistPatientListFragment();
                }

                if (selectorFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectorFragment).commit();
                }

                return true;
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TherapistHomeFragment()).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start listening for location updates
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            } else {
                // Permission denied
                Log.e("Permission", "Location permission denied");
            }
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }
}