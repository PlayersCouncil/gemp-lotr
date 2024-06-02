package com.gempukku.lotro.common;

public enum SitesBlock {
    FELLOWSHIP("Fellowship", "F"), TWO_TOWERS("Towers", "T"), KING("King", "K"),
    SHADOWS("Shadows and onwards", "S"), MULTIPATH("Multipath", "*"),
    SECOND_ED("2nd edition", "E"),
	
	//Additional Hobbit Draft block
	HOBBIT("Hobbit", "H");

    private final String _humanReadable;
    private final String _abbreviation;

    private SitesBlock(String humanReadable, String abbr) {
        _humanReadable = humanReadable;
        _abbreviation = abbr;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }
    public String getAbbreviation() { return _abbreviation; }

    public static SitesBlock findBlock(String name) {
        String nameCaps = name.toUpperCase().replace(' ', '_').replace('-', '_');
        String nameLower = name.toLowerCase();
        for (SitesBlock block : values()) {
            if (block.getHumanReadable().toLowerCase().equals(nameLower)
                    || block.toString().equals(nameCaps)
                    || block.getAbbreviation().equals(nameCaps))
                return block;
        }
        return null;
    }
}
