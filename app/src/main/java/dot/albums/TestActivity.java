package dot.albums;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

public class TestActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView print;
    Realm realm;
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    List<DataReminder> reminders = new ArrayList<>();
    long timeInMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        realm = Realm.getDefaultInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        print = findViewById(R.id.print);


        scheduleReminders();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }


    private void scheduleReminders() {
        Calendar now = Calendar.getInstance();
        String today = calendarToString(now);
        now.add(Calendar.DATE, 1);
        String tomorrow = calendarToString(now);
        reminders.addAll(
                realm.where(DataReminder.class)
                        .equalTo("date", today)
                        .or()
                        .equalTo("date", tomorrow)
                        .and()
                        .equalTo("status", DataReminder.STATUS_CREATED)
                        .findAll()
        );
        for(int i = reminders.size(); i > 0; i--) {
            DataReminder reminder = reminders.get(i-1);
            if (reminder.isDeleted())
                reminders.remove(i-1);
        }
        print.setText(print.getText()+"Found reminders matching criteria:\n"+reminders.size());
        for(DataReminder reminder: reminders){
            String sc = "\nDon't Schedule: \n";
            if(shouldSchedule(reminder)){
                sc = "\nShould Schedule: \n";
            }

            print.setText(
                    print.getText()+sc+reminder.getTitle()+"\n"+reminder.getDate()+" "+reminder.getTime()+"\n"
            );
        }
    }

    public boolean shouldSchedule(DataReminder dataReminder){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");

        try {
            Date reminderDate = simpleDateFormat.parse(dataReminder.getDate() + " " + dataReminder.getTime());
            reminderDate = getDisplayTime(reminderDate, dataReminder.getAlarmtime());
            Date now = Calendar.getInstance().getTime();
            timeInMillis = reminderDate.getTime();
            long differenceInMillis = reminderDate.getTime() - now.getTime();
            long differenceInMinutes = TimeUnit.MINUTES.convert(differenceInMillis, TimeUnit.MILLISECONDS);

            return differenceInMinutes <= 30;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Date getDisplayTime(Date date, String displayTime){
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

    private String calendarToString(Calendar calendar){
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        return String.format("%02d", day) + " " + months[month] + " " + year;
    }

}
