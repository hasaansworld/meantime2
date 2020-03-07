package dot.albums;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

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

public class ContactsTask extends AsyncTask<Void, Integer, Void> {
    private Context context;
    ImportContacts importContacts;
    List<Contact> allContacts;
    List<String> allNumbers = new ArrayList<>();
    List<String> allNames = new ArrayList<>();
    List<DataContact> contactsFound = new ArrayList<>();
    String appName;
    Realm realm;
    int numbersChecked = 0;

    OnFinishedListener listener;

    public ContactsTask(Context context){
        this.context = context;
        appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }


    @Override
    protected Void doInBackground(Void... args) {
        realm = RealmUtils.getRealm();
        importContacts = new ImportContacts((Activity) context);
        allContacts = importContacts.getContacts();

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
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
            reference.child(number).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        DataContact contact = dataSnapshot.getValue(DataContact.class);
                        if(contact != null) {
                            contact.setName(name);
                            contact.setPhoneNumber(number);
                            if(contact.getProfilePic() == null)
                                contact.setProfilePic("");
                            contactsFound.add(contact);
                        }
                    }
                    else{
                        DataContact contact = realm.where(DataContact.class).equalTo("phoneNumber", number).findFirst();
                        if(contact != null) {
                            realm.beginTransaction();
                            contact.deleteFromRealm();
                            realm.commitTransaction();
                        }
                    }
                    numbersChecked++;
                    if (numbersChecked == allNumbers.size()) {
                        Collections.sort(contactsFound);
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

    private void downloadImages() {
        numbersChecked = 0;
        Realm threadRealm = Realm.getDefaultInstance();
        for (int i = 0; i < contactsFound.size(); i++) {
            DataContact contact = contactsFound.get(i);
            DataContact searchContact = threadRealm.where(DataContact.class).equalTo("phoneNumber", contact.getPhoneNumber()).findFirst();
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
                    countAndUpdateRealm();
                }
            }
            else{
                contact.setProfilePic(searchContact.getProfilePic());
                countAndUpdateRealm();
            }
        }
        threadRealm.close();
    }

    private void countAndUpdateRealm(){
        numbersChecked++;
        if(numbersChecked == contactsFound.size())
            updateRealm();
    }

    private void updateRealm(){
        Realm threadRealm = Realm.getDefaultInstance();
        for(DataContact contact : contactsFound){
            threadRealm.beginTransaction();
            DataContact searchContact = threadRealm.where(DataContact.class).equalTo("phoneNumber", contact.getPhoneNumber()).findFirst();
            if(!contact.getProfilePic().contains("/"))
                contact.setProfilePic(searchContact != null ? searchContact.getProfilePic() : "");

            if(searchContact != null) {
                if(allNumbers.contains(contact.getPhoneNumber()))
                    contact.setName(searchContact.getName());
                contact.setAutoApprove(searchContact.isAutoApprove());
            }
            threadRealm.copyToRealmOrUpdate(contact);
            threadRealm.commitTransaction();
        }
        threadRealm.close();
        if(listener != null)
            listener.onFinish();
    }

    public static int dpToPixel(float dp, Context context) {
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    public void setOnFinishedListener(OnFinishedListener listener){
        this.listener = listener;
    }

    public interface OnFinishedListener{
        public void onFinish();
    }
}
