package app.meantime;

import android.app.Application;

import androidx.multidex.MultiDexApplication;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

public class MyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        RealmUtils.init(this);
        EmojiManager.install(new IosEmojiProvider());
    }
}
