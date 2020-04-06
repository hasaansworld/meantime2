package app.meantime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import io.realm.Realm;

public class FullScreenReminderActivity extends AppCompatActivity {
    Realm realm;
    DataReminder reminder;
    Toolbar toolbar;
    TextView title, description;
    TextView time, day, date, alarmTime, repeat;
    LinearLayout repeatLayout;
    ImageView image;
    View circle;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_reminder);

        realm = RealmUtils.getRealm();
        reminder = realm.where(DataReminder.class).equalTo("reminderId", getIntent().getStringExtra("id")).findFirst();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        image = findViewById(R.id.image);
        title.setText(reminder.getTitle());

        time = findViewById(R.id.time);
        day = findViewById(R.id.day);
        date = findViewById(R.id.date);
        circle = findViewById(R.id.circle);
        alarmTime = findViewById(R.id.text_alarm_time);
        repeat = findViewById(R.id.text_repeat);
        repeatLayout = findViewById(R.id.layout_repeat);

        day.setText(reminder.getDay());
        date.setText(reminder.getDate());
        time.setText(reminder.getTime());
        Drawable d = getResources().getDrawable(R.drawable.circle_white);
        String[] colors = {"#FFEE58", "#FF9700", "#F44336"};
        d.setColorFilter(Color.parseColor(colors[reminder.getImportance()]), PorterDuff.Mode.SRC_ATOP);
        circle.setBackground(d);
        alarmTime.setText(reminder.getAlarmtime());
        if(!reminder.getRepeat().equals("No repeat")) {
            repeatLayout.setVisibility(View.VISIBLE);
            repeat.setText(reminder.getRepeat());
        }

        description = findViewById(R.id.description);
        if(reminder.getDescription() != null && !reminder.getDescription().equals(""))
            description.setText(reminder.getDescription());

        String path = reminder.getImage();
        if(path != null && !path.equals("")){
            image.setVisibility(View.VISIBLE);
            Glide.with(this).asBitmap().load(path).into(image);
        }

        title.setText(title.getText());

        mediaPlayer = MediaPlayer.create(this, R.raw.quite_impressed);
        mediaPlayer.start();

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
        mediaPlayer.release();
    }
}
