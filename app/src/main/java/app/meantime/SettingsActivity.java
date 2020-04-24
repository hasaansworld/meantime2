package app.meantime;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    Toolbar toolbar;
    LinearLayout feedback, support, rate, removeAds, licences;
    ImageView facebook, twitter;
    ImageView checkNoAds, checkSupportUs;
    TextView alarmTone;
    MediaPlayer mediaPlayer;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        alarmTone = findViewById(R.id.alarm_tone);
        String toneName = sharedPreferences.getString("toneName", "You have new message");
        alarmTone.setText("Alarm Tone: \""+toneName+"\"");
        alarmTone.setOnClickListener(v -> showAlarmTonePicker());

        feedback = findViewById(R.id.feedback);
        feedback.setOnClickListener(v -> composeEmail(new String[]{"jinnahinc.pk@gmail.com"}, "Meantime: Feedback"));

        removeAds = findViewById(R.id.remove_ads);
        removeAds.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, RemoveAdsActivity.class)));

        support = findViewById(R.id.support);
        support.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, SupportActivity.class)));

        rate = findViewById(R.id.rate);
        rate.setOnClickListener(v -> {
            String url = "http://play.google.com/store/apps/details?id=app.meantime";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.setPackage("com.android.vending");
            startActivity(i);
        });

        licences = findViewById(R.id.licences);
        licences.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, LicencesActivity.class)));

        checkNoAds = findViewById(R.id.check_no_ads);
        checkSupportUs = findViewById(R.id.check_support_us);

        facebook = findViewById(R.id.button_facebook);
        twitter = findViewById(R.id.button_twitter);

        facebook.setOnClickListener(v -> {
            String url = "http://facebook.com/meantime.reminders";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        twitter.setOnClickListener(v -> {
            String url = "http://twitter.com/meantime_app";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

    }

    private void showAlarmTonePicker() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_tone_picker, null, false);
        RadioGroup toneGroup = v.findViewById(R.id.tone_group);
        RadioButton newMessage = v.findViewById(R.id.new_message);
        RadioButton happyLife = v.findViewById(R.id.happy_life);
        RadioButton getItDone = v.findViewById(R.id.get_it_done);
        RadioButton ringingBells = v.findViewById(R.id.ringing_bells);
        View.OnClickListener onClickListener = v1 -> {
            int tone = R.raw.you_have_new_message;
            if(((RadioButton)v1).isChecked()){
                if(v1 == happyLife)
                    tone = R.raw.happy_life;
                else if(v1 == getItDone)
                    tone = R.raw.quick_piano;
                else if(v1 == ringingBells)
                    tone = R.raw.slow_guitar;
                if(mediaPlayer != null)
                    mediaPlayer.release();
                mediaPlayer = MediaPlayer.create(this, tone);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        };
        newMessage.setOnClickListener(onClickListener);
        happyLife.setOnClickListener(onClickListener);
        getItDone.setOnClickListener(onClickListener);
        ringingBells.setOnClickListener(onClickListener);
        String toneS = sharedPreferences.getString("tone", "new_message");
        if(toneS != null && toneS.equals("happy_life"))
            happyLife.setChecked(true);
        else if(toneS != null && toneS.equals("get_it_done"))
            getItDone.setChecked(true);
        else if(toneS != null && toneS.equals("ringing_bells"))
            ringingBells.setChecked(true);
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Alarm Tones")
                .setView(v)
                .setPositiveButton("Select", (dialog, which) -> {
                    String toneName = "New message";
                    String tone = "new_message";
                    if(toneGroup.getCheckedRadioButtonId() == R.id.happy_life){
                        toneName = "Happy life";
                        tone = "happy_life";
                    }
                    else if(toneGroup.getCheckedRadioButtonId() == R.id.get_it_done){
                        toneName = "Get it done";
                        tone = "get_it_done";
                    }
                    else if(toneGroup.getCheckedRadioButtonId() == R.id.ringing_bells){
                        toneName = "Ringing bells";
                        tone = "ringing_bells";
                    }
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("tone", tone);
                    editor.putString("toneName", toneName);
                    editor.apply();
                    alarmTone.setText("Alarm Tone: \""+toneName+"\"");
                    if(mediaPlayer != null)
                        mediaPlayer.release();
                })
                .show();
        int colorAccent = getResources().getColor(R.color.colorAccent);
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(colorAccent);
        d.setOnCancelListener(dialog -> {
            if(mediaPlayer != null)
                mediaPlayer.release();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        if(sharedPreferences.getBoolean("noAds", false))
            checkNoAds.setVisibility(View.VISIBLE);
        if(sharedPreferences.getBoolean("supportUs", false)){
            checkNoAds.setVisibility(View.VISIBLE);
            checkSupportUs.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    public void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
