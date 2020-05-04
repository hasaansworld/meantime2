package app.meantime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class SnoozeReceiver extends BroadcastReceiver {
    public static final String ACTION_SNOOZE = "app.meantime.ACTION_SNOOZE";
    Context context;
    SharedPreferences sharedPreferences;
    Realm realm;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        realm = Realm.getDefaultInstance();

        String reminderId = intent.getStringExtra("reminderId");
        String snoozeDuration = sharedPreferences.getString("snoozeDuration", "5 minutes");
        DataReminder dataReminder = realm.where(DataReminder.class).equalTo("reminderId", reminderId).findFirst();
        if(dataReminder != null){
            int reminderNumber = intent.getIntExtra("notificationId", 0);
            if(reminderNumber != -1) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(1300 + reminderNumber);
                int minutes = 5;
                if(snoozeDuration != null && snoozeDuration.equals("10 minutes"))
                    minutes = 10;
                else if(snoozeDuration != null && snoozeDuration.equals("15 minutes"))
                    minutes = 15;
                else if(snoozeDuration != null && snoozeDuration.equals("30 minutes"))
                    minutes = 30;
                else if(snoozeDuration != null && snoozeDuration.equals("1 hour"))
                    minutes = 60;
                long timeInMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes);
                snoozeReminder(reminderId, timeInMillis);
            }
            Toast.makeText(context, "Reminder snoozed for "+snoozeDuration+":\n"+dataReminder.getTitle(), Toast.LENGTH_SHORT).show();
        }

        realm.close();
    }


    public void snoozeReminder(String id, long timeInMillis){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent1 = new Intent(context.getApplicationContext(), NotificationReceiver.class);
        intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
        intent1.putExtra("id", id);

        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
        int requestCode = reminder.getReminderNumber();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent1,
                0);
        if (Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else if (Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

}
