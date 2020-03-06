package dot.albums;

import io.realm.RealmObject;

public class DataContact extends RealmObject implements Comparable<DataContact>{
    private String phoneNumber, name, about, profilePic;
    private boolean autoApprove = false;

    public DataContact() {
    }

    public DataContact(String phoneNumber, String name, String about, String profilePic) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.about = about;
        this.profilePic = profilePic;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public boolean isAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }


    @Override
    public int compareTo(DataContact o) {
        return getName().compareTo(o.getName());
    }
}
