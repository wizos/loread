package me.wizos.loread.network.api;

import androidx.annotation.NonNull;

import me.wizos.loread.bean.Token;
import me.wizos.loread.bean.inoreader.GsTags;
import me.wizos.loread.bean.inoreader.ItemIds;
import me.wizos.loread.bean.inoreader.StreamContents;
import me.wizos.loread.bean.inoreader.Subscriptions;
import me.wizos.loread.bean.inoreader.UserInfo;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Wizos on 2019/5/12.
 */

public interface InoReaderService {
    @FormUrlEncoded
    @POST("oauth2/token")
    Call<Token> getAccessToken(
            @Field("grant_type") String grantType,
            @Field("redirect_uri") String redirectUri,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("code") String code
    );

    @FormUrlEncoded
    @POST("oauth2/token")
    Call<Token> refreshingAccessToken(
            @Field("grant_type") String grantType,
            @Field("refresh_token") String refreshToken,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret
    );


    @FormUrlEncoded
    @POST("accounts/ClientLogin")
    Call<String> login(
            @Field("Email") String email,
            @Field("Passwd") String password
    );

    @GET("/reader/api/0/user-info")
    Call<UserInfo> getUserInfo(
            @Header("authorization") String authorization
    );


    @GET("reader/api/0/tag/list")
    Call<GsTags> getCategoryItems(
            @Header("authorization") String authorization
    );

    @GET("reader/api/0/subscription/list")
    Call<Subscriptions> getFeeds(
            @Header("authorization") String authorization
    );

    @GET("reader/api/0/stream/items/ids")
    Call<ItemIds> getStreamItemsIds(
            @Header("authorization") String authorization,
            @Query("s") String s,
            @Query("xt") String xt,
            @Query("n") int count,
            @Query("includeAllDirectStreamIds") boolean includeAllDirectStreamIds,
            @Query("continuation") String continuation
    );

    @GET("reader/api/0/stream/items/ids")
    Call<ItemIds> getUnreadRefs(
            @Header("authorization") String authorization,
            @Query("s") String s,
            @Query("xt") String xt,
            @Query("n") int count,
            @Query("includeAllDirectStreamIds") boolean includeAllDirectStreamIds,
            @Query("continuation") String continuation
    );

    @GET("reader/api/0/stream/items/ids")
    Call<ItemIds> syncStarredRefs(
            @Header("authorization") String authorization,
            @Query("s") String s,
            @Query("xt") String xt,
            @Query("n") int count,
            @Query("includeAllDirectStreamIds") boolean includeAllDirectStreamIds,
            @Query("continuation") String continuation
    );

    @POST("reader/api/0/stream/items/contents")
    Call<StreamContents> getItemContents(
            @Header("authorization") String authorization,
            @NonNull @Body RequestBody requestBody
    );


    @POST("reader/api/0/subscription/quickadd")
    Call<String> addFeed(
            @NonNull @Body RequestBody requestBody
    );

    @POST("reader/api/0/subscription/edit")
    Call<String> editFeed(
            @Header("authorization") String authorization,
            @NonNull @Body RequestBody requestBody
    );


    @POST("reader/api/0/edit-tag")
    Call<String> markArticle(
            @Header("authorization") String authorization,
            @Body RequestBody requestBody
    );

    // 失败返回：Error=Tag not found!
    // 成功返回：OK
    @POST("reader/api/0/rename-tag")
    Call<String> renameTag(
            @Header("authorization") String authorization,
            @Body RequestBody requestBody
    );

}
