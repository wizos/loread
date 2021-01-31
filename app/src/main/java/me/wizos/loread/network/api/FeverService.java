package me.wizos.loread.network.api;

import androidx.annotation.NonNull;

import me.wizos.loread.bean.fever.Feeds;
import me.wizos.loread.bean.fever.FeverResponse;
import me.wizos.loread.bean.fever.Groups;
import me.wizos.loread.bean.fever.Items;
import me.wizos.loread.bean.fever.SavedItemIds;
import me.wizos.loread.bean.fever.UnreadItemIds;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
* Created by Wizos on 2019/11/23.
*/

public interface FeverService {
   // Post请求的文本参数则用注解@Field来声明，同时还必须给方法添加注解@FormUrlEncoded来告知Retrofit参数为表单参数，如果只为参数增加@Field注解，而不给方法添加@FormUrlEncoded注解运行时会抛异常。
   @FormUrlEncoded
   @POST("?api")
   Call<FeverResponse> login(
           @NonNull @Field("api_key") String apiKey
   );

   @POST("?api&groups")
   Call<Groups> getGroups(
           @NonNull @Query("api_key") String apiKey
   );

   @POST("?api&feeds")
   Call<Feeds> getFeeds(
           @NonNull @Query("api_key") String apiKey
   );

   @POST("?api&unread_item_ids")
   Call<UnreadItemIds> getUnreadItemIds(
           @NonNull @Query("api_key") String apiKey
   );

   @POST("?api&saved_item_ids")
   Call<SavedItemIds> getSavedItemIds(
           @NonNull @Query("api_key") String apiKey
   );


   /**
    * 获取所有的项
    * API version >= 2
    */
   @POST("?api&items&total_items")
   Call<Items> getAllItems(
           @NonNull @Query("api_key") String apiKey
   );

   /**
    * 每次最多 50 条
    * API version >= 2
    */
   @FormUrlEncoded
   @POST("?api&items")
   Call<Items> getItemsWithIds(
           @NonNull @Field("api_key") String apiKey,
           @NonNull @Field("with_ids") String withIds
   );

   /**
    * 从指定的id往后取
    * 每次最多 50 条
    */
   @POST("?api&items")
   Call<Items> getItemsSinceId(
           @NonNull @Query("api_key") String apiKey,
           @Query("max_id") String maxId
   );

   /**
    * 从指定的id往前取
    * API version >= 2
    */
   @POST("?api&items")
   Call<Items> getItemsMaxId(
           @NonNull @Query("api_key") String apiKey,
           @Query("max_id") String maxId
   );

   /**
    * 从指定的id往前取
    * API version >= 2
    * @param ids 支持多个id，用,分割
    * @param as 支持 read,unread,saved,unsaved
    * @return
    */
   @POST("?api&mark=item")
   Call<FeverResponse> markItemsByIds(
           @NonNull @Query("api_key") String apiKey,
           @Query("id") String ids,
           @Query("as") String as
   );
}
