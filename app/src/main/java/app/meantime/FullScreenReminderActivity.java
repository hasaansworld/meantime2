package app.meantime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class FullScreenReminderActivity extends AppCompatActivity {
    Realm realm;
    SharedPreferences sharedPreferences;
    DataReminder reminder;
    Toolbar toolbar;
    TextView title, description;
    TextView time, day, date, alarmTime, repeat;
    LinearLayout repeatLayout;
    ImageView image, alarmIcon;
    View circle;
    MediaPlayer mediaPlayer;

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
        reminder = realm.where(DataReminder.class).equalTo("reminderId", getIntent().getStringExtra("id")).findFirst();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        image = findViewById(R.id.image);

        time = findViewById(R.id.time);
        day = findViewById(R.id.day);
        date = findViewById(R.id.date);
        circle = findViewById(R.id.circle);
        alarmTime = findViewById(R.id.text_alarm_time);
        alarmIcon = findViewById(R.id.alarm_icon);
        repeat = findViewById(R.id.text_repeat);
        repeatLayout = findViewById(R.id.layout_repeat);

        if(reminder != null) {
            title.setText(reminder.getTitle());
            day.setText(reminder.getDay());
            date.setText(reminder.getDate());
            time.setText(reminder.getTime());
            Drawable d = getResources().getDrawable(R.drawable.circle_white);
            String[] colors = {"#FFEE58", "#FF9700", "#F44336"};
            d.setColorFilter(Color.parseColor(colors[reminder.getImportance()]), PorterDuff.Mode.SRC_ATOP);
            circle.setBackground(d);
            alarmTime.setText(reminder.getAlarmtime());
            if (!reminder.getRepeat().equals("No repeat")) {
                repeatLayout.setVisibility(View.VISIBLE);
                repeat.setText(reminder.getRepeat());
            }

            description = findViewById(R.id.description);
            if (reminder.getDescription() != null && !reminder.getDescription().equals(""))
                description.setText(reminder.getDescription());

            String path = reminder.getImage();
            if (path != null && !path.equals("")) {
                image.setVisibility(View.VISIBLE);
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
                        day.setText(days[now.get(Calendar.DAY_OF_WEEK)-1]);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else{
            title.setText("Reminder Not Found");
            date.setVisibility(View.GONE);
            day.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
            circle.setVisibility(View.GONE);
            description.setVisibility(View.GONE);
            alarmTime.setVisibility(View.GONE);
            alarmIcon.setVisibility(View.GONE);
            repeatLayout.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
        }

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
