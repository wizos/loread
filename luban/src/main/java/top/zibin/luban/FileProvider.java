package top.zibin.luban;

import java.io.File;

/**
 * 自己增加的，为的是最后能在回调中区分文件到底有没有压缩
 * @author Wizos on 2018/12/27.
 */

public class FileProvider {
    public boolean hasCompressed = false;
    public File file;
}
