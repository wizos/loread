package me.wizos.loread.utils;

import android.text.TextUtils;

import me.wizos.loread.App;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.net.Api;

/**
 * Created by Wizos on 2018/4/25.
 */

public class UnreadCountUtil {

    public static int getTagUnreadCount(String tagId) {
        int count = 0;
        if (TextUtils.isEmpty(tagId)) {
            return count;
        }
        if (tagId.contains(Api.U_READING_LIST)) {
            count = WithDB.i().getUnreadArtsCount();
        } else if (tagId.contains(Api.U_NO_LABEL)) {
            count = WithDB.i().getUnreadArtsCountNoTag();
        } else {
            if (App.unreadCountMap.containsKey(tagId)) {
                count = App.unreadCountMap.get(tagId);
//                KLog.e("【getGroupView】复用" + tagId  );
            } else {
                count = WithDB.i().getUnreadArtsCountByTag(tagId);
                App.unreadCountMap.put(tagId, count);
//                KLog.e("【getGroupView】初始" + tagId );
            }
        }
        return count;
    }


}
