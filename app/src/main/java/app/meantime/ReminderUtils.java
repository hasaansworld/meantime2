package app.meantime;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class ReminderUtils {
    public static long timeInMillis = 0;

    public static boolean shouldSchedule(DataReminder reminder){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);

        try {
            Date reminderDate = simpleDateFormat.parse(reminder.getDate() + " " + reminder.getTime());
            reminderDate = getDisplayTime(reminderDate, reminder.getAlarmtime());
            Date now = Calendar.getInstance().getTime();
            timeInMillis = reminderDate.getTime();
            long differenceInMillis = reminderDate.getTime() - now.getTime();
            long differenceInMinutes = TimeUnit.MINUTES.convert(differenceInMillis, TimeUnit.MILLISECONDS);

            return differenceInMinutes <= 180;
        } catch (ParseException e) {
            //sendNotification(959, "Parse Exception occurred!", e.toString());
            e.printStackTrace();
        }
        return false;
    }

    public static Date getDisplayTime(Date date, String displayTime){
        if(displayTime.equals("Exact time")){
            return date;
        }
        else if(displayTime.equals("5 minutes before")) {
            return new Date(date.getTime() - TimeUnit.MINUTES.toMillis(5));
        }
        else if(displayTime.equals("10 minutes before")) {
            return new Date(date.getTime() - TimeUnit.MINUTES.toMillis(10));
        }
        else if(displayTime.equals("15 minutes before")) {
            return new Date(date.getTime() - TimeUnit.MINUTES.toMillis(15));
        }
        else if(displayTime.equals("30 minutes before")) {
            return new Date(date.getTime() - TimeUnit.MINUTES.toMillis(30));
        }
        else if(displayTime.equals("1 hour before")) {
            return new Date(date.getTime() - TimeUnit.HOURS.toMillis(1));
        }
        return date;
    }

    public static void scheduleReminder(Context context, String id){
        Realm realm = Realm.getDefaultInstance();
        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
        if(reminder != null)
            scheduleReminder(context, reminder);
        realm.close();
    }

    public static void scheduleReminder(Context context, DataReminder reminder){
        Realm realm = Realm.getDefaultInstance();
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent1 = new Intent(context.getApplicationContext(), NotificationReceiver.class);
        intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
        intent1.putExtra("id", reminder.getReminderId());

        int requestCode = reminder.getReminderNumber();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent1,
                0);

        if (Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else if (Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);

        //sendNotification(1190, "Scheduled reminder with Id: "+reminder.getReminderId(), "Scheduled reminder with Id: "+reminder.getReminderId());

        realm.beginTransaction();
            reminder.setStatus(DataReminder.STATUS_SCHEDULED);
        realm.copyToRealmOrUpdate(reminder);
        realm.commitTransaction();

        if(context instanceof Activity)
            Toast.makeText(context, "Alarm scheduled!", Toast.LENGTH_SHORT).show();
        realm.close();
    }

    public static void schedule(Context context, DataReminder reminder){
        if(shouldSchedule(reminder)){
            scheduleReminder(context, reminder);
        }
    }

    public static void schedule(Context context, String id){
        Realm realm = Realm.getDefaultInstance();
        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
        if(reminder != null && shouldSchedule(reminder))
            scheduleReminder(context, reminder);
        realm.close();
    }

}
