package me.wizos.loread.presenter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.bean.Tag;


/**
 * Created by Wizos on 2016/3/12.
 */
public class TagSlvAdapter extends ArrayAdapter<Tag>{

    public TagSlvAdapter(Context context, int textViewResourceId, ArrayList<Tag> itemArray){
        super(context, textViewResourceId, itemArray);
        this.tags = itemArray;
        this.context = context;
    }

    public TagSlvAdapter(Context context, ArrayList<Tag> itemArray){
        super(context, 0 , itemArray);
        this.tags = itemArray;
        this.context = context;
    }

    List<Tag> tags;
    Context context;

    @Override
    public int getCount() {
        return tags.size();
    }
    @Override
    public Tag getItem(int position) {
        return tags.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomViewHolder cvh;
        if (convertView == null) {
            cvh = new CustomViewHolder();
            convertView = LayoutInflater.from(context).inflate(me.wizos.loread.R.layout.tagslv_item, null);
            cvh.tagTitle = (TextView) convertView.findViewById(me.wizos.loread.R.id.tag_title);
            convertView.setTag(cvh);
        } else {
            cvh = (CustomViewHolder) convertView.getTag();
        }
        Tag tag = this.getItem(position);

        cvh.tagTitle.setText(tag.getTitle());

        System.out.println("【TagSlvAdapterGetView】" + tag.getTitle());

        return convertView;
    }
    class CustomViewHolder {
        //        public ImageView imgIcon;
        public TextView tagTitle;
//        public TextView txtMoney;
    }


}
