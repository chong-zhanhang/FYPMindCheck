package com.example.mindcheckdatacollectionapp.ui.theme.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mindcheckdatacollectionapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    public List<Notification> notificationList;

    public static class Notification {
        private String notificationsID;
        private String senderID;
        private String receiverID;
        private String notificationsType;
        private String notificationsHeader;
        private String notificationsMessage;
        private String notificationsReadStatus;
        private Date notificationsTimestamp;

        public Notification() {

        }

        public Notification(String notificationsID, String senderID, String receiverID, String notificationsType, String notificationsHeader, String notificationsMessage, String notificationsReadStatus, Date notificationsTimestamp) {
            this.notificationsID = notificationsID;
            this.senderID = senderID;
            this.receiverID = receiverID;
            this.notificationsType = notificationsType;
            this.notificationsHeader = notificationsHeader;
            this.notificationsMessage = notificationsMessage;
            this.notificationsReadStatus = notificationsReadStatus;
            this.notificationsTimestamp = notificationsTimestamp;
        }

        public Date getNotificationsTimestamp() {
            return notificationsTimestamp;
        }

        public String getNotificationsHeader() {
            return notificationsHeader;
        }

        public String getNotificationsMessage() {
            return notificationsMessage;
        }

        public String getNotificationsReadStatus() {
            return notificationsReadStatus;
        }

        public String getNotificationsID() {
            return notificationsID;
        }

        public String getNotificationsType() {
            return notificationsType;
        }

        public String getReceiverID() {
            return receiverID;
        }

        public String getSenderID() {
            return senderID;
        }

        public void setNotificationsID(String notificationsID) {
            this.notificationsID = notificationsID;
        }

        public void setNotificationsHeader(String notificationsHeader) {
            this.notificationsHeader = notificationsHeader;
        }

        public void setNotificationsMessage(String notificationsMessage) {
            this.notificationsMessage = notificationsMessage;
        }

        public void setNotificationsReadStatus(String notificationsReadStatus) {
            this.notificationsReadStatus = notificationsReadStatus;
        }

        public void setNotificationsTimestamp(Date notificationsTimestamp) {
            this.notificationsTimestamp = notificationsTimestamp;
        }

        public void setNotificationsType(String notificationsType) {
            this.notificationsType = notificationsType;
        }

        public void setReceiverID(String receiverID) {
            this.receiverID = receiverID;
        }

        public void setSenderID(String senderID) {
            this.senderID = senderID;
        }
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
        private List<Notification> notifications;
        public NotificationAdapter(List<Notification> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d("DEBUG", "Creating view holder");
            try {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
                Log.d("DEBUG", "view holder declaring");
                return new NotificationViewHolder(view);
            } catch (Exception e) {
                Log.e("DEBUG", "Error creating view holder", e);
                throw e; // Re-throw the exception after logging it
            }
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            Notification notification = notifications.get(position);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.textViewTitle.setText(notification.getNotificationsHeader());
            holder.textViewMessage.setText(notification.getNotificationsMessage());
            holder.timestamp.setText(dateFormat.format(notification.getNotificationsTimestamp()));
            if ("unread".equals(notification.getNotificationsReadStatus())) {
                holder.itemView.setBackgroundColor(holder.itemView.getContext().getColor(R.color.light_blue));
            } else {
                holder.itemView.setBackgroundColor(holder.itemView.getContext().getColor(R.color.white));
            }
        }

        @Override
        public int getItemCount() {
            int count = notifications.size();
            Log.d("DEBUG", "ItemCount: " + count);
            return notifications.size();
        }

        public void setNotifications(List<Notification> notificationList) {
            this.notifications = notificationList;
            notifyDataSetChanged();
        }

        class NotificationViewHolder extends RecyclerView.ViewHolder {
            TextView textViewTitle, textViewMessage, timestamp;
            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                try {
                    textViewTitle = itemView.findViewById(R.id.text_view_title);
                    textViewMessage = itemView.findViewById(R.id.text_view_message);
                    timestamp = itemView.findViewById(R.id.text_view_timestamp);
                    Log.d("DEBUG", "View holder initialized successfully");
                } catch (Exception e) {
                    Log.e("DEBUG", "Error initializing view holder", e);
                }
            }
        }
    }

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
        loadNotifications();

        return view;
    }

    private void markNotificationsAsRead(List<Notification> notifications) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (Notification notification : notifications) {
            if ("unread".equals(notification.getNotificationsReadStatus())) {
                db.collection("notification")
                        .document(notification.getNotificationsID())
                        .update("notificationsReadStatus", "read")
                        .addOnSuccessListener(aVoid -> Log.d("DEBUG", "Notification marked as read: " + notification.getNotificationsID()))
                        .addOnFailureListener(e -> Log.e("DEBUG", "Error updating notification read status", e));
            }
        }
    }

    private void loadNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("notification").whereEqualTo("receiverID", userID)
                    .orderBy("notificationsTimestamp", Query.Direction.DESCENDING)
                    .limit(20)
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            notificationList.clear();
                            List<Notification> unreadNotifications = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Notification notification = document.toObject(Notification.class);
                                    notification.setNotificationsID(document.getId());
                                    notificationList.add(notification);

                                    unreadNotifications.add(notification);
                                }catch (Exception e) {
                                    Log.e("DEBUG", "Failed to parse notification", e);
                                }
                            }
                            adapter.setNotifications(notificationList);
                            adapter.notifyDataSetChanged();
                            if (!unreadNotifications.isEmpty()) {
                                markNotificationsAsRead(unreadNotifications);
                            }
                        } else {
                            Log.d("DEBUG", "Error getting documents: ", task.getException());
                        }
                    });
        } else {
            Log.e("DEBUG", "User not logged in");
        }
    }
}