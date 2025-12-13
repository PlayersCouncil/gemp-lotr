package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;

import java.util.Collections;

public class PutPlayedEventIntoHandEffect extends AbstractEffect {
    private final PhysicalCard card;
    private final boolean overrideZone;

    public PutPlayedEventIntoHandEffect(PhysicalCard card) {
        this(card, false);
    }

    public PutPlayedEventIntoHandEffect(PhysicalCard card, boolean skipZoneChecks) {
        this.card = card;
        this.overrideZone = skipZoneChecks;
    }

    @Override
    public String getText(LotroGame game) {
        return "Put " + GameUtils.getFullName(card) + " into hand";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        Zone zone = card.getZone();
        return overrideZone || zone == Zone.VOID || zone == Zone.VOID_FROM_HAND;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().sendMessage(card.getOwner() + " puts " + GameUtils.getCardLink(card) + " back into hand");
            game.getGameState().removeCardsFromZone(card.getOwner(), Collections.singletonList(card));
            game.getGameState().addCardToZone(game, card, Zone.HAND);
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}