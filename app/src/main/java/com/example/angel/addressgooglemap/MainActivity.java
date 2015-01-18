package com.example.angel.addressgooglemap;

import android.location.Geocoder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.location.Address;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

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
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
            map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    try {
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
                    }
                }
            });
        }
    }

    public void addMarker(){
        marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(20,20))
                        .title("Mi marcador")
        );
    }
}
