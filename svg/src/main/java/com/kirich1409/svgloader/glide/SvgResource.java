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

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.bumptech.glide.load.engine.Resource;
import com.caverock.androidsvg.SVG;
import com.kirich1409.svgloader.glide.utils.SvgUtils;

import java.io.IOException;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class SvgResource implements Resource<SVG> {

    private final SVG mSvg;
    private final int mWidth;
    private final int mHeight;
    private final int mSize;

    public SvgResource(
            @NonNull SVG svg,
            @IntRange(from = 1) int width,
            @IntRange(from = 1) int height,
            @IntRange(from = 0) int size
    ) throws IOException {
        SvgUtils.fix(svg);
        mSvg = svg;
        mWidth = width;
        mHeight = height;
        mSize = size;
    }

    @IntRange(from = 1)
    public int getWidth() {
        return mWidth;
    }

    @IntRange(from = 1)
    public int getHeight() {
        return mHeight;
    }

    @NonNull
    public Class<SVG> getResourceClass() {
        return SVG.class;
    }

    @NonNull
    public SVG get() {
        return mSvg;
    }

    public int getSize() {
        return mSize;
    }

    @Override
    public void recycle() {
        // No need to recycle any resources
    }

    public String toString() {
        return "SvgResource{width=" + mWidth + ", height=" + mHeight + ", size=" + mSize + '}';
    }
}
