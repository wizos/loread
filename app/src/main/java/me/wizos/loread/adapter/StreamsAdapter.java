package me.wizos.loread.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hjq.toast.ToastUtils;
import com.yanzhenjie.recyclerview.ExpandableAdapter;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.R;
import me.wizos.loread.bean.collectiontree.Collection;
import me.wizos.loread.bean.collectiontree.CollectionFeed;
import me.wizos.loread.bean.collectiontree.CollectionTree;
import me.wizos.loread.utils.ColorfulUtils;
import me.wizos.loread.utils.StringUtils;

/**
 * Created by Wizos on 2019/4/17.
 */

public class StreamsAdapter extends ExpandableAdapter<RecyclerView.ViewHolder>{ // implements StickyCreator
    private LayoutInflater mInflater;
    private List<CollectionTree> categories;
    private Context context;
    private RequestOptions circleCropOptions;

    public StreamsAdapter(Context context) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        circleCropOptions = new RequestOptions().circleCrop();
    }

    public void setGroups(List<CollectionTree> parents) {
        if(categories == null){
            categories = new ArrayList<>();
            // categories.clear();
            categories.addAll(parents);
        }else {
            // categories.clear();
            // categories.addAll(parents);
            categories = parents;
        }
        // notifyDataSetChanged();
    }

    public CollectionTree getGroup(int groupPos){
        if(groupPos < categories.size()){
            return categories.get(groupPos);
        }else if(categories.size() > 0){
            return categories.get(0);
        }else {
            return null;
        }

    }

    public Collection getChild(int groupPos, int childPos) {
        return getChildren(groupPos).get(childPos);
    }

    private List<Collection> getChildren(int groupPos) {
        return categories.get(groupPos).getChildren();
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
        View view = mInflater.inflate(R.layout.main_bottom_sheet_category_group, root, false);
        return new ParentHolder(context, circleCropOptions, view);
    }

    @Override
    public RecyclerView.ViewHolder createChildHolder(@NonNull ViewGroup root, int viewType) {
        // XLog.d("createChildHolder");
        View view = mInflater.inflate(R.layout.main_bottom_sheet_category_child, root, false);
        return new ChildHolder(context, circleCropOptions, view);
    }

    @Override
    public void bindParentHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // XLog.d("bindParentHolder：" + position);
        ((ParentHolder) holder).bind(this, categories.get(position), position);
    }

    @Override
    public void bindChildHolder(@NonNull RecyclerView.ViewHolder holder, int parentPosition, int position) {
        // XLog.d("bindChildHolder" + position);
        ((ChildHolder) holder).bind((CollectionFeed)getChildren(parentPosition).get(position));
    }

    static class ParentHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView titleView;
        ImageView errorView;
        TextView countView;
        StreamsAdapter adapter;
        Context context;
        RequestOptions circleCropOptions;

        ParentHolder(Context context, RequestOptions options, @NonNull View itemView) {
            super(itemView);
            this.context = context;
            this.circleCropOptions = options;
            iconView = itemView.findViewById(R.id.group_item_icon);
            titleView = itemView.findViewById(R.id.group_item_title);
            errorView = itemView.findViewById(R.id.group_item_error);
            countView = itemView.findViewById(R.id.group_item_count);
        }

        public void bind(@NonNull StreamsAdapter mAdapter, @NonNull CollectionTree category, final int parentPosition) {
            adapter = mAdapter;
            if(category.getType() == CollectionTree.SMART){
                iconView.setImageResource(R.drawable.ic_bookmark);
                errorView.setVisibility(View.GONE);
                titleView.setTextColor(ColorfulUtils.getColor(context, R.attr.list_item_title_color));
            }else if(category.getType() == CollectionTree.CATEGORY){
                if (category.getChildren() == null || category.getChildren().size() == 0) {
                    iconView.setImageResource(R.drawable.ic_bookmark);
                }else if (adapter.isExpanded(parentPosition)){
                    iconView.setImageResource(R.drawable.ic_arrow_down);
                }else{
                    iconView.setImageResource(R.drawable.ic_arrow_right);
                }
                errorView.setVisibility(View.GONE);
                titleView.setTextColor(ColorfulUtils.getColor(context, R.attr.list_item_title_color));
            }else if(category.getType() == CollectionTree.FEED){
                Glide.with(itemView.getContext()).load(((CollectionFeed)category.getParent()).getIconUrl()).apply(circleCropOptions).into(iconView);

                if(((CollectionFeed)category.getParent()).getSyncInterval() == -1){
                    titleView.setAlpha(0.40f);
                    titleView.setTextColor(ColorfulUtils.getColor(context, R.attr.list_item_title_color));
                }else {
                    titleView.setAlpha(1f);
                    if(((CollectionFeed)category.getParent()).getLastErrorCount() > 0){
                        titleView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    }else {
                        titleView.setTextColor(ColorfulUtils.getColor(context, R.attr.list_item_title_color));
                    }
                }

                if(!StringUtils.isEmpty(((CollectionFeed)category.getParent()).getLastSyncError())){
                    errorView.setVisibility(View.VISIBLE);
                }else {
                    errorView.setVisibility(View.GONE);
                }
            }

            errorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtils.show(((CollectionFeed)category.getParent()).getLastSyncError());
                }
            });

            titleView.setText(category.getParent().getTitle());

            int count = category.getParent().getCount();
            if (count > 0) {
                countView.setText(String.valueOf(count));
                countView.setVisibility(View.VISIBLE);
            } else {
                countView.setVisibility(View.GONE);
            }


            iconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 判断parent是否打开了二级菜单
                    if (adapter.isExpanded(parentPosition)) {
                        // 关闭该parent下的二级菜单
                        adapter.collapseParent(parentPosition);
                        iconView.setImageResource(R.drawable.ic_arrow_right);
                    } else {
                        // 打开该parent下的二级菜单
                        adapter.expandParent(parentPosition);
                        iconView.setImageResource(R.drawable.ic_arrow_down);
                    }
                }
            });
        }
    }

    static class ChildHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView titleView;
        ImageView errorView;
        TextView countView;
        Context context;
        RequestOptions circleCropOptions;

        ChildHolder(Context context, RequestOptions options, @NonNull View itemView) {
            super(itemView);
            this.context = context;
            this.circleCropOptions = options;
            iconView = itemView.findViewById(R.id.child_item_icon);
            titleView = itemView.findViewById(R.id.child_item_title);
            errorView = itemView.findViewById(R.id.child_item_error);
            countView = itemView.findViewById(R.id.child_item_count);
        }

        public void bind(CollectionFeed feed) {
            Glide.with(itemView.getContext()).load(feed.getIconUrl()).apply(circleCropOptions).into(iconView);
            titleView.setText(feed.getTitle());
            if(feed.getSyncInterval() == -1){
                titleView.setAlpha(0.40f);
                titleView.setTextColor(ColorfulUtils.getColor(context, R.attr.list_item_desc_color));
            }else {
                titleView.setAlpha(1f);
                if(feed.getLastErrorCount() > 0){
                    titleView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                }else {
                    titleView.setTextColor(ColorfulUtils.getColor(context, R.attr.list_item_title_color));
                }
            }
            if(!StringUtils.isEmpty(feed.getLastSyncError())){
                errorView.setVisibility(View.VISIBLE);
            }else {
                errorView.setVisibility(View.GONE);
            }
            errorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtils.show(feed.getLastSyncError());
                }
            });
            int count = feed.getCount();
            countView.setText(String.valueOf(count));
            countView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }
}
