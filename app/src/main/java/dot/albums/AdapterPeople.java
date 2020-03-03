package dot.albums;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterPeople extends RecyclerView.Adapter<AdapterPeople.ViewHolder> {
    Context context;

    public AdapterPeople(Context context){
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView autoApprove;

        public ViewHolder(View v){
            super(v);
            autoApprove = v.findViewById(R.id.auto_approve);
        }
    }

    @NonNull
    @Override
    public AdapterPeople.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPeople.ViewHolder holder, int position) {
        if(position % 3 == 0) {
            holder.autoApprove.setBackgroundResource(R.drawable.auto_approve_on_background);
            holder.autoApprove.setImageResource(R.drawable.ic_done_all_white_18dp);
        }
        else{
            holder.autoApprove.setBackgroundResource(R.drawable.auto_approve_background);
            holder.autoApprove.setImageResource(R.drawable.ic_done_all_accent_18dp);
        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }
}
