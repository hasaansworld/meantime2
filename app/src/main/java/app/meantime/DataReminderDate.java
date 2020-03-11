package app.meantime;

public class DataReminderDate {
    int adapterPosition;
    String day, date;

    public DataReminderDate(){

    }

    public DataReminderDate(int adapterPosition, String day, String date) {
        this.adapterPosition = adapterPosition;
        this.day = day;
        this.date = date;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
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

}
