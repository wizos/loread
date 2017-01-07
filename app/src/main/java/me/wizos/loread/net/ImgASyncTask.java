package me.wizos.loread.net;

import android.os.AsyncTask;
import android.support.v4.util.ArrayMap;

import com.socks.library.KLog;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

import me.wizos.loread.bean.gson.ExtraImg;
import me.wizos.loread.bean.gson.SrcPair;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UMsg;
import me.wizos.loread.utils.UToast;

/**
 * Created by Wizos on 2017/1/2.
 */
// Params, Progress 和 Result
public class ImgASyncTask extends AsyncTask<ArrayMap<Integer,SrcPair>, Object, ArrayMap<Integer,SrcPair>> {
    @Override
    public ArrayMap<Integer,SrcPair> doInBackground(ArrayMap<Integer,SrcPair>... params){
//        for( String imgUrl: params ){
//            down(imgUrl);
////            KLog.d("【获取图片的key为：" + entry.getKey() );
//        }
//        down(params[0].)
        ArrayMap<Integer,SrcPair> imgList = params[0];
        for(ArrayMap.Entry<Integer, SrcPair> entry: imgList.entrySet()){
            downSave(entry.getValue().getNetSrc(), entry.getValue().getSaveSrc(), entry.getKey() );
            KLog.d("【获取图片的key为：" + entry.getKey() );
        }
        return null;
    }
    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
//        Log.i("iSpring", "DownloadTask -> onProgressUpdate, Thread name: " + Thread.currentThread().getName());
        int byteCount = (int)values[0];
        String blogName = (String)values[1];
        String text = textView.getText().toString();
        text += "\n博客《" + blogName + "》下载完成，共" + byteCount + "字节";
        textView.setText(text);
    }
    private void downSave( final String imgUrl ,final String filePath ,final int imgNo ){

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
//                    UMsg.img(handler,API.F_BITMAP,imgUrl, filePath ,imgNo);
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
                    publishProgress( imgNo );
                    KLog.d("【成功保存图片】" + imgUrl );
//                    UMsg.img(handler,state, imgUrl, filePath,imgNo);

                    //得到响应内容的文件格式
//                        String fileTypeInResponse = getMediaType( response.body().contentType() );
                }
            }
        });
    }

    //得到响应内容的文件格式
    private String getMediaType( MediaType contentType ){
        MediaType mediaType = contentType;
        if (mediaType != null) {
            return  "." + contentType.subtype();
        }
        return "";
    }


    private void img(){
        imgNo = msg.getData().getInt("imgNo");
        SrcPair imgSrcPair = lossSrcList.get(imgNo);
        if(imgSrcPair==null){
//                        KLog.i("【 imgSrc为空 】==");
//                        UToast.showShort("");
            imgSrcPair = obtainSrcList.get(imgNo);
        }else {
            obtainSrcList.put(imgNo, lossSrcList.get(imgNo) );
            lossSrcList.remove(imgNo);
        }
        KLog.i("【2】" + lossSrcList.size()  + obtainSrcList.get(imgNo)  );
        numOfGetImgs = numOfGetImgs + 1;
        KLog.i("【 API.S_BITMAP 】" + imgNo + "=" + numOfGetImgs + "--" + numOfImgs);
        if( numOfGetImgs >= numOfImgs ) { // || numOfGetImgs % 5 == 0
            KLog.i("【图片全部下载完成】" + numOfGetImgs + "=" +  numOfImgs );
            webView.clearCache(true);
            lossSrcList.clear();
            logImgStatus(ExtraImg.DOWNLOAD_OVER);
            UToast.showShort("图片全部下载完成");
//                        webView.notify();
        }else {
            logImgStatus(ExtraImg.DOWNLOAD_ING);
        }
        KLog.i("【1】" +  imgSrcPair );
        if( imgSrcPair != null ){
            replaceSrc( imgNo, imgSrcPair.getLocalSrc() );
        }
    }


}
