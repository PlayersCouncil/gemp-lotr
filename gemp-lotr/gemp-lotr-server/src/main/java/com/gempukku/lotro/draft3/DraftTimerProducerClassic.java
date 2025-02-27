package com.gempukku.lotro.draft3;

public class DraftTimerProducerClassic implements DraftTimerProducer {

    @Override
    public DraftTimer getDraftTimer() {
        return new DraftTimerClassic();
    }
}
