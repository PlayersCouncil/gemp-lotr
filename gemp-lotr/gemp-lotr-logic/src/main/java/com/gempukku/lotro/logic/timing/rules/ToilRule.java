package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.cards.build.ActionSource;
import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.discount.ToilDiscountEffect;
import com.gempukku.lotro.logic.modifiers.AbstractModifier;
import com.gempukku.lotro.logic.modifiers.ModifierEffect;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;

import java.util.HashMap;
import java.util.Map;

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
                            return toilCount * countMostToilPaid(game, discountCard, toilCount);

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

    /**
     * The most times the toil number can come off the card's cost: once for each character of its culture its
     * player could exert for toil, and once for each use of a Response of his that answers the toil about to be
     * paid (Úlairë Enquëa 12_175, "exert Úlairë Enquëa to reduce that event's twilight cost by X": a `beforeToil`
     * Response with `incrementToil`, the one effect such a Response is written with).  Ketura (Q-0381, A): the
     * response counts toward whether the card can be played.
     * <p>
     * A Response may be used more than once (CR 4.2 "response"), so each one is counted in uses in a row, each
     * asked with the wounds of the uses before it in place (ActionSource.tallyWoundsOfPayingCosts), as long as it
     * can still be paid; a use whose costs place no wound that can be foreseen is counted once.  The characters
     * that could still exert for toil are counted after those wounds, so a character the responses exhaust is not
     * counted twice.  Only a card's own written Responses are asked, not text it copies from another card.
     */
    private static int countMostToilPaid(LotroGame game, PhysicalCard card, int toilCount) {
        final String player = card.getOwner();
        final GameState gameState = game.getGameState();
        final ToilDiscountEffect toilToCome = new ToilDiscountEffect(null, card, player, card.getBlueprint().getCulture(), toilCount);

        final Map<PhysicalCard, Integer> wounds = new HashMap<>();
        int uses = 0;
        int most = countToilCandidates(game, card);
        for (PhysicalCard responder : Filters.filterActive(game, Filters.owner(player))) {
            if (!(responder.getBlueprint() instanceof BuiltLotroCardBlueprint built))
                continue;
            for (ActionSource response : built.getOwnInPlayBeforeResponses()) {
                final DefaultActionContext context = new DefaultActionContext(player, game, responder, null, toilToCome);
                while (gameState.withWoundsToCome(wounds, () -> response.isValid(context))) {
                    final Map<PhysicalCard, Integer> placed = new HashMap<>();
                    gameState.withWoundsToCome(wounds, () -> {
                        response.tallyWoundsOfPayingCosts(context, placed);
                        return null;
                    });
                    uses++;
                    if (placed.isEmpty()) {
                        most = Math.max(most, uses + gameState.withWoundsToCome(wounds, () -> countToilCandidates(game, card)));
                        break;
                    }
                    for (Map.Entry<PhysicalCard, Integer> entry : placed.entrySet())
                        wounds.merge(entry.getKey(), entry.getValue(), Integer::sum);
                    most = Math.max(most, uses + gameState.withWoundsToCome(wounds, () -> countToilCandidates(game, card)));
                }
            }
        }
        return most;
    }

    private static int countToilCandidates(LotroGame game, PhysicalCard card) {
        return Filters.countActive(game, Filters.owner(card.getOwner()),
                card.getBlueprint().getCulture(), Filters.character, Filters.canExert(card));
    }
}
