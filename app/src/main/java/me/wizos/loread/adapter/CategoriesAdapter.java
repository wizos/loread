package me.wizos.loread.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yanzhenjie.recyclerview.ExpandableAdapter;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.R;
import me.wizos.loread.bean.CategoryFeeds;
import me.wizos.loread.db.Collection;
import me.wizos.loread.view.IconFontView;

/**
 * Created by Wizos on 2019/4/17.
 */

public class CategoriesAdapter extends ExpandableAdapter<RecyclerView.ViewHolder>{ // implements StickyCreator
    private LayoutInflater mInflater;
    private List<CategoryFeeds> categories;

    public CategoriesAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void setGroups(List<CategoryFeeds> parents) {
        if(categories == null){
            categories = new ArrayList<>();
            categories.clear();
            categories.addAll(parents);
        }else {
            this.categories = parents;
        }
    }

    public CategoryFeeds getGroup(int groupPos){
        return categories.get(groupPos);
    }

    public Collection getChild(int groupPos, int childPos) {
        return getChildren(groupPos).get(childPos);
    }

    private List<Collection> getChildren(int groupPos) {
        return categories.get(groupPos).getFeeds();
    }

    public void notifyDataChanged() {
        super.notifyDataSetChanged();
    }

    // @Override
    // public int getGroupCount() {
    //     return parentItemCount();
    // }

    // @Override
    // public int getGroupPosition(int adapterPosition) {
    //     return parentItemPosition(adapterPosition);
    // }
    //
    // @Override
    // public int getChildPosition(int adapterPosition) {
    //     return childItemPosition(adapterPosition);
    // }
    //
    // @Override
    // public boolean isGroup(int adapterPosition) {
    //     return isParentItem(adapterPosition);
    // }

    @Override
    public int parentItemCount() {
        // XLog.i("父的数量：" + (categories == null ? 0 : categories.size()) );
        return categories == null ? 0 : categories.size();
    }

    @Override
    public int childItemCount(int parentPosition) {
        List<Collection> children = getChildren(parentPosition);
        // XLog.i("子的数量：" + (children == null ? 0 : children.size()) );
        return children == null ? 0 : children.size();
    }


    // @Override
    // public int getStickyHeaderState(int firstVisibleGroupPosition, int firstVisibleChildPosition) {
    //     return 0;
    // }
    //
    // @Override
    // public void onBindStickyHeader(View header, int groupPosition, int childPosition, int alpha) {
    //     // CategoryFeeds category = categories.get(groupPosition);
    //     //
    //     // if(category.getCategoryId().contains(App.CATEGORY_UNCATEGORIZED)){
    //     //     if(CoreDB.i().feedDao().getFeedsCountByUnCategory(App.i().getUser().getId()) == 0){
    //     //         ((TextView)header.findViewById(R.id.group_item_icon)).setText(context.getString(R.string.font_tag));
    //     //     }else if (category.isExpand) {
    //     //         ((TextView)header.findViewById(R.id.group_item_icon)).setText(context.getString(R.string.font_arrow_down));
    //     //     } else {
    //     //         ((TextView)header.findViewById(R.id.group_item_icon)).setText(context.getString(R.string.font_arrow_right));
    //     //     }
    //     // } else if (CoreDB.i().feedCategoryDao().getCountByCategoryId(App.i().getUser().getId(), category.getCategoryId()) == 0) {
    //     //     ((TextView)header.findViewById(R.id.group_item_icon)).setText(context.getString(R.string.font_tag));
    //     // } else if (category.isExpand) {
    //     //     ((TextView)header.findViewById(R.id.group_item_icon)).setText(context.getString(R.string.font_arrow_down));
    //     // } else {
    //     //     ((TextView)header.findViewById(R.id.group_item_icon)).setText(context.getString(R.string.font_arrow_right));
    //     // }
    //     //
    //     //
    //     // ((TextView)header.findViewById(R.id.group_item_title)).setText(category.getCategoryName());
    //     //
    //     // int count = category.getCount();
    //     // if (count > 0) {
    //     //     ((TextView)header.findViewById(R.id.group_item_count)).setText(String.valueOf(count));
    //     //     ((TextView)header.findViewById(R.id.group_item_count)).setVisibility(View.VISIBLE);
    //     // } else {
    //     //     ((TextView)header.findViewById(R.id.group_item_count)).setVisibility(View.INVISIBLE);
    //     // }
    //     //
    //     // ((TextView)header.findViewById(R.id.group_item_icon)).setOnClickListener(new View.OnClickListener() {
    //     //     @Override
    //     //     public void onClick(View v) {
    //     //         long time = System.currentTimeMillis();
    //     //         // 判断parent是否打开了二级菜单
    //     //         if (isExpanded(groupPosition)) {
    //     //             // 关闭该parent下的二级菜单
    //     //             collapseParent(groupPosition);
    //     //             ((TextView)v).setText(context.getString(R.string.font_arrow_right));
    //     //         } else {
    //     //             // 打开该parent下的二级菜单
    //     //             expandParent(groupPosition);
    //     //             ((TextView)v).setText(context.getString(R.string.font_arrow_down));
    //     //         }
    //     //         // KLog.e("点击展开收缩：" + (System.currentTimeMillis() - time) );
    //     //     }
    //     // });
    //
    //     // header.setOnLongClickListener(new View.OnLongClickListener() {
    //     //     @Override
    //     //     public boolean onLongClick(View v) {
    //     //         if (groupClickListener != null) {
    //     //             groupClickListener.onLongClick(v, groupPosition);
    //     //         }
    //     //         return false;
    //     //     }
    //     // });
    // }

    @Override
    public RecyclerView.ViewHolder createParentHolder(@NonNull ViewGroup root, int viewType) {
        // XLog.d("createParentHolder");
        View view = mInflater.inflate(R.layout.tag_expandable_item_group, root, false);
        return new ParentHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder createChildHolder(@NonNull ViewGroup root, int viewType) {
        // XLog.d("createChildHolder");
        View view = mInflater.inflate(R.layout.tag_expandable_item_child, root, false);
        return new ChildHolder(view);
    }

    @Override
    public void bindParentHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // XLog.d("bindParentHolder");
        ((ParentHolder) holder).bind(this, categories.get(position), position);
    }

    @Override
    public void bindChildHolder(@NonNull RecyclerView.ViewHolder holder, int parentPosition, int position) {
        // XLog.d("bindChildHolder");
        ((ChildHolder) holder).bind(getChildren(parentPosition).get(position));
    }

    static class ParentHolder extends RecyclerView.ViewHolder {
        Context context;
        IconFontView icon;
        TextView title;
        TextView countView;
        CategoriesAdapter adapter;

        ParentHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.group_item_icon);
            title = itemView.findViewById(R.id.group_item_title);
            countView = itemView.findViewById(R.id.group_item_count);
            context = itemView.getContext();
        }

        public void bind(@NonNull CategoriesAdapter mAdapter, @NonNull CategoryFeeds category, final int parentPosition) {
            adapter = mAdapter;
            if (category.getFeeds() == null || category.getFeeds().size() == 0) {
                icon.setText(context.getString(R.string.font_tag));
            } else if (adapter.isExpanded(parentPosition)) {
                icon.setText(context.getString(R.string.font_arrow_down));
            } else {
                icon.setText(context.getString(R.string.font_arrow_right));
            }


            title.setText(category.getCategoryName());

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

        public void bind(Collection feed) {
            title.setText(feed.getTitle());
            int count = feed.getCount();
            countView.setText(String.valueOf(count));
            countView.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
