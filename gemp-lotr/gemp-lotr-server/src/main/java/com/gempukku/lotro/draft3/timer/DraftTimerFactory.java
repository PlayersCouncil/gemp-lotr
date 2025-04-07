package com.gempukku.lotro.draft3.timer;

import java.util.ArrayList;
import java.util.List;

public class DraftTimerFactory {
    private DraftTimerFactory() {

    }

    public enum Type {
        CLASSIC, FAST, NO_TIMER
    }

    public static DraftTimer getDraftTimer(Type type) {
        if (type == Type.FAST) {
            return new DraftTimerFast();
        } else if (type == Type.CLASSIC) {
            return new DraftTimerClassic();
        } else {
            return null;
        }
    }

    public static Type getTypeFromString(String type) {
        try {
            return Type.valueOf(type);
        } catch (IllegalArgumentException ignored) {
            return Type.NO_TIMER;
        }
    }

    public static List<String> getAllTypes() {
        List<String> tbr = new ArrayList<>();
        for (Type value : Type.values()) {
            tbr.add(value.name());
        }
        return tbr;
    }


}
