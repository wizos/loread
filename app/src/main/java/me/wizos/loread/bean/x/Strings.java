package me.wizos.loread.bean.x;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/26.
 */
public class Strings {

    public String stringA,stringB,stringC,stringD,stringE;
    public ArrayList listA;

    public Strings(String stringA,String stringB){
        this.stringA = stringA;
        this.stringB = stringB;
    }

    public String getStringA(){
        return stringA;
    }
    public String getStringB(){
        return stringB;
    }
    public String getStringC(){
        return stringC;
    }

    public static ArrayList<Strings> asValues(String[] array){
        int num = array.length;
        String[] xx = array[0].split("|");
        ArrayList<Strings> arrayList = new ArrayList<>();
        if(xx.length==2){
            for(int i=0; i<num; i++){
                xx = array[i].split("|");
                arrayList.add(new Strings(xx[0],xx[1]));
            }
        }
        return arrayList;
    }

}
