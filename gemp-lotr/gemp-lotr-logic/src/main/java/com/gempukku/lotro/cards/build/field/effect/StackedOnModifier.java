package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.modifiers.DelegateModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class StackedOnModifier implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "modifier", "on");

        final String onFilter = FieldUtils.getString(value.get("on"), "on");

        final FilterableSource onFilterableSource = environment.getFilterFactory().generateFilter(onFilter, environment);

        JSONObject jsonObject = (JSONObject) value.get("modifier");
        final ModifierSource modifier = environment.getModifierSourceFactory().getModifier(jsonObject, environment);

        blueprint.appendStackedOnModifier(
                new ModifierSource() {
                    @Override
                    public Modifier getModifier(ActionContext actionContext) {
                        Modifier originalModifier = modifier.getModifier(actionContext);
                        Condition originalModifierCondition = originalModifier.getCondition();

                        Filterable filterable = onFilterableSource.getFilterable(actionContext);
                        Filter filter = Filters.changeToFilter(filterable);

                        Condition resultCondition = new Condition() {
                            @Override
                            public boolean isFullfilled(LotroGame game) {
                                PhysicalCard source = actionContext.getSource();
                                PhysicalCard stackedOn = source.getStackedOn();
                                return stackedOn != null && filter.accepts(game, stackedOn)
                                        && (originalModifierCondition == null || originalModifierCondition.isFullfilled(game));
                            }
                        };

                        return new DelegateModifier(originalModifier) {
                            @Override
                            public Condition getCondition() {
                                return resultCondition;
                            }
                        };
                    }
                });
    }
}
