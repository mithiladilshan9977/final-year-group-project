package com.example.myapplication;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class historySingelActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {


    private String rideid, currentuserid ,customerid, driverid , userDriverOrCustomer;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private TextView ridelocation;
    private TextView ridedistance;
    private TextView ridedate;
    private TextView username;
    private TextView userphonenumber;

    private ImageView imageUser;
    private DatabaseReference historYRiderInforDB;
    private LatLng destinationLatLon , pickLatLon;
    private RatingBar mRatingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_singel);
        mMapFragment = (SupportMapFragment)  getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        rideid = getIntent().getExtras().getString("rideId");

        ridelocation = (TextView) findViewById(R.id.rideLocation);
        ridedistance = (TextView) findViewById(R.id.ridedistance);
        ridedate = (TextView) findViewById(R.id.ridedate);
        username = (TextView) findViewById(R.id.username);
        userphonenumber = (TextView) findViewById(R.id.userphone);
        mRatingBar = (RatingBar) findViewById(R.id.ratingbar);
        polylines = new ArrayList<>();

        currentuserid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        historYRiderInforDB = FirebaseDatabase.getInstance().getReference().child("history").child(rideid);
        getRiderInformation();



    }

    private void getRiderInformation() {
             historYRiderInforDB.addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                      if(datasnapshot.exists()){
                             for (DataSnapshot child : datasnapshot.getChildren())    {
                                 if (child.getKey().equals("customer")){
                                     customerid = child.getValue().toString();
                                     if(!customerid.equals(currentuserid)){
                                         userDriverOrCustomer = "Driver"  ;
                                         getUserInformation("Customer" , customerid);

                                     }
                                 }
                                 if (child.getKey().equals("driver")){
                                     driverid = child.getValue().toString();
                                     if(!driverid.equals(currentuserid)){
                                         userDriverOrCustomer = "Customer"  ;
                                         getUserInformation("Driver" , driverid);
                                         displayCustomerRelatedObject();
                                     }
                                 }
                                 if (child.getKey().equals("timeStamp")){
                                      ridedate.setText(getDate(Long.valueOf(child.getValue().toString())));

                                 }
                                 if (child.getKey().equals("rating")){
                                     mRatingBar.setRating(Integer.valueOf(child.getValue().toString()));

                                 }

                                 if (child.getKey().equals("destination")){
                                     ridelocation.setText(getDate(Long.valueOf(child.getValue().toString())));

                                 }
                                 if (child.getKey().equals("location")){
                                    pickLatLon = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()),Double.valueOf(child.child("from").child("Lon").getValue().toString()));
                                    destinationLatLon = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()),Double.valueOf(child.child("to").child("Lon").getValue().toString()));

                                    if(destinationLatLon != new LatLng(0,0)){
                                        getRouterMaker();
                                    }
                                 }


                             }
                      }
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {

                 }
             });
    }

    private void displayCustomerRelatedObject() {
        mRatingBar.setVisibility(View.VISIBLE);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                 historYRiderInforDB.child("rating").setValue(rating);
                 DatabaseReference mDriverRatingDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverid).child("rating");
                mDriverRatingDB.child(rideid).setValue(rating);
            }
        });
    }

    private void getUserInformation(String otherUserOrDriverOrCustomer, String otherUserId) {

        DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserOrDriverOrCustomer).child(otherUserId);
        mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                 if (datasnapshot.exists()){
                     Map<String, Object> map = (Map<String, Object>) datasnapshot.getValue();
                     if(map.get("name") != null){
                         username.setText(map.get("name").toString());

                     }
                     if(map.get("phone") != null){
                         userphonenumber.setText(map.get("phone").toString());

                     }

                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private String getDate(Long timeStamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timeStamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm" , cal).toString();
        return date;
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
  mMap = googleMap;
    }
    private void getRouterMaker() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickLatLon , destinationLatLon)
                .build();
        routing.execute();

    }


    private List<Polyline> polylines;
    static final int[] COLORS = new int[]{com.karumi.dexter.R.color.design_default_color_primary_dark};



    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shotestRouterIndex) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickLatLon);
        builder.include(destinationLatLon);

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(pickLatLon).title("pick location new"));
        mMap.addMarker(new MarkerOptions().position(destinationLatLon).title("destination"));



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
            Polyline polyline = mMap.addPolyline(polylineOptions);
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
    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

}