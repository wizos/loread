package me.wizos.loread.adapter;


import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ditclear.swipelayout.SwipeDragLayout;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import java.io.File;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.config.FeedConfig;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.net.Api;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.TimeUtil;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.ListViewS;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;


/**
 * @author Wizos on 2016/3/15.
 */
public class MainListViewAdapter extends ArrayAdapter<Article> {
    private List<Article> articleList;
    private Context context;
    private ListViewS slv;

    public MainListViewAdapter(Context context, List<Article> articleList, ListViewS slv) {
        super(context, 0, articleList);
        this.context = context;
        this.articleList = articleList;
        this.slv = slv;
    }
    @Override
    public int getCount() {
        return articleList.size();
    }

    @Override
    public Article getItem(int position) {
        return articleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Article article = this.getItem(position);
        if (convertView == null) {
            cvh = new CustomViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_main_list_item, null);
            cvh.articleTitle = convertView.findViewById(R.id.main_slv_item_title);
            cvh.articleSummary = convertView.findViewById(R.id.main_slv_item_summary);
            cvh.articleFeed = convertView.findViewById(R.id.main_slv_item_author);
            cvh.articleImg = convertView.findViewById(R.id.main_slv_item_img);
            cvh.articleImg.setBackgroundColor(getContext().getResources().getColor(R.color.placeholder_bg));
            cvh.articlePublished = convertView.findViewById(R.id.main_slv_item_time);
            cvh.articleStar = convertView.findViewById(R.id.main_slv_item_icon_star);
            cvh.articleReading = convertView.findViewById(R.id.main_slv_item_icon_reading);
            cvh.articleSave = convertView.findViewById(R.id.main_slv_item_icon_save);
            cvh.markLeft = convertView.findViewById(R.id.main_list_item_menu_left);
            cvh.markRight = convertView.findViewById(R.id.main_list_item_menu_right);
            swipeDragLayout = convertView.findViewById(R.id.swip_layout);
            swipeDragLayout.addListener(slv);

            convertView.setTag(cvh);
        } else {
            cvh = (CustomViewHolder) convertView.getTag();
        }

        cvh.articleTitle.setText(Html.fromHtml(article.getTitle()));
        if (article.getSummary().length() == 0) {
            cvh.articleSummary.setVisibility(View.GONE);
        } else {
            cvh.articleSummary.setVisibility(View.VISIBLE);
            cvh.articleSummary.setText(article.getSummary());
        }

        if (!TextUtils.isEmpty(article.getCoverSrc())) {
            String idInMD5 = StringUtil.str2MD5(article.getId());
            final String coverPath = App.externalFilesDir + "/cache/" + "/" + idInMD5 + "/";
            final String fileName = idInMD5 + StringUtil.getImageSuffix(article.getCoverSrc());

            Glide.clear(cvh.articleImg);
            if (new File(coverPath + fileName).exists()) {
                cvh.articleImg.setVisibility(View.VISIBLE);
                Glide.with(context).load(coverPath + fileName).centerCrop().into(cvh.articleImg); // diskCacheStrategy(DiskCacheStrategy.NONE).
            }
//            else if( new File(coverPath + fileName + "_files/"  + fileName ).exists() ){
//
//            }

            else if (!new File(coverPath + fileName + Api.EXT_TMP).exists() && Tool.canDownImg()) {
                cvh.articleImg.setVisibility(View.VISIBLE);
                Glide.with(context).load("file:///android_asset/image/placeholder.png").centerCrop().into(cvh.articleImg);

                downCoverImage(coverPath, fileName, article, cvh.articleImg);
            }
        } else {
            cvh.articleImg.setVisibility(View.GONE);
        }

        if (article.getOriginTitle() != null) {
            cvh.articleFeed.setText(Html.fromHtml(article.getOriginTitle()));
        }
        cvh.articlePublished.setText(TimeUtil.stampToTime(article.getPublished() * 1000, "yyyy-MM-dd HH:mm"));
        if (article.getReadState().equals(Api.ART_READED)) {
            cvh.articleTitle.setAlpha(0.40f);
        } else {
            cvh.articleTitle.setAlpha(1f);
        }

        if (article.getReadState().equals(Api.ART_UNREADING)) {
            cvh.articleReading.setVisibility(View.VISIBLE);
            cvh.markRight.setText(context.getResources().getString(R.string.font_readed));
        } else if (article.getReadState().equals(Api.ART_UNREAD)) {
            cvh.articleReading.setVisibility(View.GONE);
            cvh.markRight.setText(context.getResources().getString(R.string.font_readed));
        } else {
            cvh.articleReading.setVisibility(View.GONE);
            cvh.markRight.setText(context.getResources().getString(R.string.font_unread));
        }
        if (article.getStarState().equals(Api.ART_STARED)) {
            cvh.articleStar.setVisibility(View.VISIBLE);
            cvh.markLeft.setText(context.getResources().getString(R.string.font_unstar));
        } else {
            cvh.articleStar.setVisibility(View.GONE);
            cvh.markLeft.setText(context.getResources().getString(R.string.font_stared));
        }
        if (Api.SAVE_DIR_CACHE.equals(article.getSaveDir())) {
            cvh.articleSave.setVisibility(View.GONE);
        } else {
            cvh.articleSave.setVisibility(View.VISIBLE);
        }
        return convertView;
    }


//    private void downImage(final String filePath, final String fileNameExt,final Article article){
//        if( new File(filePath + fileNameExt+ ".temp").exists() ){
//            return;
//        }
//        FileCallback fileCallback = new FileCallback(filePath, fileNameExt + ".temp") {
//            @Override
//            public void onSuccess(Response<File> response) {
//                new File(filePath + fileNameExt + ".temp").renameTo(new File(filePath + fileNameExt));
//                article.setCoverSrc(filePath + fileNameExt);
//                WithDB.i().saveArticle(article);
//                MainListViewAdapter.this.notifyDataSetChanged();
//            }
//
//            // 该方法执行在主线程中
//            @Override
//            public void onError(Response<File> response) {
//                new File(filePath + fileNameExt + ".temp").delete();
//            }
//        };
//
////        KLog.e("下载图片：" + article.getCoverSrc() + "      " + "file://" + filePath + fileNameExt  + "    "+ new File("file://" + filePath + fileNameExt).exists());
//        OkGo.<File>get(article.getCoverSrc())
//                .execute(fileCallback);
//    }

//    private void copyImage( final String coverPath, final String fileName){
//        new File(coverPath + fileName + "_files/"  + fileName ).
//    }

    private void downCoverImage(final String coverPath, final String fileName, Article article, final View view) {
        // 2018/4/14 如果发现文章正文的图片有下载，就直接用？不行，因为根据CoverSrc找不到这个图片

        FileCallback fileCallback = new FileCallback(coverPath, fileName + Api.EXT_TMP) {
            @Override
            public void onSuccess(final Response<File> response) {
//                KLog.e("图片下载成功，开始压缩" + width + "  高度" + height );
                if (!response.isSuccessful()) {
                    onError(response);
                    return;
                }
                new File(coverPath + fileName + Api.EXT_TMP).renameTo(new File(coverPath + fileName));
//                YaSuo.with(context)
//                        .load(coverPath , fileName)
////                        .setSize(width, height)
//                        .size(view)
//                        .setCompressListener(new YaSuo.OnCompressListener() {
//                            @Override
//                            public void onSuccess(File file) {
//                                file.renameTo( new File(coverPath + fileName) );
//                                MainListViewAdapter.this.notifyDataSetChanged();
////                                KLog.e("压缩成了");
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
////                                KLog.e("压缩失败");
//                                MainListViewAdapter.this.notifyDataSetChanged();
//                            }
//                        }).launch();    //启动压缩

                Luban.with(context)
                        .load(coverPath + fileName)
                        .ignoreBy(50)
                        .setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onSuccess(File file) {
                                file.renameTo(new File(coverPath + fileName));
                                MainListViewAdapter.this.notifyDataSetChanged();
//                                KLog.e("压缩成了");
                            }

                            @Override
                            public void onError(Throwable e) {
                                MainListViewAdapter.this.notifyDataSetChanged();
//                                KLog.e("压缩失败");
                            }
                        }).launch();    //启动压缩


            }

            @Override
            public void onError(Response<File> response) {
                new File(coverPath + fileName + Api.EXT_TMP).delete();
            }
        };

        Request request = OkGo.<File>get(article.getCoverSrc())
                .client(App.imgHttpClient);
        Feed feed = WithDB.i().getFeed(article.getOriginStreamId());
        if (feed != null && null != feed.getConfig()) {
            FeedConfig feedConfig = feed.getConfig();
            String referer = feedConfig.getReferer();
            if (TextUtils.isEmpty(referer)) {
            } else if (Api.Auto.equals(referer)) {
                request.headers(Api.Referer, article.getCanonical());
            } else {
                request.headers(Api.Referer, referer);
            }
            String userAgent = feedConfig.getUserAgent();
            if (!TextUtils.isEmpty(userAgent)) {
                request.headers(Api.UserAgent, userAgent);
            }
        }
        request.execute(fileCallback);
    }



    private CustomViewHolder cvh;
    private SwipeDragLayout swipeDragLayout;
    public class CustomViewHolder {
        TextView articleTitle;
        TextView articleSummary;
        TextView articleFeed;
        TextView articlePublished;
        IconFontView articleStar;
        IconFontView articleReading;
        IconFontView articleSave;
        ImageView articleImg;

        public IconFontView markLeft;
        public IconFontView markRight;
    }

}
