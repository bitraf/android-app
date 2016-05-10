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

    public static DoorResponseData unlock(final String username, final String password) throws IOException{
        return mapRawHtmlStringToData(unlockRequest(username, password));
    }

    private static DoorResponseData mapRawHtmlStringToData(String s) {
        Log.d("BOB",s);
        DoorResponseData responseData = new DoorResponseData();
        responseData.html = s;
        responseData.success = s.contains("<div class='alert alert-success'>");
        return responseData;
    }

    private static String unlockRequest(final String username, final String password) throws IOException {
        String url = "https://p2k12.bitraf.no/door/";
        RequestBody body = RequestBody.create(MediaType(), createUnlockActionString(username, password));
        Request req = new Request.Builder().post(body).url(url).build();
        Response response = HttpClientFactory.execute(req);
        return response.body().string();
    }


    private static MediaType MediaType() {
        return MediaType.parse("application/x-www-form-urlencoded");
    }

    private static String createUnlockActionString(String username, String password) {
        return "action=unlock&user=" + username + "&pin=" + password;
    }
}
