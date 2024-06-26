package com.gempukku.lotro.cards.set2.shire;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.condition.NotCondition;
import com.gempukku.lotro.logic.modifiers.condition.PhaseCondition;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Mines of Moria
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 0
 * Type: Condition
 * Game Text: Tale. Bearer must be Bilbo. Bilbo may not take wounds (except during the archery phase). Fellowship or
 * Regroup: Exert Bilbo or discard this condition to remove (1).
 */
public class Card2_097 extends AbstractAttachable {
    public Card2_097() {
        super(Side.FREE_PEOPLE, CardType.CONDITION, 0, Culture.SHIRE, null, "Consorting With Wizards");
        addKeyword(Keyword.TALE);
    }

    @Override
    public Filter getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.name("Bilbo");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new CantTakeWoundsModifier(self, new NotCondition(new PhaseCondition(Phase.ARCHERY)), Filters.hasAttached(self)));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.FELLOWSHIP, self)
                || PlayConditions.canUseFPCardDuringPhase(game, Phase.REGROUP, self)) {
            ActivateCardAction action = new ActivateCardAction(self);
            List<Effect> possibleCosts = new LinkedList<>();
            possibleCosts.add(
                    new ExertCharactersEffect(action, self, self.getAttachedTo()) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Exert Bilbo";
                        }
                    });
            possibleCosts.add(
                    new SelfDiscardEffect(self) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Discard this condition";
                        }
                    });
            action.appendCost(
                    new ChoiceEffect(action, playerId, possibleCosts));
            action.appendEffect(
                    new RemoveTwilightEffect(1));
            return Collections.singletonList(action);
        }
        return null;
    }
}
