package bitraf.bitraf.geofence;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by havchr on 07/05/16.
 */
public class RegisterBitrafGeoFenceIntentService extends IntentService {

    public static final String BOBTAG = "BOB";
    public static final String BITRAF_GEOFENCE_ID = "bitraf_geofence";
    public static String REGISTER_GEOFENCE_ACTION = "bitraf.geofence.register";
    public static String REMOVE_GEOFENCE_ACTION = "bitraf.geofence.remove";
    GoogleApiClient mGoogleApiClient;

    public RegisterBitrafGeoFenceIntentService() {
        super("RegisterBitrafGeoFenceIntentService");
    }

    public RegisterBitrafGeoFenceIntentService(String man) {
        super(man);
    } //random joys of life

    @Override
    protected void onHandleIntent(final Intent intent) {


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d("BOB", "on connected ");
                        try {
                            if(REGISTER_GEOFENCE_ACTION.equals(intent.getAction())){
                                addBitrafGeoFence();
                            }
                            else if(REMOVE_GEOFENCE_ACTION.equals(intent.getAction())){
                                removeBitrafGeoFence();
                            }
                        } catch (SecurityException securityException) {
                            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                            logSecurityException(securityException);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d("BOB", "connection suspended : " + i);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d("BOB", "connection failed google api client : " + connectionResult.toString());
                    }
                })
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    private void removeBitrafGeoFence() {
        List<String> geoFences = new ArrayList<>();
        geoFences.add(BITRAF_GEOFENCE_ID);
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,geoFences);
    }

    private void addBitrafGeoFence() throws SecurityException{
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Toast.makeText(getApplicationContext(), "geofence added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.d("BOB", "on failure adding fence: " + status.toString());
            }

        });
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        double lat = 59.914112;
        double lon = 10.749105;
        Geofence geofence = new Geofence.Builder()
                .setCircularRegion(lat, lon, 500.0f)
                .setRequestId(BITRAF_GEOFENCE_ID)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(5 * 60 * 1000) // 5 minutes
                .build();
        // Add the geofences to be monitored by geofencing service.
        builder.addGeofence(geofence);

        return builder.build();
    }


    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(BOBTAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }
}
