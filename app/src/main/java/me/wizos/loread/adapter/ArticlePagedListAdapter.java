package me.wizos.loread.adapter;

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
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.carlt.networklibs.NetType;
import com.carlt.networklibs.utils.NetworkUtils;
import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.config.HeaderRefererConfig;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.ArticleMeta;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.service.TimeHandler;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.view.IconFontView;

public class ArticlePagedListAdapter extends PagedListAdapter<ArticleMeta, ArticlePagedListAdapter.ArticleViewHolder> {
    private RequestOptions canDownloadOptions;
    private RequestOptions cannotDownloadOptions;
    private Context context;

    public ArticlePagedListAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        canDownloadOptions = new RequestOptions()
                .centerCrop()
                .onlyRetrieveFromCache(false)
                .priority(Priority.HIGH);
        cannotDownloadOptions = new RequestOptions()
                .centerCrop()
                .onlyRetrieveFromCache(true)
                .priority(Priority.HIGH);
    }

    @NonNull
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        return new ArticleViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_main_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        ArticleMeta article = super.getItem(position);
        // XLog.d("创建onBindViewHolder，LastKey = " + Objects.requireNonNull(getCurrentList()).getLastKey() + " , " + getCurrentList().getPositionOffset() + "  " + (article == null) + "  " + position);
        // 如果article是null，在此处不停循环的获取getItem得到的还是null
        if (article != null) {
            holder.bindTo(article);
        } else {
            // Null defines a placeholder item - PagedListAdapter automatically invalidates this row when the actual object is loaded from the database.
            holder.placeholder();
        }
    }

    private static DiffUtil.ItemCallback<ArticleMeta> DIFF_CALLBACK = new DiffUtil.ItemCallback<ArticleMeta>() {
        @Override
        public boolean areItemsTheSame(ArticleMeta oldArticle, ArticleMeta newArticle) {
            // XLog.d("文章：判断id是否一致");
            return oldArticle.getId().equals(newArticle.getId());
        }

        @Override
        public boolean areContentsTheSame(ArticleMeta oldArticle, ArticleMeta newArticle) {
            // XLog.d("文章：判断内容是否一致");
            return oldArticle.getReadStatus() == newArticle.getReadStatus()
                    && oldArticle.getStarStatus() == newArticle.getStarStatus()
                    && oldArticle.getSaveStatus() == newArticle.getSaveStatus()
                    // && oldArticle.getTitle().equals(newArticle.getTitle())
                    && (oldArticle.getTitle() != null ? oldArticle.getTitle().equals(newArticle.getTitle()) : newArticle.getTitle()==null)
                    && (oldArticle.getImage() != null ? oldArticle.getImage().equals(newArticle.getImage()) : newArticle.getImage()==null)
                    && (oldArticle.getSummary() != null ? oldArticle.getSummary().equals(newArticle.getSummary()) : newArticle.getSummary()==null);
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
            articleFeed.setText("");
            articlePublished.setText("");
            articleSave.setVisibility(View.GONE);
            articleReading.setVisibility(View.GONE);
            articleStar.setVisibility(View.GONE);
        }


        void bindTo(ArticleMeta article){
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
                    // XLog.e( "数据：" + article.getTitle() + "   "  +  App.Referer+ "   "  +  article.getLink() );
                    String referer = HeaderRefererConfig.i().guessRefererByUrl(article.getImage());
                    if (StringUtils.isEmpty(referer) && !TextUtils.isEmpty(article.getLink())){
                        referer = StringUtils.urlEncode(article.getLink());
                    }

                    if (!TextUtils.isEmpty(referer)) {
                        GlideUrl gliderUrl = new GlideUrl(article.getImage(), new LazyHeaders.Builder().addHeader(Contract.REFERER, referer).build());
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

            articleFeed.setText(article.getFeedTitle());

            articlePublished.setText(TimeHandler.i().readability(article.getPubDate()));

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

    private List<String> articleIds = new ArrayList<>();
    public void setArticleIds(List<String> articleIds){
        this.articleIds = articleIds;
    }
    public String getArticleId(int position){
        if(articleIds != null && position < articleIds.size()){
            return articleIds.get(position);
        }else {
            XLog.e("articleIds 为空 或 索引超出下标。position = " + position + ", articleIds size = " + (articleIds!=null ? articleIds.size():-1));
            return "";
        }
    }
    public Article getArticle(int position){
        return CoreDB.i().articleDao().getById(App.i().getUser().getId(), getArticleId(position));
    }

    /**
     * 之所以会产生“更新页面最后几项而下一页前几项会跳动”，是因为：
     * 更新页面最后几项时，使用了getItem来获取，而在getItem的默认实现中，会将getItem不为null标识为PagedList的LastKey（需要加载的最后一项）。
     * 但是实际上被修改的项不是视图中的最后一项，所以视图中下一页的前几项会需要重新加载，进而走到onBindViewHolder的getItem。
     * 又因为这几项没有提前被加载到内存中，所以得到的是null，又触发了更新为占位符的逻辑，等到数据加载完了重新渲染时，就产生了跳动的现象。
     */
    // private int lastPos = 0;
    // @Override
    // public Article getItem(int position) {
    //     XLog.e("获取项目：" + position + " , " + lastPos + " , " );
    //    return super.getItem(position);
    // }

    // public Article getItem(int position){
    //     return super.getItem(position);
    // }
    // public void setLastPos(int position){
    //     lastPos = position;
    // }

    // // 不能再 submitList 用 lastPos = (int)getCurrentList().getLastKey()。因为修改了列表的某项时，lastKey已经变为该项了。
    // @Override
    // public void submitList(@Nullable PagedList<Article> pagedList) {
    //     super.submitList(pagedList);
    //     // XLog.i("提交的文件数量：" + lastPos + " , " );
    //     if(pagedList == null){
    //         lastPos = 0;
    //     }else if(lastPos >= 0 && lastPos < pagedList.size() && lastPos < getItemCount()){
    //         super.getItem(lastPos);
    //     }else {
    //         lastPos = pagedList.size();
    //     }
    // }
}
