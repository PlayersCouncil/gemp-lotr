package com.gempukku.lotro.cards.set13.gollum;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.ShuffleDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPutCardFromDeckIntoDiscardEffect;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ResistanceModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.cost.AddBurdenExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Free
 * Culture: Gollum
 * Twilight Cost: 0
 * Type: Companion
 * Strength: 3
 * Vitality: 4
 * Resistance: 5
 * Game Text: Ring-bound. To play, add a burden. For each card titled Deagol in your discard pile, Smeagol is
 * strength +2 and resistance -1. Each time Smeagol wins a skirmish, you may take a [GOLLUM] card from your draw deck
 * and place it in your discard pile.
 */
public class Card13_055 extends AbstractCompanion {
    public Card13_055() {
        super(0, 3, 4, 5, Culture.GOLLUM, null, null, "Sméagol", "Simple Stoor", true);
        addKeyword(Keyword.RING_BOUND);
    }

    @Override
    public List<? extends AbstractExtraPlayCostModifier> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new AddBurdenExtraPlayCostModifier(self, 1, null, self));
    }

        @Override
        public List<? extends Modifier> getInPlayModifiers(LotroGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new StrengthModifier(self, self, null,
                        new Evaluator() {
                            @Override
                            public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                return Filters.filter(game.getGameState().getDiscard(self.getOwner()), game, Filters.name("Deagol")).size() * 2;
                            }
                        }));
        modifiers.add(
                new ResistanceModifier(self, self, null,
                        new Evaluator() {
                            @Override
                            public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                return -Filters.filter(game.getGameState().getDiscard(self.getOwner()), game, Filters.name("Deagol")).size();
                            }
                        }));
        return modifiers;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.winsSkirmish(game, effectResult, self)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
                    new ChooseAndPutCardFromDeckIntoDiscardEffect(action, playerId, 1, 1, Culture.GOLLUM));
            action.appendEffect(
                    new ShuffleDeckEffect(playerId));
            return Collections.singletonList(action);
        }
        return null;
    }
}
