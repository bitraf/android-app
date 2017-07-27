package bitraf.bitraf.geofence; /**
 * Copyright 2014 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import bitraf.bitraf.R;
import bitraf.bitraf.Storage;
import bitraf.bitraf.api.DoorRequest;
import bitraf.bitraf.api.DoorRequestIntentService;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";
    public static final int NOTIFICATION_ID = 808;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = "jaja: error : " + geofencingEvent.getErrorCode();
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for (Geofence g : triggeringGeofences) {
                if (g.getRequestId().equals(RegisterBitrafGeoFenceIntentService.BITRAF_GEOFENCE_ID)) {
                    if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                        sendNotification();
                    }else{
                        removeNotification();
                    }
                    break;
                }
            }
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private void removeNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification() {
        // Create an explicit content Intent that starts the main Activity.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        NotificationCompat.Action actionFrontDoor = createUnlockAction(DoorRequestIntentService.ACTION_UNLOCK_FRONTNLAB);
        NotificationCompat.Action action3Floor = createUnlockAction(DoorRequestIntentService.ACTION_UNLOCK_F3);
        NotificationCompat.Action action4Floor = createUnlockAction(DoorRequestIntentService.ACTION_UNLOCK_F4);

        builder.setSmallIcon(R.drawable.ic_lock_white_48dp)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setColor(getResources().getColor(R.color.colorGraySmallNotificaitonBgCol))
                .setContentTitle("Bitraf's nearby")
                .setContentText("interact to unlock door")
                .addAction(actionFrontDoor)
                .addAction(action3Floor)
                .addAction(action4Floor);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(false);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @NonNull
    private NotificationCompat.Action createUnlockAction(String unlockAction) {
        Intent unlock = new Intent(getApplicationContext(), DoorRequestIntentService.class);
        unlock.setAction(unlockAction);
        unlock.putExtra(DoorRequestIntentService.USERNAME_EXTRA, Storage.retreiveUsername());
        unlock.putExtra(DoorRequestIntentService.PASWD_EXTRA, Storage.retreivePassword());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, unlock, PendingIntent.FLAG_UPDATE_CURRENT);
        // Define the notification settings.

        String doorLabel = getString(R.string.open_door,getString(getDoorLabelStringResourceFromDoorUnlockAction(unlockAction)));

        return new NotificationCompat.Action.Builder(R.drawable.ic_lock_white_48dp,
                doorLabel,pendingIntent)
                .build();
    }

    private int getDoorLabelStringResourceFromDoorUnlockAction(final String unlockAction) {
        if(DoorRequestIntentService.ACTION_UNLOCK_FRONTNLAB.equals(unlockAction)){
            return R.string.frontnlab_opendoor_button;
        }
        if(DoorRequestIntentService.ACTION_UNLOCK_F3.equals(unlockAction)){
            return R.string.floor_3_opendoor_button;
        }
        if(DoorRequestIntentService.ACTION_UNLOCK_F4.equals(unlockAction)){
            return R.string.floor_4_opendoor_button;
        }
        return R.string.frontnlab_opendoor_button;
    }

}
