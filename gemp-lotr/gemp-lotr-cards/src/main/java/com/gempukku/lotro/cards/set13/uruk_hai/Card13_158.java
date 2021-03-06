package com.gempukku.lotro.cards.set13.uruk_hai;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.modifiers.ResistanceModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.CountActiveEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.MultiplyEvaluator;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Shadow
 * Culture: Uruk-hai
 * Twilight Cost: 4
 * Type: Minion • Uruk-Hai
 * Strength: 9
 * Vitality: 3
 * Site: 5
 * Game Text: Damage +1. Shadow: Exert this minion and spot a Free Peoples condition to make an unbound companion
 * resistance -1 for each card that has the same card title as that condition.
 */
public class Card13_158 extends AbstractMinion {
    public Card13_158() {
        super(4, 9, 3, 5, Race.URUK_HAI, Culture.URUK_HAI, "Assault Commander");
        addKeyword(Keyword.DAMAGE, 1);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SHADOW, self, 0)
                && PlayConditions.canSelfExert(self, game)
                && PlayConditions.canSpot(game, Side.FREE_PEOPLE, CardType.CONDITION)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendCost(
                    new ChooseActiveCardEffect(self, playerId, "Choose a Free Peoples condition") {
                        @Override
                        protected void cardSelected(LotroGame game, final PhysicalCard condition) {
                            action.appendEffect(
                                    new ChooseActiveCardEffect(self, playerId, "Choose an unbound companion", Filters.unboundCompanion) {
                                        @Override
                                        protected void cardSelected(LotroGame game, PhysicalCard companion) {
                                            action.appendEffect(
                                                    new AddUntilEndOfPhaseModifierEffect(
                                                            new ResistanceModifier(self, companion, null,
                                                                    new MultiplyEvaluator(-1, new CountActiveEvaluator(Filters.name(condition.getBlueprint().getTitle()))))));
                                        }
                                    });
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
