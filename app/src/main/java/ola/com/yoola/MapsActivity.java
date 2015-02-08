package ola.com.yoola;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ola.com.yoola.models.Place;
import ola.com.yoola.utils.NetUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ola.com.yoola.utils.NetUtils.showToast;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button exploreButton;
    private AsyncHttpClient client;

    private Handler handler = new Handler();

    List<Place> places;

    private String category;
    private String distance;
    private LatLng location;

    // Constants
    public static final float ZOOM_LEVEL = 17.0f;
    private static final long GPS_UPDATE_FREQUENCY = 5000;
    private static final float GPS_UPDATE_DISTANCE = 1;
    boolean hasData = false;

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!NetUtils.isGooglePlayServicesAvailable(this)){
            showToast(MapsActivity.this,R.string.services_na);
            finish();
        }

        setContentView(R.layout.activity_maps);
        exploreButton = (Button)findViewById(R.id.exploreButton);
        setUpMapIfNeeded();

        Location loc = getCurrentLocation();
        location = new LatLng(loc.getLatitude(),loc.getLongitude());


        hasData = getIntent().hasExtra("category")&&getIntent().hasExtra("distance");
        if(hasData) {
            category = getIntent().getStringExtra("category");
            distance = getIntent().getStringExtra("distance");
            searchPlaces(getRequestURL());
        }

        exploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this,FormActivity.class));
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // onMapReady Callback

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if(hasData)
          showToast(MapsActivity.this,R.string.message_tap_marker);
        else
            showToast(MapsActivity.this,R.string.welcome_message);

        mMap = googleMap;

        if(location!=null) {
            setUpMap();
        }

    }

    // Helpers

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            mapFragment.getMapAsync(MapsActivity.this);
        }
    }

    private void setUpMap() {

        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title("You are here")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                startNewActivity(getApplicationContext(),"com.olacabs.customer");

                return false;
            }
        });

    }

    private void searchPlaces(String url) {

        if(NetUtils.isOnline(MapsActivity.this)) {

            showToast(MapsActivity.this,"Searching for the places...");

            client = new AsyncHttpClient();

            client.get(MapsActivity.this,url,new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);

                    try {
                        showToast(MapsActivity.this,response.getJSONArray("results").length()+" places found");
                        parseJson(response.getJSONArray("results"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToast(MapsActivity.this,R.string.unexpected);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    showToast(MapsActivity.this,R.string.unexpected);
                }
            });



        } else {
            showToast(MapsActivity.this,R.string.message_internet);
        }
    }

    public void parseJson(final JSONArray results) {


        Runnable parseJsonRunnable = new Runnable() {

            @Override
            public void run() {

                places = Place.fromJsonArray(results);

                if (places != null) {
                    addMarkers();
                } else {
                    showToast(MapsActivity.this,R.string.unexpected);
                }

            }
        };

        handler.post(parseJsonRunnable);

    }

    public void addMarkers() {

        Runnable addMarkersRunnable = new Runnable() {

            private Set<PoiTarget> poiTargets = null;


            @Override
            public void run() {
                if (mMap == null || places==null || places.size() == 0) {
                    return;
                }

                poiTargets = new HashSet<PoiTarget>();
                mMap.clear();
                PoiTarget pt;
                for (int i = 0; i < places.size(); i++) {
                    Marker marker =  mMap.addMarker(new MarkerOptions()
                            .position(places.get(i).getLocation())
                            .title(places.get(i).getName()));

                    pt = new PoiTarget(marker);
                    poiTargets.add(pt);
                    Picasso.with(getApplicationContext())
                            .load(R.drawable.ic_marker)
                            // places.get(i).getIconUrl()
                            .centerCrop()
                            .resize(50,50)
                            .into(pt);

                }

                // Setting zoom level
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,ZOOM_LEVEL));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));;

                // Setting on marker click
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                       // playAudio(R.raw.audio_2);
                        return false;
                    }
                });
            }


            class PoiTarget implements Target {

                private Marker m;

                public PoiTarget(Marker m) { this.m = m; }

                @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    m.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                    poiTargets.remove(this);
                }

                @Override public void onBitmapFailed(Drawable errorDrawable) {
                    poiTargets.remove(this);
                }

                @Override public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            }

        };


        handler.post(addMarkersRunnable);

    }

    /*

    public void playAudio(final int resid) {

        Runnable playAudioSongRunnable = new Runnable() {


            public static final int NOTIFICATION_ID = 1;

            @Override
            public void run() {

                MediaPlayer mMediaPlayer = MediaPlayer.create(getApplicationContext(), resid);
                mMediaPlayer.start();
                generateNotification(getApplicationContext(), R.string.notification_title, R.string.notification_content);
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mMediaPlayer) {
                        removeNotification(getApplicationContext());
                        mMediaPlayer.release();
                    }

                });

                showToast(MapsActivity.this,"Playing sample audio file");

            }

            private void generateNotification(Context context,int titleId, int messageId) {

                String title = getResources().getString(titleId);
                String message = getResources().getString(messageId);

                NotificationCompat.Builder builder =  new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.InboxStyle());

                // Add as notification
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(NOTIFICATION_ID, builder.build());
            }

            private  void removeNotification(Context context){
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancelAll();

            }
        };

        handler.post(playAudioSongRunnable);


    }
    */



    public String getRequestURL() {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + this.location.latitude + "," + this.location.longitude);
        googlePlacesUrl.append("&radius=" + this.distance);
        googlePlacesUrl.append("&types=" + this.category);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=AIzaSyCgzVjQb0s6R9zgWtL1sXiN6tkEsjPOVFc");

        return googlePlacesUrl.toString();
    }

    public Location getCurrentLocation()
    {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE );
        Location location = null;
        boolean isGPSEnabled = locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
        boolean isNetworkEnabled = locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
        if ( !( isGPSEnabled || isNetworkEnabled ) )
            showToast(MapsActivity.this,"GPS and Network not available");
        else
        {
            if ( location == null )
            {
                if ( isGPSEnabled )
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_UPDATE_FREQUENCY, GPS_UPDATE_DISTANCE, locationListener);
                    location = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
                }

                if ( isNetworkEnabled )
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_UPDATE_FREQUENCY, GPS_UPDATE_DISTANCE, locationListener);
                    location = locationManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
                }
            }
        }
        return location;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // starting acitivity of other application if installed else market
    public void startNewActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
        /* We found the activity now start the activity */
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
        /* Bring user to the market or let them choose an app? */
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            context.startActivity(intent);
        }
    }

}
