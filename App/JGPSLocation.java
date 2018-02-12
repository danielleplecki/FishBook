package com.example.android.fishbook;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by danie on 4/17/2017.
 */

public class GPSLocation implements Parcelable{
    private double latitude;
    private double longitude;

    public GPSLocation(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public GPSLocation(){
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Location createLocation(){
        Location newLocation = new Location("");
        newLocation.setLatitude(this.latitude);
        newLocation.setLongitude(this.longitude);
        return newLocation;
    }

    //Parcelable attributes
    protected GPSLocation(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GPSLocation> CREATOR = new Creator<GPSLocation>() {
        @Override
        public GPSLocation createFromParcel(Parcel in) {
            return new GPSLocation(in);
        }

        @Override
        public GPSLocation[] newArray(int size) {
            return new GPSLocation[size];
        }
    };
}
