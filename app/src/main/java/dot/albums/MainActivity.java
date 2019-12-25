package dot.albums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Toolbar toolbar;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    AdapterReminders adapter;
    TextView title, day;
    LinearLayout headerLayout;
    FrameLayout jumpLayout;
    ImageView jumpIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //pickPhoto();

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        /*boolean accessDone = sharedPreferences.getBoolean("accessDone", false);
        if(!accessDone){
            startActivity(new Intent(this, ManageAccessActivity.class));
            finish();
        }*/

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setProfilePicture();

        title = findViewById(R.id.title);
        day = findViewById(R.id.day);
        headerLayout = findViewById(R.id.layout);
        jumpLayout = findViewById(R.id.jumpLayout);
        jumpIcon = findViewById(R.id.jumpIcon);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AdapterReminders(this);
        recyclerView.setAdapter(adapter);
        //StickyHeaderItemDecorator decorator = new StickyHeaderItemDecorator(adapter);
        //decorator.attachToRecyclerView(recyclerView);
        recyclerView.scrollToPosition(6);
        day.setText(adapter.getDayFromPositon(6));
        title.setText(adapter.getHeaderFromPosition(6));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstPosition = layoutManager.findFirstVisibleItemPosition();
                View v = layoutManager.findViewByPosition(firstPosition);
                int height = v.getHeight();
                int difference = height + v.getTop();
                float perc = ((float)difference/height)*100;
                int percentage = Math.round(perc);
                if(percentage < 40)
                    firstPosition++;
                int headerPosition = adapter.getHeaderPositionForItem(firstPosition);
                if(headerPosition == 2) {
                    headerLayout.setBackgroundColor(adapter.colorAccent);
                    jumpLayout.setVisibility(View.GONE);
                }
                else {
                    headerLayout.setBackgroundColor(Color.parseColor("#999999"));
                    jumpLayout.setVisibility(View.VISIBLE);
                    if(headerPosition > 2)
                        jumpIcon.setImageResource(R.drawable.ic_expand_less_black_18dp);
                    else
                        jumpIcon.setImageResource(R.drawable.ic_expand_more_black_18dp);
                }
                day.setText(adapter.getDayFromPositon(firstPosition));
                title.setText(adapter.getHeaderFromPosition(firstPosition));
            }
        });
        jumpLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutManager.scrollToPositionWithOffset(6, 0);
            }
        });
        /*GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(position == 0)
                    return 2;
                else
                    return 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new MainAdapter(this);
        recyclerView.setAdapter(adapter);*/

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
        getMenuInflater().inflate(R.menu.options_main, menu);
        /*if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.people)
            startActivity(new Intent(this, ManageAccessActivity.class));
        return true;
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

}
