package app.meantime;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;

public class DeletedActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    AppBarLayout appbar, appbarSearch;
    Toolbar toolbar, searchToolbar;
    TextView toolbarTitle;
    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    EditText search;
    ImageView searchButton;
    LinearLayout searchNoResults, nothingHere;
    AdapterReminders adapterReminders;
    boolean isSearching = false, isInSelectionMode = false;
    int filter = -1;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        coordinatorLayout = findViewById(R.id.coordinator_layout);

        appbar = findViewById(R.id.appbar);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterReminders = new AdapterReminders(this, 2);
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

        adapterReminders.setOnItemSelectedListener(new AdapterReminders.OnItemSelectedListener() {
            @Override
            public void onStart() {
                isInSelectionMode = true;
                menu.clear();
                toolbarTitle.setText("1 selected");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
                getMenuInflater().inflate(R.menu.options_reminder_delete_selected, menu);
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
        toolbarTitle.setText("Deleted");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        menu.clear();
        getMenuInflater().inflate(R.menu.options_history, menu);
        enableToolbarScroll();
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
        new Handler().postDelayed(() -> appbar.setVisibility(View.GONE), 400);
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

    @Override
    protected void onStart() {
        super.onStart();
        if(sharedPreferences.getBoolean("updateDeletedList", false)){
            adapterReminders = new AdapterReminders(this, 2);
            recyclerView.setAdapter(adapterReminders);
            if (filter != -1)
                adapterReminders.setFilter(filter);
            if(isSearching)
                adapterReminders.search(search.getText().toString());
            if(adapterReminders.getItemCount() == 0 && !isSearching)
                nothingHere.setVisibility(View.VISIBLE);
            else if(adapterReminders.getItemCount() == 0 && isSearching)
                searchNoResults.setVisibility(View.VISIBLE);
            else
                nothingHere.setVisibility(View.GONE);
            String message = sharedPreferences.getString("message", "");
            if(message != null && !message.equals("")){
                showSnackbar(message);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sharedPreferences.getBoolean("updateDeletedList", false)){
            appbar.setExpanded(true, true);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("updateDeletedList", false);
            editor.putString("message", "");
            editor.apply();
        }
    }

    private void showSnackbar(String message){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.options_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if(isInSelectionMode){
                adapterReminders.clearSelections();
                clearSelectionMode();
            }
            else
                finish();
        }
        else if(item.getItemId() == R.id.filter){
            PopupMenu popup = new PopupMenu(this, toolbar);
            popup.setGravity(Gravity.END);
            popup.setOnMenuItemClickListener(popupItem -> {
                if(popupItem.getItemId() == R.id.no_filter) {
                    adapterReminders.setFilter(-1);
                    filter =  -1;
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
        else if(item.getItemId() == R.id.search)
            showSearch();
        else if(item.getItemId() == R.id.delete_all){
            AlertDialog d = new AlertDialog.Builder(DeletedActivity.this, R.style.AppTheme_Dialog)
                    .setTitle("Clear")
                    .setMessage("All deleted reminders will be lost. Do you want to continue?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        Realm realm = Realm.getDefaultInstance();
                        RealmResults<DataReminder> deletedReminders = realm.where(DataReminder.class).equalTo("deleted", true).findAll();
                        realm.beginTransaction();
                        deletedReminders.deleteAllFromRealm();
                        realm.commitTransaction();
                        realm.close();
                        adapterReminders = new AdapterReminders(this, 2);
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
        else if(item.getItemId() == R.id.selection_delete){
            int count = Integer.parseInt(toolbarTitle.getText().toString().substring(0, 1));
            clearSelectionMode();
            adapterReminders.deleteSelections(2);
            String half = count+" reminders ";
            if(count == 1)
                half = "1 reminder ";
            showSnackbar(half + "deleted!");
            if(adapterReminders.getItemCount() == 0)
                nothingHere.setVisibility(View.VISIBLE);
        }
        else if(item.getItemId() == R.id.selection_restore){
            int count = Integer.parseInt(toolbarTitle.getText().toString().substring(0, 1));
            clearSelectionMode();
            adapterReminders.deleteSelections(3);
            String half = count+" reminders ";
            if(count == 1)
                half = "1 reminder ";
            showSnackbar(half + "restored!");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("updateMainList", true);
            editor.apply();
            if(adapterReminders.getItemCount() == 0)
                nothingHere.setVisibility(View.VISIBLE);
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if(isInSelectionMode) {
            adapterReminders.clearSelections();
            clearSelectionMode();
        }
        else
            super.onBackPressed();
    }
}
