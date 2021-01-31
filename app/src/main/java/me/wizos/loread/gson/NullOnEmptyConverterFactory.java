package me.wizos.loread.gson;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Retrofit HTTP body返回为空的情况报错 EOFException
 * https://blog.csdn.net/EthanCo/article/details/85689888
 *
 * 遇到HTTP请求code为200，response body为空的情况。
 * 这种情况下，RetrofitGson解析阶段会出现报错。
 * .addConverterFactory(new NullOnEmptyConverterFactory()) //必须是要第一个
 */
public class NullOnEmptyConverterFactory extends Converter.Factory {
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
        return new Converter<ResponseBody, Object>() {
            @Override
            public Object convert(ResponseBody body) throws IOException {
                long contentLength = body.contentLength();
                if (contentLength == 0) {
                    return null;
                }
                return delegate.convert(body);
            }
        };
    }
}
