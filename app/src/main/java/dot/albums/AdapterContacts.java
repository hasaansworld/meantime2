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
    String appName, thumbnailPath;

    public AdapterContacts(Context context, ProgressBar progressBar) {
        this.context = context;
        this.progressBar = progressBar;
        appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();

        realm = RealmUtils.getRealm();

        importContacts = new ImportContacts((Activity) context);
        allContacts = importContacts.getContacts();
        new ContactsTask().execute();
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
        }

        @Override
        public void onClick(View v) {

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

    public class ContactsTask extends AsyncTask<Void, Integer, Void> {
        DatabaseReference reference;
        List<String> allNumbers = new ArrayList<>();
        List<String> allNames = new ArrayList<>();
        int numbersChecked = 0;
        int count = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                                contact.setAutoApprove(false);
                                contact.setName(name);
                                contact.setPhoneNumber(number);
                                contactsFound.add(contact);
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
            Collections.sort(contactsFound);
        }

        private void downloadImages() {

            for (int i = 0; i < contactsFound.size(); i++) {
                DataContact contact = contactsFound.get(i);
                final int in = i;
                if (contact.getProfilePic() != null && contact.getPhoneNumber() != null) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("users").child(contact.getPhoneNumber()).child("profile picture").child(contact.getProfilePic());
                    String dirPath = Environment.getExternalStorageDirectory() + "/"+appName+"/Profile Pictures/" + contact.getPhoneNumber();
                    File dirFile = new File(dirPath);
                    if (!dirFile.exists())
                        dirFile.mkdirs();
                    final String dirPath2 = Environment.getExternalStorageDirectory() + "/"+appName+"/Profile Pictures Thumbnails/" + contact.getPhoneNumber();
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
            if (values[0] == 0) {
                progressBar.setVisibility(View.GONE);
                notifyDataSetChanged();
            }
            if (values[0] == 1) {
                notifyItemChanged(values[1]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public static int dpToPixel(float dp, Context context) {
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}
