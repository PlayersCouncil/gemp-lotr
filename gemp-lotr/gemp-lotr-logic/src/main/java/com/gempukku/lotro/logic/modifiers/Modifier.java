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

public interface Modifier {
    PhysicalCard getSource();

    String getText(LotroGame game, PhysicalCard self);

    ModifierEffect getModifierEffect();

    boolean isNonCardTextModifier();

    Condition getCondition();

    boolean isExtraPossessionClass(LotroGame game, PhysicalCard physicalCard, PhysicalCard attachedTo);

    boolean affectsCard(LotroGame game, PhysicalCard physicalCard);

    boolean hasRemovedText(LotroGame game, PhysicalCard physicalCard);

    boolean hasKeyword(LotroGame game, PhysicalCard physicalCard, Keyword keyword);

    boolean hasSignet(LotroGame game, PhysicalCard physicalCard, Signet signet);

    int getKeywordCountModifier(LotroGame game, PhysicalCard physicalCard, Keyword keyword);

    boolean appliesKeywordModifier(LotroGame game, PhysicalCard modifierSource, Keyword keyword);

    boolean isKeywordRemoved(LotroGame game, PhysicalCard physicalCard, Keyword keyword);

    int getStrengthModifier(LotroGame game, PhysicalCard physicalCard);

    boolean appliesStrengthBonusModifier(LotroGame game, PhysicalCard modifierSource, PhysicalCard modifierTaget);

    int getVitalityModifier(LotroGame game, PhysicalCard physicalCard);

    int getResistanceModifier(LotroGame game, PhysicalCard physicalCard);

    int getMinionSiteNumberModifier(LotroGame game, PhysicalCard physicalCard);

    boolean isAdditionalCardTypeModifier(LotroGame game, PhysicalCard physicalCard, CardType cardType);

    int getTwilightCostModifier(LotroGame game, PhysicalCard physicalCard, PhysicalCard target, boolean ignoreRoamingPenalty);

    int getRoamingPenaltyModifier(LotroGame game, PhysicalCard physicalCard);

    int getOverwhelmMultiplier(LotroGame game, PhysicalCard physicalCard);

    boolean canCancelSkirmish(LotroGame game, PhysicalCard physicalCard);

    boolean canTakeWounds(LotroGame game, Collection<PhysicalCard> woundSources, PhysicalCard physicalCard, int woundsAlreadyTakenInPhase, int woundsToTake);

    boolean canTakeWoundsFromLosingSkirmish(LotroGame game, PhysicalCard physicalCard);

    boolean canTakeArcheryWound(LotroGame game, PhysicalCard physicalCard);

    boolean canBeExerted(LotroGame game, PhysicalCard exertionSource, PhysicalCard exertedCard);

    boolean isAllyParticipateInArcheryFire(LotroGame game, PhysicalCard card);

    boolean isAllyParticipateInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card);

    boolean isUnhastyCompanionAllowedToParticipateInSkirmishes(LotroGame game, PhysicalCard card);

    boolean isAssignmentCostPaid(LotroGame game, PhysicalCard card);

    boolean isAllyPreventedFromParticipatingInArcheryFire(LotroGame game, PhysicalCard card);

    boolean isAllyPreventedFromParticipatingInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card);

    int getArcheryTotalModifier(LotroGame game, Side side);

    int getMoveLimitModifier(LotroGame game);

    boolean addsTwilightForCompanionMove(LotroGame game, PhysicalCard companion);

    boolean addsToArcheryTotal(LotroGame game, PhysicalCard card);

    boolean canPlayAction(LotroGame game, String performingPlayer, Action action);

    boolean canPlayCard(LotroGame game, String performingPlayer, PhysicalCard card);

    List<? extends Action> getExtraPhaseAction(LotroGame game, PhysicalCard card);

    List<? extends Action> getExtraPhaseActionFromStacked(LotroGame game, PhysicalCard card);

    boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard card);

    void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard card);

    boolean canHavePlayedOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canHaveTransferredOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target);

    boolean canBeTransferred(LotroGame game, PhysicalCard attachment);

    boolean shouldSkipPhase(LotroGame game, Phase phase, String playerId);

    boolean isValidAssignments(LotroGame game, Side Side, PhysicalCard companion, Set<PhysicalCard> minions);

    boolean isValidAssignments(LotroGame game, Side Side, Map<PhysicalCard, Set<PhysicalCard>> assignments);

    boolean isPreventedFromBeingAssignedToSkirmish(LotroGame game, Side sidePlayer, PhysicalCard card);

    boolean canBeDiscardedFromPlay(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeLiberated(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source);

    boolean canBeReturnedToHand(LotroGame game, PhysicalCard card, PhysicalCard source);

    boolean canBeHealed(LotroGame game, PhysicalCard card);

    boolean canAddBurden(LotroGame game, String performingPlayer, PhysicalCard source);

    boolean canRemoveBurden(LotroGame game, PhysicalCard source);

    boolean canRemoveThreat(LotroGame game, PhysicalCard source);

    boolean canLookOrRevealCardsInHand(LotroGame game, String revealingPlayerId, String actingPlayerId);

    boolean canDiscardCardsFromHand(LotroGame game, String playerId, PhysicalCard source);

    boolean canDiscardCardsFromTopOfDeck(LotroGame game, String playerId, PhysicalCard source);

    int getSpotCountModifier(LotroGame game, Filterable filter);

    boolean hasFlagActive(LotroGame game, ModifierFlag modifierFlag);

    boolean isSiteReplaceable(LotroGame game, String playerId);

    boolean canPlaySite(LotroGame game, String playerId);

    boolean shadowCanHaveInitiative(LotroGame game);

    Side hasInitiative(LotroGame game);

    int getInitiativeHandSizeModifier(LotroGame game);

    boolean lostAllKeywords(LotroGame game, PhysicalCard card);

    Evaluator getFpSkirmishStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter);
    Evaluator getShadowSkirmishStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter);

    boolean canSpotCulture(LotroGame game, Culture culture, String playerId);

    int getFPCulturesSpotCountModifier(LotroGame game, String playerId);

    int getSiteControlledSpotCountModifier(LotroGame game, String playerId);

    int getSanctuaryHealModifier(LotroGame game);

    int getPotentialDiscount(LotroGame game, PhysicalCard discountCard);

    void appendPotentialDiscounts(LotroGame game, CostToEffectAction action, PhysicalCard card);
}
