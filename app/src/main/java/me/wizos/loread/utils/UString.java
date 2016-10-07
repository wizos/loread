package me.wizos.loread.utils;


import com.socks.library.KLog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;

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
        return content==null || content.isEmpty() || content.equals("");
    }
    public static boolean isBlank(List list){return  list==null || list.isEmpty() || list.size()==0;}

    public static ArrayList<String> changeHtmlForBox(String oldHtml, String fileName  ){
        StringBuilder boxHtml = new StringBuilder(oldHtml);
        StringBuilder cacheHtml = new StringBuilder(oldHtml);
        String srcPath, boxSrcPath, cacheSrcPath;
        int indexB = 0,indexA, indexC = 0;
        do  {
            indexA = boxHtml.indexOf(" src=\"", indexB);
            if (indexA == -1) {
                break;
            }
            indexB = boxHtml.indexOf("\"", indexA + 6);
            if (indexB == -1) {
                break;
            }
            srcPath = boxHtml.substring(indexA + 6, indexB);
//            if ( srcPath.substring(0, 3).equals("file")) {
//                break;
//            }
            String FileNameExt = UFile.getFileNameExtByUrl(srcPath);
            boxSrcPath = "./" + fileName + "_files" + File.separator + FileNameExt;
            cacheSrcPath = App.boxAbsolutePath  + fileName + "_files" + File.separator + FileNameExt;

            KLog.e( indexA + 6 + " - " + indexB + " - " + boxHtml.length() + " - " );
            KLog.e( indexA + 6 + indexC + " - " + (indexB + indexC) + " - " + cacheHtml.length() + " - " );
            KLog.e( "=" + boxSrcPath );
            KLog.e( "=" + cacheSrcPath );

            boxHtml = boxHtml.replace( indexA + 6, indexB, boxSrcPath );
            cacheHtml = cacheHtml.replace( indexA + 6 + indexC, indexB + indexC, cacheSrcPath );

            KLog.e( "--" + boxHtml );
            KLog.e( "--" + cacheHtml );

            indexC = indexC +  cacheSrcPath.length() - boxSrcPath.length() ;
            indexB = indexA + 6 + boxSrcPath.length() + 1;
        }while (true);
        ArrayList<String> twohtml = new ArrayList<>(2);
        twohtml.add( cacheHtml.toString() );
        twohtml.add( boxHtml.toString() );
        return twohtml;
    }


    public static ArrayList<String[]> asList(String[] array){
        if(array==null || array.length==0){return null;}
        long xx = System.currentTimeMillis();
        ArrayList<String[]> arrayList = new ArrayList<>(array.length);
        String[] srcPair;
        for(String s:array){
            srcPair = s.split("|");
            arrayList.add( srcPair );
            KLog.d("【测试】" + s );
            KLog.d("【测试】" + srcPair[0] );
            KLog.d("【测试】" + srcPair[1] );
        }
        KLog.d("【时间】1测试" + (System.currentTimeMillis() - xx));
        return arrayList;
    }
    public static String[][] asArray(String[] array){
        if(array==null || array.length==0){return null;}
        long xx = System.currentTimeMillis();
        String[][] arrayList = new String[array.length][2];
        String[] srcPair;
        int num = array.length;
        for(int i=0 ; i<num ; i++){
            srcPair = array[i].split("|");
            arrayList[i] = srcPair;
        }
        KLog.d("【时间】2测试" + (System.currentTimeMillis() - xx));
        return arrayList;
    }


    public interface Con<V>{
        Object inputKey(V key);
    }
    public static <K,V> List<V> mapToList(Map<K,V> map){
        ArrayList<V> list = new ArrayList<>(map.size());
        for( Map.Entry<K,V> entry: map.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }
    public static <V> Map<Object,V> listToMap(ArrayList<V> arrayList, Con<? super V> con){
        Map<Object,V> map = new HashMap<>(arrayList.size());
        for(V item:arrayList){
            map.put(con.inputKey(item),item);
        }
        return map;
    }


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



    public static ArrayList<String[]> formStringToParamList(String paramString){
        if( paramString == null || isBlank(paramString) ){
            return null;
        }
        String[] paramStringArray = paramString.split("_");
        String[] paramPair;
        ArrayList<String[]> paramList = new ArrayList<>();
        for(String string : paramStringArray){
            paramPair = string.split("#");
            if(paramPair.length!=2){continue;}
            paramList.add(paramPair);
            KLog.d("【1】" + paramPair[0] + paramPair[1]);
        }
        return paramList;
    }

    public static String formParamListToString(ArrayList<String[]> paramList){
        if( paramList==null){
            return null;
        }
        if(paramList.size()==0){
            return null;
        }
        StringBuilder sb = new StringBuilder("");
        for( String[] paramPair:paramList){
            sb.append(paramPair[0] + "#" + paramPair[1] + "_");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

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
