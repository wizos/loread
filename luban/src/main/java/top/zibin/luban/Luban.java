package top.zibin.luban;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class Luban implements Handler.Callback {
    private static final String TAG = "Luban";
    private static final String DEFAULT_DISK_CACHE_DIR = "luban_disk_cache";

    private static final int MSG_COMPRESS_UNCHANGE = 3;
    private static final int MSG_COMPRESS_SUCCESS = 0;
    private static final int MSG_COMPRESS_START = 1;
    private static final int MSG_COMPRESS_ERROR = 2;

    private int maxWidth;
    private int maxHeight;
    private String mTargetDir;
    private String mTargetPath; // 自加
    private boolean focusAlpha;
    private int mLeastCompressSize;
    private OnRenameListener mRenameListener;
    private OnCompressListener mCompressListener;
    private CompressionPredicate mCompressionPredicate;
    private List<InputStreamProvider> mStreamProviders;

    private Handler mHandler;

    private Luban(Builder builder) {
        // 自加
        this.maxWidth = builder.maxWidth;
        this.maxHeight = builder.maxHeight;
        this.mTargetPath = builder.mTargetPath;

        this.mTargetDir = builder.mTargetDir;
        this.mRenameListener = builder.mRenameListener;
        this.mStreamProviders = builder.mStreamProviders;
        this.mCompressListener = builder.mCompressListener;
        this.mLeastCompressSize = builder.mLeastCompressSize;
        this.mCompressionPredicate = builder.mCompressionPredicate;
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    /**
     * Returns a file with a cache image name in the private cache directory.
     *
     * @param context A context.
     */
    private File getImageCacheFile(Context context, String suffix) {
        if (TextUtils.isEmpty(mTargetDir)) {
            mTargetDir = getImageCacheDir(context).getAbsolutePath();
        }

        String cacheBuilder = mTargetDir + "/" +
                System.currentTimeMillis() +
                (int) (Math.random() * 1000) +
                (TextUtils.isEmpty(suffix) ? ".jpg" : suffix);

//        System.out.print("当前的0：" + mTargetDir + "   " + cacheBuilder);
        return new File(cacheBuilder);
    }

    // 自加
    private File getImageCacheFile(Context context, InputStreamProvider input, String suffix) {
        if (TextUtils.isEmpty(mTargetDir)) {
            mTargetDir = getImageCacheDir(context).getAbsolutePath();
        }

        String cacheBuilder = mTargetDir + "/" +
                md5(input.getPath()) +
                (TextUtils.isEmpty(suffix) ? ".jpg" : suffix);

        //System.out.print("当前的1：" + mTargetDir + "   " + cacheBuilder + "   " + input.getPath() + "   "  + suffix );
        return new File(cacheBuilder);
    }

    private File getImageCacheFile(Context context, InputStreamProvider input, String suffix, String mTargetPath) {
        if (TextUtils.isEmpty(mTargetPath)) {
            return getImageCacheFile(context, input, suffix);
        }
        //System.out.print("当前的2：" + mTargetDir + "  --  " + suffix + "  == " + mTargetPath);
        return new File(mTargetPath);
    }

    /**
     * 将字符串转成MD5值
     *
     * @param string 字符串
     * @return MD5 后的字符串
     */
    private String md5(String string) {
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
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

//  public String getPreSavePath(){
//    getImageCacheFile(context, Checker.SINGLE.extSuffix(input));
//  }


    private File getImageCustomFile(Context context, String filename) {
        if (TextUtils.isEmpty(mTargetDir)) {
            mTargetDir = getImageCacheDir(context).getAbsolutePath();
        }

        String cacheBuilder = mTargetDir + "/" + filename;

        return new File(cacheBuilder);
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store retrieved audio.
     *
     * @param context A context.
     * @see #getImageCacheDir(Context, String)
     */
    private File getImageCacheDir(Context context) {
        return getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context   A context.
     * @param cacheName The name of the subdirectory in which to store the cache.
     * @see #getImageCacheDir(Context)
     */
    private static File getImageCacheDir(Context context, String cacheName) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    /**
     * start asynchronous compress thread
     */
    private void launch(final Context context) {
        if (mStreamProviders == null || mStreamProviders.size() == 0 && mCompressListener != null) {
            mCompressListener.onError(new NullPointerException("image file cannot be null"));
        }

        Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();

        while (iterator.hasNext()) {
            final InputStreamProvider path = iterator.next();

            AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_START));
                        FileProvider result = compress(context, path);
                        //Log.e("返回结果" , result.hasCompressed + "" );
                        if( result.hasCompressed ){
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_SUCCESS, result.file));
                        }else {
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_UNCHANGE, result.file));
                        }
                    } catch (IOException e) {
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_ERROR, e));
                    }
                }
            });

            iterator.remove();
        }
    }

    /**
     * start compress and return the file
     */
    private File get(InputStreamProvider input, Context context) throws IOException {
        return new Engine(input, getImageCacheFile(context, input, Checker.SINGLE.extSuffix(input)),maxWidth,maxHeight, focusAlpha).compress();
//    return new Engine(input, getImageCacheFile(context,input, Checker.SINGLE.extSuffix(input),mTargetPath), focusAlpha).compress();
    }

    private List<File> get(Context context) throws IOException {
        List<File> results = new ArrayList<>();
        Iterator<InputStreamProvider> iterator = mStreamProviders.iterator();

        while (iterator.hasNext()) {
            results.add(compress(context, iterator.next()).file);
            iterator.remove();
        }

        return results;
    }

    /**
     * 自己新增加的函数
     * @param context
     * @param path
     * @return
     * @throws IOException
     */
    private FileProvider compress(Context context, InputStreamProvider path) throws IOException {
        FileProvider fileProvider = new FileProvider();

        File outFile = getImageCacheFile(context, path, Checker.SINGLE.extSuffix(path));
        if (mRenameListener != null) {
            String filename = mRenameListener.rename(path.getPath());
            outFile = getImageCustomFile(context, filename); // 此时还是空文件
        }

        if (mCompressionPredicate != null) {
            if ( mCompressionPredicate.apply(path.getPath(),path) && Checker.SINGLE.needCompress(mLeastCompressSize, path.getPath())  ) {
                fileProvider.file = new Engine(path, outFile,maxWidth,maxHeight, focusAlpha).compress();
                fileProvider.hasCompressed = true;
                //Log.e("检测：","要压缩，" + mCompressionPredicate.apply(path.getPath(),path)  + Checker.SINGLE.needCompress(mLeastCompressSize, path.getPath()));
            } else {
                fileProvider.file = new File(path.getPath());
                fileProvider.hasCompressed = false;
                //Log.e("检测：","不要压缩，" + mCompressionPredicate.apply(path.getPath(),path)  + Checker.SINGLE.needCompress(mLeastCompressSize, path.getPath()) );
            }
        } else {
            if( Checker.SINGLE.needCompress(mLeastCompressSize, path.getPath()) ){
                fileProvider.file = new Engine(path, outFile, maxWidth, maxHeight, focusAlpha).compress();
                fileProvider.hasCompressed = true;
            }else {
                fileProvider.file = new File(path.getPath());
                fileProvider.hasCompressed = false;
            }
        }

        return fileProvider;
    }

//    private File compress(Context context, InputStreamProvider path) throws IOException {
//        File result;
//
//        File outFile = getImageCacheFile(context, path, Checker.SINGLE.extSuffix(path));
////    File outFile = getImageCacheFile(context,path, Checker.SINGLE.extSuffix(path),mTargetPath);
//
//        if (mRenameListener != null) {
//            String filename = mRenameListener.rename(path.getPath());
//            outFile = getImageCustomFile(context, filename);
//        }
//
//
//        if (mCompressionPredicate != null) {
//            if ( mCompressionPredicate.apply(path.getPath()) && Checker.SINGLE.needCompress(mLeastCompressSize, path.getPath()) ) {
//                result = new Engine(path, outFile, focusAlpha).compress();
//            } else {
//                result = new File(path.getPath());
//            }
//        } else {
//            result = Checker.SINGLE.needCompress(mLeastCompressSize, path.getPath()) ?
//                    new Engine(path, outFile, focusAlpha).compress() :
//                    new File(path.getPath());
//        }
//
//        return result;
//    }

    @Override
    public boolean handleMessage(Message msg) {
        if (mCompressListener == null) return false;

        switch (msg.what) {
            case MSG_COMPRESS_START:
                mCompressListener.onStart();
                break;
            case MSG_COMPRESS_SUCCESS:
                mCompressListener.onSuccess((File) msg.obj);
                break;
            case MSG_COMPRESS_UNCHANGE:
                mCompressListener.onUnChange((File) msg.obj);
                break;
            case MSG_COMPRESS_ERROR:
                mCompressListener.onError((Throwable) msg.obj);
                break;
        }
        return false;
    }

    public static class Builder {
        private Context context;
        private String mTargetDir;
        private int maxWidth;
        private int maxHeight;
        private boolean focusAlpha;
        private int mLeastCompressSize = 100;
        private OnRenameListener mRenameListener;
        private OnCompressListener mCompressListener;
        private CompressionPredicate mCompressionPredicate;
        private List<InputStreamProvider> mStreamProviders;

        Builder(Context context) {
            this.context = context;
            this.mStreamProviders = new ArrayList<>();
        }

        private Luban build() {
            return new Luban(this);
        }

        public Builder load(InputStreamProvider inputStreamProvider) {
            mStreamProviders.add(inputStreamProvider);
            return this;
        }

        public Builder load(final File file) {
            mStreamProviders.add(new InputStreamProvider() {
                @Override
                public InputStream open() throws IOException {
                    return new FileInputStream(file);
                }

                @Override
                public String getPath() {
                    return file.getAbsolutePath();
                }
            });
            return this;
        }


        private String mTargetPath;

        public Builder setTargetPath(String mTargetPath) {
            this.mTargetPath = mTargetPath;
            return this;
        }

        public Builder load(final String string) {
            mStreamProviders.add(new InputStreamProvider() {
                @Override
                public InputStream open() throws IOException {
                    return new FileInputStream(string);
                }

                @Override
                public String getPath() {
                    return string;
                }
            });
            return this;
        }

        public <T> Builder load(List<T> list) {
            for (T src : list) {
                if (src instanceof String) {
                    load((String) src);
                } else if (src instanceof File) {
                    load((File) src);
                } else if (src instanceof Uri) {
                    load((Uri) src);
                } else {
                    throw new IllegalArgumentException("Incoming data type exception, it must be String, File, Uri or Bitmap");
                }
            }
            return this;
        }

        public Builder load(final Uri uri) {
            mStreamProviders.add(new InputStreamProvider() {
                @Override
                public InputStream open() throws IOException {
                    return context.getContentResolver().openInputStream(uri);
                }

                @Override
                public String getPath() {
                    return uri.getPath();
                }
            });
            return this;
        }

        public Builder putGear(int gear) {
            return this;
        }

        public Builder setRenameListener(OnRenameListener listener) {
            this.mRenameListener = listener;
            return this;
        }

        public Builder setCompressListener(OnCompressListener listener) {
            this.mCompressListener = listener;
            return this;
        }

        public Builder setTargetDir(String targetDir) {
            this.mTargetDir = targetDir;
            return this;
        }

        /**
         * 自己添加的，用于控制压缩出来的图片的尺寸大小
         * @param maxWidth
         * @return
         */
        public Builder setMaxSiz(int maxWidth, int maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            return this;
        }

        /**
         * Do I need to keep the image's alpha channel
         *
         * @param focusAlpha <p> true - to keep alpha channel, the compress speed will be slow. </p>
         *                   <p> false - don't keep alpha channel, it might have a black background.</p>
         */
        public Builder setFocusAlpha(boolean focusAlpha) {
            this.focusAlpha = focusAlpha;
            return this;
        }

        /**
         * do not compress when the origin image file size less than one value
         *
         * @param size the value of file size, unit KB, default 100K
         */
        public Builder ignoreBy(int size) {
            this.mLeastCompressSize = size;
            return this;
        }

        /**
         * do compress image when return value was true, otherwise, do not compress the image file
         *
         * @param compressionPredicate A predicate callback that returns true or false for the given input path should be compressed.
         */
        public Builder filter(CompressionPredicate compressionPredicate) {
            this.mCompressionPredicate = compressionPredicate;
            return this;
        }


        /**
         * begin compress image with asynchronous
         */
        public void launch() {
            build().launch(context);
        }

        public File get(final String path) throws IOException {
            return build().get(new InputStreamProvider() {
                @Override
                public InputStream open() throws IOException {
                    return new FileInputStream(path);
                }

                @Override
                public String getPath() {
                    return path;
                }
            }, context);
        }

        /**
         * begin compress image with synchronize
         *
         * @return the thumb image file list
         */
        public List<File> get() throws IOException {
            return build().get(context);
        }
    }
}