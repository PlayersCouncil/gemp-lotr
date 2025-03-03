package com.gempukku.lotro.draft3;

public interface DraftTimerProducer {
    DraftTimer getDraftTimer();

    enum Type {
        CLASSIC
    }

    static DraftTimerProducer getDraftTimerProducer(Type type) {
        if (type == Type.CLASSIC) {
            return new DraftTimerProducerClassic();
        } else {
            return null;
        }
    }
}
