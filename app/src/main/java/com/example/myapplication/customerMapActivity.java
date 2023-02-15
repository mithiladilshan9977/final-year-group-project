package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class customerMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    boolean isPermmsionGranter;
    GoogleMap googleMap;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    Button logoutbtn , mrequest, msettings , mHistory;
    Location mLastLocation;
    Double latitudenew , longitudenew;
    LatLng pickuplocation , destinationLatLng;
    Boolean requestBool = false;

    private Marker pickUpMaker;
    String destination , requestmservice;

    LinearLayout mcdriverinfo;
    private TextView mDrivername, mDriverPhone , mDriverCar;

    RadioGroup mRadioGroup;
    private RatingBar mRatingBar;
    private TextView mresturent, mhospitel;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);





        logoutbtn = findViewById(R.id.logoutbutton);
        mrequest = findViewById(R.id.request);
        msettings = findViewById(R.id.settings);

        mRatingBar = (RatingBar) findViewById(R.id.ratingbar);

        mresturent = (TextView) findViewById(R.id.returent);
        mhospitel = (TextView) findViewById(R.id.hospitel);

        mcdriverinfo= findViewById(R.id.driverinfo);
        mDrivername= findViewById(R.id.driverName);
        mDriverPhone= findViewById(R.id.driverphonenumber);
        mDriverCar= findViewById(R.id.drivercar);
        mHistory = findViewById(R.id.history);
        destinationLatLng = new LatLng(0.0,0.0);
       mRadioGroup = findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.userX);



        mresturent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder stringBuilder  = new StringBuilder("http://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                stringBuilder.append("location=" + latitudenew + longitudenew);
            }
        });
        msettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(customerMapActivity.this, customerSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });

        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(customerMapActivity.this, HistoryActivity.class);
                intent.putExtra("customerOrDriver" , "Customer") ;
                startActivity(intent);
                return;
            }
        });


        mrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(requestBool){
                   endRide();


                }else{
                    int selectid = mRadioGroup.getCheckedRadioButtonId();
                    final RadioButton radiobutton = (RadioButton) findViewById(selectid);
                    if(radiobutton.getText() == null){
                        return;
                    }
                    requestmservice = radiobutton.getText().toString();

                    requestBool= true;
                    String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");

                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userid , new GeoLocation(latitudenew, longitudenew));

                    pickuplocation =  new LatLng(latitudenew ,longitudenew );

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title("Pick me here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.carnew_ic_launcher));
                    markerOptions.position(pickuplocation);
                    pickUpMaker = googleMap.addMarker(markerOptions);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pickuplocation, 15);
                    googleMap.animateCamera(cameraUpdate);

                    mrequest.setText("Calling request");
                    getClosestDriver();
                }




            }
        });

        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(customerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        checkPermission();

        if (isPermmsionGranter) {
            if (checkGooglePlayservices()) {
                SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
                getSupportFragmentManager().beginTransaction().add(R.id.container, supportMapFragment).commit();
                supportMapFragment.getMapAsync(this);
                if (isPermmsionGranter) {
                    checkGPS();
                }

            } else {
                Toast.makeText(customerMapActivity.this, "Googl pleay servces are not availabel", Toast.LENGTH_LONG).show();

            }
        }

        mDriverPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                     Intent intent = new Intent(getApplicationContext() , ShowDriverChatPage.class);
                     intent.putExtra("PHONE_NUMBER" , mDriverPhone.toString());
                     intent.putExtra("DRIVER_NAME" , mDrivername.toString());
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);
            }
        });


    }
//            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
//            getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment1);
//
//                autocompleteFragment.setTypeFilter(TypeFilter.CITIES);

    private int radius = 2;
    private boolean driverFound = false;
    private  String driverFoundID ;
    GeoQuery geoQuery;
    private void getClosestDriver() {
        DatabaseReference driverlocation = FirebaseDatabase.getInstance().getReference().child("driverAvailabel");
        GeoFire geoFire = new GeoFire(driverlocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickuplocation.latitude, pickuplocation.longitude), radius );
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestBool){
                    DatabaseReference mCustomerdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(key);
                    mCustomerdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                             if(datasnapshot.exists() && datasnapshot.getChildrenCount() > 0){
                                 Map<String, Object> drivermap = (Map<String, Object>) datasnapshot.getValue();
                                 if(driverFound){
                                     return;
                                 }
                                 if(drivermap.get("service").equals(requestmservice)){
                                     driverFound = true;
                                     driverFoundID = datasnapshot.getKey();
                                     DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID);
                                     String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                     HashMap map = new HashMap();
                                     map.put("customerRiderID", customerID);
                                     map.put("destinationLat", destinationLatLng.latitude);
                                     map.put("destinationLon", destinationLatLng.longitude);
                                     driverRef.updateChildren(map);

                                     getDriverLocation();
                                     gerDriverInfo();
                                     gethasRiderEnded();
                                     mrequest.setText("Looking for driver location");
                                 }
                             }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
        //increse the reqdius by one
                if(!driverFound){
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }



    private void gerDriverInfo(){
        mcdriverinfo.setVisibility(View.VISIBLE);
        DatabaseReference  mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID);

        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if(datasnapshot.exists() && datasnapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) datasnapshot.getValue();
                    if(map.get("name") != null){

                        mDrivername.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){

                        mDriverPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("car") != null){

                        mDriverCar.setText(map.get("car").toString());
                    }
                    int mRatingSum = 0;
                    int ratingTotal = 0;
                    float ratingAvg = 0;
                    for (DataSnapshot child : datasnapshot.child("rating").getChildren()){
                        mRatingSum = mRatingSum + Integer.valueOf(child.getValue().toString());
                        ratingTotal++;
                    }
                    if(ratingTotal !=0){
                        ratingAvg = mRatingSum / ratingTotal;
                        mRatingBar.setRating(ratingAvg);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
private  DatabaseReference drivehasendedRef;
    private  ValueEventListener drivehasendedRefLisnter;
    private void gethasRiderEnded() {
          drivehasendedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("customerRiderID");
        drivehasendedRefLisnter= drivehasendedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if(datasnapshot.exists() && datasnapshot.getChildrenCount() > 0) {
                    if (datasnapshot.exists()) {


                    }else{

                         endRide();
                    }
                }
                else{
                    Toast.makeText(customerMapActivity.this, "No user found" , Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void endRide() {
        requestBool = false;
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationLister);
        drivehasendedRef.removeEventListener(drivehasendedRefLisnter);

        if(driverFoundID != null){

            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("CustomerRequest");
            driverRef.removeValue();
            driverFoundID = null;

        }
        driverFound = false ;
        radius = 1;
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userid);

        if(pickUpMaker != null){
            pickUpMaker.remove();
        }
        mrequest.setText("Customer make call");
        mcdriverinfo.setVisibility(View.GONE);
        mDrivername.setText("");
        mDriverPhone.setText("");
        mDriverCar.setText("");
    }


    private Marker mDriverMaker;
    private   DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationLister;
    private void getDriverLocation() {
          driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driverWorking").child(driverFoundID).child("l");
          driverLocationLister = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                 if(datasnapshot.exists() && requestBool){
                     List<Object> map = (List<Object>) datasnapshot.getValue();
                     double locationLat = 0;
                     double locationLng = 0;
                     mrequest.setText("Driver Found");
                     if(map.get(0) != null){
                         locationLat = Double.parseDouble(map.get(0).toString());
                     }
                     if(map.get(1) != null){
                         locationLng = Double.parseDouble(map.get(1).toString());

                     }
                     LatLng driverlatLng = new LatLng(locationLat, locationLng);
                      if(mDriverMaker != null){
                          mDriverMaker.remove();
                      }
                      Location loc1 = new Location("");
                     loc1.setLatitude(pickuplocation.latitude);
                     loc1.setLongitude(pickuplocation.longitude);


                     Location loc2 = new Location("");
                     loc2.setLatitude(driverlatLng.latitude);
                     loc2.setLongitude(driverlatLng.longitude);

                     float distance = loc1.distanceTo(loc2);
                     if(distance < 100){
                         mrequest.setText("Driver has arrived");
                     }else{
                         mrequest.setText(String.valueOf(distance) + " m");

                     }

                     mDriverMaker = googleMap.addMarker(new MarkerOptions().position(driverlatLng).title("Your host").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));



                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private boolean checkGooglePlayservices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(result)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Toast.makeText(customerMapActivity.this, "user canced dialof", Toast.LENGTH_LONG).show();

                }
            });
            dialog.show();

        }
        return false;
    }

    private void checkPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isPermmsionGranter = true;

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Toast.makeText(customerMapActivity.this, "Going settings", Toast.LENGTH_LONG).show();

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMapnew) {
        googleMap = googleMapnew;

//        getCurrentLocationUpdate();
//        LatLng latLng = new LatLng(latitudenew, longitudenew);
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.title("location");
//        markerOptions.position(latLng);
//        googleMap.addMarker(markerOptions);
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
//        googleMap.animateCamera(cameraUpdate);

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            return;
        }
        googleMap.setMyLocationEnabled(true);


    }

    private void checkGPS() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdateDelayMillis(1000)
                .build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        Task<LocationSettingsResponse> locationSettingsResponseTask = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        locationSettingsResponseTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse respose = task.getResult(ApiException.class);
                    getCurrentLocationUpdate();

                } catch (ApiException e) {
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        try {
                            resolvableApiException.startResolutionForResult(customerMapActivity.this, 101);

                        } catch (IntentSender.SendIntentException ex) {
                            throw new RuntimeException(ex);
                        }
                        if (e.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                            Toast.makeText(customerMapActivity.this, "setting anvaible able no in dedice", Toast.LENGTH_LONG).show();

                        }
                    }
                }
            }
        });
    }

    private void getCurrentLocationUpdate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(customerMapActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                  latitudenew = locationResult.getLastLocation().getLatitude();
                  longitudenew = locationResult.getLastLocation().getLongitude();

                String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Location");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userid , new GeoLocation(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude() ));

//                Toast.makeText(customerMapActivity.this, "location new" + latitudenew + " -- " + longitudenew  , Toast.LENGTH_LONG).show();

            }
        }, Looper.getMainLooper());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==101){

            if(resultCode==RESULT_OK){

                Toast.makeText(customerMapActivity.this, "now gps is enabled", Toast.LENGTH_LONG).show();

            }if(resultCode==RESULT_CANCELED){

                Toast.makeText(customerMapActivity.this, "cancelld", Toast.LENGTH_LONG).show();

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
          getMenuInflater().inflate(R.menu.menu, menu);
          return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
     if(item.getItemId() == R.id.noneMap){
         googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
     }
        if(item.getItemId() == R.id.NoremalMap){
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        if(item.getItemId() == R.id.satelliteMap){
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        if(item.getItemId() == R.id.maphybride){
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        if(item.getItemId() == R.id.mapTerrain){
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}