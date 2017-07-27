package bitraf.bitraf.api;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import bitraf.bitraf.http.HttpClientFactory;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by havchr on 05/05/16.
 */
public class DoorRequest {

    public static final int DOOR_FRONTNLAB = 0;
    public static final int DOOR_THIRD_FLOOR = 1;
    public static final int DOOR_FOURTH_FLOOR = 2;

    public static class DoorResponseData {
        private String html;
        private boolean success;

        public String getHtml() {
            return html;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    public static DoorResponseData unlock(final String username, final String password,final int door) throws IOException{
        return mapRawHtmlStringToData(unlockRequest(username, password,door));
    }

    private static DoorResponseData mapRawHtmlStringToData(String s) {
        Log.d("BOB",s);
        DoorResponseData responseData = new DoorResponseData();
        responseData.html = s;
        responseData.success = s.contains("<div class='alert alert-success'>");
        return responseData;
    }

    private static String unlockRequest(final String username, final String password,final int door) throws IOException {
        String url = "https://p2k12.bitraf.no/door/";
        RequestBody body = RequestBody.create(MediaType(), createUnlockActionString(username, password,door));
        Request req = new Request.Builder().post(body).url(url).build();
        Response response = HttpClientFactory.execute(req);
        return response.body().string();
    }

    private static String getDoorStringFromDoorInt(final int door){
       switch(door){
           case DOOR_FRONTNLAB:
               return "frontdoor-2floor";
           case DOOR_THIRD_FLOOR:
               return "3office-3workshop";
           case DOOR_FOURTH_FLOOR:
               return "4floor";
       }
       return "frontdoor-2floor";
    }


    private static MediaType MediaType() {
        return MediaType.parse("application/x-www-form-urlencoded");
    }

    private static String createUnlockActionString(String username, String password,final int door) {
        return "action=unlock&user=" + username + "&pin=" + password + "&door="+getDoorStringFromDoorInt(door);
    }
}
