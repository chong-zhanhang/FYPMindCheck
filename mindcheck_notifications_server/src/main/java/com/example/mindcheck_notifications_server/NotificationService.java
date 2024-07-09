package com.example.mindcheck_notifications_server;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
public class NotificationService {
    public void sendNotification(String topic, String title, String body) throws Exception {
        Message message = Message.builder()
                .putData("title", title)
                .putData("body", body)
                .setTopic(topic)
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Sucessfuly sent message: " + response);
    }
}
