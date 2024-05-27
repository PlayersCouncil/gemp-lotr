package com.gempukku.lotro.game.state;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.ActionsEnvironment;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.logic.modifiers.ModifiersEnvironment;
import com.gempukku.lotro.logic.modifiers.ModifiersQuerying;

public interface LotroGame {
    GameState getGameState();

    LotroCardBlueprintLibrary getLotroCardBlueprintLibrary();

    ModifiersEnvironment getModifiersEnvironment();

    ModifiersQuerying getModifiersQuerying();

    ActionsEnvironment getActionsEnvironment();

    UserFeedback getUserFeedback();

    void checkRingBearerCorruption();

    void checkRingBearerAlive();

    void playerWon(String currentPlayerId, String reason);

    void playerLost(String currentPlayerId, String reason);

    String getWinnerPlayerId();

    LotroFormat getFormat();

    boolean shouldAutoPass(String playerId, Phase phase);

    boolean isSolo();
}
