package app.meantime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.nguyenhoanglam.imagepicker.helper.PermissionHelper;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Toolbar toolbar;
    TextView toolbarTitle;
    FloatingActionButton fabAdd;
    ViewPager viewPager;
    TabLayout tabLayout;
    MaterialButton groupAdd;
    Menu menu;
    BackgroundService backgroundService;
    Intent mServiceIntent;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    boolean isPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //setProfilePicture();
        toolbarTitle = findViewById(R.id.toolbarTitle);

        /*viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);*/

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, new RemindersFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if(viewPager.getCurrentItem() == 0)
                startActivity(new Intent(MainActivity.this, CreateActivity.class));

            }
        });

        groupAdd = findViewById(R.id.group_add);

        /*viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int[] menus = {R.menu.options_main_reminder, R.menu.options_main_groups, R.menu.options_main_people};
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                if(menu != null) {
                    menu.clear();
                    if (position == 0) {
                        fabAdd.show();
                        getMenuInflater().inflate(R.menu.options_main_reminder, menu);
                    } else {
                        fabAdd.hide();
                        getMenuInflater().inflate(R.menu.options_main_groups, menu);
                    }
                *//*if(position == 1)
                    groupAdd.setVisibility(View.VISIBLE);
                else
                    groupAdd.setVisibility(View.GONE);*//*
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });*/

        backgroundWork();

    }

    private void showAddGroup(){
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(groupAdd, "scaleX", 0, 1);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(groupAdd, "scaleY", 0, 1);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.playTogether(animatorX, animatorY);
        animatorSet.start();
    }

    private void hideAddGroup(){
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(groupAdd, "scaleX", 1, 0);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(groupAdd, "scaleY", 1, 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.playTogether(animatorX, animatorY);
        animatorSet.start();
    }


    public void setProfilePicture(){
        if(getSupportActionBar() != null){
            String path = sharedPreferences.getString("profilePicPath", "");
            final int dpToPx = Math.round(dpToPixel(30, this));
            Glide.with(this)
                    .load(path)
                    .override(dpToPx, dpToPx)
                    .apply(RequestOptions.circleCropTransform())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.profile_picture)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            getSupportActionBar().setHomeAsUpIndicator(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int[] options = {R.menu.options_main_reminder, R.menu.options_main_groups, R.menu.options_main_people};
        this.menu = menu;
        //getMenuInflater().inflate(options[viewPager.getCurrentItem()], menu);
        getMenuInflater().inflate(R.menu.options_main_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            //startActivity(new Intent(this, ProfileActivity.class));
            startActivity(new Intent(this, TestActivity.class));
        else if(item.getItemId() == R.id.contacts)
            startActivity(new Intent(this, ContactsActivity.class));
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //checkPermission();
    }

    public static float dpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
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

    private class MainPagerAdapter extends FragmentPagerAdapter{
        String[] titles = {"Reminders", "Groups", "People"};
        List<Fragment> fragmentList = new ArrayList<>();

        public MainPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            fragmentList.add(new RemindersFragment());
            fragmentList.add(new GroupsFragment());
            fragmentList.add(new PeopleFragment());
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private void backgroundWork() {
        PeriodicWorkRequest backgroundRequest =
                new PeriodicWorkRequest.Builder(BackgroundWorker.class, 30, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork("backgroundWork", ExistingPeriodicWorkPolicy.KEEP, backgroundRequest);

    }

    public void updateContacts(){
        String lastContactsUpdate = sharedPreferences.getString("lastContactsUpdate", "");
        String today = getCurrentDate();
        if(!lastContactsUpdate.equals(today)){
            ContactsTask task = new ContactsTask(this);
            task.setOnFinishedListener(() -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("lastContactsUpdate", today);
                editor.apply();
                Toast.makeText(backgroundService, "Contacts Updated!", Toast.LENGTH_SHORT).show();
            });
            task.execute();
        }
        else
            Toast.makeText(this, "Already updated contacts today!", Toast.LENGTH_SHORT).show();
    }

    public String getCurrentDate(){
        Calendar now = Calendar.getInstance();
        int day = now.get(Calendar.DAY_OF_MONTH);
        int month = now.get(Calendar.MONTH);
        int year = now.get(Calendar.YEAR);
        return String.format("%02d", day) + " " + months[month] + " " + year;
    }


    public void checkPermission(){
        if(!isPermissionGranted) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    showPermissionDialog(false);
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            100);
                }
            } else {
                isPermissionGranted = true;
                updateContacts();
            }
        }
    }


    public void showPermissionDialog(boolean settings){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Contacts Permission")
                .setMessage("Please allow us to read your contacts so you can share reminders with your friends.");
        if(settings){
            builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    PermissionHelper.openAppSettings(MainActivity.this);
                }
            });
        }
        else {
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            100);
                }
            });
        }
        AlertDialog dialog = builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        })
                .show();
        int colorAccent = getResources().getColor(R.color.colorAccent);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(colorAccent);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(colorAccent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        for(int i = 0; i < permissions.length; i++) {
            switch (requestCode) {
                case 100: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        updateContacts();
                        isPermissionGranted = true;
                    } else {
                        showPermissionDialog(Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[i]));
                    }
                    return;
                }
            }
        }
    }

}
