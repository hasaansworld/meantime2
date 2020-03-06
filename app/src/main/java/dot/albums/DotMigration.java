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
        if(oldVersion < 8)
            schema.get("DataReminder").addField("deleted", boolean.class);
        if(oldVersion < 9)
            schema.create("DataContact")
                    .addField("name", String.class)
                    .addField("about", String.class)
                    .addField("profilePic", String.class)
                    .addField("phoneNumber", String.class)
                    .addField("autoApprove", boolean.class);
        if(oldVersion < 10)
            schema.get("DataContact")
                    .addPrimaryKey("phoneNumber");

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
