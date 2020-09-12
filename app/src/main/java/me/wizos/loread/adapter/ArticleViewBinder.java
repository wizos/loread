//package me.wizos.loread.adapter;
//
//import android.content.Context;
//import android.text.Html;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.Priority;
//import com.bumptech.glide.load.model.GlideUrl;
//import com.bumptech.glide.load.model.LazyHeaders;
//import com.bumptech.glide.request.RequestOptions;
//import com.carlt.networklibs.NetType;
//import com.carlt.networklibs.utils.NetworkUtils;
//import com.drakeet.multitype.ItemViewBinder;
//
//import me.wizos.loread.App;
//import me.wizos.loread.R;
//import me.wizos.loread.db.Article;
//import me.wizos.loread.db.CoreDB;
//import me.wizos.loread.db.Feed;
//import me.wizos.loread.utils.TimeUtil;
//import me.wizos.loread.view.IconFontView;
//
///**
// * Created by Wizos on 2019/4/14.
// */
//
//public class ArticleViewBinder extends ItemViewBinder<Article, ArticleViewBinder.ArticleViewHolder> {
//    private RequestOptions canDownloadOptions;
//    private RequestOptions cannotDownloadOptions;
//    private GlideUrl gliderUrl;
//    private Context context;
//
//    @NonNull
//    @Override
//    public ArticleViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
//        context = parent.getContext();
//        canDownloadOptions = new RequestOptions()
//                .centerCrop()
//                .onlyRetrieveFromCache(false)
//                .priority(Priority.HIGH);
//        cannotDownloadOptions = new RequestOptions()
//                .centerCrop()
//                .onlyRetrieveFromCache(true)
//                .priority(Priority.HIGH);
//        return new ArticleViewHolder(inflater.inflate(R.layout.activity_main_list_item, parent, false));
//    }
//
//
//    @Override
//    public void onBindViewHolder(@NonNull ArticleViewHolder holder, @NonNull Article article) {
//        if (TextUtils.isEmpty(article.getTitle())) {
//            holder.articleTitle.setText(App.i().getString(R.string.no_title));
//        } else {
//            holder.articleTitle.setText(article.getTitle());
//        }
//
//        if (TextUtils.isEmpty(article.getSummary()) || article.getSummary().length() == 0) {
//            holder.articleSummary.setVisibility(View.GONE);
//        } else {
//            holder.articleSummary.setVisibility(View.VISIBLE);
//            holder.articleSummary.setText(article.getSummary());
//        }
//
//        if (!TextUtils.isEmpty(article.getImage())) {
//            holder.articleImg.setVisibility(View.VISIBLE);
//
//
//            if ( NetworkUtils.isAvailable() && (!App.i().getUser().isDownloadImgOnlyWifi() || NetworkUtils.getNetType().equals(NetType.WIFI)) ) {
//                // KLog.e( "数据：" + article.getTitle() + "   "  +  App.Referer+ "   "  +  article.getLink() );
//                if (!TextUtils.isEmpty(article.getLink())) {
//                    gliderUrl = new GlideUrl(article.getImage(), new LazyHeaders.Builder().addHeader(App.Referer, article.getLink()).build());
//                    Glide.with(context).load(gliderUrl).apply(canDownloadOptions).into(holder.articleImg);
//                } else {
//                    Glide.with(context).load(article.getImage()).apply(canDownloadOptions).into(holder.articleImg);
//                }
//            } else {
//                Glide.with(context).load(article.getImage()).apply(cannotDownloadOptions).into(holder.articleImg);
//            }
//        } else {
//            holder.articleImg.setVisibility(View.GONE);
//        }
//
//        Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), article.getFeedId());
//        if (feed != null && !TextUtils.isEmpty(feed.getTitle())) {
//            holder.articleFeed.setText(Html.fromHtml(feed.getTitle()));
//        } else {
//            holder.articleFeed.setText(article.getFeedTitle());
//        }
//
//        holder.articlePublished.setText(TimeUtil.format(article.getPubDate(), "yyyy-MM-dd HH:mm"));
//
//        if (article.getReadStatus() == App.STATUS_READED) {
//            holder.articleTitle.setAlpha(0.40f);
//        } else {
//            holder.articleTitle.setAlpha(1f);
//        }
//
//        if (App.STATUS_NOT_FILED == article.getSaveStatus()) {
//            holder.articleSave.setVisibility(View.GONE);
//        } else {
//            holder.articleSave.setVisibility(View.VISIBLE);
//        }
//
//        if (article.getReadStatus() == App.STATUS_UNREADING) {
//            holder.articleReading.setVisibility(View.VISIBLE);
//        } else if (article.getReadStatus() == App.STATUS_UNREAD) {
//            holder.articleReading.setVisibility(View.GONE);
//        } else {
//            holder.articleReading.setVisibility(View.GONE);
//        }
//        if (article.getStarStatus() == App.STATUS_STARED) {
//            holder.articleStar.setVisibility(View.VISIBLE);
//        } else {
//            holder.articleStar.setVisibility(View.GONE);
//        }
//
//    }
//
//    static class ArticleViewHolder extends RecyclerView.ViewHolder {
//        @NonNull
//        TextView articleTitle;
//        TextView articleSummary;
//        TextView articleFeed;
//        TextView articlePublished;
//        IconFontView articleStar;
//        IconFontView articleReading;
//        IconFontView articleSave;
//        ImageView articleImg;
////        IconFontView markLeft;
////        IconFontView markRight;
////        SwipeDragLayout swipeDragLayout;
//
//        ArticleViewHolder(@NonNull View itemView) {
//            super(itemView);
//            articleTitle = (TextView) itemView.findViewById(R.id.main_slv_item_title);
//            articleSummary = (TextView) itemView.findViewById(R.id.main_slv_item_summary);
//            articleFeed = (TextView) itemView.findViewById(R.id.main_slv_item_author);
//            articleImg = (ImageView) itemView.findViewById(R.id.main_slv_item_img);
//            // articleImg.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.placeholder_bg));
//            articlePublished = (TextView) itemView.findViewById(R.id.main_slv_item_time);
//            articleStar = (IconFontView) itemView.findViewById(R.id.main_slv_item_icon_star);
//            articleReading = (IconFontView) itemView.findViewById(R.id.main_slv_item_icon_reading);
//            articleSave = (IconFontView) itemView.findViewById(R.id.main_slv_item_icon_save);
////            markLeft = itemView.findViewById(R.id.main_list_item_menu_left);
////            markRight = itemView.findViewById(R.id.main_list_item_menu_right);
////            swipeDragLayout = itemView.findViewById(R.id.swipe_layout);
//        }
//    }
//
//}
