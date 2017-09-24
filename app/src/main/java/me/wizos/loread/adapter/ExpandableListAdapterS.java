package me.wizos.loread.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.R;
import me.wizos.loread.bean.Feed;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.view.ExpandableListViewS;
import me.wizos.loread.view.IconFontView;

/**
 * Created by Wizos on 2017/9/17.
 */

public class ExpandableListAdapterS extends BaseExpandableListAdapter { // implements ExpandableListViewS.HeaderAdapter
    Context context;
    private List<Tag> tags = new ArrayList<>();
//    private ExpandableListViewS listView;

    public ExpandableListAdapterS(Context context, List<Tag> tags) {
        this.context = context;
        this.tags = tags;
//        this.listView = listView;
    }

    //  获得某个父项的某个子项
    @Override
    public Object getChild(int parentPos, int childPos) {
        try {
            KLog.i("获取子项：" + tags.get(parentPos).getFeeds().get(childPos));
            return tags.get(parentPos).getFeeds().get(childPos);
        } catch (RuntimeException e) {
            KLog.i("获取子项：" + WithDB.i().getFeeds().get(childPos));
            return 0;
        }
    }

    //  获得父项的数量
    @Override
    public int getGroupCount() {
//        KLog.e("父项的数量：" + tags.size());
        return tags.size();
    }

    //  获得某个父项的子项数目
    @Override
    public int getChildrenCount(int parentPos) {
        KLog.e("getChildrenCount：" + parentPos);
        try {
            KLog.e("子项的数量：" + tags.get(parentPos).getFeeds().size());
            return tags.get(parentPos).getFeeds().size();
        } catch (RuntimeException e) {
            KLog.e("子项的数量：" + WithDB.i().getFeeds().size());
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

    //  获得父项显示的view
    private ItemViewHolder groupViewHolder;

    @Override
    public View getGroupView(final int groupPos, final boolean isExpanded, View convertView, final ViewGroup parent) {
        // 使用一个 ViewHolder，可减少在该函数中每次都要去 findViewById ，这很费时间。具体见：https://zhidao.baidu.com/question/544207312.html
        if (convertView == null) {
            groupViewHolder = new ItemViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.main_expandable_item_group, null);
            groupViewHolder.icon = (IconFontView) convertView.findViewById(R.id.group_item_icon);
            groupViewHolder.title = (TextView) convertView.findViewById(R.id.group_item_title);
            groupViewHolder.count = (TextView) convertView.findViewById(R.id.group_item_count);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (ItemViewHolder) convertView.getTag();
        }

        groupViewHolder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KLog.e("箭头被点击");
                if (isExpanded) {
                    ((ExpandableListViewS) parent).collapseGroup(groupPos);
                    ((IconFontView) v).setText(context.getString(R.string.font_collapse));
                } else {
                    ((ExpandableListViewS) parent).expandGroup(groupPos);
                    ((IconFontView) v).setText(context.getString(R.string.font_expand));
                }
            }
        });

        Tag theTag = tags.get(groupPos);
        if (theTag != null) {
            try {
                groupViewHolder.title.setText(theTag.getTitle());
                groupViewHolder.count.setText(theTag.getUnreadcount());
            } catch (Exception e) {

            }
//            KLog.e("父分类：" + theTag.getTitle() + "--" + theTag.getUnreadcount());
        }
        return convertView;
    }


    private class ItemViewHolder {
        private IconFontView icon;
        private TextView title;
        private TextView count;
    }


    //  获得子项显示的view
    @Override
    public View getChildView(int parentPos, int childPos, boolean isExpanded, View convertView, final ViewGroup parent) {
        ItemViewHolder childViewHolder;
        if (convertView == null) {
            childViewHolder = new ItemViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.main_expandable_item_child, null);
//            childViewHolder.icon = (IconFontView) convertView.findViewById(R.id.group_item_icon);
            childViewHolder.title = (TextView) convertView.findViewById(R.id.child_item_title);
            childViewHolder.count = (TextView) convertView.findViewById(R.id.child_item_count);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ItemViewHolder) convertView.getTag();
        }

//        KLog.e("子分类：" + theFeed.getTitle() + "--");

        Feed feed;
        try {
            feed = tags.get(parentPos).getFeeds().get(childPos);
//            KLog.i("getChildView1：" + feed.getTitle());
        } catch (RuntimeException e) {
            feed = WithDB.i().getFeeds().get(childPos);
            KLog.i("getChildView2：" + feed.getTitle());
        }

        String feedTitle = feed.getTitle();
        String feedCount = String.valueOf(0); // TEST:  待补充

//        childViewHolder.icon.setText("");
        childViewHolder.title.setText(feedTitle);
        childViewHolder.count.setText(feedCount);

//        childViewTitle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                KLog.d("子项被点击：");
//                final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(context);
//                adapter.add(new MaterialSimpleListItem.Builder(context)
//                        .content("编辑")
////                        .icon(R.drawable.about)
//                        .backgroundColor(Color.WHITE)
//                        .build());
//                new MaterialDialog.Builder(context)
//                        .adapter(adapter, new MaterialDialog.ListCallback() {
//                            @Override
//                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
//                                KLog.d("按的是= " + theFeed.getId() + " = " + text);
////                                dialog.dismiss();
////                                Intent intent = new Intent(context,NoteActivity.class);
////                                intent.putExtra("memoId",theFeed.getId() );
////                                context.startActivity( intent );
//                            }
//                        })
//                        .show();
//            }
//        });

        return convertView;
    }

    //  子项是否可选中，如果需要设置子项的点击事件，需要返回true
    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }


//    private class header implements ExpandableListViewS.HeaderAdapter {

//        @Override
//        public int getHeaderState(int groupPosition, int childPosition) {
//            final int childCount = getChildrenCount(groupPosition);
//            KLog.e("getHeaderState列表的子数量：" + listView.getChildCount() );
//            KLog.e("getHeaderState获取头部的状态：" + groupPosition + ":" + childPosition  + "=" + childCount + ":" + listView.isGroupExpanded(groupPosition));
//            if (childPosition == childCount - 1) {
//                return PINNED_HEADER_PUSHED_UP;
//            } else if (childPosition == -1 && !listView.isGroupExpanded(groupPosition)) { // 如果某项是父项，并且没有展开
//                return PINNED_HEADER_GONE;
//            } else {
//                return PINNED_HEADER_VISIBLE;
//            }
//        }
//
//
//        @Override
//        public void configureHeader(View header, int groupPosition, int childPosition, int alpha) {
//            String groupTitle = tags.get(groupPosition).getTitle();
//            ((TextView) header.findViewById(R.id.header_item_title)).setText(groupTitle);
//        }
//
//
//        private SparseIntArray groupStatusMap = new SparseIntArray();
//
//        @Override
//        public void setGroupClickStatus(int groupPosition, int status) {
//            groupStatusMap.put(groupPosition, status);
//        }
//
//        @Override
//        public int getGroupClickStatus(int groupPosition) {
//            if (groupStatusMap.keyAt(groupPosition) >= 0) {
//                return groupStatusMap.get(groupPosition);
//            } else {
//                return 0;
//            }
//        }

//    }


}
