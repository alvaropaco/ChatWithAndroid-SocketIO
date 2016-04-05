package com.example.alvaro.androidsocketio;

import android.graphics.Bitmap;

/**
 * Created by sreejeshpillai on 10/05/15.
 */
public class User {
    private String uId;

    public void setuName(String uName) {
        this.uName = uName;
    }

    public void setuNickName(String uNickName) {
        this.uNickName = uNickName;
    }

    public void setuImage(Bitmap uImage) {
        this.uImage = uImage;
    }

    private String uName;
    private String uNickName;
    private Bitmap uImage;

    User(){

    }

    User (String name, String nick) {
        User user = new User();
        user.setuName(name);
        user.setuNickName(nick);
    }

    public String getId() {
        return uId;
    };

    public String getName() {
        return uName;
    };

    public String getNickName() {
        return "@" + uNickName;
    };

    public Bitmap getImage() {
        return uImage;
    };


    public static class Builder {
        private String uId;
        private String uName;
        private String uNickName;
        private Bitmap uImage;

        public User build() {
            User user = new User();
            user.uId = uId;
            user.uName = uName;
            user.uNickName = uNickName;
            user.uImage = uImage;
            return user;
        }
    }
}
