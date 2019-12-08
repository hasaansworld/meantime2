package dot.albums;

class AccountInfo{
    private String username, profilePic;

    public AccountInfo() {
    }

    public AccountInfo(String username, String profilePic) {
        this.username = username;
        this.profilePic = profilePic;
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

}
