package com.example.mindcheckdatacollectionapp.ui.theme.Fragments;

import android.appwidget.AppWidgetHost;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;


public class TherapistProfileFragment extends Fragment {
    private TextView profileName, clinic, address, email, phoneNum, gender, licenseNum, qualification;


    public TherapistProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_therapist_profile, container, false);

        profileName = view.findViewById(R.id.profile_name);
        clinic = view.findViewById(R.id.therapistClinic);
        address = view.findViewById(R.id.therapistAddress);
        email = view.findViewById(R.id.email);
        phoneNum = view.findViewById(R.id.phone_num);
        gender = view.findViewById(R.id.gender);
        licenseNum = view.findViewById(R.id.therapistLicenseNum);
        qualification = view.findViewById(R.id.qualification);

        Bundle args = getArguments();
        if (args != null) {
            String therapistID = args.getString("therapistID");
            loadTherapistProfile(therapistID);
        } else {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String currentUserID = currentUser.getUid();
                loadTherapistProfile(currentUserID);
            }
        }


        return view;
    }

    private void loadTherapistProfile(String therapistID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("therapist")
                .whereEqualTo("therapistID", therapistID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                String nameTxt = String.valueOf(data.get("therapistName"));
                                String clinicTxt = String.valueOf(data.get("therapistClinic"));
                                String addressTxt = String.valueOf(data.get("therapistAddress"));
                                String emailTxt = String.valueOf(data.get("therapistEmail"));
                                String phoneNumTxt = String.valueOf(data.get("therapistPhoneNum"));
                                String genderTxt = String.valueOf(data.get("therapistGender"));
                                String licenseNumTxt = String.valueOf(data.get("therapistLicenseNum"));
                                String qualificationTxt = String.valueOf(data.get("therapistQualification"));
                                if (nameTxt != null && clinicTxt != null && addressTxt != null && emailTxt != null && phoneNumTxt != null && genderTxt != null && licenseNumTxt != null && qualificationTxt != null) {
                                    if (!nameTxt.isEmpty() && !clinicTxt.isEmpty() && !addressTxt.isEmpty() && !emailTxt.isEmpty() && !phoneNumTxt.isEmpty() && !genderTxt.isEmpty() && !licenseNumTxt.isEmpty() && !qualificationTxt.isEmpty()) {
                                        profileName.setText(nameTxt);
                                        clinic.setText(clinicTxt);
                                        address.setText(addressTxt);
                                        email.setText(emailTxt);
                                        phoneNum.setText(phoneNumTxt);
                                        gender.setText(genderTxt);
                                        licenseNum.setText(licenseNumTxt);
                                        qualification.setText(qualificationTxt);
                                        Log.d("DEBUG", "All data fetched from database and is set onto page");
                                    } else {
                                        Log.e("DEBUG", "At least one of the fields is empty");
                                    }
                                } else {
                                    Log.e("DEBUG", "At least one field is null");
                                }
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
        Log.d("DEBUG", "Therapist profile loaded");
    }
}