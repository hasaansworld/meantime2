package dot.albums;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import id.zelory.compressor.Compressor;
import jagerfield.mobilecontactslibrary.Contact.Contact;
import jagerfield.mobilecontactslibrary.ElementContainers.NumberContainer;
import jagerfield.mobilecontactslibrary.ImportContacts;

public class SuggestionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INFO = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_SUGGESTION = 2;
    private static final int TYPE_INVITE = 3;

    Context context;
    ProgressBar progressBar;
    boolean showInfo = false;
    int infoCount = 0, contactCount = 0, suggestionsCount = 0;

    ImportContacts importContacts;
    List<Contact> allContacts;
    List<AccountInfo> contactsFound = new ArrayList<>();
    List<User> allContactUsers = new ArrayList<>();

    boolean suggestionsDownloaded = false;

    public SuggestionsAdapter(Context context, boolean showInfo, ProgressBar progressBar){
        this.context = context;
        this.progressBar = progressBar;
        this.showInfo = showInfo;
        if(showInfo)
            infoCount = 1;

        importContacts = new ImportContacts((Activity)context);
        allContacts =importContacts.getContacts();
        new ContactsTask().execute();
    }

    public class ViewHolderInfo extends RecyclerView.ViewHolder{
        public ViewHolderInfo(View v){
            super(v);
        }
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder{
        TextView header;
        LinearLayout inviteLayout;
        public ViewHolderHeader(View v){
            super(v);
            header = v.findViewById(R.id.header);
            inviteLayout = v.findViewById(R.id.inviteLayout);
        }
    }

    public class ViewHolderSuggestion extends RecyclerView.ViewHolder{
        CircularImageView profilePicture;
        TextView username, follow;
        public ViewHolderSuggestion(View v){
            super(v);
            profilePicture = v.findViewById(R.id.profilePicture);
            username = v.findViewById(R.id.username);
            follow = v.findViewById(R.id.follow);
        }
    }

    public class ViewHolderInvite extends RecyclerView.ViewHolder{
        public ViewHolderInvite(View v){
            super(v);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if(viewType == TYPE_INFO) {
            v = LayoutInflater.from(context).inflate(R.layout.item_suggestion_info, parent, false);
            return new ViewHolderInfo(v);
        }
        else if(viewType == TYPE_HEADER){
            v = LayoutInflater.from(context).inflate(R.layout.item_suggestion_header, parent, false);
            return new ViewHolderHeader(v);
        }
        else if(viewType == TYPE_INVITE){
            v = LayoutInflater.from(context).inflate(R.layout.item_suggestion_invite, parent, false);
            return new ViewHolderInvite(v);
        }
        else{
            v = LayoutInflater.from(context).inflate(R.layout.item_suggestion, parent, false);
            return new ViewHolderSuggestion(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolderHeader){
            ViewHolderHeader holderHeader = (ViewHolderHeader)holder;
            if(position == infoCount){
                if(contactCount == 0)
                    holderHeader.inviteLayout.setVisibility(View.VISIBLE);
                else
                    holderHeader.inviteLayout.setVisibility(View.GONE);
                holderHeader.header.setText("Your Contacts");
            }
            else{
                holderHeader.inviteLayout.setVisibility(View.GONE);
                holderHeader.header.setText("Suggestions");
            }
        }
        else if(holder instanceof ViewHolderSuggestion){
            ViewHolderSuggestion holderSuggestion = (ViewHolderSuggestion)holder;
            int contactPosition = position - infoCount - 1;
            User user = allContactUsers.get(contactPosition);
            holderSuggestion.username.setText(user.getName());
            if(user.getProfilePic() == null)
                holderSuggestion.profilePicture.setImageResource(R.drawable.profile_picture);
            else{
                String path = Environment.getExternalStorageDirectory()+"/dotAlbums/"+user.getUsername()+"/Profile Pictures Thumbnails/"+user.getProfilePic();
                File file = new File(path);
                if(file.exists()) Glide.with(context).asBitmap().load(path).placeholder(R.drawable.imagepicker_image_placeholder).into(holderSuggestion.profilePicture);
                else
                    holderSuggestion.profilePicture.setImageResource(R.drawable.circular_place_holder);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 && showInfo)
            return TYPE_INFO;
        else if(position == infoCount || position == infoCount+contactCount+2)
            return TYPE_HEADER;
        else if(position == infoCount+contactCount+1)
            return TYPE_INVITE;
        else
            return TYPE_SUGGESTION;
    }

    @Override
    public int getItemCount() {
        if(!suggestionsDownloaded)
            return 1;
        else
        return infoCount + 2 + contactCount + suggestionsCount + 1;
    }

    public class ContactsTask extends AsyncTask<Void, Integer, Void>{
        DatabaseReference reference;
        List<String> allNumbers = new ArrayList<>();
        List<String> allNames = new ArrayList<>();
        int numbersChecked = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            reference = FirebaseDatabase.getInstance().getReference().child("account info");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for(Contact contact: allContacts){
                for(NumberContainer container: contact.getNumbers()){
                    String number = container.getNormalizedNumber();
                    if(!allNumbers.contains(number) && number.length() > 6) {
                        allNumbers.add(number);
                        allNames.add(contact.getDisplaydName());
                    }
                }
            }
            for (int i = 0; i < allNumbers.size(); i++){
                final String number = allNumbers.get(i);
                final String name = allNames.get(i);
                reference.child(number).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            contactsFound.add(dataSnapshot.getValue(AccountInfo.class));
                            User user = new User(number, name);
                            allContactUsers.add(user);
                        }
                        numbersChecked++;
                        if(numbersChecked == allNumbers.size()) {
                            makeUsers();
                            publishProgress(0);
                            downloadImages();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            return null;
        }

        private void makeUsers(){
            for(int i = 0; i < contactsFound.size(); i++){
                AccountInfo info = contactsFound.get(i);
                User user = allContactUsers.get(i);
                if(info.getUsername() != null) {
                    user.setUsername(Username.decode(info.getUsername()));
                    user.setProfilePic(info.getProfilePic());
                    allContactUsers.set(i, user);
                }
            }
            Collections.sort(allContactUsers);
        }

        private void downloadImages(){
            for(int i = 0; i < allContactUsers.size(); i++){
                User user = allContactUsers.get(i);
                final int in = i;
                if(user.getProfilePic() != null){
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("users").child(Username.encode(user.getUsername().substring(1))).child("profile picture").child(user.getProfilePic());
                    String dirPath = Environment.getExternalStorageDirectory()+"/dotAlbums/"+user.getUsername()+"/Profile Pictures";
                    File dirFile = new File(dirPath);
                    if(!dirFile.exists())
                        dirFile.mkdirs();
                    final String dirPath2 = Environment.getExternalStorageDirectory()+"/dotAlbums/"+user.getUsername()+"/Profile Pictures Thumbnails";
                    File dirFile2 = new File(dirPath2);
                    if(!dirFile2.exists())
                        dirFile2.mkdirs();
                    final File file = new File(dirPath+"/"+user.getProfilePic());
                    storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            int dp50 = dpToPixel(50, context);
                            try {
                                new Compressor(context)
                                        .setMaxWidth(dp50)
                                        .setMaxHeight(dp50)
                                        .setQuality(75)
                                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                                        .setDestinationDirectoryPath(dirPath2)
                                        .compressToFile(file);
                                int updatePosition = in+infoCount+1;
                                publishProgress(1, updatePosition);
                            }
                            catch (IOException e){

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(values[0] == 0){
                progressBar.setVisibility(View.GONE);
                suggestionsDownloaded = true;
                contactCount = contactsFound.size();
                notifyDataSetChanged();
            }
            if(values[0]==1){
                notifyItemChanged(values[1]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public static int dpToPixel(float dp, Context context){
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}
