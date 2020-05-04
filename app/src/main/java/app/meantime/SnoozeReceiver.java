package app.meantime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import io.realm.Realm;

public class SnoozeReceiver extends BroadcastReceiver {
    public static final String ACTION_SNOOZE = "app.meantime.ACTION_SNOOZE";
    SharedPreferences sharedPreferences;
    Realm realm;

    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        realm = Realm.getDefaultInstance();

        DataReminder dataReminder = realm.where(DataReminder.class).equalTo("reminderId", intent.getStringExtra("reminderId")).findFirst();
        if(dataReminder != null){
            int reminderNumber = intent.getIntExtra("notificationId", 0);
            if(reminderNumber != -1) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(1300 + reminderNumber);
            }
            Toast.makeText(context, "Reminder snoozed for "+sharedPreferences.getString("snoozeDuration", "5 minutes")+":\n"+dataReminder.getTitle(), Toast.LENGTH_SHORT).show();
        }

        realm.close();
    }
}
