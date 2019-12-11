package dot.albums;

import android.app.Application;

public class DotApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RealmUtils.init(this);
    }
}
