package com.gempukku.lotro.draft3;

import java.util.List;

public interface DraftTimerProducer {
    DraftTimer getDraftTimer();

    enum Type {
        CLASSIC, NO_TIMER
    }

    static DraftTimerProducer getDraftTimerProducer(Type type) {
        if (type == Type.CLASSIC) {
            return new DraftTimerProducerClassic();
        } else {
            return null;
        }
    }

    static Type getDraftTimerProducer(String type) {
        if (type.equals("CLASSIC")) {
            return Type.CLASSIC;
        } else {
            return Type.NO_TIMER;
        }
    }

    static List<String> getAllTypes() {
        return List.of("CLASSIC", "NO_TIMER");
    }
}
