package me.wizos.loread.net;

//import com.squareup.okhttp.Request;

import com.lzy.okgo.request.base.Request;

import java.util.ArrayList;

import me.wizos.loread.App;
import me.wizos.loread.bean.gson.itemContents.Items;

/**
 * 基础的 Activity
 * Created by Wizos on 2016/3/5.
 */
public class Api {

    public static Request request;
    public static String MyFileType = ".loread";
    static final String INOREADER_APP_ID = "1000001277";
    static final String INOREADER_APP_KEY = "8dByWzO4AYi425yx5glICKntEY2g3uJo";
    public static String INOREADER_ATUH = "";
    public static String HOST = "";

    public static String ACTION_LOGIN = "login";


    public static final String HOST_OFFICIAL = "https://www.inoreader.com";
    public static final String U_CLIENTLOGIN ="/accounts/ClientLogin";

    public static final String U_USER_INFO ="/reader/api/0/user-info";
    public static final String U_TAG_LIST = "/reader/api/0/tag/list";
    public static final String U_STREAM_PREFS ="/reader/api/0/preference/stream/list";
    public static final String U_SUSCRIPTION_LIST = "/reader/api/0/subscription/list"; // 这个不知道现在用在了什么地方
    public static final String U_UNREAD_COUNTS ="/reader/api/0/unread-count";
    public static final String U_ITEM_IDS = "/reader/api/0/stream/items/ids"; // 获取所有文章的id
    public static final String U_ITEM_CONTENTS = "/reader/api/0/stream/items/contents"; // 获取流的内容
    public static final String U_EDIT_TAG ="/reader/api/0/edit-tag";

    public static final String U_STREAM_CONTENTS ="/reader/api/0/stream/contents/";
    public static final String U_Stream_Contents_Atom ="/reader/atom";
    public static final String U_Stream_Contents_User ="/reader/api/0/stream/contents/user/";

    public static final String U_Search = "state/com.google/search";
    public static final String U_READING_LIST ="/state/com.google/reading-list";
    public static final String U_STARRED ="user/-/state/com.google/starred";
    public static final String U_NO_LABEL ="/state/com.google/no-label";
    public static final String U_UNREAND ="/state/com.google/unread";

    public static String getLabelStreamFlag() {
        return "user/" + App.UserID + "/label/";
    }


//    public static String U_READED ="user/-/state/com.google/read";
//    public static String U_BROADCAST ="user/-/state/com.google/broadcast";
//    public static String U_LIKED ="user/-/state/com.google/like";
//    public static String U_SAVED ="user/-/state/com.google/saved-web-pages";



    /*
    Streams 可以是 feeds, tags (folders) 或者是 system types.
    feed/http://feeds.arstechnica.com/arstechnica/science - Feed.
    user/-/label/Tech - Tag (or folder).
    user/-/state/com.google/read - Read articles.已阅读文章
    user/-/state/com.google/starred - Starred articles.
    user/-/state/com.google/broadcast - Broadcasted articles.
    user/-/state/com.google/like - Likes articles.
    user/-/state/com.google/saved-web-pages - Saved web pages.
    user/-/state/com.google/reading-list.阅读列表(包括已读和未读)
     */
    /**
     * 从上面的API也可以知道，这些分类是很混乱的。
     * 本质上来说，Tag 或者 Feed 都是一组 Articles (最小单位) 的集合（Stream）。（Tag 是 Feed 形而上的抽离/集合）
     * 而我们用户对其中某些 Article 的 Read, Star, Save, Comment, Broadcast 等操作，无意中又生成了一组集合（Stream）
     * 所以为了以后的方便，我最好是抽离/包装出一套标准的 Api。
     */


    public static final int ActivityResult_TagToMain = 1;
    public static final int ActivityResult_ArtToMain = 2;

    public static final int MSG_DOUBLE_TAP = -1;

    public static final int F_NONET = -2;
    public static final int F_NoMsg = 0;
    public static final int F_Request = 1;
    public static final int F_Response = 2;

    public static final int F_BITMAP = 3;

    public static final int SYNC_START = 101;
    public static final int SYNC_NEED_AUTH = 102;
    public static final int SYNC_FAILURE = 0;
    public static final int SYNC_SUCCESS = 100;
    public static final int SYNC_PROCESS = -100;

    public static final int M_BEGIN_SYNC = 10;
    public static final int S_CLIENTLOGIN = 11;
    public static final int S_USER_INFO = 12;
    public static final int S_TAGS_LIST = 13;
    public static final int S_STREAM_PREFS = 14;
    public static final int S_SUBSCRIPTION_LIST = 15;

    public static final int S_UNREAD_COUNTS = 30;

    public static final int S_ITEM_IDS = 31;
    public static final int S_ITEM_IDS_UNREAD = 32;
    public static final int S_ITEM_IDS_UNREAD_LOOP = 33;
    public static final int S_ITEM_IDS_STARRED = 35;
    public static final int S_ITEM_IDS_STARRED_LOOP = 36;

    public static final int S_STARRED = 37;
    public static final int S_STREAM_CONTENTS_STARRED = 38;

    public static final int S_READING_LIST = 39;
    public static final int S_ITEM_CONTENTS = 40;

    public static final int S_EDIT_TAG = 60;
    public static final int S_ARTICLE_CONTENTS = 61;

    public static final int S_BITMAP = 62;
    public static final int S_Contents = 63; // 似乎没有被用到


    public static ArrayList<Items> itemlist;

    public static final int ReplaceImgSrc = 69;
//    public static final int ReplaceImgSrcAgain = 68;

    public static final int ImgMeta_Downover = 1; // 下载完成
    public static final int ImgMeta_Downing = 0; // 未下载
    public static final String ImgState_Downing = "0";
    public static final String ImgState_NoImg = "";
    public static final String ImgState_Over = "1";


    // Note: 下面这个是不是能做成map的形式方便维护
    public static int url2int(String api){
        if (api.equals(HOST + U_CLIENTLOGIN)){
            return S_CLIENTLOGIN;
        }else if(api.equals(HOST + U_USER_INFO)){
            return S_USER_INFO;
        }else if(api.equals(HOST + U_STREAM_PREFS)){
            return S_STREAM_PREFS;
        } else if (api.equals(HOST + U_TAG_LIST)) {
            return S_TAGS_LIST;
        }else if(api.equals(HOST + U_SUSCRIPTION_LIST)) {
            return S_SUBSCRIPTION_LIST;
        }else if(api.contains(HOST + U_ITEM_IDS)){
            return S_ITEM_IDS;
        }else if(api.contains( U_READING_LIST )){
            return S_READING_LIST;
        }else if(api.equals(HOST + U_UNREAD_COUNTS)){
            return S_UNREAD_COUNTS;
        }else if(api.equals(HOST + U_ITEM_CONTENTS)){
            return S_ITEM_CONTENTS;
        } else if (api.equals(HOST + U_ITEM_CONTENTS)) {
            return S_ARTICLE_CONTENTS;
        }else if(api.equals(HOST + U_EDIT_TAG)){
            return S_EDIT_TAG;
        }else if(api.contains( U_STREAM_CONTENTS + U_STARRED)){
            return S_STREAM_CONTENTS_STARRED;
        }
        return F_NoMsg;
    }

    /**
     * 是否需要改变这个为 int 以方便比较呢？
     */
    public static final String ART_READED = "Readed";// 1 已读
    public static final String ART_UNREADING = "UnReading"; // 00 强制未读
    public static final String ART_UNREAD = "UnRead"; // 0 未读
    public static final String ART_STARED = "Stared"; // 1
    public static final String ART_UNSTAR = "UnStar"; // 0
    public static final String ART_ALL = "%";


    public static final String SAVE_DIR_CACHE = "cache";
    public static final String SAVE_DIR_BOX = "box";
    public static final String SAVE_DIR_STORE = "store";
    public static final String SAVE_DIR_BOXREAD = "boxRead";
    public static final String SAVE_DIR_STOREREAD = "storeRead";


//    public static final String LIST_READ = "Readed";
//    public static final String LIST_UNREADING = "UnReading";
//    public static final String LIST_UNSTAR = "UnStar";

//    public static final String ARTICLE_HEADER = "UnRead";

}