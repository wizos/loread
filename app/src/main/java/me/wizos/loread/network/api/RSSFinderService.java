package me.wizos.loread.network.api;

import me.wizos.loread.bean.rssfinder.RSSFinderResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Wizos on 2019/11/23.
 */

public interface RSSFinderService {
    // String BASE_URL = "http://api.wizos.me/";
    String BASE_URL = "https://rssfinder.vercel.app/";
    @FormUrlEncoded
    @POST("find.php")
    Call<RSSFinderResponse> find(
            @Field("url") String url,
            @Field("user") String user
    );

}
