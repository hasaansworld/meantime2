package app.meantime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import io.realm.Realm;

public class FullScreenReminderActivity extends AppCompatActivity {
    Realm realm;
    DataReminder reminder;
    Toolbar toolbar;
    TextView title, description;
    TextView time, day, date, alarmTime;
    ImageView image;
    View circle;

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

        day.setText(reminder.getDay());
        date.setText(reminder.getDate());
        time.setText(reminder.getTime());
        Drawable d = getResources().getDrawable(R.drawable.circle_white);
        String[] colors = {"#FFEE58", "#FF9700", "#F44336"};
        d.setColorFilter(Color.parseColor(colors[reminder.getImportance()]), PorterDuff.Mode.SRC_ATOP);
        circle.setBackground(d);
        alarmTime.setText(reminder.getAlarmtime());

        description = findViewById(R.id.description);
        if(reminder.getDescription() != null && !reminder.getDescription().equals(""))
            description.setText(reminder.getDescription());

        String path = reminder.getImage();
        if(path != null && !path.equals("")){
            image.setVisibility(View.VISIBLE);
            Glide.with(this).asBitmap().load(path).into(image);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }
}