package me.wizos.loread.net;

import com.squareup.okhttp.Request;

import java.util.ArrayList;

import me.wizos.loread.gson.Item;

/**
 * Created by Wizos on 2016/3/5.
 */
public class API {


    public static Request request;

    public static final String INOREADER_APP_ID = "1000001277";
    public static final String INOREADER_APP_KEY = "8dByWzO4AYi425yx5glICKntEY2g3uJo";
    public static String INOREADER_ATUH = "";
    public static final String INOREADER_BASE_URL = "https://www.inoreader.com/";

    public static String U_CLIENTLOGIN ="https://www.inoreader.com/accounts/ClientLogin";

    public static String U_USER_INFO ="https://www.inoreader.com/reader/api/0/user-info";
    public static String U_TAGS_LIST ="https://www.inoreader.com/reader/api/0/tag/list";
    public static String U_STREAM_PREFS ="https://www.inoreader.com/reader/api/0/preference/stream/list";
    public static String U_SUSCRIPTION_LIST ="https://www.inoreader.com/reader/api/0/subscription/list";
    public static String U_UNREAD_COUNTS ="https://www.inoreader.com/reader/api/0/unread-count";
    public static String U_ITEM_IDS ="https://www.inoreader.com/reader/api/0/stream/items/ids";
    public static String U_ITEM_CONTENTS ="https://www.inoreader.com/reader/api/0/stream/items/contents";
    public static String U_ARTICLE_CONTENTS ="https://www.inoreader.com/reader/api/0/stream/items/contents";
    public static String U_EDIT_TAG ="https://www.inoreader.com/reader/api/0/edit-tag";

    public static String U_STREAM_CONTENTS ="https://www.inoreader.com/reader/api/0/stream/contents/";
    public static String U_Stream_Contents_Atom ="https://www.inoreader.com/reader/atom";
    public static String U_Stream_Contents_User ="https://www.inoreader.com/reader/api/0/stream/contents/user/";

    public static String U_READING_LIST ="/state/com.google/reading-list";
    public static String U_STARRED ="user/-/state/com.google/starred";
    public static String U_NO_LABEL ="/state/com.google/no-label";
    public static String U_UNREAND ="/state/com.google/unread";

    public static String proxySite = "http://wizos.me/Inoreader.php";


//    public static String U_READED ="user/-/state/com.google/read";
//    public static String U_BROADCAST ="user/-/state/com.google/broadcast";
//    public static String U_LIKED ="user/-/state/com.google/like";
//    public static String U_SAVED ="user/-/state/com.google/saved-web-pages";

    public static String MyFileType = ".loread";



//    public static final int SUCCESS_AUTH = 1;
//    public static final int FAILURE_AUTH = 2;
//    public static final int SUCCESS_NET = 3;
//    public static final int ID_UPDATE_UI = 4;
//    public static final int ID_FROM_CACHE = 5;

    public static final int M_BEGIN_SYNC = 66;
    public static final int S_CLIENTLOGIN = 10;
    public static final int S_USER_INFO = 11;
    public static final int S_TAGS_LIST = 12;
    public static final int S_STREAM_PREFS = 13;
    public static final int S_SUBSCRIPTION_LIST = 22;
    public static final int S_UNREAD_COUNTS = 23;
    public static final int S_ITEM_IDS = 20;
    public static final int S_ITEM_IDS_STARRED = 28;
    public static final int S_STREAM_CONTENTS_STARRED = 31;
    public static final int S_ITEM_CONTENTS = 24;
    public static final int S_READING_LIST = 21;
    public static final int S_EDIT_TAG = 25;
    public static final int S_BITMAP = 26;
    public static final int S_Contents = 19;
    public static final int S_ARTICLE_CONTENTS = 27;


    public static final int S_ALL_STARRED = 30;
    public static final int FAILURE = 00;
    public static final int FAILURE_Request = 01;
    public static final int FAILURE_Response = 02;
    public static final int F_BITMAP = 03;
    public static ArrayList<Item> itemlist;



    public static int url2int(String api){
        if (api.equals(U_CLIENTLOGIN)){
            return S_CLIENTLOGIN;
        }else if(api.equals(U_USER_INFO)){
            return S_USER_INFO;
        }else if(api.equals(U_STREAM_PREFS)){
            return S_STREAM_PREFS;
        }else if(api.equals(U_TAGS_LIST)){
            return S_TAGS_LIST;
        }else if(api.equals(U_SUSCRIPTION_LIST)) {
            return S_SUBSCRIPTION_LIST;
        }else if(api.contains(U_ITEM_IDS)){
            return S_ITEM_IDS;
        }else if(api.contains(U_READING_LIST)){
            return S_READING_LIST;
        }else if(api.equals(U_UNREAD_COUNTS)){
            return S_UNREAD_COUNTS;
        }else if(api.equals(U_ITEM_CONTENTS)){
            return S_ITEM_CONTENTS;
        }else if(api.equals(U_ARTICLE_CONTENTS)) {
            return S_ARTICLE_CONTENTS;
        }else if(api.equals(U_EDIT_TAG)){
            return S_EDIT_TAG;
        }else if(api.contains(U_STREAM_CONTENTS + U_STARRED)){
            return S_STREAM_CONTENTS_STARRED;
        }
        return FAILURE;
    }

    /**
     * 是否需要改变这个为 int 以方便比较呢？
     */
    public static final String ART_READ = "Readed";// 1
    public static final String ART_READING = "UnReading"; // 00
    public static final String ART_UNREAD = "UnRead"; // 0
    public static final String ART_STAR = "Stared"; // 1
    public static final String ART_UNSTAR = "UnStar"; // 0

    public static final String LIST_ALL = "%";
    public static final String LIST_STAR = "Stared";
    public static final String LIST_UNREAD = "UnRead";

//    public static final String LIST_READ = "Readed";
//    public static final String LIST_UNREADING = "UnReading";
//    public static final String LIST_UNSTAR = "UnStar";


//    public static final String ARTICLE_HEADER = "UnRead";

}
