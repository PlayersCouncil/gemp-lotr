package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DiscardStackedCardsEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final Collection<? extends PhysicalCard> _cards;

    public DiscardStackedCardsEffect(PhysicalCard source, Collection<? extends PhysicalCard> cards) {
        _source = source;
        _cards = cards;
    }

    @Override
    public String getText(LotroGame game) {
        return "Discard stacked - " + getAppendedTextNames(_cards);
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        for (PhysicalCard card : _cards) {
            if (card.getZone() != Zone.STACKED)
                return false;
        }
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        GameState gameState = game.getGameState();

        Set<PhysicalCard> toDiscard = new HashSet<>();
        for (PhysicalCard card : _cards) {
            if (card.getZone() == Zone.STACKED)
                toDiscard.add(card);
        }

        if (toDiscard.size() > 0)
            gameState.sendMessage(getAppendedNames(toDiscard) + " " + GameUtils.be(toDiscard) + " discarded from being stacked");
        gameState.removeCardsFromZone(_source.getOwner(), toDiscard);
        for (PhysicalCard card : toDiscard)
            gameState.addCardToZone(game, card, Zone.DISCARD);

        return new FullEffectResult(toDiscard.size() == _cards.size());
    }
}
