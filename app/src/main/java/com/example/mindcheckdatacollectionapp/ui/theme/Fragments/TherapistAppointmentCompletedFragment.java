package com.example.mindcheckdatacollectionapp.ui.theme.Fragments;

import static com.example.mindcheckdatacollectionapp.ui.theme.Fragments.AppointmentsCompletedFragment.combineDateTime;

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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class TherapistAppointmentCompletedFragment extends Fragment {
    private RecyclerView recyclerView;
    private AppointmentAdapter appointmentAdapter;
    private List<AppointmentsUpcomingFragment.Appointment> appointments;
    public TherapistAppointmentCompletedFragment() {
        // Required empty public constructor
    }

    private class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
        private Context context;
        private List<AppointmentsUpcomingFragment.Appointment> appointments;
        public AppointmentAdapter(Context context, List<AppointmentsUpcomingFragment.Appointment> appointments) {
            this.context = context;
            this.appointments = appointments;
        }
        @NonNull
        @Override
        public AppointmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_therapist_completed_appointment, parent, false);
            return new AppointmentAdapter.ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull AppointmentAdapter.ViewHolder holder, int position) {
            AppointmentsUpcomingFragment.Appointment appointment = appointments.get(position);
            holder.bind(appointment, position);
        }
        @Override
        public int getItemCount() {
            return appointments.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private TextView userName, userPhoneNum, status, date, time;
            private ImageButton userProfile, callUser;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.userName);
                userPhoneNum = itemView.findViewById(R.id.userPhoneNum);
                status = itemView.findViewById(R.id.appointmentStatus);
                date = itemView.findViewById(R.id.appointmentDateCC);
                time = itemView.findViewById(R.id.appointmentTimeCC);
                userProfile = itemView.findViewById(R.id.userProfile);
                callUser = itemView.findViewById(R.id.callUser);
            }
            public void bind(AppointmentsUpcomingFragment.Appointment appointment, int position) {
                date.setText(appointment.getDate());
                time.setText(appointment.getTime());
                status.setText("Status: " + appointment.getStatus().toUpperCase());
                appointment.getPatientName(new AppointmentsUpcomingFragment.Appointment.PatientDetailsCallback() {
                    @Override
                    public void onCallback(String patientDetails) {
                        userName.setText(patientDetails);
                    }
                });
                appointment.getPatientPhoneNum(new AppointmentsUpcomingFragment.Appointment.PatientDetailsCallback() {
                    @Override
                    public void onCallback(String patientDetails) {
                        userPhoneNum.setText(patientDetails);
                    }
                });
                userProfile.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       FirebaseFirestore db = FirebaseFirestore.getInstance();
                       Query query = db.collection("mobileUser").whereEqualTo("userID", appointment.userID);
                       query.get().addOnCompleteListener(task -> {
                           if (task.isSuccessful() && !task.getResult().isEmpty()) {
                               DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                               Boolean currentUnsharedState = documentSnapshot.getBoolean("unshared");
                               if (currentUnsharedState == Boolean.TRUE) {
                                   Toast.makeText(getContext(), "Patient did not share data with you.", Toast.LENGTH_SHORT).show();
                               } else {
                                   ProfileFragment profileFragment = new ProfileFragment();
                                   Bundle args = new Bundle();
                                   args.putString("userID", appointment.userID);
                                   profileFragment.setArguments(args);
                                   getParentFragment().getParentFragmentManager().beginTransaction()
                                           .replace(R.id.fragment_container, profileFragment)
                                           .addToBackStack(null)
                                           .commit();
                               }
                           }

                       });
                   }
               });
                callUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appointment.getPatientPhoneNum(new AppointmentsUpcomingFragment.Appointment.PatientDetailsCallback() {
                            @Override
                            public void onCallback(String patientDetails) {
                                String phoneNum = patientDetails.toString();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("tel:" + phoneNum));
                                v.getContext().startActivity(intent);
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_therapist_appointment_completed, container, false);
        recyclerView = view.findViewById(R.id.completedAppointmentsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(getContext(), appointments);
        recyclerView.setAdapter(appointmentAdapter);
        fetchAppointments();

        return view;
    }

    private void fetchAppointments() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String therapistID = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            long currentTime = System.currentTimeMillis();

            db.collection("appointment")
                    .whereEqualTo("therapistID", therapistID)
                    .whereIn("appointmentStatus", new ArrayList<String>(){{
                        add("confirmed");
                        add("completed");
                    }})
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                appointments.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String date = document.getString("appointmentDate");
                                    String time = document.getString("appointmentTime");
                                    long appointmentDateTime = combineDateTime(date, time);
                                    if (appointmentDateTime <= currentTime) {
                                        String status = document.getString("appointmentStatus");
                                        if ("confirmed".equals(status)) {
                                            db.collection("appointment")
                                                    .document(document.getId())
                                                    .update("appointmentStatus", "completed")
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Log.d("DEBUG", "Appointment status updated to completed");
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("DEBUG", "Error updating appointment status: ", e);
                                                        }
                                                    });
                                        }
                                        AppointmentsUpcomingFragment.Appointment appointment = document.toObject(AppointmentsUpcomingFragment.Appointment.class);
                                        appointments.add(appointment);
                                    }
                                }
                                Log.d("DEBUG", "Number of appointments fetched: " + appointments.size());
                                appointmentAdapter.notifyDataSetChanged();
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