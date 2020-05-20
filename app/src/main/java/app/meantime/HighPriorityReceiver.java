package app.meantime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import io.realm.Realm;

public class HighPriorityReceiver extends BroadcastReceiver {
    public static final String ACTION_FULLSCREEN = "app.meantime.ACTION_FULLSCREEN";
    SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("isDisabled", false)) {
            Realm.init(context);
            Realm realm = Realm.getDefaultInstance();
            String id = intent.getStringExtra("id");
            DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
            if (reminder != null) {
                ReminderUtils.schedule(context, reminder);
            }
            realm.close();
        }
    }
}
