package app.meantime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class WakeReceiver extends BroadcastReceiver {
    AlarmManager alarmManager;
    @Override
    public void onReceive(Context context, Intent intent) {
        Realm realm = Realm.getDefaultInstance();
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        List<DataReminder> scheduledReminders = new ArrayList<>();
        scheduledReminders.addAll(realm.where(DataReminder.class).equalTo("status", DataReminder.STATUS_SCHEDULED).findAll());

        for(int i = scheduledReminders.size(); i > 0; i--) {
            DataReminder reminder = scheduledReminders.get(i-1);
            if (reminder == null || reminder.isDeleted())
                scheduledReminders.remove(i-1);
        }

        for(DataReminder reminder: scheduledReminders)
            ReminderUtils.schedule(context, reminder);

        Intent intent2 = new Intent(context, PeriodicReceiver.class);
        intent.setAction(PeriodicReceiver.ACTION_BACKGROUND_SCHEDULE);
        context.sendBroadcast(intent2);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, ForegroundService.class));
        } else {
            context.startService(new Intent(context, BackgroundService.class));
        }*/
    }

    private void scheduleReminder(Context context, DataReminder reminder){
        Intent intent1 = new Intent(context.getApplicationContext(), NotificationReceiver.class);
        intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
        intent1.putExtra("id", reminder.getReminderId());
        int requestCode = reminder.getReminderNumber();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent1,
                0);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            Date date = sdf.parse(reminder.getDate());
            long timeInMillis = date.getTime();
            if (Build.VERSION.SDK_INT >= 19)
                alarmManager.setExact(AlarmManager.RTC, timeInMillis, pendingIntent);
            else
                alarmManager.set(AlarmManager.RTC, timeInMillis, pendingIntent);
        } catch (ParseException e){

        }
    }
}
