package com.gempukku.lotro.draft3.timer;


import java.util.ArrayList;
import java.util.List;

public class DraftTimer {
    private static final int CLASSIC_SECS_PER_CARD = 6;
    private static final int SLOW_SECS_PER_CARD = 10;

    public enum Type {
        CLASSIC, SLOW, NO_TIMER
    }

    private final int secsPerCard;

    public static DraftTimer get(DraftTimer.Type type) {
        if (type == DraftTimer.Type.CLASSIC) {
            return new DraftTimer(CLASSIC_SECS_PER_CARD);
        } else if (type == DraftTimer.Type.SLOW) {
            return new DraftTimer(SLOW_SECS_PER_CARD);
        } else {
            return null;
        }
    }


    private DraftTimer(int secsPerCard) {
        this.secsPerCard = secsPerCard;
    }

    public int getSecondsAllotted(int cardsInPack) {
        int tbr =  secsPerCard * (cardsInPack - 1);
        if (tbr <= 0) {
            return secsPerCard;
        } else {
            return tbr;
        }
    }

    public static DraftTimer.Type getTypeFromString(String type) {
        try {
            return DraftTimer.Type.valueOf(type);
        } catch (IllegalArgumentException ignored) {
            return DraftTimer.Type.NO_TIMER;
        }
    }

    public static List<String> getAllTypes() {
        List<String> tbr = new ArrayList<>();
        for (DraftTimer.Type value : DraftTimer.Type.values()) {
            tbr.add(value.name());
        }
        return tbr;
    }
}
