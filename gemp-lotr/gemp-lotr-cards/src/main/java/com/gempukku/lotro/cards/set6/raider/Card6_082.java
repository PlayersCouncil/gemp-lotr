package com.gempukku.lotro.cards.set6.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.PreventableEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Ents of Fangorn
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Maneuver: Spot a mounted [RAIDER] Man to discard a companion (except the Ring-bearer). The Free Peoples
 * player may add (4) to prevent this.
 */
public class Card6_082 extends AbstractEvent {
    public Card6_082() {
        super(Side.SHADOW, 0, Culture.RAIDER, "Trample", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.RAIDER, Race.MAN, Filters.mounted);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, final PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new PreventableEffect(action,
                        new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, CardType.COMPANION, Filters.not(Filters.ringBearer)) {
                            @Override
                            public String getText(LotroGame game) {
                                return "Discard a companion (except the Ring-bearer)";
                            }
                        },
                        game.getGameState().getCurrentPlayerId(),
                        new PreventableEffect.PreventionCost() {
                            @Override
                            public Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId) {
                                return new AddTwilightEffect(self, 4);
                            }
                        }
                ));
        return action;
    }
}
