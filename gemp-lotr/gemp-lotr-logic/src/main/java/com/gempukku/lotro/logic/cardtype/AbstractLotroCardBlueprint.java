package com.gempukku.lotro.logic.cardtype;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.ExtraPlayCost;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.*;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractLotroCardBlueprint implements LotroCardBlueprint {

    private String _id;
    private final int _twilightCost;
    private final String _name;
    private final String _sanitizedName;
    private final String _subTitle;
    private final CardType _cardType;
    private final Side _side;
    private final Culture _culture;
    private final boolean _unique;
    private final Map<Keyword, Integer> _keywords = new HashMap<>();

    public AbstractLotroCardBlueprint(int twilightCost, Side side, CardType cardType, Culture culture, String name) {
        this(twilightCost, side, cardType, culture, name, null, false);
    }

    public AbstractLotroCardBlueprint(int twilightCost, Side side, CardType cardType, Culture culture, String name, String subTitle, boolean unique) {
        _twilightCost = twilightCost;
        _side = side;
        _cardType = cardType;
        _culture = culture;
        _name = name;
        _sanitizedName = Names.SanitizeName(name);
        _subTitle = subTitle;
        _unique = unique;
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return true;
    }

    protected void addKeyword(Keyword keyword) {
        addKeyword(keyword, 1);
    }

    protected void addKeyword(Keyword keyword, int number) {
        _keywords.put(keyword, number);
    }

    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public final int getTwilightCost() {
        return _twilightCost;
    }

    @Override
    public final boolean hasKeyword(Keyword keyword) {
        return _keywords.containsKey(keyword);
    }

    @Override
    public final int getKeywordCount(Keyword keyword) {
        Integer count = _keywords.get(keyword);
        if (count == null)
            return 0;
        else
            return count;
    }

    @Override
    public final Culture getCulture() {
        return _culture;
    }

    @Override
    public final CardType getCardType() {
        return _cardType;
    }

    @Override
    public final String getId() {
        return _id;
    }

    @Override
    public void setId(String id) {
        if(_id != null)
            throw new UnsupportedOperationException("Id for this blueprint has already been set");

        _id = id;
    }

    @Override
    public final Side getSide() {
        return _side;
    }

    @Override
    public final String getTitle() {
        return _name;
    }

    @Override
    public final String getSanitizedTitle() {
        return _sanitizedName;
    }

    @Override
    public final String getSubtitle() {
        return _subTitle;
    }

    @Override
    public final boolean isUnique() {
        return _unique;
    }

    @Override
    public boolean skipUniquenessCheck() {
        return false;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public List<? extends Modifier> getStackedOnModifiers(LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public List<? extends Modifier> getInDiscardModifiers(LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public List<? extends Modifier> getControlledSiteModifiers(LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public List<? extends Modifier> getPermanentSiteModifiers(LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public Race getRace() {
        return null;
    }

    @Override
    public int getStrength() {
        return 0;
    }

    @Override
    public int getVitality() {
        return 0;
    }

    @Override
    public int getResistance() {
        return 0;
    }

    @Override
    public int[] getAllyHomeSiteNumbers() {
        throw new UnsupportedOperationException("This method should not be called on this card");
    }

    @Override
    public SitesBlock getAllyHomeSiteBlock() {
        throw new UnsupportedOperationException("This method should not be called on this card");
    }

    @Override
    public SitesBlock getSiteBlock() {
        throw new UnsupportedOperationException("This method should not be called on this card");
    }

    @Override
    public int getSiteNumber() {
        return 0;
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target) {
        return 0;
    }

    @Override
    public Set<PossessionClass> getPossessionClasses() {
        return null;
    }

    @Override
    public boolean isExtraPossessionClass(LotroGame game, PhysicalCard self, PhysicalCard attachedTo) {
        return false;
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public List<? extends Action> getPhaseActionsInHand(String playerId, LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsFromStacked(String playerId, LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public List<? extends Action> getPhaseActionsFromDiscard(String playerId, LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect, PhysicalCard self) {
        return null;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        return null;
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        return null;
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        return null;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        return null;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        return null;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalInHandAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        return null;
    }

    @Override
    public RequiredTriggerAction getDiscardedFromPlayRequiredTrigger(LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public OptionalTriggerAction getDiscardedFromPlayOptionalTrigger(String playerId, LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public RequiredTriggerAction getKilledRequiredTrigger(LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public OptionalTriggerAction getKilledOptionalTrigger(String playerId, LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public Signet getSignet() {
        return null;
    }

    @Override
    public String getDisplayableInformation(PhysicalCard self) {
        return null;
    }

    @Override
    public Direction getSiteDirection() {
        throw new UnsupportedOperationException("This method should not be called on this card");
    }

    @Override
    public List<? extends ExtraPlayCost> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        return null;
    }

    @Override
    public int getPotentialDiscount(LotroGame game, String playerId, PhysicalCard self) {
        return 0;
    }

    @Override
    public void appendPotentialDiscountEffects(LotroGame game, CostToEffectAction action, String playerId, PhysicalCard self) {

    }

    @Override
    public boolean canPayAidCost(LotroGame game, PhysicalCard self) {
        return false;
    }

    @Override
    public void appendAidCosts(LotroGame game, CostToEffectAction action, PhysicalCard self) {

    }
}
