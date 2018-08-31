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
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.ditclear.swipelayout.SwipeDragLayout;

import java.util.List;

import me.wizos.loread.R;
import me.wizos.loread.db.Article;
import me.wizos.loread.net.Api;
import me.wizos.loread.utils.NetworkUtil;
import me.wizos.loread.utils.TimeUtil;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.ListView.ListViewS;


/**
 * @author Wizos on 2016/3/15.
 */
public class MainListViewAdapter extends ArrayAdapter<Article> {
    private List<Article> articleList;
    private Context context;
    private ListViewS slv;
    private RequestOptions canDownloadOptions;
    private RequestOptions cannotDownloadOptions;

    public MainListViewAdapter(Context context, List<Article> articleList, ListViewS slv) {
        super(context, 0, articleList);
        this.context = context;
        this.articleList = articleList;
        this.slv = slv;
        canDownloadOptions = new RequestOptions()
                .centerCrop()
                .onlyRetrieveFromCache(false)
                .priority(Priority.HIGH);
        cannotDownloadOptions = new RequestOptions()
                .centerCrop()
                .onlyRetrieveFromCache(true)
                .priority(Priority.HIGH);
    }
    @Override
    public int getCount() {
        return articleList.size();
    }

    @Override
    public Article getItem(int position) {
        try {
            return articleList.get(position);
        } catch (Exception e) {
            e.printStackTrace();
            return articleList.get(position);
        }
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
            swipeDragLayout = convertView.findViewById(R.id.swipe_layout);
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
            cvh.articleImg.setVisibility(View.VISIBLE);

//            String idInMD5 = StringUtil.str2MD5(article.getId());
//            String coverPath = App.externalFilesDir + "/cache/" +  idInMD5 + "/";
//            String fileName = idInMD5 + StringUtil.getImageSuffix(article.getCoverSrc());
//            if (new File(coverPath + fileName).exists()) {
//                Glide.with(context).load(coverPath + fileName).centerCrop().into(cvh.articleImg); // diskCacheStrategy(DiskCacheStrategy.NONE).
//            }

//            KLog.e("网络", Tool.canDownImg() );
            if (NetworkUtil.canDownImg()) {
                Glide.with(context).load(article.getCoverSrc()).apply(canDownloadOptions).into(cvh.articleImg); // diskCacheStrategy(DiskCacheStrategy.NONE).
            } else {
                Glide.with(context).load(article.getCoverSrc()).apply(cannotDownloadOptions).into(cvh.articleImg);
            }
        } else {
            cvh.articleImg.setVisibility(View.GONE);
        }

        if (article.getOriginTitle() != null) {
            cvh.articleFeed.setText(Html.fromHtml(article.getOriginTitle()));
        }
        cvh.articlePublished.setText(TimeUtil.stampToTime(article.getPublished() * 1000, "yyyy-MM-dd HH:mm"));
        if (article.getReadStatus() == Api.READED) {
            cvh.articleTitle.setAlpha(0.40f);
        } else {
            cvh.articleTitle.setAlpha(1f);
        }

        if (article.getReadStatus() == Api.UNREADING) {
            cvh.articleReading.setVisibility(View.VISIBLE);
            cvh.markRight.setText(context.getResources().getString(R.string.font_readed));
        } else if (article.getReadStatus() == Api.UNREAD) {
            cvh.articleReading.setVisibility(View.GONE);
            cvh.markRight.setText(context.getResources().getString(R.string.font_readed));
        } else {
            cvh.articleReading.setVisibility(View.GONE);
            cvh.markRight.setText(context.getResources().getString(R.string.font_unread));
        }
        if (article.getStarStatus() == Api.STARED) {
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


//    private void downCoverImage(final String coverPath, final String fileName, final Article article,final String idInMD5, final View view) {
//        // 2018/4/14 如果发现文章正文的图片有下载，就直接用？不行，因为根据CoverSrc找不到这个图片
//
//        FileCallback fileCallback = new FileCallback(coverPath, fileName + Api.EXT_TMP) {
//            @Override
//            public void onSuccess(final Response<File> response) {
////                KLog.e("图片下载成功，开始压缩" + width + "  高度" + height );
//                if (!response.isSuccessful()) {
//                    onError(response);
//                    return;
//                }
//
//                AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
//                    @Override public void run() {
//                        try {
//
//                            FileUtil.copy(new File(coverPath + fileName + Api.EXT_TMP), new File(coverPath + idInMD5 + "_files/0-" + StringUtil.getFileNameExtByUrl(article.getCoverSrc())) );
//                            new File(coverPath + fileName + Api.EXT_TMP).renameTo(new File(coverPath + fileName));
//
//                            Luban.with(context)
//                                    .load(coverPath + fileName)
//                                    .ignoreBy(50)
//                                    .setCompressListener(new OnCompressListener() {
//                                        @Override
//                                        public void onStart() {
//                                        }
//
//                                        @Override
//                                        public void onSuccess(File file) {
//                                            KLog.e("压缩成了" + file.getAbsolutePath() );
//                                            file.renameTo(new File(coverPath + fileName));
//                                            MainListViewAdapter.this.notifyDataSetChanged();
//                                        }
//
//                                        @Override
//                                        public void onError(Throwable e) {
//                                        }
//                                    }).launch();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                        KLog.e("当前线程是：" + Thread.currentThread());
//                    }
//                });
//
//            }
//
//            @Override
//            public void onError(Response<File> response) {
//                new File(coverPath + fileName + Api.EXT_TMP).delete();
//            }
//        };
//
//        Request request = OkGo.<File>get(article.getCoverSrc())
//                .client(App.imgHttpClient);
//        Feed feed = WithDB.i().getFeed(article.getOriginStreamId());
//        if (feed != null && null != feed.getConfig()) {
//            FeedConfig feedConfig = feed.getConfig();
//            String referer = feedConfig.getReferer();
//            if (TextUtils.isEmpty(referer)) {
//            } else if (Api.Auto.equals(referer)) {
//                request.headers(Api.Referer, article.getCanonical());
//            } else {
//                request.headers(Api.Referer, referer);
//            }
//            String userAgent = feedConfig.getUserAgent();
//            if (!TextUtils.isEmpty(userAgent)) {
//                request.headers(Api.UserAgent, userAgent);
//            }
//        }
//        request.execute(fileCallback);
//    }



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
