package me.wizos.loread.adapter;


import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ditclear.swipelayout.SwipeDragLayout;

import java.util.List;

import me.wizos.loread.R;
import me.wizos.loread.db.Article;
import me.wizos.loread.net.Api;
import me.wizos.loread.utils.TimeUtil;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.ListViewS;


/**
 * Created by Wizos on 2016/3/15.
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
//        KLog.e("【getCount】" + articleList.size() );
//        Tool.printCallStatck();
        return articleList.size();
    }

    @Override
    public Article getItem(int position) {
//        KLog.e("【getItem】" + position );
        return articleList.get(position);
    }

    @Override
    public long getItemId(int position) {
//        KLog.e("【getItemId】" + position );
        return position;
    }


    //    Menu menu;
//    View itemView;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Article article = this.getItem(position);
        if (convertView == null) {
            cvh = new CustomViewHolder();
//            convertView = LayoutInflater.from(context).inflate(R.layout.activity_main_slv_item, null);
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_main_list_item, null);
            cvh.articleTitle = convertView.findViewById(R.id.main_slv_item_title);
            cvh.articleSummary = convertView.findViewById(R.id.main_slv_item_summary);
            cvh.articleFeed = convertView.findViewById(R.id.main_slv_item_author);
            cvh.articleImg = convertView.findViewById(R.id.main_slv_item_img);
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
//            cvh.articleSummary.setText( Jsoup.parse(article.getSummary()).text() );
        }

        if (article.getCoverSrc() != null) {
            cvh.articleImg.setVisibility(View.VISIBLE);
            Glide.with(context).load(article.getCoverSrc()).centerCrop().into(cvh.articleImg);
        } else {
            cvh.articleImg.setVisibility(View.GONE);
        }

        if (article.getOriginTitle() != null) {
            cvh.articleFeed.setText(Html.fromHtml(article.getOriginTitle()));
        }
        cvh.articlePublished.setText(TimeUtil.stampToTime(article.getPublished() * 1000, "yyyy-MM-dd HH:mm"));
        if (article.getReadState().equals(Api.ART_READED)) { //  & !App.StreamState.equals(Api.ART_STARED)
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

//        KLog.e("++++++++  "  + article.getTitle() + " - " + article.getReadState() + " - " + article.getStarState() + " - "  + cvh.articleTitle.getVisibility());
        return convertView;
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
