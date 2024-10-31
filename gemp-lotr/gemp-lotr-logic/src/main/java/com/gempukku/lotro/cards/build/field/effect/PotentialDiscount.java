package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.StringWhileInZoneData;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.DiscountEffect;
import com.gempukku.lotro.logic.effects.discount.*;
import org.json.simple.JSONObject;

public class PotentialDiscount implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "max", "discount", "memorize");

        final ValueSource maxSource = ValueResolver.resolveEvaluator(value.get("max"), 1000, environment);
        final JSONObject discount = (JSONObject) value.get("discount");
        final String memory = FieldUtils.getString(value.get("memorize"), "memorize", "_temp");

        final String discountType = FieldUtils.getString(discount.get("type"), "type");
        if (discountType.equalsIgnoreCase("perDiscardFromHand")) {
            // Used only by Second Edition - just ignore
            FieldUtils.validateAllowedFields(discount, "filter");

            final String filter = FieldUtils.getString(discount.get("filter"), "filter", "any");
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

            blueprint.appendDiscountSource(
                    new DiscountSource() {
                        @Override
                        public int getPotentialDiscount(ActionContext actionContext) {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            int count = Filters.filter(actionContext.getGame(), actionContext.getGame().getGameState().getHand(actionContext.getPerformingPlayer()), filterable).size();
                            int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                            return Math.min(count, max);
                        }

                        @Override
                        public DiscountEffect getDiscountEffect(CostToEffectAction action, ActionContext actionContext) {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            return new DiscardCardFromHandDiscountEffect(action, actionContext.getPerformingPlayer(), filterable) {
                                @Override
                                protected void discountPaidCallback(int paid) {
                                    actionContext.setValueToMemory(memory, String.valueOf(paid));
                                    actionContext.getSource().setWhileInZoneData(new StringWhileInZoneData(String.valueOf(paid)));
                                }
                            };
                        }
                    });
        } else if (discountType.equalsIgnoreCase("perExert")) {
            FieldUtils.validateAllowedFields(discount, "multiplier", "filter");

            final ValueSource multiplierSource = ValueResolver.resolveEvaluator(discount.get("multiplier"), environment);
            final String filter = FieldUtils.getString(discount.get("filter"), "filter", "any");
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

            blueprint.appendDiscountSource(
                    new DiscountSource() {
                        @Override
                        public int getPotentialDiscount(ActionContext actionContext) {
                            Filterable filterable = filterableSource.getFilterable(actionContext);
                            final int multiplier = multiplierSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                            int possibleExertCount = Filters.countActive(actionContext.getGame(), filterable, Filters.canExert(actionContext.getSource())) * multiplier;
                            int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                            return Math.min(possibleExertCount, max);
                        }

                        @Override
                        public DiscountEffect getDiscountEffect(CostToEffectAction action, ActionContext actionContext) {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            final int multiplier = multiplierSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                            return new ExertCharactersDiscountEffect(action, actionContext.getSource(),
                                    actionContext.getPerformingPlayer(), multiplier, filterable) {
                                @Override
                                protected void discountPaidCallback(int paid) {
                                    actionContext.setValueToMemory(memory, String.valueOf(paid));
                                    actionContext.getSource().setWhileInZoneData(new StringWhileInZoneData(String.valueOf(paid)));
                                }
                            };
                        }
                    });
        } else if (discountType.equalsIgnoreCase("perThreatRemoved")) {
            blueprint.appendDiscountSource(
                    new DiscountSource() {
                        @Override
                        public int getPotentialDiscount(ActionContext actionContext) {
                            int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                            int maxFromThreats = actionContext.getGame().getModifiersQuerying().canRemoveThreat(actionContext.getGame(), actionContext.getSource()) ?
                                    actionContext.getGame().getGameState().getThreats() : 0;
                            return Math.min(max, maxFromThreats);
                        }

                        @Override
                        public DiscountEffect getDiscountEffect(CostToEffectAction action, ActionContext actionContext) {
                            return new RemoveThreatsToDiscountEffect(action) {
                                @Override
                                protected void discountPaidCallback(int paid) {
                                    actionContext.setValueToMemory(memory, String.valueOf(paid));
                                    actionContext.getSource().setWhileInZoneData(new StringWhileInZoneData(String.valueOf(paid)));
                                }
                            };
                        }
                    });
        } else if (discountType.equalsIgnoreCase("ifDiscardFromPlay")) {
            FieldUtils.validateAllowedFields(discount, "count", "filter");

            final ValueSource discardCountSource = ValueResolver.resolveEvaluator(discount.get("count"), environment);
            final String filter = FieldUtils.getString(discount.get("filter"), "filter", "any");
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

            blueprint.appendDiscountSource(
                    new DiscountSource() {
                        @Override
                        public int getPotentialDiscount(ActionContext actionContext) {
                            Filterable filterable = filterableSource.getFilterable(actionContext);
                            int possibleDiscard = Filters.countActive(actionContext.getGame(), filterable, Filters.canBeDiscarded(actionContext.getPerformingPlayer(), actionContext.getSource()));
                            int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                            return Math.min(possibleDiscard, max);
                        }

                        @Override
                        public DiscountEffect getDiscountEffect(CostToEffectAction action, ActionContext actionContext) {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            final int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                            final int discardCount = discardCountSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                            actionContext.setValueToMemory(memory, "No");
                            return new OptionalDiscardDiscountEffect(action, max, actionContext.getPerformingPlayer(), discardCount, filterable) {
                                @Override
                                protected void discountPaidCallback(int paid) {
                                    actionContext.setValueToMemory(memory, "Yes");
                                    actionContext.getSource().setWhileInZoneData(new StringWhileInZoneData(memory));
                                }
                            };
                        }
                    });
        } else if (discountType.equalsIgnoreCase("ifRemoveFromDiscard")) {
            FieldUtils.validateAllowedFields(discount, "count", "filter");

            final ValueSource removeCountSource = ValueResolver.resolveEvaluator(discount.get("count"), environment);
            final String filter = FieldUtils.getString(discount.get("filter"), "filter", "any");
            final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

            blueprint.appendDiscountSource(
                    new DiscountSource() {
                        @Override
                        public int getPotentialDiscount(ActionContext actionContext) {
                            int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                            Filterable filterable = filterableSource.getFilterable(actionContext);
                            int countToRemove = Filters.filter(actionContext.getGame(), actionContext.getGame().getGameState().getDiscard(actionContext.getPerformingPlayer()), filterable).size();
                            int count = removeCountSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                            if (countToRemove >= count) {
                                return max;
                            } else {
                                return 0;
                            }
                        }

                        @Override
                        public DiscountEffect getDiscountEffect(CostToEffectAction action, ActionContext actionContext) {
                            final Filterable filterable = filterableSource.getFilterable(actionContext);
                            final int max = maxSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                            final int removeCount = removeCountSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());
                            actionContext.setValueToMemory(memory, "No");
                            return new RemoveCardsFromDiscardDiscountEffect(actionContext.getSource(), actionContext.getPerformingPlayer(), removeCount, max, filterable) {
                                @Override
                                protected void discountPaidCallback(int paid) {
                                    actionContext.setValueToMemory(memory, "Yes");
                                    actionContext.getSource().setWhileInZoneData(new StringWhileInZoneData(memory));
                                }
                            };
                        }
                    });
        } else {
            throw new InvalidCardDefinitionException("Unknown type of discount: " + discountType);
        }
    }
}
