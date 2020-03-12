package app.meantime;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import io.realm.Realm;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_NOTIFICATION = "app.meantime.ACTION_NOTIFICATION";
    Context context;
    int notificationId = 1300;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Realm.init(context);
        Realm realm = Realm.getDefaultInstance();
        String id = intent.getStringExtra("id");
        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
        sendNotification(reminder);
        notificationId = (int)System.currentTimeMillis()/10000;
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
            context.startActivity(i);
        }
        else {
            Intent intent = new Intent(context, FullScreenReminderActivity.class);
            intent.putExtra("id", reminder.getReminderId());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            createNotificationChannel(importance);
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(R.drawable.ic_notifications_none_black_24dp);
            builder.setContentTitle("Reminder: \"" + reminder.getTitle() + "\"");
            builder.setContentText(reminder.getDescription())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(reminder.getDescription()))
                    .setPriority(reminder.getImportance()==0 ? NotificationCompat.PRIORITY_DEFAULT : NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setSound(soundUri)
                    .setVibrate(new long[]{100, 200, 300})
                    .setChannelId(Integer.toString(reminder.getImportance()+1))
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            notificationId++;
        }
    }

}
