package com.example.android.fishbook;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class NewPostActivity extends AppCompatActivity {
    DatabaseReference mDatabase;
    String newSubject;
    String newContent;
    String newDate;
    Location newLocation;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        final EditText subjectInput = (EditText) findViewById(R.id.subject_editText);
        final EditText contentInput = (EditText) findViewById(R.id.content_editText);
        final View postButton = findViewById(R.id.createPost_button);
        final View helpButton = findViewById(R.id.help_button);
        final View closePopUpButton = findViewById(R.id.exitSample_button);
        //Declare popUp window view and initially make it invisible
        final View popUpWindow = findViewById(R.id.popUp_View);
        popUpWindow.setVisibility(View.INVISIBLE);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Retrieve user input and current location/date/time
                newSubject = subjectInput.getText().toString();
                newContent = contentInput.getText().toString();
                newDate = dateFormat.format(Calendar.getInstance().getTime());
                newLocation = MainActivity.getCurrentLocation();
                //Create database reference
                mDatabase = FirebaseDatabase.getInstance().getReference();
                //Check if user put text in both fields
                if(newSubject.length() == 0 || newContent.length() == 0){
                    Toast.makeText(NewPostActivity.this,"Your post must have a subject and content.", Toast.LENGTH_LONG).show();
                }
                //New post created, user is notified and returned to MainActivity
                else{
                    writeNewPost(newSubject, newContent, newDate, newLocation);
                    Toast.makeText(NewPostActivity.this,"Post Created!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(NewPostActivity.this, MainActivity.class));
                }
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            //Opens popUp window on click of help button
            @Override
            public void onClick(View view) {
                popUpWindow.setVisibility(View.VISIBLE);
                hideKeyboard();
                closePopUpButton.setOnClickListener(new View.OnClickListener() {
                    //Closes popUp window on click of close button
                    @Override
                    public void onClick(View view) {
                        popUpWindow.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

    }

    //Adds new post to Firebase with unique key
    public void writeNewPost(String subject, String content, String date, Location location){
        Post newPost = new Post(subject, content, date, location.getLatitude(), location.getLongitude());
        DatabaseReference newRef = mDatabase.child("all_posts").push();
        newRef.setValue(newPost);
    }

    public void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
