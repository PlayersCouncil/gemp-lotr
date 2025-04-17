package com.gempukku.lotro.draft3.bot;

import com.gempukku.lotro.draft3.DraftPlayer;
import com.gempukku.lotro.draft3.TableDraft;

import java.util.List;
import java.util.Random;

public class RandomDraftBot extends DraftPlayer implements DraftBot{

    public RandomDraftBot(TableDraft table, String name) {
        super(table, name);
    }

    @Override
    public String chooseCard(List<String> cardsToPickFrom) {
        return chooseRandom(cardsToPickFrom);
    }

    private String chooseRandom(List<String> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        Random rand = new Random();
        int randomIndex = rand.nextInt(source.size());
        return source.get(randomIndex);
    }
}
