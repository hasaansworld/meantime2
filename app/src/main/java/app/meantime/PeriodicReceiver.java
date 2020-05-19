package app.meantime;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class PeriodicReceiver extends BroadcastReceiver {
    public static final String ACTION_BACKGROUND_SCHEDULE = "app.meantime.ACTION_BACKGROUND_SCHEDULE";
    Context context;
    List<DataReminder> reminders = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        doScheduling();
        reschedule();
    }

    private void doScheduling(){
        Realm realm = Realm.getDefaultInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        Calendar now = Calendar.getInstance();
        String today = simpleDateFormat.format(now.getTime());
        now.add(Calendar.DATE, 1);
        String tomorrow = simpleDateFormat.format(now.getTime());
        reminders.addAll(
                realm.where(DataReminder.class)
                        .equalTo("date", today)
                        .or()
                        .equalTo("date", tomorrow)
                        .findAll()
        );

        // Don't schedule deleted reminders
        for(int i = reminders.size(); i > 0; i--) {
            DataReminder reminder = reminders.get(i-1);
            if (reminder == null || reminder.isDeleted() || reminder.getStatus() != DataReminder.STATUS_CREATED)
                reminders.remove(i-1);
        }

        log();

        for(DataReminder reminder: reminders){
            ReminderUtils.schedule(context, reminder);
        }

    }

    private void reschedule(){
        long timeInMillis = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2);
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("periodicUpdate", timeInMillis);
        editor.apply();
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent1 = new Intent(context.getApplicationContext(), PeriodicReceiver.class);
        intent1.setAction(PeriodicReceiver.ACTION_BACKGROUND_SCHEDULE);

        int requestCode = 12;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent1,
                0);

        if (Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else if (Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

    private void log(){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            String appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
            File dir = new File(Environment.getExternalStorageDirectory() + "/" + appName + "/Logs");
            File file = new File(Environment.getExternalStorageDirectory() + "/" + appName + "/Logs/" + "/log " + currentDateandTime + ".txt");
            if (!file.exists()) {
                try {
                    dir.mkdirs();
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
