package app.meantime;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.balsikandar.crashreporter.CrashReporter;
import com.bumptech.glide.Glide;
import com.github.florent37.viewtooltip.ViewTooltip;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class CreateActivity extends AppCompatActivity {
    CoordinatorLayout root;
    Toolbar toolbar;
    TextView toolbarTitle;
    ImageView emojiTitle, emojiDescription, circleImportance, image;
    LinearLayout alarmTime, layoutRepeat, imageLayout;
    EmojiEditText title, description;
    TextView textImportance, textAlarmTime, textRepeat, textImage;
    LinearLayout lowImportance, mediumImportance, highImportance, importanceLayout, alarmToneLayout;
    TextView textDate, textTime, textError, hintAlarmTone, textAlarmTone;
    View dividerTone;
    MaterialButton saveButton;
    int importance=1;
    String[] importances = {"Low", "Medium", "High"};
    int[] circles = {R.drawable.circle_yellow, R.drawable.circle_orange, R.drawable.circle_red};
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    String[] titles = {"New Message (Default)", "Alarm Sound", "Awesome Tune", "Business Tone", "Cute Melody",
            "Door Bell", "Great Tone", "Office Phone", "Positive Vibes", "Relaxing", "Ringtone Pro", "Romantic",
            "Wake Up Sound", "White Noise"};
    int[] tones = {R.raw.you_have_new_message, R.raw.alarm_sound, R.raw.awesome_tune,
            R.raw.business_tone, R.raw.cute_melody, R.raw.door_bell, R.raw.great_tone,
            R.raw.office_phone, R.raw.positive_vibes, R.raw.relaxing, R.raw.ringtone_pro,
            R.raw.romantic, R.raw.wake_up_sound, R.raw.white_noise};
    int day, month, year, hour = 0, minute = 0;
    String dayOfWeek;
    long timeInMillis = -1;
    Realm realm;
    boolean isEditing = false, isHistory = false;
    String reminderId, path = "";
    DataReminder oldReminder;
    SharedPreferences sharedPreferences;
    int alarmTone = 0;
    int alarmRadio = 0;
    MediaPlayer mediaPlayer;

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
        importanceLayout = findViewById(R.id.layout_importance);
        textDate = findViewById(R.id.text_date);
        textTime = findViewById(R.id.text_time);
        circleImportance = findViewById(R.id.circle_importance);
        textImportance = findViewById(R.id.text_importance);
        alarmToneLayout = findViewById(R.id.layout_alarm_tone);
        hintAlarmTone = findViewById(R.id.hint_alarm_tone);
        textAlarmTone = findViewById(R.id.text_alarm_tone);
        dividerTone = findViewById(R.id.divider_tone);
        layoutRepeat = findViewById(R.id.layout_repeat);
        textRepeat = findViewById(R.id.text_repeat);
        description = findViewById(R.id.description);
        emojiDescription = findViewById(R.id.emojiDescription);
        textImage = findViewById(R.id.text_image);
        image = findViewById(R.id.image);
        imageLayout = findViewById(R.id.layout_image);
        textError = findViewById(R.id.textError);
        saveButton = findViewById(R.id.saveButton);
        initializeEmoji(emojiTitle, title);
        initializeEmoji(emojiDescription, description);
        title.setHorizontallyScrolling(false);
        title.setImeOptions(EditorInfo.IME_ACTION_DONE);
        title.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        String dateS = getCurrentDate();
        textDate.setText(dateS);
        String timeS = getCurrentTime();
        textTime.setText(timeS);
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
            dpd.setMinDate(Calendar.getInstance());
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
            if(textDate.getText().toString().equals(getToday())){
                Calendar calendar = Calendar.getInstance();
                int min_hour = calendar.get(Calendar.HOUR_OF_DAY);
                int min_minute = calendar.get(Calendar.MINUTE);
                tpd.setMinTime(min_hour, min_minute, 0);
            }
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
//            importanceLayout.setBackgroundResource(R.drawable.button_date);
//            v.setBackgroundResource(R.drawable.button_importance_selected);
            PopupMenu popup = new PopupMenu(CreateActivity.this, v);
            popup.setOnMenuItemClickListener(item -> {
                String importanceS = item.getTitle().toString();
                textImportance.setText(importanceS);
                if(importanceS.equals("Low"))
                    importance = 0;
                else if(importanceS.equals("Medium"))
                    importance = 1;
                else
                    importance = 2;
                circleImportance.setImageResource(circles[importance]);
                if(importance == 2){
                    alarmToneLayout.setVisibility(View.VISIBLE);
                    dividerTone.setVisibility(View.VISIBLE);
                }
                else{
                    alarmToneLayout.setVisibility(View.GONE);
                    dividerTone.setVisibility(View.GONE);
                }
                return true;
            });
            popup.inflate(R.menu.options_importance);
            popup.show();

        };
        importanceLayout.setOnClickListener(importanceListener);
//        mediumImportance.setOnClickListener(importanceListener);
//        highImportance.setOnClickListener(importanceListener);

        alarmToneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
                View v = LayoutInflater.from(CreateActivity.this).inflate(R.layout.dialog_tone_picker, null, false);
                RadioGroup toneGroup = v.findViewById(R.id.tone_group);

                for(int i = 0; i < tones.length; i++) {
                    RadioButton radioButton = new RadioButton(CreateActivity.this);
                    RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                    int paddingHorizontal = Math.round(dpToPixel(10, CreateActivity.this));
                    int paddingVertical = Math.round(dpToPixel(8, CreateActivity.this));
                    radioButton.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
                    toneGroup.addView(radioButton, layoutParams);
                    final int buttonPosition = i;
                    radioButton.setText(titles[buttonPosition]);
                    radioButton.setTextSize(16);
                    radioButton.setTextColor(Color.BLACK);
                    if(alarmTone == buttonPosition)
                        radioButton.setChecked(true);
                    View.OnClickListener onClickListener = v1 -> {
                        int tone = tones[buttonPosition];
                        alarmRadio = buttonPosition;
                        if (mediaPlayer != null)
                            mediaPlayer.release();
                        mediaPlayer = MediaPlayer.create(CreateActivity.this, tone);
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                    };
                    radioButton.setOnClickListener(onClickListener);
                }

                AlertDialog d = new AlertDialog.Builder(CreateActivity.this, R.style.AppTheme_Dialog)
                        .setTitle("Alarm Tones")
                        .setView(v)
                        .setPositiveButton("Select", (dialog, which) -> {
                            textAlarmTone.setText(titles[alarmRadio]);
                            alarmTone = alarmRadio;
                            if(mediaPlayer != null)
                                mediaPlayer.release();
                        })
                        .setNegativeButton("Cancel", ((dialog, which) -> {
                            dialog.cancel();
                            if(mediaPlayer != null)
                                mediaPlayer.release();
                        }))
                        .show();
                int colorAccent = getResources().getColor(R.color.colorAccent);
                d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(colorAccent);
                d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(colorAccent);
                d.setOnCancelListener(dialog -> {
                    if(mediaPlayer != null)
                        mediaPlayer.release();
                });
            }
        });

        layoutRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(CreateActivity.this, v);
                popup.setOnMenuItemClickListener(item -> {
                    textRepeat.setText(item.getTitle());
                    return true;
                });
                popup.inflate(R.menu.options_repeat);
                popup.show();
            }
        });

        imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(path.equals("")){
                    pickPhoto();
                }
                else{
                    PopupMenu popup = new PopupMenu(CreateActivity.this, image);
                    popup.setOnMenuItemClickListener(item -> {
                        if(item.getItemId() == R.id.change)
                            pickPhoto();
                        else{
                            path = "";
                            image.setVisibility(View.GONE);
                            textImage.setText("Tap to add an image.");
                        }
                        return true;
                    });
                    popup.inflate(R.menu.options_image);
                    popup.show();
                }
            }
        });

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
                    alarmTone,
                    textRepeat.getText().toString(),
                    "You"
                );
                dataReminder.setDescription(description.getText().toString());
                dataReminder.setImage(path);
                boolean isTimeDifferent = false;
                if(isEditing){
                    isTimeDifferent = !textDate.getText().toString().equals(oldReminder.getDate())
                            || !textTime.getText().toString().equals(oldReminder.getTime())
                            || !textAlarmTime.getText().toString().equals(oldReminder.getAlarmtime());
                    if(isTimeDifferent)
                        dataReminder.setStatus(DataReminder.STATUS_CREATED);
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

                if (!isEditing || isTimeDifferent) {
                    //scheduleReminder(dataReminder.getReminderId());
                    //DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    //ref.child("reminders").child(reminderId).setValue(dataReminder);
                    ReminderUtils.schedule(CreateActivity.this, dataReminder);
                }
                //else if(!isEditing && !shouldSchedule() || isEditing && isTimeDifferent && !shouldSchedule()){
                    //sendNotification(1191, "Should schedule returns false!", "Should schedule returns false!");
                //}

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("updateMainList", true);
                editor.apply();

                if(!isEditing) {
                    Intent i = new Intent(CreateActivity.this, ReminderActivity.class);
                    i.putExtra("id", dataReminder.getReminderId());
                    startActivity(i);
                }

                ScheduleWidgetReceiver.refreshList(CreateActivity.this);
                finish();
            }
        });


        if(isHistory = getIntent().getBooleanExtra("isHistory", false)){
            title.setEnabled(false);
            emojiTitle.setClickable(false);
            emojiTitle.setColorFilter(Color.parseColor("#999999"), PorterDuff.Mode.SRC_ATOP);
            title.setTextColor(Color.parseColor("#999999"));
            lowImportance.setClickable(false);
            mediumImportance.setClickable(false);
            highImportance.setClickable(false);
            layoutRepeat.setClickable(false);
        }
        if(isEditing = getIntent().getBooleanExtra("isEditing", false)){
            saveButton.setText("Update Reminder");
            toolbarTitle.setText("Edit Reminder");
            reminderId = getIntent().getStringExtra("reminderId");
            DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", reminderId).findFirst();
            title.setText(reminder.getTitle());
            title.setSelection(title.getText().toString().length());
            textDate.setText(reminder.getDate());
            textTime.setText(reminder.getTime());
            textAlarmTime.setText(reminder.getAlarmtime());
            textRepeat.setText(reminder.getRepeat());
            description.setText(reminder.getDescription());
            importance = reminder.getImportance();
//            if(reminder.getImportance() == 0){
//                mediumImportance.setBackgroundResource(R.drawable.button_date);
//                lowImportance.setBackgroundResource(R.drawable.button_importance_selected);
//                importanceLayout = lowImportance;
//            }
            textImportance.setText(importances[reminder.getImportance()]);
            circleImportance.setImageResource(circles[reminder.getImportance()]);
            if(reminder.getImportance() == 2){
//                mediumImportance.setBackgroundResource(R.drawable.button_date);
//                highImportance.setBackgroundResource(R.drawable.button_importance_selected);
//                importanceLayout = highImportance;
//                hintAlarmTone.setVisibility(View.VISIBLE);
                alarmToneLayout.setVisibility(View.VISIBLE);
                dividerTone.setVisibility(View.VISIBLE);
                alarmTone = reminder.getAlarmTone();
                alarmRadio = reminder.getAlarmTone();
                textAlarmTone.setText(titles[alarmTone]);
            }
            if(reminder.getImage() != null && !reminder.getImage().equals("")) {
                image.setVisibility(View.VISIBLE);
                textImage.setText("Tap to change or remove.");
                path = reminder.getImage();
                Glide.with(this).asBitmap().load(path).placeholder(R.drawable.broken_image).into(image);
            }
            oldReminder = reminder;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
            try {
                Date reminderDate = simpleDateFormat.parse(reminder.getDate() + " " + reminder.getTime());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(reminderDate);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);
                dayOfWeek = days[calendar.get(Calendar.DAY_OF_WEEK)-1];
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    public String getCurrentDate(){
        Calendar now = Calendar.getInstance();
        day = now.get(Calendar.DAY_OF_MONTH);
        dayOfWeek = days[now.get(Calendar.DAY_OF_WEEK) - 1];
        month = now.get(Calendar.MONTH);
        year = now.get(Calendar.YEAR);
        hour = now.get(Calendar.HOUR_OF_DAY);
        minute = now.get(Calendar.MINUTE);
        return String.format(Locale.ENGLISH, "%02d", day) + " " + months[month] + " " + year;
    }

    public String getToday(){
        Calendar now = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        return simpleDateFormat.format(now.getTime());
    }

    public String getCurrentTime(){
        Calendar now = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        Date date = now.getTime();
        return simpleDateFormat.format(date);
    }

    public boolean shouldSchedule(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);

        try {
            Date reminderDate = simpleDateFormat.parse(textDate.getText().toString() + " " + textTime.getText().toString());
            reminderDate = getDisplayTime(reminderDate);
            Date now = Calendar.getInstance().getTime();
            timeInMillis = reminderDate.getTime();
            long differenceInMillis = reminderDate.getTime() - now.getTime();
            long differenceInMinutes = TimeUnit.MINUTES.convert(differenceInMillis, TimeUnit.MILLISECONDS);

            return differenceInMinutes <= 40;
        } catch (ParseException e) {
            //sendNotification(959, "Parse Exception occurred!", e.toString());
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

        if (Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else if (Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);

        //sendNotification(1190, "Scheduled reminder with Id: "+reminder.getReminderId(), "Scheduled reminder with Id: "+reminder.getReminderId());

        realm.beginTransaction();
        if(reminder != null)
            reminder.setStatus(DataReminder.STATUS_SCHEDULED);
        realm.copyToRealmOrUpdate(reminder);
        realm.commitTransaction();

        Toast.makeText(this, "Alarm scheduled!", Toast.LENGTH_SHORT).show();
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


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Low Importance Reminders";
            String description = "Get simple notifications about low importance reminders.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void pickPhoto() {
        ImagePicker.Builder builder = ImagePicker.with(this);                         //  Initialize ImagePicker with activity or fragment context
        if(Build.VERSION.SDK_INT >= 23) builder.setStatusBarColor("#FFFFFF"); //  StatusBar color
        builder.setToolbarColor("#FFFFFF")
                .setToolbarTextColor("#000000")     //  Toolbar text color (Title and Done button)
                .setToolbarIconColor("#000000")     //  Toolbar icon color (Back and Camera button)
                .setProgressBarColor("#5C6BC0")     //  ProgressBar color
                .setBackgroundColor("#FFFFFF")      //  Background color
                .setCameraOnly(false)               //  Camera mode
                .setMultipleMode(false)              //  Select multiple images or single image
                .setFolderMode(true)                //  Folder mode
                .setShowCamera(true)                //  Show camera button
                .setFolderTitle("Pick a Photo")           //  Folder title (works with FolderMode = true)
                .setImageTitle("Photos")            //  Image title (works with FolderMode = false)
                .setDoneTitle("Done")               //  Done button title
                .setLimitMessage("You have reached selection limit")    // Selection limit message
                .setMaxSize(1)                     //  Max images can be selected
                .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                .setRequestCode(100)                //  Set request code, default Config.RC_PICK_IMAGES
                .setKeepScreenOn(true)              //  Keep screen on when selecting images
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
//            String appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
//            File dirFile = new File(Environment.getExternalStorageDirectory()+"/"+appName+"/Pictures");
//            if (!dirFile.exists()){
//                dirFile.mkdirs();
//            }

            path = images.get(0).getPath();
            //path = Environment.getExternalStorageDirectory() + "/"+appName+"/Pictures/"+imagePath.substring(imagePath.lastIndexOf("/"));
//            try{
//                //copy(new File(imagePath), new File(path));
//                realm.beginTransaction();
//                DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
//                reminder.setImage(path);
//                realm.commitTransaction();
            image.setVisibility(View.VISIBLE);
            Glide.with(this).asBitmap().load(path).placeholder(R.drawable.broken_image).into(image);
            textImage.setText("Tap to change or remove.");
//            }
//            catch (IOException e){
//                Toast.makeText(this, "Could not copy image :(", Toast.LENGTH_SHORT).show();
//            }
        }
    }


    /*void sendNotification(int id, String message, String extra) {
        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra("message", extra);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        createNotificationChannel();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(Build.VERSION.SDK_INT >= 21 ? R.drawable.ic_notifications_none_black_24dp : R.drawable.ic_notifications_none_white_24dp);
        builder.setContentTitle(message);
        builder.setContentText("debug message")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setSound(soundUri)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(id, builder.build());
    }*/

    public static float dpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

}
