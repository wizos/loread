// package me.wizos.loread.config;
//
// import android.net.Uri;
// import android.text.TextUtils;
// import android.util.ArrayMap;
//
// import com.elvishew.xlog.XLog;
// import com.google.gson.Gson;
// import com.google.gson.GsonBuilder;
// import com.google.gson.reflect.TypeToken;
//
// import java.util.Arrays;
//
// import javax.script.Bindings;
// import javax.script.SimpleBindings;
//
// import me.wizos.loread.App;
// import me.wizos.loread.log.JSLog;
// import me.wizos.loread.utils.FileUtil;
// import me.wizos.loread.utils.HttpCall;
// import me.wizos.loread.utils.ScriptUtil;
// import me.wizos.loread.utils.StringUtils;
//
// /**
//  * @author Wizos on 2020/4/14.
//  */
// public class BigImageConfig {
//     private static final String CONFIG_FILENAME = "big_image.json";
//     private static BigImageConfig instance;
//     private ArrayMap<String, String> urlRewrite;
//
//     private BigImageConfig() { }
//     public static BigImageConfig i() {
//         if (instance == null) {
//             synchronized (BigImageConfig.class) {
//                 if (instance == null) {
//                     instance = new BigImageConfig();
//                     String config = FileUtil.readFile(App.i().getUserConfigPath() + CONFIG_FILENAME);
//                     if (TextUtils.isEmpty(config)) {
//                         instance.urlRewrite = new ArrayMap<>();
//                         instance.save();
//                     } else {
//                         instance.urlRewrite = new Gson().fromJson(config, new TypeToken<ArrayMap<String, String>>() {}.getType());
//                     }
//                 }
//             }
//         }
//         return instance;
//     }
//     public void reset() {
//         instance = null;
//     }
//     public void save() {
//         FileUtil.save(App.i().getUserConfigPath() + CONFIG_FILENAME, new GsonBuilder().setPrettyPrinting().create().toJson(instance.urlRewrite));
//     }
//
//
//     public String getOriginalSrc(String url) {
//         if (TextUtils.isEmpty(url)) {
//             return null;
//         }
//
//         String host = Uri.parse(url).getHost();
//         if (TextUtils.isEmpty(host)) {
//             return null;
//         }
//
//         String[] slices = host.split("\\.");
//         for (int i = 0, size = slices.length; i+1 < size; i++) {
//             host = StringUtils.join(".", Arrays.copyOfRange(slices, i, size));
//             XLog.d("Host 推测：" + host );
//             if (urlRewrite.containsKey(host)) {
//                 // Bindings接口可以理解为上下文，可以往上下文中设置一个Java对象或通过key获取一个对象，它有一个实现类，SimpleBindings，内部就是一个map。
//                 Bindings bindings = new SimpleBindings();
//                 bindings.put("url", url);
//                 bindings.put("call", HttpCall.i());
//                 bindings.put("log", JSLog.i());
//                 ScriptUtil.i().eval(urlRewrite.get(host), bindings);
//                 XLog.d("重定向JS：" + bindings.get("url"));
//                 return (String) bindings.get("url");
//             }
//         }
//         return null;
//     }
// }