package app.meantime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;

public class AdapterCreateGroup extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<DataContact> contacts = new ArrayList<>();

    public AdapterCreateGroup(Context context){
        this.context = context;
        Realm realm = RealmUtils.getRealm();
        contacts.addAll(realm.where(DataContact.class).findAll());
        Collections.sort(contacts);
    }

    public class ViewHolderName extends RecyclerView.ViewHolder{

        public ViewHolderName(View v){
            super(v);
        }
    }

    public class ViewHolderContacts extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView profilePicture;
        TextView name, about;
        LinearLayout layout;

        public ViewHolderContacts(View v){
            super(v);
            name = v.findViewById(R.id.name);
            about = v.findViewById(R.id.about);
            profilePicture = v.findViewById(R.id.profilePicture);
            layout = v.findViewById(R.id.layout);
            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_contact_new_group, parent, false);
        if(viewType == 0){
            v = LayoutInflater.from(context).inflate(R.layout.item_group_name, parent, false);
            return new ViewHolderName(v);
        }
        return new ViewHolderContacts(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolderContacts){
            ViewHolderContacts holderContacts = (ViewHolderContacts) holder;
            DataContact contact = contacts.get(position-1);
            holderContacts.name.setText(contact.getName());
            holderContacts.about.setText(contact.getAbout());
            Glide.with(context).asBitmap().load(contact.getProfilePic()).placeholder(R.drawable.profile_picture).into(holderContacts.profilePicture);
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
