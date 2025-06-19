package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.Collection;

public abstract class ChooseActiveCardEffect extends ChooseActiveCardsEffect {
    public ChooseActiveCardEffect(PhysicalCard source, String playerId, String choiceText, Filterable... filters) {
        super(source, playerId, choiceText, 1, 1, SpotOverride.NONE, filters);
    }

    protected abstract void cardSelected(LotroGame game, PhysicalCard card);

    @Override
    protected final void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
        if (cards.size() == 1)
            cardSelected(game, cards.iterator().next());
    }
}
