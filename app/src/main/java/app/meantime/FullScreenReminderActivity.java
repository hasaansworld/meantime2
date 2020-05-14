package app.meantime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class FullScreenReminderActivity extends AppCompatActivity {
    Realm realm;
    SharedPreferences sharedPreferences;
    String reminderId;
    DataReminder reminder;
    Toolbar toolbar;
    TextView title, description;
    TextView time, date, alarmTime, repeat;
    LinearLayout snooze;
    LinearLayout repeatLayout, descriptionLayout, imageLayout, alarmLayout, dateTimeLayout;
    ImageView circle, image;
    MediaPlayer mediaPlayer;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_reminder);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            |WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            |WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            |WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new Handler().postDelayed(() -> getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON), 6000);

        realm = Realm.getDefaultInstance();
        reminderId = getIntent().getStringExtra("id");
        reminder = realm.where(DataReminder.class).equalTo("reminderId", reminderId).findFirst();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        image = findViewById(R.id.image);

        time = findViewById(R.id.time);
        date = findViewById(R.id.date);
        circle = findViewById(R.id.circle);
        alarmTime = findViewById(R.id.text_alarm_time);
        repeat = findViewById(R.id.text_repeat);
        dateTimeLayout = findViewById(R.id.layout_date_time);
        alarmLayout = findViewById(R.id.layout_alarm_time);
        repeatLayout = findViewById(R.id.layout_repeat);
        descriptionLayout = findViewById(R.id.layout_description);
        imageLayout = findViewById(R.id.layout_image);
        snooze = findViewById(R.id.snooze);

        if(reminder != null) {
            title.setText(reminder.getTitle());
            String dateAndDay = reminder.getDay().substring(0, 3)+", "+reminder.getDate();
            date.setText(dateAndDay);
            time.setText(reminder.getTime());
            int[] circles = {R.drawable.circle_yellow, R.drawable.circle_orange, R.drawable.circle_red};
            circle.setImageResource(circles[reminder.getImportance()]);
            alarmTime.setText(reminder.getAlarmtime());
            if (!reminder.getRepeat().equals("No repeat")) {
                repeatLayout.setVisibility(View.VISIBLE);
                repeat.setText(reminder.getRepeat());
            }

            if (reminder.getDescription() != null && !reminder.getDescription().equals("")) {
                description.setText(reminder.getDescription());
                descriptionLayout.setVisibility(View.VISIBLE);
            }

            String path = reminder.getImage();
            if (path != null && !path.equals("")) {
                imageLayout.setVisibility(View.VISIBLE);
                Glide.with(this).asBitmap().placeholder(R.drawable.broken_image).load(path).into(image);
            }

            if(reminder.getImportance() == 2) {
                int[] tones = {R.raw.you_have_new_message, R.raw.alarm_sound, R.raw.awesome_tune,
                        R.raw.business_tone, R.raw.cute_melody, R.raw.door_bell, R.raw.great_tone,
                        R.raw.office_phone, R.raw.positive_vibes, R.raw.relaxing, R.raw.ringtone_pro,
                        R.raw.romantic, R.raw.wake_up_sound, R.raw.white_noise};
                int tone = R.raw.you_have_new_message;
                if(reminder.getAlarmTone() >= 0 && reminder.getAlarmTone() < tones.length)
                    tone = tones[reminder.getAlarmTone()];
                mediaPlayer = MediaPlayer.create(this, tone);
                mediaPlayer.start();

                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if(vibrator != null && vibrator.hasVibrator()){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        long[] mVibratePattern = new long[]{0, 200, 400, 800, 0, 200, 400, 800};
                        // -1 : Play exactly once
                        VibrationEffect effect = VibrationEffect.createWaveform(mVibratePattern, -1);
                        vibrator.vibrate(effect);
                    }
                    else{
                        // -1 : Play exactly once
                        vibrator.vibrate(new long[]{0, 200, 400, 800, 0, 200, 400, 800}, -1);
                    }
                }
            }

            if(!reminder.getRepeat().equals("No repeat")){
                Calendar now = Calendar.getInstance();
                Date d1 = now.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                String today = sdf.format(d1);
                if(!reminder.getDate().equals(today)) {
                    try {
                        Date d2 = sdf.parse(reminder.getDate());
                        now.setTime(d2);
                        if (reminder.getRepeat().equals("Repeat every day")) {
                            now.add(Calendar.DATE, -1);
                        } else if (reminder.getRepeat().equals("Repeat every week")) {
                            now.add(Calendar.DATE, -7);
                        } else if (reminder.getRepeat().equals("Repeat every weekday (Mon-Fri)")) {
                            do
                                now.add(Calendar.DATE, -1);
                            while(now.get(Calendar.DAY_OF_WEEK) < Calendar.MONDAY || now.get(Calendar.DAY_OF_WEEK) > Calendar.FRIDAY);
                        } else if (reminder.getRepeat().equals("Repeat every month")) {
                            now.add(Calendar.MONTH, -1);
                        } else if (reminder.getRepeat().equals("Repeat every year")) {
                            now.add(Calendar.YEAR, -1);
                        }
                        d2 = now.getTime();
                        String dateS = sdf.format(d2);
                        date.setText(dateS);
                        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                        String dayS = days[now.get(Calendar.DAY_OF_WEEK)-1];
                        dateAndDay = dayS.substring(0, 3) + ", " + reminder.getDate();
                        date.setText(dateAndDay);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else{
            title.setText("Reminder Not Found");
            dateTimeLayout.setVisibility(View.GONE);
            circle.setVisibility(View.GONE);
            descriptionLayout.setVisibility(View.GONE);
            alarmLayout.setVisibility(View.GONE);
            repeatLayout.setVisibility(View.GONE);
            imageLayout.setVisibility(View.GONE);
            snooze.setVisibility(View.GONE);
        }

        snooze.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(FullScreenReminderActivity.this, snooze);
            popup.setOnMenuItemClickListener(item -> {
                int minutes = 10;
                String snoozeDuration = item.getTitle().toString();
                if(snoozeDuration.equals("5 minutes")){
                    minutes = 5;
                }
                else if(snoozeDuration.equals("10 minutes")){
                    minutes = 10;
                }
                else if(snoozeDuration.equals("15 minutes")){
                    minutes = 15;
                }
                else if(snoozeDuration.equals("30 minutes")){
                    minutes = 30;
                }
                else if(snoozeDuration.equals("1 hour")){
                    minutes = 60;
                }
                long timeInMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes);
                snoozeReminder(reminderId, timeInMillis);
                new Handler().postDelayed(() -> {
                    Toast.makeText(FullScreenReminderActivity.this, "Reminder snoozed for "+snoozeDuration+":\n"+reminder.getTitle(), Toast.LENGTH_SHORT).show();
                    finish();
                }, 500);
                return true;
            });
            popup.inflate(R.menu.options_snooze_time);
            popup.show();
        });


        if(!sharedPreferences.getBoolean("noAds", false))
            showAd();

    }


    public void snoozeReminder(String id, long timeInMillis){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent1 = new Intent(getApplicationContext(), NotificationReceiver.class);
        intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
        intent1.putExtra("id", id);

        int requestCode = reminder.getReminderNumber();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent1,
                0);
        if (Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else if (Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

    private void showAd(){
        new Handler().postDelayed(() -> {
            MobileAds.initialize(FullScreenReminderActivity.this, initializationStatus -> {
            });
            RequestConfiguration requestConfiguration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("F68088F697A5D97E60C69783F1EBD9A4")).build();
            MobileAds.setRequestConfiguration(requestConfiguration);

            adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
//            adView.setAdListener(new AdListener(){
//                @Override
//                public void onAdLoaded() {
//                    Toast.makeText(ReminderActivity.this, "Ad loaded!", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onAdFailedToLoad(int i) {
//                    String error = "Unknown error";
//                    switch (i){
//                        case AdRequest.ERROR_CODE_INTERNAL_ERROR:
//                            error = "Internal error";
//                            break;
//                        case AdRequest.ERROR_CODE_INVALID_REQUEST:
//                            error = "Invalid request";
//                            break;
//                        case AdRequest.ERROR_CODE_NETWORK_ERROR:
//                            error = "Netwrok error";
//                            break;
//                        case AdRequest.ERROR_CODE_NO_FILL:
//                            error = "No fill";
//                            break;
//                    }
//                    Toast.makeText(ReminderActivity.this, "Ads Failed: "+error, Toast.LENGTH_SHORT).show();
//                }
//            });
        }, 1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null) mediaPlayer.release();
        realm.close();
    }
}
