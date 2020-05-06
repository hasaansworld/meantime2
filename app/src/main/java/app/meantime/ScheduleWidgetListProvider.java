package app.meantime;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

public class ScheduleWidgetListProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    Realm realm;
    String today, tomorrow;
    List<TemporaryReminder> reminders = new ArrayList<>();
    int appWidgetId;
    SharedPreferences sharedPreferences;

    public ScheduleWidgetListProvider(Context context, int appWidgetId){
        this.context = context;
        this.appWidgetId = appWidgetId;
        sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        reminders.clear();
        fetchData();
    }

    private void fetchData(){
        String widgetMode = sharedPreferences.getString("widgetMode"+appWidgetId, "today");
        realm = Realm.getDefaultInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        Calendar now = Calendar.getInstance();
        today = simpleDateFormat.format(now.getTime());
        now.add(Calendar.DATE, 1);
        tomorrow = simpleDateFormat.format(now.getTime());
        List<DataReminder> listOne = realm.where(DataReminder.class).equalTo("date", widgetMode != null && widgetMode.equals("today") ? today : tomorrow).findAll();
        for(DataReminder reminder: listOne){
            if(!reminder.isDeleted()) reminders.add(new TemporaryReminder(reminder));
        }
        Collections.sort(reminders);
        realm.close();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return reminders.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        TemporaryReminder reminder = reminders.get(position);
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.item_schedule_widget_reminder);
        remoteViews.setTextViewText(R.id.title, reminder.getTitle());
        remoteViews.setTextViewText(R.id.time, reminder.getTime());
        remoteViews.setTextViewText(R.id.date, reminder.getDate());
        int[] circles = {R.drawable.circle_yellow, R.drawable.circle_orange, R.drawable.circle_red};
        remoteViews.setImageViewResource(R.id.circle, circles[reminder.getImportance()]);
        remoteViews.setTextViewText(R.id.repeat, getRepeatTitle(reminder.getRepeat()));
        remoteViews.setViewVisibility(R.id.repeat, reminder.getRepeat().equals("No repeat") ? View.GONE : View.VISIBLE);
        remoteViews.setTextViewText(R.id.description, reminder.getDescription() == null || reminder.getDescription().equals("") ? "No description." : reminder.getDescription());
        Intent fillIntent = new Intent();
        fillIntent.putExtra("id", reminder.getReminderId());
        remoteViews.setOnClickFillInIntent(R.id.layout, fillIntent);
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), R.layout.item_schedule_widget_loading);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private class TemporaryReminder implements Comparable<TemporaryReminder>{
        String reminderId;
        String title;
        String date;
        String time;
        String description;
        String repeat;
        int importance;

        public TemporaryReminder(DataReminder reminder){
            this.reminderId = reminder.getReminderId();
            this.title = reminder.getTitle();
            this.date = reminder.getDate();
            this.time = reminder.getTime();
            this.description = reminder.getDescription();
            this.repeat = reminder.getRepeat();
            this.importance = reminder.getImportance();
        }

        public String getReminderId() {
            return reminderId;
        }

        public void setReminderId(String reminderId) {
            this.reminderId = reminderId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRepeat() {
            return repeat;
        }

        public void setRepeat(String repeat) {
            this.repeat = repeat;
        }

        public int getImportance() {
            return importance;
        }

        public void setImportance(int importance) {
            this.importance = importance;
        }

        @Override
        public int compareTo(TemporaryReminder o) {
            if(!getDate().equals(o.getDate())) {
                String[] dates1 = getDate().split(" ");
                String[] dates2 = o.getDate().split(" ");
                dates1[1] = getMonthNumber(dates1[1]);
                dates2[1] = getMonthNumber(dates2[1]);
                if(!dates1[2].equals(dates2[2]))
                    return Integer.compare(Integer.parseInt(dates1[2]), Integer.parseInt(dates2[2]));
                else if(!dates1[1].equals(dates2[1]))
                    return Integer.compare(Integer.parseInt(dates1[1]), Integer.parseInt(dates2[1]));
                else
                    return Integer.compare(Integer.parseInt(dates1[0]), Integer.parseInt(dates2[0]));
            }
            else{
                String am1 = getTime().substring(6);
                String am2 = o.getTime().substring(6);
                if(!am1.equals(am2))
                    return am1.compareTo(am2);
                else
                    return getTime().replace("12:", "00:").compareTo(o.getTime().replace("12:", "00:"));
            }
        }
    }

    public String getMonthNumber(String name){
        return name.replace("Jan", "1")
                .replace("Feb", "2")
                .replace("Mar", "3")
                .replace("Apr", "4")
                .replace("May", "5")
                .replace("Jun", "6")
                .replace("Jul", "7")
                .replace("Aug", "8")
                .replace("Sep", "9")
                .replace("Oct", "10")
                .replace("Nov", "11")
                .replace("Dec", "12");
    }



    private String getRepeatTitle(String repeat){
        if(repeat.equals("Repeat every day")){
            return "Every day";
        }
        else if(repeat.equals("Repeat every week")){
            return "Every week";
        }
        else if(repeat.equals("Repeat every weekday (Mon-Fri)")){
            return "Every Mon-Fri";
        }
        else if(repeat.equals("Repeat every month")){
            return "Every month";
        }
        else{
            return "Every year";
        }
    }

}
