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

public class DelegateModifier implements Modifier {
    protected final Modifier delegate;

    public DelegateModifier(Modifier delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean addsToArcheryTotal(LotroGame game, PhysicalCard card) {
        return delegate.addsToArcheryTotal(game, card);
    }

    @Override
    public boolean addsTwilightForCompanionMove(LotroGame game, PhysicalCard companion) {
        return delegate.addsTwilightForCompanionMove(game, companion);
    }

    @Override
    public boolean affectsCard(LotroGame game, PhysicalCard physicalCard) {
        return delegate.affectsCard(game, physicalCard);
    }

    @Override
    public void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard card) {
        delegate.appendExtraCosts(game, action, card);
    }

    @Override
    public void appendPotentialDiscounts(LotroGame game, CostToEffectAction action, PhysicalCard card) {
        delegate.appendPotentialDiscounts(game, action, card);
    }

    @Override
    public boolean appliesKeywordModifier(LotroGame game, PhysicalCard modifierSource, Keyword keyword) {
        return delegate.appliesKeywordModifier(game, modifierSource, keyword);
    }

    @Override
    public boolean appliesStrengthBonusModifier(LotroGame game, PhysicalCard modifierSource, PhysicalCard modifierTaget) {
        return delegate.appliesStrengthBonusModifier(game, modifierSource, modifierTaget);
    }

    @Override
    public boolean canAddBurden(LotroGame game, String performingPlayer, PhysicalCard source) {
        return delegate.canAddBurden(game, performingPlayer, source);
    }

    @Override
    public boolean canBeDiscardedFromPlay(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source) {
        return delegate.canBeDiscardedFromPlay(game, performingPlayer, card, source);
    }

    @Override
    public boolean canBeExerted(LotroGame game, PhysicalCard exertionSource, PhysicalCard exertedCard) {
        return delegate.canBeExerted(game, exertionSource, exertedCard);
    }

    @Override
    public boolean canBeHealed(LotroGame game, PhysicalCard card) {
        return delegate.canBeHealed(game, card);
    }

    @Override
    public boolean canBeLiberated(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source) {
        return delegate.canBeLiberated(game, performingPlayer, card, source);
    }

    @Override
    public boolean canBeReturnedToHand(LotroGame game, PhysicalCard card, PhysicalCard source) {
        return delegate.canBeReturnedToHand(game, card, source);
    }

    @Override
    public boolean canBeTransferred(LotroGame game, PhysicalCard attachment) {
        return delegate.canBeTransferred(game, attachment);
    }

    @Override
    public boolean canCancelSkirmish(LotroGame game, PhysicalCard physicalCard) {
        return delegate.canCancelSkirmish(game, physicalCard);
    }

    @Override
    public boolean canDiscardCardsFromHand(LotroGame game, String playerId, PhysicalCard source) {
        return delegate.canDiscardCardsFromHand(game, playerId, source);
    }

    @Override
    public boolean canDiscardCardsFromTopOfDeck(LotroGame game, String playerId, PhysicalCard source) {
        return delegate.canDiscardCardsFromTopOfDeck(game, playerId, source);
    }

    @Override
    public boolean canHavePlayedOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target) {
        return delegate.canHavePlayedOn(game, playedCard, target);
    }

    @Override
    public boolean canHaveTransferredOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target) {
        return delegate.canHaveTransferredOn(game, playedCard, target);
    }

    @Override
    public boolean canLookOrRevealCardsInHand(LotroGame game, String revealingPlayerId, String actingPlayerId) {
        return delegate.canLookOrRevealCardsInHand(game, revealingPlayerId, actingPlayerId);
    }

    @Override
    public boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard card) {
        return delegate.canPayExtraCostsToPlay(game, card);
    }

    @Override
    public boolean canPlayAction(LotroGame game, String performingPlayer, Action action) {
        return delegate.canPlayAction(game, performingPlayer, action);
    }

    @Override
    public boolean canPlayCard(LotroGame game, String performingPlayer, PhysicalCard card) {
        return delegate.canPlayCard(game, performingPlayer, card);
    }

    @Override
    public boolean canPlaySite(LotroGame game, String playerId) {
        return delegate.canPlaySite(game, playerId);
    }

    @Override
    public boolean canRemoveBurden(LotroGame game, PhysicalCard source) {
        return delegate.canRemoveBurden(game, source);
    }

    @Override
    public boolean canRemoveThreat(LotroGame game, PhysicalCard source) {
        return delegate.canRemoveThreat(game, source);
    }

    @Override
    public boolean canSpotCulture(LotroGame game, Culture culture, String playerId) {
        return delegate.canSpotCulture(game, culture, playerId);
    }

    @Override
    public boolean canTakeArcheryWound(LotroGame game, PhysicalCard physicalCard) {
        return delegate.canTakeArcheryWound(game, physicalCard);
    }

    @Override
    public boolean canTakeWounds(LotroGame game, Collection<PhysicalCard> woundSources, PhysicalCard physicalCard, int woundsAlreadyTakenInPhase, int woundsToTake) {
        return delegate.canTakeWounds(game, woundSources, physicalCard, woundsAlreadyTakenInPhase, woundsToTake);
    }

    @Override
    public boolean canTakeWoundsFromLosingSkirmish(LotroGame game, PhysicalCard physicalCard) {
        return delegate.canTakeWoundsFromLosingSkirmish(game, physicalCard);
    }

    @Override
    public int getArcheryTotalModifier(LotroGame game, Side side) {
        return delegate.getArcheryTotalModifier(game, side);
    }

    @Override
    public Condition getCondition() {
        return delegate.getCondition();
    }

    @Override
    public List<? extends Action> getExtraPhaseAction(LotroGame game, PhysicalCard card) {
        return delegate.getExtraPhaseAction(game, card);
    }

    @Override
    public List<? extends Action> getExtraPhaseActionFromStacked(LotroGame game, PhysicalCard card) {
        return delegate.getExtraPhaseActionFromStacked(game, card);
    }

    @Override
    public int getFPCulturesSpotCountModifier(LotroGame game, String playerId) {
        return delegate.getFPCulturesSpotCountModifier(game, playerId);
    }

    @Override
    public Evaluator getFpSkirmishStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter) {
        return delegate.getFpSkirmishStrengthOverrideEvaluator(game, fpCharacter);
    }

    @Override
    public int getInitiativeHandSizeModifier(LotroGame game) {
        return delegate.getInitiativeHandSizeModifier(game);
    }

    @Override
    public int getKeywordCountModifier(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        return delegate.getKeywordCountModifier(game, physicalCard, keyword);
    }

    @Override
    public int getMinionSiteNumberModifier(LotroGame game, PhysicalCard physicalCard) {
        return delegate.getMinionSiteNumberModifier(game, physicalCard);
    }

    @Override
    public ModifierEffect getModifierEffect() {
        return delegate.getModifierEffect();
    }

    @Override
    public int getMoveLimitModifier(LotroGame game) {
        return delegate.getMoveLimitModifier(game);
    }

    @Override
    public int getOverwhelmMultiplier(LotroGame game, PhysicalCard physicalCard) {
        return delegate.getOverwhelmMultiplier(game, physicalCard);
    }

    @Override
    public int getPotentialDiscount(LotroGame game, PhysicalCard discountCard) {
        return delegate.getPotentialDiscount(game, discountCard);
    }

    @Override
    public int getResistanceModifier(LotroGame game, PhysicalCard physicalCard) {
        return delegate.getResistanceModifier(game, physicalCard);
    }

    @Override
    public int getRoamingPenaltyModifier(LotroGame game, PhysicalCard physicalCard) {
        return delegate.getRoamingPenaltyModifier(game, physicalCard);
    }

    @Override
    public int getSanctuaryHealModifier(LotroGame game) {
        return delegate.getSanctuaryHealModifier(game);
    }

    @Override
    public Evaluator getShadowSkirmishStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter) {
        return delegate.getShadowSkirmishStrengthOverrideEvaluator(game, fpCharacter);
    }

    @Override
    public PhysicalCard getSource() {
        return delegate.getSource();
    }

    @Override
    public int getSpotCountModifier(LotroGame game, Filterable filter) {
        return delegate.getSpotCountModifier(game, filter);
    }

    @Override
    public int getStrengthModifier(LotroGame game, PhysicalCard physicalCard) {
        return delegate.getStrengthModifier(game, physicalCard);
    }

    @Override
    public String getText(LotroGame game, PhysicalCard self) {
        return delegate.getText(game, self);
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard physicalCard, PhysicalCard target, boolean ignoreRoamingPenalty) {
        return delegate.getTwilightCostModifier(game, physicalCard, target, ignoreRoamingPenalty);
    }

    @Override
    public int getVitalityModifier(LotroGame game, PhysicalCard physicalCard) {
        return delegate.getVitalityModifier(game, physicalCard);
    }

    @Override
    public boolean hasFlagActive(LotroGame game, ModifierFlag modifierFlag) {
        return delegate.hasFlagActive(game, modifierFlag);
    }

    @Override
    public Side hasInitiative(LotroGame game) {
        return delegate.hasInitiative(game);
    }

    @Override
    public boolean hasKeyword(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        return delegate.hasKeyword(game, physicalCard, keyword);
    }

    @Override
    public boolean hasRemovedText(LotroGame game, PhysicalCard physicalCard) {
        return delegate.hasRemovedText(game, physicalCard);
    }

    @Override
    public boolean hasSignet(LotroGame game, PhysicalCard physicalCard, Signet signet) {
        return delegate.hasSignet(game, physicalCard, signet);
    }

    @Override
    public boolean isAdditionalCardTypeModifier(LotroGame game, PhysicalCard physicalCard, CardType cardType) {
        return delegate.isAdditionalCardTypeModifier(game, physicalCard, cardType);
    }

    @Override
    public boolean isAllyParticipateInArcheryFire(LotroGame game, PhysicalCard card) {
        return delegate.isAllyParticipateInArcheryFire(game, card);
    }

    @Override
    public boolean isAllyParticipateInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card) {
        return delegate.isAllyParticipateInSkirmishes(game, sidePlayer, card);
    }

    @Override
    public boolean isAllyPreventedFromParticipatingInArcheryFire(LotroGame game, PhysicalCard card) {
        return delegate.isAllyPreventedFromParticipatingInArcheryFire(game, card);
    }

    @Override
    public boolean isAllyPreventedFromParticipatingInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card) {
        return delegate.isAllyPreventedFromParticipatingInSkirmishes(game, sidePlayer, card);
    }

    @Override
    public boolean isAssignmentCostPaid(LotroGame game, PhysicalCard card) {
        return delegate.isAssignmentCostPaid(game, card);
    }

    @Override
    public boolean isExtraPossessionClass(LotroGame game, PhysicalCard physicalCard, PhysicalCard attachedTo) {
        return delegate.isExtraPossessionClass(game, physicalCard, attachedTo);
    }

    @Override
    public boolean isKeywordRemoved(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        return delegate.isKeywordRemoved(game, physicalCard, keyword);
    }

    @Override
    public boolean isNonCardTextModifier() {
        return delegate.isNonCardTextModifier();
    }

    @Override
    public boolean isPreventedFromBeingAssignedToSkirmish(LotroGame game, Side sidePlayer, PhysicalCard card) {
        return delegate.isPreventedFromBeingAssignedToSkirmish(game, sidePlayer, card);
    }

    @Override
    public boolean isSiteReplaceable(LotroGame game, String playerId) {
        return delegate.isSiteReplaceable(game, playerId);
    }

    @Override
    public boolean isUnhastyCompanionAllowedToParticipateInSkirmishes(LotroGame game, PhysicalCard card) {
        return delegate.isUnhastyCompanionAllowedToParticipateInSkirmishes(game, card);
    }

    @Override
    public boolean isValidAssignments(LotroGame game, Side Side, Map<PhysicalCard, Set<PhysicalCard>> assignments) {
        return delegate.isValidAssignments(game, Side, assignments);
    }

    @Override
    public boolean isValidAssignments(LotroGame game, Side Side, PhysicalCard companion, Set<PhysicalCard> minions) {
        return delegate.isValidAssignments(game, Side, companion, minions);
    }

    @Override
    public boolean lostAllKeywords(LotroGame game, PhysicalCard card) {
        return delegate.lostAllKeywords(game, card);
    }

    @Override
    public boolean shadowCanHaveInitiative(LotroGame game) {
        return delegate.shadowCanHaveInitiative(game);
    }

    @Override
    public boolean shouldSkipPhase(LotroGame game, Phase phase, String playerId) {
        return delegate.shouldSkipPhase(game, phase, playerId);
    }
}
