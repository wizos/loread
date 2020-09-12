package me.wizos.loread.config;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;

import com.socks.library.KLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import me.wizos.loread.App;


public class AdBlock {
    private transient static AdBlock instance;
    private static final String FILE = "ad_block.txt";
    private static final Set<String> hosts = new HashSet<>();
    @SuppressLint("ConstantLocale")
    private static final Locale locale = Locale.getDefault();

    private AdBlock() {}
    public static AdBlock i() {
        if (instance == null) {
            synchronized (AdBlock.class) {
                if (instance == null) {
                    instance = new AdBlock();
                    loadHosts();
                }
            }
        }
        return instance;
    }
    public void reset() {
        instance = null;
    }


    private static void loadHosts() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if( !new File(App.i().getGlobalAssetsFilesDir() + FILE).exists() ){
                        return;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(App.i().getGlobalConfigPath() + "ad_block.txt")));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        hosts.add(line.toLowerCase(locale));
                    }
                } catch (IOException i) {
                    i.printStackTrace();
                    KLog.i(i);
                }
            }
        });
    }


    private static String getDomain(String url) {
        Uri uri = Uri.parse(url);
        String domain = uri.getHost();

        if (domain == null) {
            url = url.toLowerCase(locale);
            int index = url.indexOf('/', 8); // -> http://(7) and https://(8)
            if (index != -1) {
                url = url.substring(0, index);
            }
            return url;
        }
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }


    public boolean isAd(String url) {
        return hosts.contains(getDomain(url).toLowerCase(locale));
    }


//    public boolean isWhite(String url) {
//        for (String domain : whitelist) {
//            if (url.contains(domain)) {
//                return true;
//            }
//        }
//        return false;
//    }

//    private synchronized static void loadDomains(Context context) {
//        RecordAction action = new RecordAction(context);
//        action.open(false);
//        whitelist.clear();
//        for (String domain : action.listDomains()) {
//            whitelist.add(domain);
//        }
//        action.close();
//    }
//    public synchronized void addDomain(String domain) {
//        RecordAction action = new RecordAction(context);
//        action.open(true);
//        action.addDomain(domain);
//        action.close();
//        whitelist.add(domain);
//    }
//    public synchronized void removeDomain(String domain) {
//        RecordAction action = new RecordAction(context);
//        action.open(true);
//        action.deleteDomain(domain);
//        action.close();
//        whitelist.remove(domain);
//    }
//    public synchronized void clearDomains() {
//        RecordAction action = new RecordAction(context);
//        action.open(true);
//        action.clearDomains();
//        action.close();
//        whitelist.clear();
//    }
}
