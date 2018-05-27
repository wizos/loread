package me.wizos.loread.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.utils.UnreadCountUtil;
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

    public void removeChild(int groupPos, int childPos) {
        try {
            tags.get(groupPos).getFeeds().remove(childPos);
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

//            int count = 0;
//            if (tag.getId().contains(Api.U_READING_LIST)) {
//                count = WithDB.i().getUnreadArtsCount();
//            } else if (tag.getId().contains(Api.U_NO_LABEL)) {
//                count = WithDB.i().getUnreadArtsCountNoTag();
//            } else {
//                if( App.unreadCountMap.containsKey(tag.getId()) ){
//                    count = App.unreadCountMap.get(tag.getId());
//                    KLog.e("【getGroupView】复用" + tag.getId() + " -- " + tag.getTitle() + "--"  );
//                }else {
//                    count = WithDB.i().getUnreadArtsCountByTag(tag);
//                    App.unreadCountMap.put(tag.getId(),count);
//                    KLog.e("【getGroupView】初始" + tag.getId() + " -- " + tag.getTitle() + "--"  );
//                }
//            }
            groupViewHolder.count.setText(String.valueOf(UnreadCountUtil.getTagUnreadCount(tag.getId())));


        } catch (Exception e) {
            groupViewHolder.id = "";
            groupViewHolder.type = ItemViewHolder.TYPE_GROUP;
            groupViewHolder.groupPos = groupPos;
            groupViewHolder.title.setText(App.i().getString(R.string.item_error));
//            KLog.e("父分类：" + theTag.getTitle() + "--" + theTag.getUnreadcount());
        }
        return convertView;
    }

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
        }

        try {
            Feed feed = tags.get(groupPos).getFeeds().get(childPos);
            childViewHolder.id = feed.getId();
            childViewHolder.type = ItemViewHolder.TYPE_CHILD;
            childViewHolder.groupPos = groupPos;
            childViewHolder.childPos = childPos;
            childViewHolder.title.setText(feed.getTitle());
//            childViewHolder.count.setText(String.valueOf(App.unreadCountMap.get(feed.getId())));
            childViewHolder.count.setText(String.valueOf(feed.getUnreadCount()));

            if (App.unreadCountMap.containsKey(feed.getId())) {
                childViewHolder.count.setText(String.valueOf(App.unreadCountMap.get(feed.getId())));
//                KLog.e("【getChildView】复用" + feed.getId() + " -- " + feed.getTitle());
            } else {
                int count = WithDB.i().getUnreadArtsCountByFeed(feed.getId());
                App.unreadCountMap.put(feed.getId(), count);
                childViewHolder.count.setText(String.valueOf(count));
//                KLog.e("【getChildView】初始" + feed.getId() + " -- " + feed.getTitle());
            }

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
