package com.gempukku.lotro.filters;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CompletePhysicalCardVisitor;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardVisitor;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.RuleUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Filters {
    private static final Map<CardType, Filter> _typeFilterMap = new HashMap<>();
    private static final Map<PossessionClass, Filter> _possessionClassFilterMap = new HashMap<>();
    private static final Map<Signet, Filter> _signetFilterMap = new HashMap<>();
    private static final Map<Race, Filter> _raceFilterMap = new HashMap<>();
    private static final Map<Zone, Filter> _zoneFilterMap = new HashMap<>();
    private static final Map<Side, Filter> _sideFilterMap = new HashMap<>();
    private static final Map<Culture, Filter> _cultureFilterMap = new HashMap<>();
    private static final Map<Keyword, Filter> _keywordFilterMap = new HashMap<>();
    private static final Map<Timeword, Filter> _timewordFilterMap = new HashMap<>();

    static {
        for (Culture culture : Culture.values())
            _cultureFilterMap.put(culture, culture(culture));
        for (Side side : Side.values())
            _sideFilterMap.put(side, side(side));
        for (Zone zone : Zone.values())
            _zoneFilterMap.put(zone, zone(zone));
        for (CardType cardType : CardType.values())
            _typeFilterMap.put(cardType, type(cardType));
        for (Race race : Race.values())
            _raceFilterMap.put(race, race(race));
        for (Signet signet : Signet.values())
            _signetFilterMap.put(signet, signet(signet));
        for (PossessionClass possessionClass : PossessionClass.values())
            _possessionClassFilterMap.put(possessionClass, possessionClass(possessionClass));
        for (Keyword keyword : Keyword.values())
            _keywordFilterMap.put(keyword, keyword(keyword));
        for (Timeword timeword : Timeword.values()) {
            _timewordFilterMap.put(timeword, timeword(timeword));
        }


        // Some simple shortcuts for filters
        // Only companions can be rangers
        _keywordFilterMap.put(Keyword.RANGER, Filters.and(CardType.COMPANION, keyword(Keyword.RANGER)));
        // Only allies can be villagers
        _keywordFilterMap.put(Keyword.VILLAGER, Filters.and(CardType.ALLY, keyword(Keyword.VILLAGER)));

        // Minion groups
        _keywordFilterMap.put(Keyword.SOUTHRON, Filters.and(CardType.MINION, keyword(Keyword.SOUTHRON)));
        _keywordFilterMap.put(Keyword.EASTERLING, Filters.and(CardType.MINION, keyword(Keyword.EASTERLING)));
        _keywordFilterMap.put(Keyword.CORSAIR, Filters.and(CardType.MINION, keyword(Keyword.CORSAIR)));
        _keywordFilterMap.put(Keyword.TRACKER, Filters.and(CardType.MINION, keyword(Keyword.TRACKER)));
        _keywordFilterMap.put(Keyword.WARG_RIDER, Filters.and(CardType.MINION, keyword(Keyword.WARG_RIDER)));
        _keywordFilterMap.put(Keyword.BESIEGER, Filters.and(CardType.MINION, keyword(Keyword.BESIEGER)));
    }

    public static boolean canSpot(LotroGame game, Filterable... filters) {
        return canSpot(game, 1, filters);
    }

    public static boolean canSpot(LotroGame game, int count, Filterable... filters) {
        return countSpottable(game, filters) >= count;
    }

    public static boolean hasActive(LotroGame game, Filterable... filters) {
        return findFirstActive(game, filters) != null;
    }

    public static Collection<PhysicalCard> filterActive(LotroGame game, Filterable... filters) {
        Filter filter = Filters.and(filters);
        GetCardsMatchingFilterVisitor getCardsMatchingFilter = new GetCardsMatchingFilterVisitor(game, filter);
        game.getGameState().iterateActiveCards(getCardsMatchingFilter);
        return getCardsMatchingFilter.getPhysicalCards();
    }

    public static Collection<PhysicalCard> filter(LotroGame game, Iterable<? extends PhysicalCard> cards, Filterable... filters) {
        Filter filter = Filters.and(filters);
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard card : cards) {
            if (filter.accepts(game, card))
                result.add(card);
        }
        return result;
    }

    public static PhysicalCard findFirstActive(LotroGame game, Filterable... filters) {
        FindFirstActiveCardInPlayVisitor visitor = new FindFirstActiveCardInPlayVisitor(game, Filters.and(filters));
        game.getGameState().iterateActiveCards(visitor);
        return visitor.getCard();
    }

    public static int countSpottable(LotroGame game, Filterable... filters) {
        GetCardsMatchingFilterVisitor matchingFilterVisitor = new GetCardsMatchingFilterVisitor(game, Filters.and(filters, Filters.spottable));
        game.getGameState().iterateActiveCards(matchingFilterVisitor);
        int result = matchingFilterVisitor.getCounter();
        //TODO: make this less dependent on how the definition is arranged
        if (filters.length == 1)
            result += game.getModifiersQuerying().getSpotBonus(game, filters[0]);
        return result;
    }

    public static int countActive(LotroGame game, Filterable... filters) {
        GetCardsMatchingFilterVisitor matchingFilterVisitor = new GetCardsMatchingFilterVisitor(game, Filters.and(filters));
        game.getGameState().iterateActiveCards(matchingFilterVisitor);
        return matchingFilterVisitor.getCounter();
    }

    // Filters available

    public static Filter timeword(Timeword timeword) {
        return (game, physicalCard) -> physicalCard.getBlueprint().hasTimeword(timeword);
    }

    public static Filter maxResistance(final int resistance) {
        return (game, physicalCard) -> game.getModifiersQuerying().getResistance(game, physicalCard) <= resistance;
    }

    public static Filter minResistance(final int resistance) {
        return (game, physicalCard) -> game.getModifiersQuerying().getResistance(game, physicalCard) >= resistance;
    }

    public static Filter minVitality(final int vitality) {
        return (game, physicalCard) -> game.getModifiersQuerying().getVitality(game, physicalCard) >= vitality;
    }

    public static Filter maxVitality(final int vitality) {
        return (game, physicalCard) -> game.getModifiersQuerying().getVitality(game, physicalCard) <= vitality;
    }

    public static Filter strengthEqual(final Evaluator evaluator) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) == evaluator.evaluateExpression(game, null);
    }

    public static Filter minStrength(final int strength) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) >= strength;
    }

    public static Filter maxStrength(final int strength) {
        return (game, physicalCard) -> game.getModifiersQuerying().getStrength(game, physicalCard) <= strength;
    }

    private static Filter possessionClass(final PossessionClass possessionClass) {
        return (game, physicalCard) -> {
            final Set<PossessionClass> possessionClasses = physicalCard.getBlueprint().getPossessionClasses();
            return possessionClasses != null && possessionClasses.contains(possessionClass);
        };
    }

    public static Filter hasAnyCultureTokens() {
        return hasAnyCultureTokens(1);
    }

    public static Filter hasAnyCultureTokens(final int count) {
        return (game, physicalCard) -> {
            Map<Token, Integer> tokens = game.getGameState().getTokens(physicalCard);
            for (Map.Entry<Token, Integer> tokenCount : tokens.entrySet()) {
                if (tokenCount.getKey().getCulture() != null)
                    if (tokenCount.getValue() >= count)
                        return true;
            }
            return false;
        };
    }

    public static Filter printedTwilightCost(final int printedTwilightCost) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() == printedTwilightCost;
    }

    public static Filter maxPrintedTwilightCost(final int printedTwilightCost) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() <= printedTwilightCost;
    }

    public static Filter minPrintedTwilightCost(final int printedTwilightCost) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() >= printedTwilightCost;
    }

    public static Filter hasToken(final Token token) {
        return hasToken(token, 1);
    }

    public static Filter hasToken(final Token token, final int count) {
        return (game, physicalCard) -> game.getGameState().getTokenCount(physicalCard, token) >= count;
    }

    public static Filter assignableToSkirmishAgainst(final Side assignedBySide, final Filterable againstFilter) {
        return assignableToSkirmishAgainst(assignedBySide, againstFilter, false, false, false);
    }

    public static Filter assignableToSkirmishAgainst(final Side assignedBySide, final Filterable againstFilter,
            final boolean ignoreExistingAssignments, final boolean ignoreDefender, final boolean allowAllyToSkirmish) {
        return Filters.and(
                assignableToSkirmish(assignedBySide, ignoreExistingAssignments, ignoreDefender, allowAllyToSkirmish),
                (Filter) (game, physicalCard) -> {
                    for (PhysicalCard card : Filters.filterActive(game, againstFilter)) {
                        if (card.getBlueprint().getSide() != physicalCard.getBlueprint().getSide()
                                && Filters.assignableToSkirmish(assignedBySide, ignoreExistingAssignments, ignoreDefender, allowAllyToSkirmish).accepts(game, card)) {
                            Map<PhysicalCard, Set<PhysicalCard>> thisAssignment = new HashMap<>();
                            if (card.getBlueprint().getSide() == Side.FREE_PEOPLE) {
                                if (thisAssignment.containsKey(card))
                                    thisAssignment.get(card).add(physicalCard);
                                else
                                    thisAssignment.put(card, Collections.singleton(physicalCard));
                            } else {
                                if (thisAssignment.containsKey(physicalCard))
                                    thisAssignment.get(physicalCard).add(card);
                                else
                                    thisAssignment.put(physicalCard, Collections.singleton(card));
                            }
                            if (game.getModifiersQuerying().isValidAssignments(game, assignedBySide, thisAssignment))
                                return true;
                        }
                    }

                    return false;
                });
    }

    public static Filter assignableToSkirmish(final Side assignedBySide, final boolean ignoreExistingAssignments,
            final boolean ignoreDefender, final boolean allowAllyToSkirmish) {
        Filter assignableFilter = Filters.or(
                Filters.and(
                        CardType.ALLY,
                        (Filter) (game, physicalCard) -> {
                            if (allowAllyToSkirmish)
                                return true;
                            boolean allowedToSkirmish = game.getModifiersQuerying().isAllyAllowedToParticipateInSkirmishes(game, assignedBySide, physicalCard);
                            if (allowedToSkirmish)
                                return true;
                            boolean preventedByCard = game.getModifiersQuerying().isAllyPreventedFromParticipatingInSkirmishes(game, assignedBySide, physicalCard);
                            if (preventedByCard)
                                return false;
                            return RuleUtils.isAllyAtHome(physicalCard, game.getGameState().getCurrentSiteNumber(), game.getGameState().getCurrentSiteBlock());
                        }),
                Filters.and(
                        CardType.COMPANION,
                        (Filter) (game, physicalCard) -> assignedBySide == Side.SHADOW || !game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.UNHASTY)
                                || game.getModifiersQuerying().isUnhastyCompanionAllowedToParticipateInSkirmishes(game, physicalCard)),
                Filters.and(
                        CardType.MINION,
                        new Filter() {
                            @Override
                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                return (game.getGameState().getCurrentPhase() != Phase.ASSIGNMENT || !game.getGameState().isFierceSkirmishes()) || game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.FIERCE);
                            }
                        }));

        return Filters.and(
                assignableFilter,
                (Filter) (game, physicalCard) -> {
                    if (!ignoreExistingAssignments) {
                        boolean assignedToSkirmish = Filters.assignedToSkirmish.accepts(game, physicalCard);

                        if (assignedToSkirmish) {
                            if(!ignoreDefender) {
                                int defender = game.getModifiersQuerying().getKeywordCount(game, physicalCard, Keyword.DEFENDER);
                                var assignments = game.getGameState().getAssignments()
                                        .stream().filter(x -> x.getFellowshipCharacter() == physicalCard || x.getShadowCharacters().contains(physicalCard)).collect(
                                                Collectors.toSet());
                                if(1 + defender <= assignments.size())
                                    return false;
                            }
                            else {
                                return false;
                            }
                        }
                    }
                    return game.getModifiersQuerying().canBeAssignedToSkirmish(game, assignedBySide, physicalCard);
                });
    }

    public static Filter siteBlock(final SitesBlock block) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getSiteBlock() == block;
    }
    public static final Filter frodo = Filters.name("Frodo");
    public static final Filter sam = Filters.name("Sam");

    public static final Filter weapon = Filters.or(PossessionClass.HAND_WEAPON, PossessionClass.RANGED_WEAPON);
    public static final Filter item = Filters.or(CardType.ARTIFACT, CardType.POSSESSION);
    public static final Filter character = Filters.or(CardType.ALLY, CardType.COMPANION, CardType.MINION);

    public static final Filter ringBearer = (game, physicalCard) -> game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId()) == physicalCard;

    public static final Filter inSkirmish = (game, physicalCard) -> {
        Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish != null) {
            return (skirmish.getFellowshipCharacter() == physicalCard)
                    || skirmish.getShadowCharacters().contains(physicalCard);
        }
        return false;
    };

    public static final Filter inFierceSkirmish = (game, physicalCard) -> {
        Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish != null && game.getGameState().isFierceSkirmishes()) {
            return (skirmish.getFellowshipCharacter() == physicalCard)
                    || skirmish.getShadowCharacters().contains(physicalCard);
        }
        return false;
    };

    public static final Filter inPlay = (game, physicalCard) -> physicalCard.getZone().isInPlay();

    public static final Filter active = (game, physicalCard) -> game.getGameState().isCardInPlayActive(physicalCard);

    public static Filter canTakeWounds(final PhysicalCard woundSource, final int count) {
        return (game, physicalCard) -> game.getModifiersQuerying().canTakeWounds(game, (woundSource != null) ? Collections.singleton(woundSource) : Collections.emptySet(), physicalCard, count) && game.getModifiersQuerying().getVitality(game, physicalCard) >= count;
    }

    public static Filter canBeDiscarded(final String performingPlayer, final PhysicalCard source) {
        return (game, physicalCard) -> game.getModifiersQuerying().canBeDiscardedFromPlay(game, performingPlayer, physicalCard, source);
    }

    public static final Filter exhausted = (game, physicalCard) -> game.getModifiersQuerying().getVitality(game, physicalCard) == 1;

    public static Filter inSkirmishAgainst(final Filterable... againstFilter) {
        return (game, physicalCard) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish != null && skirmish.getFellowshipCharacter() != null) {
                return (skirmish.getFellowshipCharacter() == physicalCard && Filters.acceptsAny(game, skirmish.getShadowCharacters(), Filters.and(againstFilter)))
                        || (skirmish.getShadowCharacters().contains(physicalCard) && Filters.and(againstFilter).accepts(game, skirmish.getFellowshipCharacter()));
            }
            return false;
        };
    }

    public static Filter recentlyInSkirmishAgainst(final Filterable... againstFilter) {
        return (game, physicalCard) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish != null && skirmish.getFellowshipCharacter() != null) {
                return (skirmish.getFellowshipCharacter() == physicalCard && (Filters.acceptsAny(game, skirmish.getShadowCharacters(), Filters.and(againstFilter)) || Filters.acceptsAny(game, skirmish.getRemovedFromSkirmish(), Filters.and(againstFilter))))
                    || (skirmish.getShadowCharacters().contains(physicalCard) && Filters.and(againstFilter).accepts(game, skirmish.getFellowshipCharacter()));

            }
            return false;
        };
    }

    public static Filter canBeReturnedToHand(final PhysicalCard source) {
        return (game, physicalCard) -> game.getModifiersQuerying().canBeReturnedToHand(game, physicalCard, source);
    }

    public static Filter canExert(final PhysicalCard source) {
        return canExert(source, 1);
    }

    public static Filter canExert(final PhysicalCard source, final int count) {
        return (game, physicalCard) -> game.getModifiersQuerying().getVitality(game, physicalCard) > count
                && game.getModifiersQuerying().canBeExerted(game, source, physicalCard);
    }

    public static Filter canHeal =
            (game, physicalCard) -> game.getGameState().getWounds(physicalCard) > 0 && game.getModifiersQuerying().canBeHealed(game, physicalCard);

    public static final Filter notAssignedToSkirmish = (game, physicalCard) -> {
        for (Assignment assignment : game.getGameState().getAssignments()) {
            if (assignment.getFellowshipCharacter() == physicalCard
                    || assignment.getShadowCharacters().contains(physicalCard))
                return false;
        }
        Skirmish skirmish = game.getGameState().getSkirmish();
        if (skirmish != null) {
            if (skirmish.getFellowshipCharacter() == physicalCard
                    || skirmish.getShadowCharacters().contains(physicalCard))
                return false;
        }
        return true;
    };

    public static final Filter assignedToSkirmish = Filters.not(Filters.notAssignedToSkirmish);

    public static Filter assignedToSkirmishAgainst(final Filterable... againstFilters) {
        return Filters.or(Filters.assignedAgainst(againstFilters), Filters.inSkirmishAgainst(againstFilters));
    }

    public static Filter assignedAgainst(final Filterable... againstFilters) {
        return (game, physicalCard) -> {
            for (Assignment assignment : game.getGameState().getAssignments()) {
                if (assignment.getFellowshipCharacter() == physicalCard)
                    return Filters.acceptsAny(game, assignment.getShadowCharacters(), Filters.and(againstFilters));
                else if (assignment.getShadowCharacters().contains(physicalCard) && assignment.getFellowshipCharacter() != null)
                    return Filters.and(againstFilters).accepts(game, assignment.getFellowshipCharacter());
            }
            return false;
        };
    }

    public static Filter playable(final LotroGame game, final int twilightModifier) {
        return playable(game, twilightModifier, false);
    }

    public static Filter playable(final LotroGame game, final int twilightModifier, final boolean ignoreRoamingPenalty) {
        return playable(game, twilightModifier, ignoreRoamingPenalty, false);
    }

    public static Filter playable(final LotroGame game, final int twilightModifier, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile) {
        return playable(game, 0, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, false);
    }

    public static Filter playable(final LotroGame game, final int twilightModifier, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile, final boolean ignoreResponseEvents) {
        return playable(game, 0, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, ignoreResponseEvents);
    }

    public static Filter playable(final LotroGame game, final int withTwilightRemoved, final int twilightModifier, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile, final boolean ignoreResponseEvents) {
        return (game1, physicalCard) -> {
            Side expectedSide = (physicalCard.getOwner().equals(game1.getGameState().getCurrentPlayerId()) ? Side.FREE_PEOPLE : Side.SHADOW);
            final LotroCardBlueprint blueprint = physicalCard.getBlueprint();
            if (blueprint.getSide() != expectedSide)
                return false;

            return PlayUtils.checkPlayRequirements(game1, physicalCard, Filters.any, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, ignoreResponseEvents);
        };
    }

    public static final Filter any = (game, physicalCard) -> true;
    public static final Filter none = (game, physicalCard) -> false;
    public static final Filter unique = (game, physicalCard) -> physicalCard.getBlueprint().isUnique();

    private static Filter signet(final Signet signet) {
        return (game, physicalCard) -> game.getModifiersQuerying().hasSignet(game, physicalCard, signet);
    }

    private static Filter race(final Race race) {
        return Filters.and(
                Filters.or(CardType.COMPANION, CardType.ALLY, CardType.MINION, CardType.FOLLOWER),
                (Filter) (game, physicalCard) -> game.getModifiersQuerying().isRace(game, physicalCard, race));
    }


    private static Filter side(final Side side) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getSide() == side;
    }

    public static Filter owner(final String playerId) {
        return (game, physicalCard) -> physicalCard.getOwner() != null && physicalCard.getOwner().equals(playerId);
    }

    public static Filter isAllyHome(final int siteNumber, final SitesBlock siteBlock) {
        return Filters.and(
                CardType.ALLY,
                (Filter) (game, physicalCard) -> RuleUtils.isAllyAtHome(physicalCard, siteNumber, siteBlock));
    }

    public static Filter isAllyInRegion(final int regionNumber, final SitesBlock siteBlock) {
        return Filters.and(
                CardType.ALLY,
                (Filter) (game, physicalCard) -> RuleUtils.isAllyInRegion(physicalCard, regionNumber, siteBlock));
    }

    public static final Filter allyAtHome = Filters.and(
            CardType.ALLY,
            (Filter) (game, physicalCard) -> RuleUtils.isAllyAtHome(physicalCard, game.getGameState().getCurrentSiteNumber(), game.getGameState().getCurrentSiteBlock()));

    public static Filter allyWithSameHome(final PhysicalCard card) {
        return Filters.and(
                CardType.ALLY,
                (Filter) (game, physicalCard) -> {
                    var originalBP = card.getBlueprint();
                    var newBP = physicalCard.getBlueprint();
                    if (originalBP.getCardType() == CardType.ALLY) {

                        var result = new HashSet(originalBP.getAllyHomes());
                        //If there is an overlap in home sites, then that overlap should end up
                        // in the result.  If result is empty, no overlaps in home sites.
                        result.retainAll(newBP.getAllyHomes());
                        return !result.isEmpty();
                    }
                    return false;
                });
    }

    public static final Filter currentSite = (game, physicalCard) -> game.getGameState().getCurrentSite() == physicalCard;

    public static final Filter nextSite = (game, physicalCard) -> game.getGameState().getSite(game.getGameState().getCurrentSiteNumber() + 1) == physicalCard;

    public static Filter siteNumber(final int siteNumber) {
        return siteNumberBetweenInclusive(siteNumber, siteNumber);
    }

    public static Filter siteHasSiteNumber = Filters.and(CardType.SITE,
            (Filter) (game, physicalCard) -> {
                int bpNumber = physicalCard.getBlueprint().getSiteNumber();
                Integer siteNumber = physicalCard.getSiteNumber();
                return Objects.requireNonNullElse(siteNumber, bpNumber) != 0;
            });

    public static Filter siteNumberBetweenInclusive(final int minSiteNumber, final int maxSiteNumber) {
        return (game, physicalCard) -> {
            if (physicalCard.getBlueprint().getCardType() == CardType.MINION) {
                int sitenum = game.getModifiersQuerying().getMinionSiteNumber(game, physicalCard);
                return sitenum >= minSiteNumber && sitenum <= maxSiteNumber;
            }

            return (physicalCard.getSiteNumber() != null)
                    && (physicalCard.getSiteNumber() >= minSiteNumber) && (physicalCard.getSiteNumber() <= maxSiteNumber);
        };
    }

    public static Filter siteInCurrentRegion = Filters.and(CardType.SITE,
            (Filter) (game, physicalCard) -> {
                int siteNumber = physicalCard.getSiteNumber();
                return GameUtils.getRegion(game) == GameUtils.getRegion(siteNumber);
            });

    public static Filter region(final int region) {
        return regionNumberBetweenInclusive(region, region);
    }

    public static Filter regionNumberBetweenInclusive(final int minRegionNumber, final int maxRegionNumber) {
        return (game, physicalCard) -> {

            if (physicalCard.getSiteNumber() == null)
                return false;

            int regionNumber = GameUtils.getRegion(physicalCard.getSiteNumber());

            return regionNumber >= minRegionNumber && regionNumber <= maxRegionNumber;
        };
    }

    public static Filter hasAttached(final Filterable... filters) {
        return hasAttached(1, filters);
    }

    public static Filter hasAttached(int count, final Filterable... filters) {
        return (game, physicalCard) -> {
            List<PhysicalCard> physicalCardList = game.getGameState().getAttachedCards(physicalCard);
            return (Filters.filter(game, physicalCardList, filters).size() >= count);
        };
    }

    public static Filter hasStacked(final Filterable... filter) {
        return hasStacked(1, filter);
    }

    public static Filter hasStacked(final int count, final Filterable... filter) {
        return (game, physicalCard) -> {
            List<PhysicalCard> physicalCardList = game.getGameState().getStackedCards(physicalCard);
            if (filter.length == 1 && filter[0] == Filters.any)
                return physicalCardList.size() >= count;
            return (Filters.filter(game, physicalCardList, Filters.and(filter, activeSide)).size() >= count);
        };
    }

    public static Filter not(final Filterable... filters) {
        return (game, physicalCard) -> !Filters.and(filters).accepts(game, physicalCard);
    }

    public static Filter sameCard(final PhysicalCard card) {
        final int cardId = card.getCardId();
        return (game, physicalCard) -> (physicalCard.getCardId() == cardId);
    }

    public static Filter in(final Collection<? extends PhysicalCard> cards) {
        final Set<Integer> cardIds = new HashSet<>();
        for (PhysicalCard card : cards)
            cardIds.add(card.getCardId());
        return (game, physicalCard) -> cardIds.contains(physicalCard.getCardId());
    }

    public static Filter zone(final Zone zone) {
        return (game, physicalCard) -> physicalCard.getZone() == zone;
    }

    public static Filter hasWounds(final int wounds) {
        return (game, physicalCard) -> game.getGameState().getWounds(physicalCard) >= wounds;
    }

    public static final Filter unwounded = (game, physicalCard) -> game.getGameState().getWounds(physicalCard) == 0;

    public static final Filter wounded = Filters.hasWounds(1);

    public static Filter name(final String name) {
        return (game, physicalCard) -> name != null && physicalCard.getBlueprint().getSanitizedTitle() != null && physicalCard.getBlueprint().getSanitizedTitle().equals(Names.SanitizeName(name));
    }

    private static Filter type(final CardType cardType) {
        return (game, physicalCard) -> game.getModifiersQuerying().isCardType(game, physicalCard, cardType);
    }

    public static Filter attachedTo(final Filterable... filters) {
        return (game, physicalCard) -> physicalCard.getAttachedTo() != null && Filters.and(filters).accepts(game, physicalCard.getAttachedTo());
    }

    public static Filter stackedOn(final Filterable... filters) {
        return (game, physicalCard) -> physicalCard.getStackedOn() != null && Filters.and(filters).accepts(game, physicalCard.getStackedOn());
    }

    public static Filter siteControlledByOtherPlayer(final String thisPlayer) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getCardType() == CardType.SITE && physicalCard.getCardController() != null && !physicalCard.getCardController().equals(thisPlayer);
    }

    public static Filter siteControlledByPlayer(final String playerId) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getCardType() == CardType.SITE && playerId.equals(physicalCard.getCardController());
    }

    public static Filter uncontrolledSite = (game, physicalCard) -> physicalCard.getBlueprint().getCardType() == CardType.SITE && physicalCard.getCardController() == null;

    private static Filter culture(final Culture culture) {
        return (game, physicalCard) -> (physicalCard.getBlueprint().getCulture() == culture);
    }

    private static Filter keyword(final Keyword keyword) {
        return (game, physicalCard) -> game.getModifiersQuerying().hasKeyword(game, physicalCard, keyword);
    }

    public static Filter and(final Filterable... filters) {
        Filter[] filtersInt = convertToFilters(filters);
        if (filtersInt.length == 1)
            return filtersInt[0];
        return andInternal(filtersInt);
    }

    public static Filter or(final Filterable... filters) {
        Filter[] filtersInt = convertToFilters(filters);
        if (filtersInt.length == 1)
            return filtersInt[0];
        return orInternal(filtersInt);
    }

    private static Filter[] convertToFilters(Filterable... filters) {
        Filter[] filtersInt = new Filter[filters.length];
        for (int i = 0; i < filtersInt.length; i++)
            filtersInt[i] = changeToFilter(filters[i]);
        return filtersInt;
    }

    public static Filter changeToFilter(Filterable filter) {
        if (filter instanceof Filter)
            return (Filter) filter;
        else if (filter instanceof PhysicalCard)
            return Filters.sameCard((PhysicalCard) filter);
        else if (filter instanceof CardType)
            return _typeFilterMap.get((CardType) filter);
        else if (filter instanceof Culture)
            return _cultureFilterMap.get((Culture) filter);
        else if (filter instanceof Keyword)
            return _keywordFilterMap.get((Keyword) filter);
        else if (filter instanceof Timeword)
            return _timewordFilterMap.get((Timeword) filter);
        else if (filter instanceof PossessionClass)
            return _possessionClassFilterMap.get((PossessionClass) filter);
        else if (filter instanceof Race)
            return _raceFilterMap.get((Race) filter);
        else if (filter instanceof Side)
            return _sideFilterMap.get((Side) filter);
        else if (filter instanceof Signet)
            return _signetFilterMap.get((Signet) filter);
        else if (filter instanceof Zone)
            return _zoneFilterMap.get((Zone) filter);
        else
            throw new IllegalArgumentException("Unknown type of filterable: " + filter);
    }

    public static Filter activeSide = (game, physicalCard) -> {
        boolean shadow = physicalCard.getBlueprint().getSide() == Side.SHADOW;
        if (shadow)
            return !physicalCard.getOwner().equals(game.getGameState().getCurrentPlayerId());
        else
            return physicalCard.getOwner().equals(game.getGameState().getCurrentPlayerId());
    };

    private static Filter andInternal(final Filter... filters) {
        return (game, physicalCard) -> {
            for (Filter filter : filters) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            return true;
        };
    }

    public static Filter and(final Filterable[] filters1, final Filterable... filters2) {
        final Filter[] newFilters1 = convertToFilters(filters1);
        final Filter[] newFilters2 = convertToFilters(filters2);
        if (newFilters1.length == 1 && newFilters2.length == 0)
            return newFilters1[0];
        if (newFilters1.length == 0 && newFilters2.length == 1)
            return newFilters2[0];
        return (game, physicalCard) -> {
            for (Filter filter : newFilters1) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            for (Filter filter : newFilters2) {
                if (!filter.accepts(game, physicalCard))
                    return false;
            }
            return true;
        };
    }

    public static boolean accepts(LotroGame game, PhysicalCard card, Filterable... filterable) {
        Filter filter = and(filterable);
        return filter.accepts(game, card);
    }

    public static boolean acceptsAny(LotroGame game, Iterable<? extends PhysicalCard> cards, Filterable... filterable) {
        Filter filter = and(filterable);
        for (PhysicalCard card : cards) {
            if (filter.accepts(game, card))
                return true;
        }
        return false;
    }

    private static Filter orInternal(final Filter... filters) {
        return (game, physicalCard) -> {
            for (Filter filter : filters) {
                if (filter.accepts(game, physicalCard))
                    return true;
            }
            return false;
        };
    }

    public static final Filter ringBoundCompanion = Filters.and(CardType.COMPANION, Keyword.RING_BOUND);
    public static final Filter unboundCompanion = Filters.and(CardType.COMPANION, Filters.not(Keyword.RING_BOUND));
    public static final Filter mounted = Filters.or(Filters.hasAttached(PossessionClass.MOUNT), Keyword.MOUNTED);

    public static Filter spottable = (game, physicalCard) -> true;

    private static class FindFirstActiveCardInPlayVisitor implements PhysicalCardVisitor {
        private final LotroGame game;
        private final Filter _filter;
        private PhysicalCard _card;

        private FindFirstActiveCardInPlayVisitor(LotroGame game, Filter filter) {
            this.game = game;
            _filter = filter;
        }

        @Override
        public boolean visitPhysicalCard(PhysicalCard physicalCard) {
            if (_filter.accepts(game, physicalCard)) {
                _card = physicalCard;
                return true;
            }
            return false;
        }

        public PhysicalCard getCard() {
            return _card;
        }
    }

    private static class GetCardsMatchingFilterVisitor extends CompletePhysicalCardVisitor {
        private final LotroGame game;
        private final Filter _filter;

        private final Set<PhysicalCard> _physicalCards = new HashSet<>();

        private GetCardsMatchingFilterVisitor(LotroGame game, Filter filter) {
            this.game = game;
            _filter = filter;
        }

        @Override
        protected void doVisitPhysicalCard(PhysicalCard physicalCard) {
            if (_filter.accepts(game, physicalCard))
                _physicalCards.add(physicalCard);
        }

        public int getCounter() {
            return _physicalCards.size();
        }

        public Set<PhysicalCard> getPhysicalCards() {
            return _physicalCards;
        }
    }
}
