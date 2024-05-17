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
    public LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, Phase phase);

    public LimitCounter getUntilEndOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase);

    public LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, Phase phase);

    public LimitCounter getUntilStartOfPhaseLimitCounter(PhysicalCard card, String prefix, Phase phase);

    public LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card);

    public LimitCounter getUntilEndOfTurnLimitCounter(PhysicalCard card, String prefix);

    public Collection<Modifier> getModifiersAffecting(LotroGame game, PhysicalCard card);

    public Evaluator getFPStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter);
    public Evaluator getShadowStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter);

    public boolean hasTextRemoved(LotroGame game, PhysicalCard card);

    // Keywords
    public boolean hasKeyword(LotroGame game, PhysicalCard physicalCard, Keyword keyword);

    public int getKeywordCount(LotroGame game, PhysicalCard physicalCard, Keyword keyword);

    public boolean hasSignet(LotroGame game, PhysicalCard physicalCard, Signet signet);

    // Archery
    public int getArcheryTotal(LotroGame game, Side side, int baseArcheryTotal);

    public boolean addsToArcheryTotal(LotroGame game, PhysicalCard card);

    // Movement
    public int getMoveLimit(LotroGame game, int baseMoveLimit);

    public boolean addsTwilightForCompanionMove(LotroGame game, PhysicalCard companion);

    // Twilight cost
    public int getTwilightCostToPlay(LotroGame game, PhysicalCard physicalCard, PhysicalCard target, int twilightCostModifier, boolean ignoreRoamingPenalty);

    public int getRoamingPenalty(LotroGame game, PhysicalCard physicalCard);

    // Stats
    public int getStrength(LotroGame game, PhysicalCard physicalCard);

    public boolean appliesStrengthBonusModifier(LotroGame game, PhysicalCard modifierSource, PhysicalCard modifierTarget);

    public int getVitality(LotroGame game, PhysicalCard physicalCard);

    public int getResistance(LotroGame game, PhysicalCard physicalCard);

    public int getMinionSiteNumber(LotroGame game, PhysicalCard physicalCard);

    public int getOverwhelmMultiplier(LotroGame game, PhysicalCard card);

    public boolean isAdditionalCardType(LotroGame game, PhysicalCard card, CardType cardType);

    // Wounds/exertions
    public boolean canTakeWounds(LotroGame game, Collection<PhysicalCard> woundSources, PhysicalCard card, int woundsToTake);

    public boolean canTakeWoundsFromLosingSkirmish(LotroGame game, PhysicalCard card, Set<PhysicalCard> winners);

    public boolean canTakeArcheryWound(LotroGame game, PhysicalCard card);

    public boolean canBeExerted(LotroGame game, PhysicalCard exertionSource, PhysicalCard exertedCard);

    public boolean canBeHealed(LotroGame game, PhysicalCard card);

    public boolean canAddBurden(LotroGame game, String performingPlayer, PhysicalCard source);

    public boolean canRemoveBurden(LotroGame game, PhysicalCard source);

    public boolean canRemoveThreat(LotroGame game, PhysicalCard source);

    // Assignments
    public boolean canBeAssignedToSkirmish(LotroGame game, Side playerSide, PhysicalCard card);

    boolean canCancelSkirmish(LotroGame game, PhysicalCard card);

    public boolean isUnhastyCompanionAllowedToParticipateInSkirmishes(LotroGame game, PhysicalCard card);

    public boolean isAllyAllowedToParticipateInArcheryFire(LotroGame game, PhysicalCard card);

    public boolean isAllyAllowedToParticipateInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card);

    public boolean isAllyPreventedFromParticipatingInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card);

    public boolean isAllyPreventedFromParticipatingInArcheryFire(LotroGame game, PhysicalCard card);

    public boolean isValidAssignments(LotroGame game, Side side, Map<PhysicalCard, Set<PhysicalCard>> assignments);

    // Playing actions
    public boolean canPlayAction(LotroGame game, String performingPlayer, Action action);

    public boolean canPlayCard(LotroGame game, String performingPlayer, PhysicalCard card);

    public boolean canHavePlayedOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target);

    public boolean canHaveTransferredOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target);

    public boolean canBeTransferred(LotroGame game, PhysicalCard attachment);

    public boolean shouldSkipPhase(LotroGame game, Phase phase, String playerId);

    public List<? extends Action> getExtraPhaseActions(LotroGame game, PhysicalCard target);

    public List<? extends Action> getExtraPhaseActionsFromStacked(LotroGame game, PhysicalCard target);

    public boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard target);

    public void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard target);

    // Others
    public boolean canBeDiscardedFromPlay(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);

    public boolean canBeReturnedToHand(LotroGame game, PhysicalCard card, PhysicalCard source);

    public boolean canDrawCardNoIncrement(LotroGame game, String playerId);

    public boolean canDrawCardAndIncrementForRuleOfFour(LotroGame game, String playerId);

    public boolean canLookOrRevealCardsInHand(LotroGame game, String revealingPlayerId, String performingPlayerId);

    public boolean canDiscardCardsFromHand(LotroGame game, String playerId, PhysicalCard source);

    public boolean canDiscardCardsFromTopOfDeck(LotroGame game, String playerId, PhysicalCard source);

    public boolean canBeLiberated(LotroGame game, String playerId, PhysicalCard card, PhysicalCard source);

    public Side hasInitiative(LotroGame game);

    public int getNumberOfSpottableFPCultures(LotroGame game, String playerId);

    public int getNumberOfSpottableShadowCultures(LotroGame game, String playerId);

    public int getSpotBonus(LotroGame game, Filterable filter);

    public boolean hasFlagActive(LotroGame game, ModifierFlag modifierFlag);

    public boolean canReplaceSite(LotroGame game, String playerId, PhysicalCard siteToReplace);

    public boolean canPlaySite(LotroGame game, String playerId);

    boolean assignmentCostWasPaid(LotroGame game, PhysicalCard card);

    int getSanctuaryHealModifier(LotroGame game);

    int getPotentialDiscount(LotroGame game, PhysicalCard playedCard);

    void appendPotentialDiscounts(LotroGame game, CostToEffectAction action, PhysicalCard playedCard);
}
