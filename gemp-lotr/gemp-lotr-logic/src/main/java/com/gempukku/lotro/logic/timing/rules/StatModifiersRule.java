package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.*;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class StatModifiersRule {
    private final ModifiersLogic modifiersLogic;

    public StatModifiersRule(ModifiersLogic modifiersLogic) {
        this.modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        modifiersLogic.addAlwaysOnModifier(
                new StrengthModifier(null, Filters.and(Filters.inPlay, Filters.character, Filters.hasAttached(Filters.any)), null,
                        new Evaluator() {
                            @Override
                            public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                int sum = 0;
                                var gameState = game.getGameState();
                                for (PhysicalCard attachedCard : gameState.getAttachedCards(cardAffected)) {
                                    if(gameState.isHindered(attachedCard))
                                        continue;
                                    final int strength = attachedCard.getBlueprint().getStrength();
                                    if (strength <= 0 || modifiersLogic.appliesStrengthBonusModifier(game, attachedCard, cardAffected))
                                        sum += strength;
                                }

                                return sum;
                            }
                        }, true));
        modifiersLogic.addAlwaysOnModifier(
                new VitalityModifier(null, Filters.and(Filters.inPlay, Filters.character, Filters.hasAttached(Filters.any)), null,
                        new Evaluator() {
                            @Override
                            public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                int sum = 0;
                                var gameState = game.getGameState();
                                for (PhysicalCard attachedCard : gameState.getAttachedCards(cardAffected)) {
                                    if (gameState.isHindered(attachedCard))
                                        continue;
                                    sum += attachedCard.getBlueprint().getVitality();
                                }

                                return sum;
                            }
                        }, true));
        modifiersLogic.addAlwaysOnModifier(
                new ResistanceModifier(null, Filters.and(Filters.inPlay, Filters.character, Filters.hasAttached(Filters.any)), null,
                        new Evaluator() {
                            @Override
                            public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                int sum = 0;
                                var gameState = game.getGameState();
                                for (PhysicalCard attachedCard : gameState.getAttachedCards(cardAffected)) {
                                    if (gameState.isHindered(attachedCard))
                                        continue;
                                    sum += attachedCard.getBlueprint().getResistance();
                                }

                                return sum;
                            }
                        }, true));
        modifiersLogic.addAlwaysOnModifier(
                new MinionSiteNumberModifier(null, Filters.and(Filters.inPlay, CardType.MINION, Filters.hasAttached(Filters.any)), null,
                        new Evaluator() {
                            @Override
                            public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                int sum = 0;
                                var gameState = game.getGameState();
                                for (PhysicalCard attachedCard : gameState.getAttachedCards(cardAffected)) {
                                    if (gameState.isHindered(attachedCard))
                                        continue;
                                    sum += attachedCard.getBlueprint().getSiteNumber();
                                }

                                return sum;
                            }
                        }, true));
    }
}
