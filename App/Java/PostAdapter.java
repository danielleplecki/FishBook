package com.example.android.fishbook;

import android.content.Intent;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by danie on 4/11/2017.
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    ArrayList<Post> posts;
    public static final String POST = "POST";
    private final double TO_MILES = 0.000621371192;

    PostAdapter(ArrayList<Post> posts) {
        this.posts = posts;
    }


    /**
     * This function is called only enough times to cover the screen with views.  After
     * that point, it recycles the views when scrolling is done.
     *
     * @param parent   the intended parent object (our RecyclerView)
     * @param viewType unused in our function (enables having different kinds of views in the same RecyclerView)
     * @return the new ViewHolder we allocate
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // a LayoutInflater turns a layout XML resource into a View object.
        final View postItem = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.post_item, parent, false);
        return new ViewHolder(postItem);
    }


    /**
     * This function gets called each time a ViewHolder needs to hold data for a different
     * position in the list.
     *
     * @param holder   the ViewHolder that knows about the Views we need to update
     * @param position the index into the array of Posts
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Post post = posts.get(position);

        holder.subjectView.setText(post.getSubject());
        holder.dateView.setText(post.getDate());
        holder.contentView.setText(post.getContent());


        //Find and set distance between post and current location
        Location searchLocation = MainActivity.getCurrentLocation();
        String distance = "--";


        if(searchLocation != null){
            Location postLocation = post.getLocation().createLocation();
            //Get distance and convert from meters to miles
            double distanceInMeters = searchLocation.distanceTo(postLocation);
            double distanceInMiles = distanceInMeters * TO_MILES;
            //Set distance output to distance in miles rounded to 2 decimal points
            distance = Double.toString(Math.round(distanceInMiles * 100d) / 100d);

        }
        holder.distanceView.setText( distance + " mi");

        //Set OnClickListener to open DetailView
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MapsActivity.class);

                intent.putExtra(POST, post);
                v.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }



    /**
     * ViewHolder class for adapter that holds referneces to subviews
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView subjectView;
        public TextView contentView;
        public TextView dateView;
        public TextView distanceView;

        public ViewHolder(View postView){
            super(postView);
            subjectView = (TextView) postView.findViewById(R.id.subjectTextView);
            contentView = (TextView) postView.findViewById(R.id.contentTextView);
            dateView = (TextView) postView.findViewById(R.id.dateTextView);
            distanceView = (TextView) postView.findViewById(R.id.distanceTextView);
        }
    }


}



