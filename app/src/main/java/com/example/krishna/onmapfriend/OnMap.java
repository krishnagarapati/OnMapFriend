package com.example.krishna.onmapfriend;

import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by krishna on 8/27/2017.
 */

public class OnMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList names ;
    int count = 0;
    ArrayList latitude = new ArrayList();
    ArrayList longitude = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_on_map);
        Intent intent = getIntent();
        names = intent.getCharSequenceArrayListExtra(MainActivity.origin);
        count = names.size();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
       derive_coordinates();
    }

    public void derive_coordinates(){

        count = names.size(); int i = 0;
//        statusUpdate.setText("");
        String name = null;
        ContentResolver cr = getContentResolver();
        for(i=0;i<count;i++) {
            name = names.get(i).toString();
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                    "DISPLAY_NAME = '" + name + "'", null, null);
            if (cursor.moveToFirst()) {
                String contactId =cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                while (phones.moveToNext()) {
                    String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    //statusUpdate.append("Found"+name+" ");
                    switch (type) {
                        case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                            longitude.add(number);
                            //statusUpdate.append(" and longitude " + number);
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                            latitude.add(number);
                            //statusUpdate.append("with lattitude: " + number);
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                            break;
                    }
                }
                phones.close();

            }
            cursor.close();
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
   @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        for(int i = 0; i <count;i++){
            LatLng coordinate = new LatLng(
                    getCord(latitude.get(i).toString()), getCord(longitude.get(i).toString()));
            String tempName = names.get(i).toString();
            mMap.addMarker(new MarkerOptions().position(coordinate).title("Marker for " + tempName+"'s position"));
        }
    }

    public double getCord(String value){
        double coordinate = 0;
        String val = "";
        int position = 2;
        if(value.charAt(0) == '-') position = 3;
        val = value.substring(0,position)+ "." + value.substring(position,value.length());
        coordinate = Double.parseDouble(val);
        return coordinate;
    }
}
