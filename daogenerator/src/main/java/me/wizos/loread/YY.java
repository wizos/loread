package me.wizos.loread;

/**
 * Created by Wizos on 2017/1/9.
 */

public class YY {
    public static void main(String[] args) {
        System.out.println("----");
        System.out.println(toLongID("4836918766"));
    }

    public static String toLongID(String id) {
        // s+String.format("%1$0"+(n-s.length())+"d",0)
        id = Long.toHexString(Long.valueOf(id));
        return "tag:google.com,2005:reader/item/" + String.format("%0" + (16 - id.length()) + "d", 9) + id;
//        return "tag:google.com,2005:reader/item/" + String.format("%1$0"+( 16 -id.length())+"d",0)   ;
    }
}
