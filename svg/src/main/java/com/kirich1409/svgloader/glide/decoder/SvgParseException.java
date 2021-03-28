package com.kirich1409.svgloader.glide.decoder;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
final class SvgParseException extends Exception {

    SvgParseException() {
    }

    SvgParseException(@Nullable String message) {
        super(message);
    }

    SvgParseException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    SvgParseException(@Nullable Throwable cause) {
        super(cause);
    }
}
