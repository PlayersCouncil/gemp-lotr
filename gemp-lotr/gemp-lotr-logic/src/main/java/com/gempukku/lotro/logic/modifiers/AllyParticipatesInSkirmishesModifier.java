package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public class AllyParticipatesInSkirmishesModifier extends AbstractModifier {
    private final PhysicalCard _source;

    public AllyParticipatesInSkirmishesModifier(PhysicalCard source, Condition condition, Filterable... affectFilters) {
        super(source, "Can participate in skirmishes", Filters.and(affectFilters), condition, ModifierEffect.PRESENCE_MODIFIER);
        _source = source;
    }

    @Override
    public boolean isAllyParticipateInSkirmishes(LotroGame game, Side sidePlayer, PhysicalCard card) {
        boolean unhasty = game.getModifiersQuerying().hasKeyword(game, card, Keyword.UNHASTY);
        return sidePlayer == Side.SHADOW
                || !unhasty || game.getModifiersQuerying().isCulture(game, _source, Culture.GANDALF);
    }
}
