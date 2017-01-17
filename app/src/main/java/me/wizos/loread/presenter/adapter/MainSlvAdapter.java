package me.wizos.loread.presenter.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.socks.library.KLog;

import java.util.List;

import me.wizos.loread.R;
import me.wizos.loread.activity.MainActivity;
import me.wizos.loread.bean.Article;
import me.wizos.loread.net.API;
import me.wizos.loread.utils.UTime;
import me.wizos.loread.view.IconFontView;

/**
 * Created by Wizos on 2016/3/15.
 */
public class MainSlvAdapter extends ArrayAdapter<Article> {


    public MainSlvAdapter(Context context, List<Article> itemArray){
        super(context, 0 , itemArray);
        this.articleList = itemArray;
        this.context = context;
    }

    List<Article> articleList;
    Context context;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomViewHolder cvh;
        Article article = this.getItem(position);
        if (convertView == null) {
            cvh = new CustomViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.main_slv_item, null);
            cvh.articleTitle = (TextView) convertView.findViewById(R.id.main_slv_item_title);
            TextPaint tp = cvh.articleTitle.getPaint();
            tp.setFakeBoldText(true);
            cvh.articleSummary =  (TextView) convertView.findViewById(R.id.main_slv_item_summary);
            cvh.articleFeed = (TextView) convertView.findViewById(R.id.main_slv_item_author);
            cvh.articleImg = (ImageView) convertView.findViewById(R.id.main_slv_item_img);
            cvh.articleTime = (TextView) convertView.findViewById(R.id.main_slv_item_time);
            cvh.articleStar = (IconFontView)convertView.findViewById(R.id.main_slv_item_icon_star);
            cvh.articleReading = (IconFontView)convertView.findViewById(R.id.main_slv_item_icon_reading);
            convertView.setTag(cvh);
        } else {
            cvh = (CustomViewHolder) convertView.getTag();
        }

        cvh.articleTitle.setText(Html.fromHtml(article.getTitle()));
        cvh.articleSummary.setText(article.getSummary());

        if(article.getCoverSrc()!=null){
            cvh.articleImg.setVisibility(View.VISIBLE);
            Glide.with(context).load(article.getCoverSrc()).centerCrop().into(cvh.articleImg);
        }else {
            cvh.articleImg.setVisibility(View.GONE);
        }
//        KLog.d("【====】");
        if (article.getOriginTitle() != null) {
            cvh.articleFeed.setText(Html.fromHtml(article.getOriginTitle()));
        }
        cvh.articleTime.setText(UTime.getFormatDate(article.getCrawlTimeMsec()));
        if ( article.getReadState().equals(API.ART_READ) &  !MainActivity.sListState.equals(API.ART_STAR) ) {
            cvh.articleTitle.setAlpha(0.40f);
            cvh.articleSummary.setAlpha(0.40f);
            cvh.articleFeed.setAlpha(0.40f);
            cvh.articleTime.setAlpha(0.40f);
        } else {
            cvh.articleTitle.setAlpha(1f);
            cvh.articleSummary.setAlpha(1f);
            cvh.articleFeed.setAlpha(1f);
            cvh.articleTime.setAlpha(1f);
        }
        KLog.d("【1】" + article.getTitle());
        if( article.getReadState().equals(API.ART_READING)){
            cvh.articleReading.setVisibility(View.VISIBLE);
        }else {
            cvh.articleReading.setVisibility(View.GONE);
        }
        if (article.getStarState().equals(API.ART_STAR)) {
            cvh.articleStar.setVisibility(View.VISIBLE);
        }else {
            cvh.articleStar.setVisibility(View.GONE);
        }
        return convertView;
    }
    private class CustomViewHolder {
        TextView articleTitle;
        TextView articleSummary;
        TextView articleFeed;
        TextView articleTime;
        IconFontView articleStar;
        IconFontView articleReading;
        ImageView articleImg;
    }
    
}
