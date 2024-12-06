package com.gempukku.lotro.common;

public enum Language {
    EN("EN", "English", "English", true),
    DE("DE", "German", "Deutsch", false),
    FR("FR", "French", "Français", false),
    IT("IT", "Italian", "Italiano", false),
    ES("ES", "Spanish", "Español", false),
    PL("PL", "Polish", "Polski", false),
    PT("PT", "Portuguese", "Português", false),
    ZH("ZH", "Chinese", "中文", false),
    EL("EL", "Greek", "Νέα Ελληνικά", false),
    HU("HU", "Hungarian", "Magyar nyelv", false),
    NL("NL", "Dutch", "Nederlands", false),
    SV("SV", "Swedish", "Svenska", false),
    TW("TW", "Tengwar", "Tengwar", true);

    private final String internationalCode;
    private final String englishName;
    private final String nativeName;
    private final boolean supported;

    Language(String internationalCode, String englishName, String nativeName, boolean supported) {
        this.internationalCode = internationalCode;
        this.englishName = englishName;
        this.nativeName = nativeName;
        this.supported = supported;
    }

    public String getInternationalCode() {
        return internationalCode;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getNativeName() {
        return nativeName;
    }

    public boolean isSupported() {
        return supported;
    }

    public static Language Parse(String value) {
        if(value == null || value.isEmpty())
            return EN;

        value = value
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        for (Language language : values()) {
            if (language.getInternationalCode().equals(value)
                || language.getEnglishName().toUpperCase().equals(value))
                return language;
        }
        return EN;
    }

}
