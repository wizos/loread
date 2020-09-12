//package me.wizos.loreadx.network.interceptor;
//
//import android.text.TextUtils;
//
//import com.socks.library.KLog;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//
//import me.wizos.loreadx.App;
//import okhttp3.Interceptor;
//import okhttp3.MediaType;
//import okhttp3.Request;
//import okhttp3.Response;
//import okhttp3.ResponseBody;
//import okio.Buffer;
//import okio.BufferedSource;
//
///**
// * 自动刷新token的拦截器
// * https://www.jianshu.com/p/62ab11ddacc8
// *
// * @author Wizos
// * @version 1.0
// * @date 2019/4/2
// */
//
//public class TokenInterceptor implements Interceptor {
//    private static final Charset UTF8 = Charset.forName("UTF-8");
//
//    @Override
//    public Response intercept(Chain chain) throws IOException {
//        Request request = chain.request();
//        Response originalResponse = chain.proceed(request);
//
//        /**通过如下的办法曲线取到请求完成的数据
//         *
//         * 原本想通过  originalResponse.body().string()
//         * 去取到请求完成的数据,但是一直报错,不知道是okhttp的bug还是操作不当
//         *
//         * 然后去看了okhttp的源码,找到了这个曲线方法,取到请求完成的数据后,根据特定的判断条件去判断token过期
//         */
//        ResponseBody responseBody = originalResponse.body();
//        BufferedSource source = responseBody.source();
//        source.request(Long.MAX_VALUE); // Buffer the entire body.
//        Buffer buffer = source.buffer();
//        Charset charset = UTF8;
//        MediaType contentType = responseBody.contentType();
//        if (contentType != null) {
//            charset = contentType.charset(UTF8);
//        }
//        String bodyString = buffer.clone().readString(charset);
//
//        if (!TextUtils.isEmpty(bodyString)) {
//            if (bodyString.length() < 22) {
//                KLog.e("body---------->" + bodyString.substring(0, 21));
//            } else {
//                KLog.e("body---------->" + bodyString);
//            }
//        }
//
//        /***************************************/
//
//        //根据和服务端的约定判断token过期
//        if (bodyString.contains("token expired")) {
//            String refreshToken = App.i().getUser().getRefreshToken();
//
//            // 通过一个特定的接口获取新的token，此处要用到同步的retrofit请求
//            String authorization = App.i().getOAuthApi().refreshingAccessToken(refreshToken);
//
//            // 创建一个新请求，并使用新令牌相应地修改它
//            Request newRequest = request.newBuilder()
//                    .header("authorization", authorization)
//                    .build();
//
//            // 重试请求
//            originalResponse.body().close();
//            KLog.e("TokenInterceptor授权过期：刷新码 " + refreshToken + "，授权码 " + authorization);
//            return chain.proceed(newRequest);
//        }
//
//        // 否则，只需传递原始响应
//        return originalResponse;
//    }
//}
