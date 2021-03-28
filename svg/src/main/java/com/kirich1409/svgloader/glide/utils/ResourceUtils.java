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

import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.RestrictTo;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class ResourceUtils {

    private static final int NAME_URI_PATH_SEGMENTS = 2;
    private static final int ID_PATH_SEGMENTS = 1;

    private static final int TYPE_PATH_SEGMENT_INDEX = 0;
    private static final int NAME_PATH_SEGMENT_INDEX = 1;

    private static final int RES_ID_SEGMENT_INDEX = 0;

    private static final String RESOURCE_TYPE_RAW = "raw";

    @RawRes
    public static int getRawResourceId(@NonNull Resources resources, @NonNull Uri source) {
        List<String> segments = source.getPathSegments();
        int resId;
        if (segments.size() == NAME_URI_PATH_SEGMENTS) {
            String typeName = segments.get(TYPE_PATH_SEGMENT_INDEX);
            checkResourceType(typeName);
            String packageName = source.getAuthority();
            String resourceName = segments.get(NAME_PATH_SEGMENT_INDEX);
            resId = resources.getIdentifier(resourceName, typeName, packageName);
        } else if (segments.size() == ID_PATH_SEGMENTS) {
            try {
                resId = Integer.valueOf(segments.get(RES_ID_SEGMENT_INDEX));
                if (resId != 0) {
                    checkResourceType(resources.getResourceTypeName(resId));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unrecognized Uri format: " + source, e);
            }
        } else {
            throw new IllegalArgumentException("Unrecognized Uri format: " + source);
        }

        if (resId == 0) {
            throw new IllegalArgumentException("Failed to obtain resource id for: " + source);
        }
        return resId;
    }

    private static void checkResourceType(@Nullable String resType) {
        if (!RESOURCE_TYPE_RAW.equals(resType)) {
            throw new IllegalArgumentException("Unsupported resource type: " + resType);
        }
    }

    public static boolean isRawResource(@NonNull Resources resources, @NonNull Uri uri) {
        if (!ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())) {
            return false;
        }

        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == NAME_URI_PATH_SEGMENTS) {
            return RESOURCE_TYPE_RAW.equals(pathSegments.get(TYPE_PATH_SEGMENT_INDEX));

        } else if (pathSegments.size() == ID_PATH_SEGMENTS) {
            try {
                int resId = Integer.parseInt(pathSegments.get(RES_ID_SEGMENT_INDEX));
                return resId != 0
                        && RESOURCE_TYPE_RAW.equals(resources.getResourceTypeName(resId));
            } catch (NumberFormatException e) {
                return false;
            }

        } else {
            return false;
        }
    }

    private ResourceUtils() {
    }
}
