package me.wizos.loread.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
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
public class ImageUtil {
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

    public static void mergeBitmap(Context context, final File bgFile, final OnMergeListener onMergeListener) {
        mergeBitmap(new WeakReference<Context>(context), bgFile, onMergeListener);
    }

    public static void mergeBitmap(final WeakReference<Context> context, final File bgFile, final OnMergeListener onMergeListener) {
        if (!bgFile.getAbsolutePath().toLowerCase().endsWith(".gif") || !bgFile.getAbsolutePath().toLowerCase().contains("/compressed/")) {
            onMergeListener.onSuccess();
            return;
        }
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream fis1 = new FileInputStream(bgFile);
                    Bitmap bgBitmap = BitmapFactory.decodeStream(fis1).copy(Bitmap.Config.ARGB_8888, true);

                    int shortSide = Math.min(bgBitmap.getWidth(), bgBitmap.getHeight());
                    Bitmap fgBitmap = BitmapFactory.decodeStream(context.get().getAssets().open("image/gif_player.png")).copy(Bitmap.Config.ARGB_8888, true);

                    if (shortSide < fgBitmap.getWidth()) {
                        onMergeListener.onSuccess();
                        return;
                    }
                    Bitmap newBitmap = ImageUtil.mergeBitmap(bgBitmap, fgBitmap);

                    //将合并后的bitmap3保存为png图片到本地
                    FileOutputStream out = new FileOutputStream(bgFile.getAbsolutePath());
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

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
                    XLog.e("报错");
                    onMergeListener.onError(e);
                }
            }
        });
    }


    /**
     * 作者：青青河边踩
     * 链接：https://www.jianshu.com/p/36fe123d973e
     * 合成图片
     */
    public static Bitmap mergeBitmap(Bitmap bgBitmap, Bitmap fgBitmap) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        //只读取图片，不加载到内存中
//        options.inJustDecodeBounds = true;
//        // isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
//        // inSampleSize只能是2的次方，如计算结果是7会按4进行压缩，计算结果是15会按8进行压缩。
//        options.inSampleSize = 1;

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
//        bgBitmap.recycle();
//        bgBitmap = null;
//        fgBitmap.recycle();
//        fgBitmap = null;
        // 存储新合成的图片
        canvas.restore();
        return newBitmap;
    }


    public static void genPic(final File file, final File fileNew) {
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                    FileInputStream fileInputStream = new FileInputStream(App.i().getExUserFilesDir() + "/compressed/pic.jpg");

                    Bitmap newBitmap = getThumbnail(file, 1080);

                    //将合并后的bitmap3保存为png图片到本地
                    FileOutputStream out = new FileOutputStream(fileNew);
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    XLog.e("成功获取 getThumbnail");
                } catch (Exception e) {
                    XLog.e("报错");
                    e.printStackTrace();
                }
            }
        });
    }
//        作者：呵呵瓤儿
//        链接：https://www.jianshu.com/p/cc61ea00f768
//        來源：简书
//        简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。

    /**
     * 这样得到的图片会比原图体积大3倍
     *
     * @param file
     * @param screenWidth
     * @return
     * @throws IOException
     */
    public static Bitmap getThumbnail(File file, int screenWidth) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //只读取图片，不加载到内存中
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;

        BitmapFactory.decodeStream(new FileInputStream(file), null, options);
        int imgWidth = options.outWidth;
        int imgHeight = options.outHeight;

        XLog.e("宽高1=" + imgWidth + "  " + imgHeight);
        // 将长宽变为偶数
        imgWidth = imgWidth % 2 == 1 ? imgWidth + 1 : imgWidth;
        imgHeight = imgHeight % 2 == 1 ? imgHeight + 1 : imgHeight;
        screenWidth = screenWidth % 2 == 1 ? screenWidth + 1 : screenWidth;

        // 如果图片宽度大于屏幕宽度，则生成缩略图
        if (imgWidth > screenWidth) {
            imgHeight = (int) (((float) screenWidth) / ((float) imgWidth) * imgHeight);
            imgWidth = screenWidth;
        }
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getAbsolutePath()), imgWidth, imgHeight);
    }

    /**
     * 未检验过
     */
//    作者：dream_monkey
//    原文：https://blog.csdn.net/dream_monkey/article/details/51461622
    public static Bitmap adjustImage(String absolutePath, int screenWidth, int screenHeight) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        // 这个isjustdecodebounds很重要
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absolutePath, opt);

        // 获取到这个图片的原始宽度和高度
        int picWidth = opt.outWidth;
//        int picHeight = opt.outHeight;

        // 将长宽变为偶数
        picWidth = picWidth % 2 == 1 ? picWidth + 1 : picWidth;
        screenWidth = screenWidth % 2 == 1 ? screenWidth + 1 : screenWidth;
//        picHeight = picHeight % 2 == 1 ? picHeight + 1 : picHeight;
//        screenHeight = screenHeight % 2 == 1 ? screenHeight + 1 : screenHeight;

        // isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
        // inSampleSize只能是2的次方，如计算结果是7会按4进行压缩，计算结果是15会按8进行压缩。
        opt.inSampleSize = 1;
        // 根据屏的大小和图片大小计算出缩放比例
        if (picWidth > screenWidth)
            opt.inSampleSize = picWidth / screenWidth;

//        if (picWidth > picHeight) {
//            if (picWidth > screenWidth)
//                opt.inSampleSize = picWidth / screenWidth;
//        } else {
//            if (picHeight > screenHeight)
//                opt.inSampleSize = picHeight / screenHeight;
//        }

        // 这次再真正地生成一个有像素的，经过缩放了的bitmap
        opt.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(absolutePath, opt);
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

    /**
     * 常见的图片格式以及SVG
     */
    public static boolean isImgOrSvg(File file) {
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
            ImgFileType[] fileTypes = ImgFileType.values();
            for (ImgFileType fileType : fileTypes) {
                if (stringBuilder.toString().startsWith(fileType.getValue())) {
                    return true;
                }
            }
        }catch (IOException e){
            return false;
        }
        return false;
    }
}
