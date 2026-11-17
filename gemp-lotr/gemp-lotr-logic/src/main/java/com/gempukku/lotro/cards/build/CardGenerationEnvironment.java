package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.cards.build.field.effect.EffectAppenderFactory;
import com.gempukku.lotro.cards.build.field.effect.filter.FilterFactory;
import com.gempukku.lotro.cards.build.field.effect.modifier.ModifierSourceFactory;
import com.gempukku.lotro.cards.build.field.effect.requirement.RequirementFactory;
import com.gempukku.lotro.cards.build.field.effect.trigger.TriggerCheckerFactory;

public interface CardGenerationEnvironment {
    EffectAppenderFactory getEffectAppenderFactory();

    FilterFactory getFilterFactory();

    RequirementFactory getRequirementFactory();

    TriggerCheckerFactory getTriggerCheckerFactory();

    ModifierSourceFactory getModifierSourceFactory();
    void recordCardVariant(String parentId, BuiltLotroCardBlueprint card);

    /**
     * Whether the named memory is already filled when the costs now being built are checked for payability:
     * filled by the trigger that sets the action off, or, for an added trigger, by the action that added it.
     * A cost that selects from such a memory can be checked before it is paid; a cost that selects from a
     * memory filled by an earlier cost of the same action cannot (see EffectUtils.validatePreEvaluate).
     */
    default boolean isMemoryFilledBeforeCosts(String memory) {
        return false;
    }
}
