package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class IsAdditionalCultureModifier extends AbstractModifier {
    private final Culture _culture;

    public IsAdditionalCultureModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Culture culture) {
        super(source, "Has additional culture - " + culture.toString(), affectFilter, condition, ModifierEffect.ADDITIONAL_CULTURE);
        _culture = culture;
    }


    public boolean isAdditionalCultureModifier(LotroGame game, PhysicalCard physicalCard, Culture culture) {
        return culture == _culture;
    }

    public Culture getCulture() {
        return _culture;
    }
}
