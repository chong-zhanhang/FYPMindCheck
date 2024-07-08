package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AppointmentsRescheduleActivity extends AppCompatActivity {
    private LinearLayout datesContainer, timeContainer;
    private EditText messageEditText;
    private Button rescheduleAppointment;
    private TextView therapistNameTextView, therapistClinicTextView, therapistAddressTextView;
    private Button selectedDate, selectedTime;
    private interface TherapistCallback {
        void onCallback(MapActivity.Therapist therapist);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments_reschedule);

        Intent intent = getIntent();
        String therapistID = intent.getStringExtra("therapistID");
        String appointmentID = intent.getStringExtra("appointmentID");

        datesContainer = findViewById(R.id.dates_container);
        timeContainer = findViewById(R.id.time_container);
        messageEditText = findViewById(R.id.message);
        rescheduleAppointment = findViewById(R.id.rescheduleAppointment);

        therapistNameTextView = findViewById(R.id.therapistNameAN);
        therapistClinicTextView = findViewById(R.id.therapistClinicAN);
        therapistAddressTextView = findViewById(R.id.therapistAddressAN);

        rescheduleAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAppointmentDetails(appointmentID, therapistID);
                finish();
            }
        });

        fetchTherapistDocument(therapistID, new TherapistCallback() {
            @Override
            public void onCallback(MapActivity.Therapist therapist) {
                therapistNameTextView.setText(therapist.getTherapistName());
                therapistClinicTextView.setText(therapist.getTherapistClinic());
                therapistAddressTextView.setText(therapist.getTherapistAddress());

                loadDates(therapistID);
            }
        });
    }

    private void updateAppointmentDetails(String appointmentID, String therapistID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String date = selectedDate != null ? selectedDate.getText().toString() : null;
        String time = selectedTime != null ? selectedTime.getText().toString() : null;
        String message = messageEditText.getText().toString();

        if (date != null && time != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("appointmentDate", date);
            updates.put("appointmentTime", time);
            updates.put("appointmentNotes", message);
            updates.put("appointmentStatus", "pending");

            db.collection("appointment").document(appointmentID)
                    .update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(AppointmentsRescheduleActivity.this, "Appointment rescheduled, pending approval.", Toast.LENGTH_SHORT).show();
                            createRescheduleNotification(date, time, therapistID);
                            Log.d("DEBUG", "Appointment successfully updated!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("DEBUG", "Error updating document", e);
                        }
                    });
        }

    }

    private void createRescheduleNotification(String date, String time, String therapistID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("notificationsHeader", "Appointment Rescheduled");
        notificationData.put("notificationsMessage", "Your appointment has been rescheduled to " + date + " at " + time + ". Consider accepting or rejecting this appointment.");
        notificationData.put("notificationsReadStatus", "unread");
        notificationData.put("notificationsTimestamp", new Timestamp(new Date()));
        notificationData.put("notificationsType", "Appointment Rescheduled");
        notificationData.put("receiverID", therapistID);
        notificationData.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        db.collection("notification").add(notificationData)
                .addOnSuccessListener(documentReference -> Log.d("DEBUG", "Notification successfully written!"))
                .addOnFailureListener(e -> Log.e("DEBUG", "Error writing document", e));
    }

    private void loadTimeSlots(String therapistID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointment")
                .whereEqualTo("therapistID", therapistID)
                .whereIn("appointmentStatus", Arrays.asList("confirmed", "pending"))
                        .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            QuerySnapshot querySnapshot = task.getResult();
                                            if (querySnapshot != null) {
                                                Set<String> confirmedTimes = new HashSet<>();
                                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                                    String appointmentDate = document.getString("appointmentDate");
                                                    String appointmentTime = document.getString("appointmentTime");
                                                    if (selectedDate != null && selectedDate.getText().toString().equals(appointmentDate)) {
                                                        confirmedTimes.add(appointmentTime);
                                                    }
                                                }
                                                loadAvailableTimeSlots(confirmedTimes);
                                            }
                                        } else {
                                            Log.e("DEBUG", "Error getting confirmed appointments", task.getException());
                                            loadAvailableTimeSlots(new HashSet<>());
                                        }
                                    }
                                });
    }

    private void loadAvailableTimeSlots(Set<String> confirmedTimes) {
        timeContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        String[] times = new String[]{"09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00"};
        for (String time : times) {
            Button button = (Button) inflater.inflate(R.layout.item_horizontal_scroll_appointments, timeContainer, false);
            button.setText(time);
            button.setBackground(getResources().getDrawable(R.drawable.appointment_button_selector));
            if (confirmedTimes.contains(time)) {
                button.setEnabled(false);
            } else {
                button.setOnClickListener(v -> {
                    for (int j = 0; j < timeContainer.getChildCount(); j++) {
                        ((Button) timeContainer.getChildAt(j)).setSelected(false);
                    }
                    button.setSelected(true);

                    selectedTime = button;
                });
            }
            timeContainer.addView(button);
        }
    }

    private void loadDates(String therapistID) {
        datesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(calendar.getTime());

            Button dateButton = (Button) inflater.inflate(R.layout.item_horizontal_scroll_appointments, datesContainer, false);
            dateButton.setText(date);
            dateButton.setBackground(getResources().getDrawable(R.drawable.appointment_button_selector));
            dateButton.setOnClickListener(v -> {
                for (int j = 0; j < datesContainer.getChildCount(); j++) {
                    ((Button) datesContainer.getChildAt(j)).setSelected(false);
                }
                dateButton.setSelected(true);

                selectedDate = dateButton;
                loadTimeSlots(therapistID);
            });

            datesContainer.addView(dateButton);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void fetchTherapistDocument(String therapistID, final TherapistCallback callback) {
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

}