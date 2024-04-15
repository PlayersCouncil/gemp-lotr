package com.gempukku.lotro.cards.set12.rohan;

import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 1
 * Type: Companion • Man
 * Strength: 5
 * Vitality: 3
 * Resistance: 5
 * Game Text: At the start of each skirmish involving Aldred, you may discard a possession borne by a minion he
 * is skirmishing.
 */
public class Card12_107 extends AbstractCompanion {
    public Card12_107() {
        super(1, 5, 3, 5, Culture.ROHAN, Race.MAN, null, "Aldred", "Éored Soldier", true);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.SKIRMISH)
                && Filters.inSkirmish.accepts(game, self)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
                    new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, CardType.POSSESSION, Filters.attachedTo(CardType.MINION, Filters.inSkirmishAgainst(self))));
            return Collections.singletonList(action);
        }
        return null;
    }
}
