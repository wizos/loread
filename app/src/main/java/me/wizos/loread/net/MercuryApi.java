package me.wizos.loread.net;

import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;

/**
 * @author Wizos on 2017/12/17.
 */

public class MercuryApi {
    private static final String KEY = "183CSA5SmIlO3Utl77XrsCEMe6W5Ap2EkVAM8ccI";
    private static final String HOST = "https://mercury.postlight.com/parser?url=";

    public static void fetchReadabilityContent(String url, StringCallback cb) {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/json");
        headers.put("x-api-key", KEY);
        WithHttp.i().asyncGet(HOST + url, null, headers, cb);
    }
}
