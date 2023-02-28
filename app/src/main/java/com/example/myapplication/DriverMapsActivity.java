package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback , RoutingListener {

    boolean isPermmsionGranter;
    GoogleMap googleMap;
    private int status = 0;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    private  LatLng destinationLatLon;
    Button logoutbtn, mSettings , mRrideStatus;
    private  String customerID ="" , destination , driverid;
    private Boolean isLoggingOut = false    ;
    LinearLayout mcustomerinfo;
    private TextView mCustomername, mCustomerPhone;

    Double driverLat , driverLon;
    LatLng pickupLatLan;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageView mCustomerProfile;

 androidx.appcompat.widget.Toolbar toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);

        mcustomerinfo= findViewById(R.id.customerinfo);
        mCustomername= findViewById(R.id.customerName);
        mCustomerPhone= findViewById(R.id.customerphonenumber);
        mSettings = findViewById(R.id.settings);
        mRrideStatus = findViewById(R.id.rideStatus);
        polylines = new ArrayList<>();
        logoutbtn = findViewById(R.id.logoutbutton);
        mCustomerProfile = (ImageView) findViewById(R.id.customerProfile);
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

//        drawerLayout.addDrawerListener(actionBarDrawerToggle);
//        actionBarDrawerToggle.syncState();




        mRrideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
switch (status){
    case 1:
        status = 2;
        erasePolyLines();
        if(destinationLatLon.latitude !=0.0 && destinationLatLon.longitude != 0.0){
            getRouterMaker(destinationLatLon);
        }
        mRrideStatus.setText("drive completed");
        break;

    case 2:
               recordRide();
             endRide();
        break;
}
            }
        });

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapsActivity.this, driverSetting.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut= true ;
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        getAssideCustomer();
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
                Toast.makeText(DriverMapsActivity.this, "Googl pleay servces are not availabel", Toast.LENGTH_LONG).show();

            }
        }
    }
private Marker pickUpMaker;
    private void endRide() {
        Toast.makeText(DriverMapsActivity.this, "End ride" , Toast.LENGTH_SHORT).show();

        mRrideStatus.setText("pick up cus again");
              erasePolyLines();
//CustomerRequest at the end
            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userid).child("CustomerRequest") ;
            driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerID);
        customerID = "";

        if(pickUpMaker != null){
            pickUpMaker.remove();
        }
        erasePolyLines();
        customerID = "";
        if(pickUpMarker != null){
            pickUpMarker.remove();
        }
        if(assigedCustomerPickupLocationRefLisner != null){
            assigedCustomerPickupLocationRef.removeEventListener(assigedCustomerPickupLocationRefLisner);

        }

        mcustomerinfo.setVisibility(View.GONE);
        mCustomername.setText("");
        mCustomerPhone.setText("");
        mCustomerProfile.setImageResource(R.mipmap.ic_launcher);

    }

    public void  recordRide(){
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userid).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(customerID).child("history");
        DatabaseReference  historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requesid = historyRef.push().getKey();
        driverRef.child(requesid).setValue(true);
        customerRef.child(requesid).setValue(true);

        HashMap map = new HashMap();
        map.put("driver" , userid);
        map.put("customer" , customerID);
        map.put("rating" , 0);
        map.put("timeStamp" , getCurrentTimeStamp());
        map.put("destination" , destination);
        map.put("location/from/lat" , pickupLatLan.latitude );
        map.put("location/from/Lon" , pickupLatLan.longitude );

        map.put("location/to/lat" , destinationLatLon.latitude );
        map.put("location/tp/Lon" , destinationLatLon.longitude );

        historyRef.child(requesid).updateChildren(map);


    }
 private Long getCurrentTimeStamp(){
 Long timeStamp =  System.currentTimeMillis()/1000;
     return timeStamp;
 }

    private void getAssideCustomer() {
        //customerRiderID at the end
          driverid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assigedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverid);
        assigedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                    if (datasnapshot.exists() && datasnapshot.getChildrenCount() > 0) {

                          Map<String , Object> map = (Map<String , Object>) datasnapshot.getValue();

                        status = 1;
                            customerID =  datasnapshot.getValue().toString();


                        if (map.get("customerRiderID") != null){
                            customerID = map.get("customerRiderID").toString();
                        }
                             getAssigedCustomerPickLocation();
                             getAssigedCustomerInfo();


                    }else{

                       endRide();

                    }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


        private void getAssigedCustomerInfo(){
            Toast.makeText(DriverMapsActivity.this, "User info show" , Toast.LENGTH_SHORT).show();

           mcustomerinfo.setVisibility(View.VISIBLE);
          DatabaseReference  mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(customerID);

            mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                    if(datasnapshot.exists() && datasnapshot.getChildrenCount() > 0){
                        Map<String, Object> map = (Map<String, Object>) datasnapshot.getValue();
                        if(map.get("name") != null){

                            mCustomername.setText(map.get("name").toString());
                        }
                        if(map.get("phone") != null){

                            mCustomerPhone.setText(map.get("phone").toString());
                        }
                        if (map.get("profileImageUrl") != null){
                            Glide.with(getApplicationContext()).load(map.get("profileImageUrl").toString()).into(mCustomerProfile);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.navigation_menu, menu);

        return true;
    }
    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            if(item.getItemId() == R.id.settingsPage){

                Toast.makeText(DriverMapsActivity.this, "No settings" , Toast.LENGTH_SHORT).show();

            }

        }





        return super.onOptionsItemSelected(item);
    }
    Marker pickUpMarker;
    private  DatabaseReference assigedCustomerPickupLocationRef;
    private  ValueEventListener assigedCustomerPickupLocationRefLisner;
    private void getAssigedCustomerPickLocation() {


          assigedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("CustomerRequest").child(customerID).child("l");
        assigedCustomerPickupLocationRefLisner=  assigedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if(datasnapshot.exists()){


                    List<Object> map = (List<Object>) datasnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());

                    }
                    LatLng driverlatLng = new LatLng(locationLat, locationLng);


                    pickUpMarker= googleMap.addMarker(new MarkerOptions().position(driverlatLng).title("pick up location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.authoritybutton)));
                    getRouterMaker(driverlatLng);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getRouterMaker( LatLng pickupLatLan) {

        Toast.makeText(DriverMapsActivity.this, "poly works" , Toast.LENGTH_SHORT).show();

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(driverLat, driverLon) , pickupLatLan)
                .build();
        routing.execute();

    }

//late found /////////////////////////////////////////////
//    private void getAssidedCustomerDestination(){
//        String driverid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference assigedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverid).child("customerRiderID");
//        assigedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
//              Map<String, Object> map = (Map<String, Object>) datasnapshot.getValue();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }


    private boolean checkGooglePlayservices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(result)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Toast.makeText(DriverMapsActivity.this, "user canced dialof", Toast.LENGTH_LONG).show();

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
                Toast.makeText(DriverMapsActivity.this, "Going settings", Toast.LENGTH_LONG).show();

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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

//        getCurrentLocationUpdate();
//        LatLng latLng = new LatLng(0, 0);
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
                            resolvableApiException.startResolutionForResult(DriverMapsActivity.this, 101);

                        } catch (IntentSender.SendIntentException ex) {
                            throw new RuntimeException(ex);
                        }
                        if (e.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                            Toast.makeText(DriverMapsActivity.this, "setting anvaible able no in dedice", Toast.LENGTH_LONG).show();

                        }
                    }
                }
            }
        });
    }

    private void getCurrentLocationUpdate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DriverMapsActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);


            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driverWorking");

                DatabaseReference refAvailabel = FirebaseDatabase.getInstance().getReference("driverAvailabel");

                GeoFire geoFireAvailable = new GeoFire(refAvailabel);
                GeoFire geoFireWorking = new GeoFire(refWorking);


                  driverLat = locationResult.getLastLocation().getLatitude();
                  driverLon = locationResult.getLastLocation().getLongitude();



                if(getApplicationContext() !=null) {
                 switch (customerID){
                     case "":
                         geoFireWorking.removeLocation(userid);
                         geoFireAvailable.setLocation(userid, new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));
                     break;

                     default:
                         geoFireAvailable.removeLocation(userid);
                         geoFireWorking.setLocation(userid, new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));

                         break;
                 }




//                    Toast.makeText(DriverMapsActivity.this, "location" + locationResult.getLastLocation().getLatitude() + " : " + locationResult.getLastLocation().getLongitude(), Toast.LENGTH_LONG).show();
                }
            }
        }, Looper.getMainLooper());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==101){

            if(resultCode==RESULT_OK){

                Toast.makeText(DriverMapsActivity.this, "now gps is enabled", Toast.LENGTH_LONG).show();

            }if(resultCode==RESULT_CANCELED){

                Toast.makeText(DriverMapsActivity.this, "cancelld", Toast.LENGTH_LONG).show();

            }
        }
    }

    public void disconnectDriver(){
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Location");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userid);
    }


    @Override
    protected void onStop() {
        if(!isLoggingOut){
            disconnectDriver();
        }

         String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailabel = FirebaseDatabase.getInstance().getReference("driverAvailabel");


            GeoFire geoFireAvailable = new GeoFire(refAvailabel);
            geoFireAvailable.removeLocation(userid);




        super.onStop();


    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e !=null){
            Toast.makeText(DriverMapsActivity.this, "error" + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(DriverMapsActivity.this, "sth wen wrrong", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }
    private List<Polyline> polylines;
    static final int[] COLORS = new int[]{com.karumi.dexter.R.color.design_default_color_primary_dark};

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shotestRouterIndex) {
if (polylines.size() > 0){
    for(Polyline poly : polylines){
        poly.remove();
    }
}
        polylines = new ArrayList<>();
for (int i =0 ; i <route.size(); i ++){

    int colorIndex = i % COLORS.length;
    PolylineOptions polylineOptions = new PolylineOptions();
    polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
    polylineOptions.width(10 + i + 3);
    polylineOptions.addAll(route.get(i).getPoints());
    Polyline polyline = googleMap.addPolyline(polylineOptions);
    polylines.add(polyline);

}
    }

    @Override
    public void onRoutingCancelled() {

    }
    public  void erasePolyLines(){
         for(Polyline line : polylines ){
             line.remove();
         }
         polylines.clear();
    }
}