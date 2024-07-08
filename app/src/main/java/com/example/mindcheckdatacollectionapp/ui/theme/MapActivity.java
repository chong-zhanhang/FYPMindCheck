package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private RecyclerView therapistsCards;
    private TherapistAdapter therapistAdapter;
    private interface FirebaseCallback {
        void onCallback(LatLng currentUserLocation);
    }
    private interface TherapistCallback {
        void onCallback(List<LatLng> therapistLocations);
    }

    private class TherapistAdapter extends RecyclerView.Adapter<TherapistAdapter.TherapistViewHolder> {
        private List<Therapist> therapistList;
        private LayoutInflater inflater;

        public TherapistAdapter(Context context, List<Therapist> therapistList){
            this.therapistList = therapistList;
            this.inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public TherapistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            View itemView = inflater.inflate(R.layout.item_therapist, parent, false);
            return new TherapistViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull TherapistViewHolder holder, int position) {
            Therapist current = therapistList.get(position);
            holder.bindTo(current);
        }

        @Override
        public int getItemCount() {
            return therapistList.size();
        }

        public void setTherapists(List<Therapist> therapists) {
            this.therapistList = therapists;
            notifyDataSetChanged();
        }

        public Therapist getTherapistAt(int position) {
            return therapistList.get(position);
        }

        class TherapistViewHolder extends RecyclerView.ViewHolder {
            private Therapist therapist;
            private final TextView therapistNameTextView;
            private final TextView therapistClinicTextView;
            private final TextView therapistAddressTextView;
            private final TextView therapistDistanceTextView;
            public ImageButton therapistProfile;
            public Button requestAssignment;

            public TherapistViewHolder(@NonNull View itemView) {
                super(itemView);
                therapistNameTextView = itemView.findViewById(R.id.therapistName);
                therapistClinicTextView = itemView.findViewById(R.id.therapistClinic);
                therapistAddressTextView = itemView.findViewById(R.id.therapistAddress);
                therapistDistanceTextView = itemView.findViewById(R.id.therapistDistance);
                therapistProfile = itemView.findViewById(R.id.therapistProfile);
                requestAssignment = itemView.findViewById(R.id.requestAssignment);

                requestAssignment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            String userID = currentUser.getUid();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("mobileUser")
                                    .whereEqualTo("userID", userID)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()){
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    DocumentReference userRef = document.getReference();
                                                    String therapistID = therapist.getTherapistID();
                                                    userRef.update("therapistID", therapistID)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("DEBUG", "Successfully assigned therapist");
                                                                    Toast.makeText(MapActivity.this, "Therapist Assigned", Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(MapActivity.this, RealMainActivity.class));
                                                                    finish();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d("DEBUG", "Error updating document", e);
                                                                }
                                                            });
                                                }
                                            } else {
                                                Log.d("DEBUG", "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
            public void bindTo(Therapist therapist) {
                this.therapist = therapist;
                therapistNameTextView.setText(therapist.getTherapistName());
                therapistClinicTextView.setText(therapist.getTherapistClinic());
                therapistAddressTextView.setText(therapist.getTherapistAddress());

                therapistProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MapActivity.this, RealMainActivity.class);
                        intent.putExtra("loadAnotherFragment", "MapActivity");
                        intent.putExtra("therapistID", therapist.therapistID);
                        startActivity(intent);
                    }
                });

                getUserLocation(new FirebaseCallback() {
                    @Override
                    public void onCallback(LatLng currentUserLocation) {
                        if (currentUserLocation != null) {
                            double dist = therapist.getDistance(currentUserLocation);
                            String formattedDist = String.format("%.2f km", dist);
                            therapistDistanceTextView.setText(formattedDist);
                        } else {
                            therapistDistanceTextView.setText("null");
                        }
                    }
                });
            }
        }
    }

    public static class Therapist {
        private String therapistID;
        private String therapistName;
        private String therapistClinic;
        private String therapistEmail;
        private String therapistDOB;
        private String therapistGender;
        private String therapistPhoneNum;
        private String therapistAddress;
        private String therapistLicenseNum;
        private String therapistQualification;
        private double longitude;
        private double latitude;
        private double distanceFromUser;

        public Therapist(){
        }
        public Therapist(String therapistID, String therapistName, String therapistClinic, String therapistEmail, String therapistDOB, String therapistGender, String therapistPhoneNum, String therapistAddress, String therapistLicenseNum, String therapistQualification, double latitude, double longitude) {
            this.therapistID = therapistID;
            this.therapistName = therapistName;
            this.therapistClinic = therapistClinic;
            this.therapistEmail = therapistEmail;
            this.therapistAddress = therapistAddress;
            this.therapistDOB = therapistDOB;
            this.therapistGender = therapistGender;
            this.therapistPhoneNum = therapistPhoneNum;
            this.therapistLicenseNum = therapistLicenseNum;
            this.therapistQualification = therapistQualification;
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public String getTherapistID() { return therapistID; }
        public String getTherapistName() {
            return therapistName;
        }
        public String getTherapistClinic(){
            return therapistClinic;
        }
        public String getTherapistEmail(){
            return therapistEmail;
        }
        public String getTherapistDob(){
            return therapistDOB;
        }
        public String getTherapistGender(){
            return therapistGender;
        }
        public String getTherapistPhoneNum() {
            return therapistPhoneNum;
        }
        public String getTherapistLicenseNum() {
            return therapistLicenseNum;
        }
        public String getTherapistQualification(){
            return therapistQualification;
        }
        public String getTherapistAddress() {
            return therapistAddress;
        }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }

        public double getDistance(LatLng currentUserLocation) {
            double userLat = Math.toRadians(currentUserLocation.latitude);
            double userLon = Math.toRadians(currentUserLocation.longitude);
            double therapistLat = Math.toRadians(latitude);
            double therapistLon = Math.toRadians(longitude);

            double dlon = therapistLon - userLon;
            double dlat = therapistLat - userLat;

            double a = Math.pow(Math.sin(dlat / 2), 2)
                    + Math.cos(userLat) * Math.cos(therapistLat)
                    * Math.pow(Math.sin(dlon / 2), 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            double distance = 6371 * c; // Earth radius = 6371
            return distance;
        }

        public void setDistanceFromUser(double distanceFromUser) {
            this.distanceFromUser = distanceFromUser;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DEBUG", "Entered activity");
        super.onCreate(savedInstanceState);
        Log.d("DEBUG", "SETTING CONTENT VIEW");
        setContentView(R.layout.activity_map);
        Log.d("DEBUG", "Making map fragment");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Log.d("DEBUG", "Map fragment made");
        if (mapFragment != null) {
            Log.d("DEBUG", "Getting map");
            mapFragment.getMapAsync(this);
        } else {
            Log.e("DEBUG", "Map Fragment is null");
        }
        Log.d("DEBUG", "Setting therapist layout");
        therapistsCards = findViewById(R.id.therapists_card_recycler_view);
        therapistsCards.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        therapistAdapter = new TherapistAdapter(this, new ArrayList<>());
        therapistsCards.setAdapter(therapistAdapter);

        therapistsCards.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int position = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Therapist visibleTherapist = therapistAdapter.getTherapistAt(position);
                    LatLng location = new LatLng(visibleTherapist.getLatitude(), visibleTherapist.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                }
            }
        });

        getUserLocation(new FirebaseCallback() {
            @Override
            public void onCallback(LatLng currentUserLocation) {
                if (currentUserLocation != null) {
                    loadTherapists(currentUserLocation);
                } else {
                    Log.e("DEBUG", "No location found");
                }
            }
        });
    }

    private void loadTherapists(LatLng currentUserLocation) {
        Log.d("DEBUG", "Entered loading function");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("therapist")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("DEBUG", "AREA 1");
                        List<Therapist> therapists = new ArrayList<>();
                        Log.d("DEBUG", "AREA 2");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("DEBUG", "AREA 3");
                            try {
                                Therapist therapist = document.toObject(Therapist.class);
                                double distance = therapist.getDistance(currentUserLocation);
                                therapist.setDistanceFromUser(distance);
                                Log.d("DEBUG", "AREA 4");
                                therapists.add(therapist);
                            } catch (Exception e) {
                                Log.e("DEBUG", "Error converting doc to obj", e);
                            }

                            Log.d("DEBUG", "AREA 5");
                        }
                        Collections.sort(therapists, (t1, t2) -> Double.compare(t1.getDistance(currentUserLocation), t2.getDistance(currentUserLocation)));
                        Log.d("DEBUG", "Therapist loaded");
                        therapistAdapter.setTherapists(therapists);
                        therapistAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("DEBUG", "Error getting documents.", task.getException());

                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getUserLocation(new FirebaseCallback() {
            @Override
            public void onCallback(LatLng currentUserLocation) {
                Log.d("DEBUG", "Map ready");
                if (currentUserLocation != null) {
                    MarkerOptions userMarkerOptions = new MarkerOptions()
                            .position(currentUserLocation)
                                    .title("Your Location")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    mMap.addMarker(userMarkerOptions);
                    Log.d("DEBUG", "Map ready 1");
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15f));
                    Log.d("DEBUG", "Map ready 2");
                }
            }
        });

        getTherapistLocations(new TherapistCallback() {
            @Override
            public void onCallback(List<LatLng> therapistLocations) {
                for (LatLng location : therapistLocations) {
                    mMap.addMarker(new MarkerOptions().position(location).title("Therapist"));
                }
            }
        });
    }

    public void getUserLocation(final FirebaseCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();

            Query query = db.collection("mobileUser").whereEqualTo("userID", userID);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                if (document.contains("longitude") && document.contains("latitude")){
                                    double longitude = document.getDouble("longitude");
                                    double latitude = document.getDouble("latitude");
                                    LatLng location = new LatLng(latitude, longitude);
                                    Log.d("DEBUG", "LOCATION1");
                                    callback.onCallback(location);
                                    break;
                                } else {
                                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                    if (locationManager != null) {
                                        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                        && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(MapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                        }

                                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (location != null) {
                                            double latitude = location.getLatitude();
                                            double longitude = location.getLongitude();
                                            db.collection("mobileUser")
                                                    .whereEqualTo("userID", userID)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                                                document1.getReference().update("latitude", latitude, "longitude", longitude)
                                                                        .addOnSuccessListener(aVoid -> Log.d("DEBUG", "User Location updated successfully"))
                                                                        .addOnFailureListener(e -> Log.e("DEBUG", "Error updating user location", e));
                                                            }
                                                        } else {
                                                            Log.e("DEBUG", "Query for updating fields failed", task1.getException());
                                                        }
                                                    });
                                            LatLng newLocation = new LatLng(latitude, longitude);
                                            callback.onCallback(newLocation);
                                        } else {
                                            Log.e("DEBUG", "Last known location is null");
                                        }
                                    } else {
                                        Log.e("DEBUG", "Location manager is null");
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e("DEBUG", "Query failed: ", task.getException());
                    }
                }
            });
        } else {
            Log.e("DEBUG", "User not logged in, cannot perform operations");
        }
    }

    public void getTherapistLocations(final TherapistCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("therapist");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<LatLng> therapistLocations = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.exists() && document.contains("longitude") && document.contains("latitude")){
                            double longitude = document.getDouble("longitude");
                            double latitude = document.getDouble("latitude");
                            LatLng location = new LatLng(latitude, longitude);
                            therapistLocations.add(location);
                        }
                        callback.onCallback(therapistLocations);
                    }
                } else {
                    Log.e("DEBUG", "Query failed: ", task.getException());
                }
            }
        });
    }
}