package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;

import java.util.Collections;

public class RemovePlayedEventFromTheGameEffect extends AbstractEffect {
    private final PhysicalCard card;

    public RemovePlayedEventFromTheGameEffect(PhysicalCard card) {
        this.card = card;
    }

    @Override
    public String getText(LotroGame game) {
        return "Remove " + GameUtils.getFullName(card) + " from the game";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        Zone zone = card.getZone();
        return zone == Zone.VOID || zone == Zone.VOID_FROM_HAND;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().sendMessage(card.getOwner() + " removes " + GameUtils.getCardLink(card) + " from the game");
            game.getGameState().removeCardsFromZone(card.getOwner(), Collections.singletonList(card));
            game.getGameState().addCardToZone(game, card, Zone.REMOVED);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}