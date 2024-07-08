
package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

public class AppointmentsNewActivity extends AppCompatActivity {
    private LinearLayout datesContainer, timeContainer;
    private EditText messageEditText;
    private Button bookAppointment;
    private TextView selectAnotherTherapist;
    private TextView therapistNameTextView, therapistClinicTextView, therapistAddressTextView;
    private Button selectedDate, selectedTime;
    private interface TherapistCallback {
        void onCallback(String therapistID);
    }
    private interface DetailsCallback {
        void onCallback(String therapistName, String therapistClinic, String therapistAddress);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments_new);

        datesContainer = findViewById(R.id.dates_container);
        timeContainer = findViewById(R.id.time_container);
        messageEditText = findViewById(R.id.message);
        bookAppointment = findViewById(R.id.bookAppointment);
        selectAnotherTherapist = findViewById(R.id.selectAnotherTherapist);
        therapistNameTextView = findViewById(R.id.therapistNameAN);
        therapistClinicTextView = findViewById(R.id.therapistClinicAN);
        therapistAddressTextView = findViewById(R.id.therapistAddressAN);
        fetchTherapistDocument(new TherapistCallback() {
            @Override
            public void onCallback(String therapistID) {
                bookAppointment.setOnClickListener(v -> saveAppointmentToDatabase(therapistID));
                fetchTherapistDetails(therapistID, new DetailsCallback() {
                    @Override
                    public void onCallback(String therapistName, String therapistClinic, String therapistAddress) {
                        therapistNameTextView.setText(therapistName);
                        therapistClinicTextView.setText(therapistClinic);
                        therapistAddressTextView.setText(therapistAddress);
                    }
                });
                loadDates(therapistID);
            }
        });


        selectAnotherTherapist.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
    }

    private void fetchTherapistDocument(final TherapistCallback callback) {
        Log.d("DEBUG", "AREA 1");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("mobileUser")
                    .whereEqualTo("userID", userID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d("DEBUG", "AREA 2");
                            if (task.isSuccessful()) {
                                Log.d("DEBUG", "AREA 3");
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null) {
                                    Log.d("DEBUG", "AREA 4");
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        Log.d("DEBUG", "AREA 5");
                                        String therapistID = document.getString("therapistID");
                                        if (therapistID != null) {
                                            Log.d("DEBUG", "AREA 6");
                                            callback.onCallback(therapistID);
                                        } else {
                                            Log.e("DEBUG", "Therapist not assigned");
                                        }
                                    }
                                } else {
                                    Log.e("DEBUG", "No user document found");
                                }

                            } else {
                                Log.e("DEBUG", "Error fetching user document");
                            }
                        }
                    });
        } else {
            Log.e("DEBUG", "User not signed in");
        }
    }

    private void fetchTherapistDetails(String therapistID, final DetailsCallback callback) {
        Log.d("DEBUG", "AREA 7");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("therapist")
                .whereEqualTo("therapistID", therapistID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("DEBUG", "AREA 8");
                        if (task.isSuccessful()) {
                            Log.d("DEBUG", "AREA 9");
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                Log.d("DEBUG", "AREA 10");
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    Log.d("DEBUG", "AREA 11");
                                    String therapistName = document.getString("therapistName");
                                    String therapistAddress = document.getString("therapistAddress");
                                    String therapistClinic = document.getString("therapistClinic");

                                    callback.onCallback(therapistName, therapistClinic, therapistAddress);
                                }
                            }
                        }
                    }
                });
    }

    private void saveAppointmentToDatabase(String therapistID) {
        Log.d("DEBUG", "AREA 12");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userID = currentUser.getUid();
        String date = selectedDate != null ? selectedDate.getText().toString() : null;
        String time = selectedTime != null ? selectedTime.getText().toString() : null;
        String message = messageEditText.getText().toString();

        if (date != null && time != null) {
            Map<String, Object> appointment = new HashMap<>();
            appointment.put("appointmentDate", date);
            appointment.put("appointmentTime", time);
            appointment.put("appointmentNotes", message);
            appointment.put("appointmentStatus", "pending");
            appointment.put("userID", userID);
            appointment.put("therapistID", therapistID);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("appointment")
                    .add(appointment)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("DEBUG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        Toast.makeText(this, "Appointment Made!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AppointmentsNewActivity.this, RealMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    }).addOnFailureListener(e -> {
                        Log.e("DEBUG", "Error adding document", e);
                        Toast.makeText(this, "Error making appointment", Toast.LENGTH_SHORT).show();
                    });

        } else {
            Log.e("DEBUG", "Date or time not selected");
            Toast.makeText(this, "Date or time not selected", Toast.LENGTH_SHORT).show();
        }
        createNewAppointmentNotification(date, time, userID);
    }

    private void createNewAppointmentNotification(String date, String time, String userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("mobileUser").whereEqualTo("userID", userID).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        if (documentSnapshot.contains("userName") && documentSnapshot.contains("therapistID")) {
                            String userName = documentSnapshot.getString("userName");
                            String therapistID = documentSnapshot.getString("therapistID");
                            String message = userName + " has requested for an appointment on " + date + " at " + time + ". Select Accept or Reject this appointment.";
                            Map<String, Object> notificationData = new HashMap<>();
                            notificationData.put("notificationsHeader", "New Appointment");
                            notificationData.put("notificationsMessage", message);
                            notificationData.put("notificationsReadStatus", "unread");
                            notificationData.put("notificationsTimestamp", new Timestamp(new Date()));
                            notificationData.put("notificationsType", "New Appointment");
                            notificationData.put("receiverID", therapistID);
                            notificationData.put("senderID", userID);
                            db.collection("notification").add(notificationData)
                                    .addOnSuccessListener(documentReference -> Log.d("DEBUG", "Notification successfully written!"))
                                    .addOnFailureListener(e -> Log.e("DEBUG", "Error writing document", e));
                        }
                    } else {
                        Log.e("DEBUG", "querySnapshot is empty");
                    }
                });


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
            Log.d("DEBUG", confirmedTimes.toString());
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
}