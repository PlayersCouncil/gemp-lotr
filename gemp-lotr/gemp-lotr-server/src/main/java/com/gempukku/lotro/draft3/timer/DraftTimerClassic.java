package com.gempukku.lotro.draft3.timer;

import java.util.HashMap;
import java.util.Map;

public class DraftTimerClassic implements DraftTimer{
    private Map<Integer, Integer> durations = new HashMap<>();
    private boolean firstCardPicked = false;

    public DraftTimerClassic() {
        // 8 secs per card in pack
        durations.put(1, 5);
        durations.put(2, 8);
        durations.put(3, 16);
        durations.put(4, 24);
        durations.put(5, 32);
        durations.put(6, 40);
        durations.put(7, 48);
        durations.put(8, 56);
        durations.put(9, 64);
        durations.put(10, 72);
        durations.put(11, 80);
        durations.put(12, 88);
        durations.put(13, 96);
        durations.put(14, 104);
        durations.put(15, 112);
        durations.put(16, 120);
        durations.put(17, 128);
        durations.put(18, 136);
        durations.put(19, 144);
        durations.put(20, 152);
    }

    @Override
    public int getSecondsAllotted(int cardsInPack) {
        // Unknown duration
        if (!durations.containsKey(cardsInPack)) {
            return -1;
        }

        // Look to map for info
        int tbr = durations.get(cardsInPack);
        // Add 5 extra minutes for the very first pick so players can get to draft
        tbr += firstCardPicked ? 0 : 300;

        firstCardPicked = true;

        return tbr;
    }
}
