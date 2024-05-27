package com.infostreamz.help;

public class Contact {

    private String name;
    private String profileImg;

    public Contact(String name, String profileImg) {
        this.name = name;
        this.profileImg = profileImg;
    }

    public String getName() {
        return name;
    }

    public String getProfileImg() {
        return profileImg;
    }
}
