package com.example.android.fishbook;

import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by danie on 4/10/2017.
 */

public class Post implements Parcelable{
    private String subject;
    private String content;
    private String date;
    private GPSLocation location;

    public Post(){}

    public Post(String subject, String content, String date, double latitude, double longitude) {
        this.subject = subject;
        this.content = content;
        this.date = date;
        this.location = new GPSLocation(latitude, longitude);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public GPSLocation getLocation(){
        return location;
    }

    public void setLocation(double latitude,  double longitude){
        this.location = new GPSLocation(latitude, longitude);
    }

    //Parcelable attributes
    protected Post(Parcel in) {
        subject = in.readString();
        content = in.readString();
        date = in.readString();
        location = (GPSLocation) in.readValue(GPSLocation.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subject);
        dest.writeString(content);
        dest.writeString(date);
        dest.writeValue(location);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };




    /**
     * Method uses custom comparators to sort an ArrayList of Posts by a specified criteria
     * @param allPosts ArrayList of Posts
     * @param criteria what user wants to sort the posts by
     * @return sorted ArrayList of Posts
     */
    public static ArrayList<Post> sortPosts(ArrayList<Post> allPosts, String criteria){
        switch(criteria){
            case "MOST RECENT":
                Collections.sort(allPosts, new PostDateComparator());
                Collections.reverse(allPosts);
                break;
            case "DISTANCE":
                Collections.sort(allPosts, new PostDistanceComparator());
        }
        return allPosts;
    }

    //Custom comparator to compare posts by dates
    @RequiresApi(api = Build.VERSION_CODES.N)
    static class PostDateComparator implements Comparator<Post> {
        final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

        @Override
        public int compare(Post p1, Post p2) {
            Date d1 = null;
            Date d2 = null;
            try {
                d1 = dateFormat.parse(p1.getDate());
                d2 = dateFormat.parse(p2.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return d1.compareTo(d2);
        }
    }

    //Custom comparator to compare posts by distance
    static class PostDistanceComparator implements Comparator<Post> {

        @Override
        public int compare(Post p1, Post p2) {
            Location currentLocation = MainActivity.getCurrentLocation();
            Location p1Location = p1.getLocation().createLocation();
            Location p2Location = p2.getLocation().createLocation();
            double p1Distance = p1Location.distanceTo(currentLocation);
            double p2Distance = p2Location.distanceTo(currentLocation);
            if(p1Distance > p2Distance){
                return 1;
            }
            else if(p1Distance < p2Distance){
                return -1;
            }
            else{
                return 0;
            }
        }
    }
}
