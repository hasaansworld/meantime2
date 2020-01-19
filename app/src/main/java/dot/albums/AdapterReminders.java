package dot.albums;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterReminders extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    Context context;
    HashMap<String, String> dates = new HashMap<>();
    List<String> titles = new ArrayList<>();
    int colorAccent;
    int todayPosition = 0;

    public AdapterReminders(Context context){
        this.context = context;
        dates.put("0", "Today");
        dates.put("4", "Tomorrow");
        dates.put("9", "15 Feb 2020");
        titles.add("Sandra's Birthday Party");
        titles.add("Dinner at Hardee's");
        titles.add("Flutter Interact DSC CEME");
        titles.add("Meeting");
        titles.add("Raiding Area 51 and Recovering Alien Life");
        colorAccent = context.getResources().getColor(R.color.colorAccent);
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, date;
        View v;
        public ViewHolderHeader(View v){
            super(v);
            this.v = v;
            title = v.findViewById(R.id.title);
            date = v.findViewById(R.id.date);
        }

        @Override
        public void onClick(View v) {

        }

    }

    public class ViewHolderReminder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, people, description;
        ImageView image;
        public ViewHolderReminder(View v){
            super(v);
            title = v.findViewById(R.id.title);
            people = v.findViewById(R.id.people);
            description = v.findViewById(R.id.description);
            image = v.findViewById(R.id.image);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, ReminderActivity.class);
            i.putExtra("position", getAdapterPosition()%5);
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
            String title = getTitleFromPositon(position);
            holderHeader.title.setText(title);
            holderHeader.date.setText(getDateFromPositon(position));
            if(title.equals("")){
                holderHeader.title.setVisibility(View.GONE);
            }
            else{
                holderHeader.title.setVisibility(View.VISIBLE);
                if(position == todayPosition){
                    holderHeader.v.setBackgroundColor(colorAccent);
                    //holderHeader.title.setTextColor(colorAccent);
                }
                else{
                    holderHeader.v.setBackgroundColor(Color.parseColor("#999999"));
                    //holderHeader.title.setTextColor(Color.parseColor("#999999"));
                }
            }
        }
        else{
            ViewHolderReminder holderReminder = (ViewHolderReminder)holder;
            if(position%3 == 0){
                holderReminder.image.setVisibility(View.VISIBLE);
                Glide.with(context).asBitmap().load(R.drawable.sample).placeholder(R.drawable.imagepicker_image_placeholder).into(holderReminder.image);
            }
            else
                holderReminder.image.setVisibility(View.GONE);
            holderReminder.title.setText(titles.get(position%5));
            if(holderReminder.people.getVisibility()==View.VISIBLE){
                holderReminder.description.setMaxLines(1);
            }
            else{
                holderReminder.description.setMaxLines(2);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 14;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 || position == 4 || position == 9)
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

    public String getHeaderFromPosition(int p){
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
    }

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
