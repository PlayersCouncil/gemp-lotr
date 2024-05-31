package com.gempukku.lotro.cards.build.field.effect.appender.resolver;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.modifiers.evaluator.*;
import com.gempukku.lotro.logic.timing.RuleUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ValueResolver {
    public static ValueSource resolveEvaluator(Object value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveEvaluator(value, null, environment);
    }

    public static ValueSource resolveEvaluator(Object value, Integer defaultValue, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        if (value == null && defaultValue == null)
            throw new InvalidCardDefinitionException("Value not defined");
        if (value == null)
            return actionContext -> new ConstantEvaluator(defaultValue);
        if (value instanceof Number)
            return actionContext -> new ConstantEvaluator(((Number) value).intValue());
        if (value instanceof String stringValue) {
            if (stringValue.equalsIgnoreCase("twilightCount")) {
                return actionContext -> (Evaluator) (game, cardAffected) -> game.getGameState().getTwilightPool();
            } else if (stringValue.equalsIgnoreCase("burdenCount")) {
                return actionContext -> (Evaluator) (game, cardAffected) -> game.getGameState().getBurdens();
            } else if (stringValue.equalsIgnoreCase("moveCount")) {
                return actionContext -> (Evaluator) (game, cardAffected) -> game.getGameState().getMoveCount();
            } else if (stringValue.contains("-")) {
                final String[] split = stringValue.split("-", 2);
                final int min = Integer.parseInt(split[0]);
                final int max = Integer.parseInt(split[1]);
                if (min > max || min < 0 || max < 1)
                    throw new InvalidCardDefinitionException("Unable to resolve count: " + value);
                return actionContext -> new RangeEvaluator(min, max);
            } else if (stringValue.equalsIgnoreCase("any")) {
                return actionContext -> new RangeEvaluator(0, Integer.MAX_VALUE);
            } else if (stringValue.startsWith("memory(") && stringValue.endsWith(")")) {
                String memory = stringValue.substring("memory(".length(), stringValue.length() - 1);
                return actionContext -> new ConstantEvaluator(Integer.parseInt(actionContext.getValueFromMemory(memory, "0")));
            } else {
                try {
                    int v = Integer.parseInt(stringValue);
                    return actionContext -> new ConstantEvaluator(v);
                } catch (NumberFormatException exp) {
                    throw new InvalidCardDefinitionException("Can't parse value as number: " + stringValue);
                }
            }
        }
        if (value instanceof JSONObject object) {
            final String type = FieldUtils.getString(object.get("type"), "type");
            /**
              * Math operations
              */
            if (type.equalsIgnoreCase("max")) {
                FieldUtils.validateAllowedFields(object, "firstNumber", "secondNumber");
                ValueSource first = resolveEvaluator(object.get("firstNumber"), environment);
                ValueSource second = resolveEvaluator(object.get("secondNumber"), environment);

                return actionContext -> {
                    Evaluator evaluator1 = first.getEvaluator(actionContext);
                    Evaluator evaluator2 = second.getEvaluator(actionContext);
                    return (game, cardAffected) ->
                            Math.max(
                                    evaluator1.evaluateExpression(game, null),
                                    evaluator2.evaluateExpression(game, null)
                            );
                };
            } else if (type.equalsIgnoreCase("min")) {
                FieldUtils.validateAllowedFields(object, "firstNumber", "secondNumber");
                ValueSource first = resolveEvaluator(object.get("firstNumber"), environment);
                ValueSource second = resolveEvaluator(object.get("secondNumber"), environment);

                return actionContext -> {
                    Evaluator evaluator1 = first.getEvaluator(actionContext);
                    Evaluator evaluator2 = second.getEvaluator(actionContext);
                    return (game, cardAffected) ->
                            Math.min(
                                    evaluator1.evaluateExpression(game, null),
                                    evaluator2.evaluateExpression(game, null)
                            );
                };
            } else if (type.equalsIgnoreCase("range")) {
                FieldUtils.validateAllowedFields(object, "from", "to");
                ValueSource fromValue = resolveEvaluator(object.get("from"), environment);
                ValueSource toValue = resolveEvaluator(object.get("to"), environment);
                return actionContext -> {
                    Evaluator fromEvaluator = fromValue.getEvaluator(actionContext);
                    Evaluator toEvaluator = toValue.getEvaluator(actionContext);
                    return new RangeEvaluator(fromEvaluator, toEvaluator);
                };
            } else if (type.equalsIgnoreCase("subtract")) {
                FieldUtils.validateAllowedFields(object, "firstNumber", "secondNumber");
                final ValueSource firstNumber = ValueResolver.resolveEvaluator(object.get("firstNumber"), 0, environment);
                final ValueSource secondNumber = ValueResolver.resolveEvaluator(object.get("secondNumber"), 0, environment);
                return actionContext -> {
                    Evaluator evaluator1 = firstNumber.getEvaluator(actionContext);
                    Evaluator evaluator2 = secondNumber.getEvaluator(actionContext);
                    return (Evaluator) (game, cardAffected) -> {
                        final int first = evaluator1.evaluateExpression(game, null);
                        final int second = evaluator2.evaluateExpression(game, null);
                        return first - second;
                    };
                };
            } else if (type.equalsIgnoreCase("sum")) {
                FieldUtils.validateAllowedFields(object, "source");
                final JSONArray sourceArray = FieldUtils.getArray(object.get("source"), "source");
                ValueSource[] sources = new ValueSource[sourceArray.size()];
                for (int i = 0; i < sources.length; i++)
                    sources[i] = ValueResolver.resolveEvaluator(sourceArray.get(i), 0, environment);

                return actionContext -> {
                    Evaluator[] evaluators = new Evaluator[sources.length];
                    for (int i = 0; i < sources.length; i++)
                        evaluators[i] = sources[i].getEvaluator(actionContext);

                    return (game, cardAffected) -> {
                        int sum = 0;
                        for (Evaluator evaluator : evaluators)
                            sum += evaluator.evaluateExpression(game, cardAffected);

                        return sum;
                    };
                };
            }

            /**
             * Stat operations
             */

            else if (type.equalsIgnoreCase("forEachStrength")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");

                final FilterableSource strengthSource = environment.getFilterFactory().generateFilter(filter, environment);

                if (filter.equals("any")) {
                    return new SmartValueSource(environment, object,
                            actionContext -> (game, cardAffected) -> game.getModifiersQuerying().getStrength(game, cardAffected));

                } else {
                    return new SmartValueSource(environment, object,
                            actionContext -> {
                                final Filterable filterable = strengthSource.getFilterable(actionContext);
                                return (game, cardAffected) -> {
                                    int strength = 0;
                                    for (PhysicalCard physicalCard : Filters.filterActive(game, filterable)) {
                                        strength += game.getModifiersQuerying().getStrength(game, physicalCard);
                                    }

                                    return strength;
                                };
                            });
                }
            }  else if (type.equalsIgnoreCase("ForEachTwilightCost")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");

                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

                return new SmartValueSource(environment, object,
                        actionContext -> {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            return (game, cardAffected) -> {
                                int result = 0;
                                for (PhysicalCard physicalCard : Filters.filterActive(game, filterable)) {
                                    result += game.getModifiersQuerying().getTwilightCostToPlay(game, physicalCard, null, 0, false);
                                }
                                return result;
                            };
                        });
            } else if (type.equalsIgnoreCase("forEachResistance")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");

                final FilterableSource resistanceSource = environment.getFilterFactory().generateFilter(filter, environment);

                if (filter.equals("any")) {
                    return new SmartValueSource(environment, object,
                            actionContext -> (game, cardAffected) -> game.getModifiersQuerying().getResistance(game, cardAffected));
                } else {
                    return new SmartValueSource(environment, object,
                            actionContext -> {
                                final Filterable filterable = resistanceSource.getFilterable(actionContext);
                                return (game, cardAffected) -> {
                                    int resistance = 0;
                                    for (PhysicalCard physicalCard : Filters.filterActive(game, filterable)) {
                                        resistance += game.getModifiersQuerying().getResistance(game, physicalCard);
                                    }

                                    return resistance;
                                };
                            });
                }
            } else if (type.equalsIgnoreCase("forEachVitality")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");

                final FilterableSource vitalitySource = environment.getFilterFactory().generateFilter(filter, environment);

                if (filter.equals("any")) {
                    return new SmartValueSource(environment, object,
                            actionContext -> (game, cardAffected) -> game.getModifiersQuerying().getVitality(game, cardAffected));
                } else {
                    return new SmartValueSource(environment, object,
                            actionContext -> {
                                final Filterable filterable = vitalitySource.getFilterable(actionContext);
                                return (game, cardAffected) -> {
                                    int resistance = 0;
                                    for (PhysicalCard physicalCard : Filters.filterActive(game, filterable)) {
                                        resistance += game.getModifiersQuerying().getVitality(game, physicalCard);
                                    }

                                    return resistance;
                                };
                            });
                }
            }

            /**
             * Other Values
             */

            else if (type.equalsIgnoreCase("archeryTotal")) {
                FieldUtils.validateAllowedFields(object, "side", "over", "limit", "multiplier", "divider");
                final Side side = FieldUtils.getSide(object.get("side"), "side");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> RuleUtils.calculateArcheryTotal(game, side));
            } else if (type.equalsIgnoreCase("cardAffectedLimitPerPhase")) {
                FieldUtils.validateAllowedFields(object, "limit", "source", "prefix", "multiplier");
                final int limit = FieldUtils.getInteger(object.get("limit"), "limit");
                final String prefix = FieldUtils.getString(object.get("prefix"), "prefix", "");
                final int multiplier = FieldUtils.getInteger(object.get("multiplier"), "multiplier", 1);
                final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("source"), 0, environment);
                return (actionContext ->
                        new MultiplyEvaluator(multiplier, new CardAffectedPhaseLimitEvaluator(
                                actionContext.getSource(),
                                actionContext.getGame().getGameState().getCurrentPhase(),
                                limit, prefix, valueSource.getEvaluator(actionContext))));
            } else if (type.equalsIgnoreCase("cardphaselimit")) {
                FieldUtils.validateAllowedFields(object, "limit", "amount");
                ValueSource limitSource = resolveEvaluator(object.get("limit"), 0, environment);
                ValueSource valueSource = resolveEvaluator(object.get("amount"), 0, environment);
                return (actionContext) -> new CardPhaseLimitEvaluator(actionContext.getSource(),
                        actionContext.getGame().getGameState().getCurrentPhase(), limitSource.getEvaluator(actionContext),
                        valueSource.getEvaluator(actionContext));
            } else if (type.equalsIgnoreCase("conditional")) {
                FieldUtils.validateAllowedFields(object, "requires", "true", "false");
                final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
                final Requirement[] conditions = environment.getRequirementFactory().getRequirements(conditionArray, environment);
                ValueSource trueValue = resolveEvaluator(object.get("true"), environment);
                ValueSource falseValue = resolveEvaluator(object.get("false"), environment);
                return (actionContext) -> (Evaluator) (game, cardAffected) -> {
                    for (Requirement condition : conditions) {
                        if (!condition.accepts(actionContext))
                            return falseValue.getEvaluator(actionContext).evaluateExpression(game, cardAffected);
                    }
                    return trueValue.getEvaluator(actionContext).evaluateExpression(game, cardAffected);
                };
            } else if (type.equalsIgnoreCase("currentSiteNumber")) {
                FieldUtils.validateAllowedFields(object, "over", "limit", "multiplier", "divider");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> game.getGameState().getCurrentSiteNumber());
            } else if (type.equalsIgnoreCase("fromMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "limit", "over", "multiplier", "divider");
                String memory = FieldUtils.getString(object.get("memory"), "memory");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> Integer.parseInt(actionContext.getValueFromMemory(memory)));
            } else if (type.equalsIgnoreCase("forEachBurden")) {
                FieldUtils.validateAllowedFields(object, "over", "limit", "multiplier", "divider");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> game.getGameState().getBurdens());
            } else if (type.equalsIgnoreCase("forEachCulture")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return new SmartValueSource(environment, object,
                        actionContext -> {
                            Filterable filterable = filterableSource.getFilterable(actionContext);
                            return (game, cardAffected) -> GameUtils.getSpottableCulturesCount(game, filterable);
                        });
            } else if (type.equalsIgnoreCase("forEachFPCulture")) {
                FieldUtils.validateAllowedFields(object, "over", "limit", "multiplier", "divider");
                return new SmartValueSource(environment, object,
                        actionContext -> {
                            String performingPlayer = actionContext.getPerformingPlayer();
                            return (game, cardAffected) -> GameUtils.getSpottableFPCulturesCount(game, performingPlayer);
                        });
            } else if (type.equalsIgnoreCase("forEachShadowCulture")) {
                FieldUtils.validateAllowedFields(object, "over", "limit", "multiplier", "divider");
                return new SmartValueSource(environment, object,
                        actionContext -> {
                            String performingPlayer = actionContext.getPerformingPlayer();
                            return (game, cardAffected) -> GameUtils.getSpottableShadowCulturesCount(game, performingPlayer);
                        });
            } else if (type.equalsIgnoreCase("forEachCultureInMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "over", "limit", "multiplier", "divider");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> {
                            Set<Culture> cultures = new HashSet<>();
                            for (PhysicalCard card : actionContext.getCardsFromMemory(memory)) {
                                cultures.add(card.getBlueprint().getCulture());
                            }
                            return cultures.size();
                        });
            } else if (type.equalsIgnoreCase("forEachCultureToken")) {
                FieldUtils.validateAllowedFields(object, "filter", "culture", "over", "limit", "multiplier", "divider");

                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final Culture culture = FieldUtils.getEnum(Culture.class, object.get("culture"), "culture");
                final Token tokenForCulture = Token.findTokenForCulture(culture);

                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter,
                        environment);

                return new SmartValueSource(environment, object,
                        actionContext -> {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            return (game, cardAffected) -> {
                                int result = 0;
                                for (PhysicalCard physicalCard : Filters.filterActive(game, filterable)) {
                                    if (tokenForCulture != null) {
                                        result += game.getGameState().getTokenCount(physicalCard, tokenForCulture);
                                    } else {
                                        for (Map.Entry<Token, Integer> tokens : game.getGameState().getTokens(
                                                physicalCard).entrySet()) {
                                            if (tokens.getKey().getCulture() != null)
                                                result += tokens.getValue();
                                        }
                                    }
                                }

                                return result;
                            };
                        });
            } else if (type.equalsIgnoreCase("forEachHasAttached")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> Filters.countActive(game, Filters.attachedTo(cardAffected), filterableSource.getFilterable(actionContext)));
            } else if (type.equalsIgnoreCase("forEachInDeadPile")) {
                FieldUtils.validateAllowedFields(object, "filter", "limit", "over", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return new SmartValueSource(environment, object,
                        actionContext -> {
                            Filterable filterable = filterableSource.getFilterable(actionContext);
                            return (game, cardAffected) -> Filters.filter(game, game.getGameState().getDeadPile(game.getGameState().getCurrentPlayerId()), filterable).size();
                        });
            } else if (type.equalsIgnoreCase("forEachInDiscard")) {
                FieldUtils.validateAllowedFields(object, "discard", "filter", "over", "limit", "multiplier", "divider");
                final String discard = FieldUtils.getString(object.get("discard"), "discard");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                PlayerSource discardSource = discard != null ? PlayerResolver.resolvePlayer(discard, environment) : null;
                return new SmartValueSource(environment, object,
                        actionContext -> {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            String playerId = discardSource != null ? discardSource.getPlayer(actionContext) : null;
                            return (game, cardAffected) -> {
                                int count = 0;
                                if (playerId != null) {
                                    count += Filters.filter(game, game.getGameState().getDiscard(playerId), filterable).size();
                                } else {
                                    for (String player : game.getGameState().getPlayerOrder().getAllPlayers())
                                        count += Filters.filter(game, game.getGameState().getDiscard(player), filterable).size();
                                }

                                return count;
                            };
                        }
                );
            } else if (type.equalsIgnoreCase("forEachInHand")) {
                FieldUtils.validateAllowedFields(object, "filter", "hand", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final String hand = FieldUtils.getString(object.get("hand"), "hand", "you");
                final PlayerSource player = PlayerResolver.resolvePlayer(hand, environment);
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

                return new SmartValueSource(environment, object,
                        actionContext -> {
                            String playerId = player.getPlayer(actionContext);
                            Filterable filterable = filterableSource.getFilterable(actionContext);
                            return (game, cardAffected) -> Filters.filter(game, game.getGameState().getHand(playerId), filterable).size();
                        });
            } else if (type.equalsIgnoreCase("forEachInMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "filter", "over", "limit", "multiplier", "divider");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) ->
                                Filters.filter(game, actionContext.getCardsFromMemory(memory),
                                        filterableSource.getFilterable(actionContext)).size());
            } else if (type.equalsIgnoreCase("forEachKeyword")) {
                FieldUtils.validateAllowedFields(object, "filter", "keyword", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");
                final Keyword keyword = FieldUtils.getEnum(Keyword.class, object.get("keyword"), "keyword");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return new SmartValueSource(environment, object,
                        actionContext -> {
                            Filterable filterable = filterableSource.getFilterable(actionContext);
                            return (game, cardAffected) -> {
                                int count = 0;
                                for (PhysicalCard physicalCard : Filters.filterActive(game, filterable)) {
                                    count += game.getModifiersQuerying().getKeywordCount(game, physicalCard, keyword);
                                }
                                return count;
                            };
                        });
            } else if (type.equalsIgnoreCase("forEachKeywordOnCardInMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "keyword", "over", "limit", "multiplier", "divider");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");
                final Keyword keyword = FieldUtils.getEnum(Keyword.class, object.get("keyword"), "keyword");
                if (keyword == null)
                    throw new InvalidCardDefinitionException("Keyword cannot be null");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> {
                            int count = 0;
                            final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                            for (PhysicalCard cardFromMemory : cardsFromMemory) {
                                count += game.getModifiersQuerying().getKeywordCount(game, cardFromMemory, keyword);
                            }
                            return count;
                        });
            } else if (type.equalsIgnoreCase("forEachThreat")) {
                FieldUtils.validateAllowedFields(object, "over", "limit", "multiplier", "divider");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> game.getGameState().getThreats());
            }    else if (type.equalsIgnoreCase("forEachTopCardOfDeckUntilMatching")) {
                FieldUtils.validateAllowedFields(object, "filter", "deck", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");
                final PlayerSource deck = PlayerResolver.resolvePlayer(FieldUtils.getString(object.get("deck"), "deck", "you"), environment);

                FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

                return new SmartValueSource(environment, object,
                        actionContext -> {
                            String deckPlayer = deck.getPlayer(actionContext);
                            Filterable filterable = filterableSource.getFilterable(actionContext);
                            return (game, cardAffected) -> {
                                int count = 0;
                                for (PhysicalCard physicalCard : game.getGameState().getDeck(deckPlayer)) {
                                    count++;
                                    if (Filters.accepts(game, filterable, physicalCard))
                                        break;
                                }

                                return count;
                            };
                        });
            }  else if (type.equalsIgnoreCase("forEachRace")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return new SmartValueSource(environment, object,
                        actionContext -> {
                            Filterable filterable = filterableSource.getFilterable(actionContext);
                            return (game, cardAffected) -> GameUtils.getSpottableRacesCount(game, filterable);
                        });
            }  else if (type.equalsIgnoreCase("forEachStacked")) {
                FieldUtils.validateAllowedFields(object, "on", "filter", "over", "limit", "multiplier", "divider");
                final String on = FieldUtils.getString(object.get("on"), "on");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");

                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(on, environment);

                return new SmartValueSource(environment, object,
                        actionContext -> {
                            Filterable stackedFilter = filterableSource.getFilterable(actionContext);
                            final Filterable onFilter = onFilterableSource.getFilterable(actionContext);
                            return (game, cardAffected) -> {
                                int count = 0;
                                for (PhysicalCard card : Filters.filterActive(game, onFilter)) {
                                    count += Filters.filter(game, game.getGameState().getStackedCards(card), stackedFilter).size();
                                }
                                return count;
                            };
                        });
            } else if (type.equalsIgnoreCase("forEachTwilight")) {
                FieldUtils.validateAllowedFields(object, "limit", "over", "multiplier", "divider");

                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> game.getGameState().getTwilightPool());
            } else if (type.equalsIgnoreCase("forEachWound")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                if (filter.equals("any")) {
                    return new SmartValueSource(environment, object,
                            actionContext -> (game, cardAffected) -> game.getGameState().getWounds(cardAffected));
                } else {
                    return new SmartValueSource(environment, object,
                            actionContext -> (game, cardAffected) -> {
                                final Filterable filterable = filterableSource.getFilterable(actionContext);
                                int wounds = 0;
                                for (PhysicalCard physicalCard : Filters.filterActive(game, filterable)) {
                                    wounds += game.getGameState().getWounds(physicalCard);
                                }
                                return wounds;
                            });
                }
            } else if (type.equalsIgnoreCase("forEachYouCanSpot")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> Filters.countSpottable(game, filterableSource.getFilterable(actionContext)));
            } else if (type.equalsIgnoreCase("maxOfRaces")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");

                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
                return new SmartValueSource(environment, object,
                        actionContext -> {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            return (game, cardAffected) -> {
                                int result = 0;
                                for (Race race : Race.values())
                                    result = Math.max(result, Filters.countSpottable(game, race, filterable));

                                return result;
                            };
                        });
            } else if (type.equalsIgnoreCase("nextSiteNumber")) {
                FieldUtils.validateAllowedFields(object, "over", "limit", "multiplier", "divider");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> game.getGameState().getCurrentSiteNumber() + 1);
            } else if (type.equalsIgnoreCase("siteNumberAfterNext")) {
                FieldUtils.validateAllowedFields(object, "over", "limit", "multiplier", "divider");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> game.getGameState().getCurrentSiteNumber() + 2);
            } else if (type.equalsIgnoreCase("siteNumberInMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "over", "limit", "multiplier", "divider");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> actionContext.getCardFromMemory(memory).getSiteNumber());
            } else if (type.equalsIgnoreCase("regionNumber")) {
                FieldUtils.validateAllowedFields(object, "over", "limit", "multiplier", "divider");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> GameUtils.getRegion(game));
            } else if (type.equalsIgnoreCase("printedStrengthFromMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "over", "limit", "multiplier", "divider");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");

                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> {
                            int result = 0;
                            for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                                result += physicalCard.getBlueprint().getStrength();
                            }
                            return result;
                        });
            } else if (type.equalsIgnoreCase("turnNumber")) {
                FieldUtils.validateAllowedFields(object, "over", "limit", "multiplier", "divider");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> game.getGameState().getTurnNumber());
            } else if (type.equalsIgnoreCase("twilightCostInMemory")) {
                FieldUtils.validateAllowedFields(object, "memory", "over", "limit", "multiplier", "divider");
                final String memory = FieldUtils.getString(object.get("memory"), "memory");
                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> {
                            int total = 0;
                            for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                                total += physicalCard.getBlueprint().getTwilightCost();
                            }
                            return total;
                        });
            } else if (type.equalsIgnoreCase("whileInZone")) {
                FieldUtils.validateAllowedFields(object, "over", "limit", "multiplier", "divider");
                return new SmartValueSource(environment, object,
                        actionContext -> {
                            PhysicalCard source = actionContext.getSource();
                            return (game, cardAffected) -> {
                                PhysicalCard.WhileInZoneData whileInZoneData = source.getWhileInZoneData();
                                if (whileInZoneData != null)
                                    return Integer.parseInt(whileInZoneData.getValue());
                                return -1;
                            };
                        });
            } else if (type.equalsIgnoreCase("woundsTakenInCurrentPhase")) {
                FieldUtils.validateAllowedFields(object, "filter", "over", "limit", "multiplier", "divider");
                final String filter = FieldUtils.getString(object.get("filter"), "filter");
                final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

                return new SmartValueSource(environment, object,
                        actionContext -> (game, cardAffected) -> {
                            int count = 0;
                            for (PhysicalCard physicalCard : Filters.filterActive(game, filterableSource.getFilterable(actionContext))) {
                                count += game.getModifiersEnvironment().getWoundsTakenInCurrentPhase(physicalCard);
                            }
                            return count;
                        });
            }
            throw new InvalidCardDefinitionException("Unrecognized type of an evaluator " + type);
        }
        throw new InvalidCardDefinitionException("Unable to resolve an evaluator");
    }

    private static class SmartValueSource implements ValueSource {
        private final ValueSource valueSource;
        private final ValueSource overSource;
        private final ValueSource limitSource;
        private final ValueSource multiplierSource;
        private final ValueSource dividerSource;

        public SmartValueSource(CardGenerationEnvironment environment,
                                JSONObject object, ValueSource valueSource) throws InvalidCardDefinitionException {
            this.valueSource = valueSource;
            overSource = resolveEvaluator(object.get("over"), 0, environment);
            limitSource = resolveEvaluator(object.get("limit"), Integer.MAX_VALUE, environment);
            multiplierSource = resolveEvaluator(object.get("multiplier"), 1, environment);
            dividerSource = resolveEvaluator(object.get("divider"), 1, environment);
        }

        @Override
        public Evaluator getEvaluator(ActionContext actionContext) {
            Evaluator value = valueSource.getEvaluator(actionContext);
            Evaluator over = overSource.getEvaluator(actionContext);
            Evaluator limit = limitSource.getEvaluator(actionContext);
            Evaluator multiplier = multiplierSource.getEvaluator(actionContext);
            Evaluator divider = dividerSource.getEvaluator(actionContext);
            return (game, cardAffected) -> {
                int result = Math.max(0, value.evaluateExpression(game, cardAffected) - over.evaluateExpression(game, cardAffected));
                result = Math.min(limit.evaluateExpression(game, cardAffected), result);
                result *= multiplier.evaluateExpression(game, cardAffected);
                result /= divider.evaluateExpression(game, cardAffected);
                return result;
            };
        }
    }
}
