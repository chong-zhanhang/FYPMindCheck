package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.AppointmentsFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.HomeFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.NotificationsFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.VisualizationFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    //private ImageView profileImage;
    private TextView profileName, email, dob, age, phoneNum, gender, currentRisk;
    private TextView editProfile, logout;
    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectorFragment = null;
                    startActivity(new Intent(ProfileActivity.this, RealMainActivity.class));
                    onPause();
                } else if (itemId == R.id.nav_profile) {
                    //selectorFragment = new ProfileFragment();
                    selectorFragment = null;
                } else if (itemId == R.id.nav_appointment) {
                    selectorFragment = new AppointmentsFragment();
                    onPause();
                } else if (itemId == R.id.nav_noti) {
                    selectorFragment = new NotificationsFragment();
                    onPause();
                } else if (itemId == R.id.nav_graph) {
                    selectorFragment = new VisualizationFragment();
                    onPause();
                }

                if (selectorFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectorFragment).commit();
                }

                return true;
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        //profileImage = findViewById(R.id.profile_picture);
        profileName = findViewById(R.id.profile_name);
        email = findViewById(R.id.email);
        phoneNum = findViewById(R.id.phone_num);
        gender = findViewById(R.id.gender);
        dob = findViewById(R.id.dob);
        currentRisk = findViewById(R.id.depression_risk);
        age = findViewById(R.id.age);
        editProfile = findViewById(R.id.edit_profile);
        logout = findViewById(R.id.logout);
        loadUserProfile();

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void loadUserProfile() {
        Log.d("DEBUG", "Loading user profile");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("mobileUser")
                    .whereEqualTo("userID", userID)
                    .get()
                    .addOnCompleteListener(task -> {
                        Log.d("DEBUG", "Retrieving user doc");
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("DEBUG", "User Document Retrieved");
                                Map<String, Object> data = document.getData();
                                if (data != null) {
                                    Log.d("DEBUG", "DATA");
                                    String name = String.valueOf(data.get("userName"));
                                    String emailTxt = String.valueOf(data.get("userEmail"));
                                    String phoneNumTxt = String.valueOf(data.get("userPhoneNum"));
                                    String genderTxt = String.valueOf(data.get("userGender"));
                                    String dobTxt = String.valueOf(data.get("userDOB"));
                                    String riskTxt = String.valueOf(document.get("isDepressed"));
                                    //Implement upload image first
                                    //String imageUrl = document.getString();
                                    if (name != null && emailTxt != null && phoneNumTxt != null && genderTxt != null && dobTxt != null && riskTxt != null) {
                                        if (!name.isEmpty() && !emailTxt.isEmpty() && !phoneNumTxt.isEmpty() && !genderTxt.isEmpty() && !dobTxt.isEmpty() && !riskTxt.isEmpty()) {
                                            profileName.setText(name);
                                            email.setText(emailTxt);
                                            phoneNum.setText(phoneNumTxt);
                                            gender.setText(genderTxt);
                                            dob.setText(dobTxt);
                                            age.setText(calculateAge(dobTxt));
                                            if (riskTxt.equals("true")){
                                                currentRisk.setText("High");
                                                currentRisk.setTextColor(Color.parseColor("#FF0000"));
                                            } else if (riskTxt.equals("false")) {
                                                currentRisk.setText("Low");
                                                currentRisk.setTextColor(Color.parseColor("#54C42C"));
                                            } else {
                                                currentRisk.setText("TBA");
                                                currentRisk.setTextColor(Color.parseColor("#1E1E1E"));
                                            }

                                            Log.d("DEBUG", "All data fetched from database and is set onto page");
                                        } else {
                                            Log.e("DEBUG", "At least one of the fields is empty");
                                        }
                                    } else {
                                        Log.e("DEBUG", "At least one field is null");
                                    }
                                }else {
                                    Log.d("DEBUG", "NO DATA");
                                }
                            }
                        } else {
                            if (task.getException() != null) {
                                Log.e("DEBUG", "Error retrieving user document", task.getException());
                                Toast.makeText(ProfileActivity.this, "Error retrieving user document", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("DEBUG", "User document not found");
                                Toast.makeText(ProfileActivity.this, "User document not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        Log.d("DEBUG", "User profile loaded");
    }

    private String calculateAge(String dobTxt) {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.UK);
        Date dob = null;
        int age = -1;
        try {
            dob = sdf.parse(dobTxt);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("DEBUG", e.toString());
        }

        if (dob != null) {
            Calendar dateOfBirth = Calendar.getInstance();
            dateOfBirth.setTime(dob);

            Calendar today = Calendar.getInstance();

            age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dateOfBirth.get(Calendar.DAY_OF_YEAR)) {
                age --;
            }
        } else {
            Log.e("DEBUG", "Parsing error, dob is null");
        }
        Log.d("DEBUG", "Finished calculating age");
        return String.valueOf(age);
    }
}