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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
    String[] colors = {"#FFEE58", "#FF9700", "#F44336"};
    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    Resources resources;
    String day, today, tomorrow, yesterday;
    int mode = 0;
    int filter = -1;
    boolean isSearching = false;
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
        TextView title, people, description, time, repeat;
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
            repeat = v.findViewById(R.id.repeat);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, ReminderActivity.class);
            i.putExtra("id", ((DataReminder)allItems.get(getAdapterPosition())).getReminderId());
            if(mode == 1)
                i.putExtra("isHistory", true);
            else if(mode == 2)
                i.putExtra("isDeleted", true);
            context.startActivity(i);
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
            holderReminder.repeat.setVisibility(reminder.getRepeat().equals("No repeat") ? View.GONE : View.VISIBLE);
            if(!reminder.getRepeat().equals("No repeat"))
                holderReminder.repeat.setText(getRepeatTitle(reminder.getRepeat()));
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
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

}
