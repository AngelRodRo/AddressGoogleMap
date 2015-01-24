package com.example.angel.addressgooglemap;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class list_notifications extends ActionBarActivity {

    private ListView list;

    GoogleCloudMessaging gcm;

    Context context;
    public static final String REG_ID = "regId";
    private static final String APP_VERSION = "appVersion";
    private static final String REGISTERED = "registered";
    boolean registered=true;
    ShareExternalServer appUtil;
    String regId="";
    AsyncTask<Void, Void, String> shareRegidTask;
    static final String TAG = "Register Activity";

    //Marker marker;
    int j=0;
    GoogleMap map;
    Double longitude,latitude;
    String distance = "";
    String duration = "";

    GoogleMap gmMap;
    Location locMyposition;
    //LatLng latLngMyposition;
    Location locDestinlocation;
    LocationManager locationManager;
    LocationListener locationListener;

    Lista_adaptador lista_adaptador;
    ArrayList<Lista_entrada> datos = new ArrayList<Lista_entrada>();

    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_notifications);
        context = getApplicationContext();
        appUtil = new ShareExternalServer();

        //Creamos un array para los parametros
        lista_adaptador = new Lista_adaptador(this,R.layout.entrada,datos) {
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {
                    TextView texto_superior_entrada = (TextView) view.findViewById(R.id.textView_superior);
                    if (texto_superior_entrada != null)
                        texto_superior_entrada.setText(((Lista_entrada) entrada).get_textoEncima());

                    TextView texto_inferior_entrada = (TextView) view.findViewById(R.id.textView_inferior);
                    if (texto_inferior_entrada != null)
                        texto_inferior_entrada.setText(((Lista_entrada) entrada).get_textoDebajo());

                    TextView texto_medio_entrada = (TextView) view.findViewById(R.id.textView_medio);
                    if (texto_medio_entrada != null)
                        texto_medio_entrada.setText(((Lista_entrada) entrada).get_textomedio());

                    ImageView imagen_entrada = (ImageView) view.findViewById(R.id.imageView_imagen);
                    if (imagen_entrada != null)
                        imagen_entrada.setImageResource(((Lista_entrada) entrada).get_idImagen());
                }
            }
        };
        list = (ListView) findViewById(R.id.ListView_listado);
        list.setAdapter(lista_adaptador);

        UpdateMyPosition();

        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(GCMNotificationIntentService.ACTION_MyIntentService);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                try
                {
                    Lista_entrada elegido = (Lista_entrada) pariente.getItemAtPosition(posicion);
                    String url = "waze://?ll=" + elegido.getLatitude() +"," + elegido.getLongitude()+"&navigate=yes";
                    //String url = "waze://?q="+elegido.get_textomedio();
                    Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
                    startActivity(intent);
                }
                catch ( ActivityNotFoundException ex  )
                {
                    Intent intent =
                            new Intent( Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze") );
                    startActivity(intent);
                }
            }
        });

        if (regId.isEmpty()) {
            regId = registerGCM();
            Log.d("RegisterActivity", "GCM RegId: " + regId);

            /* final SharedPreferences prefs = getSharedPreferences(
                    MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            Boolean registeredId = prefs.getBoolean(REGISTERED,false);*/

            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Void... params) {
                    Boolean result = appUtil.shareRegIdWithAppServer(context, regId);
                    return result;
                }

                @Override
                protected void onPostExecute(Boolean result) {

                    if (result) {
                        shareRegidTask = null;
                        Toast.makeText(getApplicationContext(), "" + result, Toast.LENGTH_LONG).show();
                        /*SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(REGISTERED, registered);
                        editor.commit();*/
                    }
                }
            }.execute(null, null, null);

            Log.d("RegisterActivity", "onClick of Share: After finish.");

        } else {
            //Toast.makeText(getApplicationContext(),"Already Registered with GCM Server!",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_notifications, menu);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //un-register BroadcastReceiver
        unregisterReceiver(myBroadcastReceiver);
    }

    public String registerGCM() {

        gcm = GoogleCloudMessaging.getInstance(this);
        regId = getRegistrationId(context);

        if (regId.isEmpty()) {

            registerInBackground();

            Log.d("RegisterActivity",
                    "registerGCM - successfully registered with GCM server - regId: "
                            + regId);
        } else {
            //Toast.makeText(getApplicationContext(),"RegId already available. RegId: " + regId,Toast.LENGTH_LONG).show();
        }
        return regId;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("RegisterActivity",
                    "I never expected this! Going down, going down!" + e);
            throw new RuntimeException(e);
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = " ";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(Config.GOOGLE_PROJECT_ID);
                    Log.d("RegisterActivity", "registerInBackground - regId: "
                            + regId);
                    msg = "Device registered, registration ID=" + regId;

                    storeRegistrationId(context, regId);
                } catch (IOException ex) {
                    msg = " Error :" + ex.getMessage();
                    Log.d("RegisterActivity", "Error: " + msg);
                }
                Log.d("RegisterActivity", "AsyncTask completed: " + msg);
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(getApplicationContext(), "Registered with GCM Server." + msg, Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.putInt(APP_VERSION, appVersion);
        editor.putBoolean(REGISTERED,false);
        editor.commit();
    }

    public void AddDatos(String Address,String latitude,String longitude, String duration, String distance ) {

        Log.d("List","Adding data.." + Address + " "+  latitude + " "+  longitude + " "+  duration + " "+  distance );
        datos.add(new Lista_entrada(R.drawable.user, "Cliente " + j, Address, duration,latitude,longitude));
        lista_adaptador.notifyDataSetChanged();
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }
            Toast.makeText(getApplicationContext(),"Pdownload url",Toast.LENGTH_LONG).show();
            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            Toast.makeText(getApplicationContext(),"Realizado DownloadURl", Toast.LENGTH_LONG).show();
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private void UpdateMyPosition()
    {
        //Obtenemos una referencia al LocationManager
        locationManager =
                (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //Obtenemos la ultima posicion conocida

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(),"Por favor encienda su GPS",Toast.LENGTH_LONG).show();
        }

            locMyposition =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //Mostramos la ultima posicion conocida

            //Nos registramos para recibir actualizaciones de la posicion

            locationListener = new LocationListener()
            {
                public void onLocationChanged(Location location) {
                    locMyposition = location;
                    //Toast.makeText(getApplicationContext()," " + location.getLongitude() + " " + location.getLatitude(), Toast.LENGTH_LONG).show();
                }
                public void onProviderDisabled(String provider){

                }
                public void onProviderEnabled(String provider){

                }
                public void onStatusChanged(String provider, int status, Bundle extras){

                }
            };

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1, 0, locationListener);

    }

    private String getDirectionsUrl(Location origin,Location dest){

        // Origin of route
        String str_origin = "origin="+origin.getLatitude()+","+origin.getLongitude();

        // Destination of route
        String str_dest = "destination="+dest.getLatitude()+","+dest.getLongitude();

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        Toast.makeText(getApplicationContext(),"Pdirection url",Toast.LENGTH_LONG).show();
        return url;
    }

    // Fetches data from url passed
    public class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
            Toast.makeText(getApplicationContext(),"ParserTask ejecutado",Toast.LENGTH_LONG).show();
        }
    }

    /** A class to parse the Google Places in JSON format */
    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionJsonParser parser = new DirectionJsonParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            distance = "";
            duration = "";

            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        Toast.makeText(getApplicationContext(),""+distance,Toast.LENGTH_LONG).show();
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        Toast.makeText(getApplicationContext(),""+duration,Toast.LENGTH_LONG).show();
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                }
            }
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //Accciones que se ejecutaran cuando se recibe nueva data desde el IntentService
            //Obtiene latitud y altitud como una sola cadena y luego lo separa
            String result = intent.getStringExtra(GCMNotificationIntentService.EXTRA_KEY_OUT);
            String [] campos = result.split("\\s+");

            //if(j==0){
            //Transformar latitude and longitude en objeto LatLng y se genera marcador
            try {
                //Toast.makeText(getApplicationContext(),"Recibido",Toast.LENGTH_LONG).show();
                latitude = Double.parseDouble(campos[0]);
                longitude = Double.parseDouble(campos[1]);

                locDestinlocation = new Location("");
                locDestinlocation.setLatitude(latitude);
                locDestinlocation.setLongitude(longitude);


                Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
                String address = addresses.get(0).getFeatureName();

                String url = getDirectionsUrl(locMyposition,locDestinlocation);

                try {
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                }

                //Start downloading json data from Google Directions API

                //AddMarker(new LatLng(latitude,longitude), address);

                AddDatos(address,Double.toString(latitude),Double.toString(longitude),duration,distance);
                j++;
            }
            catch (Exception e) {
                e.printStackTrace(); // getFromLocation() may sometimes fail
            }
            //}

/*            else{

                try {
                    RemoveMarker();
                    latitude = Double.parseDouble(campos[0]);
                    longitude = Double.parseDouble(campos[1]);
                    Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
                    String address = addresses.get(0).getFeatureName();
                    AddMarker(new LatLng(latitude,longitude),address);

                }
                catch (Exception e) {
                    e.printStackTrace(); // getFromLocation() may sometimes fail
                }

            }*/
        }
    }
}
