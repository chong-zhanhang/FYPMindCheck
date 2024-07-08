package com.example.mindcheckdatacollectionapp.ui.theme;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.AppointmentsFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.HomeFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.NotificationsFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.ProfileFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.TherapistProfileFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.VisualizationFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RealMainActivity extends AppCompatActivity {
    public static class DataSyncWorker extends Worker {
        private String PREF_FILE_NAME = "AppPreferences";
        private String LAST_SYNC_TIME_KEY = "lastSyncTime";
        public DataSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }
        @NonNull
        @Override
        public Result doWork() {
            //if (shouldSync()){
                Log.d("DEBUG", "FT Syncing");
                syncData();
                return Result.success();
//            } else {
//                Log.d("DEBUG", "FT Failed Syncing");
//            }
            //return Result.retry();
        }

        private boolean shouldSync() {
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
            long lastSyncTime = prefs.getLong(LAST_SYNC_TIME_KEY, 0);
            long currentTime = new Date().getTime();
            long threeDaysMillis = TimeUnit.DAYS.toMillis(3);
            return (currentTime - lastSyncTime) >= threeDaysMillis;
        }

        private void syncData() {
            //Implementation to prepare data and make the API call
            //Update last sync time
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
            long lastSyncTime = prefs.getLong(LAST_SYNC_TIME_KEY, 0);
            long currentTime = new Date().getTime();
            long threeDaysMillis = TimeUnit.DAYS.toMillis(3);
//            long threeDaysMillis = TimeUnit.MINUTES.toMillis(3); //TODO change back to 3 days
            if ((currentTime - lastSyncTime) >= threeDaysMillis){
                Log.d("DEBUG", "FT has been 3 days");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(LAST_SYNC_TIME_KEY, new Date().getTime());
                editor.apply();

                Log.d("DEBUG", "FT Fine-tuned operations started");

                DepressionDetectionApiService apiService = new DepressionDetectionApiService();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userID = currentUser.getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference typingSessionRef = db.collection("typingSession");
                    Query query = typingSessionRef.whereEqualTo("userId", userID).whereEqualTo("trained", "False");
                    query.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<DepressionDetectionApiService.typingSession> typingSessions = new ArrayList<>();
                            List<DepressionDetectionApiService.typingSession> userTypingSessions = new ArrayList<>();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                DepressionDetectionApiService.typingSession session = documentSnapshot.toObject(DepressionDetectionApiService.typingSession.class);
                                session.setDocId(documentSnapshot.getId());
                                typingSessions.add(session);
                                userTypingSessions.add(session);
                            }
                            Log.d("DEBUG", "FT Retrieved training data");

                            db.collection("mobileUser").whereEqualTo("userID", userID).get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                                            DocumentSnapshot userDocument = userTask.getResult().getDocuments().get(0);
                                            boolean isDepressed = userDocument.getBoolean("isDepressed");

                                            List<Float> depressionLabels = new ArrayList<>();
                                            for (int i = 0; i < typingSessions.size(); i++) {
                                                depressionLabels.add(isDepressed ? 1.0f : 0.0f);
                                            }
                                            Log.d("DEBUG", "FT Added labels");

                                            List<String> oppositeUserIDs = new ArrayList<>();
                                            Query oppositeUserQuery = db.collection("mobileUser").whereEqualTo("isDepressed", !isDepressed);
                                            oppositeUserQuery.get().addOnCompleteListener(oppUserTask -> {
                                                if (oppUserTask.isSuccessful()) {
                                                    for (QueryDocumentSnapshot oppUserDS : oppUserTask.getResult()) {
                                                        oppositeUserIDs.add(oppUserDS.getString("userID"));
                                                    }
                                                }
                                                Log.d("DEBUG", "FT Added opp userIDs" + String.valueOf(oppositeUserIDs));
                                                if (!oppositeUserIDs.isEmpty()) {
                                                    Query oppositeSessionsQuery = typingSessionRef.whereIn("userId", oppositeUserIDs)
                                                            .limit(typingSessions.size());
                                                    oppositeSessionsQuery.get().addOnCompleteListener(oppositeTask -> {
                                                        if (oppositeTask.isSuccessful()) {
                                                            for (QueryDocumentSnapshot documentSnapshot : oppositeTask.getResult()) {
                                                                typingSessions.add(documentSnapshot.toObject(DepressionDetectionApiService.typingSession.class));
                                                                depressionLabels.add(isDepressed ? 0.0f : 1.0f);
                                                                Log.d("DEBUG", "Adding opposite");
                                                            }
                                                            Log.d("DEBUG", "FT Added opposite labels");
                                                            Log.d("DEBUG", String.valueOf(depressionLabels));

                                                            apiService.fineTuneModel(userID, typingSessions, depressionLabels, new Callback<DepressionDetectionApiService.FineTuneResponse>() {
                                                                @Override
                                                                public void onResponse(Call<DepressionDetectionApiService.FineTuneResponse> call, Response<DepressionDetectionApiService.FineTuneResponse> response) {
                                                                    if (response.isSuccessful()) {
                                                                        Log.d("DEBUG", "FT Response successful");
                                                                        double baseAcc = response.body().getBase_accuracy();
                                                                        double fineTunedAcc = response.body().getFine_tuned_accuracy();
                                                                        double baseF1 = response.body().getBase_f1();
                                                                        double fineTunedF1 = response.body().getFine_tuned_f1();
                                                                        Log.d("DEBUG", "FT body values obtained");
                                                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                        WriteBatch batch = db.batch();
                                                                        for (DepressionDetectionApiService.typingSession session : userTypingSessions) {
                                                                            DocumentReference sessionRef = db.collection("typingSession").document(session.getDocId());
                                                                            batch.update(sessionRef, "trained", "True");
                                                                        }
                                                                        Log.d("DEBUG", "FT Updated training status for training data");
                                                                        db.collection("mobileUser").whereEqualTo("userID", userID).get()
                                                                                .addOnCompleteListener(uTask -> {
                                                                                    if (uTask.isSuccessful() && !uTask.getResult().isEmpty()) {
                                                                                        DocumentSnapshot userDocument = uTask.getResult().getDocuments().get(0);
                                                                                        DocumentReference userRef = userDocument.getReference();
                                                                                        batch.update(userRef, "base_f1", baseF1);
                                                                                        batch.update(userRef, "fine_tuned_f1", fineTunedF1);
                                                                                        batch.update(userRef, "base_accuracy", baseAcc);
                                                                                        batch.update(userRef, "fine_tuned_accuracy", fineTunedAcc);
                                                                                        batch.commit().addOnCompleteListener(commitTask -> {
                                                                                            if (commitTask.isSuccessful()) {
                                                                                                Log.d("DEBUG", "Successfully updated typing sessions and user F1 values.");
                                                                                            } else {
                                                                                                Log.e("DEBUG", "Failed to update typing sessions and user F1 values.", commitTask.getException());
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                        createFineTuneSuccessNotification(fineTunedAcc, getApplicationContext());
                                                                    } else {
                                                                        Log.e("DEBUG", "Fine-tune response unsuccessful.");
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<DepressionDetectionApiService.FineTuneResponse> call, Throwable t) {
                                                                    Log.e("DEBUG", "Fine-tune request failed.", t);
                                                                }
                                                            });
                                                        } else {
                                                            Log.e("DEBUG", "Unable to load opposite users");
                                                        }
                                                    });


                                                }
                                            });
                                        }
                                    });
                        }
                    });
                } else {
                    Log.e("DEBUG", "User not logged in.");
                }
            } else {
                Log.e("DEBUG", "Failed syncing, has not been 3 days");
            }
        }

        private void createFineTuneSuccessNotification(double fineTunedAcc, Context context) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String NOTIFICATION_CHANNEL_ID = "model_fine_tuning_channel";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "Fine-tuned Model Update Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription("Fine-tuned Model Update Alerts");
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.mindchecklogo)
                    .setContentTitle("Model Fine-Tuned")
                    .setContentText("Your model has been updated, now with an accuracy of " + String.valueOf(fineTunedAcc))
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Your model has been updated, now with an accuracy of " + fineTunedAcc))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(2, notificationBuilder.build());

            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notificationsHeader", "Model Fine-Tuned");
            notificationData.put("notificationsMessage", "Your model has been updated, now with an accuracy of " + String.valueOf(fineTunedAcc));
            notificationData.put("notificationsReadStatus", "unread");
            notificationData.put("notificationsTimestamp", new Timestamp(new Date()));
            notificationData.put("notificationsType", "Fine-tuned Model Update");
            notificationData.put("receiverID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            notificationData.put("senderID", "null");

            db.collection("notification").add(notificationData)
                    .addOnSuccessListener(documentReference -> Log.d("DEBUG", "FT Notification successfully written!"))
                    .addOnFailureListener(e -> Log.e("DEBUG", "FT Error writing document", e));
        }
    }

    public static class PredictionWorker extends Worker {
        private String PREF_FILE_NAME = "AppPreferences";
        private String LAST_PRED_TIME_KEY = "lastPredTime";
        public PredictionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }
        
        @NonNull
        @Override
        public Result doWork() {
            performPrediction();
            Log.d("DEBUG", "AUTO Prediction complete");
            return Result.success();
        }

        private void performPrediction() {
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
            long lastSyncTime = prefs.getLong(LAST_PRED_TIME_KEY, 0);
            long currentTime = new Date().getTime();
            long threeDaysMillis = TimeUnit.DAYS.toMillis(1);
            if ((currentTime - lastSyncTime) >= threeDaysMillis) {
                DepressionDetectionApiService apiService = new DepressionDetectionApiService();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userID = currentUser.getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR_OF_DAY, -24);
                    Date yesterday = calendar.getTime();

                    CollectionReference typingSessionRef = db.collection("typingSession");
                    Query query = typingSessionRef
                            .whereEqualTo("userId", userID)
                            .whereEqualTo("trained", "False")
                            .whereGreaterThan("timestamp", yesterday);
                    query.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("DEBUG", "AUTO Successfully fetched typing sessions");
                            List<DepressionDetectionApiService.typingSession> typingSessions = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                typingSessions.add(document.toObject(DepressionDetectionApiService.typingSession.class));
                            }
                            if (!typingSessions.isEmpty()) {
                                Log.d("DEBUG", "AUTO Typing sessions not empty");
                                db.collection("mobileUser").whereEqualTo("userID", userID).get()
                                        .addOnCompleteListener(userTask -> {
                                            if (userTask.isSuccessful() && !userTask.getResult().isEmpty() && userTask.getResult().getDocuments().get(0).contains("fine_tuned_f1") && userTask.getResult().getDocuments().get(0).contains("base_f1")) {
                                                DocumentSnapshot userDoc = userTask.getResult().getDocuments().get(0);
                                                double baseF1 = userDoc.getDouble("base_f1");
                                                double fineTunedF1 = userDoc.getDouble("fine_tuned_f1");

                                                String bestModel = (baseF1 > fineTunedF1) ? "base" : "fine_tuned";
                                                apiService.predictDepression(userID, typingSessions, bestModel, new Callback<DepressionDetectionApiService.PredictionResponse>() {
                                                    @Override
                                                    public void onResponse(Call<DepressionDetectionApiService.PredictionResponse> call, Response<DepressionDetectionApiService.PredictionResponse> response) {
                                                        if (response.isSuccessful()) {
                                                            Log.d("DEBUG", "AUTO Prediction API call successful");
                                                            boolean isDepressed = response.body().getPrediction();
                                                            Map<String, Object> userUpdates = new HashMap<>();
                                                            userUpdates.put("isDepressed", isDepressed);

                                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M", Locale.getDefault());
                                                            String currentDate = dateFormat.format(Calendar.getInstance().getTime());

                                                            db.collection("mobileUser").whereEqualTo("userID", userID).get()
                                                                    .addOnSuccessListener(querySnapshot -> {
                                                                        if (!querySnapshot.isEmpty()) {
                                                                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                                                            if (documentSnapshot.exists()) {
                                                                                DocumentReference docRef = documentSnapshot.getReference();
                                                                                docRef.get().addOnSuccessListener(documentSnapshot1 -> {
                                                                                    if (documentSnapshot1.exists() && documentSnapshot1.contains("depressionHistory")) {
                                                                                        Map<String, Object> updateAll = new HashMap<>();
                                                                                        updateAll.put("isDepressed", isDepressed);
                                                                                        Log.d("DEBUG", "AUTO added depression status");

                                                                                        Map<String, Boolean> depressionHistory = (Map<String, Boolean>) documentSnapshot1.get("depressionHistory");
                                                                                        if (depressionHistory == null) {
                                                                                            depressionHistory = new HashMap<>();
                                                                                        }
                                                                                        depressionHistory.put(currentDate, isDepressed);
                                                                                        updateAll.put("depressionHistory", depressionHistory);
                                                                                        Log.d("DEBUG", "AUTO added depression history");

                                                                                        docRef.update(updateAll)
                                                                                                .addOnSuccessListener(aVoid -> Log.d("DEBUG", "AUTO DocumentSnapshot successfully updated!"))
                                                                                                .addOnFailureListener(e -> Log.w("DEBUG", "AUTO Error updating document", e));
                                                                                    } else {
                                                                                        Log.e("DEBUG", "AUTO No matching document found");
                                                                                    }
                                                                                }).addOnFailureListener(eee -> Log.e("DEBUG", "AUTO Error retrieving document", eee));
                                                                            } else {
                                                                                Log.e("DEBUG", "AUTO No matching document found");
                                                                            }
                                                                        } else {
                                                                            Log.d("DEBUG", "No matching user found");
                                                                        }
                                                                    }).addOnFailureListener(e -> Log.e("DEBUG", "Error getting documents: ", e));

                                                            createDepressionNotification(isDepressed, getApplicationContext());
                                                            Log.d("DEBUG", "AUTO Created notifications");
                                                            showMessage(isDepressed);
                                                            Log.d("DEBUG", "AUTO Message shown");
                                                        } else {
                                                            Log.e("DEBUG", "Prediction response unsuccessful.");
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<DepressionDetectionApiService.PredictionResponse> call, Throwable t) {
                                                        Log.e("DEBUG", "Prediction request failed.", t);
                                                    }
                                                });

                                            } else {
                                                apiService.predictDepression(userID, typingSessions, "base", new Callback<DepressionDetectionApiService.PredictionResponse>() {
                                                    @Override
                                                    public void onResponse(Call<DepressionDetectionApiService.PredictionResponse> call, Response<DepressionDetectionApiService.PredictionResponse> response) {
                                                        if (response.isSuccessful()) {
                                                            boolean isDepressed = response.body().getPrediction();
                                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M", Locale.getDefault());
                                                            String currentDate = dateFormat.format(Calendar.getInstance().getTime());
                                                            db.collection("mobileUser").whereEqualTo("userID", userID).get()
                                                                    .addOnSuccessListener(querySnapshot -> {
                                                                        if (!querySnapshot.isEmpty()) {
                                                                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                                                            if (documentSnapshot.exists()) {
                                                                                DocumentReference docRef = documentSnapshot.getReference();
                                                                                docRef.get().addOnSuccessListener(documentSnapshot1 -> {
                                                                                    if (documentSnapshot1.exists() && documentSnapshot1.contains("depressionHistory")) {
                                                                                        Map<String, Object> updateAll = new HashMap<>();
                                                                                        updateAll.put("isDepressed", isDepressed);

                                                                                        Map<String, Boolean> depressionHistory = (Map<String, Boolean>) documentSnapshot1.get("depressionHistory");
                                                                                        if (depressionHistory == null) {
                                                                                            depressionHistory = new HashMap<>();
                                                                                        }
                                                                                        depressionHistory.put(currentDate, isDepressed);
                                                                                        updateAll.put("depressionHistory", depressionHistory);

                                                                                        docRef.update(updateAll)
                                                                                                .addOnSuccessListener(aVoid -> Log.d("DEBUG", "AUTO DocumentSnapshot successfully updated!"))
                                                                                                .addOnFailureListener(e -> Log.w("DEBUG", "AUTO Error updating document", e));
                                                                                    } else {
                                                                                        Log.e("DEBUG", "AUTO No matching document found");
                                                                                    }
                                                                                }).addOnFailureListener(e -> Log.e("DEBUG", "AUTO Error getting documents: ", e));



                                                                            } else {
                                                                                Log.d("DEBUG", "AUTO Document does not exist");
                                                                            }
                                                                        } else {
                                                                            Log.d("DEBUG", "No matching user found");
                                                                        }
                                                                    }).addOnFailureListener(e -> Log.e("DEBUG", "Error getting documents: ", e));

                                                            createDepressionNotification(isDepressed, getApplicationContext());
                                                            showMessage(isDepressed);
                                                        } else {
                                                            Log.e("DEBUG", "Prediction response unsuccessful.");
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<DepressionDetectionApiService.PredictionResponse> call, Throwable t) {
                                                        Log.e("DEBUG", "Prediction request failed.", t);
                                                    }
                                                });
                                            }
                                        });
                            } else {
                                Log.e("DEBUG", "No typing sessions found for prediction.");
                            }
                        } else {
                            Log.e("DEBUG", "Error getting typing sessions: ", task.getException());
                        }
                    });
                }
                Log.d("DEBUG", "AUTO Prediction done");
            } else {
                Log.e("DEBUG", "AUTO Syncing Failed, not been 1 day yet");
            }

        }

        private void showMessage(boolean isDepressed) {
            String message = "";
            if (isDepressed == true) {
                message = "The system predicts you are in high risk of depression today. Please complete the questionnaire to verify your depression occurrence.";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                getApplicationContext().startActivity(new Intent(getApplicationContext(), QuestionnaireActivity.class));
            } else {
                message = "Congrats, the system predicts you are in low risk of depression today!";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }

        private void createDepressionNotification(boolean isDepressed, Context context) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.e("DEBUG", "User not logged in");
                return;
            }
            String userID = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("mobileUser").whereEqualTo("userID", userID).get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            String therapistID = null;
                            if (documentSnapshot.contains("therapistID")) {
                                therapistID = documentSnapshot.getString("therapistID");
                            }
                            String userName = documentSnapshot.getString("userName");

                            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            String NOTIFICATION_CHANNEL_ID = "depression_detection_channel";
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                NotificationChannel channel = new NotificationChannel(
                                        NOTIFICATION_CHANNEL_ID,
                                        "Depression Detection Notifications",
                                        NotificationManager.IMPORTANCE_DEFAULT
                                );
                                channel.setDescription("Depression Detection Alerts");
                                notificationManager.createNotificationChannel(channel);
                            }

                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                                    .setSmallIcon(R.drawable.mindchecklogo)
                                    .setContentTitle("Depression Detection Result")
                                    .setContentText(isDepressed ? "Possible depression detected. Consider making an appointment with a therapist." : "Congrats, no signs of depression detected today!")
                                    .setAutoCancel(true)
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(isDepressed ? "Possible depression detected. Consider making an appointment with a therapist." : "Congrats, no signs of depression detected today!"))
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            notificationManager.notify(1, notificationBuilder.build());

                            Map<String, Object> notificationData = new HashMap<>();
                            Map<String, Object> notificationTherapist = new HashMap<>();
                            String message = "";
                            String therapistMessage = "";
                            if (isDepressed) {
                                message = "Possible depression detected. Consider making an appointment with a therapist.";
                                therapistMessage = userName + " is detected to have depression. Consider contacting the patient.";
                            } else {
                                message = "Congrats, no signs of depression detected today!";
                            }
                            notificationData.put("notificationsHeader", "Depression Detection Result");
                            notificationData.put("notificationsMessage", message);
                            notificationData.put("notificationsReadStatus", "unread");
                            notificationData.put("notificationsTimestamp", new Timestamp(new Date()));
                            notificationData.put("notificationsType", "Depression Detection");
                            notificationData.put("receiverID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            notificationData.put("senderID", "null");
                            if (isDepressed && therapistID != null) {
                                notificationTherapist.put("notificationsHeader", "Patient Depression Detected");
                                notificationTherapist.put("notificationsMessage", therapistMessage);
                                notificationTherapist.put("notificationsReadStatus", "unread");
                                notificationTherapist.put("notificationsTimestamp", new Timestamp(new Date()));
                                notificationTherapist.put("notificationsType", "Depression Detection");
                                notificationTherapist.put("receiverID", therapistID);
                                notificationTherapist.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());

                                DocumentReference docRef1 = db.collection("notification").document();
                                DocumentReference docRef2 = db.collection("notification").document();

                                WriteBatch batch = db.batch();
                                batch.set(docRef1, notificationData);
                                batch.set(docRef2, notificationTherapist);
                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("DEBUG", "Documents added successfully");
                                        } else {
                                            Log.e("DEBUG", "Error adding documents", task.getException());
                                        }
                                    }
                                });
                            } else {
                                db.collection("notification").add(notificationData)
                                        .addOnSuccessListener(documentReference -> Log.d("DEBUG", "Notification successfully written!"))
                                        .addOnFailureListener(e -> Log.e("DEBUG", "Error writing document", e));
                            }
                        }
                    });
        }
    }

    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private interface FirebaseCallback {
        void onCallback(boolean hasTherapist);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().hasExtra("loadAnotherFragment")) {
            TherapistProfileFragment profileFragment = new TherapistProfileFragment();
            Bundle args = new Bundle();
            args.putString("therapistID", getIntent().getStringExtra("therapistID"));
            profileFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_main);
        Log.d("DEBUG", "AREA A");

        schedulePeriodicWork();
        schedulePredictionWork();
        Log.d("DEBUG", "AREA B");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment()).commit();
        Log.d("DEBUG", "AREA C");

//        FirebaseMessaging.getInstance().subscribeToTopic("Reschedule")
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                String msg = "Done";
//                                if (!task.isSuccessful()) {
//                                    msg = "Failed";
//                                }
//                            }
//                        });


        createQuestionnaireNotificationChannel();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectorFragment = new HomeFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectorFragment = new ProfileFragment();
                } else if (itemId == R.id.nav_appointment) {
                    Log.e("DEBUG", "APPOINTMENT1");
                    userHasAssignedTherapist(new FirebaseCallback(){
                        @Override
                        public void onCallback(boolean hasTherapist){
                            if (hasTherapist) {
                                selectorFragment = new AppointmentsFragment();
                                Log.e("DEBUG", "APPOINTMENT2");
                            } else {
                                userHasSavedLocation(new FirebaseCallback() {
                                    @Override
                                    public void onCallback(boolean hasLocation) {
                                        if (!hasLocation) {
                                            Log.e("DEBUG", "APPOINTMENT3");
                                            navigateToSearchLocationActivity();
                                        } else {
                                            Log.e("DEBUG", "APPOINTMENT4");
                                            navigateToMapLocationActivity();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else if (itemId == R.id.nav_noti) {
                    selectorFragment = new NotificationsFragment();
                } else if (itemId == R.id.nav_graph) {
                    selectorFragment = new VisualizationFragment();
                }

                if (selectorFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectorFragment).commit();
                }

                return true;
            }
        });

        Log.d("DEBUG", "AREA D");

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (!task.isSuccessful()) {
//                            Log.d("DEBUG", "Fetching FCM registration token failed", task.getException());
//                            Toast.makeText(RealMainActivity.this, "Fetching FCM registration token failed", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//                        // Get new FCM registration token
//                        String token = task.getResult();
//
//                        // Log and toast
//                        //String msg = getString(R.string.msg_token_fmt, token);
//                        Log.d("DEBUGTOKEN", token);
//                    }
//                });

//        journalTextField = findViewById(R.id.journalTextField);
//        submit = findViewById(R.id.submitButton);
//        moreInfo = findViewById(R.id.moreInfo);
//        journalRecyclerView = findViewById(R.id.journalRecyclerView);
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = auth.getCurrentUser();
//
//        journalAdapter = new JournalAdapter(journalList);
//        journalRecyclerView.setAdapter(journalAdapter);
//        journalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        submit.setOnClickListener(v -> {
//            String journalText = journalTextField.getText().toString();
//            int wordCount = countWords(journalText);
//            int maxWordLimit = 500;
//
//            if (!journalText.isEmpty() && currentUser != null) {
//                if (wordCount > maxWordLimit) {
//                    Toast.makeText(RealMainActivity.this, "Word Limit Exceeded: Please write within 500 words", Toast.LENGTH_SHORT).show();
//                } else {
//                    Map<String, Object> journalEntry = new HashMap<>();
//                    journalEntry.put("text", journalText);
//                    journalEntry.put("timestamp", System.currentTimeMillis());
//                    journalEntry.put("userID", currentUser.getUid());
//
//                    db.collection("journal")
//                            .add(journalEntry)
//                            .addOnSuccessListener(documentReference -> {
//                                Toast.makeText(RealMainActivity.this, "Journal Saved", Toast.LENGTH_SHORT).show();
//                                journalTextField.setText("");
//                                fetchJournals();
//                            })
//                            .addOnFailureListener(e -> {
//                                Toast.makeText(RealMainActivity.this, "Error saving journal.", Toast.LENGTH_SHORT).show();
//                            });
//                }
//            } else {
//                Toast.makeText(RealMainActivity.this, "Cannot save journal to database", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        moreInfo.setOnClickListener(v -> {
//            startActivity(new Intent(RealMainActivity.this, MoreInfoActivity.class));
//        });
//
//        fetchJournals();
    }

    private void schedulePredictionWork() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        //PeriodicWorkRequest predictionRequest = new PeriodicWorkRequest.Builder(PredictionWorker.class, 1, TimeUnit.HOURS).setConstraints(constraints).build();
        PeriodicWorkRequest predictionRequest = new PeriodicWorkRequest.Builder(PredictionWorker.class, 24, TimeUnit.HOURS).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("prediction_work", ExistingPeriodicWorkPolicy.KEEP, predictionRequest);
        Log.d("DEBUG", "Auto prediction enqueued");
    }

    private void schedulePeriodicWork() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(DataSyncWorker.class, 3, TimeUnit.DAYS).build();
        //PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(DataSyncWorker.class, 3, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("finetuning_work", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
    }

    private void navigateToSearchLocationActivity() {
        Intent intent = new Intent(this, LocationActivity.class);
        startActivity(intent);
    }

    private void navigateToMapLocationActivity() {
        Log.d("DEBUG", "Entering activity");
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    private void userHasSavedLocation(final FirebaseCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();

            Query query = db.collection("mobileUser").whereEqualTo("userID", userID);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        boolean hasLocation = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                if (document.contains("longitude") && document.contains("latitude")){
                                    double longitude = document.getDouble("longitude");
                                    double latitude = document.getDouble("latitude");
                                    hasLocation = true;
                                    break;
                                }
                            }
                        }
                        callback.onCallback(hasLocation);
                    } else {
                        Log.e("DEBUG", "Query failed: ", task.getException());
                        callback.onCallback(false);
                    }
                }
            });
        } else {
            Log.e("DEBUG", "User not logged in, cannot perform operations");
        }
    }

    private void userHasAssignedTherapist(final FirebaseCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();

            Query query = db.collection("mobileUser").whereEqualTo("userID", userID);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        boolean hasTherapist = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                String therapist = document.getString("therapistID");
                                if (therapist != null && !therapist.isEmpty()) {
                                    hasTherapist = true;
                                    break;
                                }
                            }
                        }
                        callback.onCallback(hasTherapist);
                    } else {
                        Log.e("DEBUG", "Query failed: ", task.getException());
                        callback.onCallback(false);
                    }
                }
            });
        } else {
            Log.e("DEBUG", "User not logged in, cannot perform operations");
        }
        
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        Fragment homeFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
//        if (homeFragment != null) {
//            getSupportFragmentManager().beginTransaction().remove(homeFragment).commit();
//        }
//    }

    public void createQuestionnaireNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Questionnaire Time Reminder";
            String description = "Remind user to fill in questionnaire";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("NQ1", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

//    private void fetchJournals() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser != null) {
//            String currentUserID = currentUser.getUid();
//            db.collection("journal")
//                    .whereEqualTo("userID", currentUserID)
//                    .orderBy("timestamp", Query.Direction.DESCENDING)
//                    .limit(5)
//                    .get()
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        journalList.clear();
//                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                            String journalText = document.getString("text");
//                            journalList.add(journalText);
//                        }
//                        journalAdapter.notifyDataSetChanged();
//                    })
//                    .addOnFailureListener(e -> Log.d("DEBUG", "Error fetching journals" + e));
//        } else {
//            Log.d("DEBUG", "No user is logged in");
//            Toast.makeText(RealMainActivity.this, "No user is logged in", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private int countWords(String journalText) {
//        String trimmedText = journalText.trim();
//        if (trimmedText.isEmpty()) {
//            return 0;
//        }
//        return trimmedText.split("\\s+").length;
//    }
}