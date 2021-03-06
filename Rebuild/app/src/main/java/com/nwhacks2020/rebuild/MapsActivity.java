package com.nwhacks2020.rebuild;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;

    private static final String TAG = MapsActivity.class.getName();
    LocationManager mLocationManager;

    @SuppressWarnings("FieldCanBeLocal")
    private GoogleMap mMap;

    private LatLng startLocation = new LatLng(49.262599, -123.244944);
    private LatLng personLocation;

    @SuppressWarnings("FieldCanBeLocal")
    private float startZoom = 17;

    private Activity context = this;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            Log.i(TAG, location.getLatitude() + ", " + location.getLongitude());

                            LatLng myLocation = new LatLng(latitude, longitude);
                            CurrentLocationSingleton.setCurrentLocation(latitude, longitude);

                            mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(startZoom));
                        }
                    }
                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment == null) {
            Toast.makeText(this, "Could not instantiate map.", Toast.LENGTH_SHORT)
                    .show();
        } else {
            mapFragment.getMapAsync(this);
        }

        ImageButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPinMenu();
            }
        });

        if (mapFragment == null) {
            Toast.makeText(this, "Could not instantiate map.", Toast.LENGTH_SHORT)
                    .show();
        }
        else {
            mapFragment.getMapAsync(this);
        }
        requestPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        // Start NearbyConnections
        // Requires Fine Location
        String connectionServiceId = getString(R.string.package_name);
        NearbyConnections.startAdvertising(this, connectionServiceId, new ReceivePayloadListener());
        NearbyConnections.startDiscovering(this, connectionServiceId, new ReceivePayloadListener());
        startService(new Intent(this, MeshNetworkService.class));
    }

    private void addSampleMarkers() {

        // Centre for Drug Research
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
            49.262201, -123.243708, MarkerTitles.DANGER
        ));

        // UBC Chem and Biological Engineering
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
                49.262555, -123.247261, MarkerTitles.DANGER
        ));

        // Djavad Mowafaghian
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
                49.264580, -123.244279, MarkerTitles.DANGER
        ));

        // Centre for Blood Research
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
                49.262569, -123.245156, MarkerTitles.SHELTER
        ));

        // UBC Skate Park
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
                49.260938, -123.244514, MarkerTitles.SHELTER
        ));

        // Starbucks
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
                49.261307, -123.246550, MarkerTitles.FOOD
        ));

        // Purdy Pavilion
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
                49.263368, -123.245978, MarkerTitles.FOOD
        ));

        // BC Ambulance Station (South)
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
                49.263034, -123.243475, MarkerTitles.WATER
        ));

        // Walkway
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
                49.262718, -123.244174, MarkerTitles.NEED_HELP
        ));

        // Campus Energy Centre
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
                49.261745, -123.245134, MarkerTitles.POWER
        ));

        // The UBC Department of Psychiatry
        RebuildMarkerListSingleton.getInstance().addMarkerIfNew(new RebuildMarker(
                49.263819, -123.244550, MarkerTitles.POWER
        ));

    }

    private void repeatUpdateMarkersAndDevicesCount() {
        final Context context = this;
        Handler handler = new Handler();
        handler.postDelayed(() -> {

            updateAllMarkers();
            int devicesConnected = NearbyConnections.getNumberOfConnectedDevices();
            if (devicesConnected == 0) {
                setDevicesCount(getString(R.string.maps_devices_status_none));
            }
            else if (devicesConnected == 1) {
                setDevicesCount(getString(R.string.maps_devices_status_one));
            }
            else {
                setDevicesCount(devicesConnected + getString(R.string.maps_devices_status_more));
            }

            Log.d(TAG, "Updated markers and devices count.");
            repeatUpdateMarkersAndDevicesCount();

        }, 4000);
    }

    private void setDevicesCount(String s) {
        TextView view = findViewById(R.id.maps_devices_status);
        view.setText(s);
    }

    private void updateAllMarkers() {
        List<RebuildMarker> markers = RebuildMarkerListSingleton.getInstance().getList();

        mMap.clear();

        Location current = getLastKnownLocation();
        mMap.addMarker(new MarkerOptions().position(new LatLng(
                current.getLatitude(),
                current.getLongitude()
        ))).setZIndex(Float.MAX_VALUE);

        for (RebuildMarker m : markers) {

            int iconDrawable = 0;
            switch(m.getMarkerType()) {
                case NONE:
                    break;
                case DANGER:
                    iconDrawable = R.drawable.danger;
                    break;
                case SHELTER:
                    iconDrawable = R.drawable.shelter;
                    break;
                case FOOD:
                    iconDrawable = R.drawable.food;
                    break;
                case WATER:
                    iconDrawable = R.drawable.water;
                    break;
                case NEED_HELP:
                    iconDrawable = R.drawable.needhelp;
                    break;
                case POWER:
                    iconDrawable = R.drawable.power;
                    break;
            }

            if (iconDrawable == 0) {
                LatLng location = new LatLng(m.getLatitude(), m.getLongitude());
                mMap.addMarker(new MarkerOptions().position(location));
            }
            else {
                int dim = 100;
                Bitmap b = BitmapFactory.decodeResource(getResources(), iconDrawable);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, dim, dim, false);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(smallMarker);

                LatLng location = new LatLng(m.getLatitude(), m.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(m.getMarkerType().toString())
                        .icon(icon)
                );
            }
        }

    }

    public void openPinMenu() {
        Intent intent = new Intent(this, PinMenu.class);
        startActivity(intent);
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
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
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.google_maps_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        requestPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION);

        double longitude;
        double latitude;

        Location location = getLastKnownLocation();

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        LatLng myLocation = new LatLng(latitude, longitude);

        // Add a marker and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(startZoom));

        if (DemoModeSingleton.demoMarkersActivated()) {
            addSampleMarkers();
        }

        updateAllMarkers();
        repeatUpdateMarkersAndDevicesCount();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                String strEditText = data.getStringExtra("editTextValue");
                switch (strEditText){
                    case "Danger":
                        if(personLocation == null){
                            Log.i(TAG,"FAIL");
                        }else {
                            Bitmap danger = BitmapFactory.decodeResource(getResources(),R.drawable.danger);
                            danger = Bitmap.createScaledBitmap(danger, 40,40,false);
                            mMap.addMarker(new MarkerOptions().position(personLocation).title("Marker").icon(BitmapDescriptorFactory.fromBitmap(danger)));
                        }

                }
            }
        }
    }

    // For permission, use Manifest.permission
    private static void requestPermissions(
            Activity thisActivity,
            @SuppressWarnings("SameParameterValue") String permission) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(thisActivity, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            //noinspection StatementWithEmptyBody
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(thisActivity, new String[]{permission},0);
            }

        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    Log.d(TAG,Double.toString(location.getLatitude()));
                                    personLocation = new LatLng(location.getLatitude(),location.getLongitude());
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        //mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            personLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(personLocation).title("Marker"));

            CurrentLocationSingleton.setCurrentLocation(personLocation.latitude, personLocation.longitude);

        }
    };
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }

    private class ReceivePayloadListener extends PayloadCallback {

        @Override
        public void onPayloadReceived(@NonNull String endpointId, Payload payload) {
            // This always gets the full data of the payload. Will be null if it's not a BYTES
            // payload.
            // Check the payload type with payload.getType().
            byte[] receivedBytes = payload.asBytes();
            if (receivedBytes != null) {
                Log.d(TAG, "Received data: " + new String(receivedBytes));
            }
            else {
                Log.d(TAG, "Empty data received.");
            }

            if (receivedBytes != null) {
                RebuildMarkerListSingleton.getInstance().addMarkersFromJson(
                        context,
                        new String(receivedBytes)
                );
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId,
                                            @NonNull PayloadTransferUpdate update) {
            // Action after the completed call to onPayloadReceived
        }
    }

}
