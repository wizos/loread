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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class SvgUtils {

    public static void fix(@NonNull SVG svg) throws IOException {
        RectF viewBox = svg.getDocumentViewBox();
        float docWidth = svg.getDocumentWidth();
        float docHeight = svg.getDocumentHeight();

        if (viewBox == null) {
            if (docWidth > 0 && docHeight > 0) {
                svg.setDocumentViewBox(0F, 0F, docWidth, docHeight);
            } else {
                throw new IOException("SVG must have specify 'width' & 'height' tags or 'viewbox'");
            }

        } else if (docWidth <= 0 && docHeight <= 0) {
            svg.setDocumentWidth(viewBox.width());
            svg.setDocumentHeight(viewBox.height());

        } else if (docWidth <= 0) {
            svg.setDocumentWidth(aspectRation(viewBox) * docHeight);

        } else if (docHeight <= 0) {
            svg.setDocumentHeight(docWidth / aspectRation(viewBox));
        }
    }

    private static float aspectRation(@NonNull RectF rect) {
        return rect.width() / rect.height();
    }

    public static SVG getSvg(@NonNull File file) throws SVGParseException, IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File: '" + file.getAbsolutePath() + "' not exists");
        }

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            return SVG.getFromInputStream(is);
        }
    }

    public static SVG getSvg(@NonNull FileDescriptor descriptor)
            throws SVGParseException, IOException {

        try (InputStream is = new BufferedInputStream(new FileInputStream(descriptor))) {
            return SVG.getFromInputStream(is);
        }
    }

    public static void scaleDocumentSize(
            @NonNull SVG svg,
            @FloatRange(from = 0, fromInclusive = false) float scale
    ) {
        svg.setDocumentWidth(svg.getDocumentWidth() * scale);
        svg.setDocumentHeight(svg.getDocumentHeight() * scale);
    }

    @NonNull
    public static Bitmap toBitmap(
            @NonNull SVG svg,
            @NonNull BitmapProvider provider,
            @NonNull Bitmap.Config config
    ) {
        int outImageWidth = Math.round(svg.getDocumentWidth());
        int outImageHeight = Math.round(svg.getDocumentHeight());
        Bitmap bitmap = provider.get(outImageWidth, outImageHeight, config);
        Canvas canvas = new Canvas(bitmap);
        svg.renderToCanvas(canvas);
        return bitmap;
    }

    private SvgUtils() {
    }
}
