package bitraf.bitraf.http;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import bitraf.bitraf.BitApp;
import bitraf.bitraf.http.cookie.PersistentCookieJar;
import bitraf.bitraf.http.cookie.SetCookieCache;
import bitraf.bitraf.http.cookie.SharedPrefsCookiePersistor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author havchr
 */
public abstract class HttpClientFactory {

    private static final int HTTPS_PORT = 443;
    private static final int HTTP_PORT = 80;
    private static final int THIRTY_SECONDS_IN_MILLISECONDS = 1000 * 30;
    private static final int CONNECTION_TIMEOUT = THIRTY_SECONDS_IN_MILLISECONDS;
    private static final int SO_TIMEOUT = THIRTY_SECONDS_IN_MILLISECONDS;
    private static OkHttpClient clientInstance;


    public static Response execute(final Request req) throws IOException {
        if (clientInstance == null) {
            clientInstance = createNewClient();
        }
        Request.Builder reqBuilder = setPort(req, req.newBuilder());
        return clientInstance.newCall(reqBuilder.build()).execute();
    }

    private static OkHttpClient createNewClient() {

        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .cookieJar(new PersistentCookieJar(new SetCookieCache(),new SharedPrefsCookiePersistor(BitApp.appContext)))
                .build();
    }

    private static Request.Builder setPort(final Request req, Request.Builder reqBuilder) {
        if (req.url().isHttps()) {
            reqBuilder = reqBuilder.url(req.url().newBuilder().port(HTTPS_PORT).build());
        } else {
            reqBuilder = reqBuilder.url(req.url().newBuilder().port(HTTP_PORT).build());
        }
        return reqBuilder;
    }

    public static OkHttpClient httpClientInstance() {
        return httpClientInstance(false);
    }

    public static synchronized OkHttpClient httpClientInstance(boolean forceNewInstance) {
        if (clientInstance== null || forceNewInstance)
            clientInstance = createNewClient();
        return clientInstance;
    }

}
