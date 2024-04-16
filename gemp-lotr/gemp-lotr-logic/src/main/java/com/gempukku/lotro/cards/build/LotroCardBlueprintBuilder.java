package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.cards.build.field.*;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderFactory;
import com.gempukku.lotro.cards.build.field.effect.filter.FilterFactory;
import com.gempukku.lotro.cards.build.field.effect.modifier.ModifierSourceFactory;
import com.gempukku.lotro.cards.build.field.effect.requirement.RequirementFactory;
import com.gempukku.lotro.cards.build.field.effect.trigger.TriggerCheckerFactory;
import com.gempukku.lotro.game.LotroCardBlueprint;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LotroCardBlueprintBuilder implements CardGenerationEnvironment {
    private final Map<String, FieldProcessor> fieldProcessors = new HashMap<>();

    private final EffectAppenderFactory effectAppenderFactory = new EffectAppenderFactory();
    private final FilterFactory filterFactory = new FilterFactory();
    private final RequirementFactory requirementFactory = new RequirementFactory();
    private final TriggerCheckerFactory triggerCheckerFactory = new TriggerCheckerFactory();
    private final ModifierSourceFactory modifierSourceFactory = new ModifierSourceFactory();

    public LotroCardBlueprintBuilder() {
        fieldProcessors.put("title", new TitleFieldProcessor());
        fieldProcessors.put("subtitle", new SubtitleFieldProcessor());
        fieldProcessors.put("unique", new UniqueFieldProcessor());
        fieldProcessors.put("side", new SideFieldProcessor());
        fieldProcessors.put("culture", new CultureFieldProcessor());
        fieldProcessors.put("type", new CardTypeFieldProcessor());
        fieldProcessors.put("race", new RaceFieldProcessor());
        fieldProcessors.put("itemclass", new PossessionClassFieldProcessor());
        fieldProcessors.put("keyword", new KeywordFieldProcessor());
        fieldProcessors.put("keywords", new KeywordFieldProcessor());
        fieldProcessors.put("twilight", new CostFieldProcessor());
        fieldProcessors.put("strength", new StrengthFieldProcessor());
        fieldProcessors.put("vitality", new VitalityFieldProcessor());
        fieldProcessors.put("resistance", new ResistanceFieldProcessor());
        fieldProcessors.put("signet", new SignetFieldProcessor());
        fieldProcessors.put("block", new SiteBlockFieldProcessor());
        fieldProcessors.put("site", new SiteNumberFieldProcessor());
        fieldProcessors.put("allyhome", new AllyHomeFieldProcessor());
        fieldProcessors.put("direction", new DirectionFieldProcessor());
        fieldProcessors.put("target", new TargetFieldProcessor());
        fieldProcessors.put("effects", new EffectFieldProcessor());

        fieldProcessors.put("gametext", new GameTextFieldProcessor());
        fieldProcessors.put("lore", new LoreFieldProcessor());
        fieldProcessors.put("promotext", new PromoTextFieldProcessor());

        //Soon!  But not yet
        fieldProcessors.put("cardinfo", new NullProcessor());
        fieldProcessors.put("alts", new NullProcessor());
    }

    public LotroCardBlueprint buildFromJson(JSONObject json) throws InvalidCardDefinitionException {
        BuiltLotroCardBlueprint result = new BuiltLotroCardBlueprint();

        Set<Map.Entry<String, Object>> values = json.entrySet();
        for (Map.Entry<String, Object> value : values) {
            final String field = value.getKey().toLowerCase();
            final Object fieldValue = value.getValue();
            final FieldProcessor fieldProcessor = fieldProcessors.get(field);
            if (fieldProcessor == null)
                throw new InvalidCardDefinitionException("Unrecognized field: " + field);

            fieldProcessor.processField(field, fieldValue, result, this);
        }

        result.validateConsistency();

        return result;
    }

    @Override
    public EffectAppenderFactory getEffectAppenderFactory() {
        return effectAppenderFactory;
    }

    @Override
    public FilterFactory getFilterFactory() {
        return filterFactory;
    }

    @Override
    public RequirementFactory getRequirementFactory() {
        return requirementFactory;
    }

    @Override
    public TriggerCheckerFactory getTriggerCheckerFactory() {
        return triggerCheckerFactory;
    }

    @Override
    public ModifierSourceFactory getModifierSourceFactory() {
        return modifierSourceFactory;
    }

    private static class NullProcessor implements FieldProcessor {
        @Override
        public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) {
            // Ignore
        }
    }
}
