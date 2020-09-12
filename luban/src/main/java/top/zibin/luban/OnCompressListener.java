package top.zibin.luban;

import java.io.File;

public interface OnCompressListener {

    /**
     * Fired when the compression is started, override to handle in your own code
     */
    void onStart();

    /**
     * Fired when a compression returns successfully, override to handle in your own code
     */
    void onSuccess(File file);

    /**
     * 自己增加的
     * 条件不符合，最后没有压缩
     */
    void onUnChange(File file);
    /**
     * Fired when a compression fails to complete, override to handle in your own code
     */
    void onError(Throwable e);
}
