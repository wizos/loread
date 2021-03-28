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

package com.kirich1409.svgloader.glide.utils;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.annotation.RestrictTo;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class SizeUtils {

    public static int getSize(@NonNull ByteBuffer buffer) {
        return buffer.limit();
    }

    public static int getSize(@NonNull FileDescriptor source) throws IOException {
        try (FileInputStream fis = new FileInputStream(source)) {
            try (FileChannel channel = fis.getChannel()) {
                return (int) channel.size();
            }
        }
    }

    public static int getSize(@NonNull ParcelFileDescriptor source) throws IOException {
        return SizeUtils.getSize(source.getFileDescriptor());
    }

    public static int getSize(@NonNull AssetFileDescriptor source) throws IOException {
        return SizeUtils.getSize(source.getFileDescriptor());
    }

    public static int getRawResourceSize(
            @NonNull Resources resources, @RawRes int rawResId
    ) throws IOException {
        try {
            return SizeUtils.getSize(resources.openRawResourceFd(rawResId));
        } catch (Resources.NotFoundException e) {
            throw new IOException(e);
        }
    }

    public static int getSize(@NonNull String string, @NonNull String encoding) throws IOException {
        try {
            return string.getBytes(encoding).length;
        } catch (UnsupportedEncodingException e) {
            throw new IOException(e);
        }
    }

    public static int getSize(@NonNull String string) throws IOException {
        return getSize(string, "UTF-8");
    }

    public static int getSize(@NonNull InputStream stream) throws IOException {
        return stream.available();
    }

    private SizeUtils() {
    }
}
