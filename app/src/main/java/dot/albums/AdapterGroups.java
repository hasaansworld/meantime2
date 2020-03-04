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

public class AdapterGroups extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;

    public AdapterGroups(Context context){
        this.context = context;
    }

    public class ViewHolderGroup extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView autoApprove;
        LinearLayout layout;

        public ViewHolderGroup(View v){
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

    public class ViewHolderNew extends RecyclerView.ViewHolder{

        public ViewHolderNew(View v){
            super(v);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_group_add, parent, false);
            return new ViewHolderNew(v);
        }
        else {
            View v = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
            return new ViewHolderGroup(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolderGroup) {
            ViewHolderGroup holderGroup = (ViewHolderGroup) holder;
            if (position % 3 == 0) {
                holderGroup.autoApprove.setBackgroundResource(R.drawable.auto_approve_background_selected);
                holderGroup.autoApprove.setImageResource(R.drawable.auto_approve_icon_selected);
            } else {
                holderGroup.autoApprove.setBackgroundResource(R.drawable.auto_approve_background_normal);
                holderGroup.autoApprove.setImageResource(R.drawable.auto_approve_icon_normal);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 7;
    }
}
