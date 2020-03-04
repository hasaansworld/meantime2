package dot.albums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
  Toolbar toolbar;
  ImageView profilePicture, pickPhoto, editName, editAbout;
  ProgressBar progressBar;
  TextView name, about;
  SharedPreferences sharedPreferences;
  FirebaseDatabase db;
  String path = "";
  String previousPath = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);
    sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
    db = FirebaseDatabase.getInstance();

    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    profilePicture = findViewById(R.id.profilePicture);
    pickPhoto = findViewById(R.id.pickPhoto);
    name = findViewById(R.id.name);
    about = findViewById(R.id.about);
    editName = findViewById(R.id.edit_name);
    editAbout = findViewById(R.id.edit_about);
    progressBar = findViewById(R.id.progessBar);

    path = sharedPreferences.getString("profilePicPath", "");
    if(!path.equals(""))
      Glide.with(this).asBitmap().load(path).placeholder(R.drawable.profile_picture).into(profilePicture);
    previousPath = path;
    name.setText(sharedPreferences.getString("name", "No Name"));
    about.setText(sharedPreferences.getString("about", "Let's share some pictures!"));

    pickPhoto.setOnClickListener(v -> pickPhoto());

    editName.setOnClickListener(v -> {
      View view = getLayoutInflater().inflate(R.layout.layout_name_edit, null);
      EditText name2 = view.findViewById(R.id.name2);
      name2.setText(name.getText().toString());
      name2.setSelection(name2.getText().toString().length());
      BottomSheetDialog dialog = new BottomSheetDialog(this);
      dialog.setContentView(view);
      dialog.show();
      MaterialButton saveButton = view.findViewById(R.id.save_name);
      saveButton.setOnClickListener(v1 -> {
        String nameS = name2.getText().toString();
        name.setText(nameS);
        dialog.dismiss();
        String phone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        db.getReference("users").child(phone).child("name").setValue(nameS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", nameS);
        editor.apply();
      });
      MaterialButton cancelButton = view.findViewById(R.id.cancel_name);
      cancelButton.setOnClickListener(v1 -> {
        dialog.dismiss();
      });
    });

    editAbout.setOnClickListener(v -> {
      View view = getLayoutInflater().inflate(R.layout.layout_about_edit, null);
      EmojiEditText about2 = view.findViewById(R.id.about2);
      about2.setText(about.getText().toString());
      about2.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
      about2.setSingleLine(false);
      about2.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
      about2.setSelection(about2.getText().toString().length());
      ImageView aboutEmoji = view.findViewById(R.id.about_emoji);
      final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(view)
              .setOnEmojiPopupShownListener(() -> {
                aboutEmoji.setImageResource(R.drawable.ic_keyboard_black_24dp);
              })
              .setOnEmojiPopupDismissListener(() -> {
                aboutEmoji.setImageResource(R.drawable.outline_sentiment_satisfied_black_24);
              })
              .build(about2);
      aboutEmoji.setOnClickListener(v2 -> emojiPopup.toggle());

      BottomSheetDialog dialog = new BottomSheetDialog(this);
      dialog.setContentView(view);
      dialog.show();
      MaterialButton saveButton = view.findViewById(R.id.save_about);
      saveButton.setOnClickListener(v1 -> {
        String aboutS = about2.getText().toString();
        about.setText(aboutS);
        dialog.dismiss();
        String phone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        db.getReference("users").child(phone).child("about").setValue(aboutS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("about", aboutS);
        editor.apply();
      });
      MaterialButton cancelButton = view.findViewById(R.id.cancel_about);
      cancelButton.setOnClickListener(v1 -> {
        dialog.dismiss();
      });
    });
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
      progressBar.setVisibility(View.VISIBLE);
      updateProfilePic();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(item.getItemId() == android.R.id.home)
      finish();
    return true;
  }

  private void updateProfilePic(){
    Uri file = Uri.fromFile(new File(path));
    FirebaseStorage storage = FirebaseStorage.getInstance();
    final String lastPath = file.getLastPathSegment();
    String phone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    StorageReference profilePictureRef = storage.getReference().child("users").child(phone).child("profile picture").child(file.getLastPathSegment());
    profilePictureRef.putFile(file)
            .addOnSuccessListener(taskSnapshot -> {
              DatabaseReference profileRef = db.getReference().child("users").child(phone).child("profilePic");
              profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  String profilePicName = dataSnapshot.getValue(String.class);
                  StorageReference profilePictureRef = storage.getReference().child("users").child(phone).child("profile picture").child(profilePicName);
                  profilePictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                      profileRef.setValue(lastPath);
                      SharedPreferences.Editor editor = sharedPreferences.edit();
                      editor.putString("profilePicPath", path);
                      editor.apply();
                      previousPath = path;
                      progressBar.setVisibility(View.GONE);
                    }
                  })
                          .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                              failedToUpdateProfilePic();
                            }
                          });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                  failedToUpdateProfilePic();
                }
              });
            })
            .addOnFailureListener(e -> {
              failedToUpdateProfilePic();
            });
  }

  private void failedToUpdateProfilePic(){
    Toast.makeText(ProfileActivity.this, "Could not update profile picture!", Toast.LENGTH_SHORT).show();
    progressBar.setVisibility(View.GONE);
    path = previousPath;
    Glide.with(this).asBitmap().load(path).placeholder(R.drawable.profile_picture).into(profilePicture);
  }
}