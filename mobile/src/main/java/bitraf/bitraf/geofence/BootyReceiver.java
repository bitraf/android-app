package bitraf.bitraf.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import bitraf.bitraf.Storage;

/**
 * Created by havchr on 09/05/16.
 */
public class BootyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            if(Storage.getIfGeofenceEnabled()){
                Intent registerGeoFence = new Intent(context,RegisterBitrafGeoFenceIntentService.class);
                registerGeoFence.setAction(RegisterBitrafGeoFenceIntentService.REGISTER_GEOFENCE_ACTION);
                context.startService(registerGeoFence);
            }
        }
    }
}
