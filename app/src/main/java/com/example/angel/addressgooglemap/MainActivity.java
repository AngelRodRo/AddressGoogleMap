package com.example.angel.addressgooglemap;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.location.Address;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.support.v4.app.FragmentActivity;
import java.util.List;
import java.util.Locale;


public class MainActivity extends FragmentActivity {

    GoogleMap map;
    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getGoogleMap();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Obtener y mostrar mapa
    public void getGoogleMap(){

        if(map==null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager
                    .findFragmentById(R.id.mapView);
            map = mapFragment.getMap();
            //map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
            map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
/*                    try {
                        double latitude = latLng.latitude;
                        double longitude = latLng.longitude;
                        Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
                        if (addresses.isEmpty()) {
                            Toast.makeText(getApplicationContext(),"Waiting for Location",Toast.LENGTH_LONG).show();
                        }
                        else {
                            if (addresses.size() > 0) {
                                Toast.makeText(getApplicationContext(), "Address: " + addresses.get(0).getFeatureName() + " " + addresses.get(0).getAdminArea() + " " +  addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }*/

                    try {
                        double latitude = latLng.latitude;
                        double longitude = latLng.longitude;
                        Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                        List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
                        String address = addresses.get(0).getFeatureName();
                        addMarker(latLng,address);
                    }
                    catch (Exception e) {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }
                }
            });
        }
    }

    public void addMarker(LatLng latLng, String address){
        marker = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Address : " + address)
        );

    }

    public void onClick(View view)
    {
        String url = "http://maps.google.com/maps?saddr=Deustua&daddr=tarata";

        Intent navigation = new Intent(Intent.ACTION_VIEW);
        navigation.setData(Uri.parse(url));

        startActivity(navigation);
        /*try
        {
            String url = "waze://?q=Deustua";
            Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse(url) );
            startActivity( intent );
        }
        catch ( ActivityNotFoundException ex  )
        {
            Intent intent =
                    new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
            startActivity(intent);
        }*/
    }
}
