//package me.wizos.loread.gson.Request;
//
//import com.google.gson.annotations.SerializedName;
//
//import java.util.ArrayList;
//
//import me.wizos.loread.gson.Request.Request;
//
///**
// * Created by Wizos on 2016/4/21.
// */
//public class RequestRefs {
//    @SerializedName("id")
//    String id; // RequestRefs
//
//    @SerializedName("updatedUsec")
//    String updatedUsec;
//
//    @SerializedName("items")
//    ArrayList<Request> items;
//
//
////    POST /reader/api/0/stream/items/contents?output=json HTTP/1.1
////    AppKey: tg6UMNG9cqIJgSU7Y1zDNKrKIH5Mf0yI
////    AppId: 1000001134
////    User-Agent: FeedMe2.2.4(120)
////    Authorization: GoogleLogin auth=4u2kpM_uIlUXpqASrR6F6mn51obsJ334
////    Content-Type: application/x-www-form-urlencoded; charset=utf-8
////    Content-Length: 3049
////    Host: www.inoreader.com
////    Connection: Keep-Alive
////    Accept-Encoding: gzip
////
////    GET /reader/api/0/stream/items/ids?output=json&n=100&xt=user/-/state/com.google/read&ot=0&s=user/-/state/com.google/reading-list HTTP/1.1
////    AppKey: FuuRNr63xMIQM5_hZJYCIw6o3od3OjCC
////    AppId: 1000001291
////    User-Agent: FeedMe2.2.0(115)
////    Authorization: GoogleLogin auth=hW46Aj_2YQYWPY33qcnogA25hcIh9IJz
////    Host: www.inoreader.com
////    Connection: Keep-Alive
////    Accept-Encoding: gzip
//
////    n - Number of items to return (default 20, max 1000).
////    r - Order. By default, it is newest first. You can pass o here to get oldest first.
////    ot - Start time (unix timestamp) from which to start to get items. If r=o and the time is older than one month ago, one month ago will be used instead.
////    xt - Exclude Target - You can query all items from a feed that are not flagged as read by setting this to user/-/state/com.google/read.
////    it - Include Target - You can query for a certain label with this. Accepted values: user/-/state/com.google/starred, user/-/state/com.google/like.
//
//
//
//}
