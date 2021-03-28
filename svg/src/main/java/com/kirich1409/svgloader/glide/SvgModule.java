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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.UnitModelLoader;
import com.bumptech.glide.module.LibraryGlideModule;
import com.caverock.androidsvg.SVG;
import com.kirich1409.svgloader.glide.decoder.ByteBufferSvgDecoder;
import com.kirich1409.svgloader.glide.decoder.FileDescriptorSvgDecoder;
import com.kirich1409.svgloader.glide.decoder.FileSvgDecoder;
import com.kirich1409.svgloader.glide.decoder.InputStreamSvgDecoder;
import com.kirich1409.svgloader.glide.decoder.ParcelFileDescriptorSvgDecoder;
import com.kirich1409.svgloader.glide.decoder.RawResourceSvgDecoder;
import com.kirich1409.svgloader.glide.decoder.StringSvgDecoder;
import com.kirich1409.svgloader.glide.decoder.UnitSVGDecoder;

import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Register components for load SVG from different sources and convert it to bitmap.
 */
@GlideModule
public final class SvgModule extends LibraryGlideModule {

    private static final String REGISTRY = "com.kirich1409.svgloader.glide";

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.register(SVG.class, BitmapDrawable.class, new SvgBitmapDrawableTranscoder(context, glide))
                .append(SVG.class, SVG.class, UnitModelLoader.Factory.getInstance())
                .append(String.class, String.class, StringLoader.Factory.getInstance());
        registerDecoders(context, registry);
    }

    private void registerDecoders(@NonNull Context context, @NonNull Registry registry) {
        registry.append(REGISTRY, InputStream.class, SVG.class, new InputStreamSvgDecoder())
                .append(REGISTRY, File.class, SVG.class, new FileSvgDecoder())
                .append(REGISTRY, FileDescriptor.class, SVG.class, new FileDescriptorSvgDecoder())
                .append(REGISTRY, ParcelFileDescriptor.class, SVG.class, new ParcelFileDescriptorSvgDecoder())
                .append(REGISTRY, SVG.class, SVG.class, new UnitSVGDecoder())
                .append(REGISTRY, ByteBuffer.class, SVG.class, new ByteBufferSvgDecoder())
                .append(REGISTRY, String.class, SVG.class, new StringSvgDecoder())
                .append(REGISTRY, Uri.class, SVG.class, new RawResourceSvgDecoder(context));
    }
}
