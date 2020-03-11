package app.meantime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.vanniktech.emoji.EmojiPopup;

import io.realm.Realm;

public class AddDescriptionActivity extends AppCompatActivity {
    Toolbar toolbar;
    Realm realm;
    String id;
    EditText description;
    CoordinatorLayout root;
    TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_description);
        root = findViewById(R.id.root);
        id = getIntent().getStringExtra("id");
        realm = RealmUtils.getRealm();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        DataReminder reminder = realm.where(DataReminder.class).equalTo("reminderId", id).findFirst();
        description = findViewById(R.id.description2);
        if(reminder.getDescription() != null && !reminder.getDescription().equals("")) {
            description.setText(reminder.getDescription());
            toolbarTitle.setText("Edit Description");
        }
        description.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        description.setSingleLine(false);
        description.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        description.setSelection(description.getText().toString().length());
        ImageView descriptionEmoji = findViewById(R.id.description_emoji);
        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(root)
                .setOnEmojiPopupShownListener(() -> {
                    descriptionEmoji.setImageResource(R.drawable.ic_keyboard_black_24dp);
                })
                .setOnEmojiPopupDismissListener(() -> {
                    descriptionEmoji.setImageResource(R.drawable.outline_sentiment_satisfied_black_24);
                })
                .build(description);
        descriptionEmoji.setOnClickListener(v2 -> emojiPopup.toggle());

        MaterialButton saveButton = findViewById(R.id.save_description);
        saveButton.setOnClickListener(v1 -> {
            String descriptionS = description.getText().toString();
            realm.beginTransaction();
            reminder.setDescription(descriptionS);
            realm.commitTransaction();
            setResult(Activity.RESULT_OK);
            finish();
        });
        MaterialButton cancelButton = findViewById(R.id.cancel_description);
        cancelButton.setOnClickListener(v1 -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        return true;
    }
}
