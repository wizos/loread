package me.wizos.loread.log;

import android.content.Context;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.ClassicFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;

import me.wizos.loread.Contract;

public class CoreLog {
    public static void init(Context context, boolean allLogging){
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(allLogging ? LogLevel.ALL: LogLevel.INFO)
                .tag(Contract.LOREAD)
                .enableStackTrace(1)
                .stackTraceFormatter(new SingleStackTraceFormatter())
                .build();

        Printer androidPrinter = new AndroidPrinter();
        Printer filePrinter = new FilePrinter
                .Builder( context.getExternalCacheDir()  +"/log/") // 指定保存日志文件的路径
                .flattener(new ClassicFlattener())
                .fileNameGenerator(new DateFileNameGenerator())    // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
                // .backupStrategy(new NeverBackupStrategy())         // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
                // .shouldBackup()
                .cleanStrategy(new FileLastModifiedCleanStrategy(14*24*60*1000))     // 指定日志文件清除策略，默认为 NeverCleanStrategy()
                .build();
        // 初始化 XLog
        XLog.init(config, androidPrinter, filePrinter);
    }
}
