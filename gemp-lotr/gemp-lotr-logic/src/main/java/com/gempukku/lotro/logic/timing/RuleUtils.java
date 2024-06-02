package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RuleUtils {
    public static int calculateArcheryTotal(LotroGame game, Side side) {
        if (side == Side.FREE_PEOPLE)
            return calculateFellowshipArcheryTotal(game);
        else
            return calculateShadowArcheryTotal(game);
    }

    public static int calculateFellowshipArcheryTotal(LotroGame game) {
        int normalArcheryTotal = Filters.countActive(game,
                Filters.or(
                        CardType.COMPANION,
                        Filters.and(
                                CardType.ALLY,
                                Filters.or(
                                        Filters.and(
                                                Filters.allyAtHome,
                                                new Filter() {
                                                    @Override
                                                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                                        return !game.getModifiersQuerying().isAllyPreventedFromParticipatingInArcheryFire(game, physicalCard);
                                                    }
                                                }),
                                        new Filter() {
                                            @Override
                                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                                return game.getModifiersQuerying().isAllyAllowedToParticipateInArcheryFire(game, physicalCard);
                                            }
                                        })
                        )
                ),
                Keyword.ARCHER,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return game.getModifiersQuerying().addsToArcheryTotal(game, physicalCard);
                    }
                });

        return game.getModifiersQuerying().getArcheryTotal(game, Side.FREE_PEOPLE, normalArcheryTotal);
    }

    public static int calculateShadowArcheryTotal(LotroGame game) {
        int normalArcheryTotal = Filters.countActive(game,
                CardType.MINION,
                Keyword.ARCHER,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return game.getModifiersQuerying().addsToArcheryTotal(game, physicalCard);
                    }
                });

        return game.getModifiersQuerying().getArcheryTotal(game, Side.SHADOW, normalArcheryTotal);
    }

    public static int calculateMoveLimit(LotroGame game) {
        return game.getModifiersQuerying().getMoveLimit(game, 2);
    }

    public static int getFellowshipSkirmishStrength(LotroGame game) {
        final Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish == null)
            return 0;

        PhysicalCard fpChar = skirmish.getFellowshipCharacter();
        if (fpChar == null)
            return 0;

        final Evaluator fpStrengthOverrideEvaluator = game.getModifiersQuerying().getFPStrengthOverrideEvaluator(game, fpChar);
        if (fpStrengthOverrideEvaluator != null)
            return fpStrengthOverrideEvaluator.evaluateExpression(game, fpChar);

        final Evaluator overrideEvaluator = skirmish.getFpStrengthOverrideEvaluator();
        if (overrideEvaluator != null)
            return overrideEvaluator.evaluateExpression(game, fpChar);

        return game.getModifiersQuerying().getStrength(game, fpChar);
    }

    public static int getShadowSkirmishStrength(LotroGame game) {
        final Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish == null)
            return 0;

        int total = 0;
        final Evaluator overrideEvaluator = skirmish.getShadowStrengthOverrideEvaluator();
        for (PhysicalCard minion : skirmish.getShadowCharacters()) {
            final Evaluator modifierOverrideEvaluator = game.getModifiersQuerying().getShadowStrengthOverrideEvaluator(game, minion);
            if(modifierOverrideEvaluator != null) {
                total += modifierOverrideEvaluator.evaluateExpression(game, minion);
            }
            else if(overrideEvaluator != null) {
                total += overrideEvaluator.evaluateExpression(game, minion);
            }
            else {
                total += game.getModifiersQuerying().getStrength(game, minion);
            }
        }

        return total;
    }

    public static int getFellowshipSkirmishDamageBonus(LotroGame game) {
        if (game.getGameState().getSkirmish() == null)
            return 0;

        PhysicalCard fpChar = game.getGameState().getSkirmish().getFellowshipCharacter();
        if (fpChar == null)
            return 0;

        return game.getModifiersQuerying().getKeywordCount(game, fpChar, Keyword.DAMAGE);
    }

    public static int getShadowSkirmishDamageBonus(LotroGame game) {
        if (game.getGameState().getSkirmish() == null)
            return 0;

        int totalBonus = 0;

        for (PhysicalCard physicalCard : game.getGameState().getSkirmish().getShadowCharacters())
            totalBonus += game.getModifiersQuerying().getKeywordCount(game, physicalCard, Keyword.DAMAGE);

        return totalBonus;
    }

    public static Filter getFullValidTargetFilter(String playerId, final LotroGame game, final PhysicalCard self) {
        final LotroCardBlueprint blueprint = self.getBlueprint();
        return Filters.and(blueprint.getValidTargetFilter(playerId, game, self),
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        final CardType thisType = blueprint.getCardType();
                        if (thisType == CardType.POSSESSION || thisType == CardType.ARTIFACT) {
                            final CardType targetType = physicalCard.getBlueprint().getCardType();
                            return targetType == CardType.COMPANION || targetType == CardType.ALLY
                                    || targetType == CardType.MINION;
                        }
                        return true;
                    }
                },
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard attachedTo) {
                        Set<PossessionClass> possessionClasses = blueprint.getPossessionClasses();
                        if (possessionClasses != null) {
                            for (PossessionClass possessionClass : possessionClasses) {
                                List<PhysicalCard> attachedCards = game.getGameState().getAttachedCards(attachedTo);

                                Collection<PhysicalCard> matchingClassPossessions = Filters.filter(game, attachedCards, Filters.or(CardType.POSSESSION, CardType.ARTIFACT), possessionClass);
                                if (matchingClassPossessions.size() > 1)
                                    return false;

                                boolean extraPossessionClass = self.getBlueprint().isExtraPossessionClass(game, self, attachedTo);
                                if (!extraPossessionClass && matchingClassPossessions.size() == 1) {
                                    final PhysicalCard attachedPossession = matchingClassPossessions.iterator().next();
                                    if (!attachedPossession.getBlueprint().isExtraPossessionClass(game, attachedPossession, attachedTo))
                                        return false;
                                }
                            }
                        }
                        return true;
                    }
                });
    }

    public static boolean isAllyAtHome(PhysicalCard ally, int siteNumber, SitesBlock siteBlock) {
        for(var home : ally.getBlueprint().getAllyHomes()) {
            if(home.block() == siteBlock && home.siteNum() == siteNumber)
                return true;
        }

        return false;
    }

    public static boolean isAllyInRegion(PhysicalCard ally, int regionNumber, SitesBlock siteBlock) {
        for(var home : ally.getBlueprint().getAllyHomes()) {
            if(home.block() == siteBlock && GameUtils.getRegion(home.siteNum()) == regionNumber)
                return true;
        }

        return false;
    }

}
