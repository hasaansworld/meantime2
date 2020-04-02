package app.meantime;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.appbar.AppBarLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class HistoryActivity extends AppCompatActivity {
    AppBarLayout appbar, appbarSearch;
    Toolbar toolbar, searchToolbar;
    EditText search;
    ImageView searchButton;
    LinearLayout searchNoResults, nothingHere;
    RecyclerView recyclerView;
    AdapterReminders adapterReminders;
    boolean isSearching = false;
    int filter = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        appbar = findViewById(R.id.appbar);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterReminders = new AdapterReminders(this, 1);
        recyclerView.setAdapter(adapterReminders);

        search = findViewById(R.id.search);
        appbarSearch = findViewById(R.id.appbar_search);
        searchToolbar = findViewById(R.id.toolbar_search);
        searchButton = findViewById(R.id.button_search);
        searchNoResults = findViewById(R.id.search_no_results);
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

        searchButton.setOnClickListener(v-> {
            search();
        });
        search.setOnEditorActionListener((v, actionId, event) -> {
            search();
            return true;
        });

        nothingHere = findViewById(R.id.nothing_here);
        if(adapterReminders.getItemCount() == 0)
            nothingHere.setVisibility(View.VISIBLE);
    }

    private void search(){
        adapterReminders.search(search.getText().toString());
        if(adapterReminders.getItemCount() == 0)
            searchNoResults.setVisibility(View.VISIBLE);
        else
            searchNoResults.setVisibility(View.GONE);
    }

    private void showSearch(){
        appbarSearch.setTranslationY(0-appbarSearch.getHeight());
        ObjectAnimator anim = ObjectAnimator.ofFloat(appbarSearch, "translationY", 0-appbarSearch.getHeight(), 0);
        anim.setDuration(400);
        anim.start();
        appbarSearch.setVisibility(View.VISIBLE);
        search.setText("");
        search.requestFocus();
        showKeyboard();
        isSearching = true;
        if(nothingHere.getVisibility() == View.VISIBLE)
            nothingHere.setVisibility(View.INVISIBLE);
        if(Build.VERSION.SDK_INT >= 21){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    appbar.setElevation(0);
                }
            }, 400);
        }
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
        hideKeyboard();
        adapterReminders.cancelSearch();
        isSearching = false;
        searchNoResults.setVisibility(View.GONE);
        if(nothingHere.getVisibility() == View.INVISIBLE)
            nothingHere.setVisibility(View.VISIBLE);
        if(Build.VERSION.SDK_INT >= 21)
            appbar.setElevation(MainActivity.dpToPixel(4, HistoryActivity.this));
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

    @Override
    protected void onStart() {
        super.onStart();
        if(getSharedPreferences("data", MODE_PRIVATE).getBoolean("updateHistoryList", false)){
            adapterReminders = new AdapterReminders(this, 1);
            recyclerView.setAdapter(adapterReminders);
            if (filter != -1)
                adapterReminders.setFilter(filter);
            if(isSearching)
                adapterReminders.search(search.getText().toString());
            if(adapterReminders.getItemCount() == 0)
                nothingHere.setVisibility(View.VISIBLE);
            else
                nothingHere.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        else if(item.getItemId() == R.id.filter){
            PopupMenu popup = new PopupMenu(this, toolbar);
            popup.setGravity(Gravity.END);
            popup.setOnMenuItemClickListener(popupItem -> {
                if(popupItem.getItemId() == R.id.no_filter) {
                    adapterReminders.setFilter(-1);
                    filter = -1;
                    item.setIcon(R.drawable.ic_filter_list_black_24dp);
                }
                else if(popupItem.getItemId() == R.id.low_importance) {
                    adapterReminders.setFilter(0);
                    filter = 0;
                    item.setIcon(R.drawable.circle_low_importance);
                }
                else if(popupItem.getItemId() == R.id.mid_importance) {
                    adapterReminders.setFilter(1);
                    filter = 1;
                    item.setIcon(R.drawable.circle_mid_importance);
                }
                else if(popupItem.getItemId() == R.id.high_importance) {
                    adapterReminders.setFilter(2);
                    filter = 2;
                    item.setIcon(R.drawable.circle_high_importance);
                }
                if(adapterReminders.getItemCount() == 0)
                    nothingHere.setVisibility(View.VISIBLE);
                else
                    nothingHere.setVisibility(View.GONE);
                return true;
            });
            popup.inflate(R.menu.options_filter);
            popup.show();
        }
        else if(item.getItemId() == R.id.search){
            showSearch();
        }
        else if(item.getItemId() == R.id.delete_all){
            AlertDialog d = new AlertDialog.Builder(HistoryActivity.this)
                    .setTitle("Clear")
                    .setMessage("All reminders in \"History\" will be lost. Do you want to continue?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        Realm realm = Realm.getDefaultInstance();
                        List<DataReminder> historyReminders = new ArrayList<>();
                        historyReminders.addAll(realm.where(DataReminder.class).findAll());
                        Collections.sort(historyReminders);
                        Collections.reverse(historyReminders);
                        historyReminders = removeNewReminders(historyReminders);
                        if(historyReminders != null){
                            for(int i = 0; i < historyReminders.size(); i++) {
                                DataReminder reminder = historyReminders.get(i);
                                realm.beginTransaction();
                                reminder.deleteFromRealm();
                                realm.commitTransaction();
                            }
                        }
                        realm.close();
                        adapterReminders = new AdapterReminders(this, 1);
                        recyclerView.setAdapter(adapterReminders);
                        adapterReminders.setFilter(filter);
                        if(adapterReminders.getItemCount() == 0)
                            nothingHere.setVisibility(View.VISIBLE);
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


    private List<DataReminder> removeNewReminders(List<DataReminder> allReminders){
        Calendar now = Calendar.getInstance();
        Date dT = now.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String today = sdf.format(dT);
        int todayIndex = -1;
        for(int i = 0; i < allReminders.size(); i++){
            DataReminder reminder = allReminders.get(i);
            try {
                Date d = sdf.parse(today);
                Date d2 = sdf.parse(reminder.getDate());
                if(d2.getTime() < d.getTime()) {
                    todayIndex = i;
                    break;
                }
            }
            catch (ParseException e){
                // Couldn't parse, maybe ignore?
            }
        }
        if(todayIndex == -1)
            return null;
        else
            allReminders = allReminders.subList(todayIndex, allReminders.size());
        return allReminders;
    }

}
