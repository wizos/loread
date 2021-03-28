package me.wizos.loread.network;

import com.elvishew.xlog.XLog;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class StringConverterFactory extends Converter.Factory {
    //工厂方法，用于创建实例
    public static StringConverterFactory create() {
        return new StringConverterFactory();
    }

    //response返回到本地后会被调用，这里先判断是否要拦截处理，不拦截则返回null
    // 判断是否处理的依据就是type参数，type就是上面接口出现的List了
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        XLog.i("响应的类型：" + type);
        if (type == String.class) {
            return new StringBodyConverter<Type>();
        }
        //如果返回null则不处理，交给别的Converter处理
        return null;
    }

    // 一个Converter类，T就是上面接口中的List了
    private static class StringBodyConverter<T> implements Converter<ResponseBody, T> {
        StringBodyConverter() {}
        //在这个方法中处理response
        @Override
        public T convert(ResponseBody value) throws IOException {
            return (T) value.string();
        }
    }
}
