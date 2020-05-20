package app.meantime;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    public static String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};


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
        PendingIntent pendingIntent;
        if(reminder.getImportance() == 2){
//            int minutes = (int)(TimeUnit.MINUTES.convert(timeInMillis-System.currentTimeMillis(), TimeUnit.MILLISECONDS));
//            if(minutes <= 15) {
                Intent i = new Intent(context, FullScreenReminderActivity.class);
                i.putExtra("id", reminder.getReminderId());
                i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                if (Build.VERSION.SDK_INT >= 21)
                    i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_NEW_TASK);
                else
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                int requestCode = reminder.getReminderNumber();
                pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), requestCode, i, 0);
//            }
//            else{
//                Intent intent1 = new Intent(context.getApplicationContext(), HighPriorityReceiver.class);
//                intent1.setAction(HighPriorityReceiver.ACTION_FULLSCREEN);
//                intent1.putExtra("id", reminder.getReminderId());
//
//                int requestCode = reminder.getReminderNumber();
//                pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent1,
//                        0);
//                timeInMillis = timeInMillis - TimeUnit.MINUTES.toMillis(5);
//            }
            if(Build.VERSION.SDK_INT < 21) {
                timeInMillis = Math.max(System.currentTimeMillis(), timeInMillis);
                timeInMillis = timeInMillis+1500;
            }
        }
        else {
            Intent intent1 = new Intent(context.getApplicationContext(), NotificationReceiver.class);
            intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
            intent1.putExtra("id", reminder.getReminderId());

            int requestCode = reminder.getReminderNumber();
            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent1,
                    0);
        }

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

    public static void repeatReminder(Context context, DataReminder reminder, Realm realm){
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        try {
            Date d = sdf.parse(reminder.getDate());
            Calendar now = Calendar.getInstance();
            now.setTime(d);
            if (reminder.getRepeat().equals("Repeat every day")) {
                now.add(Calendar.DATE, 1);
            } else if (reminder.getRepeat().equals("Repeat every week")) {
                now.add(Calendar.DATE, 7);
            } else if (reminder.getRepeat().equals("Repeat every weekday (Mon-Fri)")) {
                do
                    now.add(Calendar.DATE, 1);
                while(now.get(Calendar.DAY_OF_WEEK) < Calendar.MONDAY || now.get(Calendar.DAY_OF_WEEK) > Calendar.FRIDAY);
            } else if (reminder.getRepeat().equals("Repeat every month")) {
                now.add(Calendar.MONTH, 1);
            } else if (reminder.getRepeat().equals("Repeat every year")) {
                now.add(Calendar.YEAR, 1);
            }
            d = now.getTime();
            String newDate = sdf.format(d);
            String newDayOfWeek = days[now.get(Calendar.DAY_OF_WEEK)-1];

            //Number id = realm.where(DataReminder.class).max("reminderNumber");
            //int next_id = (id == null) ? 117 : id.intValue() + 1;
            DataReminder dataReminder = new DataReminder(
                    reminder.getReminderNumber(),
                    reminder.getReminderId(),
                    reminder.getTitle(),
                    newDayOfWeek,
                    newDate,
                    reminder.getTime(),
                    reminder.getAlarmtime(),
                    reminder.getImportance(),
                    reminder.getAlarmTone(),
                    reminder.getRepeat(),
                    "You"
            );
            dataReminder.setDescription(reminder.getDescription());
            dataReminder.setImage(reminder.getImage());
            realm.beginTransaction();
            realm.insertOrUpdate(dataReminder);
            realm.commitTransaction();
            SharedPreferences.Editor editor = context.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
            editor.putBoolean("updateMainList", true);
            editor.apply();
        }
        catch (ParseException e){

        }
    }


}
