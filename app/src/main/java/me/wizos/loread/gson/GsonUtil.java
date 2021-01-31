package me.wizos.loread.gson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * author  Wizos
 * created 2019/8/3
 */
public class GsonUtil {
    private static Gson gson = new Gson();

    public static <T> T fromJson(InputStream inputStream, TypeToken<T> token) {
        return gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), token.getType());
    }

    public static <T> T fromJson(InputStream inputStream, Class<T> classOfT) {
        return gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), classOfT);
    }

    public static <T> T fromJson(String jsonText, TypeToken<T> token) {
        try {
            return gson.fromJson(jsonText, token.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T fromJson(String jsonText, Class<T> classOfT) {
        try {
            return gson.fromJson(jsonText, classOfT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
