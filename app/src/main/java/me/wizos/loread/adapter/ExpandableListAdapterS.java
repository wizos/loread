package me.wizos.loread.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.socks.library.KLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.net.Api;
import me.wizos.loread.view.ExpandableListViewS;
import me.wizos.loread.view.IconFontView;

/**
 * @author Wizos on 2017/9/17.
 */

public class ExpandableListAdapterS extends BaseExpandableListAdapter implements ExpandableListViewS.HeaderAdapter { // implements ExpandableListViewS.HeaderAdapter
    private Context context;
    private List<Tag> tags = new ArrayList<>();
    private ExpandableListView listView;
//    private List<List<Feed>> feeds;

    public ExpandableListAdapterS(Context context, List<Tag> tags, ExpandableListView listView) {
        this.context = context;
        this.tags = tags;
//        feeds = new ArrayList<>(tags.size());
//        for (int i = 0; i < tags.size(); i++) {
//            try {
//                feeds.add(i, tags.get(i).getFeeds());
//            } catch (RuntimeException e) {
//                KLog.i("无法获取子项：" + i + tags.get(i).getTitle());
//            }
//        }

        this.listView = listView;
    }

    //  获得某个父项的某个子项
    @Override
    public Object getChild(int groupPos, int childPos) {
        try {
            return tags.get(groupPos).getFeeds().get(childPos);
//            return feeds.get(groupPos).get(childPos);
        } catch (RuntimeException e) {
            KLog.i("无法获取子项B：" + WithDB.i().getFeeds().get(childPos));
            return 0;
        }
    }

    public void removeChild(int groupPos, Feed feed) {
        try {
            tags.get(groupPos).getFeeds().remove(feed);
//            feeds.get(groupPos).remove(childPos);
        } catch (Exception e) {
        }
    }

    //  获得父项的数量
    @Override
    public int getGroupCount() {
        return tags.size();
    }

    //  获得某个父项的子项数目
    @Override
    public int getChildrenCount(int groupPos) {
        try {
            return tags.get(groupPos).getFeeds().size();
//            return feeds.get(groupPos).size();
        } catch (RuntimeException e) {
//            KLog.e("子项的数量B：" + WithDB.i().getFeeds().size());
            return 0; // WithDB.i().getFeeds().size()
        }
    }

    //  获得某个父项
    @Override
    public Object getGroup(int parentPos) {
        KLog.e("getGroup：" + tags.get(parentPos));
        return tags.get(parentPos);
    }

    //  获得某个父项的id
    @Override
    public long getGroupId(int parentPos) {
        return parentPos;
    }

    //  获得某个父项的某个子项的id
    @Override
    public long getChildId(int parentPos, int childPos) {
        return childPos;
    }

    //  按函数的名字来理解应该是是否具有稳定的id，这个方法目前一直都是返回false，没有去改动过
    @Override
    public boolean hasStableIds() {
        return false;
    }


    public class ItemViewHolder {
        public String id; // 当前这个 group(Tag) 或者 child(Feed) 的 id
        public int type; // 当前这个 Item 是 group 还是 child
        public int groupPos;
        public int childPos;
        public final static int TYPE_GROUP = 0;
        public final static int TYPE_CHILD = 1;

        IconFontView icon;
        TextView title;
        TextView count;
    }

//    private ArrayMap<Tag,List<Feed>> stream = new ArrayMap<>();
//    public void updateData( ArrayMap<Tag,List<Feed>> stream){
//        this.stream = stream;
//    }



    @Override
    public View getGroupView(final int groupPos, final boolean isExpanded, View convertView, final ViewGroup parent) {
        ItemViewHolder groupViewHolder;
        // 使用一个 ViewHolder，可减少在该函数中每次都要去 findViewById ，这很费时间。具体见：https://zhidao.baidu.com/question/544207312.html
        if (convertView == null) {
            groupViewHolder = new ItemViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.tag_expandable_item_group, null);
            groupViewHolder.icon = convertView.findViewById(R.id.group_item_icon);
            groupViewHolder.title = convertView.findViewById(R.id.group_item_title);
            groupViewHolder.count = convertView.findViewById(R.id.group_item_count);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (ItemViewHolder) convertView.getTag();
        }

        if (isExpanded) {
            groupViewHolder.icon.setText(context.getString(R.string.font_arrow_down));
        } else {
            groupViewHolder.icon.setText(context.getString(R.string.font_arrow_right));
        }

        groupViewHolder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                KLog.e("箭头被点击");
                if (isExpanded) {
                    ((ExpandableListViewS) parent).collapseGroup(groupPos);
                    ((IconFontView) v).setText(context.getString(R.string.font_arrow_right));
                } else {
                    ((ExpandableListViewS) parent).expandGroup(groupPos);
                    ((IconFontView) v).setText(context.getString(R.string.font_arrow_down));
                }
            }
        });


        try {
            Tag tag = tags.get(groupPos);
            if (tag.getFeeds().size() == 0) {
                groupViewHolder.icon.setText(context.getString(R.string.font_tag));
            }
            groupViewHolder.id = tag.getId();
            groupViewHolder.type = ItemViewHolder.TYPE_GROUP;
            groupViewHolder.groupPos = groupPos;
            groupViewHolder.title.setText(tag.getTitle());

            // 方法一
            int count = 0;
            if (tag.getId().contains(Api.U_READING_LIST)) {
                count = WithDB.i().getUnreadArtsCount();
            } else if (tag.getId().contains(Api.U_NO_LABEL)) {
                count = WithDB.i().getUnreadArtsCountNoTag();
            } else {
                count = tag.getUnreadCount();
            }
            groupViewHolder.count.setText(String.valueOf(count));

            // 方法2
//            groupViewHolder.count.setText(String.valueOf( UnreadCountUtil.getTagUnreadCount(tag.getId()) ));
//            count = UnreadCountUtil.getTagUnreadCount(tag.getId());


            groupViewHolder.count.setText(String.valueOf(count));
            groupViewHolder.count.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);

        } catch (Exception e) {
            groupViewHolder.id = "";
            groupViewHolder.type = ItemViewHolder.TYPE_GROUP;
            groupViewHolder.groupPos = groupPos;
            groupViewHolder.title.setText(App.i().getString(R.string.item_error));
//            KLog.e("父分类：" + theTag.getTitle() + "--" + theTag.getUnreadcount());
        }
        return convertView;
    }


    private ArrayMap<ItemViewHolder, QueryTask> map = new ArrayMap<>();
    private QueryTask queryTask = null;
    //  获得子项显示的view
    @Override
    public View getChildView(int groupPos, int childPos, boolean isExpanded, View convertView, final ViewGroup parent) {
        ItemViewHolder childViewHolder;
        if (convertView == null) {
            childViewHolder = new ItemViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.tag_expandable_item_child, null);
//            childViewHolder.icon = (IconFontView) convertView.findViewById(R.id.group_item_icon);
            childViewHolder.title = convertView.findViewById(R.id.child_item_title);
            childViewHolder.count = convertView.findViewById(R.id.child_item_count);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ItemViewHolder) convertView.getTag();
//            queryTask = map.get(childViewHolder);
//            if( queryTask!=null  ){
//                queryTask.cancel(true);
//            }
        }

        try {
            final Feed feed = tags.get(groupPos).getFeeds().get(childPos);
            childViewHolder.id = feed.getId();
            childViewHolder.type = ItemViewHolder.TYPE_CHILD;
            childViewHolder.groupPos = groupPos;
            childViewHolder.childPos = childPos;
            childViewHolder.title.setText(feed.getTitle());

            Integer count;
            count = feed.getUnreadCount();
            childViewHolder.count.setText(String.valueOf(count));


//            if (App.unreadCountMap.containsKey(feed.getId())) {
//                count = App.unreadCountMap.get(feed.getId());
////                KLog.e("【getChildView】复用" + feed.getId() + " -- " + feed.getTitle());
//            } else {
//                count = WithDB.i().getUnreadArtsCountByFeed(feed.getId());
////                App.unreadCountMap.put(feed.getId(), count);
//                KLog.e("数据库，【getChildView】初始" + feed.getId() + " -- " + feed.getTitle() + "  数量：" + count);
//            }
//            childViewHolder.count.setText(String.valueOf(count));

            childViewHolder.count.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);

            // 方法2
//            childViewHolder.count.setVisibility(View.INVISIBLE);
//            queryTask = new QueryTask(this,childViewHolder);
//            queryTask.execute(feed.getId());
//            map.put(childViewHolder,queryTask );

            // 方法3
//            WithDB.i().query2(feed.getId(), new DBInterface.Query() {
//                @Override
//                public void onQuerySuccess(String id, int entries) {
//                    if( !childViewHolder.id.equals(id) ){
//                        KLog.e(id + "获取到的错乱了");
//                        return;
//                    }
//                    KLog.e(id + "获取正常的：" + entries);
//                    childViewHolder.count.setText(String.valueOf(entries));
//                    childViewHolder.count.setVisibility( entries > 0 ? View.VISIBLE : View.INVISIBLE );
//                }
//            });
        } catch (RuntimeException e) {
            childViewHolder.id = "";
            childViewHolder.type = ItemViewHolder.TYPE_CHILD;
            childViewHolder.groupPos = groupPos;
            childViewHolder.childPos = childPos;
            childViewHolder.title.setText(App.i().getString(R.string.item_error));
            KLog.e(e);
        }
        return convertView;
    }


    //
    // Params, Progress, Result
    private static class QueryTask extends AsyncTask<String, String, Integer> {
        private WeakReference<ExpandableListAdapterS> mAdapter;
        private ItemViewHolder childViewHolder;
        private String feedId;

        QueryTask(ExpandableListAdapterS adapter, ItemViewHolder childViewHolder) {
            mAdapter = new WeakReference<>(adapter);
            this.childViewHolder = childViewHolder;
        }

        @Override
        protected Integer doInBackground(String... params) {
            if (isCancelled()) {
                return 0;
            }
            feedId = params[0];
//            int count = WithDB.i().getUnreadArtsCountByFeed2(feedId);
//            publishProgress(feedId,count+"");
            //返回结果
            return WithDB.i().getUnreadArtsCountByFeed2(feedId);
        }

        /**
         * 在doInbackground之后执行
         */
        @Override
        protected void onPostExecute(Integer count) {
            try {
                if (!childViewHolder.id.equals(feedId)) {
                    KLog.e(feedId + "获取错乱了");
                    return;
                }
                KLog.e(feedId + "获取正常的：" + count);
                childViewHolder.count.setText(count + "");
                childViewHolder.count.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
            } catch (Exception e) {
                KLog.e("出错了");
                e.printStackTrace();
            }
        }
//        @Override
//        protected void onProgressUpdate(String... progress) {
//            if(isCancelled()){
//                return;
//            }
//            String feedId = progress[0];
//            String count = progress[1];
//
//            try {
//                if( !childViewHolder.id.equals(feedId) ){
//                    KLog.e(feedId + "获取错乱了");
//                    return;
//                }
//
//                KLog.e(feedId + "获取正常的：" + count);
//                childViewHolder.count.setText( count );
//                childViewHolder.count.setVisibility( Integer.valueOf(count) > 0 ? View.VISIBLE : View.INVISIBLE );
//            }catch (Exception e){
//                KLog.e("出错了");
//                e.printStackTrace();
//            }
//        }
    }


    //  子项是否可选中，如果需要设置子项的点击事件，需要返回true
    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    // 根据以下2个案例修改而成
    // https://github.com/qianxin2016/DockingExpandableListView/blob/master/app/src/main/java/com/xinxin/dockingexpandablelistview/adapter/DockingExpandableListViewAdapter.java
    // https://github.com/CotTan/PinnedHeaderExpandable/blob/master/app/src/main/java/intelstd/com/pinnedheaderexpandable/PinnedHeaderExpandableListView.java
    @Override
    public int getHeaderState(int firstVisibleGroupPosition, int firstVisibleChildPosition) {
        // 如果这个 group 不包含 child 并且没有展开，那么不绘制 header view
        if (firstVisibleChildPosition == -1 && !listView.isGroupExpanded(firstVisibleGroupPosition)) {
            return PINNED_HEADER_GONE;
        }
        // 到达当前 Group 的最后一个 Child，准备对接下一个 Group header。
        if (firstVisibleChildPosition == getChildrenCount(firstVisibleGroupPosition) - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }
        return PINNED_HEADER_VISIBLE;
    }


    @Override
    public void configureHeader(View header, int groupPosition, int childPosition, int alpha) {
        String groupTitle = tags.get(groupPosition).getTitle();
        ((TextView) header.findViewById(R.id.header_item_title)).setText(groupTitle);
    }

}
