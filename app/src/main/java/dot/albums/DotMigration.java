package dot.albums;

import android.content.Context;

import java.nio.charset.StandardCharsets;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class DotMigration implements RealmMigration {
    Context context;

    public DotMigration(Context context){
        this.context = context;
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        if(oldVersion <= 6)
            schema.create("DataReminder")
                    .addField("reminderId", String.class)
                    .addField("title", String.class)
                    .addField("day", String.class)
                    .addField("date", String.class)
                    .addField("time", String.class)
                    .addField("description", String.class)
                    .addField("image", String.class)
                    .addField("alarmtime", String.class)
                    .addField("importance", int.class);
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
