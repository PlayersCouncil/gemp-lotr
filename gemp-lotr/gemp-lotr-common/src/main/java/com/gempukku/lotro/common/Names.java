package com.gempukku.lotro.common;

import java.text.Normalizer;

public class Names {

    public static String SanitizeName(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("’", "'")
                .replaceAll("‘", "'")
                .replaceAll("”", "\"")
                .replaceAll("“", "\"")
                //Removes any unicode "combining mark", aka diacritics
                .replaceAll("\\p{M}", "")
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
                //Removes any unicode "combining mark", aka diacritics
                .replaceAll("\\p{M}", "");
    }

    public static String AddAccentsToPlainText(String text) {
        return text
            .replaceAll("Anarion", "Anárion")
            .replaceAll("Anduril", "Andúril")
            .replaceAll("Annun", "Annûn")
            .replaceAll("Attea", "Attëa")
            .replaceAll("Barad-dur", "Barad-dûr")
            .replaceAll("Baruk khazad", "Baruk Khazâd")
            .replaceAll("Cantea", "Cantëa")
            .replaceAll("Cirdan", "Círdan")
            .replaceAll("Curunir", "Curunír")
            .replaceAll("Deagol", "Déagol")
            .replaceAll("Deor", "Déor")
            .replaceAll("Dinendal", "Dínendal")
            .replaceAll("Dunadan", "Dúnadan")
            .replaceAll("Dunedain", "Dúnedain")
            .replaceAll("Enquea", "Enquëa")
            .replaceAll("Eomer", "Éomer")
            .replaceAll("Eomund", "Éomund")
            .replaceAll("Eored", "Éored")
            .replaceAll("Eothain", "Éothain")
            .replaceAll("Eowyn", "Éowyn")
            .replaceAll("Erethon", "Erethón")
            .replaceAll("Fror", "Frór")
            .replaceAll("Ghan-buri-ghan", "Ghan-bûri-ghan")
            .replaceAll("Gloin", "Glóin")
            .replaceAll("Grima", "Gríma")
            .replaceAll("Grishnakh", "Grishnákh")
            .replaceAll("Guthlaf", "Guthláf")
            .replaceAll("Guthwine", "Gúthwinë")
            .replaceAll("Hama", "Háma")
            .replaceAll("Jarnsmid", "Járnsmid")
            .replaceAll("Khazad ai-menu", "Khazâd ai-mênu")
            .replaceAll("Khazad-dum", "Khazad-dûm")
            .replaceAll("Lathspell", "Láthspell")
            .replaceAll("Leod", "Léod")
            .replaceAll("Leofric", "Léofric")
            .replaceAll("Leowyn", "Léowyn")
            .replaceAll("Lorien", "Lórien")
            .replaceAll("Lothlorien", "Lothlórien")
            .replaceAll("Luthien", "Lúthien")
            .replaceAll("Mauhur", "Mauhúr")
            .replaceAll("Miruvore", "Miruvóre")
            .replaceAll("Mumak", "Mûmak")
            .replaceAll("Mumakil", "Mûmakil")
            .replaceAll("Namarie", "Namárië")
            .replaceAll("Nazgul", "Nazgûl")
            .replaceAll("Nertea", "Nertëa")
            .replaceAll("Numenor", "Númenor")
            .replaceAll("Numenorean", "Númenorean")
            .replaceAll("Otsea", "Otsëa")
            .replaceAll("Palantir", "Palantír")
            .replaceAll("Rhun", "Rhûn")
            .replaceAll("Rumil", "Rúmil")
            .replaceAll("Simbelmyne", "Simbelmynë")
            .replaceAll("Smeagol", "Sméagol")
            .replaceAll("Theoden", "Théoden")
            .replaceAll("Theodred", "Théodred")
            .replaceAll("Thonnas", "Thónnas")
            .replaceAll("Thror", "Thrór")
            .replaceAll("Toldea", "Toldëa")
            .replaceAll("Udun", "Udûn")
            .replaceAll("Ugluk", "Uglúk")
            .replaceAll("Ulaire", "Úlairë");
    }
}
