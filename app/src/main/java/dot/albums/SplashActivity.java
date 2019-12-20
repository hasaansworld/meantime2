package dot.albums;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nguyenhoanglam.imagepicker.helper.PermissionHelper;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePickerActivity;

import java.io.File;
import java.util.Arrays;

public class SplashActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;

    SharedPreferences sharedPreferences;
    LinearLayout layoutSetup;
    boolean permissionGranted = false, isProfileSetup = false, setupStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        isProfileSetup = sharedPreferences.getBoolean("profileDone", false);

        layoutSetup = findViewById(R.id.layoutSetup);
        handleSignIn();

    }

    private void handleSignIn(){
        if(isProfileSetup){
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    // already signed in
                    if(!isProfileSetup) {
                        profileSetupWithPermission();
                    }
                } else {
                    // not signed in
                    signIn();
                }
            }
        }, 1000);
    }

    private void profileSetup() {
        setupStarted = true;
        layoutSetup.setVisibility(View.VISIBLE);
        String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usernames = db.getReference().child("account info");
        usernames.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    AccountInfo accountInfo = dataSnapshot.getValue(AccountInfo.class);
                    final String userId = accountInfo.getUserId();
                    final String username = accountInfo.getUsername();
                    if(accountInfo.getProfilePic() != null) {
                        StorageReference ref = FirebaseStorage.getInstance().getReference().child("users").child(userId).child("profile picture").child(accountInfo.getProfilePic());
                        File dirFile = new File(Environment.getExternalStorageDirectory() + "/dotAlbums/Profile Pictures");
                        if (!dirFile.exists()){
                            dirFile.mkdirs();
                        }
                        File file = new File(Environment.getExternalStorageDirectory() + "/dotAlbums/Profile Pictures/"+accountInfo.getProfilePic());
                        final String path = file.getAbsolutePath();
                        Toast.makeText(SplashActivity.this, path, Toast.LENGTH_SHORT).show();

                        ref.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                saveInfo(userId, username, path);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                saveInfo(userId, username, "");
                                Toast.makeText(SplashActivity.this, "profile pic failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        Toast.makeText(SplashActivity.this, "no profile pic", Toast.LENGTH_SHORT).show();
                        saveInfo(userId, username, "");
                    }
                }
                else{
                    startActivity(new Intent(SplashActivity.this, ProfileEditActivity.class));
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void saveInfo(String userId, String username, String path){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.putString("username", username);
        editor.putString("profilePicPath", path);
        editor.putBoolean("profileDone", true);
        editor.apply();
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    public void signIn(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme_SignIn)
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.PhoneBuilder().build()))
                        .build(),
                RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                profileSetupWithPermission();
            } else {
                // Sign in failed

                if (response == null) {
                    // User pressed back button
                    finish();
                    return;
                }

                // retry
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        signIn();
                    }
                }, 3000);

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "Can't connect to the internet!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, "Unknown error!", Toast.LENGTH_SHORT).show();
                Log.e("blogside login", "Sign-in error: ", response.getError());

            }
        }
    }


    private void profileSetupWithPermission() {
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        PermissionHelper.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionHelper.PermissionAskListener() {
            @Override
            public void onNeedPermission() {
                PermissionHelper.requestAllPermissions(SplashActivity.this, permissions, Config.RC_WRITE_EXTERNAL_STORAGE_PERMISSION);
            }

            @Override
            public void onPermissionPreviouslyDenied() {
                PermissionHelper.requestAllPermissions(SplashActivity.this, permissions, Config.RC_WRITE_EXTERNAL_STORAGE_PERMISSION);
            }

            @Override
            public void onPermissionDisabled() {
                Toast.makeText(SplashActivity.this, "Please provide storage permission from settings!", Toast.LENGTH_LONG).show();
                PermissionHelper.openAppSettings(SplashActivity.this);
            }

            @Override
            public void onPermissionGranted() {
                permissionGranted = true;
                profileSetup();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case Config.RC_WRITE_EXTERNAL_STORAGE_PERMISSION: {
                if (PermissionHelper.hasGranted(grantResults) && !isProfileSetup) {
                    permissionGranted = true;
                    profileSetup();
                    return;
                }
                Toast.makeText(this, "Storage permission missing, exiting app!", Toast.LENGTH_SHORT).show();
                finish();
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(PermissionHelper.hasSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionGranted = true;
        if(!isProfileSetup && permissionGranted && !setupStarted){
            profileSetup();
        }
    }
}
