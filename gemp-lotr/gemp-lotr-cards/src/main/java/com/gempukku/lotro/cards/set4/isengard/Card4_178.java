package com.gempukku.lotro.cards.set4.isengard;

import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.AssignAgainstResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 2
 * Type: Minion • Man
 * Strength: 6
 * Vitality: 2
 * Site: 3
 * Game Text: Each time the Free Peoples player assigns a companion or ally to an [ISENGARD] Man, that companion
 * or ally must exert.
 */
public class Card4_178 extends AbstractMinion {
    public Card4_178() {
        super(2, 6, 2, 3, Race.MAN, Culture.ISENGARD, "Unferth", "Gríma's Bodyguard", true);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.assignedAgainst(game, effectResult, Side.FREE_PEOPLE, Filters.and(Culture.ISENGARD, Race.MAN), Filters.or(CardType.COMPANION, CardType.ALLY))) {
            AssignAgainstResult assignmentResult = (AssignAgainstResult) effectResult;
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.setText("Exert " + GameUtils.getFullName(assignmentResult.getAssignedCard()));
            action.appendEffect(
                    new ExertCharactersEffect(action, self, assignmentResult.getAssignedCard()));
            return Collections.singletonList(action);
        }
        return null;
    }
}
