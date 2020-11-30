package com.lyricslover.onelyrics.pojos;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable{

    private String track;
    private String artist;
    private String fileName;
    private long id;
    private long position;
    private long duration;
    private long lastModified;

    public Song(Song another) {
        this.id = another.id;
        this.track = another.track;
        this.artist = another.artist;
        this.position = another.position;
        this.duration = another.duration;
        this.fileName = another.fileName;
        this.lastModified = another.lastModified;
    }

    public Song(String track, String artist, long id) {
        this.track = track;
        this.artist = artist;
        this.id = id;
    }

    public Song(String track, String artist, long position, long duration) {
        this.track = track;
        this.artist = artist;
        this.position = position;
        this.duration = duration;
    }

    public Song(String track, String artist, String fileName) {
        this.track = track;
        this.artist = artist;
        this.fileName = fileName;
    }

    public Song(String track, String artist, String fileName, long lastModified) {
        this.track = track;
        this.artist = artist;
        this.fileName = fileName;
        this.lastModified = lastModified;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getFileName() {
        return fileName;
    }

    /*public void setFileName(String fileName) {
        this.fileName = fileName;
    }*/

    public long getLastModified() {
        return lastModified;
    }


    protected Song(Parcel in) {
        track = in.readString();
        artist = in.readString();
        fileName = in.readString();
        id = in.readLong();
        duration = in.readLong();
        position = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(track);
        dest.writeString(artist);
        dest.writeString(fileName);
        dest.writeLong(id);
        dest.writeLong(duration);
        dest.writeLong(position);
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public boolean equals (Object object) {
        boolean result;
        if (object == null || object.getClass() != getClass()) {
            result = false;
        } else {
            Song song = (Song) object;
            result =  this.artist.equals(song.getArtist()) && this.track.equals(song.getTrack());
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 37 + this.artist.hashCode();
        hashCode = hashCode * 37 + this.track.hashCode();
        return hashCode;
    }
}
