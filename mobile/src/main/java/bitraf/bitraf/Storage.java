package bitraf.bitraf;

import android.content.SharedPreferences;

/**
 * Created by havchr on 08/05/16.
 */
public class Storage {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GEOFENCE_ENABLED = "GEOFENCE_ENABLED";

    public static void storePassword(String passw) {
        prefFile().edit().putString(PASSWORD,passw).commit();
    }

    public static void storeUsername(String username) {
        prefFile().edit().putString(USERNAME,username).commit();
    }

    public static String retreiveUsername() {
        return prefFile().getString(USERNAME,"");
    }

    public static String retreivePassword() {
        return prefFile().getString(PASSWORD,"");
    }

    public static void clearCredentials(){
        Storage.storeUsername("");
        Storage.storePassword("");
    }

    public static boolean getIfGeofenceEnabled(){
        return prefFile().getBoolean(GEOFENCE_ENABLED,false);
    }

    public static void setIfGeofenceEnabled(boolean isItEnabled){
        prefFile().edit().putBoolean(GEOFENCE_ENABLED,isItEnabled).commit();
    }

    private static SharedPreferences prefFile() {
        return BitApp.appContext.getSharedPreferences("credentials",0);
    }
}
