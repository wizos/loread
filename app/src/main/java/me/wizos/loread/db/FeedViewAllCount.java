package me.wizos.loread.db;

import androidx.room.DatabaseView;

@DatabaseView(
        "SELECT feed.uid, feed.id, a.allCount FROM feed INNER JOIN ( SELECT uid, feedId, COUNT(1) AS allCount FROM article GROUP BY uid, feedId ) a ON feed.uid = a.uid AND feed.id = a.feedId"
)
public class FeedViewAllCount extends Feed{
}
