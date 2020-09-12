package me.wizos.loread.network.api;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import me.wizos.loread.bean.ttrss.request.GetArticles;
import me.wizos.loread.bean.ttrss.request.GetCategories;
import me.wizos.loread.bean.ttrss.request.GetFeeds;
import me.wizos.loread.bean.ttrss.request.GetHeadlines;
import me.wizos.loread.bean.ttrss.request.GetSavedItemIds;
import me.wizos.loread.bean.ttrss.request.GetUnreadItemIds;
import me.wizos.loread.bean.ttrss.request.LoginParam;
import me.wizos.loread.bean.ttrss.request.SubscribeToFeed;
import me.wizos.loread.bean.ttrss.request.UnsubscribeFeed;
import me.wizos.loread.bean.ttrss.request.UpdateArticle;
import me.wizos.loread.bean.ttrss.result.ArticleItem;
import me.wizos.loread.bean.ttrss.result.CategoryItem;
import me.wizos.loread.bean.ttrss.result.FeedItem;
import me.wizos.loread.bean.ttrss.result.SubscribeToFeedResult;
import me.wizos.loread.bean.ttrss.result.TTRSSLoginResult;
import me.wizos.loread.bean.ttrss.result.TinyResponse;
import me.wizos.loread.bean.ttrss.result.UpdateArticleResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Wizos on 2019/11/23.
 */

public interface LoreadService {
    // Post请求的文本参数则用注解@Field来声明，同时还必须给方法添加注解@FormUrlEncoded来告知Retrofit参数为表单参数，如果只为参数增加@Field注解，而不给方法添加@FormUrlEncoded注解运行时会抛异常。
    @Headers("Accept: application/json")
    @POST("plugins.local/loread/")
    Call<TinyResponse<TTRSSLoginResult>> login(
            @NonNull @Body LoginParam loginParam
    );

    @Headers("Accept: application/json")
    @POST("plugins.local/loread/")
    Call<TinyResponse<List<CategoryItem>>> getCategories(
            @Header("authorization") String authorization,
            @NonNull @Body GetCategories getCategories
    );

    @Headers("Accept: application/json")
    @POST("plugins.local/loread/")
    Call<TinyResponse<List<FeedItem>>> getFeeds(
            @Header("authorization") String authorization,
            @NonNull @Body GetFeeds getFeeds
    );

    @Headers("Accept: application/json")
    @POST("plugins.local/loread/")
    Call<TinyResponse<String>> getUnreadItemIds(
            @Header("authorization") String authorization,
            @NonNull @Body GetUnreadItemIds getUnreadItemIds
    );
    @Headers("Accept: application/json")
    @POST("plugins.local/loread/")
    Call<TinyResponse<String>> getSavedItemIds(
            @Header("authorization") String authorization,
            @NonNull @Body GetSavedItemIds getSavedItemIds
    );
    @Headers("Accept: application/json")
    @POST("plugins.local/loread/")
    Call<TinyResponse<List<ArticleItem>>> getHeadlines(
            @Header("authorization") String authorization,
            @NonNull @Body GetHeadlines getHeadlines
    );

    @Headers("Accept: application/json")
    @POST("plugins.local/loread/")
    Call<TinyResponse<List<ArticleItem>>> getArticles(
            @Header("authorization") String authorization,
            @NonNull @Body GetArticles getArticles
    );

    @Headers("Accept: application/json")
    @POST("plugins.local/loread/")
    Call<TinyResponse<UpdateArticleResult>> updateArticle(
            @Header("authorization") String authorization,
            @NonNull @Body UpdateArticle updateArticle
    );


    @Headers("Accept: application/json")
    @POST("plugins.local/loread/")
    Call<TinyResponse<SubscribeToFeedResult>> subscribeToFeed(
            @Header("authorization") String authorization,
            @NonNull @Body SubscribeToFeed subscribeToFeed
    );

    @Headers("Accept: application/json")
    @POST("plugins.local/loread/")
    Call<TinyResponse<Map>> unsubscribeFeed(
            @Header("authorization") String authorization,
            @NonNull @Body UnsubscribeFeed unsubscribeFeed
    );

//    @Headers("Accept: application/json")
//    @POST("subscriptions")
//    Call<List<EditFeed>> editFeed(
//            @NonNull @Body EditFeed editFeed
//    );
}
