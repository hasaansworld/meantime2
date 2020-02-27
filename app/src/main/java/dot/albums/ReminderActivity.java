package dot.albums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;

public class ReminderActivity extends AppCompatActivity {

    Toolbar toolbar;
    AppBarLayout appBarLayout;
    TextView title, time, day, date, alarmTime;
    ImageView image;
    View circle;
    List<String> titles = new ArrayList<>();
    int elevation;
    ScrollView scrollView;
    LinearLayout addImage, addDescription;
    FrameLayout imageLayout;
    String id;
    Realm realm;
    HashMap<String, String> alarmTimesShort = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        elevation = dpToPixel(4, this);
        realm = RealmUtils.getRealm();
        fillAlarmTimes();

        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        title = findViewById(R.id.title);
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
        });

        addDescription.setOnClickListener(v -> {

        });
    }

    private void fillAlarmTimes() {
        alarmTimesShort.put("Exact time", "Exact");
        alarmTimesShort.put("5 minutes before", "5 min");
        alarmTimesShort.put("10 minutes before", "10 min");
        alarmTimesShort.put("15 minutes before", "15 min");
        alarmTimesShort.put("30 minutes before", "30 min");
        alarmTimesShort.put("1 hour before", "1 hour");
    }


    void setData() {
        String[] colors = {"#FFEE58", "#FF9700", "#F44336"};
        id = getIntent().getStringExtra("id");
        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
        title.setText(reminder.getTitle());
        day.setText(reminder.getDay());
        date.setText(reminder.getDate());
        time.setText(reminder.getTime());
        alarmTime.setText(alarmTimesShort.get(reminder.getAlarmtime()));
        Drawable d = getResources().getDrawable(R.drawable.circle_white);
        d.setColorFilter(Color.parseColor(colors[reminder.getImportance()]), PorterDuff.Mode.SRC_ATOP);
        circle.setBackground(d);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        else if(item.getItemId() == R.id.delete){
            AlertDialog d = new AlertDialog.Builder(ReminderActivity.this)
                    .setTitle("Delete")
                    .setMessage("Do you really want to delete this reminder?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        realm.beginTransaction();
                        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
                        reminder.setDeleted(true);
                        realm.commitTransaction();
                        Toast.makeText(this, "Reminder deleted!", Toast.LENGTH_SHORT).show();
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
            }
            catch (IOException e){
                Toast.makeText(this, "Could not copy image :(", Toast.LENGTH_SHORT).show();
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
}
