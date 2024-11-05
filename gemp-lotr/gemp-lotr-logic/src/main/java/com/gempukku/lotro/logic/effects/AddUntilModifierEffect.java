package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.cards.build.field.effect.appender.resolver.TimeResolver;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collections;
import java.util.List;

public class AddUntilModifierEffect extends UnrespondableEffect {
    private final List<Modifier> _modifiers;
    private final TimeResolver.Time until;

    public AddUntilModifierEffect(List<Modifier> modifiers, TimeResolver.Time until) {
        _modifiers = modifiers;
        this.until = until;
    }

    public AddUntilModifierEffect(Modifier modifier, TimeResolver.Time until) {
        this(Collections.singletonList(modifier), until);
    }

    @Override
    public void doPlayEffect(LotroGame game) {
        Phase phase = until.getPhase();
        if (phase == null)
            phase = game.getGameState().getCurrentPhase();

        for (Modifier modifier : _modifiers) {
            if (until.isPermanent())
                game.getModifiersEnvironment().addAlwaysOnModifier(modifier);
            else if (until.isEndOfTurn())
                game.getModifiersEnvironment().addUntilEndOfTurnModifier(modifier);
            else if (until.isStart())
                game.getModifiersEnvironment().addUntilStartOfPhaseModifier(modifier, phase);
            else
                game.getModifiersEnvironment().addUntilEndOfPhaseModifier(modifier, phase);
        }
    }
}
