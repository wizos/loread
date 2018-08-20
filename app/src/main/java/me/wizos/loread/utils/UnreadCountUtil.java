package me.wizos.loread.utils;

import android.text.TextUtils;

import me.wizos.loread.data.WithDB;
import me.wizos.loread.net.Api;

/**
 * Created by Wizos on 2018/4/25.
 */

public class UnreadCountUtil {

//    public static int getTagUnreadCount(String tagId) {
//        int count = 0;
//        if (TextUtils.isEmpty(tagId)) {
//            return count;
//        }
//        if (tagId.contains(Api.U_READING_LIST)) {
//            count = WithDB.i().getUnreadArtsCount();
//        } else if (tagId.contains(Api.U_NO_LABEL)) {
//            count = WithDB.i().getUnreadArtsCountNoTag();
//        } else {
//            if (App.unreadCountMap.containsKey(tagId)) {
//                count = App.unreadCountMap.get(tagId);
////                KLog.e("【getGroupView】复用" + tagId  );
//            } else {
//                count = WithDB.i().getUnreadArtsCountByTag(tagId);
//                App.unreadCountMap.put(tagId, count);
////                KLog.e("【getGroupView】初始" + tagId );
//            }
//        }
//        return count;
//    }


    public static int getTag(String id) {
        int count = 0;
        if (id.contains(Api.U_READING_LIST)) {
            count = WithDB.i().getUnreadArtsCount();
        } else if (id.contains(Api.U_NO_LABEL)) {
            count = WithDB.i().getUnreadArtsCountNoTag();
        } else {
            try {
                count = WithDB.i().getTag(id).getUnreadCount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return count;
    }


    public static int getUnreadCount(String id) {
        if (TextUtils.isEmpty(id)) {
            return 0;
        }
        if (id.startsWith("user/")) {
            return getTag(id);
        } else if (id.startsWith("feed/")) {
            try {
                return WithDB.i().getFeed(id).getUnreadCount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }



}
