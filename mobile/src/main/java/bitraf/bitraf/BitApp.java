package bitraf.bitraf;

import android.app.Application;
import android.content.Context;

/**
 * Created by havchr on 05/05/16.
 */
public class BitApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }

    public static Context appContext;
}
