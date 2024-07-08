package com.example.mindcheckdatacollectionapp.ui.theme.Fragments;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mindcheckdatacollectionapp.R;
import com.example.mindcheckdatacollectionapp.ui.theme.AppointmentsNewActivity;
import com.example.mindcheckdatacollectionapp.ui.theme.DepressionDetectionApiService;
import com.example.mindcheckdatacollectionapp.ui.theme.HelplineAdapter;
import com.example.mindcheckdatacollectionapp.ui.theme.HelplineItem;
import com.example.mindcheckdatacollectionapp.ui.theme.LocationActivity;
import com.example.mindcheckdatacollectionapp.ui.theme.MapActivity;
import com.example.mindcheckdatacollectionapp.ui.theme.QuestionnaireActivity;
import com.example.mindcheckdatacollectionapp.ui.theme.VideoItem;
import com.example.mindcheckdatacollectionapp.ui.theme.VideosAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private EditText locationSearchField;
    private TextView welcomeTextView;
    private TextView healthyStatusText, healthyStatusDays;
    private TextView depressedStatusText, depressedStatusDays;
    private RelativeLayout healthyStatusView;
    private RelativeLayout depressedStatusView;
    private LinearLayout healthyAssignedTherapist, healthyAppointments;
    private LinearLayout depressedAssignedTherapist, depressedAppointments;
    private TextView findTherapist, makeAppointment;
    private ImageButton therapistProfile, callTherapist;
    private Button unshareData, predictNow;
    private TextView therapistName, therapistClinic, therapistAddress;
    private ImageButton appointmentTherapistProfile, appointmentCallTherapist;
    private LinearLayout appointmentDetails;
    private TextView appointmentDate, appointmentTime;
    private TextView appointmentTherapistName, appointmentTherapistClinic, appointmentTherapistAddress;
    private RecyclerView videosRecyclerView;
    private VideosAdapter adapter;
    private ArrayList<VideoItem> videoList;
    private RecyclerView helplinesRecyclerView;
    private HelplineAdapter helplineAdapter;
    private ArrayList<HelplineItem> helplinesList;

    private interface LocationCallback {
        void onCallback(LatLng currentUserLocation);
    }

    public static class mobileUser {
        private boolean isDepressed;
        private String userDOB;
        private String userEmail;
        private String userGender;
        private String userID;
        private String userName;
        private String userPhoneNum;
        private String fcmToken;
        private double latitude;
        private double longitude;
        private String therapistID;
        private boolean unshared;
        private double base_f1;
        private double fine_tuned_f1;

        public mobileUser(boolean isDepressed, String userDOB, String userEmail, String userGender, String userID, String userName, String userPhoneNum, String fcmToken, String therapistID, double latitude, double longitude, boolean unshared, double base_f1, double fine_tuned_f1){
            this.isDepressed = isDepressed;
            this.userDOB = userDOB;
            this.userEmail = userEmail;
            this.userGender =userGender;
            this.userID = userID;
            this.userName = userName;
            this.userPhoneNum = userPhoneNum;
            this.fcmToken = fcmToken;
            this.latitude = latitude;
            this.longitude = longitude;
            this.therapistID = therapistID;
            this.base_f1 = base_f1;
            this.fine_tuned_f1 = fine_tuned_f1;
            this.unshared = unshared;
        }
        public mobileUser() {

        }
        public boolean getIsDepressed() { return isDepressed; }
        public String getUserDOB() { return userDOB; }
        public String getUserEmail() { return userEmail; }

        public String getUserGender() {
            return userGender;
        }

        public double getBase_f1() {
            return base_f1;
        }

        public double getFine_tuned_f1() {
            return fine_tuned_f1;
        }
        public boolean getUnshared() {
            return unshared;
        }

        public String getUserID() {
            return userID;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserPhoneNum() {
            return userPhoneNum;
        }

        public boolean isDepressed() {
            return isDepressed;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getFcmToken() {
            return fcmToken;
        }

        public String getTherapistID() {
            return therapistID;
        }

    }

    public void updateVideoList(List<VideoItem> newVideoList) {
        videoList.clear();
        videoList.addAll(newVideoList);
        adapter.notifyDataSetChanged();
    }
    public interface userCallback {
        void onCallback(mobileUser user);
    }
    private interface therapistCallback {
        void onCallback(MapActivity.Therapist therapist);
    }
    public interface upcomingAppointmentsCallback {
        void onCallback(@Nullable AppointmentsUpcomingFragment.Appointment appointment);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Log.d("DEBUG", "HOME1");

        locationSearchField = view.findViewById(R.id.location);
        setNearestPlaceHint();

        predictNow = view.findViewById(R.id.predictNow);
        predictNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerPrediction();
            }
        });
        Log.d("DEBUG", "HOME2");
        locationSearchField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), LocationActivity.class));
            }
        });

        welcomeTextView = view.findViewById(R.id.welcome);

        healthyStatusView = view.findViewById(R.id.healthyStatusView);
        healthyStatusText = view.findViewById(R.id.healthyStatusText);
        healthyStatusDays = view.findViewById(R.id.healthyStatusDays);
        healthyAssignedTherapist = view.findViewById(R.id.healthyAssignedTherapist);
        healthyAppointments = view.findViewById(R.id.healthyAppointments);
        findTherapist = view.findViewById(R.id.findTherapist);
        makeAppointment = view.findViewById(R.id.makeAppointment);

        depressedStatusView = view.findViewById(R.id.depressedStatusView);
        depressedStatusText = view.findViewById(R.id.depressedStatusText);
        depressedStatusDays = view.findViewById(R.id.depressedStatusDays);
        depressedAssignedTherapist = view.findViewById(R.id.depressedAssignedTherapist);
        depressedAppointments = view.findViewById(R.id.depressedAppointment);
        therapistProfile = view.findViewById(R.id.therapistProfile);
        callTherapist = view.findViewById(R.id.callTherapist);
        unshareData = view.findViewById(R.id.unshareData);
        therapistName = view.findViewById(R.id.therapistName);
        therapistClinic = view.findViewById(R.id.therapistClinic);
        therapistAddress = view.findViewById(R.id.therapistAddress);
        appointmentTherapistProfile = view.findViewById(R.id.appointmentTherapistProfile);
        appointmentCallTherapist = view.findViewById(R.id.appointmentCallTherapist);
        appointmentDetails = view.findViewById(R.id.appointmentDetails);
        appointmentDate = view.findViewById(R.id.appointmentDate);
        appointmentTime = view.findViewById(R.id.appointmentTime);
        appointmentTherapistName = view.findViewById(R.id.appointmentTherapistName);
        appointmentTherapistClinic = view.findViewById(R.id.appointmentTherapistClinic);
        appointmentTherapistAddress = view.findViewById(R.id.appointmentTherapistAddress);

        videosRecyclerView = view.findViewById(R.id.videoScrollSpace);
        videoList = new ArrayList<>();

        helplinesRecyclerView = view.findViewById(R.id.helplinesRecyclerView);
        helplinesList = new ArrayList<>();

        Drawable bg = unshareData.getBackground();
        if (bg instanceof GradientDrawable) {
            ((GradientDrawable) bg).setColor(Color.parseColor("#FFFFFF"));
        }
        Log.d("DEBUG", "HOME3");
        unshareData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userID = currentUser.getUid();
                    Query query = db.collection("mobileUser").whereEqualTo("userID", userID);
                    query.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            Boolean currentUnsharedState  = documentSnapshot.getBoolean("unshared");
                            if (currentUnsharedState  == null) {
                                currentUnsharedState  = false;
                            }
                            final Boolean finalUnsharedState = currentUnsharedState;
                            DocumentReference userRef = documentSnapshot.getReference();
                            userRef.update("unshared", !finalUnsharedState )
                                    .addOnSuccessListener(aVoid -> {
                                        if (!finalUnsharedState ) {
                                            unshareData.setText("Share Data");
                                            Toast.makeText(getContext(), "Data sharing disabled.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            unshareData.setText("Unshare Data");
                                            Toast.makeText(getContext(), "Data sharing enabled.", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(e -> Log.e("DEBUG", "Error updating document", e));
                        }
                    });
                } else {
                    Log.e("DEBUG", "User not logged in");
                }
            }
        });

        //updateButtonState();
        Log.d("DEBUG", "HOME4");
        loadMobileUser(new userCallback() {
            @Override
            public void onCallback(mobileUser user) {
                String userID = user.getUserID();
                String username = user.getUserName();
                boolean userIsDepressed = user.getIsDepressed();
                String userTherapistID = user.getTherapistID();
                double userLongitude = user.getLongitude();
                double userLatitude = user.getLatitude();

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                welcomeTextView.setText("Welcome, " + username + "!");

                if (!Double.isNaN(userLatitude) && !Double.isNaN(userLongitude) && userLatitude != 0.0 && userLongitude != 0.0) {
                    findTherapist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), MapActivity.class));
                        }
                    });
                } else {
                    findTherapist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), LocationActivity.class));
                        }
                    });
                }

                if (userIsDepressed) {
                    depressedStatusView.setVisibility(View.VISIBLE);
                    healthyStatusView.setVisibility(View.GONE);

                    Drawable background = depressedStatusView.getBackground();
                    if (background instanceof GradientDrawable) {
                        ((GradientDrawable) background).setStroke(10, Color.parseColor("#C42C2C"));
                    }

                    db.collection("mobileUser").whereEqualTo("userID", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                Map<String, Boolean> depressionHistory = (Map<String, Boolean>) task.getResult().getDocuments().get(0).get("depressionHistory");
                                int continuousDays = calculateContinuousDays(depressionHistory, userIsDepressed);
                                depressedStatusText.setText("You have been depressed for ");
                                depressedStatusDays.setText(String.valueOf(continuousDays) + " days");
                            } else {
                                Log.e("DEBUG", "Error reading depression history: ", task.getException());
                            }
                        }
                    });

                } else {
                    depressedStatusView.setVisibility(View.GONE);
                    healthyStatusView.setVisibility(View.VISIBLE);

                    Drawable background = healthyStatusView.getBackground();
                    if (background instanceof GradientDrawable) {
                        ((GradientDrawable) background).setStroke(10, Color.parseColor("#14DC5E"));
                    }

                    db.collection("mobileUser").whereEqualTo("userID", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                Map<String, Boolean> depressionHistory = (Map<String, Boolean>) task.getResult().getDocuments().get(0).get("depressionHistory");
                                int continuousDays = calculateContinuousDays(depressionHistory, userIsDepressed);
                                healthyStatusText.setText(" You have been healthy for ");
                                healthyStatusDays.setText(String.valueOf(continuousDays) + " day(s)");
                            } else {
                                Log.e("DEBUG", "Error reading depression history: ", task.getException());
                            }
                        }
                    });


                }

                if (userTherapistID != null) {
                    healthyAssignedTherapist.setVisibility(View.GONE);
                    depressedAssignedTherapist.setVisibility(View.VISIBLE);
                    loadTherapistDetails(userTherapistID, new therapistCallback() {
                        @Override
                        public void onCallback(MapActivity.Therapist therapist) {
                            String therapistNameValue = therapist.getTherapistName();
                            String therapistClinicValue = therapist.getTherapistClinic();
                            String therapistAddressValue = therapist.getTherapistAddress();

                            therapistName.setText(therapistNameValue);
                            therapistClinic.setText(therapistClinicValue);
                            therapistAddress.setText(therapistAddressValue);

                            therapistProfile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    navigateToTherapistProfile(userTherapistID);
                                }
                            });

                            callTherapist.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String phoneNum = therapist.getTherapistPhoneNum().toString();
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("tel:" + phoneNum));
                                    v.getContext().startActivity(intent);
                                }
                            });
                        }
                    });

                } else {
                    healthyAssignedTherapist.setVisibility(View.VISIBLE);
                    depressedAssignedTherapist.setVisibility(View.GONE);
                }

                loadUpcomingAppointments(userID, new upcomingAppointmentsCallback() {
                    @Override
                    public void onCallback(@Nullable AppointmentsUpcomingFragment.Appointment appointment) {
                        if (appointment != null) {
                            healthyAppointments.setVisibility(View.GONE);
                            depressedAppointments.setVisibility(View.VISIBLE);

                            appointmentTherapistProfile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    navigateToTherapistProfile(appointment.therapistID);
                                }
                            });

                            appointmentCallTherapist.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    appointment.getTherapistPhoneNum(new AppointmentsUpcomingFragment.Appointment.TherapistDetailsCallback() {
                                        @Override
                                        public void onCallback(String therapistDetails) {
                                            String phoneNum = therapistDetails.toString();
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("tel:" + phoneNum));
                                            v.getContext().startActivity(intent);
                                        }
                                    });
                                }
                            });

                            appointment.getTherapistName(new AppointmentsUpcomingFragment.Appointment.TherapistDetailsCallback() {
                                @Override
                                public void onCallback(String therapistDetails) {
                                    appointmentTherapistName.setText(therapistDetails);
                                }
                            });
                            appointment.getTherapistClinic(new AppointmentsUpcomingFragment.Appointment.TherapistDetailsCallback() {
                                @Override
                                public void onCallback(String therapistDetails) {
                                    appointmentTherapistClinic.setText(therapistDetails);
                                }
                            });
                            appointment.getTherapistAddress(new AppointmentsUpcomingFragment.Appointment.TherapistDetailsCallback() {
                                @Override
                                public void onCallback(String therapistDetails) {
                                    appointmentTherapistAddress.setText(therapistDetails);
                                }
                            });
                            appointmentDate.setText(appointment.getDate());
                            appointmentTime.setText(appointment.getTime());

                        } else {
                            healthyAppointments.setVisibility(View.VISIBLE);
                            depressedAppointments.setVisibility(View.GONE);

                            makeAppointment.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(requireContext(), AppointmentsNewActivity.class));
                                }
                            });
                        }
                    }
                });
            }
        });

        Log.d("DEBUG", "HOME5");

        helplineAdapter = new HelplineAdapter(helplinesList);
        helplinesRecyclerView.setAdapter(helplineAdapter);
        helplinesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        HelplineItem helpline1 = new HelplineItem("MIASA Crisis Helpline", "1-800-18-0066", "http://miasa.org.my");
        HelplineItem helpline2 = new HelplineItem("Sage Centre", "6012-339-7121", "http://sagecentre.co");
        HelplineItem helpline3 = new HelplineItem("Buddy Bear Childline", "1800182327", "http://humankind.my");
        helplinesList.add(helpline1);
        helplinesList.add(helpline2);
        helplinesList.add(helpline3);

        adapter = new VideosAdapter(videoList, getContext());
        videosRecyclerView = view.findViewById(R.id.videoScrollSpace);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        videosRecyclerView.setLayoutManager(layoutManager);
        videosRecyclerView.setAdapter(adapter);

        loadVideos();
        Log.d("DEBUG", "HOME6");

        return view;
    }

    private int calculateContinuousDays(Map<String, Boolean> depressionHistory, boolean isDepressed) {
        if (depressionHistory == null || depressionHistory.isEmpty()) {
            return 0;
        }

        List<Map.Entry<String, Boolean>> sortedHistory = new ArrayList<>(depressionHistory.entrySet());
        Collections.sort(sortedHistory, new Comparator<Map.Entry<String, Boolean>>() {
            @Override
            public int compare(Map.Entry<String, Boolean> o1, Map.Entry<String, Boolean> o2) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/M");
                try {
                    Date date1 = formatter.parse(o1.getKey());
                    Date date2 = formatter.parse(o2.getKey());
                    return date2.compareTo(date1);  // Descending order (newest to oldest)
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0; // Handle parsing exception gracefully
                }
            }
        });

        Log.d("DEBUG", String.valueOf(sortedHistory));

        SimpleDateFormat formatter = new SimpleDateFormat("dd/M", Locale.getDefault());
        String today = formatter.format(new Date());

        boolean isCurrentlyDepressed = isDepressed;
        int continuousDays = 0;

        if (!sortedHistory.isEmpty() && sortedHistory.get(0).getKey().equals(today) && sortedHistory.get(0).getValue() == isDepressed) {
            continuousDays++;
        }

        boolean foundGap = false;
        for (int i = 0; i < sortedHistory.size(); i++) {
            Map.Entry<String, Boolean> currentEntry = sortedHistory.get(i);
            String currentDate = currentEntry.getKey();

            // Check if date is before today (use helper function)
            if (isPastDate(currentDate, today)) {
                if (foundGap) {
                    break;
                }
                if (!foundGap && currentEntry.getValue() == isCurrentlyDepressed) {
                    continuousDays++;
                } else {
                    foundGap = true;  // Stop counting continuous days
                }
            }
        }
        return continuousDays;
    }

    private boolean isPreviousDay(String date1, String date2) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/M", Locale.getDefault());
        try {
            Date prevDate = formatter.parse(date1);
            Date currentDate = formatter.parse(date2);
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.DAY_OF_YEAR, -1); // Move to the previous day
            Date expectedPrevDate = cal.getTime();
            return prevDate.equals(expectedPrevDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isPastDate(String date1, String date2) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/M", Locale.getDefault());
            Date dateOne = format.parse(date1);
            Date dateTwo = format.parse(date2);
            return dateOne.before(dateTwo);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Handle parsing exception gracefully
        }
    }

    private void updateButtonState() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid(); // get current user ID
            Query query = db.collection("mobileUser").whereEqualTo("userID", currentUserId);
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                    Boolean isUnshared = documentSnapshot.getBoolean("unshared");
                    unshareData.setText(isUnshared != null && isUnshared ? "Share Data" : "Unshare Data");
                } else {
                    Log.e("DEBUG", "Error getting document or document does not exist");
                }
            });
        } else {
            Log.e("DEBUG", "User not logged in.");
        }

    }

    private void triggerPrediction() {
        DepressionDetectionApiService apiService = new DepressionDetectionApiService();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null){
            String userID = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            Date yesterday = calendar.getTime();

            CollectionReference typingSessionRef = db.collection("typingSession");
            Query query = typingSessionRef
                    .whereEqualTo("userId", userID)
                    .whereEqualTo("trained", "False")
                    .whereGreaterThan("timestamp", yesterday);

            Log.d("DEBUG", "Preparing to fetch typing sessions");

            query.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG", "Successfully fetched typing sessions");
                            List<DepressionDetectionApiService.typingSession> typingSessions = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                typingSessions.add(document.toObject(DepressionDetectionApiService.typingSession.class));
                            }
                            Log.d("DEBUG", "HELLO1");
                            if (!typingSessions.isEmpty()) {
                                Log.d("DEBUG", "Typing sessions not empty");
                                db.collection("mobileUser").whereEqualTo("userID", userID).get()
                                        .addOnCompleteListener(userTask -> {
                                            if (userTask.isSuccessful() && !userTask.getResult().isEmpty() && userTask.getResult().getDocuments().get(0).contains("fine_tuned_f1") && userTask.getResult().getDocuments().get(0).contains("base_f1")) {
                                                Log.d("DEBUG", "HELLO2");
                                                DocumentSnapshot userDoc = userTask.getResult().getDocuments().get(0);
                                                Log.d("DEBUG", "HELLO2.5");
                                                double baseF1 = userDoc.getDouble("base_f1");
                                                double fineTunedF1 = userDoc.getDouble("fine_tuned_f1");
                                                Log.d("DEBUG", "HELLO2.7");

                                                String bestModel = (baseF1 > fineTunedF1) ? "base" : "fine_tuned";
                                                Log.d("DEBUG", "HELLO3");
                                                apiService.predictDepression(userID, typingSessions, bestModel, new Callback<DepressionDetectionApiService.PredictionResponse>() {
                                                    @Override
                                                    public void onResponse(Call<DepressionDetectionApiService.PredictionResponse> call, Response<DepressionDetectionApiService.PredictionResponse> response) {
                                                        if (response.isSuccessful()) {
                                                            Log.d("DEBUG", "Prediction API call successful");
                                                            boolean isDepressed = response.body().getPrediction();
                                                            Map<String, Object> userUpdates = new HashMap<>();
                                                            userUpdates.put("isDepressed", isDepressed);

                                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M", Locale.getDefault());
                                                            String currentDate = dateFormat.format(Calendar.getInstance().getTime());

                                                            db.collection("mobileUser").whereEqualTo("userID", userID).get()
                                                                    .addOnSuccessListener(querySnapshot -> {
                                                                        if (!querySnapshot.isEmpty()) {
                                                                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                                                            if (documentSnapshot.exists()) {
                                                                                DocumentReference docRef = documentSnapshot.getReference();
                                                                                docRef.get().addOnSuccessListener(documentSnapshot1 -> {
                                                                                    if (documentSnapshot1.exists() && documentSnapshot1.contains("depressionHistory")) {
                                                                                        Map<String, Object> updateAll = new HashMap<>();
                                                                                        updateAll.put("isDepressed", isDepressed);

                                                                                        Map<String, Boolean> depressionHistory = (Map<String, Boolean>) documentSnapshot1.get("depressionHistory");
                                                                                        if (depressionHistory == null) {
                                                                                            depressionHistory = new HashMap<>();
                                                                                        }
                                                                                        depressionHistory.put(currentDate, isDepressed);
                                                                                        updateAll.put("depressionHistory", depressionHistory);

                                                                                        docRef.update(updateAll)
                                                                                                .addOnSuccessListener(aVoidd -> Log.d("DEBUG", "UpdateAll DocumentSnapshot successfully updated!"))
                                                                                                .addOnFailureListener(ee -> Log.e("DEBUG", "Error updating document", ee));
                                                                                    } else {
                                                                                        Log.e("DEBUG", "No matching document found");
                                                                                    }
                                                                                }).addOnFailureListener(eee ->  Log.e("DEBUG", "Error retrieving document", eee));
                                                                            } else {
                                                                                Log.d("DEBUG", "Document does not exist");
                                                                            }
                                                                        } else {
                                                                            Log.d("DEBUG", "No matching user found");
                                                                        }
                                                                    }).addOnFailureListener(e -> Log.e("DEBUG", "Error getting documents: ", e));
                                                            createDepressionNotification(isDepressed, getContext());
                                                            showMessage(isDepressed);
                                                        } else {
                                                            Log.e("DEBUG", "Prediction response unsuccessful: " + response.errorBody());
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<DepressionDetectionApiService.PredictionResponse> call, Throwable t) {
                                                        Log.e("DEBUG", "Prediction request failed.", t);
                                                    }
                                                });

                                            } else {
                                                apiService.predictDepression(userID, typingSessions, "base", new Callback<DepressionDetectionApiService.PredictionResponse>() {
                                                    @Override
                                                    public void onResponse(Call<DepressionDetectionApiService.PredictionResponse> call, Response<DepressionDetectionApiService.PredictionResponse> response) {
                                                        if (response.isSuccessful()) {
                                                            boolean isDepressed = response.body().getPrediction();
                                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M", Locale.getDefault());
                                                            String currentDate = dateFormat.format(Calendar.getInstance().getTime());
                                                            db.collection("mobileUser").whereEqualTo("userID", userID).get()
                                                                    .addOnSuccessListener(querySnapshot -> {
                                                                        if (!querySnapshot.isEmpty()) {
                                                                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                                                            if (documentSnapshot.exists()) {
                                                                                DocumentReference docRef = documentSnapshot.getReference();
                                                                                docRef.get().addOnSuccessListener(documentSnapshot1 -> {
                                                                                    if (documentSnapshot1.exists() && documentSnapshot1.contains("depressionHistory")) {
                                                                                        Map<String, Object> updateAll = new HashMap<>();
                                                                                        updateAll.put("isDepressed", isDepressed);

                                                                                        Map<String, Boolean> depressionHistory = (Map<String, Boolean>) documentSnapshot1.get("depressionHistory");
                                                                                        if (depressionHistory == null) {
                                                                                            depressionHistory = new HashMap<>();
                                                                                        }
                                                                                        depressionHistory.put(currentDate, isDepressed);
                                                                                        updateAll.put("depressionHistory", depressionHistory);

                                                                                        docRef.update(updateAll)
                                                                                                .addOnSuccessListener(aVoidd -> Log.d("DEBUG", "UpdateAll DocumentSnapshot successfully updated!"))
                                                                                                .addOnFailureListener(ee -> Log.e("DEBUG", "Error updating document", ee));
                                                                                    } else {
                                                                                        Log.e("DEBUG", "No matching document found");
                                                                                    }
                                                                                }).addOnFailureListener(eee ->  Log.e("DEBUG", "Error retrieving document", eee));
                                                                            } else {
                                                                                Log.d("DEBUG", "Document does not exist");
                                                                            }
                                                                        } else {
                                                                            Log.d("DEBUG", "No matching user found");
                                                                        }
                                                                    }).addOnFailureListener(e -> Log.e("DEBUG", "Error getting documents: ", e));

                                                            createDepressionNotification(isDepressed, getContext());
                                                            showMessage(isDepressed);
                                                        } else {
                                                            Log.e("DEBUG", "Prediction response unsuccessful.");
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<DepressionDetectionApiService.PredictionResponse> call, Throwable t) {
                                                        Log.e("DEBUG", "Prediction request failed.", t);
                                                    }
                                                });
                                            }
                                        });
                            } else {
                                Log.e("DEBUG", "No typing sessions found for prediction.");
                                Toast.makeText(getContext(), "No typing sessions found for prediction", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("DEBUG", "Error getting typing sessions: ", task.getException());
                        }
                    });
        } else {
            Log.e("DEBUG", "User not logged in");
        }
    }

    private void showMessage(boolean isDepressed) {
        String message = "";
        if (isDepressed == true) {
            message = "The system predicts you are in high risk of depression today. Please complete the questionnaire to verify your depression occurrence.";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getContext(), QuestionnaireActivity.class));
        } else {
            message = "Congrats, the system predicts you are in low risk of depression today!";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void createDepressionNotification(boolean isDepressed, Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("DEBUG", "User not logged in");
            return;
        }
        String userID = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("mobileUser").whereEqualTo("userID", userID).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        String therapistID = null;
                        if (documentSnapshot.contains("therapistID")) {
                            therapistID = documentSnapshot.getString("therapistID");
                        }
                        String userName = documentSnapshot.getString("userName");

                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        String NOTIFICATION_CHANNEL_ID = "depression_detection_channel";
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel(
                                    NOTIFICATION_CHANNEL_ID,
                                    "Depression Detection Notifications",
                                    NotificationManager.IMPORTANCE_DEFAULT
                            );
                            channel.setDescription("Depression Detection Alerts");
                            notificationManager.createNotificationChannel(channel);
                        }

                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                                .setSmallIcon(R.drawable.mindchecklogo)
                                .setContentTitle("Depression Detection Result")
                                .setContentText(isDepressed ? "Possible depression detected. Consider making an appointment with a therapist." : "Congrats, no signs of depression detected today!")
                                .setAutoCancel(true)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(isDepressed ? "Possible depression detected. Consider making an appointment with a therapist." : "Congrats, no signs of depression detected today!"))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        notificationManager.notify(1, notificationBuilder.build());

                        Map<String, Object> notificationData = new HashMap<>();
                        Map<String, Object> notificationTherapist = new HashMap<>();
                        String message = "";
                        String therapistMessage = "";
                        if (isDepressed) {
                            message = "Possible depression detected. Consider making an appointment with a therapist.";
                            therapistMessage = userName + " is detected to have depression. Consider contacting the patient.";
                        } else {
                            message = "Congrats, no signs of depression detected today!";
                        }
                        notificationData.put("notificationsHeader", "Depression Detection Result");
                        notificationData.put("notificationsMessage", message);
                        notificationData.put("notificationsReadStatus", "unread");
                        notificationData.put("notificationsTimestamp", new Timestamp(new Date()));
                        notificationData.put("notificationsType", "Depression Detection");
                        notificationData.put("receiverID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        notificationData.put("senderID", "null");
                        if (isDepressed && therapistID != null) {
                            notificationTherapist.put("notificationsHeader", "Patient Depression Detected");
                            notificationTherapist.put("notificationsMessage", therapistMessage);
                            notificationTherapist.put("notificationsReadStatus", "unread");
                            notificationTherapist.put("notificationsTimestamp", new Timestamp(new Date()));
                            notificationTherapist.put("notificationsType", "Depression Detection");
                            notificationTherapist.put("receiverID", therapistID);
                            notificationTherapist.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());

                            DocumentReference docRef1 = db.collection("notification").document();
                            DocumentReference docRef2 = db.collection("notification").document();

                            WriteBatch batch = db.batch();
                            batch.set(docRef1, notificationData);
                            batch.set(docRef2, notificationTherapist);
                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("DEBUG", "Documents added successfully");
                                    } else {
                                        Log.e("DEBUG", "Error adding documents", task.getException());
                                    }
                                }
                            });
                        } else {
                            db.collection("notification").add(notificationData)
                                    .addOnSuccessListener(documentReference -> Log.d("DEBUG", "Notification successfully written!"))
                                    .addOnFailureListener(e -> Log.e("DEBUG", "Error writing document", e));
                        }
                    }
                });
    }

    private void navigateToTherapistProfile(String userTherapistID) {
        TherapistProfileFragment profileFragment = new TherapistProfileFragment();
        Bundle args = new Bundle();
        args.putString("therapistID", userTherapistID);
        profileFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadUpcomingAppointments(String userID, upcomingAppointmentsCallback upcomingAppointmentsCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Calendar currentDateTime = Calendar.getInstance();

        db.collection("appointment")
                .whereEqualTo("userID", userID)
                .whereEqualTo("appointmentStatus", "confirmed")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                upcomingAppointmentsCallback.onCallback(null);
                            } else {
                                AppointmentsUpcomingFragment.Appointment earliestAppointment = null;
                                long earliestDateTime = Long.MAX_VALUE;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String documentID = document.getId();
                                    AppointmentsUpcomingFragment.Appointment appointment = document.toObject(AppointmentsUpcomingFragment.Appointment.class);
                                    appointment.setDocumentID(documentID);

                                    String appointmentDate = appointment.getDate();
                                    String appointmentTime = appointment.getTime();
                                    long appointmentDateTime = AppointmentsUpcomingFragment.parseDateTime(appointmentDate, appointmentTime);
                                    if (appointmentDateTime < earliestDateTime) {
                                        earliestAppointment = appointment;
                                        earliestDateTime = appointmentDateTime;
                                    }
                                }
                                if (earliestAppointment != null) {
                                    upcomingAppointmentsCallback.onCallback(earliestAppointment);
                                } else {
                                    Log.e("DEBUG", "Error fetching earliest appointment");
                                }
                            }

                        }
                    }
                });
    }
    private void loadTherapistDetails(String therapistID, final therapistCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("therapist")
                .whereEqualTo("therapistID", therapistID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                MapActivity.Therapist therapist = document.toObject(MapActivity.Therapist.class);
                                callback.onCallback(therapist);
                            } catch (Exception e) {
                                Log.e("DEBUG", "Error converting doc to obj", e);
                            }
                        }
                    } else {
                        Log.e("DEBUG", "Error getting documents.", task.getException());
                    }
                });
    }
    private void loadMobileUser(final userCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("mobileUser")
                    .whereEqualTo("userID", userID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    mobileUser user = document.toObject(mobileUser.class);
                                    callback.onCallback(user);
                                } catch (Exception e) {
                                    Log.e("DEBUG", "Error converting doc to obj", e);
                                }
                            }
                        } else {
                            Log.e("DEBUG", "Error getting documents.", task.getException());
                        }
                    });
        } else {
            Log.e("DEBUG", "User not signed in.");
        }

    }

    private void getUserLocation(final LocationCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("mobileUser")
                    .whereEqualTo("userID", userID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()){
                            Log.d("DEBUG", "AREA C");
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            if (document.contains("latitude") && document.contains("longitude")) {
                                double latitude = document.getDouble("latitude");
                                double longitude = document.getDouble("longitude");
                                callback.onCallback(new LatLng(latitude, longitude));
                            } else {
                                callback.onCallback(new LatLng(Double.NaN, Double.NaN));
                            }
                        } else {
                            Log.e("DEBUG", "Error getting user location from database");
                        }
                    });
        } else {
            Log.e("DEBUG", "No user signed in");
        }
    }

    private void setNearestPlaceHint() {
        Log.d("DEBUG", "AREA A");
        getUserLocation(new LocationCallback() {
            @Override
            public void onCallback(LatLng currentUserLocation) {
                Log.d("DEBUG", "AREA B");
                if (currentUserLocation.latitude == Double.NaN || currentUserLocation.longitude == Double.NaN) {
                    startActivity(new Intent(getContext(), LocationActivity.class));
                } else {
                    try {
                        Places.initialize(getContext(), "AIzaSyBX63LUrObENdgvpB3kPO6An8BrDHlfiQg");
                        PlacesClient placesClient = Places.createClient(getContext());
                        Log.d("DEBUG", "AREA C");
                        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME);
                        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                                .setLocationBias(RectangularBounds.newInstance(currentUserLocation, currentUserLocation))
                                .build();
                        Log.d("DEBUG", "AREA D");
                        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                            if (!response.getAutocompletePredictions().isEmpty()) {
                                String nearestPlaceName = response.getAutocompletePredictions().get(0).getPrimaryText(null).toString();
                                Log.d("DEBUG", "AREA E");
                                getActivity().runOnUiThread(() -> locationSearchField.setHint("Nearest place: " + nearestPlaceName));
                                Log.d("DEBUG", "AREA F");
                            } else {
                                Log.e("DEBUG", "No place found");
                            }
                        }).addOnFailureListener((exception) -> {
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                Log.e("DEBUG", "Place not found: " + apiException.getStatusCode());
                            }
                        });
                    } catch (Exception e) {
                        Log.e("DEBUG", "Error in setting nearest place hint", e);
                    }
                }
            }
        });

    }

    private void loadVideos() {
        ArrayList<VideoItem> videos = new ArrayList<>();
        videos.add(new VideoItem("https://www.youtube.com/watch?v=qKcRUOWYQ9w", "https://i.ytimg.com/vi/qKcRUOWYQ9w/hq720.jpg?sqp=-oaymwEcCNAFEJQDSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&amp;rs=AOn4CLDuU-Vlf3QLK-84uxqAYMfoIcAsvQ", "Self-help for low mood and depression | NHS"));
        videos.add(new VideoItem("https://www.youtube.com/watch?v=8-uMPruTUWc", "https://i.ytimg.com/vi/8-uMPruTUWc/hq720.jpg?sqp=-oaymwEcCNAFEJQDSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLAlJxT0kmKvWq9FTOoy9YN6oSGDvA", "Low mood? Depression? A doctor's guide for help and next steps."));
        videos.add(new VideoItem("https://www.youtube.com/watch?v=F9l61cXa6Gg", "https://i.ytimg.com/vi/F9l61cXa6Gg/hq720.jpg?sqp=-oaymwEcCNAFEJQDSFXyq4qpAw4IARUAAIhCGAFwAcABBg==&rs=AOn4CLDRCEwray2_PBZiQP76NTakLf3P6A", "Self care: Managing anxiety and low mood"));

        videoList.clear();
        videoList.addAll(videos);
        adapter.notifyDataSetChanged();
    }
}