package me.wizos.loread.net;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.socks.library.KLog;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

import me.wizos.loread.net.API;
import me.wizos.loread.presenter.UMsg;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UToast;

/**
 * Created by Wizos on 2017/1/2.
 */

public class ImgDownloadService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        KLog.d("ImgDS","DownloadService -> onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int imgNo) {
//        allimgNoList.add(imgNo);
        String name = intent.getStringExtra("name");
        String url = intent.getStringExtra("url");
        KLog.d("ImgDS","ImgDS -> onStartCommand,imgNo: " + imgNo + ", name: " + name);
        DownloadThread downloadThread = new DownloadThread(  url, name, imgNo);
        downloadThread.start();
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        KLog.d("ImgDS","ImgDS -> onDestroy");
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                String tip = (String)msg.obj;
                UToast.showShort( tip );
            }
        }
    };


    class DownloadThread extends Thread {
        //要下载的图片地址
        private String imgUrl = null;
        //对应的intent的imgNo信息
        private int imgNo = 0;
        //要保存的文件路径名
        private String filePath = null;


        public DownloadThread( String imgUrl, String filePath, int imgNo){
            this.imgUrl = imgUrl;
            this.filePath = filePath;
            this.imgNo = imgNo;
        }

        @Override
        public void run() {
            Request.Builder builder = new Request.Builder();
            builder.url(imgUrl);
            final Request request = builder.build();
            HttpUtil.enqueue(request, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    KLog.d("【图片请求失败 = " + imgUrl + "】");
                    UMsg.img(handler,API.F_BITMAP, imgUrl, filePath ,imgNo );
                }
                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        KLog.d("【图片响应失败】" + response);
                        UMsg.img(handler,API.F_BITMAP,imgUrl, filePath ,imgNo);
                    }else {
                        InputStream inputStream = null;
                        int state = API.S_BITMAP;
                        try {
//                            is = response.body.byteStream();
//                            is.reset();
//                            BitmapFactory.Options ops = new BitmapFactory.Options();
//                            ops.inJustDecodeBounds = false;
//                            final Bitmap bm = BitmapFactory.decodeStream(is, null, ops);
//                            mDelivery.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    callBack.onResponse(bm);
//                                }
//                            })
                            inputStream = response.body().byteStream();
                            if( UFile.saveFromStream(inputStream, filePath) ){
                                state = API.F_BITMAP;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            response.body().close();
                        }

                        KLog.d("【成功保存图片】" + imgUrl + "==" + filePath);
                        UMsg.img(handler,state, imgUrl, filePath,imgNo);
                        //得到响应内容的文件格式
//                        String fileTypeInResponse = "";
//                        MediaType mediaType = response.body().contentType();
//                        if (mediaType != null) {
//                            fileTypeInResponse = "." + mediaType.subtype();
//                        }
                    }
                }
            });
        }
    }
}
