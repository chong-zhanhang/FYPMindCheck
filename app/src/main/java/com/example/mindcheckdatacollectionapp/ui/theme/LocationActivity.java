package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LocationActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private ListView searchResultsListView;
    private ArrayAdapter<PlaceInfo> searchResultsAdapter;
    private List<PlaceInfo> searchResultsList = new ArrayList<>();
    public class PlaceInfo{
        String name;
        String placeId;
        public PlaceInfo(String name, String placeId) {
            this.name = name;
            this.placeId = placeId;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getPlaceId(){
            return placeId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        
        searchResultsListView = findViewById(R.id.search_results_list);
        searchResultsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchResultsList);
        searchResultsListView.setAdapter(searchResultsAdapter);

        EditText searchField = findViewById(R.id.location_search_bar);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Required override, left empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    searchForPlaces(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Required override, left empty
            }
        });
        
        Button currentLocationButton = findViewById(R.id.current_location_button);
        currentLocationButton.setOnClickListener(view -> useCurrentLocation());
        
        searchResultsListView.setOnItemClickListener((adapterView, view, position, id) -> {
            PlaceInfo selectedPlace = searchResultsAdapter.getItem(position);
            if (selectedPlace != null) {
                handleSelectedPlace(selectedPlace.getPlaceId());
            } else {
                Log.e("DEBUG", "No selected place");
            }
        });
    }

    private void searchForPlaces(String query) {
        // This is a placeholder function to handle Google Maps API search queries
        // The Places API, Autocomplete predictions or a custom Places search must be implemented here
        // The result should be a list of place names/addresses which you add to `searchResultsList`
        // Then notify the adapter about the data change
        Properties properties = new Properties();
        try (InputStream inputStream = getResources().getAssets().open("secrets.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String apiKey = properties.getProperty("GOOGLE_MAPS_API_KEY");

        Places.initialize(getApplicationContext(), apiKey);
        PlacesClient placesClient = Places.createClient(this);

        // Use the builder to create a FindAutocompletePredictionsRequest
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setQuery(query)
                        .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            searchResultsList.clear();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                searchResultsList.add(new PlaceInfo(prediction.getFullText(null).toString(), prediction.getPlaceId()));
            }
            searchResultsAdapter.notifyDataSetChanged();
        }).addOnFailureListener(exception -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("DEBUG", "Place not found: " + apiException.getStatusCode());
            }
        });
    }

    private void useCurrentLocation() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                // Handle the use of the current location
                // For example, you could convert this location to a human-readable address
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                storeLocationInDatabase(latitude, longitude);
                startActivity(new Intent(LocationActivity.this, MapActivity.class));
                finish();
            } else {
                // Location is null, handle case here
                Log.e("DEBUG", "No Location Found");
            }
        });
    }

    private void handleSelectedPlace(String placeID) {
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeID, Arrays.asList(Place.Field.LAT_LNG)).build();
        PlacesClient placesClient = Places.createClient(this);
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            LatLng latLng = place.getLatLng();
            if (latLng != null) {
                storeLocationInDatabase(latLng.latitude, latLng.longitude);
                startActivity(new Intent(LocationActivity.this, MapActivity.class));
                finish();
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("DEBUG", "Place not found: " + apiException.getStatusCode());
            }
        });
    }

    private void storeLocationInDatabase(double latitude, double longitude) {
        //Store to database
        //on click of place button, send lat long to google maps api to render google maps
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("mobileUser")
                    .whereEqualTo("userID", userID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference userDocRef = document.getReference();
                                userDocRef.update(locationData)
                                        .addOnSuccessListener(aVoid -> Log.d("DEBUG", "Location successfully saved"))
                                        .addOnFailureListener(e -> Log.e("DEBUG", "Error writing document", e));
                            }
                        } else {
                            Log.e("DEBUG", "Error getting documents", task.getException());
                        }
                    });
        } else {
            Log.e("DEBUG", "No user signed in");
        }
    }
}