package com.gempukku.lotro.game.adventure;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.Adventure;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.modifiers.CantDiscardFromPlayModifier;
import com.gempukku.lotro.logic.modifiers.CantReturnToHandModifier;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;

public abstract class SoloAdventure implements Adventure {
    @Override
    public void applyAdventureRules(LotroGame game, DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        modifiersLogic.addAlwaysOnModifier(
                new CantDiscardFromPlayModifier(null, "Can't be discarded from play", null, null, CardType.ADVENTURE, Filters.any));
        modifiersLogic.addAlwaysOnModifier(
                new CantReturnToHandModifier(null, "Can't be returned to hand", null, CardType.ADVENTURE, Filters.any));
    }
}
