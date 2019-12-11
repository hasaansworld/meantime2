package dot.albums;

class AccountInfo{
    private String userId, username, profilePic;

    public AccountInfo() {
    }

    public AccountInfo(String userId, String username, String profilePic) {
        this.userId = userId;
        this.username = username;
        this.profilePic = profilePic;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
