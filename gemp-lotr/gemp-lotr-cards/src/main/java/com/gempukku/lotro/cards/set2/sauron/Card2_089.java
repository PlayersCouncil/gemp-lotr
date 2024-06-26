package com.gempukku.lotro.cards.set2.sauron;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.CancelEventEffect;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.RoamingPenaltyModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.PlayEventResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Mines of Moria
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 2
 * Type: Minion • Orc
 * Strength: 6
 * Vitality: 2
 * Site: 6
 * Game Text: Tracker. The roaming penalty for each [SAURON] minion you play is -1. Response: If an event is played
 * that spots or exerts a ranger, exert or discard this minion to cancel that event.
 */
public class Card2_089 extends AbstractMinion {
    public Card2_089() {
        super(2, 6, 2, 6, Race.ORC, Culture.SAURON, "Orc Scout");
        addKeyword(Keyword.TRACKER);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new RoamingPenaltyModifier(self, Filters.and(Culture.SAURON, CardType.MINION), -1));
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, CardType.EVENT)) {
            PlayEventResult playEffect = (PlayEventResult) effectResult;
            if (playEffect.isRequiresRanger()) {
                ActivateCardAction action = new ActivateCardAction(self);
                List<Effect> possibleCosts = new LinkedList<>();
                possibleCosts.add(
                        new SelfExertEffect(action, self) {
                            @Override
                            public String getText(LotroGame game) {
                                return "Exert this minion";
                            }
                        });
                possibleCosts.add(
                        new SelfDiscardEffect(self) {
                            @Override
                            public String getText(LotroGame game) {
                                return "Discard this minion";
                            }
                        });
                action.appendCost(
                        new ChoiceEffect(action, playerId, possibleCosts));
                action.appendEffect(
                        new CancelEventEffect(self, playEffect));
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}
