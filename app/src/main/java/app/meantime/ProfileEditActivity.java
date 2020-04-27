package app.meantime;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
//import com.yalantis.ucrop.UCrop;
//import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.util.ArrayList;

public class ProfileEditActivity extends AppCompatActivity {
  Toolbar toolbar;
  SharedPreferences sharedPreferences;
  boolean isProfileSetup;

  ImageView profilePicture, pickPhoto, emoji;
  EditText name, about;
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
      uname = sharedPreferences.getString("name", "");
      String userId = sharedPreferences.getString("userId", "");
      if(!uname.equals("") && !userId.equals("")){
        db.getReference("names").child(uname).removeValue();
        db.getReference("accountInfo").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).removeValue();
        db.getReference("users").child(userId).removeValue();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", "");
        editor.putString("name", "");
        editor.putString("profilePicPath", "");
        editor.apply();
      }
    }

    name = findViewById(R.id.name);

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
        String un = name.getText().toString();
        final String user = un.substring(1);
        if(un.equals("")){
          textError.setVisibility(View.VISIBLE);
          textError.setText("Please enter a name.");
        }
        else {
          progressBar.setVisibility(View.VISIBLE);
          saveButton.setVisibility(View.GONE);
          uname = name.getText().toString();
          String phone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
          db.getReference().child("users").child(phone).child("name").setValue(uname);
          SharedPreferences.Editor editor = sharedPreferences.edit();
          editor.putString("name", uname);
          editor.apply();
          if (!path.equals("") && !path.equals(sharedPreferences.getString("profilePicPath", ""))) {
            Uri file = Uri.fromFile(new File(path));
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final String lastPath = file.getLastPathSegment();
            StorageReference profilePictureRef = storage.getReference().child("users").child(phone).child("profile picture").child(file.getLastPathSegment());
            profilePictureRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                      @Override
                      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        db.getReference().child("users").child(phone).child("profilePic").setValue(lastPath);
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
          }

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
//      UCrop.Options options = new UCrop.Options();
//      options.setHideBottomControls(true);
//      options.setToolbarTitle("Crop Photo");
//      options.setCompressionFormat(Bitmap.CompressFormat.PNG);
//      options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.NONE);
//      UCrop.of(uri, uriDestination)
//              .withOptions(options)
//              .withAspectRatio(1, 1)
//              .withMaxResultSize(800, 800)
//              .start(this);

    }
//    if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
//      Glide.with(this).asBitmap().load(path).placeholder(R.drawable.profile_picture).into(profilePicture);
//    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(item.getItemId() == android.R.id.home)
      finish();
    return true;
  }

}