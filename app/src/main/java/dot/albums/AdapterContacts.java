package dot.albums;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.zelory.compressor.Compressor;
import io.realm.Realm;
import io.realm.RealmQuery;
import jagerfield.mobilecontactslibrary.Contact.Contact;
import jagerfield.mobilecontactslibrary.ElementContainers.NumberContainer;
import jagerfield.mobilecontactslibrary.ImportContacts;

public class AdapterContacts extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    Realm realm;
    ProgressBar progressBar;

    ImportContacts importContacts;
    List<Contact> allContacts;
    List<DataContact> contactsFound = new ArrayList<>();
    List<String> existingNumbers = new ArrayList<>();
    String appName, thumbnailPath;

    public AdapterContacts(Context context, ProgressBar progressBar) {
        this.context = context;
        this.progressBar = progressBar;
        appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();

        realm = RealmUtils.getRealm();

        //importContacts = new ImportContacts((Activity) context);
        //allContacts = importContacts.getContacts();
        contactsFound.addAll(realm.where(DataContact.class).findAll());
        Collections.sort(contactsFound);
        /*for (DataContact contact : contactsFound)
            existingNumbers.add(contact.getPhoneNumber());
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected())
            new ContactsTask().execute();
        else
            progressBar.setVisibility(View.GONE);*/

    }

    public class ViewHolderInvite extends RecyclerView.ViewHolder {

        public ViewHolderInvite(View v) {
            super(v);
        }
    }

    public class ViewHolderContact extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout layout;
        TextView name, about, count;
        ImageView profilePicture, autoApprove;
        public ViewHolderContact(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            about = v.findViewById(R.id.about);
            profilePicture = v.findViewById(R.id.profilePicture);
            autoApprove = v.findViewById(R.id.auto_approve);
            count = v.findViewById(R.id.count);
            count.setVisibility(View.GONE);
            layout = v.findViewById(R.id.layout);
            layout.setOnClickListener(this);
            autoApprove.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if(v.equals(autoApprove)){
                DataContact contact = contactsFound.get(getAdapterPosition());
                DataContact searchedContact = realm.where(DataContact.class).equalTo("phoneNumber", contact.getPhoneNumber()).findFirst();
                if(searchedContact != null) {
                    realm.beginTransaction();
                    contact.setAutoApprove(!contact.isAutoApprove());
                    realm.commitTransaction();
                    autoApprove.setBackgroundResource(contact.isAutoApprove() ? R.drawable.auto_approve_on_background : R.drawable.auto_approve_background_normal);
                    autoApprove.setImageResource(contact.isAutoApprove() ? R.drawable.auto_approve_icon_selected : R.drawable.auto_approve_icon_normal);
                }
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (viewType == getItemCount()-1) {
            v = LayoutInflater.from(context).inflate(R.layout.item_invite, parent, false);
            return new ViewHolderInvite(v);
        } else {
            v = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
            return new ViewHolderContact(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof ViewHolderContact){
                ViewHolderContact holderContact = (ViewHolderContact) holder;
                DataContact contact = contactsFound.get(position);
                holderContact.name.setText(contact.getName());
                holderContact.about.setText(contact.getAbout());
                holderContact.autoApprove.setBackgroundResource(contact.isAutoApprove() ? R.drawable.auto_approve_on_background : R.drawable.auto_approve_background_normal);
                holderContact.autoApprove.setImageResource(contact.isAutoApprove() ? R.drawable.auto_approve_icon_selected : R.drawable.auto_approve_icon_normal);
                Glide.with(context).asBitmap().load(contact.getProfilePic()).placeholder(R.drawable.profile_picture).into(((ViewHolderContact) holder).profilePicture);
            }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return contactsFound.size() == 0 ? 0 : contactsFound.size()+1;
    }

    /*public class ContactsTask extends AsyncTask<Void, Integer, Void> {
        DatabaseReference reference;
        List<String> allNumbers = new ArrayList<>();
        List<String> allNames = new ArrayList<>();
        List<DataContact> newList = new ArrayList<>();
        int numbersChecked = 0;
        int count = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            newList.addAll(contactsFound);
            reference = FirebaseDatabase.getInstance().getReference().child("users");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (Contact contact : allContacts) {
                for (NumberContainer container : contact.getNumbers()) {
                    String number = container.getNormalizedNumber();
                    if (!allNumbers.contains(number) && number.length() > 6) {
                        allNumbers.add(number);
                        allNames.add(contact.getDisplaydName());
                    }
                }
            }
            for (int i = 0; i < allNumbers.size(); i++) {
                final String number = allNumbers.get(i);
                final String name = allNames.get(i);
                reference.child(number).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            count++;
                            DataContact contact = dataSnapshot.getValue(DataContact.class);
                            if(contact != null) {
                                contact.setName(name);
                                contact.setPhoneNumber(number);
                                if(contact.getProfilePic() == null)
                                    contact.setProfilePic("");
                                if(!existingNumbers.contains(number))
                                    newList.add(contact);
                                else
                                    newList.set(existingNumbers.indexOf(number), contact);
                            }
                        }
                        else{
                            if(existingNumbers.contains(number)){
                                for(int i = 0; i < newList.size(); i++) {
                                    DataContact contact1 = newList.get(i);
                                    if (contact1.getPhoneNumber().equals(number)) {
                                        existingNumbers.remove(number);
                                        newList.remove(contact1);
                                    }
                                }
                                Realm threadRealm = Realm.getDefaultInstance();
                                DataContact contact = threadRealm.where(DataContact.class).equalTo("phoneNumber", number).findFirst();
                                if(contact != null) {
                                    threadRealm.beginTransaction();
                                    contact.deleteFromRealm();
                                    threadRealm.commitTransaction();
                                }
                                threadRealm.close();
                            }
                        }
                        numbersChecked++;
                        if (numbersChecked == allNumbers.size()) {
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

        private void makeUsers() {
            contactsFound.clear();
            contactsFound.addAll(newList);
            Collections.sort(newList);
        }

        private void downloadImages() {
            numbersChecked = 0;
            Realm threadRealm = Realm.getDefaultInstance();
            for (int i = 0; i < newList.size(); i++) {
                DataContact contact = newList.get(i);
                DataContact searchContact = threadRealm.where(DataContact.class).equalTo("phoneNumber", contact.getPhoneNumber()).findFirst();
                boolean isExisting = existingNumbers.contains(contact.getPhoneNumber());
                if(searchContact == null || !searchContact.getProfilePic().contains(contact.getProfilePic())) {
                    if (contact.getProfilePic() != null && !contact.getProfilePic().equals("") && contact.getPhoneNumber() != null) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference("users").child(contact.getPhoneNumber()).child("profile picture").child(contact.getProfilePic());
                        String dirPath = Environment.getExternalStorageDirectory() + "/" + appName + "/Profile Pictures/" + contact.getPhoneNumber();
                        File dirFile = new File(dirPath);
                        if (!dirFile.exists())
                            dirFile.mkdirs();
                        final String dirPath2 = Environment.getExternalStorageDirectory() + "/" + appName + "/Profile Pictures Thumbnails/" + contact.getPhoneNumber();
                        File dirFile2 = new File(dirPath2);
                        if (!dirFile2.exists())
                            dirFile2.mkdirs();
                        final File file = new File(dirPath + "/" + contact.getProfilePic());
                        final int position = i;
                        storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                contact.setProfilePic(file.getAbsolutePath());
                                int dp50 = dpToPixel(50, context);
                                try {
                                    new Compressor(context)
                                            .setMaxWidth(dp50)
                                            .setMaxHeight(dp50)
                                            .setQuality(75)
                                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                                            .setDestinationDirectoryPath(dirPath2)
                                            .compressToFile(file);
                                    publishProgress(1, position);
                                } catch (Exception e) {

                                }
                                countAndUpdateRealm();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                countAndUpdateRealm();
                            }
                        });
                    }
                    else{
                        publishProgress(1, i);
                        countAndUpdateRealm();
                    }
                }
                else{
                    if(!isExisting)
                        contact.setProfilePic(searchContact.getProfilePic());
                    publishProgress(1, i);
                    countAndUpdateRealm();
                }
            }
            threadRealm.close();
        }

        private void countAndUpdateRealm(){
            numbersChecked++;
            if(numbersChecked == newList.size())
                updateRealm();
        }

        private void updateRealm(){
            Realm threadRealm = Realm.getDefaultInstance();
            for(DataContact contact : newList){
                threadRealm.beginTransaction();
                DataContact searchContact = threadRealm.where(DataContact.class).equalTo("phoneNumber", contact.getPhoneNumber()).findFirst();
                if(searchContact != null && !contact.getProfilePic().contains("/"))
                    contact.setProfilePic(searchContact.getProfilePic());
                else if(searchContact == null && !contact.getProfilePic().contains("/"))
                    contact.setProfilePic("");
                if(searchContact != null && allNumbers.contains(contact.getPhoneNumber()))
                    contact.setName(searchContact.getName());
                if(searchContact != null)
                    contact.setAutoApprove(searchContact.isAutoApprove());
                threadRealm.copyToRealmOrUpdate(contact);
                threadRealm.commitTransaction();
            }
            threadRealm.close();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 0) {
                progressBar.setVisibility(View.GONE);
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ContactDiffUtil(contactsFound, newList));
                diffResult.dispatchUpdatesTo(AdapterContacts.this);
                contactsFound = newList;
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemChanged(getItemCount()-1);
                    }
                });
            }
            else if (values[0] == 1) {
                notifyItemChanged(values[1]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
    */

    public static int dpToPixel(float dp, Context context) {
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}
