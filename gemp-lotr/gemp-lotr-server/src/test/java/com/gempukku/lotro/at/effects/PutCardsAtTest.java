package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PutCardsAtTest extends AbstractAtTest {
    @Test
    public void putPlayedEventOnBottomOfDrawDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard tossMe = addToZone(createCard(P1, "6_11"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        PhysicalCard urukSavageInDeck = addToZone(createCard(P1, "1_151"), Zone.DECK);
        selectCardAction(P1, tossMe);
        playerDecided(P1, "1");

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P1);
        assertEquals(tossMe, deck.get(1));
    }

    @Test
    public void putPlayedEventOnTopOfDrawDeck() throws Exception {
        initializeSimplestGame();

        PhysicalCard tossMe = addToZone(createCard(P1, "6_11"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        PhysicalCard urukSavageInDeck = addToZone(createCard(P1, "1_151"), Zone.DECK);
        selectCardAction(P1, tossMe);
        playerDecided(P1, "0");

        List<? extends PhysicalCard> deck = _game.getGameState().getDeck(P1);
        assertEquals(tossMe, deck.get(0));
    }
}
