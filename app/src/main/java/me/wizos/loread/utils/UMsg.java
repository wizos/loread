package me.wizos.loread.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Wizos on 2017/1/2.
 */

public class UMsg {

//    Handler handler;
//    protected UMsg makeMsg( Handler handler ){
//        this.handler = handler;
//        return this;
//    }

//    Map<String,Handler> map = new ArrayMap<>();
//    private Handler getHandler(String artivityTag){
//        if ( map == null ){
//            map = new ArrayMap<>();
//            return null;
//        }
//        return map.get(artivityTag);
//    }

    public static void img(Handler handler, int msgId, String url, String filePath, int imgNo) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("filePath", filePath);
        bundle.putInt("imgNo", imgNo);
        message.what = msgId;
        message.setData(bundle);
        handler.sendMessage(message);
    }


}
