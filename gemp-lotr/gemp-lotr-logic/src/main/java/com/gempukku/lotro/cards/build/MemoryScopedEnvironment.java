package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.cards.build.field.effect.EffectAppenderFactory;
import com.gempukku.lotro.cards.build.field.effect.filter.FilterFactory;
import com.gempukku.lotro.cards.build.field.effect.modifier.ModifierSourceFactory;
import com.gempukku.lotro.cards.build.field.effect.requirement.RequirementFactory;
import com.gempukku.lotro.cards.build.field.effect.trigger.TriggerCheckerFactory;

import java.util.function.Predicate;

/**
 * The environment in which the costs of one action are built, when some memory is known to be filled before
 * those costs are checked. Everything else is the environment it wraps.
 */
public class MemoryScopedEnvironment implements CardGenerationEnvironment {
    private final CardGenerationEnvironment delegate;
    private final Predicate<String> filledBeforeCosts;

    /**
     * @param filledBeforeCosts answers for a memory name in lower case
     */
    public MemoryScopedEnvironment(CardGenerationEnvironment delegate, Predicate<String> filledBeforeCosts) {
        this.delegate = delegate;
        this.filledBeforeCosts = filledBeforeCosts;
    }

    @Override
    public boolean isMemoryFilledBeforeCosts(String memory) {
        return memory != null && filledBeforeCosts.test(memory.toLowerCase());
    }

    @Override
    public EffectAppenderFactory getEffectAppenderFactory() {
        return delegate.getEffectAppenderFactory();
    }

    @Override
    public FilterFactory getFilterFactory() {
        return delegate.getFilterFactory();
    }

    @Override
    public RequirementFactory getRequirementFactory() {
        return delegate.getRequirementFactory();
    }

    @Override
    public TriggerCheckerFactory getTriggerCheckerFactory() {
        return delegate.getTriggerCheckerFactory();
    }

    @Override
    public ModifierSourceFactory getModifierSourceFactory() {
        return delegate.getModifierSourceFactory();
    }

    @Override
    public void recordCardVariant(String parentId, BuiltLotroCardBlueprint card) {
        delegate.recordCardVariant(parentId, card);
    }
}
