package com.gempukku.lotro.cards.build.field.effect.appender.resolver;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.appender.DelayedAppender;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ChooseActiveCardsEffect;
import com.gempukku.lotro.logic.effects.ChooseArbitraryCardsEffect;
import com.gempukku.lotro.logic.effects.choose.*;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CardResolver {

    public static EffectAppender resolveSingleStack(String select, ValueSource countSource, FilterableSource stackedOn,
        String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {

        Function<ActionContext, Iterable<? extends PhysicalCard>> stackedOnSource = actionContext ->
                Filters.filter(actionContext.getGame(), actionContext.getGame().getGameState().getAllCards(), stackedOn.getFilterable(actionContext));

        Function<ActionContext, Iterable<? extends PhysicalCard>> stackSource = actionContext ->
                Filters.filter(actionContext.getGame(), actionContext.getGame().getGameState().getAllCards(), Filters.stackedOn(stackedOn.getFilterable(actionContext)));

        if (select.startsWith("memory(") && select.endsWith(")")) {
            return resolveMemoryCards(select, null, null, countSource, memory, stackSource);
        }
        else if (select.startsWith("choose(") && select.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                return new ChooseCardsFromSingleStackEffect(action, choicePlayerId, min, max, stackedOnSource.apply(actionContext).iterator().next(), Filters.in(possibleCards)) {
                    @Override
                    protected void cardsChosen(LotroGame game, Collection<PhysicalCard> stackedCards) {
                        actionContext.setCardMemory(memory, stackedCards);
                        game.getGameState().sendMessage(GameUtils.substituteText("{you} chooses {" + memory + "}.", actionContext));
                    }

                    @Override
                    public String getText(LotroGame game) {
                        return GameUtils.substituteText(choiceText, actionContext);
                    }
                };
            };

            return resolveChoiceCards(select, null, null, countSource, environment, stackSource, effectSource);
        }
        throw new InvalidCardDefinitionException("Unable to resolve card resolver of type: " + select);
    }

    public static EffectAppender resolveStackedCards(String type, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveStackedCards(type, null, countSource, stackedOn, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveStackedCards(String type, FilterableSource additionalFilter, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveStackedCards(type, additionalFilter, additionalFilter, countSource, stackedOn, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveStackedCards(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource, FilterableSource stackedOn,
                                                     String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            if(stackedOn != null) {
                final Filterable stackedOnFilter = stackedOn.getFilterable(actionContext);
                return Filters.filter(actionContext.getGame(), actionContext.getGame().getGameState().getAllCards(), Filters.stackedOn(stackedOnFilter));
            }
            else {
                return Filters.filter(actionContext.getGame(), actionContext.getGame().getGameState().getAllCards(), Filters.stackedOn(Filters.any));
            }

        };

        if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, choiceFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.equals("self")) {
            return resolveSelf(choiceFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                return new ChooseStackedCardsEffect(action, choicePlayerId, min, max, stackedOn.getFilterable(actionContext), Filters.in(possibleCards)) {
                    @Override
                    protected void cardsChosen(LotroGame game, Collection<PhysicalCard> stackedCards) {
                        actionContext.setCardMemory(memory, stackedCards);
                        game.getGameState().sendMessage(GameUtils.substituteText("{you} chooses {" + memory + "}.", actionContext));
                    }

                    @Override
                    public String getText(LotroGame game) {
                        return GameUtils.substituteText(choiceText, actionContext);
                    }
                };
            };

            return resolveChoiceCards(type, choiceFilter, playabilityFilter, countSource, environment, cardSource, effectSource);
        }
        throw new InvalidCardDefinitionException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInHand(String type, ValueSource countSource, String memory, String choicePlayer, String handPlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInHand(type, null, countSource, memory, choicePlayer, handPlayer, choiceText, false, environment);
    }

    public static EffectAppender resolveCardsInHand(String type, ValueSource countSource, String memory, String choicePlayer, String handPlayer, String choiceText, boolean showMatchingOnly, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInHand(type, null, countSource, memory, choicePlayer, handPlayer, choiceText, showMatchingOnly, environment);
    }

    public static EffectAppender resolveCardsInHand(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String handPlayer, String choiceText, boolean showMatchingOnly, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final PlayerSource handSource = PlayerResolver.resolvePlayer(handPlayer);
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String handPlayer1 = handSource.getPlayer(actionContext);
            return actionContext.getGame().getGameState().getHand(handPlayer1);
        };

        if (type.startsWith("random(") && type.endsWith(")")) {
            final int count = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")));
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    final String handPlayer = handSource.getPlayer(actionContext);
                    return actionContext.getGame().getGameState().getHand(handPlayer).size() >= count;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    final String handPlayer = handSource.getPlayer(actionContext);
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            List<? extends PhysicalCard> hand = game.getGameState().getHand(handPlayer);
                            List<PhysicalCard> randomCardsFromHand = GameUtils.getRandomCards(hand, 2);
                            actionContext.setCardMemory(memory, randomCardsFromHand);
                        }
                    };
                }
            };
        } else if (type.equals("self")) {
            return resolveSelf(additionalFilter, additionalFilter, countSource, memory, cardSource);
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, additionalFilter, additionalFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, additionalFilter, memory, environment, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String handId = handSource.getPlayer(actionContext);
                String choicePlayerId = playerSource.getPlayer(actionContext);
                if (handId.equals(choicePlayerId)) {
                    return new ChooseCardsFromHandEffect(choicePlayerId, min, max, Filters.in(possibleCards)) {
                        @Override
                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
                            actionContext.setCardMemory(memory, cards);
                        }

                        @Override
                        public String getText(LotroGame game) {
                            return GameUtils.substituteText(choiceText, actionContext);
                        }
                    };
                } else {
                    List<? extends PhysicalCard> cardsInHand = actionContext.getGame().getGameState().getHand(handId);
                    return new ChooseArbitraryCardsEffect(choicePlayerId, GameUtils.substituteText(choiceText, actionContext), cardsInHand, Filters.in(possibleCards), min, max, showMatchingOnly) {
                        @Override
                        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                            actionContext.setCardMemory(memory, selectedCards);
                        }
                    };
                }
            };

            return resolveChoiceCards(type, additionalFilter, additionalFilter, countSource, environment, cardSource, effectSource);
        }
        throw new InvalidCardDefinitionException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInAdventureDeck(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String adventureDeckPlayer, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final PlayerSource playerDeckSource = PlayerResolver.resolvePlayer(adventureDeckPlayer);
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String handPlayer1 = playerDeckSource.getPlayer(actionContext);
            return actionContext.getGame().getGameState().getAdventureDeck(handPlayer1);
        };

        if (type.startsWith("random(") && type.endsWith(")")) {
            final int count = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.lastIndexOf(")")));
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    final String playerDeck = playerDeckSource.getPlayer(actionContext);
                    return actionContext.getGame().getGameState().getHand(playerDeck).size() >= count;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    final String playerDeck = playerDeckSource.getPlayer(actionContext);
                    return new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            List<? extends PhysicalCard> adventureDeck = game.getGameState().getAdventureDeck(playerDeck);
                            List<PhysicalCard> randomCardsFromAdventureDeck = GameUtils.getRandomCards(adventureDeck, count);
                            actionContext.setCardMemory(memory, randomCardsFromAdventureDeck);
                        }
                    };
                }
            };
        } else if (type.equals("self")) {
            return resolveSelf(additionalFilter, additionalFilter, countSource, memory, cardSource);
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, additionalFilter, additionalFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, additionalFilter, memory, environment, cardSource);
        }
        throw new InvalidCardDefinitionException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInDiscard(String type, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, null, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, ValueSource countSource, String memory, String choicePlayer, String targetPlayerDiscard, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, null, countSource, memory, choicePlayer, targetPlayerDiscard, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, additionalFilter, additionalFilter, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String targetPlayerDiscard, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, additionalFilter, additionalFilter, countSource, memory, choicePlayer,  targetPlayerDiscard, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCardsInDiscard(type, choiceFilter, playabilityFilter, countSource, memory, choicePlayer, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCardsInDiscard(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource, String memory, String choicePlayer, String targetPlayerDiscard, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
        final PlayerSource targetPlayerDiscardSource = PlayerResolver.resolvePlayer(targetPlayerDiscard);

        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String targetPlayerId = targetPlayerDiscardSource.getPlayer(actionContext);
            return actionContext.getGame().getGameState().getDiscard(targetPlayerId);
        };

        if (type.equals("self")) {
            return resolveSelf(choiceFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, choiceFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, choiceFilter, memory, environment, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                String targetPlayerDiscardId = targetPlayerDiscardSource.getPlayer(actionContext);
                return new ChooseCardsFromDiscardEffect(choicePlayerId, targetPlayerDiscardId, min, max, Filters.in(possibleCards)) {
                    @Override
                    protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
                        actionContext.setCardMemory(memory, cards);
                        game.getGameState().sendMessage(GameUtils.substituteText("{you} chooses {" + memory + "}.", actionContext));
                    }

                    @Override
                    public String getText(LotroGame game) {
                        return GameUtils.substituteText(choiceText, actionContext);
                    }
                };
            };

            return resolveChoiceCards(type, choiceFilter, playabilityFilter, countSource, environment, cardSource, effectSource);
        }
        throw new InvalidCardDefinitionException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInDeadPile(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);

        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            return actionContext.getGame().getGameState().getDeadPile(actionContext.getGame().getGameState().getCurrentPlayerId());
        };

        if (type.equals("self")) {
            return resolveSelf(choiceFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, choiceFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, choiceFilter, memory, environment, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                return new ChooseCardsFromDeadPileEffect(choicePlayerId, min, max, Filters.in(possibleCards)) {
                    @Override
                    protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
                        actionContext.setCardMemory(memory, cards);
                        game.getGameState().sendMessage(GameUtils.substituteText("{you} chooses {" + memory + "}.", actionContext));
                    }

                    @Override
                    public String getText(LotroGame game) {
                        return GameUtils.substituteText(choiceText, actionContext);
                    }
                };
            };

            return resolveChoiceCards(type, choiceFilter, playabilityFilter, countSource, environment, cardSource, effectSource);
        }
        throw new InvalidCardDefinitionException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCardsInDeck(String type, FilterableSource choiceFilter, ValueSource countSource, String memory, String choicePlayer, String targetDeck,
                                                    boolean showAll, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
        final PlayerSource deckSource = PlayerResolver.resolvePlayer(targetDeck);

        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext -> {
            String deckId = deckSource.getPlayer(actionContext);
            return actionContext.getGame().getGameState().getDeck(deckId);
        };

        if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, choiceFilter, choiceFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, choiceFilter, memory, environment, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                String targetDeckId = deckSource.getPlayer(actionContext);
                return new ChooseCardsFromDeckEffect(choicePlayerId, targetDeckId, showAll, min, max, Filters.in(possibleCards)) {
                    @Override
                    protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
                        actionContext.setCardMemory(memory, cards);
                    }

                    @Override
                    public String getText(LotroGame game) {
                        return GameUtils.substituteText(choiceText, actionContext);
                    }
                };
            };

            return resolveChoiceCards(type, choiceFilter, choiceFilter, countSource, environment, cardSource, effectSource);
        }
        throw new InvalidCardDefinitionException("Unable to resolve card resolver of type: " + type);
    }

    public static EffectAppender resolveCard(String type, FilterableSource additionalFilter, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCards(type, additionalFilter, actionContext -> new ConstantEvaluator(1), memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCard(String type, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCard(type, null, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCards(type, null, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, FilterableSource additionalFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        return resolveCards(type, additionalFilter, additionalFilter, countSource, memory, choicePlayer, choiceText, environment);
    }

    public static EffectAppender resolveCards(String type, FilterableSource additionalFilter, FilterableSource playabilityFilter, ValueSource countSource, String memory, String choicePlayer, String choiceText, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource = actionContext ->
                Filters.filterActive(actionContext.getGame(), Filters.any);

        if (type.equals("self")) {
            return resolveSelf(additionalFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.equals("bearer")) {
            return new DelayedAppender() {
                @Override
                public boolean isPlayableInFull(ActionContext actionContext) {
                    int min = countSource.getEvaluator(actionContext).getMinimum(actionContext.getGame(), null);
                    return filterCards(actionContext, playabilityFilter).size() >= min;
                }

                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    Collection<PhysicalCard> result = filterCards(actionContext, additionalFilter);
                    Evaluator evaluator = countSource.getEvaluator(actionContext);
                    return new AbstractEffect() {
                        @Override
                        public boolean isPlayableInFull(LotroGame game) {
                            return true;
                        }

                        @Override
                        protected FullEffectResult playEffectReturningResult(LotroGame game) {
                            actionContext.setCardMemory(memory, result);
                            int min = evaluator.getMinimum(game, null);
                            if (result.size() >= min) {
                                return new FullEffectResult(true);
                            } else {
                                return new FullEffectResult(false);
                            }
                        }
                    };
                }

                private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                    PhysicalCard source = actionContext.getSource();
                    PhysicalCard attachedTo = source.getAttachedTo();
                    if (attachedTo == null)
                        return Collections.emptySet();
                    
                    Filterable additionalFilterable = Filters.any;
                    if (filter != null)
                        additionalFilterable = filter.getFilterable(actionContext);
                    return Filters.filter(actionContext.getGame(), cardSource.apply(actionContext), attachedTo, additionalFilterable);
                }
            };
        } else if (type.startsWith("memory(") && type.endsWith(")")) {
            return resolveMemoryCards(type, additionalFilter, playabilityFilter, countSource, memory, cardSource);
        } else if (type.startsWith("all(") && type.endsWith(")")) {
            return resolveAllCards(type, additionalFilter, memory, environment, cardSource);
        } else if (type.startsWith("choose(") && type.endsWith(")")) {
            final PlayerSource playerSource = PlayerResolver.resolvePlayer(choicePlayer);
            ChoiceEffectSource effectSource = (possibleCards, action, actionContext, min, max) -> {
                String choicePlayerId = playerSource.getPlayer(actionContext);
                return new ChooseActiveCardsEffect(actionContext.getSource(), choicePlayerId, GameUtils.substituteText(choiceText, actionContext), min, max, Filters.in(possibleCards)) {
                    @Override
                    protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
                        actionContext.setCardMemory(memory, cards);
                        game.getGameState().sendMessage(GameUtils.substituteText("{you} chooses {" + memory + "}.", actionContext));
                    }
                };
            };

            return resolveChoiceCards(type, additionalFilter, playabilityFilter, countSource, environment, cardSource, effectSource);
        }
        throw new InvalidCardDefinitionException("Unable to resolve card resolver of type: " + type);
    }

    private static DelayedAppender resolveSelf(FilterableSource choiceFilter, FilterableSource playabilityFilter,
                                               ValueSource countSource, String memory,
                                               Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource) {
        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getEvaluator(actionContext).getMinimum(actionContext.getGame(), null);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                Evaluator evaluator = countSource.getEvaluator(actionContext);
                Collection<PhysicalCard> result = filterCards(actionContext, choiceFilter);
                return new AbstractEffect() {
                    @Override
                    public boolean isPlayableInFull(LotroGame game) {
                        return true;
                    }

                    @Override
                    protected FullEffectResult playEffectReturningResult(LotroGame game) {
                        actionContext.setCardMemory(memory, result);
                        int min = evaluator.getMinimum(game, null);
                        if (result.size() >= min) {
                            return new FullEffectResult(true);
                        } else {
                            return new FullEffectResult(false);
                        }
                    }
                };
            }

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                PhysicalCard source = actionContext.getSource();
                Filterable additionalFilterable = Filters.any;
                if (filter != null)
                    additionalFilterable = filter.getFilterable(actionContext);
                return Filters.filter(actionContext.getGame(), cardSource.apply(actionContext), source, additionalFilterable);
            }
        };
    }

    private static DelayedAppender resolveMemoryCards(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter,
                                                      ValueSource countSource, String memory,
                                                      Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource) throws InvalidCardDefinitionException {
        String sourceMemory = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        if (sourceMemory.contains("(") || sourceMemory.contains(")"))
            throw new InvalidCardDefinitionException("Memory name cannot contain parenthesis");

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                if (playabilityFilter != null) {
                    int min = countSource.getEvaluator(actionContext).getMinimum(actionContext.getGame(), null);
                    return filterCards(actionContext, playabilityFilter).size() >= min;
                } else {
                    return true;
                }
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                Evaluator evaluator = countSource.getEvaluator(actionContext);
                Collection<PhysicalCard> result = filterCards(actionContext, choiceFilter);
                return new AbstractEffect() {
                    @Override
                    public boolean isPlayableInFull(LotroGame game) {
                        return true;
                    }

                    @Override
                    protected FullEffectResult playEffectReturningResult(LotroGame game) {
                        actionContext.setCardMemory(memory, result);
                        int min = evaluator.getMinimum(game, null);
                        if (result.size() >= min) {
                            return new FullEffectResult(true);
                        } else {
                            return new FullEffectResult(false);
                        }
                    }
                };
            }

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(sourceMemory);
                Filterable additionalFilterable = Filters.any;
                if (filter != null)
                    additionalFilterable = filter.getFilterable(actionContext);
                return Filters.filter(actionContext.getGame(), cardSource.apply(actionContext), Filters.in(cardsFromMemory), additionalFilterable);
            }
        };
    }

    private static DelayedAppender resolveChoiceCards(String type, FilterableSource choiceFilter, FilterableSource playabilityFilter,
                                                      ValueSource countSource, CardGenerationEnvironment environment, Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource, ChoiceEffectSource effectSource) throws InvalidCardDefinitionException {
        final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                int min = countSource.getEvaluator(actionContext).getMinimum(actionContext.getGame(), null);
                return filterCards(actionContext, playabilityFilter).size() >= min;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                Evaluator evaluator = countSource.getEvaluator(actionContext);
                int min = evaluator.getMinimum(actionContext.getGame(), null);
                int max = evaluator.getMaximum(actionContext.getGame(), null);
                return effectSource.createEffect(filterCards(actionContext, choiceFilter), action, actionContext, min, max);
            }

            private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                Filterable filterable = filterableSource.getFilterable(actionContext);
                Filterable additionalFilterable = Filters.any;
                if (filter != null)
                    additionalFilterable = filter.getFilterable(actionContext);
                return Filters.filter(actionContext.getGame(), cardSource.apply(actionContext), filterable, additionalFilterable);
            }
        };
    }

    private static DelayedAppender resolveAllCards(String type, FilterableSource additionalFilter, String memory, CardGenerationEnvironment environment, Function<ActionContext, Iterable<? extends PhysicalCard>> cardSource) throws InvalidCardDefinitionException {
        final String filter = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        actionContext.setCardMemory(memory, filterCards(actionContext, additionalFilter));
                    }

                    private Collection<PhysicalCard> filterCards(ActionContext actionContext, FilterableSource filter) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        Filterable additionalFilterable = Filters.any;
                        if (filter != null)
                            additionalFilterable = filter.getFilterable(actionContext);
                        return Filters.filter(actionContext.getGame(), cardSource.apply(actionContext), filterable, additionalFilterable);
                    }
                };
            }
        };
    }

    private interface ChoiceEffectSource {
        Effect createEffect(Collection<? extends PhysicalCard> possibleCards, CostToEffectAction action, ActionContext actionContext,
                            int min, int max);
    }
}
