package dot.albums;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterPeople extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;

    public AdapterPeople(Context context){
        this.context = context;
    }

    public class ViewHolderPeople extends RecyclerView.ViewHolder{
        ImageView autoApprove;

        public ViewHolderPeople(View v){
            super(v);
            autoApprove = v.findViewById(R.id.auto_approve);
        }
    }

    public class ViewHolderContacts extends RecyclerView.ViewHolder{

        public ViewHolderContacts(View v){
            super(v);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_contact_contacts, parent, false);
            return new ViewHolderContacts(v);
        }
        else {
            View v = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
            return new ViewHolderPeople(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolderPeople) {
            ViewHolderPeople holderPeople = (ViewHolderPeople) holder;
            if (position % 3 == 0) {
                holderPeople.autoApprove.setBackgroundResource(R.drawable.auto_approve_background_selected);
                holderPeople.autoApprove.setImageResource(R.drawable.auto_approve_icon_selected);
            } else {
                holderPeople.autoApprove.setBackgroundResource(R.drawable.auto_approve_background_normal);
                holderPeople.autoApprove.setImageResource(R.drawable.auto_approve_icon_normal);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
