package com.gempukku.lotro.draft3.bot;

import java.util.List;

public interface DraftBot {
    String chooseCard(List<String> cardsToPickFrom);
}
