package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ModifiersQuerying {
    LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, Phase phase);

    LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase);

    LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, Phase phase);

    LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase);

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card);

    LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card, String prefix);

    Collection<Modifier> getModifiersAffecting(LotroGame game, PhysicalCard card);

    Evaluator getFPStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter);

    Evaluator getShadowStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter);

    boolean hasTextRemoved(LotroGame game, PhysicalCard card);

    // Keywords
    boolean hasKeyword(LotroGame game, PhysicalCard physicalCard, Keyword keyword);

    int getKeywordCount(LotroGame game, PhysicalCard physicalCard, Keyword keyword);

    boolean hasSignet(LotroGame game, PhysicalCard physicalCard, Signet signet);

    // Archery
    int getArcheryTotal(LotroGame game, Side side, int baseArcheryTotal);

    boolean addsToArcheryTotal(LotroGame game, PhysicalCard card);

    // Movement
    int getMoveLimit(LotroGame game, int baseMoveLimit);

    int getThreatLimit(LotroGame game, int baseThreatLimit);

    boolean addsTwilightForCompanionMove(LotroGame game, PhysicalCard companion);

    // Twilight cost
    int getTwilightCostToPlay(LotroGame game, PhysicalCard physicalCard, PhysicalCard target, int twilightCostModifier, boolean ignoreRoamingPenalty);
    int getCurrentTwilightCost(LotroGame game, PhysicalCard physicalCard);

    int getRoamingPenalty(LotroGame game, PhysicalCard physicalCard);

    // Stats
    int getStrength(LotroGame game, PhysicalCard physicalCard);

    boolean appliesStrengthBonusModifier(LotroGame game, PhysicalCard modifierSource, PhysicalCard modifierTarget);

    int getVitality(LotroGame game, PhysicalCard physicalCard);

    int getResistance(LotroGame game, PhysicalCard physicalCard);

    int getMinionSiteNumber(LotroGame game, PhysicalCard physicalCard);

    int getOverwhelmMultiplier(LotroGame game, PhysicalCard card);

    List<CardType> getCardTypes(LotroGame game, PhysicalCard card);
    boolean isCardType(LotroGame game, PhysicalCard card, CardType cardType);
    List<Race> getRaces(LotroGame game, PhysicalCard card);
    boolean isRace(LotroGame game, PhysicalCard card, Race race);
    List<Culture> getCultures(LotroGame game, PhysicalCard card);
    boolean isCulture(LotroGame game, PhysicalCard card, Culture culture);

    // Wounds/exertions
    boolean canTakeWounds(LotroGame game, Collection<PhysicalCard> woundSources, PhysicalCard card, int woundsToTake);

    boolean canTakeWoundsFromLosingSkirmish(LotroGame game, PhysicalCard card);

    boolean canTakeArcheryWound(LotroGame game, PhysicalCard card);

    boolean canBeExerted(LotroGame game, PhysicalCard exertionSource, PhysicalCard exertedCard);

    boolean canBeHealed(LotroGame game, PhysicalCard card);

    boolean canAddBurden(LotroGame game, String performingPlayer, PhysicalCard source);

    boolean canRemoveBurden(LotroGame game, PhysicalCard source);

    boolean canRemoveThreat(LotroGame game, PhysicalCard source);

    // Assignments
    boolean canBeAssignedToSkirmish(LotroGame game, Side playerSide, PhysicalCard card);

    boolean canCancelSkirmish(LotroGame game, PhysicalCard card);

    boolean isUnhastyCompanionAllowedToParticipateInSkirmishes(LotroGame game, PhysicalCard card);

    boolean isAllyAllowedToParticipateInArcheryFire(LotroGame game, PhysicalCard card);

    boolean isAllyAllowedToParticipateInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card);

    boolean isAllyPreventedFromParticipatingInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card);

    boolean isAllyPreventedFromParticipatingInArcheryFire(LotroGame game, PhysicalCard card);

    boolean isValidAssignments(LotroGame game, Side side, Map<PhysicalCard, Set<PhysicalCard>> assignments);

    // Playing actions
    boolean canPlayAction(LotroGame game, String performingPlayer, Action action);

    boolean canPlayCard(LotroGame game, String performingPlayer, PhysicalCard card);

    boolean canHavePlayedOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canHaveTransferredOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canBeTransferred(LotroGame game, PhysicalCard attachment);
    int bearableItemsOfClass(LotroGame game, PhysicalCard attachedTo, PossessionClass itemClass);

    boolean shouldSkipPhase(LotroGame game, Phase phase, String playerId);

    List<? extends Action> getExtraPhaseActions(LotroGame game, PhysicalCard target);

    boolean hasExtraPhaseActionsFromOtherPlayer(LotroGame game, PhysicalCard target);

    List<? extends Action> getExtraPhaseActionsFromStacked(LotroGame game, PhysicalCard target);

    boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard target);

    void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard target);

    // Others
    boolean canBeDiscardedFromPlay(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);
    boolean canBeHinderedBy(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(LotroGame game, PhysicalCard card, PhysicalCard source);

    boolean canDrawCardNoIncrement(LotroGame game, String playerId);

    boolean canDrawCardAndIncrementForRuleOfFour(LotroGame game, String playerId);
    int getFellowshipDrawnCards(LotroGame game);

    boolean canLookOrRevealCardsInHand(LotroGame game, String revealingPlayerId, String performingPlayerId);

    boolean canDiscardCardsFromHand(LotroGame game, String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(LotroGame game, String playerId, PhysicalCard source);

    boolean canBeLiberated(LotroGame game, String playerId, PhysicalCard card, PhysicalCard source);

    Side hasInitiative(LotroGame game);

    int getNumberOfSpottableFPCultures(LotroGame game, String playerId);

    int getNumberOfSpottableShadowCultures(LotroGame game, String playerId);

    int getNumberOfSpottableControlledSites(LotroGame game, String playerId);

    int getSpotBonus(LotroGame game, Filterable filter);

    boolean hasFlagActive(LotroGame game, ModifierFlag modifierFlag);

    boolean canReplaceSite(LotroGame game, String playerId, PhysicalCard siteToReplace);

    boolean canPlaySite(LotroGame game, String playerId);

    boolean assignmentCostWasPaid(LotroGame game, PhysicalCard card);

    int getSanctuaryHealModifier(LotroGame game);

    int getPotentialDiscount(LotroGame game, PhysicalCard playedCard);

    void appendPotentialDiscounts(LotroGame game, CostToEffectAction action, PhysicalCard playedCard);
}
