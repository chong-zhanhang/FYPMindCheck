package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class QuestionnaireActivity extends AppCompatActivity {

    private RadioGroup[] questionGroups = new RadioGroup[9];
    private Button submit;
    private static String returnable;
    private static final String COLLECTION_NAME = "mobileUser";
    private static String DOCUMENT_ID;
    private static final int DEPRESSION_THRESHOLD_SCORE = 5;
    public interface FirestoreCallback {
        void onCallback(String documentId);
    }

    public static void scheduleReminder(Context context, long submittedTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, QuestionnaireNotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        long twoWeeksInMillis = 1000 * 60 * 60 * 24 *14;
        long minuteInMillis = 1000 * 60; // for testing purpose only
        Log.d("DEBUG", String.valueOf(submittedTime));

        alarmManager.set(AlarmManager.RTC_WAKEUP, submittedTime + twoWeeksInMillis, pendingIntent);
        Log.d("DEBUG", "REMINDER");
    }

    private static void retrieveDocument(FirestoreCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("DEBUG", "100");
        final String[] docId = new String[1];

        if (currentUser != null) {
            Log.d("DEBUG", "150");
            String userID = currentUser.getUid();
            Log.d("DEBUG", userID);
            db.collection("mobileUser")
                    .whereEqualTo("userID", userID)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d("DEBUG", "175");
                            if (task.isSuccessful() && task.getResult() != null) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    docId[0] = document.getId();
                                    callback.onCallback(docId[0]);
                                    Log.d("DEBUG", "200");
                                }
                                Log.d("DEBUGD", docId[0]);
                            } else {
                                Log.e("DEBUG", "Cannot retrieve user ID");
                                callback.onCallback(null);
                            }
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        questionGroups[0] = findViewById(R.id.radioGroup1);
        questionGroups[1] = findViewById(R.id.radioGroup2);
        questionGroups[2] = findViewById(R.id.radioGroup3);
        questionGroups[3] = findViewById(R.id.radioGroup4);
        questionGroups[4] = findViewById(R.id.radioGroup5);
        questionGroups[5] = findViewById(R.id.radioGroup6);
        questionGroups[6] = findViewById(R.id.radioGroup7);
        questionGroups[7] = findViewById(R.id.radioGroup8);
        questionGroups[8] = findViewById(R.id.radioGroup9);

        submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Area 1");

                retrieveDocument(new FirestoreCallback() {
                    @Override
                    public void onCallback(String documentId) {
                        if (documentId != null) {
                            DOCUMENT_ID = documentId;
                            Log.d("DEBUGS", DOCUMENT_ID);
                            scheduleReminder(QuestionnaireActivity.this, submitQuestionnaire(documentId));
                        } else {
                            DOCUMENT_ID = "";
                            Log.d("DEBUG", "Document ID not found");
                            Toast.makeText(QuestionnaireActivity.this, "Document ID not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private long submitQuestionnaire(String documentId) {
        int[] responses = new int[questionGroups.length];
        boolean allAnswered = true;

        for(int i = 0; i < questionGroups.length; i++){
            int radioButtonID = questionGroups[i].getCheckedRadioButtonId();
            View radioButton = questionGroups[i].findViewById(radioButtonID);
            int idx = questionGroups[i].indexOfChild(radioButton);

            if (idx == -1){
                allAnswered = false;
                break;
            }else {
                responses[i] = idx;
            }
        }
        Log.d("DEBUG", "Area 2");
        long submittedTime = System.currentTimeMillis();

        if (allAnswered) {
            int sumOfScores = 0;
            Map<String, Object> questionnaireData = new HashMap<>();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userID = currentUser != null ? currentUser.getUid() : "anonymous";

            for (int i = 0; i < responses.length; i++){
                sumOfScores += responses[i];
                questionnaireData.put("Question " + (i + 1), responses[i]);
            }
            questionnaireData.put("TotalScore", sumOfScores);
            questionnaireData.put("UserID", userID);
            questionnaireData.put("Timestamp", FieldValue.serverTimestamp());
            submittedTime = System.currentTimeMillis();
            Log.d("DEBUG", "Area 3");

            updateUserDocument(documentId, sumOfScores);
            Log.d("DEBUG", "Area 3.3");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("questionnaire")
                    .add(questionnaireData)
                    .addOnSuccessListener(documentReference -> Log.d("DEBUG", "Data Stored Successfully with ID: " + documentReference))
                    .addOnFailureListener(e -> Log.e("DEBUG", "Error storing data: " + e.getMessage()));
            Log.d("DEBUG", "Area 3.5");

            if (sumOfScores >= DEPRESSION_THRESHOLD_SCORE) {
                Toast.makeText(this, "You are currently experiencing depression. We recommend you to make an appointment with a therapist.", Toast.LENGTH_LONG).show();
                db.collection("mobileUser").whereEqualTo("userID", userID).get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                String therapistID = documentSnapshot.getString("therapistID");
                                if (therapistID != null) {
                                    Intent intent = new Intent(QuestionnaireActivity.this, AppointmentsNewActivity.class);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(QuestionnaireActivity.this, LocationActivity.class);
                                    startActivity(intent);
                                }
                            } else {
                                Log.e("DEBUG", "Error in Questionnaire Activity, no user document found");
                            }
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(QuestionnaireActivity.this, "Error retrieving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            Toast.makeText(QuestionnaireActivity.this, "Questionnaire Submitted", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(QuestionnaireActivity.this, RealMainActivity.class));
            Log.d("DEBUG", "Area 4");
            finish();
        } else {
            Toast.makeText(QuestionnaireActivity.this, "Please answer all the questions.", Toast.LENGTH_SHORT).show();
        }
        saveSubmittedTime(QuestionnaireActivity.this, submittedTime);
        return submittedTime;
    }

    public static void saveSubmittedTime(Context contexts, long submittedTime) {
        SharedPreferences sharedPreferences = contexts.getSharedPreferences("QuestionnairePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("submittedTime", submittedTime);
        editor.apply();
    }

    private void updateUserDocument(String documentId, int sumOfScores) {
        Log.d("DEBUG", "Area -1");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocument = db.collection(COLLECTION_NAME).document(documentId);
        Log.d("DEBUG", "Area -2");
        boolean isDepressed = sumOfScores >= DEPRESSION_THRESHOLD_SCORE;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M", Locale.getDefault());
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());

        Map<String, Object> updateDepressionStatus = new HashMap<>();
        updateDepressionStatus.put("isDepressed", isDepressed);
        Log.d("DEBUG", "Area -3");

        userDocument.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Boolean> depressionHistory = (Map<String, Boolean>) documentSnapshot.get("depressionHistory");
                if (depressionHistory == null) {
                    depressionHistory = new HashMap<>();
                }

                if (depressionHistory.containsKey(currentDate)) {
                    depressionHistory.remove(currentDate);
                    Log.d("DEBUG", "Removed existing entry for current date.");
                }

                depressionHistory.put(currentDate, isDepressed);
                Log.d("DEBUG", "Added new entry for current date");

                updateDepressionStatus.put("depressionHistory", depressionHistory);
                Log.d("DEBUG", "Added depressionHistory to update");

                //Check if "isDepressed" field exists in the document
                userDocument.set(updateDepressionStatus, SetOptions.merge()).addOnSuccessListener(unused -> {
                    Log.d("DEBUG", "User depression status updated successfully.");
                }).addOnFailureListener(e -> {
                    Log.e("DEBUG", "Failed to update user depression status.", e);
                });
            }
        }).addOnFailureListener(ee-> {
            Log.e("DEBUG", "Failed to retrieve user document.", ee);
        });


    }
}