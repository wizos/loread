package me.wizos.loread.gson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * author  Wizos
 * created 2019/8/3
 */
public class GsonUtil {
    private static Gson gson = new Gson();

    public static <T> T fromJson(InputStream inputStream, TypeToken<T> token) {
        try {
            return gson.fromJson(new InputStreamReader(inputStream, "UTF-8"), token.getType());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T fromJson(String jsonText, TypeToken<T> token) {
        try {
            return gson.fromJson(jsonText, token.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
