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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TherapistAppointmentUpcomingFragment extends Fragment {
    private RecyclerView recyclerView;
    private AppointmentAdapter appointmentAdapter;
    private List<AppointmentsUpcomingFragment.Appointment> appointments;
    public TherapistAppointmentUpcomingFragment() {
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
       public AppointmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           View view = LayoutInflater.from(context).inflate(R.layout.item_therapist_upcoming_appointment, parent, false);
           return new AppointmentAdapter.ViewHolder(view);
       }
       @Override
       public void onBindViewHolder(@NonNull AppointmentAdapter.ViewHolder holder, int position) {
           AppointmentsUpcomingFragment.Appointment appointment = appointments.get(position);
           holder.bind(appointment, position);
       }
       @Override
       public int getItemCount() {
           return appointments.size();
       }

       private class ViewHolder extends RecyclerView.ViewHolder {
           private TextView userName, userPhoneNum, appointmentStatus, appointmentDate, appointmentTime;
           private ImageButton userProfile, callPatient;
           private Button cancel;

           public ViewHolder(@NonNull View itemView) {
               super(itemView);
               userProfile = itemView.findViewById(R.id.userProfile);
               callPatient = itemView.findViewById(R.id.callPatient);
               cancel = itemView.findViewById(R.id.cancelAppointmentCU);
               userName = itemView.findViewById(R.id.userName);
               userPhoneNum = itemView.findViewById(R.id.userPhoneNum);
               appointmentStatus = itemView.findViewById(R.id.appointmentStatus);
               appointmentDate = itemView.findViewById(R.id.appointmentDate);
               appointmentTime = itemView.findViewById(R.id.appointmentTime);
           }
           public void bind(AppointmentsUpcomingFragment.Appointment appointment, int position) {
               userProfile.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       FirebaseFirestore db = FirebaseFirestore.getInstance();
                       Query query = db.collection("mobileUser").whereEqualTo("userID", appointment.userID);
                       query.get().addOnCompleteListener(task -> {
                           if (task.isSuccessful() && !task.getResult().isEmpty()) {
                               DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                               Boolean currentUnsharedState = documentSnapshot.getBoolean("unshared");
                               if (currentUnsharedState == Boolean.TRUE) {
                                   Toast.makeText(getContext(), "Patient did not share data with you.", Toast.LENGTH_SHORT).show();
                               } else {
                                   ProfileFragment profileFragment = new ProfileFragment();
                                   Bundle args = new Bundle();
                                   args.putString("userID", appointment.userID);
                                   profileFragment.setArguments(args);
                                   getParentFragment().getParentFragmentManager().beginTransaction()
                                           .replace(R.id.fragment_container, profileFragment)
                                           .addToBackStack(null)
                                           .commit();
                               }
                           }
                       });

                   }
               });
               callPatient.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       appointment.getPatientPhoneNum(new AppointmentsUpcomingFragment.Appointment.PatientDetailsCallback() {
                           @Override
                           public void onCallback(String patientDetails) {
                               String phoneNum = patientDetails.toString();
                               Intent intent = new Intent(Intent.ACTION_VIEW);
                               intent.setData(Uri.parse("tel:" + phoneNum));
                               v.getContext().startActivity(intent);
                           }
                       });
                   }
               });
               cancel.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       cancelAppointment(appointment, position);
                   }
               });
               appointmentDate.setText(appointment.getDate());
               appointmentTime.setText(appointment.getTime());
               appointmentStatus.setText("Status: " + appointment.getStatus().toUpperCase());
               appointment.getPatientName(new AppointmentsUpcomingFragment.Appointment.PatientDetailsCallback() {
                   @Override
                   public void onCallback(String patientDetails) {
                       userName.setText(patientDetails);
                   }
               });
               appointment.getPatientPhoneNum(new AppointmentsUpcomingFragment.Appointment.PatientDetailsCallback() {
                   @Override
                   public void onCallback(String patientDetails) {
                       userPhoneNum.setText(patientDetails);
                   }
               });
           }

           private void cancelAppointment(AppointmentsUpcomingFragment.Appointment appointment, int position) {
               FirebaseFirestore db = FirebaseFirestore.getInstance();
               db.collection("appointment")
                       .document(appointment.getDocumentID())
                       .update("appointmentStatus", "cancelled")
                       .addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void unused) {
                               Toast.makeText(itemView.getContext(), "Appointment Cancelled", Toast.LENGTH_SHORT).show();
                               appointments.remove(position);
                               appointmentAdapter.notifyItemRemoved(position);
                               createCancelNotification(appointment);
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Toast.makeText(itemView.getContext(), "Error Cancelling Appointment", Toast.LENGTH_SHORT).show();
                           }
                       });
           }

           private void createCancelNotification(AppointmentsUpcomingFragment.Appointment appointment) {
               FirebaseFirestore db = FirebaseFirestore.getInstance();
               Map<String, Object> notificationData = new HashMap<>();
               notificationData.put("notificationsHeader", "Appointment Cancelled");
               notificationData.put("notificationsMessage", "Your appointment on " + appointment.getDate() + " at " + appointment.getTime() + " has been cancelled.");
               notificationData.put("notificationsReadStatus", "unread");
               notificationData.put("notificationsTimestamp", new Timestamp(new Date()));
               notificationData.put("notificationsType", "Appointment Cancelled");
               notificationData.put("receiverID", appointment.userID);
               notificationData.put("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
               db.collection("notification").add(notificationData)
                       .addOnSuccessListener(documentReference -> Log.d("DEBUG", "Notification successfully written!"))
                       .addOnFailureListener(e -> Log.e("DEBUG", "Error writing document", e));
           }
       }
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_therapist_appointment_upcoming, container, false);

        recyclerView = view.findViewById(R.id.upcomingAppointmentsListT);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        appointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(getContext(), appointments);
        recyclerView.setAdapter(appointmentAdapter);

        fetchUpcomingAppointments();

        return view;
    }

    private void fetchUpcomingAppointments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentTherapist = FirebaseAuth.getInstance().getCurrentUser();
        if (currentTherapist != null) {
            String therapistID = currentTherapist.getUid();
            db.collection("appointment")
                    .whereEqualTo("therapistID", therapistID)
                    .whereEqualTo("appointmentStatus", "confirmed")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                appointments.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    AppointmentsUpcomingFragment.Appointment appointment = document.toObject(AppointmentsUpcomingFragment.Appointment.class);
                                    appointment.setDocumentID(document.getId());
                                    appointments.add(appointment);
                                }
                                Log.d("DEBUG", String.valueOf(appointments.size()));
                                appointmentAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("DEBUG", "Error fetching appointments: ", task.getException());
                            }
                        }
                    });
        } else {
            Log.e("DEBUG", "Therapist not signed in");
        }
    }
}