//package me.wizos.loread.adapter;
//
//import android.content.Context;
//import android.widget.ArrayAdapter;
//
//import java.util.ArrayList;
//
//import me.wizos.loread.gson.Item;
//
///**
// * Created by Wizos on 2016/3/6.
// */
//public class Slv extends ArrayAdapter<Item> {
//    public Slv(Context context, int textViewResourceId, ArrayList<Item> itemArray){
//        super(context, textViewResourceId, itemArray);
//    }
//
////    public Slv( String time, String desc, String meta, String imgName) {
////        this.time = time;
////        this.desc = desc;
////        this.meta = meta;
////        this.imgName = imgName;
////    }
//
//    private String title;
//    private String time;
//    private String desc;
//    private String meta;
//    private String imgName;
//
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//    public String getTitle() {
//        return title;
//    }
//    public void setTime(String time) {
//        this.time = time;
//    }
//    public String getTime() {
//        return time;
//    }
//
//    public void setDesc(String desc) {
//        this.desc = desc;
//    }
//    public String getDesc() {
//        return desc;
//    }
//
//    public void setMeta(String meta) {
//        this.meta = meta;
//    }
//    public String getMeta() {
//        return meta;
//    }
//
//    public void setImgName(String account){this.imgName = account;}
//    public String getImgName(){return imgName;}
//
//
////    public Drawable getDraw(String icontitle) {
////        int resId = ResourceIdUtils.getIdOfResource(icontitle, "drawable");
//////        System.out.println("【icontitle】" + "ttt" + icontitle + resId);
////        return App.getContext().getResources().getDrawable(resId,null);
////    }
//
//    @Override //"\n" +
//    public String toString() {
//        return "【meta:--->" + this.getMeta() + "price:--->" + this.getDesc()+"】";
//    }
//
//}
//
