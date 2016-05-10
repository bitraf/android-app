package bitraf.bitraf;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by havchr on 05/05/16.
 */
public class SendUnlockMessageToPhoneService extends IntentService {

    public SendUnlockMessageToPhoneService() {
        super("SendUnlockMessageToPhoneService");
    }

    public SendUnlockMessageToPhoneService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("BOB","intent action : " + intent.getAction());
    }
}
