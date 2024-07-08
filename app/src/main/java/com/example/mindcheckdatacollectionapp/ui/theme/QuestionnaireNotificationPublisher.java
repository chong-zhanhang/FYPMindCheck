package com.example.mindcheckdatacollectionapp.ui.theme;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mindcheckdatacollectionapp.R;

//This class is responsible for receiving alarm broadcast and show notification
public class QuestionnaireNotificationPublisher extends BroadcastReceiver {
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 100; // Or any other unique integer

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "NQ1")//insert channel ID here
                .setSmallIcon(R.drawable.mindchecklogo)
                .setContentTitle("Test Your Depression Score")
                .setContentText("It's been 2 weeks since you last completed your PHQ-9 questionnaire, please login to complete the questionnaire.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notiManager = NotificationManagerCompat.from(context);

        int notificationID = (int) System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                //ActivityCompat.requestPermissions(this, new String[]{com.example.mindcheckdatacollectionapp.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
                Log.d("DEBUG", "AREA 1000");
            } else {
                notiManager.notify(notificationID, builder.build());
                Log.d("DEBUG", "AREA 2000");
            }
        } else {
            notiManager.notify(notificationID, builder.build());
            Log.d("DEBUG", "AREA 3000");
        }


    }
}
