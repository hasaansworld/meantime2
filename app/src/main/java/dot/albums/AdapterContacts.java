package dot.albums;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
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
    List<AccountInfo> contactsFound = new ArrayList<>();
    List<User> allContactUsers = new ArrayList<>();


    public AdapterContacts(Context context, ProgressBar progressBar) {
        this.context = context;
        this.progressBar = progressBar;

        realm = RealmUtils.getRealm();

        importContacts = new ImportContacts((Activity) context);
        allContacts = importContacts.getContacts();
        //new ContactsTask().execute();
    }

    public class ViewHolderInvite extends RecyclerView.ViewHolder {

        public ViewHolderInvite(View v) {
            super(v);
        }
    }

    public class ViewHolderContact extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView count;
        public ViewHolderContact(View v) {
            super(v);
            count = v.findViewById(R.id.count);
            count.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {

        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (viewType != getItemCount()-1) {
            v = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
            return new ViewHolderContact(v);
        } else {
            v = LayoutInflater.from(context).inflate(R.layout.item_invite, parent, false);
            return new ViewHolderInvite(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public class ContactsTask extends AsyncTask<Void, Integer, Void> {
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
                            contactsFound.add(dataSnapshot.getValue(AccountInfo.class));

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
            for (int i = 0; i < contactsFound.size(); i++) {
                AccountInfo info = contactsFound.get(i);
                User user = allContactUsers.get(i);
                if (info.getUsername() != null) {
                    user.setProfilePic(info.getProfilePic());
                    allContactUsers.set(i, user);
                }
            }
            Collections.sort(allContactUsers);
        }

        private void downloadImages() {
            for (int i = 0; i < allContactUsers.size(); i++) {
                User user = allContactUsers.get(i);
                final int in = i;
                if (user.getProfilePic() != null) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("users").child(user.getPhone()).child("profile picture").child(user.getProfilePic());
                    String dirPath = Environment.getExternalStorageDirectory() + "/dotAlbums/" + user.getPhone() + "/Profile Pictures";
                    File dirFile = new File(dirPath);
                    if (!dirFile.exists())
                        dirFile.mkdirs();
                    final String dirPath2 = Environment.getExternalStorageDirectory() + "/dotAlbums/" + user.getPhone() + "/Profile Pictures Thumbnails";
                    File dirFile2 = new File(dirPath2);
                    if (!dirFile2.exists())
                        dirFile2.mkdirs();
                    final File file = new File(dirPath + "/" + user.getProfilePic());
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
                                //publishProgress(1, updatePosition);
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
                //contactCount = contactsFound.size();
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
