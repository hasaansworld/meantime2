package app.meantime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class CreateActivity extends AppCompatActivity {
    CoordinatorLayout root;
    Toolbar toolbar;
    TextView toolbarTitle;
    ImageView emojiTitle, emojiDescription;
    LinearLayout alarmTime;
    TextView textAlarmTime;
    LinearLayout lowImportance, mediumImportance, highImportance, importanceLayout;
    TextView textDate, textTime, textError;
    EmojiEditText title, description;
    MaterialButton saveButton;
    int importance=1;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    int day, month, year, hour = 0, minute = 0;
    String dayOfWeek;
    long timeInMillis = -1;
    Realm realm;
    boolean isEditing = false;
    String reminderId;
    DataReminder oldReminder;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        realm = RealmUtils.getRealm();
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle = findViewById(R.id.toolbarTitle);

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
        textError = findViewById(R.id.textError);
        saveButton = findViewById(R.id.saveButton);
        emojiDescription = findViewById(R.id.emojiDescription);
        initializeEmoji(emojiTitle, title);
        initializeEmoji(emojiDescription, description);


        String dateS = getCurrentDate();
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
            textError.setVisibility(View.GONE);
            if(title.getText().toString().equals("")){
                textError.setVisibility(View.VISIBLE);
                textError.setText( "Title can't be empty!");
            }
            else {
                Number id = realm.where(DataReminder.class).max("reminderNumber");
                int next_id = (id == null) ? 117 : id.intValue() + 1;
                DataReminder dataReminder = new DataReminder(
                    isEditing ? oldReminder.getReminderNumber():next_id,
                    isEditing ? oldReminder.getReminderId():FirebaseDatabase.getInstance().getReference().push().getKey(),
                    title.getText().toString(),
                    dayOfWeek,
                    textDate.getText().toString(),
                    textTime.getText().toString(),
                    textAlarmTime.getText().toString(),
                    importance,
                    "You"
                );
                boolean isTimeDifferent = false;
                if(isEditing){
                    isTimeDifferent = !textDate.getText().toString().equals(oldReminder.getDate())
                            || !textTime.getText().toString().equals(oldReminder.getTime())
                            || !textAlarmTime.getText().toString().equals(oldReminder.getAlarmtime());
                    if(isTimeDifferent)
                        dataReminder.setStatus(DataReminder.STATUS_CREATED);
                    dataReminder.setDescription(oldReminder.getDescription());
                    dataReminder.setImage(oldReminder.getImage());
                    Intent intent1 = new Intent(getApplicationContext(), NotificationReceiver.class);
                    intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
                    intent1.putExtra("id", oldReminder.getReminderId());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), oldReminder.getReminderNumber(), intent1,
                            0);

                    AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(pendingIntent);
                }

                realm.beginTransaction();
                realm.copyToRealmOrUpdate(dataReminder);
                realm.commitTransaction();

                if (!isEditing && shouldSchedule() || isEditing && isTimeDifferent && shouldSchedule()) {
                    scheduleReminder(dataReminder.getReminderId());
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("updateMainList", true);
                editor.apply();

                if(!isEditing) {
                    Intent i = new Intent(CreateActivity.this, ReminderActivity.class);
                    i.putExtra("id", dataReminder.getReminderId());
                    startActivity(i);
                }
                finish();
            }
        });

        if(isEditing = getIntent().getBooleanExtra("isEditing", false)){
            saveButton.setText("Update Reminder");
            toolbarTitle.setText("Edit Reminder");
            reminderId = getIntent().getStringExtra("reminderId");
            DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", reminderId).findFirst();
            title.setText(reminder.getTitle());
            textDate.setText(reminder.getDate());
            textTime.setText(reminder.getTime());
            textAlarmTime.setText(reminder.getAlarmtime());
            importance = reminder.getImportance();
            if(reminder.getImportance() == 0){
                mediumImportance.setBackgroundResource(R.drawable.button_date);
                lowImportance.setBackgroundResource(R.drawable.button_importance_selected);
                importanceLayout = lowImportance;
            }
            else if(reminder.getImportance() == 2){
                mediumImportance.setBackgroundResource(R.drawable.button_date);
                highImportance.setBackgroundResource(R.drawable.button_importance_selected);
                importanceLayout = highImportance;
            }
            oldReminder = reminder;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");
            try {
                Date reminderDate = simpleDateFormat.parse(reminder.getDate() + " " + reminder.getTime());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(reminderDate);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
                day = calendar.get(Calendar.DATE);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    public String getCurrentDate(){
        Calendar now = Calendar.getInstance();
        day = now.get(Calendar.DAY_OF_MONTH);
        dayOfWeek = days[now.get(Calendar.DAY_OF_WEEK)-1];
        month = now.get(Calendar.MONTH);
        year = now.get(Calendar.YEAR);
        hour = 0;
        minute = 0;
        return String.format("%02d", day) + " " + months[month] + " " + year;
    }

    public boolean shouldSchedule(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");

        try {
            Date reminderDate = simpleDateFormat.parse(textDate.getText().toString() + " " + textTime.getText().toString());
            reminderDate = getDisplayTime(reminderDate);
            Date now = Calendar.getInstance().getTime();
            timeInMillis = reminderDate.getTime();
            long differenceInMillis = reminderDate.getTime() - now.getTime();
            long differenceInMinutes = TimeUnit.MINUTES.convert(differenceInMillis, TimeUnit.MILLISECONDS);

            return differenceInMinutes <= 40;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Date getDisplayTime(Date date){
        String displayTime = textAlarmTime.getText().toString();
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

    public void scheduleReminder(String id){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent1 = new Intent(getApplicationContext(), NotificationReceiver.class);
        intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
        intent1.putExtra("id", id);

        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
        int requestCode = reminder.getReminderNumber();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent1,
                0);
        if (Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC, timeInMillis, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC, timeInMillis, pendingIntent);

        realm.beginTransaction();
        if(reminder != null)
            reminder.setStatus(DataReminder.STATUS_SCHEDULED);
        realm.copyToRealmOrUpdate(reminder);
        realm.commitTransaction();
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