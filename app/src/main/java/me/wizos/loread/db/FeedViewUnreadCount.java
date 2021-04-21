package me.wizos.loread.db;

import androidx.room.DatabaseView;

import me.wizos.loread.App;

@DatabaseView(
        "SELECT feed.uid, feed.id, a.unreadCount FROM feed " +
                "INNER JOIN (" +
                " SELECT uid, feedId, COUNT(1) AS unreadCount FROM article WHERE readStatus = " + App.STATUS_UNREAD + " GROUP BY uid, feedId" +
                " UNION" +
                " SELECT uid, feedId, COUNT(1) AS unreadCount FROM article WHERE readStatus = " + App.STATUS_UNREADING + " GROUP BY uid, feedId" +
                " ) a ON feed.uid = a.uid AND feed.id = a.feedId "
)
public class FeedViewUnreadCount extends Feed{
}
