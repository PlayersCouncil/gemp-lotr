package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class AllyParticipatesInArcheryFireAndSkirmishesModifier extends AbstractModifier {
    private final PhysicalCard _source;

    public AllyParticipatesInArcheryFireAndSkirmishesModifier(PhysicalCard source, Filterable affectFilter, Condition condition) {
        super(source, "Can participate in archery and skirmishes", affectFilter, condition, ModifierEffect.PRESENCE_MODIFIER);
        _source = source;
    }

    @Override
    public boolean isAllyParticipateInArcheryFire(LotroGame game, PhysicalCard card) {
        return true;
    }

    @Override
    public boolean isAllyParticipateInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card) {
        boolean unhasty = game.getModifiersQuerying().hasKeyword(game, card, Keyword.UNHASTY);
        return sidePlayer == Side.SHADOW
                || !unhasty || _source.getBlueprint().getCulture() == Culture.GANDALF;
    }
}
