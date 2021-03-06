package com.gempukku.lotro.cards.set2.sauron;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Mines of Moria
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 1
 * Type: Condition
 * Game Text: Search. To play, spot a [SAURON] Orc. Plays to your support area. Each time the fellowship moves during
 * the regroup phase, you may draw a card (or 2 cards if you spot a [SAURON] tracker).
 */
public class Card2_092 extends AbstractPermanent {
    public Card2_092() {
        super(Side.SHADOW, 1, CardType.CONDITION, Culture.SAURON, "Spies of Mordor");
        addKeyword(Keyword.SEARCH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Culture.SAURON, Race.ORC);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.moves(game, effectResult)
                && game.getGameState().getCurrentPhase() == Phase.REGROUP) {
            boolean spotsTracker = Filters.canSpot(game, Culture.SAURON, Keyword.TRACKER);
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
                    new DrawCardsEffect(action, playerId, spotsTracker ? 2 : 1));
            return Collections.singletonList(action);
        }
        return null;
    }
}
