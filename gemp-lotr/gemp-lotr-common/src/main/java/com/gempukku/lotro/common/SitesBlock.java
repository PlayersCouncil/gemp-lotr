package com.gempukku.lotro.common;

public enum SitesBlock {
    FELLOWSHIP("Fellowship"), TWO_TOWERS("Towers"), KING("King"),
    SHADOWS("Shadows and onwards"), MULTIPATH("Multipath"),
    SECOND_ED("2nd edition"),
	
	//Additional Hobbit Draft block
	HOBBIT("Hobbit");

    private final String _humanReadable;

    private SitesBlock(String humanReadable) {
        _humanReadable = humanReadable;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }

    public static SitesBlock findBlock(String name) {
        String nameCaps = name.toUpperCase().replace(' ', '_').replace('-', '_');
        String nameLower = name.toLowerCase();
        for (SitesBlock block : values()) {
            if (block.getHumanReadable().toLowerCase().equals(nameLower) || block.toString().equals(nameCaps))
                return block;
        }
        return null;
    }
}
