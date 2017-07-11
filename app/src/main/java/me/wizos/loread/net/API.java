package me.wizos.loread.net;

import com.squareup.okhttp.Request;

import java.util.ArrayList;

import me.wizos.loread.bean.gson.Item;

/**
 * 基础的 Activity
 * Created by Wizos on 2016/3/5.
 */
public class API {
    public static Request request;
    public static String MyFileType = ".loread";
    static final String INOREADER_APP_ID = "1000001277";
    static final String INOREADER_APP_KEY = "8dByWzO4AYi425yx5glICKntEY2g3uJo";
    public static String INOREADER_ATUH = "";

    public static final String HOST_OFFICIAL = "https://www.inoreader.com";
    public static final String HOST_PROXY = "http://ino-socoxx.rhcloud.com";

    public static String HOST = "";

    public static final String U_CLIENTLOGIN ="/accounts/ClientLogin";

    public static final String U_USER_INFO ="/reader/api/0/user-info";
    public static final String U_TAGS_LIST ="/reader/api/0/tag/list";
    public static final String U_STREAM_PREFS ="/reader/api/0/preference/stream/list";
    public static final String U_SUSCRIPTION_LIST ="/reader/api/0/subscription/list";
    public static final String U_UNREAD_COUNTS ="/reader/api/0/unread-count";
    public static final String U_ITEM_IDS ="/reader/api/0/stream/items/ids";
    public static final String U_ITEM_CONTENTS ="/reader/api/0/stream/items/contents";
    public static final String U_ARTICLE_CONTENTS ="/reader/api/0/stream/items/contents";
    public static final String U_EDIT_TAG ="/reader/api/0/edit-tag";

    public static final String U_STREAM_CONTENTS ="/reader/api/0/stream/contents/";
    public static final String U_Stream_Contents_Atom ="/reader/atom";
    public static final String U_Stream_Contents_User ="/reader/api/0/stream/contents/user/";

    public static final String U_READING_LIST ="/state/com.google/reading-list";
    public static final String U_STARRED ="user/-/state/com.google/starred";
    public static final String U_NO_LABEL ="/state/com.google/no-label";
    public static final String U_UNREAND ="/state/com.google/unread";


//    public static String U_READED ="user/-/state/com.google/read";
//    public static String U_BROADCAST ="user/-/state/com.google/broadcast";
//    public static String U_LIKED ="user/-/state/com.google/like";
//    public static String U_SAVED ="user/-/state/com.google/saved-web-pages";




//    public static final int SUCCESS_AUTH = 1;
//    public static final int FAILURE_AUTH = 2;
//    public static final int SUCCESS_NET = 3;
//    public static final int ID_UPDATE_UI = 4;
//    public static final int ID_FROM_CACHE = 5;

    public static final int MSG_DOUBLE_TAP = -1;
    public static final int H_WEB = -2;


    public static final int F_NoMsg = 0;
    public static final int F_Request = 1;
    public static final int F_Response = 2;

    public static final int F_BITMAP = 3;
    public static final int FAILURE = 0;
    public static final int SUCCESS = 100;
    public static final int PROCESS = -100;

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
    public static final int S_Contents = 63;


    public static ArrayList<Item> itemlist;

    public static final int ReplaceImgSrc = 69;
//    public static final int ReplaceImgSrcAgain = 68;

    public static final String ImgState_Downing = "0";
    public static final String ImgState_NoImg = "";
    public static final String ImgState_Down_Over = "1";
    //    public static final String ImgState_Over = "1";
    public static final int ImgState_Over = 1;






    public static int url2int(String api){
        if (api.equals(HOST + U_CLIENTLOGIN)){
            return S_CLIENTLOGIN;
        }else if(api.equals(HOST + U_USER_INFO)){
            return S_USER_INFO;
        }else if(api.equals(HOST + U_STREAM_PREFS)){
            return S_STREAM_PREFS;
        }else if(api.equals(HOST + U_TAGS_LIST)){
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
        }else if(api.equals(HOST + U_ARTICLE_CONTENTS)) {
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

    public static final String SAVE_DIR_CACHE = "cache";
    public static final String SAVE_DIR_BOX = "box";
    public static final String SAVE_DIR_STORE = "store";
    public static final String SAVE_DIR_BOXREAD = "boxRead";
    public static final String SAVE_DIR_STOREREAD = "storeRead";

    public static final String LIST_ALL = "%";
    public static final String LIST_STARED = "Stared";
    public static final String LIST_UNREAD = "UnRead";

//    public static final String LIST_READ = "Readed";
//    public static final String LIST_UNREADING = "UnReading";
//    public static final String LIST_UNSTAR = "UnStar";

//    public static final String ARTICLE_HEADER = "UnRead";

}
