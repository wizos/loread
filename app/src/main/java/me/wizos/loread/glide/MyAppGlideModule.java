/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-03-14 12:21:06
 */

package me.wizos.loread.glide;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.elvishew.xlog.XLog;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import me.wizos.loread.network.HttpClientManager;

/**
 * Created by Wizos on 2019/4/21.
 */

@GlideModule
public class MyAppGlideModule extends AppGlideModule {
    /**
     * 是否启用基于 Manifest 的 GlideModule，如果没有在 Manifest 中声明 GlideModule，可以通过返回 false 禁用
     */
    @Override
    public boolean isManifestParsingEnabled() {
        XLog.d("不使用 Manifest 中的 GlideModule");
        return false;
    }
    /**
     * 注册指定类型的源数据，并指定它的图片加载所使用的 ModelLoader
     */
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        XLog.d("注册指定类型的源数据，并指定它的图片加载所使用的 ModelLoader");
        // 在 Glide 中使用 OkHttp
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(HttpClientManager.i().imageHttpClient()));
    }
    /**
     * 配置图片缓存的路径和缓存空间的大小
     */
    @Override
    public void applyOptions(@NotNull Context context, GlideBuilder builder) {
        XLog.d("配置图片缓存的路径和缓存空间的大小");
        //int diskCacheSizeBytes = 1024 * 1024 * 100;//  100 MB = 104857600, 1GB = 1073741824
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, 1073741824)).setLogLevel(Log.ERROR);
    }
}
