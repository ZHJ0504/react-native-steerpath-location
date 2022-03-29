package com.reactnativesteerpathlocation;

import android.os.Parcel;
import android.os.Parcelable;

public class Setup implements Parcelable {

    private String apikey;
    private String setupName;
    private String setupRegion;
    private String user_number;
    private String liveApikey;


    public Setup() {}

    public Setup accessToken(String apikey) {
        this.apikey = apikey;
        return this;
    }

    public Setup name(String name) {
        this.setupName = name;
        return this;
    }

    public Setup region(String region) {
        this.setupRegion = region;
        return this;
    }

    public Setup liveAccessToken(String liveApikey) {
        this.liveApikey = liveApikey;
        return this;
    }

    public Setup userNumber(String number)
    {
        this.user_number = number;
        return this;
    }

    public String getApikey() {
        return apikey;
    }

    public String getName() {
        return setupName;
    }

    public String getRegion() {
        return setupRegion;
    }

    public String getLiveApikey(){
        return liveApikey;
    }

    public String getUser_num(){ return user_number;}

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Setup)) return false;

        Setup that = (Setup) obj;
        return this.apikey.equals(that.getApikey()) && this.setupName.equals(that.getName()) && this.setupRegion.equals(that.getRegion()) && this.user_number.equals(that.getUser_num());
    }

    public Setup(Parcel in) {
        apikey = in.readString();
        setupName = in.readString();
        setupRegion = in.readString();
        user_number = in.readString();
        liveApikey = in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(apikey);
        dest.writeString(setupName);
        dest.writeString(setupRegion);
        dest.writeString(user_number);
        dest.writeString(liveApikey);

    }

    @SuppressWarnings("unused")
    public static final Creator<Setup> CREATOR = new Creator<Setup>() {
        @Override
        public Setup createFromParcel(Parcel in) {
            return new Setup(in);
        }

        @Override
        public Setup[] newArray(int size) {
            return new Setup[size];
        }
    };
}
