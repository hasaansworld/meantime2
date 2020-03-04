package dot.albums;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterGroups extends RecyclerView.Adapter<AdapterGroups.ViewHolder> {
    Context context;

    public AdapterGroups(Context context){
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView autoApprove;
        LinearLayout layout;

        public ViewHolder(View v){
            super(v);
            autoApprove = v.findViewById(R.id.auto_approve);
            layout = v.findViewById(R.id.layout);
            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            context.startActivity(new Intent(context, GroupActivity.class));
        }
    }

    @NonNull
    @Override
    public AdapterGroups.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterGroups.ViewHolder holder, int position) {
        if(position % 3 == 0) {
            holder.autoApprove.setBackgroundResource(R.drawable.auto_approve_background_selected);
            holder.autoApprove.setImageResource(R.drawable.auto_approve_icon_selected);
        }
        else{
            holder.autoApprove.setBackgroundResource(R.drawable.auto_approve_background_normal);
            holder.autoApprove.setImageResource(R.drawable.auto_approve_icon_normal);
        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }
}
