package me.wizos.loread.net;

import com.google.gson.Gson;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.model.HttpHeaders;

import java.io.IOException;

import me.wizos.loread.bean.Readability;

/**
 * Created by Wizos on 2017/12/17.
 */

public class MercuryApi {
    private static final String KEY = "183CSA5SmIlO3Utl77XrsCEMe6W5Ap2EkVAM8ccI";
    private static String HOST = "https://mercury.postlight.com/parser?url=";

    public static String fetchReadabilityContent(String url, NetCallbackS cb) throws HttpException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/json");
        headers.put("x-api-key", KEY);
        String info = WithHttp.i().syncGet(HOST + url, null, headers, cb);
        Readability readability = new Gson().fromJson(info, Readability.class);
        return readability.getContent();
    }
}
