package dot.albums;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;

public class CreateActivity extends AppCompatActivity {
    CoordinatorLayout root;
    Toolbar toolbar;
    ImageView emojiTitle, emojiDescription;
    EmojiEditText title, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        root = findViewById(R.id.root);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        emojiTitle = findViewById(R.id.emojiTitle);
        emojiDescription = findViewById(R.id.emojiDescription);
        initializeEmoji(emojiTitle, title);
        initializeEmoji(emojiDescription, description);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }

    public void initializeEmoji(final ImageView imageView, EmojiEditText emojiEditText){
        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(root).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                imageView.setImageResource(R.drawable.outline_sentiment_satisfied_black_24);
            }
        }).build(emojiEditText);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(emojiPopup.isShowing() ? R.drawable.outline_sentiment_satisfied_black_24 : R.drawable.ic_keyboard_black_24dp);
                emojiPopup.toggle();
            }
        });
    }
}
