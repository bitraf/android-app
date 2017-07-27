package bitraf.bitraf.api;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.IOException;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by havchr on 05/05/16.
 */
public class DoorRequestIntentService extends IntentService {


    Handler mMainThreadHandler = null;

    public static String ACTION_UNLOCK_FRONTNLAB = "bitraf.door.unlock.frontnlab";
    public static String ACTION_UNLOCK_F3 = "bitraf.door.unlock.f3";
    public static String ACTION_UNLOCK_F4 = "bitraf.door.unlock.f4";
    public static String USERNAME_EXTRA = "USERNAME_EXTRA";
    public static String PASWD_EXTRA = "PASWD_EXTRA";
    public static String HTML_EXTRA = "HTML_EXTRA";
    public static String RESPONSE_UNLOCK_SUCCESS = "bitraf.door.unlock.success";
    public static String RESPONSE_UNLOCK_FAIL = "bitraf.door.unlock.fail";
    public static String RESPONSE_UNLOCK_ERROR = "bitraf.door.unlock.error";

    public DoorRequestIntentService() {
        super("DoorRequestIntentService");
        mMainThreadHandler = new Handler();
    }
    public DoorRequestIntentService(String name) {
        super(name);
        mMainThreadHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(     ACTION_UNLOCK_FRONTNLAB.equals(intent.getAction()) ||
                ACTION_UNLOCK_F3.equals(intent.getAction()) ||
                ACTION_UNLOCK_F4.equals(intent.getAction())){
            try {
                int whichDoor = DoorRequest.DOOR_FRONTNLAB;
                if(ACTION_UNLOCK_F3.equals(intent.getAction())){
                 whichDoor = DoorRequest.DOOR_THIRD_FLOOR;
                }
                if(ACTION_UNLOCK_F4.equals(intent.getAction())){
                    whichDoor = DoorRequest.DOOR_FOURTH_FLOOR;
                }
                DoorRequest.DoorResponseData data = DoorRequest.unlock(intent.getStringExtra(USERNAME_EXTRA),
                                                                       intent.getStringExtra(PASWD_EXTRA),
                                                                       whichDoor);
                if(data.isSuccess()){
                    if(!LocalBroadcastManager.getInstance(this).sendBroadcast(createSuccessIntent(data.getHtml()))){
                        toastIt("access granted");
                    }
                }else{
                    if(!LocalBroadcastManager.getInstance(this).sendBroadcast(createFailIntent(data.getHtml()))){
                        toastIt("Unlocking door failed");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if(!LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(RESPONSE_UNLOCK_ERROR))){
                    toastIt("Unlocking door error");
                }
            }
        }
    }

    private void toastIt(final String message) {

        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });

    }

    Intent createSuccessIntent(String html){
        return new Intent(RESPONSE_UNLOCK_SUCCESS).putExtra(HTML_EXTRA,html);
    }

    Intent createFailIntent(String html){
        return new Intent(RESPONSE_UNLOCK_FAIL).putExtra(HTML_EXTRA,html);
    }


}
