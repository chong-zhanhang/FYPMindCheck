package com.example.mindcheckdatacollectionapp.ui.theme.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.example.mindcheckdatacollectionapp.ui.theme.EditProfileActivity;
import com.example.mindcheckdatacollectionapp.ui.theme.LoginActivity;
import com.example.mindcheckdatacollectionapp.ui.theme.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView profileName, email, dob, age, phoneNum, gender, currentRisk;
    private TextView editProfile, logout;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileName = view.findViewById(R.id.profile_name);
        email = view.findViewById(R.id.email);
        phoneNum = view.findViewById(R.id.phone_num);
        gender = view.findViewById(R.id.gender);
        dob = view.findViewById(R.id.dob);
        currentRisk = view.findViewById(R.id.depression_risk);
        age = view.findViewById(R.id.age);
        editProfile = view.findViewById(R.id.edit_profile);
        logout = view.findViewById(R.id.logout);

        Bundle args = getArguments();
        if (args != null) {
            String userID = args.getString("userID");
            loadUserProfile(userID);
            editProfile.setVisibility(View.GONE);
            logout.setVisibility(View.GONE);
        } else {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String currentUserID = currentUser.getUid();
                loadUserProfile(currentUserID);
                editProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), EditProfileActivity.class));
                    }
                });
                logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    }
                });
            }
        }
        return view;
    }

    private void loadUserProfile(String userID) {
        Log.d("DEBUG", "Loading user profile");
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
                            Toast.makeText(getActivity(), "Error retrieving user document", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("DEBUG", "User document not found");
                            Toast.makeText(getActivity(), "User document not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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