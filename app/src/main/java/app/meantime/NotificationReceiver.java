package app.meantime;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import io.realm.Realm;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_NOTIFICATION = "app.meantime.ACTION_NOTIFICATION";
    Context context;
    int notificationId = 1300;
    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Realm.init(context);
        Realm realm = Realm.getDefaultInstance();
        String id = intent.getStringExtra("id");
        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
        notificationId = reminder.getReminderNumber();
        sendNotification(reminder);
        realm.beginTransaction();
        reminder.setStatus(DataReminder.STATUS_COMPLETED);
        realm.commitTransaction();
        if(!reminder.getRepeat().equals("No repeat"))
            repeatReminder(reminder, realm);
        realm.close();
    }


    private void createNotificationChannel(int imp) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Low Importance Reminders";
            String description = "Get simple notifications about low importance reminders.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            if(imp == 1) {
                name = "Mid Importance Reminders";
                description = "Get your reminders in heads up notifications.";
                importance = NotificationManager.IMPORTANCE_HIGH;
            }
            NotificationChannel channel = new NotificationChannel(Integer.toString(imp+1), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    void sendNotification(DataReminder reminder){
        int importance = reminder.getImportance();
        if(importance == 2){
            Intent i = new Intent(context, FullScreenReminderActivity.class);
            i.putExtra("id", reminder.getReminderId());
            i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            if(Build.VERSION.SDK_INT >= 21)
                i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            context.startActivity(i);
        }
        else {
            Intent intent = new Intent(context, FullScreenReminderActivity.class);
            intent.putExtra("id", reminder.getReminderId());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            createNotificationChannel(importance);
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //Uri path = Uri.parse("android.resource://"+context.getPackageName()+"/raw/quite_impressed");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(R.drawable.ic_notifications_none_black_24dp);
            builder.setContentTitle("Reminder: \"" + reminder.getTitle() + "\"");
            builder.setContentText(reminder.getDescription())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(reminder.getDescription()))
                    .setPriority(reminder.getImportance()==0 ? NotificationCompat.PRIORITY_DEFAULT : NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300})
                    .setChannelId(Integer.toString(reminder.getImportance()+1))
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            //playSound();
            notificationId++;
        }
    }

    private void repeatReminder(DataReminder reminder, Realm realm){
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
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

            Number id = realm.where(DataReminder.class).max("reminderNumber");
            int next_id = (id == null) ? 117 : id.intValue() + 1;
            DataReminder dataReminder = new DataReminder(
                    next_id,
                    FirebaseDatabase.getInstance().getReference().push().getKey(),
                    reminder.getTitle(),
                    newDayOfWeek,
                    newDate,
                    reminder.getTime(),
                    reminder.getAlarmtime(),
                    reminder.getImportance(),
                    reminder.getRepeat(),
                    "You"
            );
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

    /*private void playSound(){
        MediaPlayer mMediaPlayer = MediaPlayer.create(context, R.raw.quite_impressed);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                mMediaPlayer.start();
                }
            });
        mMediaPlayer.setOnCompletionListener(mp -> mMediaPlayer.release());
    }*/
}
