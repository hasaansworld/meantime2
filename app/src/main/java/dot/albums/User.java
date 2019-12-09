package dot.albums;

public class User implements Comparable<User>{
    private String phone;
    private String name;
    private String username;
    private String profilePic;

    public User(String phone, String name, String username, String profilePic) {
        this.phone = phone;
        this.name = name;
        this.username = username;
        this.profilePic = profilePic;
    }

    public User(String phone, String name) {
        this.phone = phone;
        this.name = name;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    @Override
    public int compareTo(User o) {
        return this.getName().compareTo(o.getName());
    }
}
