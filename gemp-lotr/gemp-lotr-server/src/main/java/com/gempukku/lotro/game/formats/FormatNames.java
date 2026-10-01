package com.gempukku.lotro.game.formats;

import com.gempukku.lotro.game.LotroFormat;

/**
 * Display names for format codes that may no longer resolve.
 * <p>
 * {@link LotroFormatLibrary#getFormat} returns null for a code that has been retired from the format definitions, so
 * {@code getFormat(code).getName()} throws for any finished event played in such a format.  Every read-only view of
 * old events (the detail XML, the event browser, the tournament report) goes through here instead and shows the raw
 * code in place of the name.
 */
public final class FormatNames {
    /** What is shown when there is neither a format nor a code. */
    public static final String UNKNOWN = "Unknown";

    private FormatNames() {
    }

    /**
     * The name of the format with this code; the code itself when the library does not know it (or fails to look
     * it up); {@link #UNKNOWN} when there is no code.  Never null, never throws.
     */
    public static String nameOrCode(LotroFormatLibrary library, String code) {
        if (isBlank(code))
            return UNKNOWN;
        LotroFormat format = null;
        if (library != null) {
            try {
                format = library.getFormat(code);
            } catch (RuntimeException exp) {
                format = null;
            }
        }
        return nameOrCode(format, code);
    }

    /**
     * The name of {@code format} when there is one, otherwise {@code code}, otherwise {@link #UNKNOWN}.  Never null.
     */
    public static String nameOrCode(LotroFormat format, String code) {
        if (format != null) {
            String name = format.getName();
            if (!isBlank(name))
                return name;
            if (isBlank(code))
                code = format.getCode();
        }
        return isBlank(code) ? UNKNOWN : code;
    }

    /** The code of {@code format}, or {@code fallback} when there is no format. */
    public static String codeOr(LotroFormat format, String fallback) {
        if (format != null && !isBlank(format.getCode()))
            return format.getCode();
        return fallback;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
