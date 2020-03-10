package dot.albums;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class BackgroundWorker extends Worker {

    Context context;
    Realm realm;
    AlarmManager alarmManager;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    List<DataReminder> reminders = new ArrayList<>();
    long timeInMillis = 0;

    public BackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        realm = Realm.getDefaultInstance();
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "alerts";
            String description = "test notifications.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    void sendNotification(int id, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        createNotificationChannel();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_notifications_none_black_24dp);
        builder.setContentTitle("New Message:");
        builder.setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setSound(soundUri)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, builder.build());
    }

    @NonNull
    @Override
    public Result doWork() {
        sendNotification(1100, "Background Worker: 30 min");
        FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        fdb.getReference("messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    sendNotification(1200, (String) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        scheduleReminders();
        return Result.success();
    }

    private void scheduleReminders() {
        Calendar now = Calendar.getInstance();
        String today = calendarToString(now);
        now.add(Calendar.DATE, 1);
        String tomorrow = calendarToString(now);
        reminders.addAll(
                realm.where(DataReminder.class)
                        .equalTo("date", today)
                        .or()
                        .equalTo("date", tomorrow)
                        .and()
                        .equalTo("status", DataReminder.STATUS_CREATED)
                        .findAll()
        );

        // Don't scheduled deleted reminders
        for(int i = reminders.size(); i > 0; i--) {
            DataReminder reminder = reminders.get(i-1);
            if (reminder.isDeleted())
                reminders.remove(i-1);
        }

        for(DataReminder reminder: reminders){
            if(shouldSchedule(reminder)){
                Intent intent1 = new Intent(getApplicationContext(), NotificationReceiver.class);
                intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
                intent1.putExtra("id", reminder.getReminderId());
                int requestCode = (int)timeInMillis/10000;
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent1,
                        0);
                if (Build.VERSION.SDK_INT >= 19)
                    alarmManager.setExact(AlarmManager.RTC, timeInMillis, pendingIntent);
                else
                    alarmManager.set(AlarmManager.RTC, timeInMillis, pendingIntent);
                realm.beginTransaction();
                reminder.setStatus(DataReminder.STATUS_SCHEDULED);
                realm.commitTransaction();
            }
        }
    }

    public boolean shouldSchedule(DataReminder dataReminder){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");

        try {
            Date reminderDate = simpleDateFormat.parse(dataReminder.getDate() + " " + dataReminder.getTime());
            reminderDate = getDisplayTime(reminderDate, dataReminder.getAlarmtime());
            Date now = Calendar.getInstance().getTime();
            timeInMillis = reminderDate.getTime();
            long differenceInMillis = reminderDate.getTime() - now.getTime();
            long differenceInMinutes = TimeUnit.MINUTES.convert(differenceInMillis, TimeUnit.MILLISECONDS);

            return differenceInMinutes <= 30;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Date getDisplayTime(Date date, String displayTime){
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

    private String calendarToString(Calendar calendar){
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        return String.format("%02d", day) + " " + months[month] + " " + year;
    }
}
