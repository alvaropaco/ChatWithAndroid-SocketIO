package com.example.alvaro.androidsocketio;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.jar.JarEntry;

/**
 * Created by sreejeshpillai on 10/05/15.
 */
public class Message {

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_LOG = 2;
    public static final int TYPE_ACTION = 3;

    private int mType;
    private User mAuthor;
    private String mMessage;
    private Bitmap mImage;

    private Message() {}

    public int getType() {
        return mType;
    };

    public User getAuthor() {
        return mAuthor;
    };

    public String getMessage() {
        return mMessage;
    };

    public Bitmap getImage() {
        return mImage;
    };


    public static class Builder {
        private final int mType;
        private User mAuthor;
        private Bitmap mImage;
        private String mMessage;

        public Builder(int type) {
            mType = type;
        }

        public Builder image(Bitmap image) {
            mImage = image;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public void setmAuthor(User mAuthor) {
            this.mAuthor = mAuthor;
        }

        public Builder author(String message, JSONObject author){
            try {
                setmAuthor(new User());
                this.mAuthor.setuName(author.getString("name").toString());
                this.mAuthor.setuNickName(author.getString("nick").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mMessage = message;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.mType = mType;
            message.mImage = mImage;
            message.mMessage = mMessage;
            message.mAuthor = mAuthor;
            return message;
        }
    }
}
