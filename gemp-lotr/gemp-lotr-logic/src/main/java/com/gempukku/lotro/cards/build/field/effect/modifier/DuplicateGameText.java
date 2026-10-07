package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.DuplicateGameTextModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class DuplicateGameText implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "from", "requires", "snapshot");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filterDef = FieldUtils.getString(object.get("filter"), "filter");
        final String fromDef = FieldUtils.getString(object.get("from"), "from");
        // "snapshot: true" remembers the `from` cards once, when the modifier is made (as an AddModifier effect is
        // carried out), and gives their text until it ends, though they have since left play or their stack:
        // "until the end of the turn, that Southron gains the game text of that stacked Man" (Ketura on F-0160).
        // The same word as on AddKeywordFromCards, which copies the keywords.  Without it the cards are looked for
        // each time the text is read.
        final boolean snapshot = FieldUtils.getBoolean(object.get("snapshot"), "snapshot", false);

        if(!filterDef.equals("self") && !filterDef.startsWith("memory(")) {
            throw new InvalidCardDefinitionException("Only 'self' and 'memory' are supported filters for DuplicateGameText.");
        }

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filterDef, environment);
        final FilterableSource fromSource = environment.getFilterFactory().generateFilter(fromDef, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                final DuplicateGameTextModifier modifier = new DuplicateGameTextModifier(actionContext.getSource(),
                        filterableSource.getFilterable(actionContext),
                        RequirementCondition.createCondition(requirements, actionContext),
                        fromSource.getFilterable(actionContext)
                );
                return snapshot ? modifier.takeSnapshot(actionContext.getGame()) : modifier;
            }
        };
    }
}
