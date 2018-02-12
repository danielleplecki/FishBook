package com.example.android.fishbook;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, View.OnClickListener{
    private final double TO_METERS = 1609.344;
    private static Location currentLocation;
    private double searchRadius; //Set default radius to 10 miles
    private String sortBy = "";

    private static Location mLastLocation;
    private LocationRequest mLocationRequest;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private long UPDATE_INTERVAL = 10000;  //10 seconds
    private long FASTEST_INTERVAL = 5000; //5 seconds

    final ArrayList<Post> allPosts = new ArrayList<>();
    // mPostAdapter connects mPosts to mRecyclerView
    private RecyclerView newRecyclerView;
    private ArrayList<Post> mPosts = new ArrayList<>();
    private PostAdapter mPostAdapter;
    private View itemView;

    //declare views, buttons, and edit text for setting search location
    private View setLocationView;
    private Button useCurrentLocationButton;
    private Button useCustomLocationButton;
    private EditText customLocationInput;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Sets Toolbar instead of Actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Sets up view for setting search location, initially invisible
        setLocationView = findViewById(R.id.setSearchLocation);
        useCurrentLocationButton = (Button) findViewById(R.id.currentLocation_button);
        useCurrentLocationButton.setOnClickListener(this);
        useCustomLocationButton = (Button) findViewById(R.id.customLocation_button);
        useCustomLocationButton.setOnClickListener(this);
        customLocationInput = (EditText) findViewById(R.id.customLocation_editText);
        setLocationView.setVisibility(View.INVISIBLE);


        //Creates new RecyclerView
        newRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        newRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        //Sets the sort by criteria amd search radius  to the defaults of most recent and 10 miles
        sortBy = "MOST RECENT";
        searchRadius  = 10 * TO_METERS;


        // Creates an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //Populate page with posts in database
        mPostAdapter = new PostAdapter(mPosts);
        newRecyclerView.setAdapter(mPostAdapter);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        new PostAsyncTask(this).execute(firebaseDatabase);
    }


    //Executes action depending on what menu item is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_newPost:
                currentLocation = mLastLocation;
                startActivity(new Intent(MainActivity.this, NewPostActivity.class));
                break;
            case R.id.action_setLocation:
                setLocationView.setVisibility(View.VISIBLE);
                newRecyclerView.setClickable(false);//Makes posts unclickable
                break;
            case R.id.action_5miles:
                searchRadius = 5 * TO_METERS;
                withinRadius(allPosts, searchRadius);
                break;
            case R.id.action_10miles:
                searchRadius = 10 * TO_METERS;
                withinRadius(allPosts, searchRadius);
                break;
            case R.id.action_20miles:
                searchRadius = 20 * TO_METERS;
                withinRadius(allPosts, searchRadius);
                break;
            case R.id.action_50miles:
                searchRadius = 50 * TO_METERS;
                withinRadius(allPosts, searchRadius);
                break;
            case R.id.action_100miles:
                searchRadius = 100 * TO_METERS;
                withinRadius(allPosts, searchRadius);
                break;
            case R.id.action_distance:
                sortBy = "DISTANCE";
                Post.sortPosts(mPosts, sortBy);
                mPostAdapter.notifyDataSetChanged();
                break;
            case R.id.action_mostRecent:
                sortBy = "MOST RECENT";
                Post.sortPosts(mPosts, sortBy);
                mPostAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //OnClick method for buttons in the set search location popup window
    public void onClick(View v){
        switch(v.getId()){
            //Sets current location to the user's current location
            case R.id.currentLocation_button:
                currentLocation = mLastLocation;
                break;

            //Sets current location to location specified by user
            case R.id.customLocation_button:
                String customLocation = customLocationInput.getText().toString();
                currentLocation = getLocationFromAddress(customLocation);
                Toast.makeText(MainActivity.this,"Location updated to " +customLocation + ".", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
        //Sorts post within radius of new location
        withinRadius(allPosts, searchRadius);
        Post.sortPosts(mPosts, sortBy);
        mPostAdapter.notifyDataSetChanged();
        setLocationView.setVisibility(View.INVISIBLE);
        newRecyclerView.setClickable(true);

        //Gets rid of keyboard after a button is clicked
        hideKeyboard();
    }

    /**
     * Gets Location object from user inputted address
     * @param strAddress requested address
     * @return Location object with corresponding latitude and longitude
     */
    public Location getLocationFromAddress(String strAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        Location newLocation = new Location("");

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);

            newLocation.setLatitude(location.getLatitude());
            newLocation.setLongitude(location.getLongitude());
        }
        catch(IOException ex){
        }
        return newLocation;
    }

    /**
     * Updates the ArrayList in the PostAdapter class to include only posts within the specified radius
     * @param allPosts ArrayList of allPosts in database
     * @param radius user specified search radius
     */
    public void withinRadius(ArrayList<Post> allPosts, double radius){
        mPosts.removeAll(mPosts);
        Iterator<Post> postIterator = allPosts.iterator();
        while (postIterator.hasNext()) {
            Post p = postIterator.next();
            Location pLocation = p.getLocation().createLocation();
            if(currentLocation.distanceTo(pLocation) <= radius){
                mPosts.add(p);
            }
        }
        if(mPosts.size() == 0){
            Toast.makeText(MainActivity.this,"There are no posts within your selected radius", Toast.LENGTH_LONG).show();
        }
        mPostAdapter.notifyDataSetChanged();
    }

    /**
     * Hides keyboard from view
     */
    public void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static Location getCurrentLocation() {
        return currentLocation;
    }

    //Connects to Google API on start of app
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    //Disconnects from Google API on end of app
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    //Creates Toast to notify user that connection has failed
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(MainActivity.this, "Connection Failed", Toast.LENGTH_LONG).show();
    }

    //Once connected, last location is retrieved, if there is no last location a request to start
    //retrieving location updates is made
    public void onConnected(Bundle arg0) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException secEx) {
        }
        if (mLastLocation != null) {
            currentLocation = mLastLocation;
            mPostAdapter.notifyDataSetChanged();
        } else {
            startLocationUpdates();
        }
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        // Request location updates
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException secEx) {
        }
    }

    //If location is changed, user is notified by Toast
    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     * Parses the data from the Firebase database into a Post object
     * @param datasnapshot
     * @return
     */
    public Post snapshotToPost(DataSnapshot datasnapshot){
        String subject = (String) datasnapshot.child("subject").getValue();
        String content = (String) datasnapshot.child("content").getValue();
        String date = (String) datasnapshot.child("date").getValue();
        double latitude = (double) datasnapshot.child("location/latitude").getValue();
        double longitude = (double) datasnapshot.child("location/longitude").getValue();
        return new Post(subject, content, date, latitude, longitude);
    }

    /**
     * Class to get data from Firebase databases, collect into an ArrayList of Posts and puts them
     * into the PostAdapter to load into the RecyclerView
     */
    public class PostAsyncTask extends AsyncTask<FirebaseDatabase, Void, ArrayList<Post>>{
        Context context;
        public PostAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected ArrayList<Post> doInBackground(FirebaseDatabase... firebaseDatabases) {
            //Gets references to database
            FirebaseDatabase firebaseDatabase = firebaseDatabases[0];
            DatabaseReference allPostsReference = firebaseDatabase.getReference("all_posts");

            allPostsReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Post newPost = snapshotToPost(dataSnapshot);
                    allPosts.add(newPost);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return allPosts;
        }

        @Override
        protected void onPostExecute(ArrayList<Post> allPosts){
            for(Post tempPost : allPosts){
                mPosts.add(tempPost);
            }
            //Sorts post by most recent by default
            Post.sortPosts(mPosts, sortBy);
            mPostAdapter.notifyDataSetChanged();
        }
    }
}
