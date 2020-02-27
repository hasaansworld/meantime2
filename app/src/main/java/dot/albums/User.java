package dot.albums;

public class User implements Comparable<User>{
    private String phone;
    private String name;
    private String profilePic;
    private String about;

    public User(String phone, String name, String profilePic, String about) {
        this.phone = phone;
        this.name = name;
        this.profilePic = profilePic;
        this.about = about;
    }

    public User(String phone, String name, String about) {
        this.phone = phone;
        this.name = name;
        this.about = about;
    }

    public User() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    @Override
    public int compareTo(User o) {
        return this.getName().compareTo(o.getName());
    }

}
