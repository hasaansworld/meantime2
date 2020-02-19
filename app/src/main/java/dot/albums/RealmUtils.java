package dot.albums;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmUtils {
    public static Realm realm;

    public static void init(Context context){
        Realm.init(context);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("database")
                .migration(new DotMigration(context))
                .schemaVersion(8)
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();
    }

    public static Realm getRealm(){
        return realm;
    }
}
