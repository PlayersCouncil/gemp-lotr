package com.gempukku.lotro.cards.set2.shire;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.CancelSkirmishEffect;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.effects.TakeOffTheOneRingEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Mines of Moria
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 1
 * Type: Condition
 * Strength: +1
 * Game Text: Tale. Bearer must be the Ring-bearer. Skirmish: Discard this condition to take off The One Ring or
 * to cancel a skirmish involving the Ring-bearer and a Nazgul.
 */
public class Card2_108 extends AbstractAttachable {
    public Card2_108() {
        super(Side.FREE_PEOPLE, CardType.CONDITION, 1, Culture.SHIRE, null, "O Elbereth! Gilthoniel!", null, true);
        addKeyword(Keyword.TALE);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.ringBearer;
    }

    @Override
    public int getStrength() {
        return 1;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));

            List<Effect> possibleEffects = new LinkedList<>();
            possibleEffects.add(
                    new TakeOffTheOneRingEffect());
            possibleEffects.add(
                    new CancelRingBearerAndNazgulSkirmish(self.getAttachedTo()));

            action.appendEffect(
                    new ChoiceEffect(action, playerId, possibleEffects));

            return Collections.singletonList(action);
        }
        return null;
    }

    private static class CancelRingBearerAndNazgulSkirmish extends CancelSkirmishEffect {
        private final PhysicalCard _attachedTo;

        private CancelRingBearerAndNazgulSkirmish(PhysicalCard attachedTo) {
            _attachedTo = attachedTo;
        }

        @Override
        public String getText(LotroGame game) {
            return "Cancel a skirmish involving the Ring-bearer and a Nazgul";
        }

        @Override
        public boolean isPlayableInFull(LotroGame game) {
            return super.isPlayableInFull(game)
                    && Filters.canSpot(game, Race.NAZGUL, Filters.inSkirmish)
                    && Filters.canSpot(game, Filters.ringBearer, Filters.inSkirmish);
        }
    }
}
