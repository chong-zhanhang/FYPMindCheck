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
import com.example.mindcheckdatacollectionapp.ui.theme.AppointmentsRescheduleActivity;
import com.example.mindcheckdatacollectionapp.ui.theme.JournalAdapter;
import com.example.mindcheckdatacollectionapp.ui.theme.LocationActivity;
import com.example.mindcheckdatacollectionapp.ui.theme.MapActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AppointmentsUpcomingFragment extends Fragment {
    private RecyclerView recyclerView;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointments;
    public AppointmentsUpcomingFragment() {
        // Required empty public constructor
    }

    public static class Appointment {
        public interface FirebaseCallback {
            void onCallback(MapActivity.Therapist therapist);
        }
        public interface PatientCallback {
            void onCallback(HomeFragment.mobileUser patient);
        }
        public interface TherapistDetailsCallback {
            void onCallback(String therapistDetails);
        }
        public interface PatientDetailsCallback {
            void onCallback(String patientDetails);
        }
        public String documentID;
        public String appointmentDate;
        public String appointmentNotes;
        public String appointmentTime;
        public String appointmentStatus;
        public String therapistID;
        public String userID;

        public Appointment() {

        }
        public Appointment(String appointmentDate, String appointmentTime, String appointmentStatus, String appointmentNotes, String therapistID, String userID) {
            this.appointmentDate = appointmentDate;
            this.appointmentTime = appointmentTime;
            this.appointmentStatus = appointmentStatus;
            this.appointmentNotes = appointmentNotes;
            this.therapistID = therapistID;
            this.userID = userID;
        }
        public void setDocumentID(String documentID) {
            this.documentID = documentID;
        }
        public void setAppointmentStatus(String appointmentStatus) {
            this.appointmentStatus = appointmentStatus;
        }
        public String getDocumentID() { return documentID; }

        public String getDate() {
            return appointmentDate;
        }
        public String getTime() {
            return appointmentTime;
        }
        public String getStatus() {
            return appointmentStatus;
        }
        public String getNotes() {return appointmentNotes; }
        public void getTherapistName(TherapistDetailsCallback TDCallback) {
            fetchTherapist(therapistID, new FirebaseCallback() {
                @Override
                public void onCallback(MapActivity.Therapist therapist) {
                    TDCallback.onCallback(therapist.getTherapistName());
                }
            });
        }
        public void getTherapistClinic(TherapistDetailsCallback TDCallback) {
            fetchTherapist(therapistID, new FirebaseCallback() {
                @Override
                public void onCallback(MapActivity.Therapist therapist) {
                    TDCallback.onCallback(therapist.getTherapistClinic());
                }
            });
        }
        public void getTherapistAddress(TherapistDetailsCallback TDCallback) {
            fetchTherapist(therapistID, new FirebaseCallback() {
                @Override
                public void onCallback(MapActivity.Therapist therapist) {
                    TDCallback.onCallback(therapist.getTherapistAddress());
                }
            });
        }
        public void getTherapistPhoneNum(TherapistDetailsCallback TDCallback) {
            fetchTherapist(therapistID, new FirebaseCallback() {
                @Override
                public void onCallback(MapActivity.Therapist therapist) {
                    TDCallback.onCallback(therapist.getTherapistPhoneNum());
                }
            });
        }
        public void fetchTherapist(String therapistID, FirebaseCallback callback) {
            Log.d("DEBUG", "Therapist1");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            AtomicReference<MapActivity.Therapist> therapistRef = new AtomicReference<>();
            db.collection("therapist")
                    .whereEqualTo("therapistID", therapistID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG", "Therapist2");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Log.d("DEBUG", "Therapist3");
                                    MapActivity.Therapist therapist = document.toObject(MapActivity.Therapist.class);
                                    therapistRef.set(therapist);

                                } catch (Exception e) {
                                    Log.e("DEBUG", "Error converting doc to obj", e);
                                }

                            }
                            Log.d("DEBUG", "Therapist loaded");
                            callback.onCallback(therapistRef.get());
                        } else {
                            Log.e("DEBUG", "Error getting documents.", task.getException());

                        }
                    });
        }
        public void getPatientPhoneNum(PatientDetailsCallback callback) {
            fetchPatient(userID, new PatientCallback() {
                @Override
                public void onCallback(HomeFragment.mobileUser patient) {
                    callback.onCallback(patient.getUserPhoneNum());
                }
            });
        }
        public void getPatientName(PatientDetailsCallback callback) {
            fetchPatient(userID, new PatientCallback() {
                @Override
                public void onCallback(HomeFragment.mobileUser patient) {
                    callback.onCallback(patient.getUserName());
                }
            });
        }
        public void fetchPatient(String userID, PatientCallback callback){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            AtomicReference<HomeFragment.mobileUser> patientRef = new AtomicReference<>();
            db.collection("mobileUser")
                    .whereEqualTo("userID", userID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    HomeFragment.mobileUser patient = document.toObject(HomeFragment.mobileUser.class);
                                    patientRef.set(patient);

                                } catch (Exception e) {
                                    Log.e("DEBUG", "Error converting doc to obj", e);
                                }

                            }
                            Log.d("DEBUG", "Therapist loaded");
                            callback.onCallback(patientRef.get());
                        } else {
                            Log.e("DEBUG", "Error getting documents.", task.getException());

                        }
                    });
        }
    }

    public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
        private Context context;
        private List<Appointment> appointments;
        public AppointmentAdapter(Context context, List<Appointment> appointments) {
            this.context = context;
            this.appointments = appointments;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_patient_upcoming_appointment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Appointment appointment = appointments.get(position);
            Log.d("DEBUG", "Binding appointment: " + appointment.getDate() + " " + appointment.getTime() + " " + appointment.getStatus());
            holder.bind(appointment, position);
        }

        @Override
        public int getItemCount() {
            return appointments.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView dateTextView, timeTextView, statusTextView, therapistNameTextView, therapistClinicTextView, therapistAddressTextView;
            private ImageButton therapistProfile, callTherapist;
            private Button cancel, reschedule;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                therapistProfile = itemView.findViewById(R.id.therapistProfileCU);
                callTherapist = itemView.findViewById(R.id.callTherapistCU);

                cancel = itemView.findViewById(R.id.cancelAppointmentCU);
                reschedule = itemView.findViewById(R.id.rescheduleAppointmentCU);

                dateTextView = itemView.findViewById(R.id.appointmentDateCU);
                timeTextView = itemView.findViewById(R.id.appointmentTimeCU);
                statusTextView = itemView.findViewById(R.id.appointmentStatusCU);

                therapistNameTextView = itemView.findViewById(R.id.therapistNameCU);
                therapistClinicTextView = itemView.findViewById(R.id.therapistClinicCU);
                therapistAddressTextView = itemView.findViewById(R.id.therapistAddressCU);
            }

            public void bind(Appointment appointment, int position) {
                therapistProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TherapistProfileFragment profileFragment = new TherapistProfileFragment();
                        Bundle args = new Bundle();
                        args.putString("therapistID", appointment.therapistID);
                        profileFragment.setArguments(args);
                        getParentFragment().getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, profileFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });
                callTherapist.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appointment.getTherapistPhoneNum(new Appointment.TherapistDetailsCallback() {
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
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelAppointment(appointment, position);
                    }
                });
                reschedule.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), AppointmentsRescheduleActivity.class);
                        intent.putExtra("therapistID", appointment.therapistID);
                        intent.putExtra("appointmentID", appointment.getDocumentID());
                        startActivity(intent);
                    }
                });
                dateTextView.setText(appointment.getDate());
                timeTextView.setText(appointment.getTime());
                statusTextView.setText("Status: " + appointment.getStatus().toUpperCase());
                appointment.getTherapistName(new Appointment.TherapistDetailsCallback() {
                    @Override
                    public void onCallback(String therapistDetails) {
                        therapistNameTextView.setText(therapistDetails);
                    }
                });
                appointment.getTherapistClinic(new Appointment.TherapistDetailsCallback() {
                    @Override
                    public void onCallback(String therapistDetails) {
                        therapistClinicTextView.setText(therapistDetails);
                    }
                });
                appointment.getTherapistAddress(new Appointment.TherapistDetailsCallback() {
                    @Override
                    public void onCallback(String therapistDetails) {
                        therapistAddressTextView.setText(therapistDetails);
                    }
                });
            }
            public void cancelAppointment(Appointment appointment, int position) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("appointment")
                        .document(appointment.getDocumentID())
                        .update("appointmentStatus", "cancelled")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(itemView.getContext(), "Appointment Cancelled", Toast.LENGTH_SHORT).show();
                                appointments.remove(position);
                                appointmentAdapter.notifyItemRemoved(position);
                                createCancelNotification(appointment);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(itemView.getContext(), "Error Cancelling Appointment", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            private void createCancelNotification(Appointment appointment) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("notificationsHeader", "Appointment Cancelled");
                notificationData.put("notificationsMessage", "Your appointment on " + appointment.getDate() + " at " + appointment.getTime() + " has been cancelled.");
                notificationData.put("notificationsReadStatus", "unread");
                notificationData.put("notificationsTimestamp", new Timestamp(new Date()));
                notificationData.put("notificationsType", "Appointment Cancelled");
                notificationData.put("receiverID", appointment.therapistID);
                notificationData.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                db.collection("notification").add(notificationData)
                        .addOnSuccessListener(documentReference -> Log.d("DEBUG", "Notification successfully written!"))
                        .addOnFailureListener(e -> Log.e("DEBUG", "Error writing document", e));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appointments_upcoming, container, false);

        recyclerView = view.findViewById(R.id.upcomingAppointmentsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        appointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(getContext(), appointments);
        recyclerView.setAdapter(appointmentAdapter);

        fetchUpcomingAppointments();

        return view;
    }

    private void fetchUpcomingAppointments() {
        Log.d("DEBUG", "Appointments1");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Calendar currentDateTime = Calendar.getInstance();

            db.collection("appointment")
                    .whereEqualTo("userID", userID)
                    .whereIn("appointmentStatus", new ArrayList<String>(){{
                        add("confirmed");
                        add("pending");
                    }})
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("DEBUG", "Appointments2");
                                appointments.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String documentID = document.getId();
                                    Appointment appointment = document.toObject(Appointment.class);

                                    String appointmentDate = appointment.getDate();
                                    String appointmentTime = appointment.getTime();
                                    Log.d("DEBUG", appointmentDate);
                                    Log.d("DEBUG", appointmentTime);
                                    long appointmentDateTime = parseDateTime(appointmentDate, appointmentTime);
                                    Log.d("DEBUG", String.valueOf(appointmentDateTime));
                                    Log.d("DEBUG", String.valueOf(currentDateTime.getTimeInMillis()));

                                    if (appointmentDateTime > currentDateTime.getTimeInMillis()) {
                                        appointment.setDocumentID(documentID);
                                        appointments.add(appointment);
                                    }
                                }
                                Log.d("DEBUG", "Appointments3");
                                Log.d("DEBUG", "Number of appointments fetched: " + appointments.size());
                                appointmentAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("DEBUG", "Error fetching appointments: ", task.getException());
                            }
                        }
                    });
        } else {
            Log.e("DEBUG", "User not signed in");
        }
    }

    public static long parseDateTime(String date, String time) {
        Calendar currentYear = Calendar.getInstance();
        int year = currentYear.get(Calendar.YEAR);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String dateWithYear = date + " " + year;
        try {
            Date parsedDate = dateFormat.parse(dateWithYear);
            Date parsedTime = timeFormat.parse(time.split(" - ")[0]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);
            calendar.set(Calendar.HOUR_OF_DAY, parsedTime.getHours());
            calendar.set(Calendar.MINUTE, parsedTime.getMinutes());
            calendar.set(Calendar.SECOND, 0);

            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }
}