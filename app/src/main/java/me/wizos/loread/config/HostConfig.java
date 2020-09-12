//package me.wizos.loread.config;
//
//import android.net.Uri;
//import android.text.TextUtils;
//import android.util.ArrayMap;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.reflect.TypeToken;
//
//import me.wizos.loread.App;
//import me.wizos.loread.utils.FileUtil;
//
///**
// * @author Wizos on 2020/4/14.
// */
//public class HostConfig {
//    private HostConfig() { }
//    public static HostConfig i() {
//        if (instance == null) {
//            synchronized (HostConfig.class) {
//                if (instance == null) {
//                    Gson gson = new Gson();
//                    instance = new HostConfig();
//
//                    String config = FileUtil.readFile(App.i().getGlobalConfigPath() + "host_rewrite.json");
//                    if (TextUtils.isEmpty(config)) {
//                        instance.domainRewrite = new ArrayMap<>();
//                    } else {
//                        instance.domainRewrite = gson.fromJson(config, new TypeToken<ArrayMap<String,String>>() {}.getType());
//                    }
//                }
//            }
//        }
//        return instance;
//    }
//    public void reset() {
//        instance = null;
//    }
//    public void save() {
//        FileUtil.save(App.i().getGlobalConfigPath() + "host_rewrite.json", new GsonBuilder().setPrettyPrinting().create().toJson(instance.domainRewrite));
//    }
//
//    private static HostConfig instance;
//    private ArrayMap<String, String> domainRewrite;
//
//    //@SerializedName("url_match_domain_rewrite_url")
//
//    public String getRedirectUrl(String url) {
//        Uri uri = Uri.parse(url);
//        String host = uri.getHost();
//        if (domainRewrite.containsKey(host)) {
//            assert host != null;
//            return url.replaceFirst(host, domainRewrite.get(host));
//        }
//        return "";
//    }
//}