package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import org.json.simple.JSONObject;

public class ExtraPossessionClassEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "attachedTo");

        final String attachedFilter = FieldUtils.getString(value.get("attachedTo"), "attachedTo", "any");

        final FilterableSource attachedFilterableSource = environment.getFilterFactory().generateFilter(attachedFilter, environment);

        blueprint.setExtraPossessionClassTest(
                (game, self, attachedTo) -> {
                    DefaultActionContext actionContext = new DefaultActionContext(self.getOwner(), game, self, null, null);
                    final Filterable attachedFilterable = attachedFilterableSource.getFilterable(actionContext);
                    return Filters.accepts(game, attachedTo, attachedFilterable);
                });
    }
}
