package com.example.mindcheckdatacollectionapp.ui.theme;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;

public class QuestionnaireUtil {
    public static void checkPHQ9Activity(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("questionnaire")
                    .whereEqualTo("UserID", userID)
                    .orderBy("Timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (!task.getResult().isEmpty()){
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                Date lastSubmitted = documentSnapshot.getDate("Timestamp");

                                if (lastSubmitted != null && weeksBetween(lastSubmitted, new Date()) >= 2) {
                                    Log.d("DEBUG", "Query snapshot obtained, showing questionnaire");
                                    context.startActivity(new Intent(context, QuestionnaireActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                } else{
                                    //Fallback or proceed with normal activity if not found or error
                                    Log.d("DEBUG", "Query snapshot obtained, has not been two weeks");
                                    context.startActivity(new Intent(context, RealMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                }
                            } else {
                                // First time taking questionnaire
                                Log.d("DEBUG", "First time taking questionnaire");
                                context.startActivity(new Intent(context, QuestionnaireActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }
                        } else {
                            Log.d("DEBUG", "Task of getting result failed");
                            // Log the specific exception message and stack trace
                            Log.e("QuestionnaireUtil", "Exception details:" + task.getException());
                        }
                    }).addOnFailureListener(e -> {
                        Log.d("DEBUG", "Failed Listener", e);
                    });
        }
    }

    private static long weeksBetween(Date start, Date end) {
        long difference = Math.abs(end.getTime() - start.getTime());
        return difference / (7 * 24 * 60 * 60 * 1000);
    }
}
