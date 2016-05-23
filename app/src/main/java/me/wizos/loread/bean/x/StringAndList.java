package me.wizos.loread.bean.x;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/26.
 */
public class StringAndList {
    protected String string;
    protected ArrayList<Strings> list;

    public void setString(String string){
        this.string = string;
    }
    public String getString(){
        return string;
    }

    public void setList(ArrayList<Strings> list){
        this.list = list;
    }
    public ArrayList<Strings> getList(){
        return list;
    }
}
