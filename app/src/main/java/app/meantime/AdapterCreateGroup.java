package app.meantime;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.button.MaterialButton;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;

public class AdapterCreateGroup extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<DataContact> contacts = new ArrayList<>();
    List<Boolean> selected = new ArrayList<>();
    View root;
    TextView toolbarTitle;
    MaterialButton createGroup;
    String groupName = "";
    EmojiPopup.Builder emojiPopupBuilder;
    int selectCount = 0;

    public AdapterCreateGroup(Context context, View root, TextView toolbarTitle, MaterialButton createGroup){
        this.context = context;
        this.root = root;
        this.toolbarTitle = toolbarTitle;
        this.createGroup = createGroup;
        emojiPopupBuilder = EmojiPopup.Builder.fromRootView(root);
        Realm realm = RealmUtils.getRealm();
        contacts.addAll(realm.where(DataContact.class).findAll());
        Collections.sort(contacts);
        for(int i = 0; i < contacts.size(); i++)
            selected.add(false);
    }

    public class ViewHolderName extends RecyclerView.ViewHolder{
        EmojiEditText name;
        ImageView emojiGroupName;

        public ViewHolderName(View v){
            super(v);
            name = v.findViewById(R.id.name);
            emojiGroupName = v.findViewById(R.id.emoji_group_name);
            initializeEmoji();
            name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    groupName = s.toString();
                }
            });
        }

        public void initializeEmoji(){
            emojiPopupBuilder.setOnEmojiPopupDismissListener(() -> emojiGroupName.setImageResource(R.drawable.outline_sentiment_satisfied_black_24));
            EmojiPopup emojiPopup = emojiPopupBuilder.build(name);
            emojiGroupName.setOnClickListener(v -> {
                emojiGroupName.setImageResource(emojiPopup.isShowing() ? R.drawable.outline_sentiment_satisfied_black_24 : R.drawable.ic_keyboard_black_24dp);
                emojiPopup.toggle();
            });
        }
    }

    public class ViewHolderContacts extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView profilePicture, checked;
        TextView name, about;
        LinearLayout layout, backgroundLayout;

        public ViewHolderContacts(View v){
            super(v);
            name = v.findViewById(R.id.name);
            about = v.findViewById(R.id.about);
            profilePicture = v.findViewById(R.id.profilePicture);
            checked = v.findViewById(R.id.checked);
            layout = v.findViewById(R.id.layout);
            backgroundLayout = v.findViewById(R.id.background_layout);
            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            boolean previousSelection = selected.get(position-1);
            selected.set(position-1, !previousSelection);
            notifyItemChanged(position);
            if(previousSelection)
                selectCount--;
            else
                selectCount++;
            if(selectCount == 0){
                toolbarTitle.setText("Create Group");
                createGroup.setVisibility(View.GONE);
            }
            else{
                createGroup.setVisibility(View.VISIBLE);
                if(selectCount == 1)
                    toolbarTitle.setText("1 participant");
                else
                    toolbarTitle.setText(selectCount+" Participants");
            }

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
            if(selected.get(position-1)) {
                holderContacts.profilePicture.setImageResource(0);
                holderContacts.checked.setVisibility(View.VISIBLE);
                holderContacts.backgroundLayout.setBackgroundColor(Color.parseColor("#E3F2FD"));
            }
            else {
                Glide.with(context).asBitmap().load(contact.getProfilePic()).placeholder(R.drawable.profile_picture).into(holderContacts.profilePicture);
                holderContacts.checked.setVisibility(View.GONE);
                holderContacts.backgroundLayout.setBackgroundColor(Color.WHITE);
            }
        }
        else if(holder instanceof ViewHolderName){
            ViewHolderName holderName = (ViewHolderName) holder;
            holderName.name.setText(groupName);
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
