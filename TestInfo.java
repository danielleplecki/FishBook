package com.example.android.fishbook;

import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;



public class TestInfo {
    public ArrayList<Post> samplePosts = new ArrayList<>();


    //Sample Posts
    Post p1 = new Post("Sanibel, FL", "Content B", "20/04/2017 11:32", 26.443397, -82.111512);
    Post p2 = new Post("Bonita Springs, FL", "Content C", "20/04/2017 14:17", 26.339806, -81.778697);
    Post p3 = new Post("Estero, FL", "Content A", "18/04/2017 21:44", 26.438136, -81.806752);
    Post p4 = new Post("Tampa, FL", "Content D", "29/04/2017 08:45", 27.950575, -82.457178);
    Post p5 = new Post("Fort Myers Beach, FL", "Content E", "30/04/2017 23:58", 26.452025, -81.948145);
    Post p6 = new Post("Estero 2", "Content F", "01/05/2017 06:31", 26.437328, -81.829102);


    public void createPosts(){
        samplePosts.add(p1);
        samplePosts.add(p2);
        samplePosts.add(p3);
        samplePosts.add(p4);
        samplePosts.add(p5);
        samplePosts.add(p6);
    }

    //**********************METHODS FROM APP ACTIVITIES***************************

    public Location setCurrentLocation(String area){
        Location location = new Location("");
        switch(area){
            case "Estero":
                location.setLatitude(26.434755);
                location.setLongitude(-81.809180);
                break;
            case "Tampa":
                location.setLatitude(27.907351);
                location.setLongitude(-82.450194);
                break;
            default:
                break;
        }
        return location;
    }

    public ArrayList<Post> withinRadius(ArrayList<Post> allPosts, double radius, Location currentLocation){
        ArrayList<Post> mPosts = new ArrayList<>();
        Iterator<Post> postIterator = allPosts.iterator();
        while (postIterator.hasNext()) {
            Post p = postIterator.next();
            Location postLocation = p.getLocation().createLocation();
            double distance = currentLocation.distanceTo(postLocation);
            if(distance <= radius){
                mPosts.add(p);
            }
        }
        return mPosts;
    }

    public ArrayList<Post> sortMostRecent(ArrayList<Post> allPosts){
        Collections.sort(allPosts, new PostDateComparator());
        Collections.reverse(allPosts);
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

    public ArrayList<Post> sortDistance(ArrayList<Post> allPosts){
        Collections.sort(allPosts, new PostDistanceComparator());
        return allPosts;
    }

    //Custom comparator to compare posts by distance
    class PostDistanceComparator implements Comparator<Post> {

        @Override
        public int compare(Post p1, Post p2) {
            Location currentLocation = setCurrentLocation("Estero");
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
