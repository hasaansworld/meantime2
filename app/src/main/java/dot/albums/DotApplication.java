package dot.albums;

import android.app.Application;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

public class DotApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RealmUtils.init(this);
        EmojiManager.install(new IosEmojiProvider());
    }
}
