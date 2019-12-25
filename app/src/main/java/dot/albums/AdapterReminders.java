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
    int todayPosition = 6;

    public AdapterReminders(Context context){
        this.context = context;
        dates.put("0", "6 Jun 2019");
        dates.put("3", "Yesterday");
        dates.put("6", "Today");
        dates.put("10", "Tomorrow");
        dates.put("15", "7 Dec 2019");
        titles.add("Sandra's Birthday Party");
        titles.add("Dinner at Hardee's");
        titles.add("Flutter Interact DSC CEME");
        titles.add("Meeting");
        titles.add("Raiding Area 51 and Recovering Alien Life");
        colorAccent = context.getResources().getColor(R.color.colorAccent);
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, day;
        View v;
        public ViewHolderHeader(View v){
            super(v);
            this.v = v;
            title = v.findViewById(R.id.title);
            title.setTextColor(Color.WHITE);
            day = v.findViewById(R.id.day);
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
            holderHeader.title.setText(dates.get(Integer.toString(position)));
            holderHeader.day.setText(getDayFromPositon(position));
            if(position == todayPosition){
                holderHeader.v.setBackgroundColor(colorAccent);
            }
            else{
                holderHeader.v.setBackgroundColor(Color.parseColor("#999999"));
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
        return 23;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 || position == 3 || position == 6 || position == 10 || position == 15)
            return 0;
        else
            return 1;
    }

    public int getHeaderPositionForItem(int itemPosition) {
        if(itemPosition < 3)
            return 0;
        else if(itemPosition < 6)
            return 1;
        else if(itemPosition < 10)
            return 2;
        else if(itemPosition < 15)
            return 3;
        else
            return 4;
    }

    public int getTodayPosition(){
        return todayPosition;
    }

    public String getHeaderFromPosition(int p){
        int position = getHeaderPositionForItem(p);
        if(position == 0)
            return "6 Jun 2019";
        else if(position == 1)
            return "Yesterday";
        else if(position == 2)
            return "Today";
        else if(position == 3)
            return "Tomorrow";
        else
            return "7 Dec 2019";
    }

    public String getDayFromPositon(int p){
        int position = getHeaderPositionForItem(p);
        if(position == 0)
            return "Monday";
        else if(position == 1)
            return "Tuesday, 24 Dec";
        else if(position == 2)
            return "Wednesday, 25 Dec";
        else if(position == 3)
            return "Thursday, 26 Dec";
        else
            return "Friday";
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
