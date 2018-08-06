package io.github.spyguy001.icanpick;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    final String latitude = "40.7463956";
    final String longtitude = "-73.9852992";
    private static final String TAG = "MainActivity";
    Button picker;
    SearchView searchBar;
    SeekBar distancer;
    TextView textViewDist;
    TextView textViewTags;
    ArrayList<String> currTags= new ArrayList<>(); // This should reset after app close
    public static final String TAGS = "TAGS TO BE USED";
    public static final String DISTANCE = "DISTANCE TO BE USED";
    public static final int RESULT_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.picker = findViewById(R.id.button);



        this.searchBar = findViewById(R.id.searchBar);
        searchBar.setOnQueryTextListener(searchQueryListener);

        // This is the distance slider and it's text box
        this.distancer = findViewById(R.id.seekBar);
        distancer.setProgress(2);
        this.textViewDist =  findViewById(R.id.textViewDist);
        distancer.setOnSeekBarChangeListener(seekBarChangeListener);
        textViewDist.setText("0 Km(s)");

        //This displays the current tags
        textViewTags = findViewById(R.id.textViewTags);

        //TextView tv1 = (TextView)findViewById(R.id.text);
        //tv1.setText("Hey");

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        String placeId = "ChIJv1rQpeoVK4gRd4bDfGYxLTU";
        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place myPlace = places.get(0);
                    Log.i(TAG, "Place found: " + myPlace.getName());
                    TextView tv1 = (TextView)findViewById(R.id.text);
                    tv1.setText(myPlace.getName());
                    places.release();
                } else {
                    Log.e(TAG, "Place not found.");
                }
            }
        });
    }


    protected void onResume(){
        super.onResume();
        this.picker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToRestaurantView();
            }
        });
    }

    protected void goToRestaurantView(){
        Intent intent = new Intent(this, RestaurantActivity.class);
        intent.putStringArrayListExtra(TAGS,currTags);
        int progress = distancer.getProgress();
        intent.putExtra(DISTANCE,progress);
        this.startActivityForResult(intent,RESULT_REQUEST);
    }

    private SearchView.OnQueryTextListener searchQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            currTags.add(s);
            textViewTags.append(s+ ", ");
            searchBar.setQuery("",false);
            searchBar.setIconified(true);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }

    };

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int distance, boolean b) {
            textViewDist.setText(String.valueOf(distance) + " Km(s)");

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };


    private void displayTags(TextView textView){

    }
}
