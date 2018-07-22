package me.wizos.loread.net;

import android.text.TextUtils;

import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.socks.library.KLog;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.data.WithPref;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;


/**
 * 本接口对接 Inoreader 服务，从他那获取数据
 * @author Wizos on 2016/3/10.
 */
public class InoApi {
    public int FETCH_CONTENT_EACH_CNT = 20;

    private static final String APP_ID = "1000001277";
    private static final String APP_KEY = "8dByWzO4AYi425yx5glICKntEY2g3uJo";
    public static String HOST = "https://www.inoreader.com";
    public static String INOREADER_ATUH = "";

    public static final String CLIENTLOGIN = "/accounts/ClientLogin";
    public static final String USER_INFO = "/reader/api/0/user-info";
    public static final String TAG_LIST = "/reader/api/0/tag/list";
    public static final String STREAM_PREFS = "/reader/api/0/preference/stream/list";
    public static final String SUSCRIPTION_LIST = "/reader/api/0/subscription/list"; // 这个不知道现在用在了什么地方
    public static final String UNREAD_COUNTS = "/reader/api/0/unread-count";
    public static final String ITEM_IDS = "/reader/api/0/stream/items/ids"; // 获取所有文章的id
    public static final String ITEM_CONTENTS = "/reader/api/0/stream/items/contents"; // 获取流的内容
    public static final String EDIT_TAG = "/reader/api/0/edit-tag";
    public static final String RENAME_TAG = "/reader/api/0/rename-tag";
    public static final String EDIT_FEED = "/reader/api/0/subscription/edit";
    public static final String ADD_FEED = "/reader/api/0/subscription/quickadd";

    public static final String STREAM_CONTENTS = "/reader/api/0/stream/contents/";
    public static final String Stream_Contents_Atom = "/reader/atom";
    public static final String Stream_Contents_User = "/reader/api/0/stream/contents/user/";

    public static final String READING_LIST = "/state/com.google/reading-list";
    public static final String NO_LABEL = "/state/com.google/no-label";
    public static final String STARRED = "/state/com.google/starred";
    public static final String UNREAND = "/state/com.google/unread";
    
    /*
Code 	Description
200 	Request OK
400 	Mandatory parameter(s) missing
401 	End-user not authorized
403 	You are not sending the correct AppID and/or AppSecret
404 	Method not implemented
429 	Daily limit reached for this zone
503 	Service unavailable
     */


    private static long mUserID;
    private HttpHeaders authHeaders;

    private static InoApi inoApi;

    private InoApi() {
        initAuthHeaders();
    }

    public static InoApi i() {
        if (inoApi == null) {
            synchronized (InoApi.class) {
                if (inoApi == null) {
                    inoApi = new InoApi();
                    mUserID = App.UserID;
                }
            }
        }
        return inoApi;
    }

    public void initAuthHeaders() {
        authHeaders = new HttpHeaders();
        authHeaders.put("AppId", APP_ID);
        authHeaders.put("AppKey", APP_KEY);
        if (!TextUtils.isEmpty(WithPref.i().getAuth())) {
            authHeaders.put("Authorization", WithPref.i().getAuth()); // TEST:  这里不对
        }
    }


    public String syncBootstrap() throws HttpException, IOException {
        return WithHttp.i().syncGet(HOST + "/reader/api/0/bootstrap", null, authHeaders);
    }

    public String clientLogin(String accountId, String accountPd) throws HttpException, IOException {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("Email", accountId);
        builder.add("Passwd", accountPd);
        KLog.e(HOST + "/accounts/ClientLogin" + accountId + "-" + accountPd);
        return WithHttp.i().syncPost(HOST + "/accounts/ClientLogin", builder, authHeaders);
    }

    public void clientLogin2(String accountId, String accountPd, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("Email", accountId);
        builder.add("Passwd", accountPd);
        KLog.e(HOST + "/accounts/ClientLogin" + accountId + "-" + accountPd);
        WithHttp.i().asyncPost(HOST + "/accounts/ClientLogin", builder, authHeaders, cb);
    }


    public String fetchUserInfo() throws HttpException, IOException {
        return WithHttp.i().syncGet(HOST + "/reader/api/0/user-info", null, authHeaders);
    }

    public String syncTagList() throws HttpException, IOException {
        return WithHttp.i().syncGet(HOST + "/reader/api/0/tag/list", null, authHeaders);
    }

    public String syncSubList() throws HttpException, IOException {
        return WithHttp.i().syncGet(HOST + "/reader/api/0/subscription/list", null, authHeaders);
    }

    public String syncStreamPrefs() throws HttpException, IOException {
        return WithHttp.i().syncGet(HOST + "/reader/api/0/preference/stream/list", null, authHeaders);
    }

    public String syncUnreadCounts() throws HttpException, IOException {
        return WithHttp.i().syncGet(HOST + "/reader/api/0/unread-count", null, authHeaders);
    }

    public String syncUnReadRefs(String continuation) throws HttpException, IOException {
        HttpParams httpParams = new HttpParams();
//        addHeader("ot","0");
        httpParams.put("n", "1000");
        httpParams.put("xt", "user/" + mUserID + "/state/com.google/read");
        httpParams.put("s", "user/" + mUserID + "/state/com.google/reading-list");
        httpParams.put("includeAllDirectStreamIds", "false");
        if (continuation != null) {
            httpParams.put("c", continuation);
        }
        return WithHttp.i().syncGet(HOST + ITEM_IDS, httpParams, authHeaders);
    }

    public String syncStarredRefs(String continuation) throws HttpException, IOException {
        HttpParams httpParams = new HttpParams();
//        addHeader("ot","0");
        httpParams.put("n", "1000");
        httpParams.put("s", "user/" + mUserID + "/state/com.google/starred");
        httpParams.put("includeAllDirectStreamIds", "false");
        httpParams.put("c", continuation);
        return WithHttp.i().syncGet(HOST + ITEM_IDS, httpParams, authHeaders);
    }

    public String syncItemContents(List<String> ids) throws HttpException, IOException {
        FormBody.Builder builder = new FormBody.Builder();
        for (String id : ids) {
            builder.add("i", id);
        }
        return syncItemContents(builder);
    }


    public String syncItemContents(FormBody.Builder builder) throws HttpException, IOException {
        return WithHttp.i().syncPost(HOST + ITEM_CONTENTS, builder, authHeaders);
    }

    public String syncStaredStreamContents(String continuation) throws HttpException, IOException {
        HttpParams httpParams = new HttpParams();
        httpParams.put("n", FETCH_CONTENT_EACH_CNT);
        httpParams.put("r", "o");
        httpParams.put("c", continuation);
        return WithHttp.i().syncGet(HOST + "/reader/api/0/stream/contents/" + "user/-/state/com.google/starred", httpParams, authHeaders);
    }


//    public String syncStarredContents( String continuation, NetCallbackS cb ) throws HttpException,IOException{
//        HttpParams httpParams = new HttpParams();
//        httpParams.put("n", "20");
//        httpParams.put("r", "o");
//        httpParams.put("c", continuation);
//        return WithHttp.i().syncGet( HOST + "/reader/api/0/stream/contents/" + "user/-/state/com.google/starred", httpParams, authHeaders, cb);
//    }




    public void articleRemoveTag(String articleID, String tagId, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("r", tagId);
        builder.add("i", articleID);
        WithHttp.i().asyncPost(HOST + EDIT_TAG, builder, authHeaders, cb);
    }

    public void articleAddTag(String articleID, String tagId, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", tagId);
        builder.add("i", articleID);
        WithHttp.i().asyncPost(HOST + EDIT_TAG, builder, authHeaders, cb);
    }

    public void renameTag(String sourceTagId, String destTagId, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("s", sourceTagId);
        builder.add("dest", destTagId);
        WithHttp.i().asyncPost(HOST + RENAME_TAG, builder, authHeaders, cb);
    }

    public void addFeed(String feedId, StringCallback cb) {
        // /reader/api/0/subscription/quickadd
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("quickadd", feedId);
        WithHttp.i().asyncPost(HOST + ADD_FEED, builder, authHeaders, cb);
    }

    public void renameFeed(String feedId, String renamedTitle, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
//        builder.add("ac", "edit"); // 可省略
        builder.add("s", feedId);
        builder.add("t", renamedTitle);
        WithHttp.i().asyncPost(HOST + EDIT_FEED, builder, authHeaders, cb);
    }

    public void renameFeed(OkHttpClient httpClient, String feedId, String renamedTitle, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
//        builder.add("ac", "edit"); // 可省略
        builder.add("s", feedId);
        builder.add("t", renamedTitle);
        WithHttp.i().asyncPost(httpClient, HOST + EDIT_FEED, builder, authHeaders, cb);
    }

    public void unsubscribeFeed(String feedId, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("ac", "unsubscribe");
        builder.add("s", feedId);
        WithHttp.i().asyncPost(HOST + EDIT_FEED, builder, authHeaders, cb);
    }

    public void unsubscribeFeed(OkHttpClient httpClient, String feedId, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("ac", "unsubscribe");
        builder.add("s", feedId);
        WithHttp.i().asyncPost(httpClient, HOST + EDIT_FEED, builder, authHeaders, cb);
    }

    public void markArticleListReaded(List<String> articleIDs, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", "user/-/state/com.google/read");
        for (String articleID : articleIDs) {
            builder.add("i", articleID);
        }
        WithHttp.i().asyncPost(HOST + EDIT_TAG, builder, authHeaders, cb);
    }

    public void markArticleReaded(String articleID, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", "user/-/state/com.google/read");
        builder.add("i", articleID);
        WithHttp.i().asyncPost(HOST + EDIT_TAG, builder, authHeaders, cb);
    }

    public void markArticleReaded(OkHttpClient httpClient, String articleID, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", "user/-/state/com.google/read");
        builder.add("i", articleID);
        WithHttp.i().asyncPost(httpClient, HOST + EDIT_TAG, builder, authHeaders, cb);
    }

    public void markArticleUnread(String articleID, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("r", "user/-/state/com.google/read");
        builder.add("i", articleID);
        WithHttp.i().asyncPost(HOST + EDIT_TAG, builder, authHeaders, cb);
    }

    public void markArticleUnstar(String articleID, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("r", "user/-/state/com.google/starred");
        builder.add("i", articleID);
        WithHttp.i().asyncPost(HOST + EDIT_TAG, builder, authHeaders, cb);
    }

    public void markArticleStared(String articleID, StringCallback cb) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("a", "user/-/state/com.google/starred");
        builder.add("i", articleID);
        WithHttp.i().asyncPost(HOST + EDIT_TAG, builder, authHeaders, cb);
    }


    public static boolean isFeed(String id) {
        return id.startsWith("feed/");
    }

    public static boolean isTag(String id) {
        return id.startsWith("user/");
    }


    public static boolean isLabel(String paramString) {
        String REGEX_LABEL = "^user\\/\\d{0,12}\\/label\\/"; // user/1006097346/label/tag_name
        Pattern p_label = Pattern.compile(REGEX_LABEL, Pattern.CASE_INSENSITIVE);
        Matcher m_label = p_label.matcher(paramString);
        return m_label.find();
    }

    public static boolean isState(String paramString) {
        String REGEX_LABEL = "^user\\/\\d{0,12}\\/state\\/";
        Pattern p = Pattern.compile(REGEX_LABEL, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(paramString);
        return m.find();
    }


}
