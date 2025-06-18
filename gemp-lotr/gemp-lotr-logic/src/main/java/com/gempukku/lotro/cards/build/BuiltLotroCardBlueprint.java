package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.ExtraPlayCost;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.*;
import com.gempukku.lotro.logic.effects.DiscountEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.google.common.collect.Sets;

import java.util.*;

public class BuiltLotroCardBlueprint implements LotroCardBlueprint {
    private String id;

    private CardInfo info;

    //Sanitized versions of text are used for searching and comparisons, as opposed
    // to the regular display versions which can have capitalization, spaces, accents, etc.
    private String title;
    private String sanitizedTitle;
    private String subtitle;
    private String sanitizedSubtitle;
    private String fullName;
    private String sanitizedFullName;
    private boolean canStartWithRing;
    private boolean unique;
    private Side side;
    private CardType cardType;
    private Culture culture;
    private Race race;
    private Signet signet;
    private Map<Keyword, Integer> keywords;
    private Set<Timeword> timewords;
    private int cost = -1;
    private int strength;
    private int vitality;
    private int resistance;
    private SitesBlock siteBlock;
    private int siteNumber;
    private Set<PossessionClass> possessionClasses;
    private Direction direction;
    private final Set<AllyHome> allyHomeSites = new HashSet<>();

    private String gameText;
    private String formattedGameText;
    private String loreText;
    private String promoText;
    private String displayableInformation;

    private List<Requirement> requirements;
    private List<FilterableSource> targetFilters;

    private List<ActionSource> requiredBeforeTriggers;
    private List<ActionSource> requiredAfterTriggers;
    private List<ActionSource> optionalBeforeTriggers;
    private List<ActionSource> optionalAfterTriggers;

    private List<ActionSource> beforeActivatedTriggers;
    private List<ActionSource> afterActivatedTriggers;

    private List<ActionSource> optionalInHandBeforeActions;
    private List<ActionSource> optionalInHandAfterActions;

    private List<ActionSource> optionalInHandAfterTriggers;

    private List<ActionSource> inPlayPhaseActions;
    private List<ActionSource> inDiscardPhaseActions;
    private List<ActionSource> inDrawDeckPhaseActions;
    private List<ActionSource> fromStackedPhaseActions;

    private List<ModifierSource> inPlayModifiers;
    private List<ModifierSource> stackedOnModifiers;
    private List<ModifierSource> inDiscardModifiers;
    private List<ModifierSource> controlledSiteModifiers;
    private List<ModifierSource> permanentSiteModifiers;

    private List<TwilightCostModifierSource> twilightCostModifiers;

    private List<ExtraPlayCostSource> extraPlayCosts;
    private List<DiscountSource> discountSources;

    private Map<Requirement, EffectAppender[]> playInOtherPhaseConditions;
    private List<FilterableSource> copiedFilters;

    private ActionSource playEventAction;
    private ActionSource killedRequiredTriggerAction;
    private ActionSource killedOptionalTriggerAction;
    private ActionSource discardedFromPlayRequiredTriggerAction;
    private ActionSource discardedFromPlayOptionalTriggerAction;

    private AidCostSource aidCostSource;

    private ExtraPossessionClassTest extraPossessionClassTest;

    private PreGameDeckValidation deckValidation;

//    //Constructor used when creating a raw blueprint from scratch
//    public BuiltLotroCardBlueprint() { }
//
//    //Used when creating an errata copy that is mostly the same and only
//    // changes a few pieces.
//    public BuiltLotroCardBlueprint(BuiltLotroCardBlueprint other) {
//
//    }

    // Building methods

    public void setBlueprintId(String id) {
        this.id = id;
    }

    public void setCanStartWithRing(boolean canStartWithRing) {
        this.canStartWithRing = canStartWithRing;
    }

    public void setDeckValidation(PreGameDeckValidation validation) {
        this.deckValidation = validation;
    }

    public void setAllyHomeSites(AllyHome home) {
        this.allyHomeSites.add(home);
    }

    public void setExtraPossessionClassTest(ExtraPossessionClassTest extraPossessionClassTest) {
        this.extraPossessionClassTest = extraPossessionClassTest;
    }

    public void appendCopiedFilter(FilterableSource filterableSource) {
        if (copiedFilters == null)
            copiedFilters = new LinkedList<>();
        copiedFilters.add(filterableSource);
    }

    public void appendPlayInOtherPhaseCondition(Requirement requirement, EffectAppender[] costAppenders) {
        if (playInOtherPhaseConditions == null)
            playInOtherPhaseConditions = new LinkedHashMap<>();
        playInOtherPhaseConditions.put(requirement, costAppenders);
    }

    public void appendDiscountSource(DiscountSource discountSource) {
        if (discountSources == null)
            discountSources = new LinkedList<>();
        discountSources.add(discountSource);
    }

    public void appendOptionalInHandAfterTrigger(ActionSource actionSource) {
        if (optionalInHandAfterTriggers == null)
            optionalInHandAfterTriggers = new LinkedList<>();
        optionalInHandAfterTriggers.add(actionSource);
    }

    public void appendOptionalInHandBeforeAction(ActionSource actionSource) {
        if (optionalInHandBeforeActions == null)
            optionalInHandBeforeActions = new LinkedList<>();
        optionalInHandBeforeActions.add(actionSource);
    }

    public void appendOptionalInHandAfterAction(ActionSource actionSource) {
        if (optionalInHandAfterActions == null)
            optionalInHandAfterActions = new LinkedList<>();
        optionalInHandAfterActions.add(actionSource);
    }

    public void appendExtraPlayCost(ExtraPlayCostSource extraPlayCostSource) {
        if (extraPlayCosts == null)
            extraPlayCosts = new LinkedList<>();
        extraPlayCosts.add(extraPlayCostSource);
    }

    public void appendBeforeActivatedTrigger(ActionSource actionSource) {
        if (beforeActivatedTriggers == null)
            beforeActivatedTriggers = new LinkedList<>();
        beforeActivatedTriggers.add(actionSource);
    }

    public void appendAfterActivatedTrigger(ActionSource actionSource) {
        if (afterActivatedTriggers == null)
            afterActivatedTriggers = new LinkedList<>();
        afterActivatedTriggers.add(actionSource);
    }

    public void appendRequiredBeforeTrigger(ActionSource actionSource) {
        if (requiredBeforeTriggers == null)
            requiredBeforeTriggers = new LinkedList<>();
        requiredBeforeTriggers.add(actionSource);
    }

    public void appendRequiredAfterTrigger(ActionSource actionSource) {
        if (requiredAfterTriggers == null)
            requiredAfterTriggers = new LinkedList<>();
        requiredAfterTriggers.add(actionSource);
    }

    public void appendOptionalBeforeTrigger(ActionSource actionSource) {
        if (optionalBeforeTriggers == null)
            optionalBeforeTriggers = new LinkedList<>();
        optionalBeforeTriggers.add(actionSource);
    }

    public void appendOptionalAfterTrigger(ActionSource actionSource) {
        if (optionalAfterTriggers == null)
            optionalAfterTriggers = new LinkedList<>();
        optionalAfterTriggers.add(actionSource);
    }

    public void appendPlayRequirement(Requirement requirement) {
        if (requirements == null)
            requirements = new LinkedList<>();
        requirements.add(requirement);
    }

    public void appendInPlayModifier(ModifierSource modifierSource) {
        if (inPlayModifiers == null)
            inPlayModifiers = new LinkedList<>();
        inPlayModifiers.add(modifierSource);
    }

    public void appendStackedOnModifier(ModifierSource modifierSource) {
        if (stackedOnModifiers == null)
            stackedOnModifiers = new LinkedList<>();
        stackedOnModifiers.add(modifierSource);
    }

    public void appendInDiscardModifier(ModifierSource modifierSource) {
        if (inDiscardModifiers == null)
            inDiscardModifiers = new LinkedList<>();
        inDiscardModifiers.add(modifierSource);
    }

    public void appendControlledSiteModifier(ModifierSource modifierSource) {
        if (controlledSiteModifiers == null)
            controlledSiteModifiers = new LinkedList<>();
        controlledSiteModifiers.add(modifierSource);
    }

    public void appendPermanentSiteModifier(ModifierSource modifierSource) {
        if (permanentSiteModifiers == null)
            permanentSiteModifiers = new LinkedList<>();
        permanentSiteModifiers.add(modifierSource);
    }

    public void setTargetFilter(FilterableSource targetFilter) {
        targetFilters = new LinkedList<>();
        targetFilters.add(targetFilter);
    }

    public void appendInPlayPhaseAction(ActionSource actionSource) {
        if (inPlayPhaseActions == null)
            inPlayPhaseActions = new LinkedList<>();
        inPlayPhaseActions.add(actionSource);
    }

    public void appendInDiscardPhaseAction(ActionSource actionSource) {
        if (inDiscardPhaseActions == null)
            inDiscardPhaseActions = new LinkedList<>();
        inDiscardPhaseActions.add(actionSource);
    }

    public void appendInDrawDeckPhaseAction(ActionSource actionSource) {
        if (inDrawDeckPhaseActions == null)
            inDrawDeckPhaseActions = new LinkedList<>();
        inDrawDeckPhaseActions.add(actionSource);
    }

    public void appendFromStackedPhaseAction(ActionSource actionSource) {
        if (fromStackedPhaseActions == null)
            fromStackedPhaseActions = new LinkedList<>();
        fromStackedPhaseActions.add(actionSource);
    }

    public void appendTwilightCostModifier(TwilightCostModifierSource twilightCostModifierSource) {
        if (twilightCostModifiers == null)
            twilightCostModifiers = new LinkedList<>();
        twilightCostModifiers.add(twilightCostModifierSource);
    }

    public void setPlayEventAction(ActionSource playEventAction) {
        if (this.playEventAction != null)
            throw new RuntimeException("Cant set play event action more than once");
        this.playEventAction = playEventAction;
    }

    public void setAidCostSource(AidCostSource aidCostSource) {
        this.aidCostSource = aidCostSource;
    }

    public void setKilledRequiredTriggerAction(ActionSource killedRequiredTriggerAction) {
        this.killedRequiredTriggerAction = killedRequiredTriggerAction;
    }

    public void setKilledOptionalTriggerAction(ActionSource killedOptionalTriggerAction) {
        this.killedOptionalTriggerAction = killedOptionalTriggerAction;
    }

    public void setDiscardedFromPlayRequiredTriggerAction(ActionSource discardedFromPlayRequiredTriggerAction) {
        this.discardedFromPlayRequiredTriggerAction = discardedFromPlayRequiredTriggerAction;
    }

    public void setDiscardedFromPlayOptionalTriggerAction(ActionSource discardedFromPlayOptionalTriggerAction) {
        this.discardedFromPlayOptionalTriggerAction = discardedFromPlayOptionalTriggerAction;
    }

    public void setTitle(String title) {
        this.title = title;
        this.sanitizedTitle = Names.SanitizeName(title);
        setFullName();
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        this.sanitizedSubtitle = Names.SanitizeName(subtitle);
        setFullName();
    }

    private void setFullName() {
        if(subtitle != null) {
            this.fullName = title + ", " + subtitle;
            this.sanitizedFullName = sanitizedTitle + ", " + sanitizedSubtitle;
        }
        else {
            this.fullName = title;
            this.sanitizedFullName = sanitizedTitle;
        }
    }

    public void setGameText(String text) {
        this.formattedGameText = GameText.ConvertTextToHTML(text.trim());
        this.gameText = GameText.SanitizeHTMLToSearchText(formattedGameText);
    }

    public void setLore(String text) {
        this.loreText = GameText.ConvertTextToHTML(text.trim());
    }

    public void setPromoText(String text) {
        this.promoText = GameText.ConvertTextToHTML(text.trim());
    }

    public void setInfo(CardInfo info) {
        this.info = info;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public void setSignet(Signet signet) {
        this.signet = signet;
    }

    public void setKeywords(Map<Keyword, Integer> keywords) {
        this.keywords = keywords;
    }

    public void setTimewords(Set<Timeword> timewords) {
        this.timewords = timewords;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }

    public void setResistance(int resistance) {
        this.resistance = resistance;
    }

    public void setSiteBlock(SitesBlock siteBlock) {
        this.siteBlock = siteBlock;
    }

    public void setSiteNumber(int siteNumber) {
        this.siteNumber = siteNumber;
    }

    public void setPossessionClasses(Set<PossessionClass> possessionClasses) {
        this.possessionClasses = possessionClasses;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setDisplayableInformation(String value) {
        this.displayableInformation = value;
    }

    // Implemented methods

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        if (this.id != null)
            throw new UnsupportedOperationException("Id for this blueprint has already been set");

        this.id = id;
        info = new CardInfo(id);
    }

    @Override
    public Result validatePreGameDeckCheck(List<PhysicalCardImpl> freeps, List<PhysicalCardImpl> shadow,
            List<PhysicalCardImpl> sites, PhysicalCardImpl rb, PhysicalCardImpl ring, PhysicalCardImpl map) {
        if(deckValidation != null)
            return deckValidation.validatePreGameDeckCheck(freeps, shadow, sites, rb, ring, map);

        return new Result(true, null);
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public CardType getCardType() {
        return cardType;
    }

    @Override
    public Culture getCulture() {
        return culture;
    }

    @Override
    public Race getRace() {
        return race;
    }

    @Override
    public boolean isUnique() {
        return unique;
    }

    @Override
    public String getTitle() { return title; }

    @Override
    public String getSanitizedTitle() { return sanitizedTitle; }

    @Override
    public String getSubtitle() {
        return subtitle;
    }

    @Override
    public String getFullName() { return fullName; }

    @Override
    public String getSanitizedFullName() { return sanitizedFullName; }

    @Override
    public String getGameText() { return gameText; }

    @Override
    public String getFormattedGameText() { return formattedGameText; }
    @Override
    public String getLore()  { return loreText; }
    @Override
    public String getPromoText() { return promoText; }


    @Override
    public CardInfo getCardInfo() {
        return info;
    }

    @Override
    public boolean canStartWithRing() {
        return canStartWithRing;
    }

    @Override
    public Signet getSignet() {
        return signet;
    }

    @Override
    public int getTwilightCost() {
        return cost;
    }

    @Override
    public int getStrength() {
        return strength;
    }

    @Override
    public int getVitality() {
        return vitality;
    }

    @Override
    public int getResistance() {
        return resistance;
    }

    @Override
    public SitesBlock getSiteBlock() {
        return siteBlock;
    }

    @Override
    public int getSiteNumber() {
        return siteNumber;
    }

    @Override
    public Set<AllyHome> getAllyHomes() {
        return allyHomeSites;
    }

    @Override
    public boolean hasAllyHome(AllyHome home) {
        return allyHomeSites.stream().anyMatch(x -> x.equals(home));
    }

    @Override
    public Set<PossessionClass> getPossessionClasses() {
        return possessionClasses;
    }

    @Override
    public Direction getSiteDirection() {
        return direction;
    }

    @Override
    public boolean hasKeyword(Keyword keyword) {
        return keywords != null && keywords.containsKey(keyword);
    }

    @Override
    public boolean hasTimeword(Timeword timeword) {
        return timewords != null && timewords.contains(timeword);
    }

    @Override
    public int getKeywordCount(Keyword keyword) {
        if (keywords == null)
            return 0;
        Integer count = keywords.get(keyword);
        if (count == null)
            return 0;
        else
            return count;
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        if (targetFilters == null)
            return null;

        Filterable[] result = new Filterable[targetFilters.size()];
        var actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
        for (int i = 0; i < result.length; i++) {
            final FilterableSource filterableSource = targetFilters.get(i);
            result[i] = filterableSource.getFilterable(actionContext);
        }

        return Filters.and(result);
    }

    @Override
    public List<? extends Action> getPhaseActionsFromDiscard(String playerId, LotroGame game, PhysicalCard self) {
        return getActivatedActions(playerId, game, self, inDiscardPhaseActions);
    }

    @Override
    public List<? extends Action> getPhaseActionsFromDrawDeck(String playerId, LotroGame game, PhysicalCard self) {
        return getActivatedActions(playerId, game, self, inDrawDeckPhaseActions);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        List<ActivateCardAction> activatedActions = getActivatedActions(playerId, game, self, inPlayPhaseActions);
        if (copiedFilters != null) {
            if (activatedActions == null)
                activatedActions = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(activatedActions, firstActive.getBlueprint().getPhaseActionsInPlay(playerId, game, self));
            }
        }
        return activatedActions;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsFromStacked(String playerId, LotroGame game, PhysicalCard self) {
        return getActivatedActions(playerId, game, self, fromStackedPhaseActions);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = getModifiers(game, self, inPlayModifiers);
        if (copiedFilters != null) {
            if (modifiers == null)
                modifiers = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null) {
                    addAllNotNull(modifiers, firstActive.getBlueprint().getInPlayModifiers(game, self));
                }
            }
        }
        return modifiers;
    }

    private <T> void addAllNotNull(List<T> list, List<? extends T> possiblyNullList) {
        if (possiblyNullList != null)
            list.addAll(possiblyNullList);
    }

    @Override
    public List<? extends Modifier> getStackedOnModifiers(LotroGame game, PhysicalCard self) {
        return getModifiers(game, self, stackedOnModifiers);
    }

    @Override
    public List<? extends Modifier> getInDiscardModifiers(LotroGame game, PhysicalCard self) {
        return getModifiers(game, self, inDiscardModifiers);
    }

    @Override
    public List<? extends Modifier> getControlledSiteModifiers(LotroGame game, PhysicalCard self) {
        return getModifiers(game, self, controlledSiteModifiers);
    }

    @Override
    public List<? extends Modifier> getPermanentSiteModifiers(LotroGame game, PhysicalCard self) {
        return getModifiers(game, self, permanentSiteModifiers);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        DefaultActionContext dummy = new DefaultActionContext(self.getOwner(), game, self, null, null);

        if (requirements != null)
            for (Requirement requirement : requirements) {
                if (!requirement.accepts(dummy))
                    return false;
            }

        if (playEventAction != null && !playEventAction.isValid(dummy))
            return false;

        return true;
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target) {
        if (twilightCostModifiers == null)
            return 0;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);

        int result = 0;
        for (TwilightCostModifierSource twilightCostModifier : twilightCostModifiers)
            result += twilightCostModifier.getTwilightCostModifier(actionContext, target);

        return result;
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
        PlayEventAction action = new PlayEventAction(self, playEventAction.requiresRanger());
        playEventAction.createAction(action, actionContext);
        return action;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect, PhysicalCard self) {
        List<RequiredTriggerAction> result = null;

        if (requiredBeforeTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource requiredBeforeTrigger : requiredBeforeTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, effect);

                if(requiredBeforeTrigger.getPlayer() != null) {
                    actionContext = new DefaultActionContext(requiredBeforeTrigger.getPlayer().getPlayer(actionContext), game, self, null, effect);
                }

                if (requiredBeforeTrigger.isValid(actionContext)) {
                    RequiredTriggerAction action = new RequiredTriggerAction(self);
                    requiredBeforeTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, effect);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getRequiredBeforeTriggers(game, effect, self));
            }
        }

        return result;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        List<RequiredTriggerAction> result = null;

        if (requiredAfterTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource requiredAfterTrigger : requiredAfterTriggers) {
                var player = self.getOwner();
                var actionContext = new DefaultActionContext(player, game, self, effectResult, null);

                if(requiredAfterTrigger.getPlayer() != null) {
                    actionContext = new DefaultActionContext(requiredAfterTrigger.getPlayer().getPlayer(actionContext), game, self, effectResult, null);
                }

                if (requiredAfterTrigger.isValid(actionContext)) {
                    RequiredTriggerAction action = new RequiredTriggerAction(self);
                    requiredAfterTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, effectResult, null);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getRequiredAfterTriggers(game, effectResult, self));
            }
        }

        return result;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        List<OptionalTriggerAction> result = null;

        if (optionalBeforeTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource optionalBeforeTrigger : optionalBeforeTriggers) {
                var actionContext = new DefaultActionContext(playerId, game, self, null, effect);

                if (optionalBeforeTrigger.isValid(actionContext)) {
                    OptionalTriggerAction action = new OptionalTriggerAction(self);
                    optionalBeforeTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getOptionalBeforeTriggers(playerId, game, effect, self));
            }
        }

        return result;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        List<OptionalTriggerAction> result = null;

        if (optionalAfterTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource optionalAfterTrigger : optionalAfterTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);

                if (optionalAfterTrigger.isValid(actionContext)) {
                    OptionalTriggerAction action = new OptionalTriggerAction(self);
                    optionalAfterTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getOptionalAfterTriggers(playerId, game, effectResult, self));
            }
        }

        return result;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        List<ActivateCardAction> result = null;

        if (beforeActivatedTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource beforeActivatedTrigger : beforeActivatedTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);

                if(beforeActivatedTrigger.getPlayer() != null) {
                    actionContext = new DefaultActionContext(beforeActivatedTrigger.getPlayer().getPlayer(actionContext), game, self, null, effect);
                }

                if (beforeActivatedTrigger.isValid(actionContext)) {
                    ActivateCardAction action = new ActivateCardAction(self, playerId);
                    action.setActionTimeword(Timeword.RESPONSE);
                    beforeActivatedTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getOptionalInPlayBeforeActions(playerId, game, effect, self));
            }
        }

        return result;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        List<ActivateCardAction> result = null;

        if (afterActivatedTriggers != null) {
            result = new LinkedList<>();
            for (ActionSource afterActivatedTrigger : afterActivatedTriggers) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);

                if(afterActivatedTrigger.getPlayer() != null) {
                    actionContext = new DefaultActionContext(afterActivatedTrigger.getPlayer().getPlayer(actionContext), game, self, effectResult, null);
                }

                if (afterActivatedTrigger.isValid(actionContext)) {
                    ActivateCardAction action = new ActivateCardAction(self, playerId);
                    action.setActionTimeword(Timeword.RESPONSE);
                    afterActivatedTrigger.createAction(action, actionContext);
                    result.add(action);
                }
            }
        }

        if (copiedFilters != null) {
            if (result == null)
                result = new LinkedList<>();
            for (FilterableSource copiedFilter : copiedFilters) {
                DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);
                final PhysicalCard firstActive = Filters.findFirstActive(game, copiedFilter.getFilterable(actionContext));
                if (firstActive != null)
                    addAllNotNull(result, firstActive.getBlueprint().getOptionalInPlayAfterActions(playerId, game, effectResult, self));
            }
        }

        return result;
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (optionalInHandBeforeActions == null)
            return null;

        List<PlayEventAction> result = new LinkedList<>();
        for (ActionSource optionalInHandBeforeAction : optionalInHandBeforeActions) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, effect);

            if(optionalInHandBeforeAction.getPlayer() != null) {
                actionContext = new DefaultActionContext(optionalInHandBeforeAction.getPlayer().getPlayer(actionContext), game, self, null, effect);
            }

            if (optionalInHandBeforeAction.isValid(actionContext)) {
                PlayEventAction action = new PlayEventAction(self);
                optionalInHandBeforeAction.createAction(action, actionContext);
                result.add(action);
            }
        }

        return result;
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (optionalInHandAfterActions == null)
            return null;

        List<PlayEventAction> result = new LinkedList<>();
        for (ActionSource optionalInHandAfterAction : optionalInHandAfterActions) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);

            if(optionalInHandAfterAction.getPlayer() != null) {
                actionContext = new DefaultActionContext(optionalInHandAfterAction.getPlayer().getPlayer(actionContext), game, self, effectResult, null);
            }

            if (optionalInHandAfterAction.isValid(actionContext)) {
                PlayEventAction action = new PlayEventAction(self);
                optionalInHandAfterAction.createAction(action, actionContext);
                result.add(action);
            }
        }

        return result;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalInHandAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (optionalInHandAfterTriggers == null)
            return null;

        List<OptionalTriggerAction> result = new LinkedList<>();
        for (ActionSource optionalInHandAfterTrigger : optionalInHandAfterTriggers) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, effectResult, null);

            if(optionalInHandAfterTrigger.getPlayer() != null) {
                actionContext = new DefaultActionContext(optionalInHandAfterTrigger.getPlayer().getPlayer(actionContext), game, self, effectResult, null);
            }

            if (optionalInHandAfterTrigger.isValid(actionContext)) {
                OptionalTriggerAction action = new OptionalTriggerAction(self);
                optionalInHandAfterTrigger.createAction(action, actionContext);
                result.add(action);
            }
        }

        return result;
    }

    @Override
    public List<? extends ExtraPlayCost> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        if (extraPlayCosts == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);

        List<ExtraPlayCost> result = new LinkedList<>();
        for (ExtraPlayCostSource extraPlayCost : extraPlayCosts) {
            result.add(extraPlayCost.getExtraPlayCost(actionContext));
        }

        return result;
    }

    @Override
    public List<? extends Action> getPhaseActionsInHand(String playerId, LotroGame game, PhysicalCard self) {
        if (playInOtherPhaseConditions == null)
            return null;

        List<Action> playCardActions = new LinkedList<>();
        for (Map.Entry<Requirement, EffectAppender[]> playInOtherPhaseCondition : playInOtherPhaseConditions.entrySet()) {
            Requirement condition = playInOtherPhaseCondition.getKey();
            EffectAppender[] additionalCosts = playInOtherPhaseCondition.getValue();

            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
            if (condition.accepts(actionContext)
                    && PlayUtils.checkPlayRequirements(game, self, Filters.any, 0, 0, false, false, false)) {
                boolean canPayExtraCosts = true;
                for (EffectAppender additionalCost : additionalCosts) {
                    canPayExtraCosts &= additionalCost.isPlayableInFull(actionContext);
                }

                if (canPayExtraCosts) {
                    CostToEffectAction playCardAction = PlayUtils.getPlayCardAction(game, self, 0, Filters.any, false);
                    for (EffectAppender additionalCost : additionalCosts) {
                        additionalCost.appendEffect(true, playCardAction, actionContext);
                    }
                    playCardActions.add(playCardAction);
                }
            }
        }

        return playCardActions;
    }

    // Default implementations - not needed (for now)

    @Override
    public boolean isExtraPossessionClass(LotroGame game, PhysicalCard self, PhysicalCard attachedTo) {
        if (extraPossessionClassTest != null)
            return extraPossessionClassTest.isExtraPossessionClass(game, self, attachedTo);
        return false;
    }

    @Override
    public int getPotentialDiscount(LotroGame game, String playerId, PhysicalCard self) {
        if (discountSources == null)
            return 0;

        int result = 0;
        DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
        for (DiscountSource discountSource : discountSources)
            result += discountSource.getPotentialDiscount(actionContext);

        return result;
    }

    @Override
    public void appendPotentialDiscountEffects(LotroGame game, CostToEffectAction action, String playerId, PhysicalCard self) {
        if (discountSources != null) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
            for (DiscountSource discountSource : discountSources) {
                final DiscountEffect discountEffect = discountSource.getDiscountEffect(action, actionContext);
                action.appendPotentialDiscount(discountEffect);
            }
        }
    }

    @Override
    public RequiredTriggerAction getDiscardedFromPlayRequiredTrigger(LotroGame game, PhysicalCard self) {
        if (discardedFromPlayRequiredTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
        if (discardedFromPlayRequiredTriggerAction.isValid(actionContext)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            discardedFromPlayRequiredTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public OptionalTriggerAction getDiscardedFromPlayOptionalTrigger(String playerId, LotroGame game, PhysicalCard self) {
        if (discardedFromPlayOptionalTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
        if (discardedFromPlayOptionalTriggerAction.isValid(actionContext)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            discardedFromPlayOptionalTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public RequiredTriggerAction getKilledRequiredTrigger(LotroGame game, PhysicalCard self) {
        if (killedRequiredTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
        if (killedRequiredTriggerAction.isValid(actionContext)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            killedRequiredTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public OptionalTriggerAction getKilledOptionalTrigger(String playerId, LotroGame game, PhysicalCard self) {
        if (killedOptionalTriggerAction == null)
            return null;

        DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
        if (killedOptionalTriggerAction.isValid(actionContext)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            killedOptionalTriggerAction.createAction(action, actionContext);
            return action;
        }
        return null;
    }

    @Override
    public String getDisplayableInformation(PhysicalCard self) {
        if (displayableInformation != null) {
            PhysicalCard.WhileInZoneData whileInZoneData = self.getWhileInZoneData();
            if (whileInZoneData != null) {
                return displayableInformation.replace("{stored}", whileInZoneData.getHumanReadable());
            } else {
                return displayableInformation;
            }
        }
        return null;
    }

    @Override
    public boolean canPayAidCost(LotroGame game, PhysicalCard self) {
        if (aidCostSource == null)
            return false;

        DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
        return aidCostSource.canPayAidCost(actionContext);
    }

    @Override
    public void appendAidCosts(LotroGame game, CostToEffectAction action, PhysicalCard self) {
        if (aidCostSource != null) {
            DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
            aidCostSource.appendAidCost(action, actionContext);
        }
    }

    @Override
    public boolean skipUniquenessCheck() {
        return false;
    }

    // Helper methods

    private List<Modifier> getModifiers(LotroGame game, PhysicalCard self, List<ModifierSource> sources) {
        if (sources == null)
            return null;

        List<Modifier> result = new LinkedList<>();
        for (ModifierSource source : sources) {
            ActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
            result.add(source.getModifier(actionContext));
        }
        return result;
    }

    private List<ActivateCardAction> getActivatedActions(String playerId, LotroGame game, PhysicalCard self, List<ActionSource> sources) {
        if (sources == null)
            return null;

        List<ActivateCardAction> result = new LinkedList<>();
        for (ActionSource inPlayPhaseAction : sources) {
            DefaultActionContext actionContext = new DefaultActionContext(playerId, game, self, null, null);
            if (inPlayPhaseAction.isValid(actionContext)) {
                ActivateCardAction action = new ActivateCardAction(self, playerId);
                inPlayPhaseAction.createAction(action, actionContext);
                result.add(action);
            }
        }
        return result;
    }

    private static Set<String> frodosThatCantStartWithRing = Sets.newHashSet("Frenzied Fighter");
    private static Set<String> samsWithNon5Resistance = Sets.newHashSet("Loyal Friend", "Dropper of Eaves", "Humble Halfling", "Steadfast Friend", "Innocent Traveler");

    public void validateConsistency() throws InvalidCardDefinitionException {
        if (title == null)
            throw new InvalidCardDefinitionException("Card has to have a title");
        if (cardType == null)
            throw new InvalidCardDefinitionException("Card has to have a type");
        if (cardType != CardType.THE_ONE_RING && cardType != CardType.SITE && cardType != CardType.MAP && side == null)
            throw new InvalidCardDefinitionException("All cards except The One Ring, Sites, and Maps must have a side defined");
        if (cardType != CardType.THE_ONE_RING && cardType != CardType.SITE && cardType != CardType.MAP && culture == null)
            throw new InvalidCardDefinitionException("All cards except The One Ring, Sites, and Maps have a culture defined");
        if (siteNumber != 0
                && cardType != CardType.SITE
                && cardType != CardType.MINION
                && cardType != CardType.CONDITION)
            throw new InvalidCardDefinitionException("Only minions, sites, and conditions have a site number, use siteHome for allies");
        if (cardType == CardType.EVENT) {
            List<Timeword> requiredTimewords = Arrays.asList(
                    Timeword.RESPONSE, Timeword.FELLOWSHIP, Timeword.SHADOW, Timeword.MANEUVER, Timeword.ARCHERY, Timeword.ASSIGNMENT,
                    Timeword.SKIRMISH, Timeword.REGROUP);
            if (timewords == null || Collections.disjoint(timewords, requiredTimewords))
                throw new InvalidCardDefinitionException("Events have to have a timeword(s)");

            if (timewords.contains(Timeword.RESPONSE)) {
                if (optionalInHandBeforeActions == null && optionalInHandAfterActions == null)
                    throw new InvalidCardDefinitionException("Response events have to have responseEvent type effect");
            } else {
                if (playEventAction == null)
                    throw new InvalidCardDefinitionException("Events have to have an event type effect");
            }
        }
        if (timewords != null && cardType != CardType.EVENT)
            throw new InvalidCardDefinitionException("Only events should have timewords");
        if (cardType != CardType.EVENT && playEventAction != null)
            throw new InvalidCardDefinitionException("Only events should have an event type effect");
        if (cost == -1)
            throw new InvalidCardDefinitionException("Cost was not assigned to card");
        if (Arrays.asList(CardType.MINION, CardType.COMPANION, CardType.ALLY).contains(cardType)) {
            if (vitality == 0)
                throw new InvalidCardDefinitionException("Character has 0 vitality");
            if (strength == 0 && !id.equals("15_43"))
                throw new InvalidCardDefinitionException("Character has 0 strength");
        }
        if (cardType == CardType.SITE && siteBlock == null)
            throw new InvalidCardDefinitionException("Site has to have a block defined");
        if (siteBlock != null && cardType != CardType.SITE)
            throw new InvalidCardDefinitionException("Block defined for card, that is not site");
        if (targetFilters != null && keywords != null) {
            if (keywords.size() > 1 && keywords.containsKey(Keyword.TALE))
                throw new InvalidCardDefinitionException("Attachment should not have keywords");
        }
        if (Arrays.asList(CardType.POSSESSION, CardType.CONDITION, CardType.ARTIFACT).contains(cardType)
                && targetFilters == null && (keywords == null || !keywords.containsKey(Keyword.SUPPORT_AREA)))
            throw new InvalidCardDefinitionException("Possession, condition or artifact without a filter needs a SUPPORT_AREA keyword");
        if (cardType == CardType.FOLLOWER && aidCostSource == null)
            throw new InvalidCardDefinitionException("Follower requires an aid cost");
        if (title.equals("Frodo") && !canStartWithRing && !frodosThatCantStartWithRing.contains(subtitle)) {
            throw new InvalidCardDefinitionException("Frodo (except some permitted) must be able to start with ring");
        }
        if (title.equals("Sam") && resistance != 5 && !samsWithNon5Resistance.contains(subtitle)) {
            throw new InvalidCardDefinitionException("Sam (except some permitted) needs to have resistance of 5");
        }
    }
}
