package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class AllyParticipatesInArcheryFireModifier extends AbstractModifier {
    public AllyParticipatesInArcheryFireModifier(PhysicalCard source, Filterable affectFilter, Condition condition) {
        super(source, "Can participate in archery fire", affectFilter, condition, ModifierEffect.PRESENCE_MODIFIER);
    }

    @Override
    public boolean isAllyParticipateInArcheryFire(LotroGame game, PhysicalCard card) {
        return true;
    }
}