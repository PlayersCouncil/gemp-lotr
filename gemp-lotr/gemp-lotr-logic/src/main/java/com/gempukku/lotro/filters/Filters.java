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

    public static final Filter unhindered = (game, physicalCard) ->  !game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.HINDERED);
    public static final Filter hindered = Filters.keyword(Keyword.HINDERED);

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


        //TODO: WTF is this doing?  There's no rule that says only companions can be rangers, etc.
        // Is it an optimization? Not bothering to check non-companions? But then every card still needs
        // checked for the right type.  Hmm, but then the modifiers don't needs checked and evaluated...

        // Some simple shortcuts for filters
        // Only companions can be rangers
        //_keywordFilterMap.put(Keyword.RANGER, Filters.and(CardType.COMPANION, keyword(Keyword.RANGER)));
        // Only allies can be villagers
        _keywordFilterMap.put(Keyword.VILLAGER, Filters.and(CardType.ALLY, keyword(Keyword.VILLAGER)));

        // Minion groups
        // This is the kind of crap I'm talking about.  We now have Bladetusk Mumakil that start as items and
        // grant themselves Southron as they transform, but this stupid thing keeps that from working.
        //_keywordFilterMap.put(Keyword.SOUTHRON, Filters.and(CardType.MINION, keyword(Keyword.SOUTHRON)));
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
        return countSpottable(game, SpotOverride.NONE, filters) >= count;
    }

    public static boolean canSpot(LotroGame game, int count, Map<InactiveReason, Boolean> spotOverrides, Filterable... filters) {
        return countSpottable(game, spotOverrides, filters) >= count;
    }

    /**
     * Whether the spotting player can spot this many cards, counting the bonuses to his own count
     * ({@link #countSpottable(LotroGame, String, Map, Filterable...)}).
     */
    public static boolean canSpot(LotroGame game, String spottingPlayer, int count, Map<InactiveReason, Boolean> spotOverrides, Filterable... filters) {
        return countSpottable(game, spottingPlayer, spotOverrides, filters) >= count;
    }

    public static boolean hasActive(LotroGame game, Filterable... filters) {
        return findFirstActive(game, SpotOverride.NONE, filters) != null;
    }

    public static boolean hasActive(LotroGame game, Map<InactiveReason, Boolean> spotOverrides, Filterable... filters) {
        return findFirstActive(game, spotOverrides, filters) != null;
    }

    public static Collection<PhysicalCard> filterActive(LotroGame game, Filterable... filters) { return filterActive(game, SpotOverride.NONE, filters); }

    public static Collection<PhysicalCard> filterActive(LotroGame game, Map<InactiveReason, Boolean> spotOverrides, Filterable... filters) {
        Filter filter = Filters.and(filters);
        var getCardsMatchingFilter = new GetCardsMatchingFilterVisitor(game, filter);
        game.getGameState().iterateActiveCards(getCardsMatchingFilter, spotOverrides);
        return getCardsMatchingFilter.getPhysicalCards();
    }

    public static Collection<PhysicalCard> filterActive(LotroGame game, Iterable<? extends PhysicalCard> cards, Filterable... filters) {
        Filter filter = Filters.and(filters);
        var getCardsMatchingFilter = new GetCardsMatchingFilterVisitor(game, filter);
        game.getGameState().iterateActiveCards(getCardsMatchingFilter, SpotOverride.NONE, cards);
        return getCardsMatchingFilter.getPhysicalCards();
    }

    public static Collection<PhysicalCard> filterActive(LotroGame game, Map<InactiveReason, Boolean> spotOverrides, Iterable<? extends PhysicalCard> cards, Filterable... filters) {
        Filter filter = Filters.and(filters);
        var getCardsMatchingFilter = new GetCardsMatchingFilterVisitor(game, filter);
        game.getGameState().iterateActiveCards(getCardsMatchingFilter, spotOverrides, cards);
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

    public static PhysicalCard findFirstActive(LotroGame game, Filterable... filters) { return findFirstActive(game, SpotOverride.NONE, filters); }

    public static PhysicalCard findFirstActive(LotroGame game, Map<InactiveReason, Boolean> spotOverrides, Filterable... filters) {
        FindFirstActiveCardInPlayVisitor visitor = new FindFirstActiveCardInPlayVisitor(game, Filters.and(filters));
        game.getGameState().iterateActiveCards(visitor, spotOverrides);
        return visitor.getCard();
    }

    public static int countSpottable(LotroGame game, Filterable... filters) { return countSpottable(game, SpotOverride.NONE, filters); }
    public static int countSpottable(LotroGame game, Map<InactiveReason, Boolean> spotOverrides, Filterable... filters) {
        return countSpottable(game, null, spotOverrides, filters);
    }

    /**
     * The number of cards the spotting player can spot. With one filter, the bonuses to that player's own count are
     * added ("add 1 to the number of Ents you can spot" raises its owner's count and no other: F-0502); a count made
     * for no player (null) gets none.
     */
    public static int countSpottable(LotroGame game, String spottingPlayer, Map<InactiveReason, Boolean> spotOverrides, Filterable... filters) {
        GetCardsMatchingFilterVisitor matchingFilterVisitor = new GetCardsMatchingFilterVisitor(game, Filters.and(filters, Filters.spottable));
        game.getGameState().iterateActiveCards(matchingFilterVisitor, spotOverrides);
        int result = matchingFilterVisitor.getCounter();
        //TODO: make this less dependent on how the definition is arranged
        if (filters.length == 1 && spottingPlayer != null)
            result += game.getModifiersQuerying().getSpotBonus(game, filters[0], spottingPlayer);
        return result;
    }

    public static int countActive(LotroGame game, Filterable... filters) { return countActive(game, SpotOverride.NONE, filters); }
    public static int countActive(LotroGame game, Map<InactiveReason, Boolean> spotOverrides, Filterable... filters) {
        GetCardsMatchingFilterVisitor matchingFilterVisitor = new GetCardsMatchingFilterVisitor(game, Filters.and(filters));
        game.getGameState().iterateActiveCards(matchingFilterVisitor, spotOverrides);
        return matchingFilterVisitor.getCounter();
    }

    // Filters available

    /**
     * An event of the given phase (or a response event).  A card is what is printed on it, except an event
     * printed with several phases while it is being played: that is an event of the phase it is played in and
     * of no other (CR 4.2 "phase action"; GameState.getPhaseOfEventBeingPlayed, F-0292).
     */
    public static Filter timeword(Timeword timeword) {
        return (game, physicalCard) -> {
            if (game != null && game.getGameState() != null) {
                final Timeword playedIn = game.getGameState().getPhaseOfEventBeingPlayed(physicalCard);
                if (playedIn != null)
                    return playedIn == timeword;
            }
            return physicalCard.getBlueprint().hasTimeword(timeword);
        };
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

    public static Filter printedTwilightCost(final Evaluator evaluator) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() == evaluator.evaluateExpression(game, null);
    }

    public static Filter maxPrintedTwilightCost(final int printedTwilightCost) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() <= printedTwilightCost;
    }

    public static Filter minPrintedTwilightCost(final int printedTwilightCost) {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() >= printedTwilightCost;
    }

    public static Filter evenPrintedTwilightCost() {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() % 2 == 0;
    }

    public static Filter oddPrintedTwilightCost() {
        return (game, physicalCard) -> physicalCard.getBlueprint().getTwilightCost() % 2 != 0;
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
                    for (PhysicalCard card : Filters.filterActive(game, SpotOverride.NONE, againstFilter)) {
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
                            // A card that says the ally may not participate in skirmishes beats whatever would
                            // have him skirmish: another card's "participates", or an assignment that takes an
                            // ally wherever he is ("can't" beats "can").
                            boolean preventedByCard = game.getModifiersQuerying().isAllyPreventedFromParticipatingInSkirmishes(game, assignedBySide, physicalCard);
                            if (preventedByCard)
                                return false;
                            if (allowAllyToSkirmish)
                                return true;
                            boolean allowedToSkirmish = game.getModifiersQuerying().isAllyAllowedToParticipateInSkirmishes(game, assignedBySide, physicalCard);
                            if (allowedToSkirmish)
                                return true;
                            return RuleUtils.isAllyAtHome(physicalCard, game.getGameState().getCurrentSiteNumber(), game.getGameState().getCurrentSiteBlock());
                        }),
                Filters.and(
                        CardType.COMPANION,
                        (Filter) (game, physicalCard) ->
                                //Shadow can always assign regardless of other factors
                                assignedBySide == Side.SHADOW
                                //non-unhasty companions can be assigned
                                || !game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.UNHASTY)
                                //hasty companions who have been explicitly allowed can be assigned
                                || game.getModifiersQuerying().isUnhastyCompanionAllowedToParticipateInSkirmishes(game, physicalCard)),
                Filters.and(
                        CardType.MINION,
						(Filter) (game, physicalCard) -> {

                            //If we are in a situation like evaluating targets for A Dark Shape Sprang, we automatically pass
                            if(game.getGameState().getCurrentPhase() != Phase.ASSIGNMENT)
                                return true;

                            if(game.getGameState().isNormalSkirmishes()) {
                                return true;
                            }

                            if(game.getGameState().isExtraSkirmishes()) {
                                return game.getModifiersQuerying().canBeAssignedToSkirmish(game,null, physicalCard);
                            }

                            if(game.getGameState().isRelentlessSkirmishes()) {
                                return game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.RELENTLESS);
                            }

                            if(game.getGameState().isFierceSkirmishes()) {
                                return game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.FIERCE);
                            }

							return false; //shouldn't ever hit this
						}));

        return Filters.and(
                assignableFilter,
                (Filter) (game, physicalCard) -> {
                    if (!ignoreExistingAssignments) {
                        boolean assignedToSkirmish = Filters.assignedToSkirmish.accepts(game, physicalCard);

                        if (assignedToSkirmish) {
                            if(!ignoreDefender) {
                                // One more, with defender counted against the minions the character already
                                // has (F-0355: not against the Assignment records naming him, never more than 1).
                                if(!RuleUtils.hasRoomForAssignments(game, physicalCard, 1))
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

    public static final Filter ringBearer = (NamesNoKind) (game, physicalCard) -> game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId()) == physicalCard;
    /**
     * The One Ring, by what is printed on it and not by what the game currently takes it for: a hindered card has
     * no card type to be found by (CR 5.0 "hinder"), and the rules must still find a hindered Ring (Q-0109).
     */
    public static final Filter theOneRing = (game, physicalCard) -> physicalCard.getBlueprint().getCardType() == CardType.THE_ONE_RING;

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

    public static final Filter inPlay = (game, physicalCard) -> physicalCard.getZone() != null && physicalCard.getZone().isInPlay();

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

    public static Filter canBeHindered(final PhysicalCard source) {
        return (game, physicalCard) -> game.getGameState().canBeHindered(physicalCard)
            && game.getModifiersQuerying().canBeHinderedBy(game, null, physicalCard, source);
    }

    public static Filter canBeRestored(final PhysicalCard source) {
        return (game, physicalCard) -> game.getGameState().isHindered(physicalCard);
    }

    public static Filter canExert(final PhysicalCard source) {
        return canExert(source, 1);
    }

    public static Filter canExert(final PhysicalCard source, final int count) {
        return (game, physicalCard) -> game.getModifiersQuerying().getVitality(game, physicalCard) > count
                && game.getModifiersQuerying().canBeExerted(game, source, physicalCard);
    }

    /**
     * A wounded card that performingPlayer may heal a wound from.
     */
    public static Filter canHeal(final String performingPlayer) {
        return (game, physicalCard) -> game.getGameState().getWounds(physicalCard) > 0
                && game.getModifiersQuerying().canBeHealed(game, performingPlayer, physicalCard);
    }

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

    /**
     * A card that could be played now by a card's effect ("play a ... from your discard pile", "... stacked
     * here", the definition word Playable) or as a phase action.  A response event is not such a card: it is
     * played in answer to its own trigger and to nothing else, so it is refused here unless it is also an event
     * of the current phase (Eye of Barad-dur, Skirmish and Response).  Only the rule that offers response events
     * in answer to their triggers asks with ignoreResponseEvents false.
     */
    public static Filter playable(final int twilightModifier) {
        return playable(twilightModifier, 0, false, false, true);
    }

    public static Filter playable(final int twilightModifier, final int withTwilightRemoved, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile, final boolean ignoreResponseEvents) {
        return playable(twilightModifier, withTwilightRemoved, 0, ignoreRoamingPenalty, ignoreCheckingDeadPile, ignoreResponseEvents);
    }

    /**
     * @param withTwilightRemoved twilight that will have left the pool before the card is played
     * @param withThreatsAdded threats that will have been added before the card is played
     */
    public static Filter playable(final int twilightModifier, final int withTwilightRemoved, final int withThreatsAdded, final boolean ignoreRoamingPenalty, final boolean ignoreCheckingDeadPile, final boolean ignoreResponseEvents) {
        return (game, physicalCard) -> {
            Side expectedSide = (physicalCard.getOwner().equals(game.getGameState().getCurrentPlayerId()) ? Side.FREE_PEOPLE : Side.SHADOW);
            final LotroCardBlueprint blueprint = physicalCard.getBlueprint();
            if (blueprint.getSide() != expectedSide)
                return false;

            return PlayUtils.checkPlayRequirements(game, physicalCard, Filters.any, withTwilightRemoved, withThreatsAdded, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, ignoreResponseEvents);
        };
    }

    /**
     * A card in play that a card's effect could transfer now to one of the cards the filters accept (the
     * definition word TransferableTo): nothing says the card cannot be transferred, and among those cards there
     * is an active one it may go to (RuleUtils.getTransferTargetFilter: an eligible bearer other than the one it
     * has).  It is the choice the Transfer effect makes of the card to transfer where it has checkTarget and a
     * bearer to be chosen (F-0340), so a requirement written with it withholds "you may transfer ... to another
     * eligible bearer" when a yes would move nothing (F-0324).
     */
    public static Filter transferableTo(final Filterable... to) {
        final Filter toFilter = Filters.and(to);
        return (game, physicalCard) -> physicalCard.getZone().isInPlay()
                && game.getModifiersQuerying().canBeTransferred(game, physicalCard)
                && Filters.countActive(game, toFilter, RuleUtils.getTransferTargetFilter(game, physicalCard)) > 0;
    }

    /**
     * Filter for cards that can be played ignoring all costs (twilight, ToPlay, ExtraCosts, roaming).
     * Still respects uniqueness, Rule of Nine, and CantPlayCards modifiers.
     */
    public static Filter playableIgnoringCosts() {
        return (game, physicalCard) -> PlayUtils.checkPlayRequirementsIgnoringCosts(game, physicalCard);
    }

    public static final Filter any = (NamesNoKind) (game, physicalCard) -> true;
    public static final Filter none = (game, physicalCard) -> false;
    public static final Filter unique = (game, physicalCard) -> game.getModifiersQuerying().getUniqueness(game, physicalCard) == 1;

    public static final Filter twodot = (game, physicalCard) -> game.getModifiersQuerying().getUniqueness(game, physicalCard) == 2;
    public static final Filter threedot = (game, physicalCard) -> game.getModifiersQuerying().getUniqueness(game, physicalCard) == 3;

    public static Filter restricted(int amount) {
        return (game, physicalCard) -> game.getModifiersQuerying().getUniqueness(game, physicalCard) == amount;
    }

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

    /**
     * A character a card says "may not participate in archery fire" (Streets of Edoras). Participating has
     * parts: counting for the archery total, and taking archery wounds. The bar is on both, so a card that sends
     * archery wounds to allies sends none to this one.
     */
    public static final Filter barredFromArcheryFire =
            (game, physicalCard) -> game.getModifiersQuerying().isAllyPreventedFromParticipatingInArcheryFire(game, physicalCard);

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
                return bpNumber != 0;
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
        return hasAttached(count, SpotOverride.NONE, filters);
    }

    /**
     * Bearing at least that many cards that the filters accept, counted with the overrides given: with
     * SpotOverride.INCLUDE_HINDERED a hindered card the bearer bears is counted, which is what "bearing 3 or more
     * cards" means where the text says "cards" and nothing more (Ketura, 2026-10-07, on Lying Counsel 15_85; a
     * hindered card is still a card, and still borne).
     */
    public static Filter hasAttached(int count, Map<InactiveReason, Boolean> spotOverrides, final Filterable... filters) {
        return (game, physicalCard) -> {
            List<PhysicalCard> physicalCardList = game.getGameState().getAttachedCards(physicalCard);
            return (Filters.filterActive(game, spotOverrides, physicalCardList, filters).size() >= count);
        };
    }

    /**
     * The card that bears the given card, as that card's own text means it ("bearer"). This is hasAttached, but a
     * hindered card still has its bearer: it is still attached, and CR 5.0 "hinder" has the text of a card that is
     * hindered in the middle of its own special ability or trigger carried out until fulfilled. Nothing of a
     * hindered card starts by this: its modifiers, triggers and abilities are stopped where the card is hindered.
     */
    public static Filter bearerOf(final PhysicalCard attachment) {
        return (NamesNoKind) (game, physicalCard) -> {
            List<PhysicalCard> physicalCardList = game.getGameState().getAttachedCards(physicalCard);
            return !Filters.filterActive(game, SpotOverride.INCLUDE_HINDERED, physicalCardList, attachment).isEmpty();
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
        return (RulesAKindOut) (game, physicalCard) -> !Filters.and(filters).accepts(game, physicalCard);
    }

    public static Filter sameCard(final PhysicalCard card) {
        final int cardId = card.getCardId();
        return (game, physicalCard) -> (physicalCard.getCardId() == cardId);
    }

    public static Filter in(final Collection<? extends PhysicalCard> cards) {
        final Set<Integer> cardIds = new HashSet<>();
        for (PhysicalCard card : cards)
            cardIds.add(card.getCardId());
        return (NamesNoKind) (game, physicalCard) -> cardIds.contains(physicalCard.getCardId());
    }

    public static Filter zone(final Zone zone) {
        return (game, physicalCard) -> physicalCard.getZone() == zone;
    }

    /**
     * Whether the card is in a support area: lying in one, or borne, at any depth, by a card that is.  A card
     * that a companion or a minion bears is not.  Ketura, 2026-10-07 (Q-0099): "'Support' is meant to be shorthand
     * for 'in the support area'.  A card attached to another is on Gemp in the 'attached zone', but ultimately
     * that's an implementation detail; it is still 'in the support area'."  An ally and a site that a player
     * controls lie in a support area, so what they bear is in it too.
     */
    public static boolean isInSupportArea(PhysicalCard card) {
        return supportAreaOf(card) != null;
    }

    /**
     * The player in whose support area the card is, or null if it is in nobody's.  Ketura, 2026-10-07 (Q-0101):
     * "If my opponent's Black Breath is on my Elrond, it's in my support area, because that's where Elrond is."
     * So the area is that of the card at the bottom of the chain of bearers, whoever owns the cards above it: an
     * ally lies in its owner's support area, and a site a player controls in the support area of the player who
     * controls it, whoever's adventure deck it came from.  A stacked card is in nobody's ("Stacked cards are
     * definitely not seen"), and so is anything a companion or a minion bears.
     */
    public static String supportAreaOf(PhysicalCard card) {
        PhysicalCard at = card;
        // A card is borne by one card at most, so the walk ends; the count is against a chain that is broken.
        for (int depth = 0; at != null && depth < 50; depth++) {
            if (at.getZone() == Zone.SUPPORT)
                return at.getCardController() != null ? at.getCardController() : at.getOwner();
            if (at.getZone() != Zone.ATTACHED)
                return null;
            at = at.getAttachedTo();
        }
        return null;
    }

    /**
     * "In <that player's> support area", borne cards included (see supportAreaOf).
     */
    public static Filter inSupportAreaOf(final String playerId) {
        return (game, physicalCard) -> playerId != null && playerId.equals(supportAreaOf(physicalCard));
    }

    /**
     * "In an opponent's support area", for the player given: in a support area that is not that player's.
     */
    public static Filter inSupportAreaOfAnOpponentOf(final String playerId) {
        return (game, physicalCard) -> {
            String area = supportAreaOf(physicalCard);
            return area != null && !area.equals(playerId);
        };
    }

    /**
     * A "support card": a card in a support area (see isInSupportArea).  Filters.zone(Zone.SUPPORT) is the card
     * lying in the support area itself, which is what "transfer this from your support area" means.
     */
    public static final Filter inSupportArea = (game, physicalCard) -> isInSupportArea(physicalCard);

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

    /**
     * Borne by a card that every one of the filters accepts.  A hindered bearer has no title, culture, card type,
     * race or number to be found by (CR 5.0 "hinder"), so a card it bears is not "borne by a companion" or "borne
     * by a Hobbit" (Ketura on Q-0108): a hindered bearer is found only where no filter given finds him by a kind
     * of card (findsABearerByNoKind).  The card borne is still in play and still borne, and "the cards this card
     * bears", "a card on bearer" and "each attached follower" go on finding it.
     * <p>
     * A not(...) is the other thing a hindered bearer is asked: it finds the bearer by nothing and only rules a
     * kind of bearer out, and it is asked of him as he is printed.  Asfaloth 1_31's attachedTo(not(ally)) is
     * there to leave out an ally, who is at his home site and not at the fellowship's: borne by a hindered
     * companion it is with the fellowship all the same, and borne by a hindered ally it is still at his home
     * (Ketura on Q-0252).  A not(...) joined to another word arrives here inside an and, and is not seen.
     */
    public static Filter attachedTo(final Filterable... filters) {
        final boolean findsAHinderedBearer = findsABearerByNoKind(filters);
        return (game, physicalCard) -> {
            final PhysicalCard bearer = physicalCard.getAttachedTo();
            if (bearer == null)
                return false;
            if (!findsAHinderedBearer && game.getGameState().isHindered(bearer))
                return false;
            return Filters.and(filters).accepts(game, bearer);
        };
    }

    /**
     * A filter that says which card is meant and nothing of what kind of card it is: the cards given (in), the
     * card that bears a given card (bearerOf), the Ring-bearer, who is that by the rules and not by anything
     * printed on him, and any card at all.  A hindered card is still found by these.  A filter is given this type
     * where it is made.  Every other filter but a not(...) by itself (RulesAKindOut) is taken to go by something
     * a hindered card has not got: its title, culture, card type, race, item class, keywords and numbers "do not
     * apply" (CR 5.0 "hinder").  That takes in any filter built with and or or, which cannot be seen into, and so
     * every attachedTo of a definition that gives more than one word for the bearer: FilterFactory joins them
     * before they come here.  Two things a hindered card does keep are not marked, because no definition writes
     * either inside attachedTo: its side, and being hindered.
     */
    private interface NamesNoKind extends Filter {
    }

    /**
     * A filter made by not(...): it says what a card is not.  Given to attachedTo by itself it finds the bearer
     * by no kind of card, so a hindered bearer is asked it too, as he is printed.
     */
    private interface RulesAKindOut extends Filter {
    }

    /**
     * Whether attachedTo asks these filters of a hindered bearer: none of them finds the bearer by a kind of card.
     * Each is a card itself, a filter made as NamesNoKind, or a not(...) (RulesAKindOut).  A card given as itself
     * is a card's own "self": its text is not in force while it is hindered, so this is reached only by a text
     * being carried out "until fulfilled" after its card was hindered in the middle of it (CR 5.0 "hinder"), and
     * no card in print shows it.
     */
    private static boolean findsABearerByNoKind(final Filterable... filters) {
        for (Filterable filter : filters) {
            if (!(filter instanceof PhysicalCard || filter instanceof NamesNoKind || filter instanceof RulesAKindOut))
                return false;
        }
        return true;
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
        return (game, card) -> (game.getModifiersQuerying().isCulture(game, card, culture));
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

    public static Filter spottable =  (game, physicalCard) -> true;

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

    /**
     * Gathers the cards a filter accepts, and hands them back in the order of their card ids: the same order in
     * every run of the same game, whoever asks (Ketura on Q-0142).  A card is given a new id as it comes into play,
     * so for cards in play this is the order they came into play, and a card that took another's place stands
     * where that one stood.  Whatever takes "each" or "every" card matching a filter takes them in this order,
     * and so does a decision that lists them.
     */
    private static class GetCardsMatchingFilterVisitor extends CompletePhysicalCardVisitor {
        private final LotroGame game;
        private final Filter _filter;

        // Kept in the order visited.  A set whose order was the cards' hash codes stood here before, and those
        // differ from one run to the next.
        private final Set<PhysicalCard> _physicalCards = new LinkedHashSet<>();
        // The cards in play are nearly always visited in the order of their ids already, so the sorting is only
        // done when a card has been met out of turn.
        private boolean _inIdOrder = true;
        private int _lastCardId = Integer.MIN_VALUE;

        private GetCardsMatchingFilterVisitor(LotroGame game, Filter filter) {
            this.game = game;
            _filter = filter;
        }

        @Override
        protected void doVisitPhysicalCard(PhysicalCard physicalCard) {
            if (_filter.accepts(game, physicalCard)) {
                if (_physicalCards.add(physicalCard)) {
                    final int cardId = physicalCard.getCardId();
                    if (cardId < _lastCardId)
                        _inIdOrder = false;
                    _lastCardId = cardId;
                }
            }
        }

        public int getCounter() {
            return _physicalCards.size();
        }

        public Set<PhysicalCard> getPhysicalCards() {
            if (_inIdOrder)
                return _physicalCards;

            List<PhysicalCard> sorted = new ArrayList<>(_physicalCards);
            sorted.sort(Comparator.comparingInt(PhysicalCard::getCardId));
            return new LinkedHashSet<>(sorted);
        }
    }
}
