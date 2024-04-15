package com.gempukku.lotro.cards.set4.rohan;

import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.PreventCardEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 2
 * Type: Companion • Man
 * Strength: 6
 * Vitality: 3
 * Resistance: 6
 * Signet: Aragorn
 * Game Text: Valiant. Response: If Eowyn is exhausted and about to take a wound in a skirmish, discard 2 cards from
 * hand to prevent that wound.
 */
public class Card4_271 extends AbstractCompanion {
    public Card4_271() {
        super(2, 6, 3, 6, Culture.ROHAN, Race.MAN, Signet.ARAGORN, Names.eowyn, "Sister-daughter of Théoden", true);
        addKeyword(Keyword.VALIANT);
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingWounded(effect, game, self)
                && game.getGameState().getCurrentPhase() == Phase.SKIRMISH
                && Filters.exhausted.accepts(game, self)
                && game.getGameState().getHand(playerId).size() >= 2) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 2));
            action.appendEffect(
                    new PreventCardEffect((WoundCharactersEffect) effect, self));
            return Collections.singletonList(action);
        }
        return null;
    }
}
