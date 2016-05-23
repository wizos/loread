package me.wizos.loread.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by Wizos on 2016/3/16.
 */
public class UString {
    public static String toLongID(String id) {
       return  "tag:google.com,2005:reader/item/0000000"+ Long.toHexString( Long.valueOf(id));
    }

    /**
     * 将字符串转成MD5值
     *
     * @param string
     * @return
     */
    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

//    public static String tagIdToName(String content){
//    }

    public static boolean isBlank(String content){
        return content==null || content.isEmpty();
    }
    public static boolean isBlank(List list){return  list==null || list.isEmpty() || list.size()==0;}





//    public static String beanListSort(ArrayList<Tag> list){
//        int listSize = list.size()-1;
//        for(int i=0; i<listSize; i++){
//            char[] chars1 = list.get(i).getTitle().toCharArray();
//            char[] chars2 = list.get(i+1).getTitle().toCharArray();
//            int x =0, y=0;
//            while (chars1[x]>=chars2[x]){
//                if(chars1[x]==chars2[x]){
//                    x = x + 1;
//                }
//            }
//        }
//    }



    public static String sort(String str){
        char[] charArray = str.toCharArray();
        for(int i=0;i<charArray.length;i++){
            for(int j=0;j<i;j++){
                if(charArray[i]<charArray[j]){
                    char temp = charArray[i];
                    charArray[i] = charArray[j];
                    charArray[j] = temp;
                }
            }
        }
        return String.valueOf(charArray);
    }


}
