package dot.albums;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import io.realm.Realm;

public class CreateActivity extends AppCompatActivity {
    CoordinatorLayout root;
    Toolbar toolbar;
    ImageView emojiTitle, emojiDescription;
    LinearLayout alarmTime;
    TextView textAlarmTime;
    LinearLayout lowImportance, mediumImportance, highImportance, importanceLayout;
    TextView textDate, textTime;
    EmojiEditText title, description;
    MaterialButton saveButton;
    int importance=1;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    int day, month, year, hour, minute;
    String dayOfWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        root = findViewById(R.id.root);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        emojiTitle = findViewById(R.id.emojiTitle);
        alarmTime = findViewById(R.id.layout_alarm_time);
        textAlarmTime = findViewById(R.id.text_alarm_time);
        lowImportance = findViewById(R.id.layout_low_importance);
        mediumImportance = findViewById(R.id.layout_medium_importance);
        highImportance = findViewById(R.id.layout_high_importance);
        importanceLayout = mediumImportance;
        textDate = findViewById(R.id.text_date);
        textTime = findViewById(R.id.text_time);
        saveButton = findViewById(R.id.saveButton);
        emojiDescription = findViewById(R.id.emojiDescription);
        initializeEmoji(emojiTitle, title);
        initializeEmoji(emojiDescription, description);

        Calendar now = Calendar.getInstance();
        day = now.get(Calendar.DAY_OF_MONTH);
        dayOfWeek = days[now.get(Calendar.DAY_OF_WEEK)-1];
        month = now.get(Calendar.MONTH);
        year = now.get(Calendar.YEAR);
        hour = 0;
        minute = 0;
        String dateS = String.format("%02d", day) + " " + months[month] + " " + year;
        textDate.setText(dateS);
        textDate.setOnClickListener(v -> {
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    (view, year, monthOfYear, dayOfMonth) -> {
                        this.year = year;
                        this.month = monthOfYear;
                        this.day = dayOfMonth;
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month, day);
                        dayOfWeek = days[cal.get(Calendar.DAY_OF_WEEK)-1];

                        String day = String.format("%02d", dayOfMonth);
                        String month = months[monthOfYear];
                        String date = day + " " + month + " " + year;
                        textDate.setText(date);
                    },
                    this.year, // Initial year selection
                    this.month, // Initial month selection
                    this.day // Inital day selection
            );
             dpd.show(getSupportFragmentManager(), "Datepickerdialog");
        });

        textTime.setOnClickListener(v -> {
            TimePickerDialog tpd = TimePickerDialog.newInstance((view, hourOfDay, minute, second) -> {
                this.hour = hourOfDay;
                this.minute = minute;
                String am = "AM";
                int hour = hourOfDay;
                if(hour >= 12) am = "PM";
                hour = hour%12;
                if(hour == 0) hour = 12;
                String time = String.format("%02d", hour)+":"+String.format("%02d", minute)+" "+am;
                textTime.setText(time);
            }, this.hour, this.minute, false);
            tpd.show(getSupportFragmentManager(), "Timepickerdialog");
        });

        alarmTime.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(CreateActivity.this, v);
            popup.setOnMenuItemClickListener(item -> {
                textAlarmTime.setText(item.getTitle());
                return true;
            });
            popup.inflate(R.menu.options_reminder_time);
            popup.show();
        });

        View.OnClickListener importanceListener = v -> {
            importanceLayout.setBackgroundResource(R.drawable.button_date);
            v.setBackgroundResource(R.drawable.button_importance_selected);
            if(v.equals(lowImportance))
                importance = 0;
            else if(v.equals(mediumImportance))
                importance = 1;
            else
                importance = 2;
            importanceLayout = (LinearLayout) v;
        };
        lowImportance.setOnClickListener(importanceListener);
        mediumImportance.setOnClickListener(importanceListener);
        highImportance.setOnClickListener(importanceListener);

        saveButton.setOnClickListener(v -> {
            DataReminder dataReminder = new DataReminder(
                    FirebaseDatabase.getInstance().getReference().push().getKey(),
                    title.getText().toString(),
                    dayOfWeek,
                    textDate.getText().toString(),
                    textTime.getText().toString(),
                    textAlarmTime.getText().toString(),
                    importance
            );
            Realm realm = RealmUtils.getRealm();
            realm.beginTransaction();
            realm.copyToRealm(dataReminder);
            realm.commitTransaction();

            Intent i = new Intent(CreateActivity.this, ReminderActivity.class);
            i.putExtra("id", dataReminder.getReminderId());
            startActivity(i);
            finish();

        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }

    public void initializeEmoji(final ImageView imageView, EmojiEditText emojiEditText){
        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(root).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                imageView.setImageResource(R.drawable.outline_sentiment_satisfied_black_24);
            }
        }).build(emojiEditText);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(emojiPopup.isShowing() ? R.drawable.outline_sentiment_satisfied_black_24 : R.drawable.ic_keyboard_black_24dp);
                emojiPopup.toggle();
            }
        });
    }
}
