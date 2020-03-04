package dot.albums;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GroupActivity extends AppCompatActivity {
    Toolbar toolbar;
    LinearLayout backLayout;
    TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        backLayout = findViewById(R.id.back_layout);
        toolbarTitle = findViewById(R.id.toolbarTitle);

        backLayout.setOnClickListener(v -> finish());

    }
}
