package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.EffectResult;

public class AfterAllSkirmishesResult extends EffectResult {
    private boolean _createAnExtraAssignmentAndSkirmishPhases;

    public AfterAllSkirmishesResult() {
        super(EffectResult.Type.AFTER_ALL_SKIRMISHES);
    }

    public boolean isCreateAnExtraAssignmentAndSkirmishPhases(LotroGame game) {
        return _createAnExtraAssignmentAndSkirmishPhases || isRelentlessPhase(game);
    }

    /**
     * Whether a card has set up an additional assignment and skirmish phase (The Witch-king's Beast 12_184), as
     * apart from the relentless round that a relentless minion in play brings.
     */
    public boolean isExtraPhaseSetUpByACard() {
        return _createAnExtraAssignmentAndSkirmishPhases;
    }

    public boolean isRelentlessPhase(LotroGame game) {
        return Filters.countActive(game, CardType.MINION, Keyword.RELENTLESS) > 0;
    }

    public void setCreateAnExtraAssignmentAndSkirmishPhases(boolean createAnExtraAssignmentAndSkirmishPhases) {
        _createAnExtraAssignmentAndSkirmishPhases = createAnExtraAssignmentAndSkirmishPhases;
    }
}
