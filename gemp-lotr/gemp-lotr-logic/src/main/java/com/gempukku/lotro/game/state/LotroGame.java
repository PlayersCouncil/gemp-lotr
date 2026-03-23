package com.gempukku.lotro.game.state;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.communication.UserFeedback;
import com.gempukku.lotro.game.ActionsEnvironment;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.logic.modifiers.ModifiersEnvironment;
import com.gempukku.lotro.logic.modifiers.ModifiersQuerying;
import com.gempukku.lotro.packs.PackOpener;

public interface LotroGame {
    GameState getGameState();

    LotroCardBlueprintLibrary getLotroCardBlueprintLibrary();

    /**
     * Returns the pack opener for mid-game booster pack effects, or null if not available.
     */
    PackOpener getPackOpener();

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
