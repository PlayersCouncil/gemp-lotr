package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Signet;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class AddSignetModifier extends AbstractModifier {
    private final Signet _signet;

    public AddSignetModifier(PhysicalCard source, Filterable affectFilter, Condition condition, Signet signet) {
        super(source, "Has signet " + signet, affectFilter, condition, ModifierEffect.SIGNET_MODIFIER);
        _signet = signet;
    }

    @Override
    public boolean hasSignet(LotroGame game, PhysicalCard physicalCard, Signet signet) {
        return (_signet == signet);
    }
}
