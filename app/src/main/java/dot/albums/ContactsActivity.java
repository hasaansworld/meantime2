package dot.albums;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.nguyenhoanglam.imagepicker.helper.PermissionHelper;

public class ContactsActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    AdapterContacts adapter;
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;
    boolean isPermissionGranted = false;
    LinearLayout continueLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        progressBar = findViewById(R.id.progessBar);
        continueLayout = findViewById(R.id.continueLayout);
    }

    public void checkPermission(){
        if(!isPermissionGranted) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    showPermissionDialog(false);
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            100);
                }
            } else {
                isPermissionGranted = true;
                setupRecyclerView();
            }
        }
    }

    public void showPermissionDialog(boolean settings){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Contacts Permission")
                .setMessage("Please allow us to read your contacts so you can share reminders with your friends.");
        if(settings){
            builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    PermissionHelper.openAppSettings(ContactsActivity.this);
                }
            });
        }
        else {
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(ContactsActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            100);
                }
            });
        }
        AlertDialog dialog = builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
        int colorAccent = getResources().getColor(R.color.colorAccent);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(colorAccent);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(colorAccent);
    }

    public void setupRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdapterContacts(this, progressBar);
        recyclerView.setAdapter(adapter);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            ContactsTask task = new ContactsTask(this);
            task.setOnFinishedListener(() -> {
                progressBar.setVisibility(View.GONE);
                adapter = new AdapterContacts(ContactsActivity.this, progressBar);
                recyclerView.setAdapter(adapter);
            });
            task.execute();
        }
        else
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        for(int i = 0; i < permissions.length; i++) {
            switch (requestCode) {
                case 100: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        setupRecyclerView();
                        isPermissionGranted = true;
                    } else {
                        showPermissionDialog(Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(permissions[i]));
                    }
                    return;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermission();
    }

}
