package me.wizos.loread.view.webview;

import android.content.res.AssetManager;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import me.wizos.loread.App;


public class AdBlock {
    private static AdBlock adBlock;
    private static final String FILE = "hosts.txt";
    private static final Set<String> hosts = new HashSet<>();
    private static final Locale locale = Locale.getDefault();


    private AdBlock() {
    }

    public static AdBlock i() {
        if (adBlock == null) {
            synchronized (AdBlock.class) {
                if (adBlock == null) {
                    adBlock = new AdBlock();
                    loadHosts();
                }
            }
        }
        return adBlock;
    }


    private static void loadHosts() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                AssetManager manager = App.i().getAssets();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(manager.open(FILE)));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        hosts.add(line.toLowerCase(locale));
                    }
                } catch (IOException i) {
                }
            }
        });
    }


    private static String getDomain(String url) throws URISyntaxException {
        url = url.toLowerCase(locale);

        int index = url.indexOf('/', 8); // -> http://(7) and https://(8)
        if (index != -1) {
            url = url.substring(0, index);
        }

        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain == null) {
            return url;
        }
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }


    public boolean isAd(String url) {
        String domain;
        try {
            domain = getDomain(url);
        } catch (URISyntaxException u) {
            return false;
        }
        return hosts.contains(domain.toLowerCase(locale));
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
