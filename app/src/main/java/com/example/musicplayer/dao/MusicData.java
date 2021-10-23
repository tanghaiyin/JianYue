package com.example.musicplayer.dao;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class MusicData implements Parcelable {
    private String musicName;
    private Bitmap musicImg;
    private String path;

    protected MusicData(Parcel in) {
        musicName = in.readString();
        musicImg = in.readParcelable(Bitmap.class.getClassLoader());
        path = in.readString();
    }

    public static final Creator<MusicData> CREATOR = new Creator<MusicData>() {
        @Override
        public MusicData createFromParcel(Parcel in) {
            return new MusicData(in);
        }

        @Override
        public MusicData[] newArray(int size) {
            return new MusicData[size];
        }
    };

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MusicData(String musicName, String path) {
        this.musicName = musicName;
        this.musicImg = musicImg;
        this.path = path;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public Bitmap getMusicImg() {
        return musicImg;
    }

    public void setMusicImg(Bitmap musicImg) {
        this.musicImg = musicImg;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(musicName);
        dest.writeParcelable(musicImg, flags);
        dest.writeString(path);
    }
}
