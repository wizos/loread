package me.wizos.loread.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.socks.library.KLog;
import com.yydcdut.sdlv.Menu;
import com.yydcdut.sdlv.MenuItem;
import com.yydcdut.sdlv.SlideAndDragListView;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.API;
import me.wizos.loread.utils.DensityUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.colorful.setter.ViewGroupSetter;

//import TagSlvAdapter;

public class TagActivity extends BaseActivity implements SlideAndDragListView.OnListItemLongClickListener, SlideAndDragListView.OnSlideListener,
        SlideAndDragListView.OnListItemClickListener, SlideAndDragListView.OnMenuItemClickListener {
    protected static final String TAG = "TagActivity";
    protected Context context;
    private String listState;
    private int listCount;
    private String listTag;
    private int noLabelCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_tag);
        context = this;
        listState = getIntent().getExtras().getString("ListState");
        listTag = getIntent().getExtras().getString("ListTag");
        listCount = getIntent().getExtras().getInt("ListCount");
        noLabelCount = getIntent().getExtras().getInt("NoLabelCount");
        userID = WithSet.i().getUseId();
        initToolbar();
        initSlvListener();
        initData();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        ViewGroupSetter listViewSetter = new ViewGroupSetter(slv);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        listViewSetter.childViewTextColor(R.id.tag_slv_item_icon, R.attr.tag_slv_item_icon);
        listViewSetter.childViewTextColor(R.id.tag_slv_item_title, R.attr.lv_item_title_color);
        listViewSetter.childViewTextColor(R.id.tag_slv_item_count, R.attr.lv_item_desc_color);
        listViewSetter.childViewBgColor(R.id.tag_slv_item, R.attr.root_view_bg);
        listViewSetter.childViewBgColor(R.id.tag_slv, R.attr.root_view_bg);

        mColorfulBuilder
                // 设置view的背景图片
                .backgroundColor(R.id.tag_root, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.tag_toolbar, R.attr.topbar_bg)
                .textColor(R.id.tag_toolbar_count, R.attr.topbar_fg)

                // 设置 bottombar
                .backgroundColor(R.id.tag_bottombar, R.attr.bottombar_bg)
                .textColor(R.id.tag_bottombar_main, R.attr.bottombar_fg)

                // 设置 listview 背景色
                .setter(listViewSetter); // 手动设置setter
        return mColorfulBuilder;
    }

    private long userID;

    private Tag getRootTag(){
        Tag rootTag = new Tag();
        Tag noLabelTag = new Tag();
        userID = WithSet.i().getUseId();
        if (listState.equals(API.LIST_STARED)) {
            rootTag.setTitle("所有加星");
            noLabelTag.setTitle("加星未分类");
        }else if(listState.equals(API.LIST_UNREAD)){
            rootTag.setTitle("所有未读");
            noLabelTag.setTitle("未读未分类");
        }else {
            rootTag.setTitle("所有文章");
            noLabelTag.setTitle("所有未分类");
        }
        rootTag.setUnreadcount(listCount);

        rootTag.setId("\"user/"+ userID + API.U_READING_LIST +"\"");
        rootTag.setSortid("00000000");

        noLabelTag.setId( "\"user/"+ userID + API.U_NO_LABEL +"\"");
        noLabelTag.setSortid("00000001");
        noLabelTag.setUnreadcount(noLabelCount);

        tagList.add( rootTag );
        tagList.add( noLabelTag );
        KLog.d("【listTag】 " + rootTag.toString());
        return rootTag;
    }

    
    private ArrayList<Tag> tagList;
    protected void initData(){
        List<Tag> tagListTemp = WithDB.i().getTags();
        if(!tagListTemp.isEmpty()){
            tagList = new ArrayList<>(tagListTemp.size());
            getRootTag();
            tagList.addAll( tagListTemp );
            slv.setAdapter(tagSlvAdapter);
            KLog.d("【tag的长度】 " + tagList.size());
        }else {
            slv.setVisibility(View.GONE);
            ToastUtil.showShort("没有数据");
        }
    }



    public void onMainListClicked(View view){
        goTo(MainActivity.TAG);
        finish(); // 在startActivity后调用的finish()，我想要是finish()放在之前会怎么样，结果结果和之前还是一样。继续google，才知道即使activity调用了finish()，也不会立即调用onDestory方法,而是执行完finish()后面的代码后才会调用onDestory方法。
        KLog.d("【onMainListClicked 被点击】");
    }



    @Override
    protected void notifyDataChanged(){
        tagSlvAdapter.notifyDataSetChanged();
    }

    private  SlideAndDragListView slv;
    public void initSlvListener() {
        initSlvMenu();
        slv = (SlideAndDragListView)findViewById(R.id.tag_slv);
        slv.setMenu(mMenu);
        slv.setOnListItemClickListener(this);
        slv.setOnSlideListener(this);
        slv.setOnMenuItemClickListener(this);
    }

    // 初始化（每一个列表项左右滑动时出现的）菜单
    protected Menu mMenu;
    public void initSlvMenu() {
        mMenu = new Menu(new ColorDrawable(Color.WHITE), true, 0);//第2个参数表示滑动item是否能滑的过量(true表示过量，就像Gif中显示的那样；false表示不过量，就像QQ中的那样)
        mMenu.addItem(new MenuItem.Builder().setWidth(DensityUtil.get2Px(this, R.dimen.slv_menu_left_width))
                .setBackground(new ColorDrawable(getResources().getColor(R.color.white)))
//                .setIcon(getResources().getDrawable(R.drawable.ic_launcher)) // 插入图片
                .setText("删除")
                .setTextColor(DensityUtil.getColor(R.color.crimson))
                .setTextSize((int) getResources().getDimension(R.dimen.txt_size))
                .build());
        mMenu.addItem(new MenuItem.Builder().setWidth(DensityUtil.get2Px(this, R.dimen.slv_menu_right_width))
                .setBackground(new ColorDrawable(getResources().getColor(R.color.white)))
                .setDirection(MenuItem.DIRECTION_RIGHT) // 设置是左或右
                .setTextColor(R.color.white)
                .setText("编辑")
                .setTextSize(DensityUtil.getDimen(this, R.dimen.txt_size))
                .build());
    }

    protected Toolbar toolbar;
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.tag_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // setDisplayShowHomeEnabled(true)   //使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
        // setDisplayShowCustomEnabled(true)  // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用，对应ActionBar.DISPLAY_SHOW_CUSTOM
    }


    @Override
    public void onListItemLongClick(View view, int position) {
    }


    @Override
    public void onListItemClick(View v, int position) {
        if(position==-1){return;}
        String tagId = tagList.get(position).getId().replace("\"","");
        Intent data = new Intent();
        data.putExtra("tagId", tagId );
        data.putExtra("tagTitle", tagList.get(position).getTitle());
        TagActivity.this.setResult(1, data);//注意下面的RESULT_OK常量要与回传接收的Activity中onActivityResult（）方法一致
        App.finishActivity(this);
        KLog.d("【 TagList 被点击】" + tagId );
    }


    @Override
    public int onSlideOpen(View view, View parentView, int position, int direction) {
        return Menu.ITEM_NOTHING;
    }
    @Override
    public void onSlideClose(View view, View parentView, int position, int direction) {
    }
    @Override
    public int onMenuItemClick(View v, int itemPosition, int buttonPosition, int direction) {
        switch (direction) {
            case MenuItem.DIRECTION_LEFT:
                switch (buttonPosition) {
                    case 0:
                        return Menu.ITEM_DELETE_FROM_BOTTOM_TO_TOP;
                }
                break;
            case MenuItem.DIRECTION_RIGHT:
                switch (buttonPosition) {
                    case 0:
                        KLog.d("【itemPosition】" + itemPosition);
                        return Menu.ITEM_DELETE_FROM_BOTTOM_TO_TOP;
                }
        }
        return Menu.ITEM_NOTHING;
    }

    protected BaseAdapter tagSlvAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return tagList.size();
        }

        @Override
        public Tag getItem(int position) {
            return tagList.get(position);
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
                convertView = LayoutInflater.from(TagActivity.this).inflate(R.layout.activity_tag_slv_item, null);
                cvh.tagTitle = (TextView) convertView.findViewById(R.id.tag_slv_item_title);
                cvh.tagCount = (TextView) convertView.findViewById(R.id.tag_slv_item_count);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }
            Tag tag = this.getItem(position);
            cvh.tagTitle.setText(tag.getTitle());
            cvh.tagCount.setText(String.valueOf(tag.getUnreadcount()));
//            KLog.d("【TagSlvAdapterGetView】" + tag.getUnreadcount());
            return convertView;
        }

        class CustomViewHolder {
            //        public ImageView imgIcon;
            private TextView tagTitle;
            private TextView tagCount;
        }
    };

}
