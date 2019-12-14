package dot.albums;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.HashMap;

public class AdapterReminders extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    Context context;
    HashMap<String, String> dates = new HashMap<>();
    int colorAccent;

    public AdapterReminders(Context context){
        this.context = context;
        dates.put("0", "Today");
        dates.put("4", "Tomorrow");
        dates.put("11", "7 Dec 2019");
        colorAccent = context.getResources().getColor(R.color.colorAccent);
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder{
        TextView date;
        View v;
        public ViewHolderHeader(View v){
            super(v);
            this.v = v;
            date = v.findViewById(R.id.date);
        }
    }

    public class ViewHolderReminder extends RecyclerView.ViewHolder{
        TextView title, people, description;
        ImageView image;
        public ViewHolderReminder(View v){
            super(v);
            title = v.findViewById(R.id.title);
            people = v.findViewById(R.id.people);
            description = v.findViewById(R.id.description);
            image = v.findViewById(R.id.image);
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
            holderHeader.date.setText(dates.get(Integer.toString(position)));
            if(position == 0){
                holderHeader.v.setBackgroundColor(colorAccent);
                holderHeader.date.setTextColor(Color.WHITE);
            }
            else{
                holderHeader.v.setBackgroundColor(Color.parseColor("#999999"));
                holderHeader.date.setTextColor(Color.WHITE);
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
        return 17;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 || position == 4 || position == 11)
            return 0;
        else
            return 1;
    }
}
