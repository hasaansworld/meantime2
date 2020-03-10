package dot.albums;

import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DataReminder extends RealmObject implements Comparable<DataReminder>{
    String reminderId;
    String title, day, date, time, description, image, alarmtime;
    int importance;
    boolean deleted = false;
    int status = STATUS_CREATED;

    public static final int STATUS_CREATED = 0;
    public static final int STATUS_SCHEDULED = 1;
    public static final int STATUS_COMPLETED = 2;

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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public int compareTo(DataReminder o) {
        if(!getDate().equals(o.getDate())) {
            String[] dates1 = getDate().split(" ");
            String[] dates2 = o.getDate().split(" ");
            dates1[1] = getMonthNumber(dates1[1]);
            dates2[1] = getMonthNumber(dates2[1]);
            if(!dates1[2].equals(dates2[2]))
                return Integer.compare(Integer.parseInt(dates1[2]), Integer.parseInt(dates2[2]));
            else if(!dates1[1].equals(dates2[1]))
                return Integer.compare(Integer.parseInt(dates1[1]), Integer.parseInt(dates2[1]));
            else
                return Integer.compare(Integer.parseInt(dates1[0]), Integer.parseInt(dates2[0]));
        }
        else{
            String am1 = getTime().substring(6);
            String am2 = o.getTime().substring(6);
            if(!am1.equals(am2))
                return am1.compareTo(am2);
            else
                return getTime().replace("12:", "00:").compareTo(o.getTime().replace("12:", "00:"));
        }
    }

    public String getMonthNumber(String name){
        return name.replace("Jan", "1")
                .replace("Feb", "2")
                .replace("Mar", "3")
                .replace("Apr", "4")
                .replace("May", "5")
                .replace("Jun", "6")
                .replace("Jul", "7")
                .replace("Aug", "8")
                .replace("Sep", "9")
                .replace("Oct", "10")
                .replace("Nov", "11")
                .replace("Dec", "12");
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
