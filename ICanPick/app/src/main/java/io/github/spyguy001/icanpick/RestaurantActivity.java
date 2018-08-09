package io.github.spyguy001.icanpick;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

public class RestaurantActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    Button back;
    Button redo;
    Button select;
    ArrayList<String> allTags= new ArrayList<>(); //This should be remembered after app closure
    ArrayList<String> currTags= new ArrayList<>();
    int distance;
    GeoDataClient geoDataClient;
    PlaceDetectionClient placeDetectionClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    public static final String TAG = "RestaurantActivity";

    protected void goToMainView(){
        //Intent intent = new Intent();
        //intent.putExtra(MainActivity.RESULT_REQUEST,);
        //this.startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        this.imageView = findViewById(R.id.imageView);
        this.textView = findViewById(R.id.textView2);
        this.back = findViewById(R.id.buttonBack);
        this.redo = findViewById(R.id.buttonReroll);
        this.select = findViewById(R.id.buttonSelect);

        Intent intent = getIntent();
        //TODO Add the tags to allTags after checking for dupes
        allTags.addAll(intent.getStringArrayListExtra(MainActivity.TAGS));

        currTags.addAll(intent.getStringArrayListExtra(MainActivity.TAGS));
        this.distance = intent.getIntExtra(MainActivity.DISTANCE, 2);

        textView.setText("This place is less than "+distance+" kms away and i feel like getting " );

        for (String tag:currTags){
            textView.append(tag +", ");
        }

        // Construct a GeoDataClient.
        geoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        placeDetectionClient = Places.getPlaceDetectionClient(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    protected void onResume(){
        super.onResume();
        this.loadRestaurant();
        this.back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToMainView();
            }
        });
    }

    protected void loadRestaurant(){
        try{
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location curLoc;
                            curLoc = task.getResult();
                            // Set the map's camera position to the current location of the device.
                            Log.i(TAG, "found a location " + curLoc.getLatitude() + " " + curLoc.getLongitude());
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
            else{
                throw new SecurityException("permission was denied");
            }
        }
        catch(SecurityException e){
            Log.e("Exception: %s", e.getMessage());
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
    }

    protected void logCurrentPlace(){
    }

    protected void loadPlace(Place currentPlace){
        getPhotos();
        String PLACEREPORT_REVIEW = "current";
        final PlaceReport report = PlaceReport.create(currentPlace.getId(), PLACEREPORT_REVIEW);
        placeDetectionClient.reportDeviceAtPlace(report);
        this.textView.setText(currentPlace.getName());
    }

    private void getPhotos() {
        final String placeId = "ChIJa147K9HX3IAR-lwiGIQv9i4";
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = geoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = geoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        Bitmap bitmap = photo.getBitmap();
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }



    //TODO : Write a function that will add to allTags and maybe check for dupes
}