package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private int year, month, day;
    private Calendar calendar;
    private EditText phoneNum;
    private EditText name;
    private EditText email;
    private EditText password;
    private Button datePicker;
    private Button register;
    private Spinner gender;
    private TextView loginUser;
    private TextView registerTherapist;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        phoneNum = findViewById(R.id.phoneNum);

        datePicker = findViewById(R.id.datePicker);
        calendar = Calendar.getInstance();

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
        mAuth = FirebaseAuth.getInstance();

        loginUser = findViewById(R.id.login_user);
        register = findViewById(R.id.register);
        registerTherapist = findViewById(R.id.registerTherapist);

        pd = new ProgressDialog(this);
        loginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        registerTherapist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, TherapistRegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String txt_phone_num = phoneNum.getText().toString();
                String txt_name = name.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();
                String txt_gender = gender.getSelectedItem().toString();
                String txt_dob = datePicker.getText().toString();

                if(TextUtils.isEmpty(txt_name) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_gender) || TextUtils.isEmpty(txt_phone_num) || TextUtils.isEmpty(txt_dob)){
                    Toast.makeText(RegisterActivity.this, "Empty Credentials!", Toast.LENGTH_SHORT).show();
                }else if(txt_password.length() < 8) {
                    Toast.makeText(RegisterActivity.this, "Password must be 8 characters or above", Toast.LENGTH_SHORT).show();
                }else{
                    registerUser(txt_phone_num, txt_name, txt_email, txt_password, txt_gender, txt_dob);
                }
            }
        });

    }

    private void showDate(int year, int month, int day) {
        datePicker.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private void registerUser(String phoneNum, String username, String email, String password, String gender, String dob) {
        pd.setMessage("Loading...");
        pd.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                HashMap<String,Object> map = new HashMap<>();
                map.put("userPhoneNum", phoneNum);
                map.put("userName", username);
                map.put("userEmail", email);
                map.put("userDOB", dob);
                map.put("userGender", gender);
                map.put("userID", mAuth.getCurrentUser().getUid());

                db.collection("mobileUser").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                       Toast.makeText(RegisterActivity.this, "Registration Successful with ID: " + documentReference, Toast.LENGTH_SHORT).show();
                       QuestionnaireUtil.checkPHQ9Activity(RegisterActivity.this);
                       finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}