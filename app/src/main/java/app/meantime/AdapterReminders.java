package app.meantime;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.florent37.viewtooltip.ViewTooltip;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

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
    List<DataReminder> displayReminders = new ArrayList<>();
    ArrayList<Object> allItems = new ArrayList<>();
    int[] circles = {R.drawable.circle_yellow, R.drawable.circle_orange, R.drawable.circle_red};
    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    Resources resources;
    String day, today, tomorrow, yesterday;
    int mode = 0;
    int filter = -1;
    boolean isSearching = false;
    List<Boolean> selectedItems = new ArrayList<>();
    int selectCount = 0;
    int selectableItemBackground;
    OnItemSelectedListener listener;
    long timeInMillis = 0;
    //UnifiedNativeAd ad;
    //int adPostion = -1;

    public AdapterReminders(Context context, int mode){
        this.context = context;
        resources = context.getResources();

        realm = RealmUtils.getRealm();

        this.mode = mode;
        getTodayAndTomorrow();
        if(mode == 0 || mode == 2) {
            reminders = realm.where(DataReminder.class).equalTo("deleted", mode != 0).findAll();
            allReminders.addAll(reminders);
            Collections.sort(allReminders);

            if(mode == 0)
                removeOldReminders();
        }
        else if(mode == 1){
            reminders = realm.where(DataReminder.class).equalTo("deleted", false).findAll();
            allReminders.addAll(reminders);
            Collections.sort(allReminders);
            Collections.reverse(allReminders);

            removeNewReminders();
        }
        displayReminders.addAll(allReminders);
        filterAndArrange(filter);

        colorAccent = context.getResources().getColor(R.color.colorAccent);
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackground});
        selectableItemBackground = typedArray.getResourceId(0, 0);
        typedArray.recycle();
    }

    private void filterAndArrange(int filter){
        String previousDate = "";
        int position = 0;
        allItems.clear();
        titles.clear();
        if(mode == 0){
            previousDate = today;
            DataReminderDate drd = new DataReminderDate(position, day, today);
            allItems.add(drd);
            titles.add("Today");
            position++;
            if(displayReminders.size() == 0 || !displayReminders.get(0).getDate().equals(today)) {
                allItems.add(null);
                position++;
                titles.add("Today");
            }
        }
        for(DataReminder reminder: displayReminders){
            if(filter == -1 || reminder.getImportance() == filter) {
                String title = getTitleFromDate(reminder.getDate());
                if (!reminder.getDate().equals(previousDate)) {
                    if(position == 1) {
                        allItems.add(null);
                        position++;
                        titles.add("Today");
                    }
                    previousDate = reminder.getDate();
                    DataReminderDate drd = new DataReminderDate(position, reminder.getDay(), reminder.getDate());
                    allItems.add(drd);
                    position++;
                    titles.add(title);
                }
                titles.add(title);
                allItems.add(reminder);
                position++;
            }
        }
        if(allItems.size() == 1) {
            allItems.add(null);
            titles.add("Today");
        }
        selectedItems.clear();
        for(int i = 0; i < allItems.size(); i++)
            selectedItems.add(false);
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

    public class ViewHolderReminder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        LinearLayout layout;
        TextView title, people, description, time, repeat;
        ImageView image, circle;

        public ViewHolderReminder(View v){
            super(v);
            layout = v.findViewById(R.id.layout);
            circle = v.findViewById(R.id.circle);
            time = v.findViewById(R.id.time);
            title = v.findViewById(R.id.title);
            people = v.findViewById(R.id.people);
            description = v.findViewById(R.id.description);
            image = v.findViewById(R.id.image);
            repeat = v.findViewById(R.id.repeat);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(selectCount == 0 || isSearching) {
                Intent i = new Intent(context, ReminderActivity.class);
                i.putExtra("id", ((DataReminder) allItems.get(getAdapterPosition())).getReminderId());
                if (mode == 1)
                    i.putExtra("isHistory", true);
                else if (mode == 2)
                    i.putExtra("isDeleted", true);
                context.startActivity(i);
            }
            else{
                selectDeselect();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(!isSearching)
                selectDeselect();
            return !isSearching;
        }

        private void selectDeselect(){
            int position = getAdapterPosition();
            Boolean selected = selectedItems.get(position);
            selectedItems.set(position, !selected);
            notifyItemChanged(position);
            int prevSelectCount = selectCount;
            selectCount = selected ? selectCount-1 : selectCount+1;
            if(listener != null) {
                listener.onUpdate(selectCount);
                if (selectCount == 0 && prevSelectCount == 1)
                    listener.onEnd();
                else if (selectCount == 1 && prevSelectCount == 0)
                    listener.onStart();
            }
        }

    }

    public class ViewHolderNone extends RecyclerView.ViewHolder{

        public ViewHolderNone(View v){
            super(v);
        }
    }

    /*public class ViewHolderAds extends RecyclerView.ViewHolder{
        View v;
        UnifiedNativeAdView adView;
        ImageView appIcon;
        TextView adHeadline, adAdvertiser, adBody, adPrice, adStore;
        RatingBar adStars;
        TextView adCallToAction;
        //MediaView adMediaView;
        public ViewHolderAds(View v){
            super(v);
            this.v = v;
            adView = v.findViewById(R.id.ad_view);
            adHeadline = adView.findViewById(R.id.ad_headline);
            adAdvertiser = adView.findViewById(R.id.ad_advertiser);
            adBody = adView.findViewById(R.id.ad_body);
            adPrice = adView.findViewById(R.id.ad_price);
            adStore = adView.findViewById(R.id.ad_store);
            adStars = adView.findViewById(R.id.ad_stars);
            adCallToAction = adView.findViewById(R.id.ad_call_to_action);
            //adMediaView = adView.findViewById(R.id.ad_media);
            appIcon = adView.findViewById(R.id.ad_app_icon);
            adView.setHeadlineView(adHeadline);
            adView.setIconView(appIcon);
            adView.setAdvertiserView(adAdvertiser);
            adView.setBodyView(adBody);
            adView.setPriceView(adPrice);
            adView.setStoreView(adStore);
            adView.setStarRatingView(adStars);
            adView.setCallToActionView(adCallToAction);
            //adView.setMediaView(adMediaView);
        }
    }*/

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            View v = LayoutInflater.from(context).inflate(R.layout.item_reminder_header, parent, false);
            return new ViewHolderHeader(v);
        }
        else if(viewType == 1){
            View v = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
            return new ViewHolderReminder(v);
        }
        /*else if(viewType == 3){
            View v = LayoutInflater.from(context).inflate(R.layout.item_ads, parent, false);
            return new ViewHolderAds(v);
        }*/
        else{
            View v = LayoutInflater.from(context).inflate(R.layout.item_reminder_none, parent, false);
            return new ViewHolderNone(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolderHeader){
            ViewHolderHeader holderHeader = (ViewHolderHeader)holder;
            DataReminderDate reminderDate = (DataReminderDate)allItems.get(position);
            String title = "";
            if(position == 0 || position > 0 && !titles.get(position).equals(titles.get(position-1)))
                title = getTitleFromDate(reminderDate.getDate());
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
        else if (holder instanceof ViewHolderReminder){
            ViewHolderReminder holderReminder = (ViewHolderReminder)holder;
            DataReminder reminder = (DataReminder)allItems.get(position);

            if(reminder.getImage() != null && !reminder.getImage().equals("")){
                holderReminder.image.setVisibility(View.VISIBLE);
                Glide.with(context).asBitmap().load(reminder.getImage()).placeholder(R.drawable.broken_image).into(holderReminder.image);
            }
            else
                holderReminder.image.setVisibility(View.GONE);
            holderReminder.title.setText(reminder.getTitle());
            if(mode == 0)
                holderReminder.title.setTextColor(reminder.getStatus() != DataReminder.STATUS_COMPLETED ? Color.BLACK : Color.parseColor("#666666"));
            holderReminder.time.setText(reminder.getTime());
            holderReminder.people.setText(reminder.getOwner());
            holderReminder.circle.setImageResource(circles[reminder.getImportance()]);
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
            holderReminder.repeat.setVisibility(reminder.getRepeat().equals("No repeat") ? View.GONE : View.VISIBLE);
            if(!reminder.getRepeat().equals("No repeat"))
                holderReminder.repeat.setText(getRepeatTitle(reminder.getRepeat()));

            if(selectCount > 0 && selectedItems.get(position)){
                holderReminder.layout.setBackgroundColor(Color.parseColor("#E6FFE7"));
            }
            else{
                holderReminder.layout.setBackgroundResource(selectableItemBackground);
            }
        }
        /*else if(holder instanceof ViewHolderAds){
            ViewHolderAds holderAds = (ViewHolderAds)holder;
            holderAds.v.setVisibility(ad != null ? View.VISIBLE : View.GONE);
            if(ad != null) {
                holderAds.adHeadline.setText(ad.getHeadline());
                holderAds.adAdvertiser.setText(ad.getAdvertiser());
                if(ad.getIcon() != null) {
                    holderAds.appIcon.setVisibility(View.VISIBLE);
                    holderAds.appIcon.setImageDrawable(ad.getIcon().getDrawable());
                }
                else
                    holderAds.appIcon.setVisibility(View.GONE);
                if(ad.getStarRating() != null){
                    holderAds.adStars.setRating(ad.getStarRating().floatValue());
                    holderAds.adStars.setVisibility(View.VISIBLE);
                }
                else
                    holderAds.adStars.setVisibility(View.GONE);
                holderAds.adBody.setText(ad.getBody());
                if(ad.getPrice() != null && !ad.getPrice().equals("")){
                    holderAds.adPrice.setVisibility(View.VISIBLE);
                    holderAds.adPrice.setText(ad.getPrice());
                }
                else
                    holderAds.adPrice.setVisibility(View.GONE);
                if(ad.getStore() != null && !ad.getStore().equals("")){
                    holderAds.adStore.setVisibility(View.VISIBLE);
                    holderAds.adStore.setText(ad.getStore());
                }
                else
                    holderAds.adStore.setVisibility(View.GONE);
                holderAds.adCallToAction.setText(ad.getCallToAction());
                //holderAds.adView.setNativeAd(ad);
            }
        }*/
    }

    private void removeOldReminders(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        int todayIndex = -1;
        for(int i = 0; i < allReminders.size(); i++){
            DataReminder reminder = allReminders.get(i);
            try {
                Date d = sdf.parse(today);
                Date d2 = sdf.parse(reminder.getDate());
                if(d2.getTime() >= d.getTime()) {
                    todayIndex = i;
                    break;
                }
            }
            catch (ParseException e){
                // Couldn't parse, maybe ignore?
            }
        }
        if(todayIndex == -1)
            allReminders.clear();
        else
            allReminders = allReminders.subList(todayIndex, allReminders.size());
    }

    private void removeNewReminders(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
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
            allReminders.clear();
        else
            allReminders = allReminders.subList(todayIndex, allReminders.size());
    }

    private void getTodayAndTomorrow(){
        Calendar now = Calendar.getInstance();
        Date d = now.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        today = sdf.format(d);
        day = days[now.get(Calendar.DAY_OF_WEEK)-1];
        now.add(Calendar.DATE, 1);
        d = now.getTime();
        tomorrow = sdf.format(d);
        now.add(Calendar.DATE, -2);
        d = now.getTime();
        yesterday = sdf.format(d);
    }

    private String getTitleFromDate(String date) {
        if(date.equals(today))
            return "Today";
        else if(date.equals(tomorrow))
            return "Tomorrow";
        else if(date.equals(yesterday))
            return "Yesterday";
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            Calendar now = Calendar.getInstance();
            try {
                Date d = sdf.parse(date);
                Calendar rem = Calendar.getInstance();
                rem.setTime(d);
                if(rem.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                    if(rem.get(Calendar.MONTH) == now.get(Calendar.MONTH)+1)
                        return "Next Month";
                    else if(rem.get(Calendar.MONTH) == now.get(Calendar.MONTH)-1)
                        return "Last Month";
                    else if (rem.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR))
                        return "This Week";
                    else if (rem.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)+1)
                        return "Next Week";
                    else if (rem.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)-1)
                        return "Last Week";
                }
                else if(rem.get(Calendar.YEAR) == now.get(Calendar.YEAR)+1)
                    return "Next Year";
                else if(rem.get(Calendar.YEAR) == now.get(Calendar.YEAR)-1)
                    return "Last Year";
            }
            catch (ParseException e){

            }
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return allItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object object = allItems.get(position);
        if(object == null)
            return 2;
        else if(object instanceof DataReminderDate)
            return 0;
            //else if(object instanceof DataAds)
            //return 3;
        else
            return 1;
    }

    public void setFilter(int filter){
        displayReminders.clear();
        displayReminders.addAll(allReminders);
        filterAndArrange(filter);
        notifyDataSetChanged();
        this.filter = filter;
    }

    public void search(String query){
        query = query.toLowerCase().trim();
        displayReminders.clear();
        for(int i = 0; i < allReminders.size(); i++){
            DataReminder reminder = allReminders.get(i);
            if(reminder.getTitle().toLowerCase().contains(query)
                    || reminder.getDescription() != null && reminder.getDescription().toLowerCase().contains(query)
                    || reminder.getDate().toLowerCase().contains(query)
                    || getFullMonth(reminder.getDate()).toLowerCase().contains(query)
                    || reminder.getTime().toLowerCase().contains(query)
                    || getTitleFromDate(reminder.getDate()).toLowerCase().contains(query))
                displayReminders.add(reminder);
        }
        int prevMode = mode;
        if(!query.equals("today"))
            mode = -1;
        filterAndArrange(filter);
        mode = prevMode;
        isSearching = true;
        notifyDataSetChanged();
    }

    public void cancelSearch(){
        displayReminders.clear();
        displayReminders.addAll(allReminders);
        isSearching = false;
        filterAndArrange(filter);
        notifyDataSetChanged();
    }

    /*public void showAd(UnifiedNativeAd ad){
        this.ad = ad;
        if(ad != null) {
            if (!titles.contains("Ad") && mode == 0 && !isSearching) {
                int lastTodayPosition = 0;
                for (String title : titles)
                    if (title.equals("Today"))
                        lastTodayPosition++;

                allItems.add(lastTodayPosition, new DataAds());
                titles.add(lastTodayPosition, "Ad");
                notifyItemInserted(lastTodayPosition);
            }
        }
        //if(adPostion != -1)
            //notifyItemChanged(adPostion);
    }*/

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

    String[] short_months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    String[] full_months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private String getFullMonth(String date){
        String updatedDate = date;
        for(int i = 0; i < 12; i++){
            updatedDate = updatedDate.replace(short_months[i], full_months[i]);
        }
        return updatedDate;
    }

//    public void removeItem(int position){
//        allItems.remove(position);
//        titles.remove(position);
//        notifyItemRemoved(position);
//        Object prevItem = allItems.get(position-1);
//        Object nextItem = null;
//        if(position != allItems.size())
//            nextItem = allItems.get(position);
//        if(mode == 0 && position == 1 && ((DataReminderDate)prevItem).getDate().equals(today)){
//            allItems.add(1, null);
//            titles.add("Today");
//            notifyItemInserted(1);
//        }
//        else if(position == 1 || position == getItemCount() && prevItem instanceof DataReminderDate || prevItem instanceof DataReminderDate && nextItem instanceof DataReminderDate) {
//            allItems.remove(position-1);
//            titles.remove(position-1);
//            notifyItemRemoved(position-1);
//        }
//    }

    public void clearSelections(){
        for(int i = 0; i < allItems.size(); i++){
            if(selectedItems.get(i)){
                selectedItems.set(i, false);
                notifyItemChanged(i);
            }
        }
        selectCount = 0;
    }

    public void deleteSelections(int mode){
        for(int i = allItems.size()-1; i >= 0; i--){
            if(selectedItems.get(i)){
                DataReminder reminder = (DataReminder)allItems.get(i);
                realm.beginTransaction();
                if(mode == 0) {
                    reminder.setDeleted(true);
                    if(reminder.getStatus() == DataReminder.STATUS_SCHEDULED)
                        reminder.setStatus(DataReminder.STATUS_CREATED);
                }
                else if(mode == 1 || mode == 2)
                    reminder.deleteFromRealm();
                else if(mode == 3)
                    reminder.setDeleted(false);
                realm.commitTransaction();
                removeItem(i);
                if(mode == 0)
                    cancelScheduling(reminder);
                else if(mode == 3)
                    reschedule(reminder);
            }
        }
        for (int i = allItems.size()-1; i >= 0; i--){
            if(i == allItems.size()-1 && allItems.get(i) instanceof DataReminderDate || i == 0 && allItems.get(1) instanceof DataReminderDate){
                if(mode == 0 && i == 0){
                    allItems.add(i+1, null);
                    titles.add(i+1, "Today");
                    selectedItems.add(false);
                    notifyItemInserted(1);
                }
                else{
                    removeItem(i);
                }
            }
            else if(allItems.get(i) instanceof DataReminderDate && allItems.get(i+1) instanceof DataReminderDate){
                removeItem(i);
            }
        }
        selectCount = 0;
    }

    private void removeItem(int position){
        allItems.remove(position);
        titles.remove(position);
        selectedItems.remove(position);
        notifyItemRemoved(position);
    }

    private void cancelScheduling(DataReminder reminder){
        Intent intent1 = new Intent(context.getApplicationContext(), NotificationReceiver.class);
        intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
        intent1.putExtra("id", reminder.getReminderId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), reminder.getReminderNumber(), intent1,
                0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void reschedule(DataReminder reminder){
        if(reminder.getStatus() == DataReminder.STATUS_CREATED && shouldSchedule(reminder))
            scheduleReminder(reminder);
    }


    public boolean shouldSchedule(DataReminder reminder){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);

        try {
            Date reminderDate = simpleDateFormat.parse(reminder.getDate() + " " + reminder.getTime());
            reminderDate = getDisplayTime(reminderDate, reminder);
            Date now = Calendar.getInstance().getTime();
            timeInMillis = reminderDate.getTime();
            long differenceInMillis = reminderDate.getTime() - now.getTime();
            long differenceInMinutes = TimeUnit.MINUTES.convert(differenceInMillis, TimeUnit.MILLISECONDS);

            return differenceInMinutes <= 40;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Date getDisplayTime(Date date, DataReminder reminder){
        String displayTime = reminder.getAlarmtime();
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

    public void scheduleReminder(DataReminder reminder){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent1 = new Intent(context.getApplicationContext(), NotificationReceiver.class);
        intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
        intent1.putExtra("id", reminder.getReminderId());

        int requestCode = reminder.getReminderNumber();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), requestCode, intent1,
                0);
        if (Build.VERSION.SDK_INT >= 23)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else if (Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);

        realm.beginTransaction();
        reminder.setStatus(DataReminder.STATUS_SCHEDULED);
        realm.copyToRealmOrUpdate(reminder);
        realm.commitTransaction();
    }


    public void setOnItemSelectedListener(OnItemSelectedListener listener){
        this.listener = listener;
    }

    public interface OnItemSelectedListener{
        public void onStart();
        public void onEnd();
        public void onUpdate(int count);
    }
}
