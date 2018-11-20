package com.example.talit.location;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity implements DapatkanAlamatTask.onTaskSelesai {
    private Button btn;
    private Location mLastLocation;
    private LocationCallback mLocationCallback;
    private TextView mLocationTextView;
    private FusedLocationProviderClient mFusedLocationClient;
    private AnimatorSet mRotateAnimator;
    private ImageView mAndroidImageView;
    private boolean mTrackingLocation;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private PlaceDetectionClient mPlaceDetectionClient;
    private  String mLastPlaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationTextView = (TextView) findViewById(R.id.textMap);
        btn = (Button) findViewById(R.id.btn);
        mAndroidImageView = (ImageView) findViewById(R.id.image_view);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult);
// apabila tracking aktif, proses geocode jadi data alamat.
                if(mTrackingLocation){
                    new DapatkanAlamatTask(MainActivity.this, MainActivity.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };

        // buat animasi
        mRotateAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.rotate );
        mRotateAnimator.setTarget(mAndroidImageView);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getLocation();
//                mulaiTrackingLokasi();
                if (!mTrackingLocation){
                    mulaiTrackingLokasi();
                } else {
                    stopTrackingLokasi();
                }
                mLocationTextView.setText(getString(R.string.alamat_text,
                        "sedang mencari nama tempat",
                        "sedang mencari alamat",
                        System.currentTimeMillis()));
                mTrackingLocation = true;
                btn.setText("Stop tracking lokasi");
            }
        });
    }

//    private void getLocation(){
//        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,
//                    REQUEST_LOCATION_PERMISSION );
//        } else {
////            Log.d("GETPERMISSION", "getLocation : permission granted");
//            mFusedLocationClient.getLastLocation().addOnSuccessListener(
//                    new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            if(location != null){
////                                get lang long
//
////                                mLastLocation = location;
////                                mLocationTextView. setText(
////                                        getString(R.string.location_text,
////                                                mLastLocation.getLatitude(),
////                                                mLastLocation.getLongitude(),
////                                                mLastLocation.getTime())
////                                );
//                                new DapatkanAlamatTask(MainActivity.this, MainActivity.this).execute(location);
//                            } else {
//                                mLocationTextView.setText("Lokasi tidak tersedia");
//                            }
//                        }
//                    }
//            );
//        }
//        mLocationTextView.setText(getString(R.string.alamat_text, "sedang mencari alamat",
//                System.currentTimeMillis()));
//
//    }

    //end
//

    private void mulaiTrackingLokasi(){
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,
                    REQUEST_LOCATION_PERMISSION );
        } else {
//            Log.d("GETPERMISSION", "getLocation : permission granted");

            mFusedLocationClient.requestLocationUpdates
                    (getLocationRequest(), mLocationCallback,null );


           // mLocationTextView.setText(getString(R.string.alamat_text, "sedang mencari alamat",
                 //   System.currentTimeMillis()));
            mTrackingLocation = true;
            btn.setText("Stop Tracking Lokasi");
            mRotateAnimator.start();
        }
    }




    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:

                // jika permission dibolehkan maka getlocation,jika tidak dibolehkan, tampilkan peringatan

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mulaiTrackingLokasi();
                } else {
                    Toast.makeText(this, "tidak dapat permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskCompleted(final String result) throws SecurityException {
        if (mTrackingLocation) {
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(
                    new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if(task.isSuccessful()){
                            PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                            float maxLikelihood = 0;
                            Place currentPlace = null;

                            for (PlaceLikelihood placeLikelihood : likelyPlaces){

                                    if(maxLikelihood < placeLikelihood.getLikelihood()){
                                    maxLikelihood = placeLikelihood.getLikelihood();
                                    currentPlace = placeLikelihood.getPlace();
                                }
                                }
                                if (currentPlace !=null) {
                                    mLocationTextView.setText(
                                            getString(R.string.alamat_text,
                                                    currentPlace.getName(),
                                                    result, System.currentTimeMillis())
                                    );
                                    setTipeLokasi(currentPlace);
                                }
                                    likelyPlaces.release();
                            } else {
                                mLocationTextView.setText(
                                        getString(R.string.alamat_text,
                                                "nama lokasi tidak ditemukan!",
                                                result,
                                                System.currentTimeMillis())
                                );
                                }

                            }

                    });
            mPlaceDetectionClient.getCurrentPlace(null);

            // menampilkan alamat
           // mLocationTextView.setText(getString(R.string.alamat_text, result, System.currentTimeMillis()));
        }
    }



//        mLocationTextView.setText(getString(R.string.alamat_text, result, System.currentTimeMillis()));


//


    private void stopTrackingLokasi(){
        if(mTrackingLocation){
            mTrackingLocation = false;


            mFusedLocationClient.removeLocationUpdates(mLocationCallback); // menghapus req update location

            btn.setText("Mulai Tracking Lokasi");
            mLocationTextView.setText("Tracking sedang dihentikan");
            mRotateAnimator.end();
        }

    }

    // digunakan untuk menentukan frekuensi req dan tingkat akurasi dari update lokasi
    private LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);  //  untuk seberapa sering update lokasi yg diinginkan
        locationRequest.setFastestInterval(5000); // seberapa sering update lokasi dari app lain yang minta req location
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // untuk memilih akurasi tinggi menggunakan GPS
        return locationRequest;
    }
    private void setTipeLokasi(Place currentPlace){
        int drawableID = -1;
        for(Integer placeType : currentPlace.getPlaceTypes()){
            switch (placeType){
                case Place.TYPE_UNIVERSITY:
                    drawableID = R.drawable.campus;
                    break;
                case Place.TYPE_CAFE:
                    drawableID = R.drawable.coffee;
                    break;
                case Place.TYPE_SHOPPING_MALL:
                    drawableID = R.drawable.store;
                    break;
                case Place.TYPE_MOVIE_THEATER:
                    drawableID = R.drawable.movie;
                    break;
            }
        }
        if (drawableID < 0){
            drawableID = R.drawable.unknown;
        }
        mAndroidImageView.setImageResource(drawableID);
    }
}