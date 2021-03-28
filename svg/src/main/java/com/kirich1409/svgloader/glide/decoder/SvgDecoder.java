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

package com.kirich1409.svgloader.glide.decoder;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.SVG;
import com.kirich1409.svgloader.glide.SvgResource;
import com.kirich1409.svgloader.glide.utils.SvgUtils;

import java.io.IOException;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class SvgDecoder<T> implements ResourceDecoder<T, SVG> {

    @Override
    public boolean handles(@NonNull T source, @NonNull Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<SVG> decode(@NonNull T source, int width, int height, @NonNull Options options)
            throws IOException {
        try {
            int sourceSize = getSize(source);
            SVG svg = loadSvg(source, width, height, options);
            SvgUtils.fix(svg);
            int[] sizes = getResourceSize(svg, width, height);
            return new SvgResource(svg, sizes[0], sizes[1], sourceSize);
        } catch (SvgParseException e) {
            throw new IOException("Cannot load SVG", e);
        }
    }

    private static int[] getResourceSize(@NonNull SVG svg, int width, int height) {
        int[] sizes = new int[]{width, height};
        if (width == Target.SIZE_ORIGINAL && height == Target.SIZE_ORIGINAL) {
            sizes[0] = Math.round(svg.getDocumentWidth());
            sizes[1] = Math.round(svg.getDocumentHeight());

        } else if (width == Target.SIZE_ORIGINAL) {
            sizes[0] = Math.round(svg.getDocumentAspectRatio() * height);

        } else if (height == Target.SIZE_ORIGINAL) {
            sizes[1] = Math.round(width / svg.getDocumentAspectRatio());
        }
        return sizes;
    }

    @IntRange(from = 0)
    protected abstract int getSize(@NonNull T source) throws IOException;

    abstract SVG loadSvg(@NonNull T source, int width, int height, @NonNull Options options)
            throws SvgParseException;
}
