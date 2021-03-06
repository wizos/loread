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
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Wizos on 2019/11/23.
 */

public interface TinyRSSService {
    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<TTRSSLoginResult>> isLoginIn(
            @NonNull @Body LoginParam loginParam
    );

    // Post请求的文本参数则用注解@Field来声明，同时还必须给方法添加注解@FormUrlEncoded来告知Retrofit参数为表单参数，如果只为参数增加@Field注解，而不给方法添加@FormUrlEncoded注解运行时会抛异常。
    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<TTRSSLoginResult>> login(
            @NonNull @Body LoginParam loginParam
    );

    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<List<CategoryItem>>> getCategories(
            @NonNull @Body GetCategories getCategories
    );

    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<List<FeedItem>>> getFeeds(
            @NonNull @Body GetFeeds getFeeds
    );

    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<String>> getUnreadItemIds(
            @NonNull @Body GetUnreadItemIds getUnreadItemIds
    );
    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<String>> getSavedItemIds(
            @NonNull @Body GetSavedItemIds getSavedItemIds
    );
    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<List<ArticleItem>>> getHeadlines(
            @NonNull @Body GetHeadlines getHeadlines
    );

    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<List<ArticleItem>>> getArticles(
            @NonNull @Body GetArticles getArticles
    );

    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<UpdateArticleResult>> updateArticle(
            @NonNull @Body UpdateArticle updateArticle
    );

    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<SubscribeToFeedResult>> subscribeToFeed(
            @NonNull @Body SubscribeToFeed subscribeToFeed
    );

    @Headers("Accept: application/json")
    @POST("api/")
    Call<TinyResponse<Map>> unsubscribeFeed(
            @NonNull @Body UnsubscribeFeed unsubscribeFeed
    );
}
