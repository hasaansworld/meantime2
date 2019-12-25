package dot.albums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

public class ReminderActivity extends AppCompatActivity {

    Toolbar toolbar;
    AppBarLayout appBarLayout;
    TextView title;
    List<String> titles = new ArrayList<>();
    int elevation;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        elevation = dpToPixel(4, this);

        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        title = findViewById(R.id.title);
        scrollView = findViewById(R.id.scrollView);

        setTitle();

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

    }


    void setTitle() {
        titles.add("Sandra's Birthday Party");
        titles.add("Dinner at Hardee's");
        titles.add("Flutter Interact DSC CEME");
        titles.add("Meeting");
        titles.add("Raiding Area 51 and Recovering Alien Life");
        int position = getIntent().getIntExtra("position", 0);
        title.setText(titles.get(position));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    public static int dpToPixel(float dp, Context context) {
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
