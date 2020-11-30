package com.lyricslover.onelyrics.pojos;

import android.os.Parcel;
import android.os.Parcelable;

public class Lyrics implements Parcelable {

    private String text;
    private final String type;

    //private boolean availableOffline;

    public Lyrics(String text, String type) {
        this.text = text;
        this.type = type;
        //this.availableOffline = availableOffline;
    }


    //public boolean isAvailableOffline() { return availableOffline; }

    //public void setAvailableOffline(boolean availableOffline) { this.availableOffline = availableOffline; }

    public String getText() {
        return text;
    }

    public void setTrack(String text) {
        this.text = text;
    }


    public String getType() {
        return type;
    }

    /*public void setType(String fileName) {
        this.type = fileName;
    }*/


    protected Lyrics(Parcel in) {
        text = in.readString();
        type = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(type);
    }

    public static final Creator<Lyrics> CREATOR = new Creator<Lyrics>() {
        @Override
        public Lyrics createFromParcel(Parcel in) {
            return new Lyrics(in);
        }

        @Override
        public Lyrics[] newArray(int size) {
            return new Lyrics[size];
        }
    };

    @Override
    public boolean equals (Object object) {
        boolean result;
        if (object == null || object.getClass() != getClass()) {
            result = false;
        } else {
            Lyrics song = (Lyrics) object;
            result =  this.text.equals(song.getText());
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        hashCode = hashCode * 37 + this.text.hashCode();
        hashCode = hashCode * 37 + this.type.hashCode();

        return hashCode;
    }
}
