package com.kiatech.kia;

public class UserDetails {
    String name, gender, group, counsellor;
    int profilePic;

    public UserDetails() {
    }

    public UserDetails(String name, String gender, String group, String counsellor) {
        this.name = name;
        this.gender = gender;
        this.group = group;
        this.counsellor = counsellor;
    }

    public int getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(int profilePic) {
        this.profilePic = profilePic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getCounsellor() {
        return counsellor;
    }

    public void setCounsellor(String counsellor) {
        this.counsellor = counsellor;
    }
}
