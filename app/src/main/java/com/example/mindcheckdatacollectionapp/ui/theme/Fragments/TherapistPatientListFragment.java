package com.example.mindcheckdatacollectionapp.ui.theme.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TherapistPatientListFragment extends Fragment {
    private RecyclerView recyclerView;
    private PatientAdapter patientAdapter;
    private List<HomeFragment.mobileUser> patients;
    public TherapistPatientListFragment() {
        // Required empty public constructor
    }

    public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {
        private Context context;
        private List <HomeFragment.mobileUser> patients;
        public PatientAdapter(Context context, List<HomeFragment.mobileUser> patients) {
            this.context = context;
            this.patients = patients;
        }
        @NonNull
        @Override
        public PatientAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_therapist_patient_list, parent, false);
            return new PatientAdapter.ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull PatientAdapter.ViewHolder holder, int position) {
            HomeFragment.mobileUser patient = patients.get(position);
            holder.bind(patient, position);
        }
        @Override
        public int getItemCount() { return patients.size(); }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private ImageButton userProfile, callPatient;
            private TextView userName, userPhoneNum;
            private Button dismissPatient, viewPatientHistory;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                userProfile = itemView.findViewById(R.id.userProfile);
                callPatient = itemView.findViewById(R.id.callPatient);
                userName = itemView.findViewById(R.id.userName);
                userPhoneNum = itemView.findViewById(R.id.userPhoneNum);
                dismissPatient = itemView.findViewById(R.id.dismissPatient);
                viewPatientHistory = itemView.findViewById(R.id.viewPatientHistory);
            }
            public void bind(HomeFragment.mobileUser patient, int position) {
                userProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (patient.getUnshared()) {
                            Toast.makeText(getContext(), "Patient did not share data with you.", Toast.LENGTH_SHORT).show();
                        } else {
                            ProfileFragment profileFragment = new ProfileFragment();
                            Bundle args = new Bundle();
                            args.putString("userID", patient.getUserID());
                            profileFragment.setArguments(args);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, profileFragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                });
                callPatient.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phoneNum = patient.getUserPhoneNum();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("tel:" + phoneNum));
                        v.getContext().startActivity(intent);
                    }
                });
                userName.setText(patient.getUserName());
                userPhoneNum.setText(patient.getUserPhoneNum());
                dismissPatient.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String therapist = patient.getTherapistID();
                        if (therapist != null && !therapist.isEmpty()) {
                            removePatient(patient, position);
                        }
                    }
                });
                viewPatientHistory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (patient.getUnshared()) {
                            Toast.makeText(getContext(), "Patient did not share data with you.", Toast.LENGTH_SHORT).show();
                        } else {
                            HistoryFragment historyFragment = new HistoryFragment();
                            Bundle args = new Bundle();
                            args.putString("userID", patient.getUserID());
                            historyFragment.setArguments(args);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, historyFragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                });
            }
            private void removePatient(HomeFragment.mobileUser patient, int position) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("mobileUser")
                        .whereEqualTo("userID", patient.getUserID())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().update("therapistID", null)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        patients.remove(position);
                                                        patientAdapter.notifyItemRemoved(position);
                                                        Log.d("DEBUG", "TherapistID updated to null successfully");
                                                    } else {
                                                        Log.e("DEBUG", "Failed to update therapistID", task.getException());
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Log.e("DEBUG", "Error getting documents", task.getException());
                            }
                        });

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_therapist_patient_list, container, false);

        recyclerView = view.findViewById(R.id.patientListScrollSpace);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        patients = new ArrayList<>();
        patientAdapter = new PatientAdapter(getContext(), patients);
        recyclerView.setAdapter(patientAdapter);

        fetchPatients();

        return view;
    }

    private void fetchPatients() {
        FirebaseUser currentTherapist = FirebaseAuth.getInstance().getCurrentUser();
        if (currentTherapist != null) {
            String therapistID = currentTherapist.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("mobileUser")
                    .whereEqualTo("therapistID", therapistID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                patients.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    HomeFragment.mobileUser patient = document.toObject(HomeFragment.mobileUser.class);
                                    patients.add(patient);
                                }
                                patientAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("DEBUG", "Error fetching appointments: ", task.getException());
                            }
                        }
                    });
        } else {
            Log.e("DEBUG", "Therapist not signed in");
        }
    }
}