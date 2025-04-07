package com.gempukku.lotro.draft3.timer;

import java.util.HashMap;
import java.util.Map;

public class DraftTimerClassic implements DraftTimer{
    private Map<Integer, Integer> durations = new HashMap<>();
    private static final int SECS_PER_CARD = 6;

    public DraftTimerClassic() {
        durations.put(1, 5);
        for (int i = 2; i <= 20; i++) {
            durations.put(i, (i - 1) * SECS_PER_CARD);
        }
    }

    @Override
    public int getSecondsAllotted(int cardsInPack) {
        // Unknown duration
        if (!durations.containsKey(cardsInPack)) {
            return -1;
        }

        // Look to map for info
        return durations.get(cardsInPack);
    }
}
