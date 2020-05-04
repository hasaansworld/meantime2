package app.meantime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import io.realm.Realm;

public class SnoozeReceiver extends BroadcastReceiver {
    public static final String ACTION_SNOOZE = "app.meantime.ACTION_SNOOZE";
    Realm realm;

    @Override
    public void onReceive(Context context, Intent intent) {
        realm = Realm.getDefaultInstance();
        DataReminder dataReminder = realm.where(DataReminder.class).equalTo("reminderId", intent.getStringExtra("reminderId")).findFirst();
        if(dataReminder != null){
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            int reminderNumber = intent.getIntExtra("notificationId", 0);
            notificationManager.cancel(1300+reminderNumber);
        }

        realm.close();
    }
}
