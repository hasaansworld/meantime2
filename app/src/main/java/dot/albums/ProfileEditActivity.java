package dot.albums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nguyenhoanglam.imagepicker.helper.PermissionHelper;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {
    Toolbar toolbar;
    SharedPreferences sharedPreferences;
    boolean isProfileSetup;

    ImageView profilePicture, pickPhoto, emoji;
    EditText username, about;
    TextView textError, toolbarTitle;
    MaterialButton saveButton;
    String path = "";
    ConnectivityManager connectivityManager;
    ProgressBar progressBar;
    FirebaseDatabase db;
    String uname = "";
    String newUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        db = FirebaseDatabase.getInstance();
        isProfileSetup = sharedPreferences.getBoolean("profileDone", false);

        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        saveButton = findViewById(R.id.saveButton);

        if(isProfileSetup){
            toolbarTitle.setText("Edit Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        }
        else{
            toolbarTitle.setText("Set Up Your Account");
            saveButton.setText("Let's Go");
            uname = sharedPreferences.getString("username", "");
            String userId = sharedPreferences.getString("userId", "");
            if(!uname.equals("") && !userId.equals("")){
                db.getReference("usernames").child(uname).removeValue();
                db.getReference("accountInfo").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).removeValue();
                db.getReference("users").child(userId).removeValue();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userId", "");
                editor.putString("username", "");
                editor.putString("profilePicPath", "");
                editor.apply();
            }
        }

        username = findViewById(R.id.username);
        username.setSelection(1);
        username.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().startsWith("@")){
                    String un = "@"+s.toString();
                    username.setText(un);
                    username.setSelection(un.length());
                }
            }
        });

        profilePicture = findViewById(R.id.profilePicture);
        pickPhoto = findViewById(R.id.pickPhoto);
        textError = findViewById(R.id.textError);
        progressBar = findViewById(R.id.progessBar);

        path = sharedPreferences.getString("profilePicPath", "");
        if(!path.equals(""))
            Glide.with(this).asBitmap().load(path).placeholder(R.drawable.profile_picture).into(profilePicture);

        pickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textError.setVisibility(View.GONE);
                String un = username.getText().toString();
                final String user = un.substring(1);
                if(un.equals("@")){
                    textError.setVisibility(View.VISIBLE);
                    textError.setText("Please enter a username.");
                }
                else if(!user.matches("^(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])")){
                    textError.setVisibility(View.VISIBLE);
                    textError.setText("Username can only contain letters, numbers, periods and underscores.");
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.GONE);
                    //uname = Username.encode(user);
                    uname = username.getText().toString();
                    db.getReference().child("usernames").child(Username.encode(uname)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                textError.setVisibility(View.VISIBLE);
                                textError.setText("Username already taken.");
                                progressBar.setVisibility(View.GONE);
                                saveButton.setVisibility(View.VISIBLE);
                            }
                            else{
                                newUserId = db.getReference().child("users").push().getKey();
                                db.getReference().child("users").child(newUserId).child("phone").setValue(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        db.getReference().child("users").child(newUserId).child("username").setValue(uname);
                                        db.getReference().child("usrnames").child(Username.encode(uname)).setValue(newUserId);
                                        DatabaseReference accountRef = db.getReference().child("account info").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                                        accountRef.child("userId").setValue(newUserId);
                                        accountRef.child("username").setValue(uname);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("userId", newUserId);
                                        editor.putString("username", Username.decode(uname));
                                        editor.apply();
                                        if (!path.equals("") && !path.equals(sharedPreferences.getString("profilePicPath", ""))) {
                                            Uri file = Uri.fromFile(new File(path));
                                            FirebaseStorage storage = FirebaseStorage.getInstance();
                                            final String lastPath = file.getLastPathSegment();
                                            StorageReference profilePictureRef = storage.getReference().child("users").child(newUserId).child("profile picture").child(file.getLastPathSegment());
                                            profilePictureRef.putFile(file)
                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            /*FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                    .setDisplayName(name.getText().toString()).build();
                                                            user.updateProfile(profileUpdates);*/
                                                            db.getReference().child("account info").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("profilePic").setValue(lastPath);
                                                            db.getReference().child("users").child(newUserId).child("profilePic").setValue(lastPath);
                                                            createProfile();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            textError.setVisibility(View.VISIBLE);
                                                            textError.setText("Failed to upload your profile picture.");
                                                            saveButton.setVisibility(View.VISIBLE);
                                                            progressBar.setVisibility(View.GONE);
                                                        }
                                                    });
                                        } else {
                                            createProfile();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        textError.setVisibility(View.VISIBLE);
                                        textError.setText("Failed to save your profile data.");
                                        progressBar.setVisibility(View.GONE);
                                        saveButton.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

    }

    private void createProfile() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profilePicPath", path);
        editor.putBoolean("profileDone", true);
        editor.apply();
        if(!isProfileSetup) startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void pickPhoto() {
        ImagePicker.Builder builder = ImagePicker.with(this);                         //  Initialize ImagePicker with activity or fragment context
        if(Build.VERSION.SDK_INT >= 23) builder.setStatusBarColor("#FFFFFF"); //  StatusBar color
        builder.setToolbarColor("#FFFFFF")
                .setToolbarTextColor("#000000")     //  Toolbar text color (Title and Done button)
                .setToolbarIconColor("#000000")     //  Toolbar icon color (Back and Camera button)
                .setProgressBarColor("#5C6BC0")     //  ProgressBar color
                .setBackgroundColor("#FFFFFF")      //  Background color
                .setCameraOnly(false)               //  Camera mode
                .setMultipleMode(false)              //  Select multiple images or single image
                .setFolderMode(true)                //  Folder mode
                .setShowCamera(true)                //  Show camera button
                .setFolderTitle("Pick a Photo")           //  Folder title (works with FolderMode = true)
                .setImageTitle("Photos")            //  Image title (works with FolderMode = false)
                .setDoneTitle("Done")               //  Done button title
                .setLimitMessage("You have reached selection limit")    // Selection limit message
                .setMaxSize(1)                     //  Max images can be selected
                .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                .setRequestCode(100)                //  Set request code, default Config.RC_PICK_IMAGES
                .setKeepScreenOn(true)              //  Keep screen on when selecting images
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            Uri uri = Uri.fromFile(new File(images.get(0).getPath()));
            String appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
            File dirFile = new File(Environment.getExternalStorageDirectory()+"/"+appName+"/Profile Pictures");
            if (!dirFile.exists()){
                dirFile.mkdirs();
            }

            path = Environment.getExternalStorageDirectory() + "/"+appName+"/Profile Pictures/profile picture "+System.currentTimeMillis()+".png";
            Uri uriDestination = Uri.fromFile(new File(path));
            UCrop.Options options = new UCrop.Options();
            options.setHideBottomControls(true);
            options.setToolbarTitle("Crop Photo");
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.NONE);
            UCrop.of(uri, uriDestination)
                    .withOptions(options)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(800, 800)
                    .start(this);

        }
       if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Glide.with(this).asBitmap().load(path).placeholder(R.drawable.profile_picture).into(profilePicture);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

}
