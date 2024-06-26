package com.gempukku.lotro.cards.set8.gondor;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractResponseEvent;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.KillEffect;
import com.gempukku.lotro.logic.effects.SpotEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExhaustCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Siege of Gondor
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 1
 * Type: Event • Response
 * Game Text: If a [GONDOR] Wraith is about to be killed, discard him and either exhaust another [GONDOR] Wraith or spot
 * another exhausted [GONDOR] Wraith to prevent that.
 */
public class Card8_033 extends AbstractResponseEvent {
    public Card8_033() {
        super(Side.FREE_PEOPLE, 1, Culture.GONDOR, "Elessar's Edict");
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingKilled(effect, game, Culture.GONDOR, Race.WRAITH)
                && PlayUtils.checkPlayRequirements(game, self, Filters.any, 0, 0, false, false)) {
            KillEffect killEffect = (KillEffect) effect;
            Collection<PhysicalCard> killedWraiths = Filters.filter(killEffect.getCharactersToBeKilled(), game, Culture.GONDOR, Race.WRAITH);
            List<PlayEventAction> actions = new LinkedList<>();
            for (PhysicalCard killedWraith : killedWraiths) {
                if (PlayConditions.canExert(self, game, Filters.not(killedWraith), Culture.GONDOR, Race.WRAITH)
                        || PlayConditions.canSpot(game, Filters.not(killedWraith), Culture.GONDOR, Race.WRAITH, Filters.exhausted)) {
                    PlayEventAction action = new PlayEventAction(self);
                    action.setText("Discard " + GameUtils.getFullName(killedWraith));
                    action.appendCost(
                            new DiscardCardsFromPlayEffect(playerId, self, killedWraith));
                    List<Effect> possibleCosts = new LinkedList<>();
                    possibleCosts.add(
                            new ChooseAndExhaustCharactersEffect(action, playerId, 1, 1, Filters.not(killedWraith), Culture.GONDOR, Race.WRAITH) {
                                @Override
                                public String getText(LotroGame game) {
                                    return "Exhaust another GONDOR Wraith";
                                }
                            });
                    possibleCosts.add(
                            new SpotEffect(1, Filters.not(killedWraith), Culture.GONDOR, Race.WRAITH, Filters.exhausted) {
                                @Override
                                public String getText(LotroGame game) {
                                    return "Spot another exhausted GONDOR Wraith";
                                }
                            });
                    action.appendCost(
                            new ChoiceEffect(action, playerId, possibleCosts));
                    actions.add(action);
                }
            }
            return actions;
        }
        return null;
    }
}
