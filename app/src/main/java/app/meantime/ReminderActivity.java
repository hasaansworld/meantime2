package app.meantime;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.florent37.viewtooltip.ViewTooltip;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.appbar.AppBarLayout;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class ReminderActivity extends AppCompatActivity {

    Toolbar toolbar;
    AppBarLayout appBarLayout;
    SharedPreferences sharedPreferences;
    TextView title, people, time, day, date, alarmTime, description, repeat;
    ImageView image, circle;
    String today = "", tomorrow = "";
    int elevation;
    ScrollView scrollView;
    LinearLayout addImage, addDescription, repeatLayout, descriptionLayout, imageLayout;
    String id, descriptionS = "";
    Realm realm;
    HashMap<String, String> alarmTimesShort = new HashMap<>();
    boolean isHistory;
    boolean isDeleted;
    long timeInMillis = 0;
    boolean update = false;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        elevation = dpToPixel(4, this);
        realm = RealmUtils.getRealm();
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        fillAlarmTimes();

        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        title = findViewById(R.id.title);
        people = findViewById(R.id.people);
        time = findViewById(R.id.time);
        day = findViewById(R.id.day);
        date = findViewById(R.id.date);
        circle = findViewById(R.id.circle);
        alarmTime = findViewById(R.id.text_alarm_time);
        scrollView = findViewById(R.id.scrollView);
        addImage = findViewById(R.id.layout_add_image);
        image = findViewById(R.id.image);
        imageLayout = findViewById(R.id.layout_image);
        addDescription = findViewById(R.id.layout_add_description);
        description = findViewById(R.id.description);
        descriptionLayout = findViewById(R.id.layout_description);
        repeat = findViewById(R.id.text_repeat);
        repeatLayout = findViewById(R.id.layout_repeat);

        setData();

        if(Build.VERSION.SDK_INT >= 21) {
            scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    int scrollY = scrollView.getScrollY();
                    if(getSupportActionBar() != null) {
                        if (scrollY > 0)
                            appBarLayout.setElevation(elevation);
                        else
                            appBarLayout.setElevation(0);
                    }
                }
            });
        }

        addImage.setOnClickListener(v -> {
            pickPhoto();
            updateLists("");
        });

        addDescription.setOnClickListener(v -> {
            editDescription();
            updateLists("");
        });

//        if(!isHistory && !isDeleted) {
//            description.setOnClickListener(v -> {
//                PopupMenu popup = new PopupMenu(ReminderActivity.this, v);
//                popup.setOnMenuItemClickListener(item -> {
//                    if (item.getItemId() == R.id.edit) {
//                        editDescription();
//                    } else {
//                        realm.beginTransaction();
//                        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
//                        reminder.setDescription("");
//                        realm.commitTransaction();
//                        addDescription.setVisibility(View.VISIBLE);
//                        descriptionLayout.setVisibility(View.GONE);
//                        ScheduleWidgetReceiver.refreshList(ReminderActivity.this);
//                    }
//                    updateLists("");
//                    return true;
//                });
//                popup.inflate(R.menu.options_description);
//                popup.show();
//            });

//            imageLayout.setOnClickListener(v -> {
//                PopupMenu popup = new PopupMenu(ReminderActivity.this, v);
//                popup.setOnMenuItemClickListener(item -> {
//                    if(item.getItemId() == R.id.change)
//                        pickPhoto();
//                    else{
//                        realm.beginTransaction();
//                        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
//                        reminder.setImage("");
//                        realm.commitTransaction();
//                        addImage.setVisibility(View.VISIBLE);
//                        imageLayout.setVisibility(View.GONE);
//                    }
//                    updateLists("");
//                    return true;
//                });
//                popup.inflate(R.menu.options_image);
//                popup.show();
//            });
//        }

        if(!sharedPreferences.getBoolean("noAds", false))
            showAd();

    }

    private void showAd(){
        new Handler().postDelayed(() -> {
            MobileAds.initialize(ReminderActivity.this, initializationStatus -> {
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


    private void editDescription(){
        Intent i = new Intent(ReminderActivity.this, AddDescriptionActivity.class);
        i.putExtra("id", id);
        startActivityForResult(i, 125);
    }

    private void updateLists(String message){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(isHistory)
            editor.putBoolean("updateHistoryList", true);
        else if(isDeleted)
            editor.putBoolean("updateDeletedList", true);
        else
            editor.putBoolean("updateMainList", true);
        editor.putString("message", message);
        editor.apply();
    }

    private void fillAlarmTimes() {
        alarmTimesShort.put("Exact time", "Exact");
        alarmTimesShort.put("5 minutes before", "5 min");
        alarmTimesShort.put("10 minutes before", "10 min");
        alarmTimesShort.put("15 minutes before", "15 min");
        alarmTimesShort.put("30 minutes before", "30 min");
        alarmTimesShort.put("1 hour before", "1 hour");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!update)
            update = true;
        else
            setData();
    }

    void setData() {
        String[] colors = {"#FFEE58", "#FF9700", "#F44336"};
        id = getIntent().getStringExtra("id");
        isHistory = getIntent().getBooleanExtra("isHistory", false);
        isDeleted = getIntent().getBooleanExtra("isDeleted", false);
        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
        title.setText(reminder.getTitle());
        //day.setText(reminder.getDay());
        getTodayAndTomorrow();
        String dateS = reminder.getDate();
        String dateAndDay = reminder.getDay().substring(0, 3) + ", " + dateS;
        if(dateS.equals(today))
            dateAndDay = "Today";
        else if(dateS.equals(tomorrow))
            dateAndDay = "Tomorrow";
        date.setText(dateAndDay);
        time.setText(reminder.getTime());
        people.setText(reminder.getOwner());
        alarmTime.setText(reminder.getAlarmtime());
        if(!reminder.getRepeat().equals("No repeat")){
            repeatLayout.setVisibility(View.VISIBLE);
            repeat.setText(reminder.getRepeat());
        }
        else{
            repeatLayout.setVisibility(View.GONE);
        }
        int[] circles = {R.drawable.circle_yellow, R.drawable.circle_orange, R.drawable.circle_red};
        circle.setImageResource(circles[reminder.getImportance()]);
        String path = reminder.getImage();
        if(path != null && !path.equals("")){
            addImage.setVisibility(View.GONE);
            imageLayout.setVisibility(View.VISIBLE);
            Glide.with(this).asBitmap().load(path).placeholder(R.drawable.broken_image).into(image);
        }
        else{
            imageLayout.setVisibility(View.GONE);
        }
        if(reminder.getDescription() != null && !reminder.getDescription().equals("")){
            addDescription.setVisibility(View.GONE);
            descriptionLayout.setVisibility(View.VISIBLE);
            description.setText(reminder.getDescription());
        }
        else{
            descriptionLayout.setVisibility(View.GONE);
        }
//        if(isHistory || isDeleted){
//            addDescription.setVisibility(View.GONE);
//            addImage.setVisibility(View.GONE);
//            descriptionLayout.setVisibility(View.VISIBLE);
//            findViewById(R.id.description_gap).setVisibility(View.VISIBLE);
//            if(reminder.getDescription() == null || reminder.getDescription().equals(""))
//                description.setText("No description.");
//        }
    }

    private void getTodayAndTomorrow(){
        Calendar now = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        today = simpleDateFormat.format(now.getTime());
        now.add(Calendar.DATE, 1);
        tomorrow = simpleDateFormat.format(now.getTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!isDeleted)
            getMenuInflater().inflate(R.menu.options_reminder, menu);
        else
            getMenuInflater().inflate(R.menu.options_reminder_deleted, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        else if(item.getItemId() == R.id.delete){
            AlertDialog d = new AlertDialog.Builder(ReminderActivity.this, R.style.AppTheme_Dialog)
                    .setTitle("Delete")
                    .setMessage(isHistory ? "This reminder will be deleted permanently. Do you want to continue?":"This reminder will be moved to the \"Deleted\" section. Do you want to continue?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();

                        Intent intent1 = new Intent(getApplicationContext(), NotificationReceiver.class);
                        intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
                        intent1.putExtra("id", reminder.getReminderId());
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), reminder.getReminderNumber(), intent1,
                                0);

                        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                        alarmManager.cancel(pendingIntent);

                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        if(isHistory) {
                            realm.beginTransaction();
                            reminder.deleteFromRealm();
                            realm.commitTransaction();
                            editor.putBoolean("updateHistoryList", true);
                            editor.putString("message", "Reminder deleted!");
                            editor.apply();
                        }
                        else {
                            realm.beginTransaction();
                            reminder.setDeleted(true);
                            realm.commitTransaction();
                            editor.putBoolean("updateMainList", true);
                            editor.putString("message", "Reminder deleted!");
                            editor.apply();
                        }
                        ScheduleWidgetReceiver.refreshList(ReminderActivity.this);

                        //.makeText(this, "Reminder deleted!", Toast.LENGTH_SHORT).show();

                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
            int colorAccent = getResources().getColor(R.color.colorAccent);
            d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#F44336"));
            d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(colorAccent);
        }
        else if(item.getItemId() == R.id.edit){
            if(isHistory) {
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putBoolean("updateHistoryList", true);
                editor.apply();
            }
            Intent i = new Intent(this, CreateActivity.class);
            i.putExtra("isEditing", true);
            i.putExtra("reminderId", id);
            i.putExtra("isHistory", isHistory);
            startActivity(i);
        }
        else if(item.getItemId() == R.id.delete_permanently){
            AlertDialog d = new AlertDialog.Builder(ReminderActivity.this, R.style.AppTheme_Dialog)
                    .setTitle("Delete")
                    .setMessage("This reminder will be deleted permanently. Do you want to continue?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        realm.beginTransaction();
                        reminder.deleteFromRealm();
                        realm.commitTransaction();
                        editor.putBoolean("updateDeletedList", true);
                        editor.putString("message", "Reminder permanently deleted!");
                        editor.apply();

                        //Toast.makeText(this, "Reminder permanently deleted!", Toast.LENGTH_SHORT).show();

                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
            int colorAccent = getResources().getColor(R.color.colorAccent);
            d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#F44336"));
            d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(colorAccent);
        }
        else if(item.getItemId() == R.id.restore){
            AlertDialog d = new AlertDialog.Builder(ReminderActivity.this, R.style.AppTheme_Dialog)
                    .setTitle("Restore")
                    .setMessage("This reminder will be restored and rescheduled. Do you want to continue?")
                    .setPositiveButton("Restore", (dialog, which) -> {

                        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        realm.beginTransaction();
                        reminder.setDeleted(false);
                        realm.copyToRealmOrUpdate(reminder);
                        realm.commitTransaction();
                        editor.putBoolean("updateDeletedList", true);
                        editor.putBoolean("updateMainList", true);
                        editor.putString("message", "Reminder restored!");
                        editor.apply();

                        if(reminder.getStatus() == DataReminder.STATUS_CREATED && shouldSchedule(reminder))
                            scheduleReminder(reminder.getReminderId());

                        ScheduleWidgetReceiver.refreshList(ReminderActivity.this);
                        //Toast.makeText(this, "Reminder restored!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
            int colorAccent = getResources().getColor(R.color.colorAccent);
            d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#4CAF50"));
            d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(colorAccent);

        }
        return true;
    }

    public static int dpToPixel(float dp, Context context) {
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
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
            String appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File dirFile = new File(Environment.getExternalStorageDirectory()+"/"+appName+"/Pictures");
            if (!dirFile.exists()){
                dirFile.mkdirs();
            }

            String imagePath = images.get(0).getPath();
            String path = Environment.getExternalStorageDirectory() + "/"+appName+"/Pictures/"+imagePath.substring(imagePath.lastIndexOf("/"));
            try{
                copy(new File(imagePath), new File(path));
                realm.beginTransaction();
                DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
                reminder.setImage(path);
                realm.commitTransaction();
                addImage.setVisibility(View.GONE);
                imageLayout.setVisibility(View.VISIBLE);
                Glide.with(this).asBitmap().load(path).into(image);

                int tooltipTimes = sharedPreferences.getInt("tooltipImage", 0);
                if(tooltipTimes < 10) {
                    ViewTooltip
                            .on(this, image)
                            .autoHide(true, 8000)
                            .corner(30)
                            .position(ViewTooltip.Position.TOP)
                            .text("Tap to change or remove")
                            .color(ContextCompat.getColor(this, R.color.colorAccent))
                            .textColor(Color.WHITE)
                            .withShadow(false)
                            .arrowWidth(10)
                            .arrowHeight(10)
                            .show();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("tooltipImage", tooltipTimes+1);
                    editor.apply();
                }
            }
            catch (IOException e){
                Toast.makeText(this, "Could not copy image :(", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == 125 && resultCode == RESULT_OK){
            DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
            descriptionS = reminder.getDescription();
            if(descriptionS != null && !descriptionS.equals("")){
                addDescription.setVisibility(View.GONE);
                description.setVisibility(View.VISIBLE);
                description.setText(descriptionS);
                int tooltipTimes = sharedPreferences.getInt("tooltipDescription", 0);
                if(tooltipTimes < 10) {
                    ViewTooltip
                            .on(this, description)
                            .autoHide(true, 8000)
                            .corner(30)
                            .position(ViewTooltip.Position.BOTTOM)
                            .text("Tap to edit or remove")
                            .color(ContextCompat.getColor(this, R.color.colorAccent))
                            .textColor(Color.WHITE)
                            .withShadow(false)
                            .arrowWidth(10)
                            .arrowHeight(10)
                            .show();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("tooltipDescription", tooltipTimes+1);
                    editor.apply();
                }
                ScheduleWidgetReceiver.refreshList(ReminderActivity.this);
            }
            else{
                addDescription.setVisibility(View.VISIBLE);
                description.setVisibility(View.GONE);
            }
        }
    }

    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }


    public boolean shouldSchedule(DataReminder reminder){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);

        try {
            Date reminderDate = simpleDateFormat.parse(reminder.getDate() + " " + reminder.getTime());
            reminderDate = getDisplayTime(reminderDate, reminder);
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

    public Date getDisplayTime(Date date, DataReminder reminder){
        String displayTime = reminder.getAlarmtime();
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
        Realm realm = RealmUtils.getRealm();
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

        realm.beginTransaction();
        if(reminder != null)
            reminder.setStatus(DataReminder.STATUS_SCHEDULED);
        realm.copyToRealmOrUpdate(reminder);
        realm.commitTransaction();
    }

}
