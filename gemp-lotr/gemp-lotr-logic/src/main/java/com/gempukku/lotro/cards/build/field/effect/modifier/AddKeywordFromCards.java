package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.AddKeywordFromModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class AddKeywordFromCards implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "from", "terrain", "unloaded", "snapshot");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String from = FieldUtils.getString(object.get("from"), "from");
        boolean terrainOnly = FieldUtils.getBoolean(object.get("terrain"), "terrain", false);
        boolean unloadedOnly = FieldUtils.getBoolean(object.get("unloaded"), "unloaded", false);
        // "snapshot: true" takes the keywords once, when the modifier is made (as an AddModifier effect is carried
        // out), and gives those until it ends: "until the regroup phase, ... gain each of the keywords on that
        // Man" (Ketura on Q-0096).  Without it the cards are read as they stand, which a standing modifier wants.
        boolean snapshot = FieldUtils.getBoolean(object.get("snapshot"), "snapshot", false);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource fromSource = environment.getFilterFactory().generateFilter(from, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                final AddKeywordFromModifier modifier = new AddKeywordFromModifier(actionContext.getSource(),
                        filterableSource.getFilterable(actionContext),
                        RequirementCondition.createCondition(requirements, actionContext),
                        fromSource.getFilterable(actionContext), terrainOnly, unloadedOnly);
                return snapshot ? modifier.takeSnapshot(actionContext.getGame()) : modifier;
            }
        };
    }
}
