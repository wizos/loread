package me.wizos.loread.network.interceptor;

import com.elvishew.xlog.XLog;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import me.wizos.loread.App;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.User;
import me.wizos.loread.utils.StringUtils;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
* 自动刷新token的拦截器
* https://www.jianshu.com/p/62ab11ddacc8
*
* @author Wizos
* @version 1.0
* @date 2019/4/2
*/

public class InoReaderTokenInterceptor implements Interceptor {
   private static final Charset UTF8 = StandardCharsets.UTF_8;

   @Override
   public Response intercept(Chain chain) throws IOException {
       Request request = chain.request();
       Response originalResponse = chain.proceed(request);

       /**通过如下的办法曲线取到请求完成的数据
        *
        * 原本想通过  originalResponse.body().string()
        * 去取到请求完成的数据,但是一直报错,不知道是okhttp的bug还是操作不当
        *
        * 然后去看了okhttp的源码,找到了这个曲线方法,取到请求完成的数据后,根据特定的判断条件去判断token过期
        */
       ResponseBody responseBody = originalResponse.body();
       BufferedSource source = responseBody.source();
       source.request(Long.MAX_VALUE); // Buffer the entire body.
       Buffer buffer = source.buffer();
       Charset charset = UTF8;
       MediaType contentType = responseBody.contentType();
       if (contentType != null) {
           charset = contentType.charset(UTF8);
       }
       String bodyString = buffer.clone().readString(charset);

       if (originalResponse.code() == 403 && !StringUtils.isEmpty(bodyString) && (bodyString.contains("AppId required") || bodyString.contains("AppId not") )) {
           String refreshToken = App.i().getUser().getRefreshToken();

           // 通过一个特定的接口获取新的token，此处要用到同步的retrofit请求
           String authorization = App.i().getOAuthApi().refreshingAccessToken(refreshToken);

           // 创建一个新请求，并使用新令牌相应地修改它
           Request newRequest = request.newBuilder()
                   .header("authorization", authorization)
                   .build();

           User user = App.i().getUser();
           if(user != null){
               user.setAuth(authorization);
               CoreDB.i().userDao().update(user);
           }
           // 重试请求
           originalResponse.body().close();
           XLog.i("TokenInterceptor授权过期：刷新码 " + refreshToken + "，授权码 " + authorization);
           return chain.proceed(newRequest);

       }
       // 否则，只需传递原始响应
       return originalResponse;
   }
}
