package com.gempukku.lotro.common;

import java.text.Normalizer;

public class Names {
    public static final String attea = "Úlairë Attëa";
    public static final String cantea = "Úlairë Cantëa";
    public static final String enquea = "Úlairë Enquëa";
    public static final String lemenya = "Úlairë Lemenya";
    public static final String nelya = "Úlairë Nelya";
    public static final String nertea = "Úlairë Nertëa";
    public static final String otsea = "Úlairë Otsëa";
    public static final String toldea = "Úlairë Toldëa";

    public static final String witchKing = "The Witch-king";
    public static final String caveTroll = "Cave Troll of Moria";

    public static final String eomer = "Éomer";
    public static final String eowyn = "Éowyn";
    public static final String theoden = "Théoden";

    public static String SanitizeName(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("’", "'")
                .replaceAll("‘", "'")
                .replaceAll("”", "\"")
                .replaceAll("“", "\"")
                .replaceAll("\\p{M}", "") //Matches all accents or diacritical marks and removes them
                .replaceAll(" ", "")
                .replaceAll("_", "")
                .toLowerCase();
    }

    public static String SanitizeDisplayName(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("’", "'")
                .replaceAll("‘", "'")
                .replaceAll("”", "\"")
                .replaceAll("“", "\"")
                .replaceAll("\\p{M}", "");
    }
}
