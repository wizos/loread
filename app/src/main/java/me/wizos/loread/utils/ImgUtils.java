package me.wizos.loread.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;

import com.elvishew.xlog.XLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;


/**
 * Created by Wizos on 2018/12/22.
 */
public class ImgUtils {
    public interface OnMergeListener {
        /**
         * Fired when a compression returns successfully, override to handle in your own code
         */
        void onSuccess();

        /**
         * Fired when a compression fails to complete, override to handle in your own code
         */
        void onError(Throwable e);
    }

    /**
     *
     * @param context
     * @param bgFile 文件名后缀必须为.gif，并且在compressed文件夹中
     * @param onMergeListener
     */
    public static void mergeBitmap(final WeakReference<Context> context, final File bgFile, boolean isGIF, final OnMergeListener onMergeListener) {
        // XLog.d("是否为动图：" + isGIF + " , " + bgFile.getAbsolutePath());
        if (!isGIF || !bgFile.getAbsolutePath().toLowerCase().contains("/compressed/")) {
            onMergeListener.onSuccess();
            return;
        }

        AsyncTask.SERIAL_EXECUTOR.execute(() -> {
            try {
                FileInputStream fis1 = new FileInputStream(bgFile);
                Bitmap bgBitmap = BitmapFactory.decodeStream(fis1).copy(Bitmap.Config.ARGB_8888, true);

                // int shortSide = Math.min(bgBitmap.getWidth(), bgBitmap.getHeight());
                Bitmap fgBitmap = BitmapFactory.decodeStream(context.get().getAssets().open("image/gif_player.png")).copy(Bitmap.Config.ARGB_8888, true);

                // XLog.d("宽度：" + shortSide + ", " + fgBitmap.getWidth());
                // if (shortSide <= fgBitmap.getWidth()) {
                //     bgBitmap.recycle();
                //     fgBitmap.recycle();
                //     onMergeListener.onSuccess();
                //     return;
                // }
                Bitmap newBitmap = mergeBitmap(bgBitmap, fgBitmap);
                // XLog.d("新图片：" + newBitmap.getWidth());
                //将合并后的bitmap3保存为png图片到本地
                FileOutputStream out = new FileOutputStream(bgFile.getAbsolutePath());
                newBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                out.close();

                bgBitmap.recycle();
                bgBitmap = null;
                fgBitmap.recycle();
                fgBitmap = null;
                newBitmap.recycle();
                newBitmap = null;
                onMergeListener.onSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                XLog.e("报错：" + e.getMessage());
                onMergeListener.onError(e);
            }
        });
    }


    /**
     * 作者：青青河边踩
     * 链接：https://www.jianshu.com/p/36fe123d973e
     * 合成图片
     */
    public static Bitmap mergeBitmap(Bitmap bgBitmap, Bitmap fgBitmap) {
        // BitmapFactory.Options options = new BitmapFactory.Options();
        // //只读取图片，不加载到内存中
        // options.inJustDecodeBounds = true;
        // // isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
        // // inSampleSize只能是2的次方，如计算结果是7会按4进行压缩，计算结果是15会按8进行压缩。
        // options.inSampleSize = 1;

        //以其中一张图片的大小作为画布的大小，或者也可以自己自定义
        Bitmap newBitmap = Bitmap.createBitmap(bgBitmap);
        //生成画布
        Canvas canvas = new Canvas(newBitmap);

        // 生成画笔
        Paint paint = new Paint();
        int w = bgBitmap.getWidth();
        int h = bgBitmap.getHeight();
        int w_2 = fgBitmap.getWidth();
        int h_2 = fgBitmap.getHeight();

        float scale = (w * 0.2f) / w_2;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        fgBitmap = Bitmap.createBitmap(fgBitmap, 0, 0, w_2, h_2, matrix, true);

        // 设置第二张图片的位置
        canvas.drawBitmap(fgBitmap, (w - fgBitmap.getWidth()) / 2, (h - fgBitmap.getHeight()) / 2, paint);
        canvas.save(); // Canvas.ALL_SAVE_FLAG
        // bgBitmap.recycle();
        // bgBitmap = null;
        // fgBitmap.recycle();
        // fgBitmap = null;
        // 存储新合成的图片
        canvas.restore();
        return newBitmap;
    }

    public static String getImageType(File srcFilePath) {
        FileInputStream imgFile;
        byte[] b = new byte[10];
        int l = -1;
        try {
            imgFile = new FileInputStream(srcFilePath);
            l = imgFile.read(b);
            imgFile.close();
        } catch (Exception e) {
            return null;
        }
        if (l == 10) {
            byte b0 = b[0];
            byte b1 = b[1];
            byte b2 = b[2];
            byte b3 = b[3];
            byte b6 = b[6];
            byte b7 = b[7];
            byte b8 = b[8];
            byte b9 = b[9];
            if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F') {
                return "gif";
            } else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G') {
                return "png";
            } else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I' && b9 == (byte) 'F') {
                return "jpg";
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static ImgFileType getImgType(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            byte[] src = new byte[28];
            is.read(src, 0, 28);
            StringBuilder stringBuilder = new StringBuilder("");
            for (byte b : src) {
                int v = b & 0xFF;
                String hv = Integer.toHexString(v).toUpperCase();
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv);
            }
            String fileHeader = stringBuilder.toString();

            ImgFileType[] imgTypes = ImgFileType.values();
            for (ImgFileType imgType : imgTypes) {
                if (fileHeader.startsWith(imgType.getValue())) {
                    return imgType;
                }
            }
            return null;
        }catch (IOException e){
            return null;
        }
    }
}
