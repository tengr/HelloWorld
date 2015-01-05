package app.helloworld.ruichen.nicta.helloworld;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ActionBarActivity
        implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener,
        OnMyLocationButtonClickListener{

    private final LatLng COORD_NICTA_VIC = new LatLng(-37.811458,144.949231);
    private final LatLng COORD_UNIMELB = new LatLng(-37.796369,144.961174);
    private final LatLng COORD_MELBCENTRAL = new LatLng(-37.810144, 144.962674);
    private final LatLng COORD_REB = new LatLng(-37.804686,144.971649);

    private Location myLocation;
    private List<Address> addresses = null;
    private LatLng myLatLng;
    private List<String> providersList = new ArrayList<String>();
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private TextView mMessageView;

    //Address Progress
    private TextView mAddress;
    private ProgressBar mActivityIndicator;
    private String addressText = "My Address is ";

    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);
        mMessageView = (TextView) findViewById(R.id.message_text);
        mAddress = (TextView) findViewById(R.id.address);
        mActivityIndicator =
                (ProgressBar) findViewById(R.id.address_progress);
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        // Add this line
        mMap.setMyLocationEnabled(true);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    protected void onStart() {
        super.onStart();  // Always call the superclass method first

        // The activity is either being restarted or started for the first time
        // so this is where we should make sure that GPS is enabled
        // LocationManager locationManager =
        //        (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        //if (!gpsEnabled) {
        // Create a dialog here that requests the user to enable GPS, and use an intent
        // with the android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS action
        // to take the user to the Settings screen to enable GPS when they click "OK"
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setUpGoogleApiClientIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
            }
        }
    }

    private void setUpGoogleApiClientIfNeeded() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    /**
     * Button to get current Location. This demonstrates how to get the current Location as required
     * without needing to register a LocationListener.
     */
    public void showMyLocation(View view) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            myLatLng = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
            String msg = "Location = "
                    + myLocation;
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(myLatLng,21);
            mMap.animateCamera(update);
            mMap.addMarker(new MarkerOptions()
                    .position(myLatLng)
                    .title("My Location").snippet("Some Address"));
             Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            // Get the current location from the input parameter list
            // Create a list to contain the result address
            try {
                addresses = geocoder.getFromLocation(myLocation.getLatitude(),
                        myLocation.getLongitude(), 1);
            } catch (IOException e1) {
                addressText =  "LocationSampleActivity" +
                        "IO Exception in getFromLocation()";
                e1.printStackTrace();
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " +
                        Double.toString(myLocation.getLatitude()) +
                        " , " +
                        Double.toString(myLocation.getLongitude()) +
                        " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                addressText = "Illegal Argument Exception: Couldn't find address";

            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0 ) {
                Address address = addresses.get(0);
                addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
            }
            else {
                addressText += "address = null";
            }

            // Return the text
            mAddress.setText("My address is " + addressText);
        }
    }

    /**
     * Implementation of {@link LocationListener}.
     */
    @Override
    public void onLocationChanged(Location location) {
        mMessageView.setText("Location = " + location);
    }

    /**
     * Callback called when connected to GCore. Implementation of {@link ConnectionCallbacks}.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                this);  // LocationListener
    }

    /**
     * Callback called when disconnected from GCore. Implementation of {@link ConnectionCallbacks}.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // Do nothing
    }

    /**
     * Implementation of {@link OnConnectionFailedListener}.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do nothing
    }


    /*below are two onClick methods for buttons*/
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    public void onClick_routes(View view){

        LatLng centroid = new LatLng( (
                COORD_NICTA_VIC.latitude +
                COORD_UNIMELB.latitude +
                COORD_MELBCENTRAL.latitude +
                COORD_REB.latitude)/4.0
                ,
                (COORD_NICTA_VIC.longitude +
                        COORD_UNIMELB.longitude +
                        COORD_MELBCENTRAL.longitude +
                        COORD_REB.longitude )/4.0
        );

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centroid, 14));

        // Polylines are useful for marking paths and routes on the map.
        mMap.addPolyline(new PolylineOptions().geodesic(true)
                .add(COORD_NICTA_VIC)
                .add(COORD_UNIMELB)
                .add(COORD_MELBCENTRAL)
                .add(COORD_REB)  // Royal Exhibition Building
        );
    }

    public void onClick_reset(View view){
        mMessageView.setText("");
        mAddress.setText("");
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0.0,0.0), 2));
    }

    /*Menu Stuff*/
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
        if (id == R.id.action_notifications) {
            Intent intent = new Intent(this, RouteNotification.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
