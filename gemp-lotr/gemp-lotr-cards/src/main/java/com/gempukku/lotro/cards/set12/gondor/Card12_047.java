package com.gempukku.lotro.cards.set12.gondor;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 3
 * Type: Companion • Man
 * Strength: 7
 * Vitality: 3
 * Resistance: 7
 * Game Text: Ranger. Skirmish: Exert Faramir to make him strength +1 for each wound on the Ring-bearer.
 */
public class Card12_047 extends AbstractCompanion {
    public Card12_047() {
        super(3, 7, 3, 7, Culture.GONDOR, Race.MAN, null, "Faramir", "Dúnadan of Gondor", true);
        addKeyword(Keyword.RANGER);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canSelfExert(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            int wounds = game.getGameState().getWounds(game.getGameState().getRingBearer(playerId));
            action.appendEffect(
                    new AddUntilEndOfPhaseModifierEffect(
                            new StrengthModifier(self, self, wounds)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
