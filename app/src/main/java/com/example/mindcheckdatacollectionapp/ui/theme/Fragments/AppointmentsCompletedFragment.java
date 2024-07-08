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
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentsCompletedFragment extends Fragment {
    private RecyclerView recyclerView;
    private AppointmentAdapter appointmentAdapter;
    private List<AppointmentsUpcomingFragment.Appointment> appointments;
    public AppointmentsCompletedFragment() {
        // Required empty public constructor
    }

    public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
        private Context context;
        private List<AppointmentsUpcomingFragment.Appointment> appointments;
        public AppointmentAdapter(Context context, List<AppointmentsUpcomingFragment.Appointment> appointments) {
            this.context = context;
            this.appointments = appointments;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_patient_cancelled_appointment, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppointmentsUpcomingFragment.Appointment appointment = appointments.get(position);
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
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                therapistProfile = itemView.findViewById(R.id.therapistProfileCC);
                callTherapist = itemView.findViewById(R.id.callTherapistCC);

                dateTextView = itemView.findViewById(R.id.appointmentDateCC);
                timeTextView = itemView.findViewById(R.id.appointmentTimeCC);
                statusTextView = itemView.findViewById(R.id.appointmentStatusCC);

                therapistNameTextView = itemView.findViewById(R.id.therapistNameCC);
                therapistClinicTextView = itemView.findViewById(R.id.therapistClinicCC);
                therapistAddressTextView = itemView.findViewById(R.id.therapistAddressCC);
            }
            public void bind(AppointmentsUpcomingFragment.Appointment appointment, int position) {
                dateTextView.setText(appointment.getDate());
                timeTextView.setText(appointment.getTime());
                statusTextView.setText("Status: " + appointment.getStatus().toUpperCase());
                appointment.getTherapistName(new AppointmentsUpcomingFragment.Appointment.TherapistDetailsCallback() {
                    @Override
                    public void onCallback(String therapistDetails) {
                        therapistNameTextView.setText(therapistDetails);
                    }
                });
                appointment.getTherapistClinic(new AppointmentsUpcomingFragment.Appointment.TherapistDetailsCallback() {
                    @Override
                    public void onCallback(String therapistDetails) {
                        therapistClinicTextView.setText(therapistDetails);
                    }
                });
                appointment.getTherapistAddress(new AppointmentsUpcomingFragment.Appointment.TherapistDetailsCallback() {
                    @Override
                    public void onCallback(String therapistDetails) {
                        therapistAddressTextView.setText(therapistDetails);
                    }
                });
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

            }
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appointments_completed, container, false);
        recyclerView = view.findViewById(R.id.completedAppointmentsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(getContext(), appointments);
        recyclerView.setAdapter(appointmentAdapter);
        fetchAppointments();

        return view;
    }

    private void fetchAppointments() {
        //Fetch "completed" and any documents where date and time has passed current time
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            long currentTime = System.currentTimeMillis();

            db.collection("appointment")
                    .whereEqualTo("userID", userID)
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
                                    Log.d("DEBUG", String.valueOf(appointmentDateTime));
                                    Log.d("DEBUG", String.valueOf(currentTime));
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
                                        Log.d("DEBUG", "Field value: " + document.getString("appointmentDate"));
                                        AppointmentsUpcomingFragment.Appointment appointment = document.toObject(AppointmentsUpcomingFragment.Appointment.class);
                                        //appointment.setAppointmentStatus("completed");
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
            Log.e("DEBUG", "User not signed in");
        }
    }

    public static long combineDateTime(String date, String time) {
        Calendar currentYear = Calendar.getInstance();
        int year = currentYear.get(Calendar.YEAR);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault());

        String dateWithYear = date + " " + year;
        Date parsedDate;
        try {
            parsedDate = dateFormat.parse(dateWithYear);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Return -1 if parsing fails
        }

        String[] timeParts = time.split(" - ");
        String startTimeString = timeParts[0];
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date parsedStartTime;
        try {
            parsedStartTime = timeFormat.parse(startTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parsedDate);
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(parsedStartTime);
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();
    }
}