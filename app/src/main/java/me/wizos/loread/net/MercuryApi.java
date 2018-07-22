package me.wizos.loread.net;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.model.HttpHeaders;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author Wizos on 2017/12/17.
 */

public class MercuryApi {
    private static final String KEY = "183CSA5SmIlO3Utl77XrsCEMe6W5Ap2EkVAM8ccI";
    private static final String HOST = "https://mercury.postlight.com/parser?url=";

    public static void fetchReadabilityContent(String url, StringCallback cb) {
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(30000L, TimeUnit.MILLISECONDS)
                .writeTimeout(30000L, TimeUnit.MILLISECONDS)
                .connectTimeout(30000L, TimeUnit.MILLISECONDS)
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/json");
        headers.put("x-api-key", KEY);
        OkGo.<String>get(HOST + url)
                .headers(headers)
                .client(httpClient)
                .execute(cb);
    }
}
