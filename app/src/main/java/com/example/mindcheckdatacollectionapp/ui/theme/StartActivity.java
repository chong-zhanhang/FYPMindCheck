package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class StartActivity extends AppCompatActivity {
    private ImageView logo;
    private LinearLayout linearLayout;
    private Button register;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        logo = findViewById(R.id.logo);
        linearLayout = findViewById(R.id.linear_layout);
        register = findViewById(R.id.register);
        login = findViewById(R.id.login);

        linearLayout.animate().alpha(0f).setDuration(1);

        TranslateAnimation animation = new TranslateAnimation(0,0,0,-1000);
        animation.setDuration(1000);
        animation.setFillAfter(false);
        animation.setAnimationListener(new MyAnimationListener());
        logo.setAnimation(animation);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, RegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

    }

    private class MyAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            logo.clearAnimation();
            logo.setVisibility(View.INVISIBLE);
            linearLayout.animate().alpha(1f).setDuration(1000);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            getUserType(FirebaseAuth.getInstance().getCurrentUser()).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    String userType = task.getResult();
                    if ("therapist".equals(userType)){
                        startActivity(new Intent(StartActivity.this, TherapistMainActivity.class));
                    } else {
                        QuestionnaireUtil.checkPHQ9Activity(StartActivity.this);
                    }
                    finish();
                } else {
                    Log.e("DEBUG", "Failed to find out user type");
                }
            });
        }
    }

    public static Task<String> getUserType(FirebaseUser currentUser) {
        String userID = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("therapist").whereEqualTo("therapistID", userID).limit(1);
        return query.get().continueWith(task -> {
            if (task.isSuccessful()){
                for (DocumentSnapshot document : task.getResult()){
                    return "therapist";
                }
            }
            return "normal";
        });
    }
}