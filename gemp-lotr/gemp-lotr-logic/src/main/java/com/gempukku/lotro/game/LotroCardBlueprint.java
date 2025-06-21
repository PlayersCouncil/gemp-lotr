package com.gempukku.lotro.game;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.*;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.List;
import java.util.Set;

public interface LotroCardBlueprint {
    enum Direction {
        LEFT, RIGHT
    }

    default LotroCardBlueprint getParent() {
        return this;
    }

    String getId();

    boolean canStartWithRing();

    //This isn't ideal, but there's 2,000 java cards that already exist.
    // Adding the argument to their constructors would be a chore.
    void setId(String id);
    Side getSide();

    CardType getCardType();

    Culture getCulture();

    Race getRace();

    boolean isUnique();

    int getUniqueRestriction();

    String getTitle();
    String getSanitizedTitle();
    String getSanitizedSubtitle();
    String getFullName();
    String getSanitizedFullName();

    String getSubtitle();
    String getGameText();
    String getFormattedGameText();
    String getLore();
    String getPromoText();
    CardInfo getCardInfo();

    Signet getSignet();

    boolean hasKeyword(Keyword keyword);

    int getKeywordCount(Keyword keyword);

    boolean hasTimeword(Timeword timeword);

    Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self);

    int getTwilightCost();

    int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target);

    int getStrength();

    int getVitality();

    int getResistance();

    Set<AllyHome> getAllyHomes();
    boolean hasAllyHome(AllyHome home);

    PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self);

    List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self);

    List<? extends Modifier> getStackedOnModifiers(LotroGame game, PhysicalCard self);

    List<? extends Modifier> getInDiscardModifiers(LotroGame game, PhysicalCard self);

    List<? extends Modifier> getControlledSiteModifiers(LotroGame game, PhysicalCard self);
    List<? extends Modifier> getPermanentSiteModifiers(LotroGame game, PhysicalCard self);

    boolean checkPlayRequirements(LotroGame game, PhysicalCard self);

    List<? extends Action> getPhaseActionsInHand(String playerId, LotroGame game, PhysicalCard self);

    List<? extends Action> getPhaseActionsFromDiscard(String playerId, LotroGame game, PhysicalCard self);

    List<? extends Action> getPhaseActionsFromDrawDeck(String playerId, LotroGame game, PhysicalCard self);

    List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self);

    List<? extends ActivateCardAction> getPhaseActionsFromStacked(String playerId, LotroGame game, PhysicalCard self);

    List<RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect, PhysicalCard self);

    List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self);


    List<OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, LotroGame game, Effect effect, PhysicalCard self);

    List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self);


    List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self);

    List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self);


    List<PlayEventAction> getPlayResponseEventAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self);

    List<PlayEventAction> getPlayResponseEventBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self);


    List<OptionalTriggerAction> getOptionalInHandAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self);


    RequiredTriggerAction getDiscardedFromPlayRequiredTrigger(LotroGame game, PhysicalCard self);

    OptionalTriggerAction getDiscardedFromPlayOptionalTrigger(String playerId, LotroGame game, PhysicalCard self);


    RequiredTriggerAction getKilledRequiredTrigger(LotroGame game, PhysicalCard self);

    OptionalTriggerAction getKilledOptionalTrigger(String playerId, LotroGame game, PhysicalCard self);

    SitesBlock getSiteBlock();

    int getSiteNumber();

    Set<PossessionClass> getPossessionClasses();

    boolean isExtraPossessionClass(LotroGame game, PhysicalCard self, PhysicalCard attachedTo);

    Direction getSiteDirection();

    String getDisplayableInformation(PhysicalCard self);

    List<? extends ExtraPlayCost> getExtraCostToPlay(LotroGame game, PhysicalCard self);

    int getPotentialDiscount(LotroGame game, String playerId, PhysicalCard self);

    void appendPotentialDiscountEffects(LotroGame game, CostToEffectAction action, String playerId, PhysicalCard self);

    boolean canPayAidCost(LotroGame game, PhysicalCard self);

    void appendAidCosts(LotroGame game, CostToEffectAction action, PhysicalCard self);

    boolean skipUniquenessCheck();

    default Result validatePreGameDeckCheck(List<PhysicalCardImpl> freeps, List<PhysicalCardImpl> shadow,
            List<PhysicalCardImpl> sites, PhysicalCardImpl rb, PhysicalCardImpl ring, PhysicalCardImpl map) {
        return new Result(true, null);
    }
}
