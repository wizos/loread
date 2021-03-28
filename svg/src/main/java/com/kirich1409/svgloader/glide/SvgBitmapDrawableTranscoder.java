/*
 * Copyright 2019 Kirill Rozov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kirich1409.svgloader.glide;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.bitmap.LazyBitmapDrawableResource;
import com.bumptech.glide.load.resource.gif.GifOptions;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.caverock.androidsvg.SVG;
import com.kirich1409.svgloader.glide.utils.BitmapProvider;
import com.kirich1409.svgloader.glide.utils.SvgUtils;

@RestrictTo(RestrictTo.Scope.LIBRARY)
final class SvgBitmapDrawableTranscoder implements ResourceTranscoder<SVG, BitmapDrawable> {

    private final BitmapPool mBitmapPool;
    private final Resources mResources;
    private final BitmapProvider mBitmapProvider;

    SvgBitmapDrawableTranscoder(@NonNull Context context, @NonNull Glide glide) {
        mResources = context.getResources();
        mBitmapPool = glide.getBitmapPool();
        mBitmapProvider = new PoolBitmapProvider(mBitmapPool);
    }

    @Override
    public Resource<BitmapDrawable> transcode(
            @NonNull Resource<SVG> toTranscode, @Nullable Options options) {
        prepareSvg(toTranscode, options);
        Bitmap bitmap = SvgUtils.toBitmap(toTranscode.get(), mBitmapProvider, getDecodeFormat(options));
        return LazyBitmapDrawableResource.obtain(mResources, new BitmapResource(bitmap, mBitmapPool));
    }

    @NonNull
    private Bitmap.Config getDecodeFormat(@Nullable Options options) {
        DecodeFormat decodeFormat = options == null ? null : options.get(GifOptions.DECODE_FORMAT);
        if (decodeFormat == null) {
            return Bitmap.Config.ARGB_8888;
        }

        switch (decodeFormat) {
            case PREFER_RGB_565:
                return Bitmap.Config.RGB_565;

            case PREFER_ARGB_8888:
            default:
                return Bitmap.Config.ARGB_8888;
        }
    }

    private void prepareSvg(@NonNull Resource<SVG> toTranscode, @Nullable Options options) {
        if (!(toTranscode instanceof SvgResource)) {
            return;
        }

        DownsampleStrategy strategy =
                options == null ? null : options.get(DownsampleStrategy.OPTION);
        if (strategy != null) {
            float scaleFactor = strategy.getScaleFactor(
                    Math.round(toTranscode.get().getDocumentWidth()),
                    Math.round(toTranscode.get().getDocumentHeight()),
                    ((SvgResource) toTranscode).getWidth(),
                    ((SvgResource) toTranscode).getHeight()
            );
            SvgUtils.scaleDocumentSize(toTranscode.get(), scaleFactor);
        }
    }

    private static final class PoolBitmapProvider implements BitmapProvider {

        private final BitmapPool mBitmapPool;

        PoolBitmapProvider(@NonNull BitmapPool bitmapPool) {
            mBitmapPool = bitmapPool;
        }

        @NonNull
        @Override
        public Bitmap get(int width, int height, @NonNull Bitmap.Config config) {
            return mBitmapPool.get(width, height, config);
        }
    }
}
