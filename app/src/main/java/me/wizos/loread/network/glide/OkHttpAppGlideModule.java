package me.wizos.loread.network.glide;

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

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import me.wizos.loread.network.HttpClientManager;

/**
 * Created by Wizos on 2019/4/21.
 */

@GlideModule
public class OkHttpAppGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(HttpClientManager.i().imageHttpClient()));
    }

    @Override
    public void applyOptions(@NotNull Context context, GlideBuilder builder) {
        //int diskCacheSizeBytes = 1024 * 1024 * 100;//  100 MB = 104857600, 1GB = 1073741824
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, 1073741824)).setLogLevel(Log.ERROR);
    }
}
