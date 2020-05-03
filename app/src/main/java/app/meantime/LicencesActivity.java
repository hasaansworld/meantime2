package app.meantime;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LicencesActivity extends AppCompatActivity {
    Toolbar toolbar;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licences);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        layout = findViewById(R.id.layout);

        addLicense("license_you_have_new_message.txt");
        addLicense("license_glide.txt");
        addLicense("license_vanniktech_emoji.txt");
        addLicense("license_wdullaer_material_date_time_picker.txt");
        addLicense("license_nguyenhoanglam_image_picker.txt");
        addLicense("license_yalantis_ucrop.txt");
        addLicense("license_view_tooltip.txt");
        addLicense("license_crash_reporter.txt");
        addLicense("license_zetbaitsu_compressor.txt");
    }

    private void addLicense(String fileName){
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setTextColor(Color.parseColor("#444444"));
        textView.setTextSize(14);
        layout.addView(textView, layoutParams);
        AssetReader assetReader = new AssetReader(this);
        textView.setText(assetReader.getTextFile(fileName));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return true;
    }
}
