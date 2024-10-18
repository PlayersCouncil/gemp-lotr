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
import com.gempukku.lotro.logic.modifiers.Condition;
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
        return countSpottable(game, filters)>=count;
    }

    public static Collection<PhysicalCard> filterActive(LotroGame game, Filterable... filters) {
        Filter filter = Filters.and(filters);
        GetCardsMatchingFilterVisitor getCardsMatchingFilter = new GetCardsMatchingFilterVisitor(game, filter);
        game.getGameState().iterateActiveCards(getCardsMatchingFilter);
        return getCardsMatchingFilter.getPhysicalCards();
    }

    public static Collection<PhysicalCard> filter(Iterable<? extends PhysicalCard> cards, LotroGame game, Filterable... filters) {
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
        if (filters.length==1)
            result+=game.getModifiersQuerying().getSpotBonus(game, filters[0]);
        return result;
    }

    public static int countActive(LotroGame game, Filterable... filters) {
        GetCardsMatchingFilterVisitor matchingFilterVisitor = new GetCardsMatchingFilterVisitor(game, Filters.and(filters));
        game.getGameState().iterateActiveCards(matchingFilterVisitor);
        return matchingFilterVisitor.getCounter();
    }

    // Filters available

    public static Filter conditionFilter(final Filterable defaultFilters, final Condition condition, final Filterable conditionMetFilter) {
        final Filter filter1 = changeToFilter(defaultFilters);
        final Filter filter2 = changeToFilter(conditionMetFilter);
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                if (condition.isFullfilled(game))
                    return filter2.accepts(game, physicalCard);
                else
                    return filter1.accepts(game, physicalCard);
            }
        };
    }

    public static Filter canSpotCompanionWithStrengthAtLeast(final int strength) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return Filters.canSpot(game, CardType.COMPANION, Filters.not(Filters.lessStrengthThan(strength)));
            }
        };
    }

    public static Filter lessVitalityThan(final int vitality) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().getVitality(game, physicalCard) < vitality;
            }
        };
    }

    public static Filter moreVitalityThan(final int vitality) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().getVitality(game, physicalCard) > vitality;
            }
        };
    }

    public static Filter maxResistance(final int resistance) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().getResistance(game, physicalCard) <= resistance;
            }
        };
    }

    public static Filter minResistance(final int resistance) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().getResistance(game, physicalCard) >= resistance;
            }
        };
    }

    public static Filter minVitality(final int vitality) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().getVitality(game, physicalCard) >= vitality;
            }
        };
    }

    public static Filter strengthEqual(final Evaluator evaluator) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().getStrength(game, physicalCard) == evaluator.evaluateExpression(game, null);
            }
        };
    }

    public static Filter moreStrengthThan(final int strength) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().getStrength(game, physicalCard) > strength;
            }
        };
    }

    public static Filter lessStrengthThan(final int strength) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().getStrength(game, physicalCard) < strength;
            }
        };
    }

    public static Filter lessStrengthThan(final PhysicalCard card) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().getStrength(game, physicalCard) < game.getModifiersQuerying().getStrength(game, card);
            }
        };
    }


    private static Filter possessionClass(final PossessionClass possessionClass) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                final Set<PossessionClass> possessionClasses = physicalCard.getBlueprint().getPossessionClasses();
                return possessionClasses != null && possessionClasses.contains(possessionClass);
            }
        };
    }

    public static Filter hasAnyCultureTokens() {
        return hasAnyCultureTokens(1);
    }

    public static Filter hasAnyCultureTokens(final int count) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                Map<Token, Integer> tokens = game.getGameState().getTokens(physicalCard);
                for (Map.Entry<Token, Integer> tokenCount : tokens.entrySet()) {
                    if (tokenCount.getKey().getCulture() != null)
                        if (tokenCount.getValue() >= count)
                            return true;
                }
                return false;
            }
        };
    }

    public static Filter printedTwilightCost(final int printedTwilightCost) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getBlueprint().getTwilightCost() == printedTwilightCost;
            }
        };
    }

    public static Filter maxPrintedTwilightCost(final int printedTwilightCost) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getBlueprint().getTwilightCost() <= printedTwilightCost;
            }
        };
    }

    public static Filter minPrintedTwilightCost(final int printedTwilightCost) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getBlueprint().getTwilightCost() >= printedTwilightCost;
            }
        };
    }

    public static Filter hasToken(final Token token) {
        return hasToken(token, 1);
    }

    public static Filter hasToken(final Token token, final int count) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getGameState().getTokenCount(physicalCard, token) >= count;
            }
        };
    }

    public static Filter assignableToSkirmishAgainst(final Side assignedBySide, final Filterable againstFilter) {
        return assignableToSkirmishAgainst(assignedBySide, againstFilter, false, false);
    }

    public static Filter notPreventedByEffectToAssign(final Side assignedBySide, final PhysicalCard againstCard) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                if (againstCard.getBlueprint().getSide() == Side.FREE_PEOPLE) {
                    Map<PhysicalCard, Set<PhysicalCard>> assignment = new HashMap<>();
                    assignment.put(againstCard, Collections.singleton(physicalCard));
                    return game.getModifiersQuerying().isValidAssignments(game, assignedBySide, assignment);
                } else {
                    Map<PhysicalCard, Set<PhysicalCard>> assignment = new HashMap<>();
                    assignment.put(physicalCard, Collections.singleton(againstCard));
                    return game.getModifiersQuerying().isValidAssignments(game, assignedBySide, assignment);
                }
            }
        };
    }

    public static Filter assignableToSkirmishAgainst(final Side assignedBySide, final Filterable againstFilter, final boolean ignoreUnassigned, final boolean allowAllyToSkirmish) {
        return Filters.and(
                assignableToSkirmish(assignedBySide, ignoreUnassigned, allowAllyToSkirmish),
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        for (PhysicalCard card : Filters.filterActive(game, againstFilter)) {
                            if (card.getBlueprint().getSide() != physicalCard.getBlueprint().getSide()
                                    && Filters.assignableToSkirmish(assignedBySide, ignoreUnassigned, allowAllyToSkirmish).accepts(game, card)) {
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
                    }
                });
    }

    public static Filter assignableToSkirmish(final Side assignedBySide, final boolean ignoreUnassigned, final boolean allowAllyToSkirmish) {
        Filter assignableFilter = Filters.or(
                Filters.and(
                        CardType.ALLY,
                        new Filter() {
                            @Override
                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                if (allowAllyToSkirmish)
                                    return true;
                                boolean allowedToSkirmish = game.getModifiersQuerying().isAllyAllowedToParticipateInSkirmishes(game, assignedBySide, physicalCard);
                                if (allowedToSkirmish)
                                    return true;
                                boolean preventedByCard = game.getModifiersQuerying().isAllyPreventedFromParticipatingInSkirmishes(game, assignedBySide, physicalCard);
                                if (preventedByCard)
                                    return false;
                                return RuleUtils.isAllyAtHome(physicalCard, game.getGameState().getCurrentSiteNumber(), game.getGameState().getCurrentSiteBlock());
                            }
                        }),
                Filters.and(
                        CardType.COMPANION,
                        new Filter() {
                            @Override
                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                return assignedBySide == Side.SHADOW || !game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.UNHASTY)
                                        || game.getModifiersQuerying().isUnhastyCompanionAllowedToParticipateInSkirmishes(game, physicalCard);
                            }
                        }),
                Filters.and(
                        CardType.MINION,
                        Filters.notAssignedToSkirmish,
                        new Filter() {
                            @Override
                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                return (game.getGameState().getCurrentPhase() != Phase.ASSIGNMENT || !game.getGameState().isFierceSkirmishes()) || game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.FIERCE);
                            }
                        }));

        return Filters.and(
                assignableFilter,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        if (!ignoreUnassigned) {
                            boolean notAssignedToSkirmish = Filters.notAssignedToSkirmish.accepts(game, physicalCard);
                            if (!notAssignedToSkirmish)
                                return false;
                        }
                        return game.getModifiersQuerying().canBeAssignedToSkirmish(game, assignedBySide, physicalCard);
                    }
                });
    }

    public static Filter siteBlock(final SitesBlock block) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getBlueprint().getSiteBlock() == block;
            }
        };
    }

    public static final Filter saruman = Filters.name("Saruman");
    public static final Filter witchKing = Filters.name(Names.witchKing);
    public static final Filter balrog = Filters.name("The Balrog");

    public static final Filter gollum = Filters.name("Gollum");
    public static final Filter smeagol = Filters.name("Smeagol");
    public static final Filter gollumOrSmeagol = Filters.or(gollum, smeagol);

    public static final Filter aragorn = Filters.name("Aragorn");
    public static final Filter gandalf = Filters.name("Gandalf");
    public static final Filter gimli = Filters.name("Gimli");
    public static final Filter arwen = Filters.name("Arwen");
    public static final Filter legolas = Filters.name("Legolas");
    public static final Filter boromir = Filters.name("Boromir");
    public static final Filter frodo = Filters.name("Frodo");
    public static final Filter sam = Filters.name("Sam");

    public static final Filter galadriel = Filters.name("Galadriel");

    public static final Filter weapon = Filters.or(PossessionClass.HAND_WEAPON, PossessionClass.RANGED_WEAPON);
    public static final Filter item = Filters.or(CardType.ARTIFACT, CardType.POSSESSION);
    public static final Filter character = Filters.or(CardType.ALLY, CardType.COMPANION, CardType.MINION);

    public static final Filter ringBearer = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId()) == physicalCard;
        }
    };

    public static final Filter inSkirmish = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish != null) {
                return (skirmish.getFellowshipCharacter() == physicalCard)
                        || skirmish.getShadowCharacters().contains(physicalCard);
            }
            return false;
        }
    };

    public static final Filter inFierceSkirmish = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish != null && game.getGameState().isFierceSkirmishes()) {
                return (skirmish.getFellowshipCharacter() == physicalCard)
                        || skirmish.getShadowCharacters().contains(physicalCard);
            }
            return false;
        }
    };

    public static final Filter inPlay = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return physicalCard.getZone().isInPlay();
        }
    };

    public static final Filter active = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return game.getGameState().isCardInPlayActive(physicalCard);
        }
    };

    public static Filter canTakeWounds(final PhysicalCard woundSource, final int count) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().canTakeWounds(game, (woundSource != null)?Collections.singleton(woundSource):Collections.emptySet(), physicalCard, count) && game.getModifiersQuerying().getVitality(game, physicalCard) >= count;
            }
        };
    }

    public static Filter canTakeWounds(final Collection<PhysicalCard> woundSources, final int count) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().canTakeWounds(game, woundSources, physicalCard, count) && game.getModifiersQuerying().getVitality(game, physicalCard) >= count;
            }
        };
    }

    public static Filter canBeDiscarded(final PhysicalCard source) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().canBeDiscardedFromPlay(game, source.getOwner(), physicalCard, source);
            }
        };
    }

    public static Filter canBeDiscarded(final String performingPlayer, final PhysicalCard source) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().canBeDiscardedFromPlay(game, performingPlayer, physicalCard, source);
            }
        };
    }

    public static final Filter exhausted = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return game.getModifiersQuerying().getVitality(game, physicalCard) == 1;
        }
    };

    public static Filter inSkirmishAgainst(final Filterable... againstFilter) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                Skirmish skirmish = game.getGameState().getSkirmish();
                if (skirmish != null && skirmish.getFellowshipCharacter() != null) {
                    return (skirmish.getFellowshipCharacter() == physicalCard && Filters.filter(skirmish.getShadowCharacters(), game, againstFilter).size() > 0)
                            || (skirmish.getShadowCharacters().contains(physicalCard) && Filters.and(againstFilter).accepts(game, skirmish.getFellowshipCharacter()));
                }
                return false;
            }
        };
    }

    public static Filter canExert(final PhysicalCard source) {
        return canExert(source, 1);
    }

    public static Filter canExert(final PhysicalCard source, final int count) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().getVitality(game, physicalCard) > count
                        && game.getModifiersQuerying().canBeExerted(game, source, physicalCard);
            }
        };
    }

    public static Filter canHeal =
            new Filter() {
                @Override
                public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                    return game.getGameState().getWounds(physicalCard) > 0 && game.getModifiersQuerying().canBeHealed(game, physicalCard);
                }
            };

    public static final Filter notAssignedToSkirmish = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
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
        }
    };

    public static final Filter assignedToSkirmish = Filters.not(Filters.notAssignedToSkirmish);

    public static Filter assignedToSkirmishAgainst(final Filterable... againstFilters) {
        return Filters.or(Filters.assignedAgainst(againstFilters), Filters.inSkirmishAgainst(againstFilters));
    }

    public static Filter assignedAgainst(final Filterable... againstFilters) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                for (Assignment assignment : game.getGameState().getAssignments()) {
                    if (assignment.getFellowshipCharacter() == physicalCard)
                        return Filters.filter(assignment.getShadowCharacters(), game, againstFilters).size() > 0;
                    else if (assignment.getShadowCharacters().contains(physicalCard) && assignment.getFellowshipCharacter() != null)
                        return Filters.and(againstFilters).accepts(game, assignment.getFellowshipCharacter());
                }
                return false;
            }
        };
    }

    public static Filter playable() {
        return playable(null);
    }

    public static Filter playable(final LotroGame game) {
        return playable(game, 0);
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
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                Side expectedSide = (physicalCard.getOwner().equals(game.getGameState().getCurrentPlayerId()) ? Side.FREE_PEOPLE : Side.SHADOW);
                final LotroCardBlueprint blueprint = physicalCard.getBlueprint();
                if (blueprint.getSide() != expectedSide)
                    return false;

                return PlayUtils.checkPlayRequirements(game, physicalCard, Filters.any, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile, ignoreResponseEvents);
            }
        };
    }

    public static final Filter any = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return true;
        }
    };

    public static final Filter none = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return false;
        }
    };

    public static final Filter unique = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return physicalCard.getBlueprint().isUnique();
        }
    };

    private static Filter signet(final Signet signet) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().hasSignet(game, physicalCard, signet);
            }
        };
    }

    private static Filter race(final Race race) {
        return Filters.and(
                Filters.or(CardType.COMPANION, CardType.ALLY, CardType.MINION, CardType.FOLLOWER),
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        LotroCardBlueprint blueprint = physicalCard.getBlueprint();
                        return blueprint.getRace() == race;
                    }
                });
    }


    private static Filter side(final Side side) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getBlueprint().getSide() == side;
            }
        };
    }

    public static Filter owner(final String playerId) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getOwner() != null && physicalCard.getOwner().equals(playerId);
            }
        };
    }

    public static Filter isAllyHome(final int siteNumber, final SitesBlock siteBlock) {
        return Filters.and(
                CardType.ALLY,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return RuleUtils.isAllyAtHome(physicalCard, siteNumber, siteBlock);
                    }
                });
    }

    public static Filter isAllyInRegion(final int regionNumber, final SitesBlock siteBlock) {
        return Filters.and(
                CardType.ALLY,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return RuleUtils.isAllyInRegion(physicalCard, regionNumber, siteBlock);
                    }
                });
    }

    public static Filter isAllyInCurrentRegion() {
        return Filters.and(
                CardType.ALLY,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return RuleUtils.isAllyInRegion(physicalCard, GameUtils.getRegion(game), game.getGameState().getCurrentSiteBlock());
                    }
                });
    }

    public static final Filter allyAtHome = Filters.and(
            CardType.ALLY,
            new Filter() {
                @Override
                public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                    return RuleUtils.isAllyAtHome(physicalCard, game.getGameState().getCurrentSiteNumber(), game.getGameState().getCurrentSiteBlock());
                }
            });

    public static Filter allyWithSameHome(final PhysicalCard card) {
        return Filters.and(
                CardType.ALLY,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        LotroCardBlueprint blueprint = card.getBlueprint();
                        if (blueprint.getCardType() == CardType.ALLY) {
                            SitesBlock homeBlock = blueprint.getAllyHomeSiteBlock();
                            int[] homeSites = blueprint.getAllyHomeSiteNumbers();
                            for (int homeSite : homeSites) {
                                if (RuleUtils.isAllyAtHome(physicalCard, homeSite, homeBlock)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                });
    }

    public static final Filter currentSite = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return game.getGameState().getCurrentSite() == physicalCard;
        }
    };

    public static final Filter currentRegion = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return GameUtils.getRegion(game) == GameUtils.getRegion(physicalCard.getSiteNumber());
        }
    };

    public static Filter siteNumber(final int siteNumber) {
        return siteNumberBetweenInclusive(siteNumber, siteNumber);
    }
    public static Filter siteHasSiteNumber = Filters.and(CardType.SITE,
            new Filter() {
                @Override
                public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                    int bpNumber = physicalCard.getBlueprint().getSiteNumber();
                    Integer siteNumber = physicalCard.getSiteNumber();
                    return Objects.requireNonNullElse(siteNumber, bpNumber) != 0;
                }
            });

    public static Filter siteNumberBetweenInclusive(final int minSiteNumber, final int maxSiteNumber) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                if(physicalCard.getBlueprint().getCardType() == CardType.MINION)
                {
                    int sitenum = game.getModifiersQuerying().getMinionSiteNumber(game, physicalCard);
                    return sitenum >= minSiteNumber && sitenum <= maxSiteNumber;
                }

                return (physicalCard.getSiteNumber()!=null)
                        && (physicalCard.getSiteNumber()>=minSiteNumber) && (physicalCard.getSiteNumber()<=maxSiteNumber);
            }
        };
    }

    public static Filter siteInCurrentRegion = Filters.and(CardType.SITE,
            new Filter() {
                @Override
                public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                    int siteNumber = physicalCard.getSiteNumber();
                    return GameUtils.getRegion(game) == GameUtils.getRegion(siteNumber);
                }
            });

    public static Filter region(final int region) { return regionNumberBetweenInclusive(region, region); }

    public static Filter regionNumberBetweenInclusive(final int minRegionNumber, final int maxRegionNumber) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {

                if(physicalCard.getSiteNumber() == null)
                    return false;

                int regionNumber = GameUtils.getRegion(physicalCard.getSiteNumber());

                return regionNumber >= minRegionNumber && regionNumber <= maxRegionNumber;
            }
        };
    }

    public static Filter hasAttached(final Filterable... filters) {
        return hasAttached(1, filters);
    }

    public static Filter hasAttached(int count, final Filterable... filters) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                List<PhysicalCard> physicalCardList = game.getGameState().getAttachedCards(physicalCard);
                return (Filters.filter(physicalCardList, game, filters).size() >= count);
            }
        };
    }

    public static Filter hasStacked(final Filterable... filter) {
        return hasStacked(1, filter);
    }

    public static Filter hasStacked(final int count, final Filterable... filter) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                List<PhysicalCard> physicalCardList = game.getGameState().getStackedCards(physicalCard);
                if (filter.length == 1 && filter[0] == Filters.any)
                    return physicalCardList.size() >= count;
                return (Filters.filter(physicalCardList, game, Filters.and(filter, activeSide)).size() >= count);
            }
        };
    }

    public static Filter not(final Filterable... filters) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return !Filters.and(filters).accepts(game, physicalCard);
            }
        };
    }

    public static Filter sameCard(final PhysicalCard card) {
        final int cardId = card.getCardId();
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return (physicalCard.getCardId() == cardId);
            }
        };
    }

    public static Filter in(final Collection<? extends PhysicalCard> cards) {
        final Set<Integer> cardIds = new HashSet<>();
        for (PhysicalCard card : cards)
            cardIds.add(card.getCardId());
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return cardIds.contains(physicalCard.getCardId());
            }
        };
    }

    public static Filter zone(final Zone zone) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getZone() == zone;
            }
        };
    }

    public static Filter hasWounds(final int wounds) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getGameState().getWounds(physicalCard) >= wounds;
            }
        };
    }

    public static final Filter unwounded = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return game.getGameState().getWounds(physicalCard) == 0;
        }
    };

    public static final Filter wounded = Filters.hasWounds(1);

    public static Filter name(final String name) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return name != null && physicalCard.getBlueprint().getSanitizedTitle() != null && physicalCard.getBlueprint().getSanitizedTitle().equals(Names.SanitizeName(name));
            }
        };
    }

    private static Filter type(final CardType cardType) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return (physicalCard.getBlueprint().getCardType() == cardType)
                        || game.getModifiersQuerying().isAdditionalCardType(game, physicalCard, cardType);
            }
        };
    }

    public static Filter attachedTo(final Filterable... filters) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getAttachedTo() != null && Filters.and(filters).accepts(game, physicalCard.getAttachedTo());
            }
        };
    }

    public static Filter stackedOn(final Filterable... filters) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getStackedOn() != null && Filters.and(filters).accepts(game, physicalCard.getStackedOn());
            }
        };
    }

    public static Filter siteControlledByShadowPlayer(final String fellowshipPlayer) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getBlueprint().getCardType() == CardType.SITE && physicalCard.getCardController() != null && !physicalCard.getCardController().equals(fellowshipPlayer);
            }
        };
    }

    public static Filter siteControlledByAnyPlayer = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return physicalCard.getBlueprint().getCardType() == CardType.SITE && physicalCard.getCardController() != null;
        }
    };

    public static Filter siteControlled(final String playerId) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getBlueprint().getCardType() == CardType.SITE && playerId.equals(physicalCard.getCardController());
            }
        };
    }

    public static Filter uncontrolledSite = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            return physicalCard.getBlueprint().getCardType() == CardType.SITE && physicalCard.getCardController() == null;
        }
    };


    private static Filter culture(final Culture culture) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return (physicalCard.getBlueprint().getCulture() == culture);
            }
        };
    }

    private static Filter keyword(final Keyword keyword) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().hasKeyword(game, physicalCard, keyword);
            }
        };
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

    private static Filter changeToFilter(Filterable filter) {
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

    public static Filter activeSide = new Filter() {
        @Override
        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
            boolean shadow = physicalCard.getBlueprint().getSide() == Side.SHADOW;
            if (shadow)
                return !physicalCard.getOwner().equals(game.getGameState().getCurrentPlayerId());
            else
                return physicalCard.getOwner().equals(game.getGameState().getCurrentPlayerId());
        }
    };

    private static Filter andInternal(final Filter... filters) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                for (Filter filter : filters) {
                    if (!filter.accepts(game, physicalCard))
                        return false;
                }
                return true;
            }
        };
    }

    public static Filter and(final Filterable[] filters1, final Filterable... filters2) {
        final Filter[] newFilters1 = convertToFilters(filters1);
        final Filter[] newFilters2 = convertToFilters(filters2);
        if (newFilters1.length == 1 && newFilters2.length == 0)
            return newFilters1[0];
        if (newFilters1.length == 0 && newFilters2.length == 1)
            return newFilters2[0];
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                for (Filter filter : newFilters1) {
                    if (!filter.accepts(game, physicalCard))
                        return false;
                }
                for (Filter filter : newFilters2) {
                    if (!filter.accepts(game, physicalCard))
                        return false;
                }
                return true;
            }
        };
    }

    private static Filter orInternal(final Filter... filters) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                for (Filter filter : filters) {
                    if (filter.accepts(game, physicalCard))
                        return true;
                }
                return false;
            }
        };
    }

    public static final Filter ringBoundCompanion = Filters.and(CardType.COMPANION, Keyword.RING_BOUND);
    public static final Filter unboundCompanion = Filters.and(CardType.COMPANION, Filters.not(Keyword.RING_BOUND));
    public static final Filter roamingMinion = Filters.and(CardType.MINION, Keyword.ROAMING);
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
