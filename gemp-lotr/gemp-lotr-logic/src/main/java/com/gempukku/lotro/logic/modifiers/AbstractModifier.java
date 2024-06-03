package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractModifier implements Modifier {
    private final PhysicalCard _physicalCard;
    private final String _text;
    private final Filter _affectFilter;
    private final Condition _condition;
    private final ModifierEffect _effect;

    protected AbstractModifier(PhysicalCard source, String text, Filterable affectFilter, Condition condition, ModifierEffect effect) {
        _physicalCard = source;
        _text = text;
        _affectFilter = (affectFilter != null) ? Filters.changeToFilter(affectFilter) : null;
        _condition = condition;
        _effect = effect;
    }

    @Override
    public boolean isNonCardTextModifier() {
        return false;
    }

    @Override
    public Condition getCondition() {
        return _condition;
    }

    @Override
    public PhysicalCard getSource() {
        return _physicalCard;
    }

    @Override
    public boolean isExtraPossessionClass(LotroGame game, PhysicalCard physicalCard, PhysicalCard attachedTo) {
        return false;
    }

    @Override
    public String getText(LotroGame game, PhysicalCard self) {
        return _text;
    }

    @Override
    public ModifierEffect getModifierEffect() {
        return _effect;
    }

    @Override
    public boolean affectsCard(LotroGame game, PhysicalCard physicalCard) {
        return (_affectFilter != null && _affectFilter.accepts(game, physicalCard));
    }

    @Override
    public boolean hasRemovedText(LotroGame game, PhysicalCard physicalCard) {
        return false;
    }

    @Override
    public boolean isKeywordRemoved(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        return false;
    }

    @Override
    public boolean hasKeyword(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        return false;
    }

    @Override
    public int getKeywordCountModifier(LotroGame game, PhysicalCard physicalCard, Keyword keyword) {
        return 0;
    }

    @Override
    public boolean appliesKeywordModifier(LotroGame game, PhysicalCard modifierSource, Keyword keyword) {
        return true;
    }

    @Override
    public boolean hasSignet(LotroGame game, PhysicalCard physicalCard, Signet signet) {
        return false;
    }

    @Override
    public int getStrengthModifier(LotroGame game, PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public boolean appliesStrengthBonusModifier(LotroGame game, PhysicalCard modifierSource, PhysicalCard modifierTaget) {
        return true;
    }

    @Override
    public int getVitalityModifier(LotroGame game, PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public int getResistanceModifier(LotroGame game, PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public int getMinionSiteNumberModifier(LotroGame game, PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public boolean isAdditionalCardTypeModifier(LotroGame game, PhysicalCard physicalCard, CardType cardType) {
        return false;
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard physicalCard, PhysicalCard target, boolean ignoreRoamingPenalty) {
        return 0;
    }

    @Override
    public int getOverwhelmMultiplier(LotroGame game, PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public boolean canCancelSkirmish(LotroGame game, PhysicalCard physicalCard) { return true; }

    @Override
    public boolean canTakeWounds(LotroGame game, Collection<PhysicalCard> woundSources, PhysicalCard physicalCard, int woundsAlreadyTaken, int woundsToTake) {
        return true;
    }

    @Override
    public boolean canTakeWoundsFromLosingSkirmish(LotroGame game, PhysicalCard physicalCard) {
        return true;
    }

    @Override
    public boolean canTakeArcheryWound(LotroGame game, PhysicalCard physicalCard) {
        return true;
    }

    @Override
    public boolean canBeExerted(LotroGame game, PhysicalCard exertionSource, PhysicalCard exertedCard) {
        return true;
    }

    @Override
    public boolean isAllyParticipateInArcheryFire(LotroGame game, PhysicalCard card) {
        return false;
    }

    @Override
    public boolean isAllyParticipateInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card) {
        return false;
    }

    @Override
    public boolean isUnhastyCompanionAllowedToParticipateInSkirmishes(LotroGame game, PhysicalCard card) {
        return false;
    }

    @Override
    public boolean isAssignmentCostPaid(LotroGame game, PhysicalCard card) {
        return false;
    }

    @Override
    public boolean isAllyPreventedFromParticipatingInArcheryFire(LotroGame game, PhysicalCard card) {
        return false;
    }

    @Override
    public boolean isAllyPreventedFromParticipatingInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card) {
        return false;
    }

    @Override
    public int getArcheryTotalModifier(LotroGame game, Side side) {
        return 0;
    }

    @Override
    public int getMoveLimitModifier(LotroGame game) {
        return 0;
    }

    @Override
    public boolean addsTwilightForCompanionMove(LotroGame game, PhysicalCard companion) {
        return true;
    }

    @Override
    public boolean addsToArcheryTotal(LotroGame game, PhysicalCard card) {
        return true;
    }

    @Override
    public boolean canPlayAction(LotroGame game, String performingPlayer, Action action) {
        return true;
    }

    @Override
    public boolean canPlayCard(LotroGame game, String performingPlayer, PhysicalCard card) {
        return true;
    }

    @Override
    public List<? extends Action> getExtraPhaseAction(LotroGame game, PhysicalCard card) {
        return null;
    }

    @Override
    public List<? extends Action> getExtraPhaseActionFromStacked(LotroGame game, PhysicalCard card) {
        return null;
    }

    @Override
    public boolean canPayExtraCostsToPlay(LotroGame game, PhysicalCard card) {
        return true;
    }

    @Override
    public void appendExtraCosts(LotroGame game, CostToEffectAction action, PhysicalCard card) {

    }

    @Override
    public boolean canHavePlayedOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target) {
        return true;
    }

    @Override
    public boolean canHaveTransferredOn(LotroGame game, PhysicalCard playedCard, PhysicalCard target) {
        return true;
    }

    @Override
    public boolean canBeTransferred(LotroGame game, PhysicalCard attachment) {
        return true;
    }

    @Override
    public boolean shouldSkipPhase(LotroGame game, Phase phase, String playerId) {
        return false;
    }

    @Override
    public boolean isValidAssignments(LotroGame game, Side side, PhysicalCard companion, Set<PhysicalCard> minions) {
        return true;
    }

    @Override
    public boolean isValidAssignments(LotroGame game, Side side, Map<PhysicalCard, Set<PhysicalCard>> assignments) {
        return true;
    }

    @Override
    public boolean isPreventedFromBeingAssignedToSkirmish(LotroGame game, Side sidePlayer, PhysicalCard card) {
        return false;
    }

    @Override
    public boolean canBeDiscardedFromPlay(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canBeLiberated(LotroGame game, String performingPlayer, PhysicalCard card, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canBeReturnedToHand(LotroGame game, PhysicalCard card, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canBeHealed(LotroGame game, PhysicalCard card) {
        return true;
    }

    @Override
    public boolean canAddBurden(LotroGame game, String performingPlayer, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canRemoveBurden(LotroGame game, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canRemoveThreat(LotroGame game, PhysicalCard source) {
        return true;
    }

    @Override
    public int getRoamingPenaltyModifier(LotroGame game, PhysicalCard physicalCard) {
        return 0;
    }

    @Override
    public boolean canLookOrRevealCardsInHand(LotroGame game, String revealingPlayerId, String actingPlayerId) {
        return true;
    }

    @Override
    public boolean canDiscardCardsFromHand(LotroGame game, String playerId, PhysicalCard source) {
        return true;
    }

    @Override
    public boolean canDiscardCardsFromTopOfDeck(LotroGame game, String playerId, PhysicalCard source) {
        return true;
    }

    @Override
    public int getSpotCountModifier(LotroGame game, Filterable filter) {
        return 0;
    }

    @Override
    public boolean hasFlagActive(LotroGame game, ModifierFlag modifierFlag) {
        return false;
    }

    @Override
    public boolean isSiteReplaceable(LotroGame game, String playerId) {
        return true;
    }

    @Override
    public boolean canPlaySite(LotroGame game, String playerId) {
        return true;
    }

    @Override
    public boolean shadowCanHaveInitiative(LotroGame game) {
        return true;
    }

    @Override
    public Side hasInitiative(LotroGame game) {
        return null;
    }

    @Override
    public int getInitiativeHandSizeModifier(LotroGame game) {
        return 0;
    }

    @Override
    public boolean lostAllKeywords(LotroGame game, PhysicalCard card) {
        return false;
    }

    @Override
    public Evaluator getFpSkirmishStrengthOverrideEvaluator(LotroGame game, PhysicalCard fpCharacter) {
        return null;
    }

    @Override
    public Evaluator getShadowSkirmishStrengthOverrideEvaluator(LotroGame game, PhysicalCard shadowCharacter) { return null; }

    @Override
    public boolean canSpotCulture(LotroGame game, Culture culture, String playerId) {
        return true;
    }

    @Override
    public int getFPCulturesSpotCountModifier(LotroGame game, String playerId) {
        return 0;
    }

    @Override
    public int getSiteControlledSpotCountModifier(LotroGame game, String playerId) { return 0; }

    @Override
    public int getSanctuaryHealModifier(LotroGame game) {
        return 0;
    }

    @Override
    public int getPotentialDiscount(LotroGame game, PhysicalCard discountCard) {
        return 0;
    }

    @Override
    public void appendPotentialDiscounts(LotroGame game, CostToEffectAction action, PhysicalCard card) {

    }
}
