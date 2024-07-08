package com.example.mindcheckdatacollectionapp.ui.theme.Fragments;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
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
import com.example.mindcheckdatacollectionapp.ui.theme.AppointmentsRescheduleActivity;
import com.example.mindcheckdatacollectionapp.ui.theme.LoginActivity;
import com.example.mindcheckdatacollectionapp.ui.theme.TherapistLoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TherapistHomeFragment extends Fragment {

    private TextView logout;
    private TextView depressedPatientNum, totalPatientNum;
    private RecyclerView upcomingAppointments, pendingAppointments;
    private List<AppointmentsUpcomingFragment.Appointment> appointmentsUpcoming, appointmentsPending;
    private UpcomingAppointmentAdapter upcomingAppointmentAdapter;
    private PendingAppointmentAdapter pendingAppointmentAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private interface PatientsCallback {
        void onCallback(int patientNum);
    }
    private interface appointmentsCallback {
        void onCallback(AppointmentsUpcomingFragment.Appointment appointment);
    }

    public class PendingAppointmentAdapter extends RecyclerView.Adapter<PendingAppointmentAdapter.ViewHolder> {
        private Context context;
        private List<AppointmentsUpcomingFragment.Appointment> appointments;
        public PendingAppointmentAdapter(Context context, List<AppointmentsUpcomingFragment.Appointment> appointments) {
            this.context = context;
            this.appointments = appointments;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_therapist_pending_appointment, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull PendingAppointmentAdapter.ViewHolder holder, int position) {
            AppointmentsUpcomingFragment.Appointment appointment = appointments.get(position);
            holder.bind(appointment, position);
        }
        @Override
        public int getItemCount() { return appointments.size(); }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private TextView userName, userPhoneNum, appointmentDate, appointmentTime, appointmentStatus;
            private ImageButton userProfile, callPatient;
            private Button accept, reject;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                userProfile = itemView.findViewById(R.id.userProfile);
                callPatient = itemView.findViewById(R.id.callPatient);

                accept = itemView.findViewById(R.id.acceptAppointment);
                reject = itemView.findViewById(R.id.rejectAppointment);

                userName = itemView.findViewById(R.id.userName);
                userPhoneNum = itemView.findViewById(R.id.userPhoneNum);

                appointmentDate = itemView.findViewById(R.id.appointmentDate);
                appointmentTime = itemView.findViewById(R.id.appointmentTime);
                appointmentStatus = itemView.findViewById(R.id.appointmentStatus);
            }
            public void bind(AppointmentsUpcomingFragment.Appointment appointment, int position) {
                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        createAcceptAppointmentNotification(appointment);
                        DocumentReference appointmentRef = db.collection("appointment").document(appointment.documentID);
                        appointmentRef.update("appointmentStatus", "confirmed")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Appointment status updated to confirmed", Toast.LENGTH_SHORT).show();
                                    appointmentsPending.remove(getAdapterPosition());
                                    notifyDataSetChanged();
                                    View parentView = (View) itemView.getRootView();;
                                    if (appointmentsPending.isEmpty()) {
                                        parentView.findViewById(R.id.noPendingAppointments).setVisibility(View.VISIBLE);
                                    } else {
                                        parentView.findViewById(R.id.noPendingAppointments).setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error updating appointment status", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
                reject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        createRejectAppointmentNotification(appointment);
                        DocumentReference appointmentRef = db.collection("appointment").document(appointment.documentID);
                        appointmentRef.update("appointmentStatus", "cancelled")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Appointment cancelled", Toast.LENGTH_SHORT).show();
                                    appointmentsPending.remove(getAdapterPosition());
                                    notifyDataSetChanged();
                                    View parentView = (View) itemView.getRootView();;
                                    if (appointmentsPending.isEmpty()) {
                                        parentView.findViewById(R.id.noPendingAppointments).setVisibility(View.VISIBLE);
                                    } else {
                                        parentView.findViewById(R.id.noPendingAppointments).setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error updating appointment status", Toast.LENGTH_SHORT).show();
                                });
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
                                    getParentFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, profileFragment)
                                            .addToBackStack(null)
                                            .commit();
                                }
                            }
                        });

                    }
                });
                callPatient.setOnClickListener(new View.OnClickListener() {
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
                appointmentDate.setText(appointment.getDate());
                appointmentTime.setText(appointment.getTime());
                appointmentStatus.setText("Status: " + appointment.getStatus().toUpperCase());
                appointment.getPatientPhoneNum(new AppointmentsUpcomingFragment.Appointment.PatientDetailsCallback() {
                    @Override
                    public void onCallback(String patientDetails) {
                        userPhoneNum.setText(patientDetails);
                    }
                });
                appointment.getPatientName(new AppointmentsUpcomingFragment.Appointment.PatientDetailsCallback() {
                    @Override
                    public void onCallback(String patientDetails) {
                        userName.setText(patientDetails);
                    }
                });
            }

            private void createRejectAppointmentNotification(AppointmentsUpcomingFragment.Appointment appointment) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("notificationsHeader", "Appointment Rejected");
                notificationData.put("notificationsMessage", "Your appointment on " + appointment.getDate() + " at " + appointment.getTime() + " has been rejected. Consider making another appointment.");
                notificationData.put("notificationsReadStatus", "unread");
                notificationData.put("notificationsTimestamp", new Timestamp(new Date()));
                notificationData.put("notificationsType", "Appointment Rejected");
                notificationData.put("receiverID", appointment.userID);
                notificationData.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                db.collection("notification").add(notificationData)
                        .addOnSuccessListener(documentReference -> Log.d("DEBUG", "Notification successfully written!"))
                        .addOnFailureListener(e -> Log.e("DEBUG", "Error writing document", e));
            }

            private void createAcceptAppointmentNotification(AppointmentsUpcomingFragment.Appointment appointment) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("notificationsHeader", "Appointment Accepted");
                notificationData.put("notificationsMessage", "Your appointment on " + appointment.getDate() + " at " + appointment.getTime() + " has been accepted.");
                notificationData.put("notificationsReadStatus", "unread");
                notificationData.put("notificationsTimestamp", new Timestamp(new Date()));
                notificationData.put("notificationsType", "Appointment Accepted");
                notificationData.put("receiverID", appointment.userID);
                notificationData.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                db.collection("notification").add(notificationData)
                        .addOnSuccessListener(documentReference -> Log.d("DEBUG", "Notification successfully written!"))
                        .addOnFailureListener(e -> Log.e("DEBUG", "Error writing document", e));
            }
        }
    }
    public class UpcomingAppointmentAdapter extends RecyclerView.Adapter<UpcomingAppointmentAdapter.ViewHolder> {
        private Context context;
        private List<AppointmentsUpcomingFragment.Appointment> appointments;
        public UpcomingAppointmentAdapter(Context context, List<AppointmentsUpcomingFragment.Appointment> appointments) {
            this.context = context;
            this.appointments = appointments;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_therapist_upcoming_appointment, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppointmentsUpcomingFragment.Appointment appointment = appointments.get(position);
            holder.bind(appointment, position);
        }

        @Override
        public int getItemCount() { return appointments.size(); }



        private class ViewHolder extends RecyclerView.ViewHolder {
            private TextView userName, userPhoneNum, date, time, status;
            private ImageButton userProfile, callPatient;
            private Button cancel;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                userProfile = itemView.findViewById(R.id.userProfile);
                callPatient = itemView.findViewById(R.id.callPatient);

                cancel = itemView.findViewById(R.id.cancelAppointmentCU);

                userName = itemView.findViewById(R.id.userName);
                userPhoneNum = itemView.findViewById(R.id.userPhoneNum);

                date = itemView.findViewById(R.id.appointmentDate);
                time = itemView.findViewById(R.id.appointmentTime);
                status = itemView.findViewById(R.id.appointmentStatus);
            }
            public void bind(AppointmentsUpcomingFragment.Appointment appointment, int position) {
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
                                    getParentFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, profileFragment)
                                            .addToBackStack(null)
                                            .commit();
                                }
                            }
                        });

                    }
                });
                callPatient.setOnClickListener(new View.OnClickListener() {
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
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelAppointment(appointment, position);
                        View parentView = (View) itemView.getRootView();;
                        if (appointmentsUpcoming.isEmpty()) {
                            parentView.findViewById(R.id.noUpcomingAppointments).setVisibility(View.VISIBLE);
                        } else {
                            parentView.findViewById(R.id.noUpcomingAppointments).setVisibility(View.GONE);
                        }
                    }
                });

                date.setText(appointment.getDate());
                time.setText(appointment.getTime());
                status.setText("Status: " + appointment.getStatus().toUpperCase());
                appointment.getPatientPhoneNum(new AppointmentsUpcomingFragment.Appointment.PatientDetailsCallback() {
                    @Override
                    public void onCallback(String patientDetails) {
                        userPhoneNum.setText(patientDetails);
                    }
                });
                appointment.getPatientName(new AppointmentsUpcomingFragment.Appointment.PatientDetailsCallback() {
                    @Override
                    public void onCallback(String patientDetails) {
                        userName.setText(patientDetails);
                    }
                });
            }

            public void cancelAppointment(AppointmentsUpcomingFragment.Appointment appointment, int position) {
                db.collection("appointment")
                        .document(appointment.getDocumentID())
                        .update("appointmentStatus", "cancelled")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(itemView.getContext(), "Appointment Cancelled", Toast.LENGTH_SHORT).show();
                                appointmentsUpcoming.remove(position);
                                upcomingAppointmentAdapter.notifyItemRemoved(position);
                                createCancelNotification(appointment);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(itemView.getContext(), "Error Cancelling Appointment", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            private void createCancelNotification(AppointmentsUpcomingFragment.Appointment appointment) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("notificationsHeader", "Appointment Cancelled");
                notificationData.put("notificationsMessage", "Your appointment on " + appointment.getDate() + " at " + appointment.getTime() + " has been cancelled.");
                notificationData.put("notificationsReadStatus", "unread");
                notificationData.put("notificationsTimestamp", new Timestamp(new Date()));
                notificationData.put("notificationsType", "Appointment Cancelled");
                notificationData.put("receiverID", appointment.userID);
                notificationData.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                db.collection("notification").add(notificationData)
                        .addOnSuccessListener(documentReference -> Log.d("DEBUG", "Notification successfully written!"))
                        .addOnFailureListener(e -> Log.e("DEBUG", "Error writing document", e));
            }
        }
    }

    public TherapistHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_therapist_home, container, false);
        logout = view.findViewById(R.id.logout);
        depressedPatientNum = view.findViewById(R.id.depressedPatientNum);
        totalPatientNum = view.findViewById(R.id.totalPatientNum);
        upcomingAppointments = view.findViewById(R.id.upcomingAppointmentsScrollSpace);
        pendingAppointments = view.findViewById(R.id.pendingAppointmentsScrollSpace);

        upcomingAppointments.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        pendingAppointments.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        appointmentsUpcoming = new ArrayList<>();
        upcomingAppointmentAdapter = new UpcomingAppointmentAdapter(getContext(), appointmentsUpcoming);
        upcomingAppointments.setAdapter(upcomingAppointmentAdapter);
        fetchUpcoming(view);

        appointmentsPending = new ArrayList<>();
        pendingAppointmentAdapter = new PendingAppointmentAdapter(getContext(), appointmentsPending);
        pendingAppointments.setAdapter(pendingAppointmentAdapter);
        fetchPending(view);

        getDepressedPatient(new PatientsCallback() {
            @Override
            public void onCallback(int patientNum) {
                depressedPatientNum.setText(String.valueOf(patientNum));
            }
        });
        getTotalPatient(new PatientsCallback() {
            @Override
            public void onCallback(int patientNum) {
                String txt = "/" + String.valueOf(patientNum);
                totalPatientNum.setText(txt);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), TherapistLoginActivity.class));
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        return view;
    }

    private void fetchPending(View view) {
        FirebaseUser currentTherapist = FirebaseAuth.getInstance().getCurrentUser();
        if (currentTherapist != null) {
            String therapistID = currentTherapist.getUid();
            db.collection("appointment")
                    .whereEqualTo("therapistID", therapistID)
                    .whereEqualTo("appointmentStatus", "pending")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                appointmentsPending.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    AppointmentsUpcomingFragment.Appointment appointment = document.toObject(AppointmentsUpcomingFragment.Appointment.class);
                                    appointment.setDocumentID(document.getId());
                                    appointmentsPending.add(appointment);
                                }
                                Log.d("DEBUG", String.valueOf(appointmentsPending.size()));
                                pendingAppointmentAdapter.notifyDataSetChanged();
                                if (appointmentsPending.isEmpty()) {
                                    view.findViewById(R.id.noPendingAppointments).setVisibility(View.VISIBLE);
                                } else {
                                    view.findViewById(R.id.noPendingAppointments).setVisibility(View.GONE);
                                }
                            } else {
                                Log.e("DEBUG", "Error fetching appointments: ", task.getException());
                            }
                        }
                    });
        } else {
            Log.e("DEBUG", "Therapist not signed in.");
        }
    }

    private void fetchUpcoming(View view) {
        FirebaseUser currentTherapist = FirebaseAuth.getInstance().getCurrentUser();
        if (currentTherapist != null) {
            String therapistID = currentTherapist.getUid();
            db.collection("appointment")
                    .whereEqualTo("therapistID", therapistID)
                    .whereEqualTo("appointmentStatus", "confirmed")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                appointmentsUpcoming.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    AppointmentsUpcomingFragment.Appointment appointment = document.toObject(AppointmentsUpcomingFragment.Appointment.class);
                                    appointment.setDocumentID(document.getId());
                                    appointmentsUpcoming.add(appointment);
                                }
                                Log.d("DEBUG", String.valueOf(appointmentsUpcoming.size()));
                                upcomingAppointmentAdapter.notifyDataSetChanged();
                                if (appointmentsUpcoming.isEmpty()) {
                                    view.findViewById(R.id.noUpcomingAppointments).setVisibility(View.VISIBLE);
                                } else {
                                    view.findViewById(R.id.noUpcomingAppointments).setVisibility(View.GONE);
                                }
                            } else {
                                Log.e("DEBUG", "Error fetching appointments: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void getTotalPatient(PatientsCallback patientsCallback) {
        FirebaseUser currentTherapist = FirebaseAuth.getInstance().getCurrentUser();
        if (currentTherapist != null) {
            String therapistID = currentTherapist.getUid();
            db.collection("mobileUser")
                    .whereEqualTo("therapistID", therapistID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            patientsCallback.onCallback(count);
                        } else {
                            Log.e("DEBUG", "Error counting users: ", task.getException());
                        }
                    });

        } else {
            Log.e("DEBUG", "Therapist not signed in.");
        }
    }

    private void getDepressedPatient(PatientsCallback patientsCallback) {
        FirebaseUser currentTherapist = FirebaseAuth.getInstance().getCurrentUser();
        if (currentTherapist != null) {
            String therapistID = currentTherapist.getUid();
            db.collection("mobileUser")
                    .whereEqualTo("therapistID", therapistID)
                    .whereEqualTo("isDepressed", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            patientsCallback.onCallback(count);
                        } else {
                            Log.e("DEBUG", "Error counting depressed users: ", task.getException());
                        }
                    });

        } else {
            Log.e("DEBUG", "Therapist not signed in.");
        }
    }
}