package com.gempukku.lotro.common;

public class BlueprintUtils {
    public static String[] split(String blueprintId) {
        return stripModifiers(blueprintId).split("_");
    }

    public static String getSet(String blueprintId) {
        return split(blueprintId)[0];
    }

    public static String getCardNumber(String blueprintId) {
        return split(blueprintId)[1];
    }

    public static String stripModifiers(String blueprintId) {
        if (blueprintId.endsWith("*"))
            blueprintId = blueprintId.substring(0, blueprintId.length() - 1);
        if (blueprintId.endsWith("T"))
            blueprintId = blueprintId.substring(0, blueprintId.length() - 1);
        return blueprintId;
    }
}
