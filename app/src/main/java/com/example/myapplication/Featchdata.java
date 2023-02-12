package com.example.myapplication;

import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Featchdata extends AsyncTask<Object, String , String> {

    String googleNearByPlaceData;
    GoogleMap googleMap;
    String url;

    @Override
    protected String doInBackground(Object... objects) {
      try {
          googleMap = (GoogleMap) objects[0];
          url = (String) objects[1];
          DownloadUrl downloadUrl = new DownloadUrl();
          googleNearByPlaceData = downloadUrl.retriveUrl(url);

      } catch (IOException e) {
          throw new RuntimeException(e);
      }
      return googleNearByPlaceData;
    }

    @Override
    protected void onPostExecute(String s) {
         try{
             JSONObject jsonObject = new JSONObject(s);
             JSONArray jsonArray = jsonObject.getJSONArray("results");

             for (int i =0 ; i< jsonArray.length();i++){
                 JSONObject jsonObject1 = new JSONObject(String.valueOf(i));
                 JSONObject getlocation = jsonObject1.getJSONObject("geometry").getJSONObject("location");

                 String lat = getlocation.getString("latitudenew");
                 String lon = getlocation.getString("longitudenew");

                 JSONObject getname = jsonArray.getJSONObject(i);
                 String name = getname.getString("name");

                 LatLng latLng = new LatLng(Double.parseDouble(lat) , Double.parseDouble(lon));
                 MarkerOptions markerOptions = new MarkerOptions();
                 markerOptions.title(name);
                 markerOptions.position(latLng);
                 googleMap.addMarker(markerOptions);
                 googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));



             }

         } catch (JSONException e) {
             throw new RuntimeException(e);
         }
    }
}
