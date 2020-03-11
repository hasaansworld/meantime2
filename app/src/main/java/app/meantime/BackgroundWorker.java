package app.meantime;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class BackgroundWorker extends Worker {

    Context context;
    Realm realm;
    AlarmManager alarmManager;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    List<DataReminder> reminders = new ArrayList<>();
    long timeInMillis = 0;
    BufferedOutputStream buffStream;

    public BackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Low Importance Reminders";
            String description = "Get simple notifications about low importance reminders.";
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
        sendNotification(1100, "Background Worker: v2");
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
        realm = Realm.getDefaultInstance();
        Calendar now = Calendar.getInstance();
        String today = calendarToString(now);
        now.add(Calendar.DATE, 1);
        String tomorrow = calendarToString(now);
        reminders.addAll(
                realm.where(DataReminder.class)
                        .equalTo("date", today)
                        .or()
                        .equalTo("date", tomorrow)
                        .findAll()
        );

        // Don't scheduled deleted reminders
        for(int i = reminders.size(); i > 0; i--) {
            DataReminder reminder = reminders.get(i-1);
            if (reminder.isDeleted() || reminder.getStatus() != DataReminder.STATUS_CREATED)
                reminders.remove(i-1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd MMM yyyy", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
        File dir = new File(Environment.getExternalStorageDirectory()+"/"+appName+"/Logs");
        File file = new File(Environment.getExternalStorageDirectory()+"/"+appName+"/Logs/"+"/log "+currentDateandTime+".txt");
        if(!file.exists()) {
            try {
                dir.mkdirs();
                file.createNewFile();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        try {
            buffStream = new BufferedOutputStream(new FileOutputStream(file));
        }
        catch (IOException e){

        }
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(buffStream)));
        String log = "Items found matching criteria: " + reminders.size() + "\n";
        for(DataReminder reminder: reminders){
            if(shouldSchedule(reminder)){
                log = log + "\nShould schedule:\n"+reminder.getTitle()+"\n"+reminder.getDate()+" "+reminder.getTime()+"\n"+reminder.getStatus();
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
            else
                log = log + "\nDon't schedule:\n"+reminder.getTitle()+"\n"+reminder.getDate()+" "+reminder.getTime();
        }
        pw.append(log);
        pw.close();
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
