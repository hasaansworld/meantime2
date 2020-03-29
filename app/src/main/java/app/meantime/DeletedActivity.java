package app.meantime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class DeletedActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    AdapterReminders adapterReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterReminders = new AdapterReminders(this, 2);
        recyclerView.setAdapter(adapterReminders);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(getSharedPreferences("data", MODE_PRIVATE).getBoolean("updateDeletedList", false)){
            adapterReminders = new AdapterReminders(this, 2);
            recyclerView.setAdapter(adapterReminders);
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
                    item.setIcon(R.drawable.ic_filter_list_black_24dp);
                }
                else if(popupItem.getItemId() == R.id.low_importance) {
                    adapterReminders.setFilter(0);
                    item.setIcon(R.drawable.circle_low_importance);
                }
                else if(popupItem.getItemId() == R.id.mid_importance) {
                    adapterReminders.setFilter(1);
                    item.setIcon(R.drawable.circle_mid_importance);
                }
                else if(popupItem.getItemId() == R.id.high_importance) {
                    adapterReminders.setFilter(2);
                    item.setIcon(R.drawable.circle_high_importance);
                }
                return true;
            });
            popup.inflate(R.menu.options_filter);
            popup.show();
        }
        return true;
    }


}
