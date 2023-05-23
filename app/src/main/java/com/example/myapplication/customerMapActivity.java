package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

import es.dmoral.toasty.Toasty;

public class customerMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    boolean isPermmsionGranter;
    GoogleMap googleMap;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    Button logoutbtn , mrequest  , show , mbottmSheetButton,mgotoimagechat;
    Location mLastLocation;
    Double latitudenew , longitudenew;
    LatLng pickuplocation , destinationLatLng;
    Boolean requestBool = false;

    private Marker pickUpMaker , mDriverMaker;
    String destination , requestmservice , userid , DriverHistoryPhone , DriverHistoryName , DriverDiscription;

    LinearLayout mcdriverinfo;
    private TextView mDrivername, mDriverPhone , mDriverCar;

    RadioGroup mRadioGroup;
    private RatingBar mRatingBar;
    private TextView mresturent, mhospitel;
    BottomSheetDialog dialog;
    private ImageView mdriverProfile;
    private String policeStationLocation;

 private Uri driverPicUri;

   private MediaPlayer mediaPlayer;
    private   DatabaseReference  driverLocationRef;
    private ValueEventListener driverLocationLister;
    private int radius = 2;
    private boolean driverFound = false;
    private  String driverFoundID ;
    GeoQuery geoQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);




        dialog = new BottomSheetDialog(this);

        mRatingBar = (RatingBar) findViewById(R.id.ratingbar);

          mbottmSheetButton = (Button) findViewById(R.id.bottmSheetButton);

        mcdriverinfo= findViewById(R.id.driverinfo);
        mDrivername= findViewById(R.id.driverName);
        mDriverPhone= findViewById(R.id.driverphonenumber);
        mDriverCar= findViewById(R.id.drivercar);
        mdriverProfile = (ImageView) findViewById(R.id.driverProfile);
        destinationLatLng = new LatLng(0.0,0.0);
       mRadioGroup = findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.userX);
        mediaPlayer = MediaPlayer.create(customerMapActivity.this, R.raw.notificationsound);
        mgotoimagechat = (Button) findViewById(R.id.gotoimagechat);

        mgotoimagechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , ImageChatActivity.class);
                intent.putExtra("DRIVER_NAME" ,mDrivername.getText().toString() );
                intent.putExtra("DRIVER_PHONE" ,mDriverPhone.getText().toString() );
                intent.putExtra("DRIVER_FOUND_ID" , driverFoundID);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        createDialog();
       mbottmSheetButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               dialog.show();
           }
       });
        mrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                      dialog.dismiss();
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
                      userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");

                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userid , new GeoLocation(latitudenew, longitudenew));

                    pickuplocation =  new LatLng(latitudenew ,longitudenew );

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title("I need help").icon(BitmapDescriptorFactory.fromResource(R.mipmap.customerlocation));
                    markerOptions.position(pickuplocation);
                    pickUpMaker = googleMap.addMarker(markerOptions);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pickuplocation, 15);
                    googleMap.animateCamera(cameraUpdate);

                    mrequest.setText("Calling...");

                    getClosestDriver();
                }




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
                Toasty.error(getApplicationContext(),"Google play services not available", Toast.LENGTH_LONG, true).show();



            }
        }

        mDriverPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                     Intent intent = new Intent(getApplicationContext() , ShowUserChatPage.class);
                     intent.putExtra("DRIVER_NAME" ,mDrivername.getText().toString() );
                     intent.putExtra("DRIVER_PHONE" ,mDriverPhone.getText().toString() );
                     intent.putExtra("DRIVER_FOUND_ID" , driverFoundID);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);
            }
        });


    }

    private void createDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_layout, null , false);
        mrequest = view.findViewById(R.id.request);

        dialog.setContentView(view);

    }


//            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
//            getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment1);
//
//                autocompleteFragment.setTypeFilter(TypeFilter.CITIES);


    private void getClosestDriver() {


        // endRide();
        // turn adriver avaible into driverWorking
        DatabaseReference driverlocation = FirebaseDatabase.getInstance().getReference().child("driverAvailabel");
        GeoFire geoFire = new GeoFire(driverlocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickuplocation.latitude, pickuplocation.longitude), radius );
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestBool){

               driverFound = true;
//                          driverFoundID = key;key
                    DatabaseReference mCustomerdatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child( key);

                    mCustomerdatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                            //later implemented
                             if(datasnapshot.exists() && datasnapshot.getChildrenCount() > 0){
                                 Map<String, Object> drivermap = (Map<String, Object>) datasnapshot.getValue();

                          // made driverfound inti not found
                                 if(!driverFound){

                                     return;
                                 }
                                 if (drivermap.get("service") != null){

                                     if(drivermap.get("service").equals(requestmservice)){

                                         driverFound = true;
                                         driverFoundID = datasnapshot.getKey();
                                         DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child( driverFoundID);

                                         String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                         HashMap map = new HashMap();
                                         map.put("customerRiderID", customerID);


                                         map.put("destinationLat", destinationLatLng.latitude);
                                         map.put("destinationLon", destinationLatLng.longitude);
                                         driverRef.updateChildren(map);

                                         mrequest.setText("Looking for your request");

                                         getDriverLocation();
                                         gerDriverInfo();
                                         gethasRiderEnded();

                                     }
                                 }else {
                                     Toasty.error(getApplicationContext(),"No information yet", Toast.LENGTH_LONG, true).show();



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

        mediaPlayer.start();
        putIntoHistoryPage();
        mcdriverinfo.setVisibility(View.VISIBLE);
        DatabaseReference  mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID);

        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
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
                    if (map.get("profileImageUrl") != null){
                        Glide.with(getApplicationContext()).load(map.get("profileImageUrl").toString()).into(mdriverProfile);
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

    public  void  putIntoHistoryPage(){
        DatabaseReference  mCustomerHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID);
        mCustomerHistoryDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                 if (datasnapshot.exists() && datasnapshot.getChildrenCount() > 0){

                     Map<String, Object> map = (Map<String, Object>) datasnapshot.getValue();
                     if(map.get("name") != null){

                           DriverHistoryName = map.get("name").toString();
                     }
                     if (map.get("phone") != null){
                           DriverHistoryPhone = map.get("phone").toString();
                     }
                     if (map.get("car") != null){
                         DriverDiscription = map.get("car").toString();
                     }


                     InsertIntoCustomerHistory();
                 }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public  void  InsertIntoCustomerHistory(){
        DatabaseReference insertDriverInfoIntoCustomer = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(userid).child("history").child(driverFoundID);
        HashMap map = new HashMap();

        map.put("driverName", DriverHistoryName );
        map.put("driverPhone", DriverHistoryPhone );
        map.put("driverDiscription", DriverDiscription );
//        map.put("driverProfile",  driverPicUri );
        insertDriverInfoIntoCustomer.setValue(map);
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
                    Toasty.warning(getApplicationContext(),"No user found", Toast.LENGTH_LONG, true).show();


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





    private void getDriverLocation() {
          driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driverWorking").child(driverFoundID).child("l");
          driverLocationLister = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                // in here
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

                     mDriverMaker = googleMap.addMarker(new MarkerOptions().position(driverlatLng).title("Your host").icon(BitmapDescriptorFactory.fromResource(R.mipmap.authoritybutton)));



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

        if (driverLocationRef != null){
            driverLocationRef.removeEventListener(driverLocationLister);

        }
        if (drivehasendedRef != null){
            drivehasendedRef.removeEventListener(drivehasendedRefLisnter);

        }


        if(driverFoundID != null){
            //part 12 9.42 time
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("CustomerRequest");
            driverRef.removeValue();
            driverFoundID = null;

        }
        driverFound = false ;
        radius = 1;
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("CustomerRequest");
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
        mdriverProfile.setImageResource(R.mipmap.ic_launcher);
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
                    Toasty.error(getApplicationContext(),"User cancelled dialog", Toast.LENGTH_LONG, true).show();



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
                            Toasty.error(getApplicationContext(),"Setting are not available in your device", Toast.LENGTH_LONG, true).show();



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

        // put the if condition here to check userid
                if (userid != null){
                    String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Location");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userid , new GeoLocation(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude() ));

                }

//                Toast.makeText(customerMapActivity.this, "location new" + latitudenew + " -- " + longitudenew  , Toast.LENGTH_LONG).show();

            }
        }, Looper.getMainLooper());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==101){

            if(resultCode==RESULT_OK){
                Toasty.success(getApplicationContext(),"GPS is enabled", Toast.LENGTH_LONG, true).show();



            }if(resultCode==RESULT_CANCELED){
                Toasty.error(getApplicationContext(),"GPS is not enabled", Toast.LENGTH_LONG, true).show();



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
        if(item.getItemId() == R.id.settingsPage){
            Intent intent = new Intent(customerMapActivity.this, customerSettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);


        }
        if(item.getItemId() == R.id.historyPagenew){

            Intent intent = new Intent(customerMapActivity.this, HistoryActivity.class);
            intent.putExtra("customerOrDriver" , "Customer") ;

            startActivity(intent);


        }
        if(item.getItemId() == R.id.logoutPage){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(customerMapActivity.this, MainActivity.class);
            startActivity(intent);
            finish();


        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}