package me.wizos.loread.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.carlt.networklibs.NetType;
import com.carlt.networklibs.utils.NetworkUtils;

import org.jetbrains.annotations.NotNull;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.db.Feed;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.TimeUtils;

public class FeedPagedListAdapter extends PagedListAdapter<Feed, FeedPagedListAdapter.FeedViewHolder> {
    private RequestOptions canDownloadOptions;
    private RequestOptions cannotDownloadOptions;
    private Context context;
    private OnItemClickListener onClickListener;

    public FeedPagedListAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        canDownloadOptions = new RequestOptions()
                .circleCrop()
                .onlyRetrieveFromCache(false)
                .priority(Priority.NORMAL);
        cannotDownloadOptions = new RequestOptions()
                .circleCrop()
                .onlyRetrieveFromCache(true)
                .priority(Priority.NORMAL);
    }

    @NonNull
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        return new FeedViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_feed_manager_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Feed feed = super.getItem(position);
        // 如果article是null，在此处不停循环的获取getItem得到的还是null
        if (feed != null) {
            holder.bindTo(feed);
        } else {
            // Null defines a placeholder item - PagedListAdapter automatically invalidates this row when the actual object is loaded from the database.
            holder.placeholder();
        }
    }

    private static DiffUtil.ItemCallback<Feed> DIFF_CALLBACK = new DiffUtil.ItemCallback<Feed>() {
        @Override
        public boolean areItemsTheSame(Feed oldFeed, Feed newFeed) {
            return oldFeed.getId().equals(newFeed.getId());
        }

        @Override
        public boolean areContentsTheSame(@NotNull Feed oldFeed, @NotNull Feed newFeed) {
            return (oldFeed.getTitle() != null ? oldFeed.getTitle().equals(newFeed.getTitle()) : newFeed.getTitle()==null)
                    && (oldFeed.getFeedUrl() != null ? oldFeed.getFeedUrl().equals(newFeed.getFeedUrl()) : newFeed.getFeedUrl()==null)
                    && (oldFeed.getLastSyncError() != null ? oldFeed.getLastSyncError().equals(newFeed.getLastSyncError()) : newFeed.getLastSyncError()==null)
                    && oldFeed.getAllCount() == newFeed.getAllCount()
                    && oldFeed.getUnreadCount() == newFeed.getUnreadCount()
                    && oldFeed.getStarCount() == newFeed.getStarCount();
        }
    };


    class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;

        TextView titleView;
        TextView feedUrlView;

        TextView errorView;
        TextView countView;
        TextView pubDateView;


        FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(R.id.feed_manager_item_icon);
            titleView = (TextView) itemView.findViewById(R.id.feed_manager_item_title);
            feedUrlView = (TextView) itemView.findViewById(R.id.feed_manager_item_feed_url);
            errorView = (TextView) itemView.findViewById(R.id.feed_manager_item_error);
            countView = (TextView) itemView.findViewById(R.id.feed_manager_item_count);
            pubDateView = (TextView) itemView.findViewById(R.id.feed_manager_item_pubDate);
        }
        void placeholder(){
            iconView.setImageDrawable(null);

            titleView.setText(App.i().getString(R.string.loading));
            titleView.setAlpha(0.40f);

            feedUrlView.setText("");
            errorView.setText("");
            countView.setText("");
            pubDateView.setText("");
            itemView.setOnClickListener(null);
        }


        @SuppressLint("SetTextI18n")
        void bindTo(Feed feed){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onClickListener!=null){
                        onClickListener.onClick(v, feed);
                    }
                }
            });

            if (!TextUtils.isEmpty(feed.getIconUrl())) {
                if ( NetworkUtils.isAvailable() && (!App.i().getUser().isDownloadImgOnlyWifi() || NetworkUtils.getNetType().equals(NetType.WIFI)) ) {
                    Glide.with(context).load(feed.getIconUrl()).apply(canDownloadOptions).into(iconView);
                } else {
                    Glide.with(context).load(feed.getIconUrl()).apply(cannotDownloadOptions).into(iconView);
                }
            } else {
                iconView.setImageDrawable(null);
            }


            if (StringUtils.isEmpty(feed.getTitle())) {
                titleView.setText(App.i().getString(R.string.no_title));
            } else {
                titleView.setText(feed.getTitle());
            }
            if (feed.getSyncInterval() == -1 ) {
                titleView.setAlpha(0.40f);
            } else {
                titleView.setAlpha(1f);
            }

            feedUrlView.setText(feed.getFeedUrl());

            if (StringUtils.isEmpty(feed.getLastSyncError())) {
                errorView.setVisibility(View.GONE);
            } else {
                errorView.setVisibility(View.VISIBLE);
                errorView.setText(feed.getLastSyncError());
            }

            countView.setText(feed.getAllCount() + " / " + feed.getUnreadCount() + " / " + feed.getStarCount());

            pubDateView.setText(TimeUtils.readability(feed.getLastPubDate()));
        }
    }

    public void setOnClickListener(OnItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
    public interface OnItemClickListener{
        void onClick(View view, Feed feed);
    }
}
