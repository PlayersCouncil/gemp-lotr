package com.gempukku.lotro.common;

public enum Token {
    BURDEN, WOUND,

    DUNLAND(Culture.DUNLAND), DWARVEN(Culture.DWARVEN), ELVEN(Culture.ELVEN), GANDALF(Culture.GANDALF),
    GONDOR(Culture.GONDOR), ISENGARD(Culture.ISENGARD), RAIDER(Culture.RAIDER), ROHAN(Culture.ROHAN), SHIRE(Culture.SHIRE),
    WRAITH(Culture.WRAITH), SAURON(Culture.SAURON), GOLLUM(Culture.GOLLUM), MORIA(Culture.MORIA),

    URUK_HAI(Culture.URUK_HAI), MEN(Culture.MEN), ORC(Culture.ORC),

    //Additional Hobbit Draft cultures
    ESGAROTH(Culture.ESGAROTH), GUNDABAD(Culture.GUNDABAD), MIRKWOOD(Culture.MIRKWOOD), SMAUG(Culture.SMAUG), SPIDER(Culture.SPIDER), TROLL(Culture.TROLL);

    private final Culture _culture;

    Token() {
        this(null);
    }

    Token(Culture culture) {
        _culture = culture;
    }

    public Culture getCulture() {
        return _culture;
    }

    public static Token findTokenForCulture(Culture culture) {
        if(culture == null)
            return null;

        for (Token token : Token.values()) {
            if (token.getCulture() == culture)
                return token;
        }
        return null;
    }
}
