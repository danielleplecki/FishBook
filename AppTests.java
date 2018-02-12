package com.example.android.fishbook;

import android.location.Location;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import static org.junit.Assert.*;

/**
 * Created by Danielle Plecki on 5/2/2017.
 */

@RunWith(AndroidJUnit4.class)
public class AppTests extends TestInfo{
    Location currentLocation;

    @Before
    public void setUp() throws Exception{
        createPosts();
    }

    @Test
    public void numResultsTest() throws Exception{
        ArrayList<Post> postsWithinRadius;

        //100 miles from Estero, FL
        currentLocation = setCurrentLocation("Estero");
        double radius = 160934.4; //100 miles
        postsWithinRadius = withinRadius(samplePosts, radius, currentLocation);
        assertTrue(postsWithinRadius.size() == 5);

        //5 miles from Estero, FL
        radius = 8046.72;
        postsWithinRadius = withinRadius(samplePosts, radius, currentLocation);
        assertTrue(postsWithinRadius.size() == 2);

        //50 miles from Tampa, FL
        currentLocation = setCurrentLocation("Tampa");
        radius = 80467.2;
        postsWithinRadius = withinRadius(samplePosts, radius, currentLocation);
        assertTrue(postsWithinRadius.size() == 1);
    }

    //Last post in the ArrayList will be the post that appears at the top of the screen, and therefore
    //the most recent post
    @Test
    public void sortMostRecentTest() throws Exception{
        sortMostRecent(samplePosts);
        String postSubject = samplePosts.get(5).getSubject();
        assertTrue(postSubject.equals("Estero, FL"));
    }

    //Last post in the ArrayList will be the post that appear at the top of the screen, and therefore
    // the post with the shortest distance
    @Test
    public void sortDistanceTest() throws Exception{
        currentLocation = setCurrentLocation("Estero");
        sortDistance(samplePosts);

        //Post closest to current location
        String postSubject = samplePosts.get(0).getSubject();
        assertEquals(postSubject, "Estero, FL");

        //Post furthest from current location
        postSubject = samplePosts.get(5).getSubject();
        assertEquals(postSubject, "Tampa, FL");
    }
}
