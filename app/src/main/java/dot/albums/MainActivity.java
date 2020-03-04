package dot.albums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Toolbar toolbar;
    TextView toolbarTitle;
    FloatingActionButton fabAdd;
    ViewPager viewPager;
    TabLayout tabLayout;
    MaterialButton groupAdd;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setProfilePicture();
        toolbarTitle = findViewById(R.id.toolbarTitle);

        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem() == 0)
                    startActivity(new Intent(MainActivity.this, CreateActivity.class));

            }
        });

        groupAdd = findViewById(R.id.group_add);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int[] menus = {R.menu.options_main_reminder, R.menu.options_main_groups, R.menu.options_main_people};
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                if(position == 0)
                    fabAdd.show();
                else
                    fabAdd.hide();
                if(position == 1)
                    showAddGroup();
                else
                    hideAddGroup();
                menu.clear();
                getMenuInflater().inflate(menus[position], menu);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

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
        this.menu = menu;
        getMenuInflater().inflate(R.menu.options_main_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            startActivity(new Intent(this, ProfileActivity.class));
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AdapterReminders(this);
        recyclerView.setAdapter(adapter);*/
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

}
