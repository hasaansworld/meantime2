package dot.albums;

import android.content.Context;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;

public class DotMigration implements RealmMigration {
    Context context;

    public DotMigration(Context context){
        this.context = context;
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof DotMigration;
    }

    @Override
    public int hashCode() {
        return DotMigration.class.hashCode();
    }
}
