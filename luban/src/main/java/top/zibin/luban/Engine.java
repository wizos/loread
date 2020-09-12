package top.zibin.luban;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Responsible for starting compress and managing active and cached resources.
 */
class Engine {
    private InputStreamProvider srcImg;
    private File tagImg;
    private int srcWidth;
    private int srcHeight;
    private int maxWidth = 1080;
    private int maxHeight = 1920;
    private boolean focusAlpha;

    Engine(InputStreamProvider srcImg, File tagImg,int maxWidth, int maxHeight, boolean focusAlpha) throws IOException {
        this.srcImg = srcImg;
        this.tagImg = tagImg;
        this.focusAlpha = focusAlpha;

        BitmapFactory.Options options = new BitmapFactory.Options();
        //只读取图片，不加载到内存中
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;

        BitmapFactory.decodeStream(srcImg.open(), null, options);
        this.srcWidth = options.outWidth;
        this.srcHeight = options.outHeight;
        //Log.e("【宽度】1：",srcWidth + " " + srcHeight);
    }

    private int computeSize() {
        // 将长宽加1，以至于成为偶数
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            } else if (longSide < 4990) {
                return 2;
            } else if (longSide > 4990 && longSide < 10240) {
                return 4;
            } else {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }

    //  旋转
    private Bitmap rotatingImage(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // 缩放
    private Bitmap scalingImage(Bitmap bitmap,float scale) {
        //System.out.print("【宽度比值A】" + maxWidth + "  " + bitmap.getHeight() + "  " + bitmap.getWidth() + "  "+ scale + " \n ");
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix, true);
    }


    Bitmap tagBitmap;
    File compress() throws IOException {
        if (tagImg != null && tagImg.exists()) {
            return tagImg;
        }
        if (!tagImg.getParentFile().exists()) {
            tagImg.getParentFile().mkdirs();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = computeSize();

        tagBitmap = BitmapFactory.decodeStream(srcImg.open(), null, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        if (Checker.SINGLE.isJPG(srcImg.open())) {
            tagBitmap = rotatingImage(tagBitmap, Checker.SINGLE.getOrientation(srcImg.open()));
        }
        int quality = 60;

        float scale = 1f;
        if(tagBitmap == null){
            return tagImg;
        }
        // oldWidth 是经过采样后的宽度
        int oldWidth = tagBitmap.getWidth();
        int oldHeight = tagBitmap.getHeight();

        if ( oldWidth > maxWidth && maxWidth > 0 ) {
            scale = ((float) maxWidth) / oldWidth;
        }
        //Log.e("【宽度比值C】", maxWidth + "  " + oldWidth + " = " + scale + "   ,  " + (((float) maxWidth) / oldWidth) );
        if( oldHeight > maxHeight && maxHeight > 0 ){
            scale = Math.min(scale,((float) maxHeight) / oldHeight );
        }
        //Log.e("【宽度比值D】", maxHeight + "  " + oldHeight + " = " + scale + "   ,  " + ((float) maxHeight) / oldHeight );

        if( scale != 1f ){
            quality = 80;
            tagBitmap = scalingImage(tagBitmap, scale );
        }

        tagBitmap.compress(focusAlpha ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, quality, stream);
        tagBitmap.recycle();
        tagBitmap = null;
        srcImg = null;
        // Bitmap.recycle();释放了图片的资源，但是Bitmap本身并没有释放，它依然在占用资源。
        // 所以还要在调用一次Bitmap=null;将Bitmap赋空，让有向图断掉，好让GC回收。

        FileOutputStream fos = new FileOutputStream(tagImg);
        fos.write(stream.toByteArray());
        fos.flush();
        fos.close();
        stream.close();
        return tagImg;
    }


}