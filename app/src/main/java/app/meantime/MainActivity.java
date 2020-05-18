package app.meantime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.florent37.viewtooltip.ViewTooltip;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyenhoanglam.imagepicker.helper.PermissionHelper;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    AppBarLayout appbar, appbarSearch;
    Toolbar toolbar, searchToolbar;
    CoordinatorLayout coordinatorLayout;
    TextView toolbarTitle;
    EditText search;
    ImageView searchButton;
    FloatingActionButton fabAdd;
    ViewPager viewPager;
    TabLayout tabLayout;
    MaterialButton groupAdd;
    Menu menu;
    BackgroundService backgroundService;
    Intent mServiceIntent;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    boolean isPermissionGranted = false, isInSelectionMode = false;
    RemindersFragment remindersFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        appbar = findViewById(R.id.appbar);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //setProfilePicture();
        toolbarTitle = findViewById(R.id.toolbarTitle);

        coordinatorLayout = findViewById(R.id.coordinator_layout);

        /*viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);*/

        remindersFragment = new RemindersFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, remindersFragment);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();


        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if(viewPager.getCurrentItem() == 0)
                startActivity(new Intent(MainActivity.this, CreateActivity.class));

            }
        });
        if(Build.VERSION.SDK_INT < 21){
            fabAdd.setRippleColor(Color.parseColor("#1976D2"));
        }

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

        search = findViewById(R.id.search);
        appbarSearch = findViewById(R.id.appbar_search);
        searchToolbar = findViewById(R.id.toolbar_search);
        searchButton = findViewById(R.id.button_search);
        searchToolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        searchToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSearch();
            }
        });
        appbarSearch.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                appbarSearch.setTranslationY(0-appbarSearch.getHeight());
                appbarSearch.setVisibility(View.GONE);
                appbarSearch.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        searchButton.setOnClickListener(v-> remindersFragment.search(search.getText().toString()));
        search.setOnEditorActionListener((v, actionId, event) -> {
            remindersFragment.search(search.getText().toString());
            return true;
        });

        int tooltipTimes = sharedPreferences.getInt("tooltipFab", 0);
        if(tooltipTimes < 10) {
            ViewTooltip
                    .on(this, fabAdd)
                    .autoHide(true, 8000)
                    .corner(30)
                    .position(ViewTooltip.Position.LEFT)
                    .text("Create a reminder")
                    .color(ContextCompat.getColor(this, R.color.colorAccent))
                    .textColor(Color.WHITE)
                    .withShadow(false)
                    .arrowWidth(10)
                    .arrowHeight(10)
                    .show();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("tooltipFab", tooltipTimes+1);
            editor.apply();
        }

        /*toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TestActivity.class));
            }
        });*/

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!sharedPreferences.getBoolean("noAds", false))
                    initializeAds();
            }
        }, 3000);*/

    }


    /*private void initializeAds(){
        // Test Ads
        // Native "ca-app-pub-3940256099942544/2247696110"
        // Native video "ca-app-pub-3940256099942544/1044960115"
        // My Native "ca-app-pub-1683035414743855/4296467221"
        MobileAds.initialize(this, initializationStatus -> {
            //Toast.makeText(this, "Ads SDK initialized.", Toast.LENGTH_SHORT).show();
        });

        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("F68088F697A5D97E60C69783F1EBD9A4")).build();
        MobileAds.setRequestConfiguration(configuration);

        AdLoader.Builder builder = new AdLoader.Builder(this, "ca-app-pub-1683035414743855/4296467221");
        AdLoader loader = builder.forUnifiedNativeAd(unifiedNativeAd -> remindersFragment.showAd(unifiedNativeAd))
        .withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build())
        .build();
        AdRequest adRequest = new AdRequest.Builder().build();
        loader.loadAd(adRequest);
    }*/



    private void showSearch(){
        appbarSearch.setTranslationY(0-appbarSearch.getHeight());
        ObjectAnimator anim = ObjectAnimator.ofFloat(appbarSearch, "translationY", 0-appbarSearch.getHeight(), 0);
        anim.setDuration(400);
        anim.start();
        appbarSearch.setVisibility(View.VISIBLE);
        fabAdd.hide();
        search.setText("");
        search.requestFocus();
        showKeyboard();
        new Handler().postDelayed(() -> appbar.setVisibility(View.GONE), 400);
        remindersFragment.getAdapter().isSearching = true;
    }

    private void hideSearch(){
        ObjectAnimator anim = ObjectAnimator.ofFloat(appbarSearch, "translationY", 0, 0-appbarSearch.getHeight());
        anim.setDuration(400);
        anim.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                appbarSearch.setVisibility(View.GONE);
            }
        }, 400);
        fabAdd.show();
        hideKeyboard();
        remindersFragment.cancelSearch();
        appbar.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if(view == null){
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
        if(item.getItemId() == android.R.id.home && isInSelectionMode) {
            remindersFragment.getAdapter().clearSelections();
            clearSelectionMode();
        }
        else if(item.getItemId() == R.id.filter){
            PopupMenu popup = new PopupMenu(this, toolbar);
            popup.setGravity(Gravity.END);
            popup.setOnMenuItemClickListener(popupItem -> {
                if(popupItem.getItemId() == R.id.no_filter) {
                    remindersFragment.setFilter(-1);
                    item.setIcon(R.drawable.ic_filter_list_black_24dp);
                }
                else if(popupItem.getItemId() == R.id.low_importance) {
                    remindersFragment.setFilter(0);
                    item.setIcon(R.drawable.circle_low_importance);
                }
                else if(popupItem.getItemId() == R.id.mid_importance) {
                    remindersFragment.setFilter(1);
                    item.setIcon(R.drawable.circle_mid_importance);
                }
                else if(popupItem.getItemId() == R.id.high_importance) {
                    remindersFragment.setFilter(2);
                    item.setIcon(R.drawable.circle_high_importance);
                }
                return true;
            });
            popup.inflate(R.menu.options_filter);
            popup.show();
        }
        else if(item.getItemId() == R.id.search)
            showSearch();
        else if(item.getItemId() == R.id.history)
            startActivity(new Intent(this, HistoryActivity.class));
        else if(item.getItemId() == R.id.deleted)
            startActivity(new Intent(this, DeletedActivity.class));
        else if(item.getItemId() == R.id.contacts)
            startActivity(new Intent(this, ContactsActivity.class));
        else if(item.getItemId() == R.id.more)
            startActivity(new Intent(this, SettingsActivity.class));
        else if(item.getItemId() == R.id.selection_delete){
            int count = Integer.parseInt(toolbarTitle.getText().toString().substring(0, 1));
            clearSelectionMode();
            remindersFragment.getAdapter().deleteSelections(0);
            String half = count+" reminders ";
            if(count == 1)
                half = "1 reminder ";
            showSnackbar(half + "deleted!");
            ScheduleWidgetReceiver.refreshList(MainActivity.this);
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sharedPreferences.getBoolean("updateMainList", false)){
            appbar.setExpanded(true, true);
            String message = sharedPreferences.getString("message", "");
            if(message != null && !message.equals("")){
                showSnackbar(message);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("updateMainList", false);
            editor.putString("message", "");
            editor.apply();
        }

        remindersFragment.getAdapter().setOnItemSelectedListener(new AdapterReminders.OnItemSelectedListener() {
            @Override
            public void onStart() {
                isInSelectionMode = true;
                menu.clear();
                toolbarTitle.setText("1 selected");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
                getMenuInflater().inflate(R.menu.options_reminder_selected, menu);
                disableToolbarScroll();
            }
            @Override
            public void onEnd() {
                clearSelectionMode();
            }
            @Override
            public void onUpdate(int count) {
                toolbarTitle.setText(count+" selected");
            }
        });
    }

    private void disableToolbarScroll(){
        appbar.setExpanded(true);
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams)toolbar.getLayoutParams();
        p.setScrollFlags(0);
    }

    private void enableToolbarScroll(){
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams)toolbar.getLayoutParams();
        p.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        toolbar.setLayoutParams(p);
    }

    private void clearSelectionMode(){
        isInSelectionMode = false;
        toolbarTitle.setText(getResources().getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        menu.clear();
        getMenuInflater().inflate(R.menu.options_main_reminder, menu);
        enableToolbarScroll();
    }

    private void showSnackbar(String message){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        if(isInSelectionMode) {
            remindersFragment.getAdapter().clearSelections();
            clearSelectionMode();
        }
        else
            super.onBackPressed();
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
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.cancelAllWork();
//        PeriodicWorkRequest backgroundRequest =
//                new PeriodicWorkRequest.Builder(BackgroundWorker.class, 30, TimeUnit.MINUTES)
//                        .build();
//
//        WorkManager workManager = WorkManager.getInstance(this);
//        workManager.enqueueUniquePeriodicWork("backgroundWork", ExistingPeriodicWorkPolicy.KEEP, backgroundRequest);
        long timeInMillis = sharedPreferences.getLong("periodicUpdate", 0);
        if(timeInMillis == 0)
            periodicWork();
        else {
            long differenceMillis = System.currentTimeMillis()-timeInMillis;
            int hoursDifference = (int)(TimeUnit.HOURS.convert(differenceMillis, TimeUnit.MILLISECONDS));
            if(hoursDifference > 1) {
                cancelPreviousBackgroundAlarm();
                periodicWork();
            }
        }
    }

    private void periodicWork(){
        Intent intent = new Intent(this, PeriodicReceiver.class);
        intent.setAction(PeriodicReceiver.ACTION_BACKGROUND_SCHEDULE);
        sendBroadcast(intent);
    }

    private void cancelPreviousBackgroundAlarm(){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent1 = new Intent(getApplicationContext(), PeriodicReceiver.class);
        intent1.setAction(PeriodicReceiver.ACTION_BACKGROUND_SCHEDULE);

        int requestCode = 12;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent1,
                0);
        alarmManager.cancel(pendingIntent);
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
