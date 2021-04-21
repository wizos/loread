// package me.wizos.loread.db;
//
// import androidx.room.DatabaseView;
//
// /**
//  indices = {@Index({"id","uid","title"})} ,
//  * Created by Wizos on 2020/3/17.
//  */
//
// @DatabaseView(
//         "SELECT CATEGORY.uid,id,title,UNREAD_SUM AS unreadCount,STAR_SUM AS starCount,ALL_SUM AS allCount FROM CATEGORY " +
//                 "LEFT JOIN " +
//                 "(" +
//                 "	SELECT uid,categoryId,sum(UNREAD_SUM) AS UNREAD_SUM,sum(STAR_SUM) AS STAR_SUM,sum(ALL_SUM) AS ALL_SUM FROM " +
//                 "	(" +
//                 "		SELECT FEEDCATEGORY.uid,categoryId,feedId,UNREAD_SUM,STAR_SUM,ALL_SUM FROM FEEDCATEGORY " +
//                 "		LEFT JOIN (SELECT uid,id,unreadCount AS UNREAD_SUM, starCount AS STAR_SUM, allCount AS ALL_SUM FROM feedview) AS FEED ON feedcategory.uid = FEED.uid AND feedcategory.feedId = FEED.ID " +
//                 "	) AS FeedCategoryCount GROUP BY uid,CATEGORYID " +
//                 ") AS C ON CATEGORY.uid = C.uid AND CATEGORY.ID = C.CATEGORYID "
// )
// public class CategoryView extends Category{
// }
