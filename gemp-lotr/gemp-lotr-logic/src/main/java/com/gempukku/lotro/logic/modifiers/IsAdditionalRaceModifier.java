package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class IsAdditionalRaceModifier extends AbstractModifier {
    private final Race _race;

    public IsAdditionalRaceModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Race race) {
        super(source, "Has additional race - " + race.toString(), affectFilter, condition, ModifierEffect.ADDITIONAL_RACE);
        _race = race;
    }

    @Override
    public boolean isAdditionalRaceModifier(LotroGame game, PhysicalCard physicalCard, Race race) {
        return race == _race;
    }

    public Race getRace() {
        return _race;
    }
}
