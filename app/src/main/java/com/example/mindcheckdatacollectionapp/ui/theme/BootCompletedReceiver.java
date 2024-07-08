package com.example.mindcheckdatacollectionapp.ui.theme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

//This class listens for device's boot completion event, and reshedules the reminders if device is restarted
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("QuestionnairePrefs", Context.MODE_PRIVATE);
            long submittedTime = sharedPreferences.getLong("submittedTime", 0);
            if (submittedTime != 0) {
                QuestionnaireActivity.scheduleReminder(context, submittedTime);
            }
        }
    }
}
