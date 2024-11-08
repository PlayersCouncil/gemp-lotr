package com.gempukku.lotro.common;

import java.util.regex.Pattern;

public class CardInfo {

    public String id;
    public Language language = Language.EN;
    public String setId;
    public CardStyle style = CardStyle.STANDARD;
    public int cardNum;
    public int revision = 0;
    public String image = "cardnotfound.jpg";
    public String collInfo;
    public String rarity;
    public CardVariant variant = CardVariant.ORIGINAL;
    public String parentId;
    public String variantPath;
    public OverlayType overlay = OverlayType.NONE;

    public CardInfo() { }

    private static final Pattern gempIdParser = Pattern.compile("([A-Z][A-Z]-)?(\\d+)(\\w)(\\d+)(\\.\\d+)?(\\*)?");
    // EN-1_1.0*
    public CardInfo(String id) {
        var matcher = gempIdParser.matcher(id);
        if(!matcher.matches())
            throw new IllegalArgumentException("Id " + id + "is an invalid Gemp id.");
        String lang = matcher.group(1);
        String set = matcher.group(2);
        String style = matcher.group(3);
        String cardnum = matcher.group(4);
        String revision = matcher.group(5);
        String foil = matcher.group(6);

        this.id = set + style + cardnum;
        this.language = Language.Parse(lang);
        this.setId = set;
        this.style = CardStyle.Parse(style);
        this.cardNum = cardnum == null ? 0 : Integer.parseInt(cardnum);
        this.revision = revision == null ? 0 : Integer.parseInt(revision);
        this.collInfo = set + "_" + cardnum;
        this.overlay = OverlayType.Parse(foil);
    }

    public boolean reliesOnParent() {
        return parentId != null || variantPath != null || (variant != null && variant != CardVariant.ORIGINAL);
    }


    /*
cardInfo: {
    language: EN
    //style: Standard
    revision: 0
    imagePath: LOTR06063.jpg
    collInfo: 6U63
    rarity: U
    variant: standard
    //parentId: 6_63
    //parentPath: alts/promo
    //overlay: none
}
     */
}
