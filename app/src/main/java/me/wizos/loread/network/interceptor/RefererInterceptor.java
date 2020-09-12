//package me.wizos.loread.network.interceptor;
//
//import androidx.annotation.NonNull;
//
//import com.socks.library.KLog;
//
//import java.io.IOException;
//
//import me.wizos.loread.config.RefererRule;
//import me.wizos.loread.utils.StringUtils;
//import okhttp3.Interceptor;
//import okhttp3.Request;
//import okhttp3.Response;
//
///**
// * Referer 拦截器
// * 用于给指定网站增加referer
// *
// * @author Wizos
// * @version 1.0
// * @date 2019/4/2
// */
//
//public class RefererInterceptor implements Interceptor {
//    @Override
//    @NonNull
//    public Response intercept(Chain chain) throws IOException {
//        Request request = chain.request();
//        String referer = RefererRule.i().guessRefererByUrl(request.url().toString());
//        if (!StringUtils.isEmpty(referer)) {
//            request = request.newBuilder().header("referer", referer).build();
//        }
//        KLog.i("拦截到的referer：" + request.url().toString() + " , " + referer );
//        return chain.proceed(request);
//    }
//}
