package me.wizos.loread.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.carlt.networklibs.NetType;
import com.carlt.networklibs.utils.NetworkUtils;
import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.utils.TimeUtil;
import me.wizos.loread.view.IconFontView;

public class ArticlePagedListAdapter extends PagedListAdapter<Article, ArticlePagedListAdapter.ArticleViewHolder> {
    private RequestOptions canDownloadOptions;
    private RequestOptions cannotDownloadOptions;
    private Context context;

    public ArticlePagedListAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        context = parent.getContext();
        canDownloadOptions = new RequestOptions()
                .centerCrop()
                .onlyRetrieveFromCache(false)
                .priority(Priority.HIGH);
        cannotDownloadOptions = new RequestOptions()
                .centerCrop()
                .onlyRetrieveFromCache(true)
                .priority(Priority.HIGH);
        return new ArticleViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_main_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        KLog.e("创建onBindViewHolder");
        Article article = getItem(position);
        // 如果article是null，在此处不停循环的获取getItem得到的还是null
        if (article != null) {
            holder.bindTo(article);
        } else {
            // Null defines a placeholder item - PagedListAdapter automatically invalidates this row when the actual object is loaded from the database.
            holder.placeholder();
        }
    }

    private static DiffUtil.ItemCallback<Article> DIFF_CALLBACK = new DiffUtil.ItemCallback<Article>() {
        @Override
        public boolean areItemsTheSame(Article oldArticle, Article newArticle) {
            return oldArticle.getId().equals(newArticle.getId());
        }

        @Override
        public boolean areContentsTheSame(Article oldArticle, Article newArticle) {
            return oldArticle.getReadStatus() == newArticle.getReadStatus()
                    && oldArticle.getStarStatus() == newArticle.getStarStatus()
                    && oldArticle.getSaveStatus() == newArticle.getSaveStatus()
                    && oldArticle.getTitle().equals(newArticle.getTitle())
                    && (oldArticle.getImage() != null && oldArticle.getImage().equals(newArticle.getImage()) )
                    && oldArticle.getSummary().equals(newArticle.getSummary());
        }
    };


    class ArticleViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        TextView articleTitle;
        TextView articleSummary;
        TextView articleFeed;
        TextView articlePublished;
        IconFontView articleStar;
        IconFontView articleReading;
        IconFontView articleSave;
        ImageView articleImg;

        ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            articleTitle = (TextView) itemView.findViewById(R.id.main_slv_item_title);
            articleSummary = (TextView) itemView.findViewById(R.id.main_slv_item_summary);
            articleFeed = (TextView) itemView.findViewById(R.id.main_slv_item_author);
            articleImg = (ImageView) itemView.findViewById(R.id.main_slv_item_img);
            articlePublished = (TextView) itemView.findViewById(R.id.main_slv_item_time);
            articleStar = (IconFontView) itemView.findViewById(R.id.main_slv_item_icon_star);
            articleReading = (IconFontView) itemView.findViewById(R.id.main_slv_item_icon_reading);
            articleSave = (IconFontView) itemView.findViewById(R.id.main_slv_item_icon_save);
        }
        void placeholder(){
            articleTitle.setText(App.i().getString(R.string.loading));
            articleTitle.setAlpha(0.40f);
            articleImg.setVisibility(View.GONE);

            articleSummary.setText("");
            //articleSummary.setVisibility(View.GONE);
            //articleFeed.setVisibility(View.GONE);
            //articleFeed.setText(App.i().getString(R.string.loading));
            //articlePublished.setText("");
            articleSave.setVisibility(View.GONE);
            articleReading.setVisibility(View.GONE);
            articleStar.setVisibility(View.GONE);
        }


        void bindTo(Article article){
            if (TextUtils.isEmpty(article.getTitle())) {
                articleTitle.setText(App.i().getString(R.string.no_title));
            } else {
                articleTitle.setText(article.getTitle());
            }
            if (article.getReadStatus() == App.STATUS_READED) {
                articleTitle.setAlpha(0.40f);
            } else {
                articleTitle.setAlpha(1f);
            }

            if (TextUtils.isEmpty(article.getSummary()) || article.getSummary().length() == 0) {
                articleSummary.setVisibility(View.GONE);
            } else {
                articleSummary.setVisibility(View.VISIBLE);
                articleSummary.setText(article.getSummary());
            }

            if (!TextUtils.isEmpty(article.getImage())) {
                articleImg.setVisibility(View.VISIBLE);

                if ( NetworkUtils.isAvailable() && (!App.i().getUser().isDownloadImgOnlyWifi() || NetworkUtils.getNetType().equals(NetType.WIFI)) ) {
                    // KLog.e( "数据：" + article.getTitle() + "   "  +  App.Referer+ "   "  +  article.getLink() );
                    if (!TextUtils.isEmpty(article.getLink())) {
                        GlideUrl gliderUrl = new GlideUrl(article.getImage(), new LazyHeaders.Builder().addHeader(App.Referer, article.getLink()).build());
                        Glide.with(context).load(gliderUrl).apply(canDownloadOptions).into(articleImg);
                    } else {
                        Glide.with(context).load(article.getImage()).apply(canDownloadOptions).into(articleImg);
                    }
                } else {
                    Glide.with(context).load(article.getImage()).apply(cannotDownloadOptions).into(articleImg);
                }
            } else {
                articleImg.setVisibility(View.GONE);
            }

            Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(),article.getFeedId());
            if (feed != null && !TextUtils.isEmpty(feed.getTitle())) {
                articleFeed.setText(Html.fromHtml(feed.getTitle()));
            } else {
                articleFeed.setText(article.getFeedTitle());
            }

            articlePublished.setText(TimeUtil.format(article.getPubDate(), "yyyy-MM-dd HH:mm"));

            if (App.STATUS_NOT_FILED == article.getSaveStatus()) {
                articleSave.setVisibility(View.GONE);
            } else {
                articleSave.setVisibility(View.VISIBLE);
            }

            if (article.getReadStatus() == App.STATUS_UNREADING) {
                articleReading.setVisibility(View.VISIBLE);
            } else {
                articleReading.setVisibility(View.GONE);
            }
            if (article.getStarStatus() == App.STATUS_STARED) {
                articleStar.setVisibility(View.VISIBLE);
            } else {
                articleStar.setVisibility(View.GONE);
            }
        }
    }

    public Article get(int position){
        return getItem(position);
        //String articleId = articleMap.get(position);
        //KLog.e("Get：" + position + " , " + articleId);
        //if(StringUtils.isEmpty(articleId)){
        //    int lastPosition = 0;
        //    if( getCurrentList()!=null ){
        //        lastPosition = (int)getCurrentList().getLastKey();
        //    }
        //    getItem(position);
        //    getItem(lastPosition);
        //    articleId = articleMap.get(position);
        //}
        //KLog.e("Get：-----------------------");
        //if(StringUtils.isEmpty(articleId)){
        //    return null;
        //}
        //return CoreDB.i().articleDao().getById(App.i().getUser().getId(), articleId);
    }
    private ArrayMap<Integer,String> articleMap = new ArrayMap<>();

    public void init(){
        //articleMap = new ArrayMap<>();
    }
    private LinearLayoutManager linearLayoutManager;
    public void setLinearLayoutManager(LinearLayoutManager linearLayoutManager){
        this.linearLayoutManager = linearLayoutManager;
    }
    public void fresh(){
        articleMap = new ArrayMap<>();
        int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
        KLog.e("刷新1：" + firstPosition + " , " + lastPosition);
        if( lastPosition >= firstPosition && firstPosition > 0 ){
            for (int i = firstPosition; i <= lastPosition; i ++){
                getItem(i);
            }
        }
    }

    public void fresh2(){
        articleMap = new ArrayMap<>();
        //int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
        //KLog.e("刷新2："  + " , " + lastPosition);
        //if( lastPosition < 0 || lastPosition >= getItemCount()){
        //    return;
        //}
        //Article article;
        //do{
        //    article = getItem(lastPosition);
        //    KLog.e("获取文章："  + " , " + lastPosition);
        //}while (article == null);
    }

    /**
     * 之所以会产生“更新页面最后几项而下一页前几项会跳动”，是因为：
     * 更新页面最后几项时，使用了getItem来获取，而在getItem的默认实现中，会将getItem不为null标识为PagedList的LastKey（需要加载的最后一项）。
     * 但是实际上被修改的项不是视图中的最后一项，所以视图中下一页的前几项会需要重新加载，进而走到onBindViewHolder的getItem。
     * 又因为这几项没有提前被加载到内存中，所以得到的是null，又触发了更新为占位符的逻辑，等到数据加载完了重新渲染时，就产生了跳动的现象。
     */
    private int lastPos = 0;
    @Override
    public Article getItem(int position) {
//        return super.getItem(position);
        Article article = super.getItem(position);
        if(position < lastPos && lastPos < getItemCount() ){
            super.getItem(lastPos);
        }else {
            lastPos = position;
        }
        KLog.e("加载：" + position  + " , " + getCurrentList().getLastKey()  + " == "+ getCurrentList().getLoadedCount() + " -- " + (article==null));
        return article;
    }
    public void setLastItem(int position){
        lastPos = position;
        super.getItem(position);
    }
    public void setLastPos(int position){
        lastPos = position;
    }
//    public void resetLastItem(int position){
//        if( getItemCount() == 0){
//            return;
//        }
//        if( position < getItemCount() && position >= 0){
//            super.getItem(position);
//        }
//    }
    public void load(int index){
        if(getCurrentList() !=null && index < getCurrentList().size()){
            getCurrentList().loadAround(index);
        }
    }
}
