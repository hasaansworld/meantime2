package app.meantime;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

public class ScheduleWidgetListProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    Realm realm;
    String today, tomorrow;
    List<TemporaryReminder> reminders = new ArrayList<>();

    public ScheduleWidgetListProvider(Context context, Intent intent){
        this.context = context;
    }

    @Override
    public void onCreate() {
        realm = Realm.getDefaultInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        Calendar now = Calendar.getInstance();
        today = simpleDateFormat.format(now.getTime());
        now.add(Calendar.DATE, 1);
        tomorrow = simpleDateFormat.format(now.getTime());
        List<DataReminder> listOne = realm.where(DataReminder.class).equalTo("date", today).findAll();
        for(DataReminder reminder: listOne){
            reminders.add(new TemporaryReminder(reminder));
        }
        realm.close();
    }

    @Override
    public void onDataSetChanged() {

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
        remoteViews.setTextViewText(R.id.repeat, reminder.getRepeat());
        remoteViews.setViewVisibility(R.id.repeat, reminder.getRepeat().equals("No repeat") ? View.GONE : View.VISIBLE);
        remoteViews.setTextViewText(R.id.description, reminder.getDescription() == null || reminder.getDescription().equals("") ? "No description." : reminder.getDescription());
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

    private class TemporaryReminder{
        String title;
        String date;
        String time;
        String description;
        String repeat;
        int importance;

        public TemporaryReminder(DataReminder reminder){
            this.title = reminder.getTitle();
            this.date = reminder.getDate();
            this.time = reminder.getTime();
            this.description = reminder.getDescription();
            this.repeat = reminder.getRepeat();
            this.importance = reminder.getImportance();
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

    }
}
