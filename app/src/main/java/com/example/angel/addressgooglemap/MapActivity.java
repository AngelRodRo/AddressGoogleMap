package com.example.angel.addressgooglemap;

import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Marker;


public class MapActivity extends ActionBarActivity {

    GoogleMap map;
    Location location;
    Marker marker;
    int i,j=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
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

    public void getGoogleMap(){
        try{
            if(map==null)
                map =((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap(); //Mostrar mapa nuevo
            map.setMyLocationEnabled(true); //Muestra la localizacion actual
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() { //Detecta cambios de posicion y lo muestra
                @Override
                public void onMyLocationChange(Location _location) {
                    location=_location;
                    Double latitude = _location.getLatitude();
                    Double longitude = _location.getLongitude();

                    //Enviar localizacion del usuario como string compuesto de altitud y latitud
                    String message = Double.toString(latitude) + "  "  +Double.toString(longitude) ;
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();


                    //SendMessage(message);
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"No se pudo obtener mapa",Toast.LENGTH_SHORT).show();
        }
    }
}
