package com.gempukku.lotro.cards.set5.gollum;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.DiscardCardAtRandomFromHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDiscardEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Battle of Helm's Deep
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Shadow: Play Gollum from your draw deck or your discard pile. Skirmish: Discard a card at random from hand
 * to make Gollum strength +3.
 */
public class Card5_030 extends AbstractEvent {
    public Card5_030() {
        super(Side.SHADOW, 0, Culture.GOLLUM, "We Must Have It", Phase.SHADOW, Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return (
                game.getGameState().getCurrentPhase() != Phase.SKIRMISH
                        || Filters.filter(game.getGameState().getHand(self.getOwner()), game, Filters.not(self)).size() >= 1);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        if (game.getGameState().getCurrentPhase() == Phase.SHADOW) {
            List<Effect> possibleEffects = new LinkedList<>();
            possibleEffects.add(
                    new ChooseAndPlayCardFromDeckEffect(playerId, Filters.gollum) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Play Gollum from your draw deck";
                        }
                    });
            if (PlayConditions.canPlayFromDiscard(playerId, game, Filters.gollum)) {
                possibleEffects.add(
                        new ChooseAndPlayCardFromDiscardEffect(playerId, game, Filters.gollum) {
                            @Override
                            public String getText(LotroGame game) {
                                return "Play Gollum from your discard pile";
                            }
                        });
            }
            action.appendEffect(
                    new ChoiceEffect(action, playerId, possibleEffects));
        } else {
            action.appendCost(
                    new DiscardCardAtRandomFromHandEffect(self, playerId, false));
            action.appendEffect(
                    new ChooseAndAddUntilEOPStrengthBonusEffect(action, self, playerId, 3, Filters.gollum));
        }
        return action;
    }
}
