package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.discount.ToilDiscountEffect;
import com.gempukku.lotro.logic.modifiers.AbstractModifier;
import com.gempukku.lotro.logic.modifiers.ModifierEffect;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;

public class ToilRule {
    private final ModifiersLogic modifiersLogic;

    public ToilRule(ModifiersLogic modifiersLogic) {
        this.modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        modifiersLogic.addAlwaysOnModifier(
                new AbstractModifier(null, "Toil discount", Keyword.TOIL, null, ModifierEffect.POTENTIAL_DISCOUNT_MODIFIER) {
                    @Override
                    public int getPotentialDiscount(LotroGame game, PhysicalCard discountCard) {
                        int toilCount = game.getModifiersQuerying().getKeywordCount(game, discountCard, Keyword.TOIL);
                        if (toilCount > 0)
                            return toilCount * Filters.countActive(game, Filters.owner(discountCard.getOwner()),
                                    discountCard.getBlueprint().getCulture(), Filters.character, Filters.canExert(discountCard));

                        return 0;
                    }

                    @Override
                    public void appendPotentialDiscounts(LotroGame game, CostToEffectAction action, PhysicalCard card) {
                        int toilCount = game.getModifiersQuerying().getKeywordCount(game, card, Keyword.TOIL);
                        if (toilCount > 0)
                            action.appendPotentialDiscount(
                                    new ToilDiscountEffect(action, card, card.getOwner(), card.getBlueprint().getCulture(), toilCount));
                    }
                });
    }
}
