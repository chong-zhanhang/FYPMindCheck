package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private int year, month, day;
    private Calendar calendar;
    private EditText phoneNum;
    private EditText name;
    private EditText email;
    private EditText newPassword;
    private Button datePicker;
    private Button save;
    private Spinner gender;
    private FirebaseFirestore db;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        name = findViewById(R.id.name);
        phoneNum = findViewById(R.id.phoneNum);
        email = findViewById(R.id.email);
        newPassword = findViewById(R.id.new_password);
        calendar = Calendar.getInstance();
        datePicker = findViewById(R.id.datePicker);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userID = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("mobileUser")
                    .whereEqualTo("userID", userID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                if (data != null && !data.isEmpty()) {
                                    if (data.containsKey("userName") && data.containsKey("userPhoneNum") && data.containsKey("userEmail")) {
                                        String originalName = data.get("userName").toString();
                                        String originalPhone = data.get("userPhoneNum").toString();
                                        String originalEmail = data.get("userEmail").toString();

                                        name.setHint("Name: " + originalName);
                                        phoneNum.setHint("Phone Number: " + originalPhone);
                                        email.setHint("Email: " + originalEmail);
                                    } else {
                                        Log.e("DEBUG", "User document does not contain important information");
                                        Toast.makeText(EditProfileActivity.this, "User document does not contain important information", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.e("DEBUG", "User information is null");
                                    Toast.makeText(EditProfileActivity.this, "User information is null", Toast.LENGTH_SHORT).show();
                                }
                            }
                    } else {
                            Log.e("DEBUG", "Unable to retrieve user information");
                            Toast.makeText(EditProfileActivity.this, "Unable to retrieve user information", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("DEBUG", "Non-existing user logged in! Unauthorized edit profile!");
            Toast.makeText(EditProfileActivity.this, "Unauthorized edit of profile!", Toast.LENGTH_SHORT).show();
        }
        gender = findViewById(R.id.gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        gender.setAdapter(adapter);

        datePicker.setOnClickListener( v -> {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            showDate(year, month+1, day);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, monthOfYear, dayofMonth) -> {
                        showDate(year, monthOfYear+1, dayofMonth);
                    }, year, month, day);
            datePickerDialog.show();

        });

        db = FirebaseFirestore.getInstance();
        save = findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameTxt = name.getText().toString().trim();
                String phoneNumTxt = phoneNum.getText().toString().trim();
                String genderTxt = gender.getSelectedItem().toString();
                String dobTxt = datePicker.getText().toString();
                String emailTxt = email.getText().toString();
                String newPasswordTxt = newPassword.getText().toString();

                Log.d("DEBUG", "Data read from fields: " + nameTxt + ", " + phoneNumTxt + ", " + genderTxt + ", " + dobTxt + ", " + emailTxt + ", " + newPasswordTxt);
                Map<String, Object> updates = new HashMap<>();
                if (!TextUtils.isEmpty(nameTxt)) {
                    updates.put("userName", nameTxt);
                }
                if (!TextUtils.isEmpty(phoneNumTxt)) {
                    updates.put("userPhoneNum", phoneNumTxt);
                }
                if (!TextUtils.isEmpty(genderTxt)) {
                    updates.put("userGender", genderTxt);
                }
                if (!TextUtils.isEmpty(dobTxt) && !dobTxt.equals("Select Date of Birth")) {
                    updates.put("userDOB", dobTxt);
                }
                if (!TextUtils.isEmpty(emailTxt)) {
                    updates.put("userEmail", emailTxt);
                }


                if (!updates.isEmpty()) {
                    Log.d("DEBUG", "Updates map is not empty, proceeding with update");
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        if (!TextUtils.isEmpty(emailTxt)) {
                            currentUser.verifyBeforeUpdateEmail(emailTxt)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(EditProfileActivity.this, "Email updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(EditProfileActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        if (!TextUtils.isEmpty(newPasswordTxt)) {
                            currentUser.updatePassword(newPasswordTxt)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(EditProfileActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(EditProfileActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        Log.d("DEBUG", "Starting Firebase update");
                        userID = currentUser.getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("mobileUser")
                                .whereEqualTo("userID", userID)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        document.getReference().update(updates)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(EditProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Unauthorized access", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "No changes made", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    private void showDate(int year, int month, int day) {
        datePicker.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }
}