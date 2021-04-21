package me.wizos.loread.db;

import androidx.room.DatabaseView;

import me.wizos.loread.App;

/**
 * Feed 与 Category 是 多对多关系，即一个 Feed 可以存在与多个 Category 中，Category 也可以包含多个 Feed
 * Created by Wizos on 2020/3/17.
 */
@DatabaseView(
        "SELECT feed.uid, feed.id, a.starCount FROM feed INNER JOIN ( SELECT uid, feedId, COUNT(1) AS starCount FROM article WHERE starStatus = " + App.STATUS_STARED +  " GROUP BY uid, feedId ) a ON feed.uid = a.uid AND feed.id = a.feedId"
)
public class FeedViewStarCount extends Feed{
}
