package me.wizos.loread.adapter;

import android.content.Context;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvishew.xlog.XLog;
import com.yanzhenjie.recyclerview.ExpandableAdapter;

import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.db.Collection;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.view.IconFontView;

/**
 * Created by Wizos on 2019/4/17.
 */

public class ExpandedAdapter extends ExpandableAdapter<RecyclerView.ViewHolder> {
    private LayoutInflater mInflater;
    private List<Collection> categories;
    //private int countMode = App.STATUS_ALL; // 0为所有， 1为unread， 2为star
    private ArrayMap<String, List<Collection>> feedsMap = new ArrayMap<>();

    public ExpandedAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void setParents(List<Collection> parents) {
        this.categories = parents;
    }
    public List<Collection> getParents() {
        return categories;
    }


    public void notifyDataChanged() {
        //KLog.e("获得notifyDataSetChanged");
        feedsMap = new ArrayMap<>();
        super.notifyDataSetChanged();
    }


    public Collection getGroup(int groupPos){
        return categories.get(groupPos);
    }

    public Collection getChild(int groupPos, int childPos) {
        return getChildren(groupPos).get(childPos);
    }

    private List<Collection> getChildren(int groupPos) {
        //KLog.e("getFeeds");
        if (null == feedsMap.get(categories.get(groupPos).getId())) {
            long time = System.currentTimeMillis();
            List<Collection> feedWraps;

            if( App.i().getUser().getStreamStatus() == App.STATUS_UNREAD ){
                if(categories.get(groupPos).getId().contains(App.CATEGORY_UNCATEGORIZED)){
                    feedWraps = CoreDB.i().feedDao().getFeedsUnreadCountByUnCategory(App.i().getUser().getId());
                }else {
                    feedWraps = CoreDB.i().feedDao().getFeedsUnreadCountByCategoryId(App.i().getUser().getId(), categories.get(groupPos).getId());
                }
            }else if( App.i().getUser().getStreamStatus() == App.STATUS_STARED ){
                if(categories.get(groupPos).getId().contains(App.CATEGORY_UNCATEGORIZED)){
                    feedWraps = CoreDB.i().feedDao().getFeedsStarCountByUnCategory(App.i().getUser().getId());
                }else {
                    feedWraps = CoreDB.i().feedDao().getFeedsStarCountByCategoryId(App.i().getUser().getId(), categories.get(groupPos).getId());
                }
            }else {
                if(categories.get(groupPos).getId().contains(App.CATEGORY_UNCATEGORIZED)){
                    feedWraps = CoreDB.i().feedDao().getFeedsAllCountByUnCategory(App.i().getUser().getId());
                }else {
                    feedWraps = CoreDB.i().feedDao().getFeedsAllCountByCategoryId(App.i().getUser().getId(), categories.get(groupPos).getId());
                }
            }

            long d = System.currentTimeMillis() - time;
            XLog.d("返回feedList，耗时：" + d + " = " + App.i().getUser().getId()  + " = " + categories.get(groupPos).getId() + " , " + ( feedWraps==null ? 0:feedWraps.size()));
            feedsMap.put(categories.get(groupPos).getId(), feedWraps);

            return feedWraps;
        }
        return feedsMap.get(categories.get(groupPos).getId());
    }

    @Override
    public int parentItemCount() {
//        KLog.e("parentItemCount");
        return categories == null ? 0 : categories.size();
    }

    @Override
    public int childItemCount(int parentPosition) {
        //KLog.e("childItemCount");
        List<Collection> memberList = getChildren(parentPosition);
        return memberList == null ? 0 : memberList.size();
    }

    @Override
    public RecyclerView.ViewHolder createParentHolder(@NonNull ViewGroup root, int viewType) {
//        KLog.e("createParentHolder");
        View view = mInflater.inflate(R.layout.tag_expandable_item_group, root, false);
        return new ParentHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder createChildHolder(@NonNull ViewGroup root, int viewType) {
        //KLog.e("createChildHolder");
        View view = mInflater.inflate(R.layout.tag_expandable_item_child, root, false);
        return new ChildHolder(view);
    }

    @Override
    public void bindParentHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        KLog.e("bindParentHolder");
        ((ParentHolder) holder).setData(this, categories.get(position), position);
    }

    @Override
    public void bindChildHolder(@NonNull RecyclerView.ViewHolder holder, int parentPosition, int position) {
        //KLog.e("bindChildHolder");
        ((ChildHolder) holder).setData(getChildren(parentPosition).get(position));
    }

    static class ParentHolder extends RecyclerView.ViewHolder {
        Context context;
        IconFontView icon;
        TextView title;
        TextView countView;
        ExpandedAdapter adapter;

        ParentHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.group_item_icon);
            title = itemView.findViewById(R.id.group_item_title);
            countView = itemView.findViewById(R.id.group_item_count);
            context = itemView.getContext();
        }

        public void setData(@NonNull ExpandedAdapter mAdapter, @NonNull Collection category, final int parentPosition) {
            adapter = mAdapter;
            if(category.getId().contains(App.CATEGORY_UNCATEGORIZED)){
                if(CoreDB.i().feedDao().getFeedsCountByUnCategory(App.i().getUser().getId()) == 0){
                    icon.setText(context.getString(R.string.font_tag));
                }else if (adapter.isExpanded(parentPosition)) {
                    icon.setText(context.getString(R.string.font_arrow_down));
                } else {
                    icon.setText(context.getString(R.string.font_arrow_right));
                }
            } else if (CoreDB.i().feedCategoryDao().getCountByCategoryId(App.i().getUser().getId(), category.getId()) == 0) {
                icon.setText(context.getString(R.string.font_tag));
            } else if (adapter.isExpanded(parentPosition)) {
                icon.setText(context.getString(R.string.font_arrow_down));
            } else {
                icon.setText(context.getString(R.string.font_arrow_right));
            }


            title.setText(category.getTitle());

            int count = category.getCount();
            if (count > 0) {
                countView.setText(String.valueOf(count));
                countView.setVisibility(View.VISIBLE);
            } else {
                countView.setVisibility(View.INVISIBLE);
            }


            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long time = System.currentTimeMillis();
                    // 判断parent是否打开了二级菜单
                    if (adapter.isExpanded(parentPosition)) {
                        // 关闭该parent下的二级菜单
                        adapter.collapseParent(parentPosition);
                        icon.setText(context.getString(R.string.font_arrow_right));
                    } else {
                        // 打开该parent下的二级菜单
                        adapter.expandParent(parentPosition);
                        icon.setText(context.getString(R.string.font_arrow_down));
                    }
                    // KLog.e("点击展开收缩：" + (System.currentTimeMillis() - time) );
                }
            });
        }
    }

    static class ChildHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView countView;

        ChildHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.child_item_title);
            countView = itemView.findViewById(R.id.child_item_count);
        }

        public void setData(Collection feed) {
            //feed.refresh();
            title.setText(feed.getTitle());
            int count = feed.getCount();
            countView.setText(String.valueOf(count));
            countView.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
