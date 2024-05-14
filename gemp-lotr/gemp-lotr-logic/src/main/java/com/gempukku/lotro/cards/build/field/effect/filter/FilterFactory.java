package com.gempukku.lotro.cards.build.field.effect.filter;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.SingleMemoryEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.CharacterLostSkirmishResult;

import java.util.*;

public class FilterFactory {
    private final Map<String, FilterableSource> simpleFilters = new HashMap<>();
    private final Map<String, FilterableSourceProducer> parameterFilters = new HashMap<>();

    public FilterFactory() {
        for (CardType value : CardType.values())
            appendFilter(value);
        for (Keyword value : Keyword.values())
            appendFilter(value);
        for (PossessionClass value : PossessionClass.values())
            appendFilter(value);
        for (Race value : Race.values())
            appendFilter(value);

        simpleFilters.put("allyincurrentregion", (actionContext) -> Filters.isAllyInCurrentRegion());
        simpleFilters.put("another", (actionContext) -> Filters.not(actionContext.getSource()));
        simpleFilters.put("other", (actionContext) -> Filters.not(actionContext.getSource()));
        simpleFilters.put("any", (actionContext) -> Filters.any);
        simpleFilters.put("attachedtoinsameregion",
                actionContext -> {
                    final PhysicalCard attachedTo = actionContext.getSource().getAttachedTo();
                    return Filters.region(GameUtils.getRegion(attachedTo.getSiteNumber()));
                });
        simpleFilters.put("bearer", (actionContext -> Filters.hasAttached(actionContext.getSource())));
        simpleFilters.put("character", (actionContext) -> Filters.character);
        simpleFilters.put("canbediscarded", (actionContext -> Filters.canBeDiscarded(actionContext.getPerformingPlayer(), actionContext.getSource())));
        simpleFilters.put("canbereturnedtohand", (actionContext -> Filters.canBeReturnedToHand(actionContext.getSource())));
        simpleFilters.put("canexert", (actionContext -> Filters.canExert(actionContext.getSource())));
        simpleFilters.put("controlledbyotherplayer",
                (actionContext -> Filters.siteControlledByOtherPlayer(actionContext.getPerformingPlayer())));
        simpleFilters.put("controlledsite",
                (actionContext -> Filters.siteControlled(actionContext.getPerformingPlayer())));
        simpleFilters.put("culturewithtokens",
                (actionContext -> {
                    final Set<Culture> cultureTokens = new HashSet<>();
                    LotroGame game = actionContext.getGame();
                    for (PhysicalCard physicalCard : Filters.filterActive(game, Filters.hasAnyCultureTokens(1))) {
                        Map<Token, Integer> tokens = game.getGameState().getTokens(physicalCard);
                        for (Map.Entry<Token, Integer> tokenIntegerEntry : tokens.entrySet()) {
                            if (tokenIntegerEntry.getValue() > 0) {
                                Culture culture = tokenIntegerEntry.getKey().getCulture();
                                if (culture != null)
                                    cultureTokens.add(culture);
                            }
                        }
                    }

                    return (Filter) (game1, physicalCard) -> {
                        Culture culture = physicalCard.getBlueprint().getCulture();
                        return cultureTokens.contains(culture);
                    };
                }));
        simpleFilters.put("currentsite",
                (actionContext) -> Filters.currentSite);
        simpleFilters.put("currentsitenumber",
                (actionContext -> Filters.siteNumber(actionContext.getGame().getGameState().getCurrentSiteNumber())));
        simpleFilters.put("exhausted", (actionContext) -> Filters.exhausted);
        simpleFilters.put("gettingdiscarded",
                (actionContext -> {
                    Effect effect = actionContext.getEffect();
                    if (effect instanceof DiscardCardsFromPlayEffect aboutToDiscardFromPlay) {
                        Collection<PhysicalCard> discardedCards = aboutToDiscardFromPlay.getAffectedCardsMinusPrevented(actionContext.getGame());
                        return Filters.in(discardedCards);
                    }
                    return Filters.none;
                }));
        simpleFilters.put("idinstored",
                (actionContext ->
                        (Filter) (game, physicalCard) -> {
                            final String whileInZoneData = (String) actionContext.getSource().getWhileInZoneData();
                            if (whileInZoneData == null)
                                return false;
                            for (String cardId : whileInZoneData.split(",")) {
                                if (cardId.equals(String.valueOf(physicalCard.getCardId())))
                                    return true;
                            }

                            return false;
                        }));
        simpleFilters.put("infierceskirmish", (actionContext) -> Filters.inFierceSkirmish);
        simpleFilters.put("inplay", (actionContext) -> Filters.inPlay);
        simpleFilters.put("insameregion",
                actionContext -> Filters.region(GameUtils.getRegion(actionContext.getSource().getSiteNumber())));
        simpleFilters.put("inskirmish", (actionContext) -> Filters.inSkirmish);
        simpleFilters.put("item", (actionContext) -> Filters.item);
        simpleFilters.put("mounted", (actionContext) -> Filters.mounted);
        simpleFilters.put("notassignedtoskirmish",
                (actionContext) -> Filters.notAssignedToSkirmish);
        simpleFilters.put("playable", (actionContext) -> Filters.playable());
        simpleFilters.put("ringbearer", (actionContext) -> Filters.ringBearer);
        simpleFilters.put("ring-bearer", (actionContext) -> Filters.ringBearer);
        simpleFilters.put("ringbound",
                (actionContext) -> Filters.ringBoundCompanion);
        simpleFilters.put("ring-bound",
                (actionContext) -> Filters.ringBoundCompanion);
        simpleFilters.put("self", ActionContext::getSource);
        simpleFilters.put("sitehassitenumber",
                (actionContext) -> Filters.siteHasSiteNumber);
        simpleFilters.put("siteincurrentregion",
                (actionContext) -> Filters.siteInCurrentRegion);
        simpleFilters.put("skirmishloser",
                (actionContext) -> {
                    final CharacterLostSkirmishResult lostSkirmish = (CharacterLostSkirmishResult) actionContext.getEffectResult();
                    return lostSkirmish.getLoser();
                });
        simpleFilters.put("unbound",
                (actionContext) -> Filters.unboundCompanion);
        simpleFilters.put("uncontrolled",
                (actionContext) -> Filters.uncontrolledSite);
        simpleFilters.put("unique", (actionContext) -> Filters.unique);
        simpleFilters.put("unwounded", (actionContext) -> Filters.unwounded);
        simpleFilters.put("weapon", (actionContext) -> Filters.weapon);
        simpleFilters.put("wounded", (actionContext) -> Filters.wounded);
        simpleFilters.put("your", (actionContext) -> Filters.owner(actionContext.getPerformingPlayer()));

        parameterFilters.put("allyhome",
                (parameter, environment) -> {
                    final String[] parameterSplit = parameter.split(",");
                    final SitesBlock sitesBlock = SitesBlock.findBlock(parameterSplit[0]);
                    int number = Integer.parseInt(parameterSplit[1]);
                    return (actionContext) -> Filters.isAllyHome(number, sitesBlock);
                });
        parameterFilters.put("allyinregion",
                (parameter, environment) -> {
                    final String[] parameterSplit = parameter.split(",");
                    final SitesBlock sitesBlock = Enum.valueOf(SitesBlock.class, parameterSplit[0].toUpperCase().replace('_', ' '));
                    int number = Integer.parseInt(parameterSplit[1]);
                    return (actionContext) -> Filters.isAllyInRegion(number, sitesBlock);
                });
        parameterFilters.put("and",
                (parameter, environment) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterableSource[] filterables = new FilterableSource[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = environment.getFilterFactory().generateFilter(filters[i], environment);
                    return (actionContext) -> {
                        Filterable[] filters1 = new Filterable[filterables.length];
                        for (int i = 0; i < filterables.length; i++)
                            filters1[i] = filterables[i].getFilterable(actionContext);

                        return Filters.and(filters1);
                    };
                });
        parameterFilters.put("assignabletoskirmishagainst",
                (parameter, environment) -> {
                    final FilterableSource againstFilterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Side side = GameUtils.getSide(actionContext.getGame(), actionContext.getPerformingPlayer());
                        return Filters.assignableToSkirmishAgainst(side, againstFilterableSource.getFilterable(actionContext));
                    };
                });
        parameterFilters.put("assignedtoskirmish",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.assignedToSkirmishAgainst(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("attachedto",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.attachedTo(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("culture", (parameter, environment) -> {
            final Culture culture = Culture.findCulture(parameter);
            if (culture == null)
                throw new InvalidCardDefinitionException("Unable to find culture for: " + parameter);

            return (actionContext) -> culture;
        });
        parameterFilters.put("culturefrommemory", ((parameter, environment) -> actionContext -> {
            Set<Culture> cultures = new HashSet<>();
            for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(parameter)) {
                cultures.add(physicalCard.getBlueprint().getCulture());
            }
            return Filters.or(cultures.toArray(new Culture[0]));
        }));
        parameterFilters.put("hasattached",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.hasAttached(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasattachedcount",
                (parameter, environment) -> {
                    String[] parameterSplit = parameter.split(",", 2);
                    int count = Integer.parseInt(parameterSplit[0]);
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameterSplit[1], environment);
                    return (actionContext) -> Filters.hasAttached(count, filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasstacked",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.hasStacked(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasstackedcount",
                (parameter, environment) -> {
                    String[] parameterSplit = parameter.split(",", 2);
                    int count = Integer.parseInt(parameterSplit[0]);
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameterSplit[1], environment);
                    return (actionContext) -> Filters.hasStacked(count, filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasanytokens", (parameter, environment) -> {
            int count = Integer.parseInt(parameter != null ? parameter : "1");
            return (actionContext) -> Filters.hasAnyCultureTokens(count);
        });
        parameterFilters.put("hastoken", (parameter, environment) -> {
            final Culture culture = Culture.findCulture(parameter);
            if (culture == null)
                throw new InvalidCardDefinitionException("Unable to find culture for: " + parameter);
            final Token token = Token.findTokenForCulture(culture);
            if (token == null)
                throw new InvalidCardDefinitionException("Unable to find token for culture: " + parameter);

            return (actionContext) -> Filters.hasToken(token);
        });
        parameterFilters.put("hastokencount", (parameter, environment) -> {
            String[] parameterSplit = parameter.split(",", 2);
            int count = Integer.parseInt(parameterSplit[0]);
            final Culture culture = Culture.findCulture(parameterSplit[1]);
            if (culture == null)
                throw new InvalidCardDefinitionException("Unable to find culture for: " + parameter);
            final Token token = Token.findTokenForCulture(culture);
            if (token == null)
                throw new InvalidCardDefinitionException("Unable to find token for culture: " + parameter);

            return (actionContext) -> Filters.hasToken(token, count);
        });
        parameterFilters.put("inskirmishagainst",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.inSkirmishAgainst(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("loweststrength",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return Filters.and(
                                sourceFilterable, Filters.strengthEqual(
                                        new SingleMemoryEvaluator(
                                                new Evaluator() {
                                                    @Override
                                                    public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                                        int minStrength = Integer.MAX_VALUE;
                                                        for (PhysicalCard card : Filters.filterActive(game, sourceFilterable))
                                                            minStrength = Math.min(minStrength, game.getModifiersQuerying().getStrength(game, card));
                                                        return minStrength;
                                                    }
                                                }
                                        )
                                )
                        );
                    };
                });
        parameterFilters.put("maxtwilight",
                (parameter, environment) -> {
                    if (parameter.startsWith("memory(") && parameter.endsWith(")")) {
                        String memory = parameter.substring(parameter.indexOf("(") + 1, parameter.lastIndexOf(")"));
                        return actionContext -> {
                            try {
                                final int value = Integer.parseInt(actionContext.getValueFromMemory(memory));
                                return Filters.maxPrintedTwilightCost(value);
                            } catch (IllegalArgumentException ex) {
                                return Filters.maxPrintedTwilightCost(100);
                            }
                        };
                    } else {
                        final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                        return actionContext -> {
                            final int value = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                            return Filters.maxPrintedTwilightCost(value);
                        };
                    }
                });
        parameterFilters.put("memory",
                (parameter, environment) -> (actionContext) -> Filters.in(actionContext.getCardsFromMemory(parameter)));
        parameterFilters.put("mintwilight",
                (parameter, environment) -> {
                    if (parameter.startsWith("memory(") && parameter.endsWith(")")) {
                        String memory = parameter.substring(parameter.indexOf("(") + 1, parameter.lastIndexOf(")"));
                        return actionContext -> {
                            try {
                                final int value = Integer.parseInt(actionContext.getValueFromMemory(memory));
                                return Filters.minPrintedTwilightCost(value);
                            } catch (IllegalArgumentException ex) {
                                return Filters.minPrintedTwilightCost(0);
                            }
                        };
                    } else {
                        final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                        return actionContext -> {
                            final int value = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                            return Filters.minPrintedTwilightCost(value);
                        };
                    }
                });

        parameterFilters.put("name",
                (parameter, environment) -> {
                    String name = Names.SanitizeName(Sanitize(parameter));
                    return (actionContext) -> (Filter)
                            (game, physicalCard) -> name != null
                                    && physicalCard.getBlueprint().getSanitizedTitle() != null
                                    && name.equals(Sanitize(physicalCard.getBlueprint().getSanitizedTitle()));
                });
        parameterFilters.put("namefrommemory",
                (parameter, environment) -> actionContext -> {
                    Set<String> titles = new HashSet<>();
                    for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(parameter))
                        titles.add(physicalCard.getBlueprint().getSanitizedTitle());
                    return (Filter) (game, physicalCard) -> titles.contains(physicalCard.getBlueprint().getSanitizedTitle());
                });
        parameterFilters.put("nameinstackedon",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return (Filter) (game, physicalCard) -> {
                            for (PhysicalCard cardWithStack : Filters.filterActive(game, sourceFilterable)) {
                                for (PhysicalCard stackedCard : game.getGameState().getStackedCards(cardWithStack)) {
                                    if (stackedCard.getBlueprint().getSanitizedTitle().equals(physicalCard.getBlueprint().getSanitizedTitle()))
                                        return true;
                                }
                            }
                            return false;
                        };
                    };
                });
        parameterFilters.put("not",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.not(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("or",
                (parameter, environment) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterableSource[] filterables = new FilterableSource[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = environment.getFilterFactory().generateFilter(filters[i], environment);
                    return (actionContext) -> {
                        Filterable[] filters1 = new Filterable[filterables.length];
                        for (int i = 0; i < filterables.length; i++)
                            filters1[i] = filterables[i].getFilterable(actionContext);

                        return Filters.or(filters1);
                    };
                });
        parameterFilters.put("printedtwilightcostfrommemory",
                (parameter, environment) -> actionContext -> {
                    PhysicalCard card = actionContext.getCardFromMemory(parameter);
                    if (card == null)
                        return Filters.none;

                    int memoryPrintedTwilightCost = card.getBlueprint().getTwilightCost();
                    return Filters.printedTwilightCost(memoryPrintedTwilightCost);
                });
        parameterFilters.put("race",
                (parameter, environment) -> {
                    if (parameter.equals("stored")) {
                        return actionContext -> (Filter) (game, physicalCard) -> {
                            final String value = (String) actionContext.getSource().getWhileInZoneData();
                            if (value == null)
                                return false;
                            else
                                return Race.valueOf(value) == physicalCard.getBlueprint().getRace();
                        };
                    } else if (parameter.equals("cannotspot")) {
                        return actionContext -> (Filter) (game, physicalCard) -> {
                            final Race race = physicalCard.getBlueprint().getRace();
                            if (race != null)
                                return !Filters.canSpot(game, race);
                            return false;
                        };
                    }
                    throw new InvalidCardDefinitionException("Unknown race definition in filter: " + parameter
                            + ".  Do not use race() for races; instead just list the race by itself.");
                });
        parameterFilters.put("regionnumber",
                (parameter, environment) -> {
                    int min, max;
                    if (parameter.contains("-")) {
                        final String[] split = parameter.split("-", 2);
                        min = Integer.parseInt(split[0]);
                        max = Integer.parseInt(split[1]);
                    } else {
                        min = max = Integer.parseInt(parameter);
                    }

                    if (min < 0 || max > 3 || max < min)
                        throw new InvalidCardDefinitionException("Region number definition is invalid: " + parameter);

                    return (actionContext) -> Filters.regionNumberBetweenInclusive(min, max);
                });
        parameterFilters.put("resistancelessthan",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);

                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.maxResistance(amount - 1);
                    };
                });
        parameterFilters.put("resistancemorethan",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.minResistance(amount + 1);
                    };
                });
        parameterFilters.put("side", (parameter, environment) -> {
            final Side side = Side.Parse(parameter);
            if (side == null)
                throw new InvalidCardDefinitionException("Unable to find side for: " + parameter);

            return (actionContext) -> side;
        });
        parameterFilters.put("signet", (parameter, environment) -> {
            final Signet signet = Signet.findSignet(parameter);
            if (signet == null)
                throw new InvalidCardDefinitionException("Unable to find signet for: " + parameter);

            return (actionContext) -> signet;
        });
        parameterFilters.put("siteblock",
                (parameter, environment) -> {
                    final SitesBlock sitesBlock = SitesBlock.findBlock(parameter);
                    return (actionContext) -> Filters.siteBlock(sitesBlock);
                });
        parameterFilters.put("sitenumber",
                (parameter, environment) -> {
                    int min, max;
                    if (parameter.contains("-")) {
                        final String[] split = parameter.split("-", 2);
                        min = Integer.parseInt(split[0]);
                        max = Integer.parseInt(split[1]);
                    } else {
                        min = max = Integer.parseInt(parameter);
                    }

                    return (actionContext) -> Filters.siteNumberBetweenInclusive(min, max);
                });
        parameterFilters.put("strengthlessthanfilter",
                (parameter, environment) -> {
                    FilterableSource filter = environment.getFilterFactory().createFilter(parameter, environment);
                    return (actionContext) -> {
                        int strength = 0;
                        LotroGame game = actionContext.getGame();
                        for (PhysicalCard physicalCard : Filters.filterActive(game, filter.getFilterable(actionContext))) {
                            strength += game.getModifiersQuerying().getStrength(game, physicalCard);
                        }
                        return Filters.lessStrengthThan(strength);
                    };
                });
        parameterFilters.put("strengthlessthan",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);

                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.lessStrengthThan(amount);
                    };
                });
        parameterFilters.put("strengthmorethan",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);

                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.moreStrengthThan(amount);
                    };
                });
        parameterFilters.put("title", parameterFilters.get("name"));
        parameterFilters.put("vitalitylessthan",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.lessVitalityThan(amount);
                    };
                });
        parameterFilters.put("vitalitymorethan",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.moreVitalityThan(amount);
                    };
                });
        parameterFilters.put("zone",
                (parameter, environment) -> {
                    // Fix for faulty "Sanitize" method, which removes spaces and underscores, therefore making it
                    // impossible to match ADVENTURE_PATH Zone
                    if (parameter.equals("adventurepath")) parameter = "adventure path";
                    final Zone zone = FieldUtils.getEnum(Zone.class, parameter, "parameter");
                    return actionContext -> zone;
                });
    }

    private void appendFilter(Filterable value) {
        final String filterName = Sanitize(value.toString());
        final String optionalFilterName = value.toString().toLowerCase().replace("_", "-");
        if (simpleFilters.containsKey(filterName))
            throw new RuntimeException("Duplicate filter name: " + filterName);
        simpleFilters.put(filterName, (actionContext) -> value);
        if (!optionalFilterName.equals(filterName))
            simpleFilters.put(optionalFilterName, (actionContext -> value));
    }

    public FilterableSource generateFilter(String value, CardGenerationEnvironment environment) throws
            InvalidCardDefinitionException {
        if (value == null)
            throw new InvalidCardDefinitionException("Filter not specified");
        String[] filterStrings = splitIntoFilters(value);
        if (filterStrings.length == 0)
            return (actionContext) -> Filters.any;
        if (filterStrings.length == 1)
            return createFilter(filterStrings[0], environment);

        FilterableSource[] filters = new FilterableSource[filterStrings.length];
        for (int i = 0; i < filters.length; i++)
            filters[i] = createFilter(filterStrings[i], environment);
        return (actionContext) -> {
            Filterable[] filter = new Filterable[filters.length];
            for (int i = 0; i < filter.length; i++) {
                filter[i] = filters[i].getFilterable(actionContext);
            }

            return Filters.and(filter);
        };
    }

    private String[] splitIntoFilters(String value) throws InvalidCardDefinitionException {
        List<String> parts = new LinkedList<>();
        final char[] chars = value.toCharArray();

        int depth = 0;
        StringBuilder sb = new StringBuilder();
        for (char ch : chars) {
            if (depth > 0) {
                if (ch == ')')
                    depth--;
                if (ch == '(')
                    depth++;
                sb.append(ch);
            } else {
                if (ch == ',') {
                    parts.add(sb.toString());
                    sb = new StringBuilder();
                } else {
                    if (ch == ')')
                        throw new InvalidCardDefinitionException("Invalid filter definition: " + value);
                    if (ch == '(')
                        depth++;
                    sb.append(ch);
                }
            }
        }

        if (depth != 0)
            throw new InvalidCardDefinitionException("Not matching number of opening and closing brackets: " + value);

        parts.add(sb.toString());

        return parts.toArray(new String[0]);
    }

    private FilterableSource createFilter(String filterString, CardGenerationEnvironment environment) throws
            InvalidCardDefinitionException {
        if (filterString.contains("(") && filterString.endsWith(")")) {
            String filterName = filterString.substring(0, filterString.indexOf("("));
            String filterParameter = filterString.substring(filterString.indexOf("(") + 1, filterString.lastIndexOf(")"));
            return lookupFilter(Sanitize(filterName), Sanitize(filterParameter), environment);
        }
        return lookupFilter(Sanitize(filterString), null, environment);
    }


    private FilterableSource lookupFilter(String name, String parameter, CardGenerationEnvironment environment) throws
            InvalidCardDefinitionException {
        if (parameter == null) {
            FilterableSource result = simpleFilters.get(Sanitize(name));
            if (result != null)
                return result;
        }

        final FilterableSourceProducer filterableSourceProducer = parameterFilters.get(Sanitize(name));
        if (filterableSourceProducer == null)
            throw new InvalidCardDefinitionException("Unable to find filter: " + name);

        return filterableSourceProducer.createFilterableSource(parameter, environment);
    }

    public static String Sanitize(String input) {
        return input
                .toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }
}
