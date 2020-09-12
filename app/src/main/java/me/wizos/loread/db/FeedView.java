package me.wizos.loread.db;

import androidx.room.DatabaseView;

import me.wizos.loread.App;

/**
 * Feed 与 Category 是 多对多关系，即一个 Feed 可以存在与多个 Category 中，Category 也可以包含多个 Feed
 * Created by Wizos on 2020/3/17.
 */
@DatabaseView(
        "SELECT uid,id,title,feedUrl,htmlUrl,iconUrl,displayMode,UNREAD_SUM AS unreadCount,STAR_SUM AS starCount,ALL_SUM AS allCount,state FROM FEED" +
        " LEFT JOIN (SELECT uid AS article_uid, feedId, COUNT(1) AS UNREAD_SUM FROM article WHERE readStatus != " + App.STATUS_READED + " GROUP BY uid,feedId) A ON FEED.uid = A.article_uid AND FEED.id = A.feedId" +
        " LEFT JOIN (SELECT uid AS article_uid, feedId, COUNT(1) AS STAR_SUM FROM article WHERE starStatus = " + App.STATUS_STARED + " GROUP BY uid,feedId) B ON FEED.uid = B.article_uid AND FEED.id = B.feedId" +
        " LEFT JOIN (SELECT uid AS article_uid, feedId, COUNT(1) AS ALL_SUM FROM article GROUP BY uid,feedId) C ON FEED.uid = c.article_uid AND FEED.id = C.feedId"
)
public class FeedView extends Feed{
}
