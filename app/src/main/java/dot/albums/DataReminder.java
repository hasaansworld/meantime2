package dot.albums;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DataReminder extends RealmObject {
    String reminderId;
    String title, day, date, time, description, image, alarmtime;
    int importance;

    public DataReminder(){}

    public DataReminder(String reminderId, String title, String day, String date, String time, String alarmtime, int importance) {
        this.reminderId = reminderId;
        this.title = title;
        this.day = day;
        this.date = date;
        this.time = time;
        this.alarmtime = alarmtime;
        this.importance = importance;
    }

    public String getReminderId() {
        return reminderId;
    }

    public void setReminderId(String reminderId) {
        this.reminderId = reminderId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAlarmtime() {
        return alarmtime;
    }

    public void setAlarmtime(String alarmtime) {
        this.alarmtime = alarmtime;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }
}
