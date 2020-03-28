package app.meantime;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class AdapterReminders extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    Context context;
    HashMap<String, String> dates = new HashMap<>();
    List<String> titles = new ArrayList<>();
    int colorAccent;
    int todayPosition = 0;
    Realm realm;
    RealmResults<DataReminder> reminders;
    List<DataReminder> allReminders = new ArrayList<>();
    ArrayList<Object> allItems = new ArrayList<>();
    ArrayList<Integer> datePositions = new ArrayList<>();
    String[] colors = {"#FFEE58", "#FF9700", "#F44336"};
    Resources resources;

    public AdapterReminders(Context context){
        this.context = context;
        resources = context.getResources();
        /*dates.put("0", "Today");
        dates.put("4", "Tomorrow");
        dates.put("9", "15 Feb 2020");
        titles.add("Sandra's Birthday Party");
        titles.add("Dinner at Hardee's");
        titles.add("Flutter Interact DSC CEME");
        titles.add("Meeting");
        titles.add("Raiding Area 51 and Recovering Alien Life");*/
        realm = RealmUtils.getRealm();
        reminders = realm.where(DataReminder.class).equalTo("deleted", false).findAll();
        allReminders.addAll(reminders);
        Collections.sort(allReminders);
        String previousDate = "";
        int position = 0;
        for(DataReminder reminder: allReminders){
            if(!reminder.getDate().equals(previousDate)){
                previousDate = reminder.getDate();
                DataReminderDate drd = new DataReminderDate(position, reminder.getDay(), reminder.getDate());
                allItems.add(drd);
                datePositions.add(position);
                position++;
            }
            allItems.add(reminder);
            position++;
        }

        colorAccent = context.getResources().getColor(R.color.colorAccent);
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder {
        TextView title, date, day;
        View v;
        public ViewHolderHeader(View v){
            super(v);
            this.v = v;
            title = v.findViewById(R.id.title);
            date = v.findViewById(R.id.date);
            day = v.findViewById(R.id.day);
        }

    }

    public class ViewHolderReminder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, people, description, time;
        ImageView image;
        View circle;
        public ViewHolderReminder(View v){
            super(v);
            circle = v.findViewById(R.id.circle);
            time = v.findViewById(R.id.time);
            title = v.findViewById(R.id.title);
            people = v.findViewById(R.id.people);
            description = v.findViewById(R.id.description);
            image = v.findViewById(R.id.image);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, ReminderActivity.class);
            i.putExtra("id", ((DataReminder)allItems.get(getAdapterPosition())).getReminderId());
            context.startActivity(i);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            View v = LayoutInflater.from(context).inflate(R.layout.item_reminder_header, parent, false);
            return new ViewHolderHeader(v);
        }
        else{
            View v = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
            return new ViewHolderReminder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolderHeader){
            ViewHolderHeader holderHeader = (ViewHolderHeader)holder;
            DataReminderDate reminderDate = (DataReminderDate)allItems.get(position);
            String title = getTitleFromDate(reminderDate.getDate());
            holderHeader.title.setText(title);
            holderHeader.day.setText(reminderDate.getDay());
            holderHeader.date.setText(reminderDate.getDate());
            holderHeader.v.setBackgroundColor(Color.parseColor("#999999"));
            if(title.equals("")){
                holderHeader.title.setVisibility(View.GONE);
            }
            else{
                holderHeader.title.setVisibility(View.VISIBLE);
                if(title.equals("Today")){
                    holderHeader.v.setBackgroundColor(colorAccent);
                }
            }
        }
        else{
            ViewHolderReminder holderReminder = (ViewHolderReminder)holder;
            DataReminder reminder = (DataReminder)allItems.get(position);

            if(reminder.getImage() != null && !reminder.getImage().equals("")){
                holderReminder.image.setVisibility(View.VISIBLE);
                Glide.with(context).asBitmap().load(reminder.getImage()).placeholder(R.drawable.imagepicker_image_placeholder).into(holderReminder.image);
            }
            else
                holderReminder.image.setVisibility(View.GONE);
            holderReminder.title.setText(reminder.getTitle());
            holderReminder.time.setText(reminder.getTime());
            holderReminder.people.setText(reminder.getOwner());
            Drawable drawable = resources.getDrawable(R.drawable.circle_white);
            drawable.setColorFilter(Color.parseColor(colors[reminder.getImportance()]), PorterDuff.Mode.SRC_ATOP);
            holderReminder.circle.setBackground(drawable);
            String descriptionS = reminder.getDescription();
            if(descriptionS == null || descriptionS.equals(""))
                holderReminder.description.setText("No description.");
            else
                holderReminder.description.setText(descriptionS);
            if(holderReminder.people.getVisibility()==View.VISIBLE){
                holderReminder.description.setMaxLines(1);
            }
            else{
                holderReminder.description.setMaxLines(2);
            }
        }
    }

    private String getTitleFromDate(String date) {
        Calendar now = Calendar.getInstance();
        Date d = now.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String today = sdf.format(d);
        now.add(Calendar.DATE, 1);
        d = now.getTime();
        String tomorrow = sdf.format(d);
        if(date.equals(today))
            return "Today";
        else if(date.equals(tomorrow))
            return "Tomorrow";
        else
            return "";
    }

    @Override
    public int getItemCount() {
        return allItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(datePositions.contains(position))
            return 0;
        else
            return 1;
    }

    public int getHeaderPositionForItem(int itemPosition) {
        if(itemPosition < 4)
            return 0;
        else if(itemPosition < 9)
            return 1;
        else
            return 2;
    }

    public int getTodayPosition(){
        return todayPosition;
    }

    /*public String getHeaderFromPosition(int p){
        int position = getHeaderPositionForItem(p);
        if(position == 0)
            return "Today";
        else if(position == 1)
            return "Tomorrow";
        else
            return "3 Weeks";
    }

    public String getDateFromPositon(int p){
        int position = getHeaderPositionForItem(p);
        if(position == 0)
            return "7 Jan 2020";
        else if(position == 1)
            return "8 Jan 2020";
        else
            return "15 Feb 2020";
    }

    public String getTitleFromPositon(int p){
        int position = getHeaderPositionForItem(p);
        if(position == 0)
            return "Today";
        else if(position == 1)
            return "Tomorrow";
        else
            return "Next Week";
    }*/

    /*@Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return createViewHolder(parent, 0);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolderHeader holderHeader = (ViewHolderHeader)holder;
        holderHeader.title.setText(getHeaderFromPosition(position));
        holderHeader.title.setVisibility(View.VISIBLE);
        if(position == 2){
            holderHeader.v.setBackgroundColor(colorAccent);
        }
        else{
            holderHeader.v.setBackgroundColor(Color.parseColor("#999999"));
        }
    }*/
}
