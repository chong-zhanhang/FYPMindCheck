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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class TherapistRegisterActivity extends AppCompatActivity {
    private int year, month, day;
    private Calendar calendar;
    private EditText phoneNum;
    private EditText name;
    private EditText email;
    private EditText password;
    private EditText licenseNum;
    private EditText address;
    private EditText clinic;
    private EditText qualification;
    private Button datePicker;
    private Button register;
    private Spinner gender;
    private TextView loginTherapist;
    private TextView registerUser;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapist_register);

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

        licenseNum = findViewById(R.id.licenseNum);
        qualification = findViewById(R.id.qualification);
        clinic = findViewById(R.id.clinic);
        address = findViewById(R.id.address);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loginTherapist = findViewById(R.id.login_therapist);
        registerUser = findViewById(R.id.registerUser);
        register = findViewById(R.id.register_therapist);

        pd = new ProgressDialog(this);

        loginTherapist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TherapistRegisterActivity.this, TherapistLoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TherapistRegisterActivity.this, RegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
                String txt_qualification = qualification.getText().toString();
                String txt_licenseNum = licenseNum.getText().toString();
                String txt_clinic = clinic.getText().toString();
                String txt_address = address.getText().toString();

                if(TextUtils.isEmpty(txt_name) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_gender) || TextUtils.isEmpty(txt_phone_num) || TextUtils.isEmpty(txt_dob) || TextUtils.isEmpty(txt_licenseNum) || TextUtils.isEmpty(txt_qualification) || TextUtils.isEmpty(txt_clinic) || TextUtils.isEmpty(txt_address)){
                    Toast.makeText(TherapistRegisterActivity.this, "Empty Credentials!", Toast.LENGTH_SHORT).show();
                }else if(txt_password.length() < 8) {
                    Toast.makeText(TherapistRegisterActivity.this, "Password must be 8 characters or above", Toast.LENGTH_SHORT).show();
                }else{
                    registerTherapist(txt_phone_num, txt_name, txt_email, txt_password, txt_gender, txt_dob, txt_qualification, txt_licenseNum, txt_clinic, txt_address);
                }
            }
        });
    }

    private void registerTherapist(String txtPhoneNum, String txtName, String txtEmail, String txtPassword, String txtGender, String txtDob, String txtQualification, String txtLicenseNum, String txtClinic, String txtAddress) {
        pd.setMessage("Loading...");
        pd.show();

        mAuth.createUserWithEmailAndPassword(txtEmail, txtPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                HashMap<String,Object> map = new HashMap<>();
                map.put("therapistPhoneNum", txtPhoneNum);
                map.put("therapistName", txtName);
                map.put("therapistEmail", txtEmail);
                map.put("therapistDOB", txtDob);
                map.put("therapistGender", txtGender);
                map.put("therapistAddress", txtAddress);
                map.put("therapistClinic", txtClinic);
                map.put("therapistLicenseNum", txtLicenseNum);
                map.put("therapistQualification", txtQualification);
                map.put("therapistID", mAuth.getCurrentUser().getUid());

                db.collection("therapist").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(TherapistRegisterActivity.this, "Registration Successful with ID: " + documentReference, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(TherapistRegisterActivity.this, TherapistMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(TherapistRegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showDate(int year, int month, int day) {
        datePicker.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }


}