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

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.bumptech.glide.load.Options;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.kirich1409.svgloader.glide.utils.ResourceUtils;
import com.kirich1409.svgloader.glide.utils.SizeUtils;

import java.io.IOException;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class RawResourceSvgDecoder extends SvgDecoder<Uri> {

    private final Resources mResources;

    public RawResourceSvgDecoder(@NonNull Context context) {
        mResources = context.getResources();
    }

    @Override
    public boolean handles(@NonNull Uri source, @NonNull Options options) {
        return ResourceUtils.isRawResource(mResources, source);
    }

    @Override
    SVG loadSvg(@NonNull Uri source, int width, int height, @NonNull Options options) throws SvgParseException {
        try {
            return SVG.getFromResource(mResources, ResourceUtils.getRawResourceId(mResources, source));
        } catch (SVGParseException e) {
            throw new SvgParseException(e);
        }
    }

    @Override
    protected int getSize(@NonNull Uri source) throws IOException {
        int rawResId = ResourceUtils.getRawResourceId(mResources, source);
        return SizeUtils.getRawResourceSize(mResources, rawResId);
    }
}
