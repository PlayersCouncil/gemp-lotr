package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoveFromGameAtTest extends AbstractAtTest {
    @Test
    public void removeCardsInDeadPileFromGame() throws Exception {
        initializeSimplestGame();

        PhysicalCard ruffian = addToZone(createCard(P2, "18_72"), Zone.SHADOW_CHARACTERS);
        PhysicalCard corsairHalberd = attachTo(createCard(P2, "18_63"), ruffian);
        PhysicalCard gimliInDeadPile = addToZone(createCard(P1, "5_7"), Zone.DEAD);

        passUntil(Phase.FELLOWSHIP);
        pass(P1);
        selectCardAction(P2, corsairHalberd);
        assertEquals(Zone.REMOVED, gimliInDeadPile.getZone());
    }

    @Test
    public void removeCardsInDeckFromGame() throws Exception {
        initializeSimplestGame();

        PhysicalCard grond = addToZone(createCard(P2, "18_82"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);
        PhysicalCard gimliInDeck = addToZone(createCard(P1, "5_7"), Zone.DECK);
        PhysicalCard aragornInDeck = addToZone(createCard(P1, "1_89"), Zone.DECK);

        passUntil(Phase.REGROUP);
        pass(P1);
        pass(P2);
        selectYes(P1);
        selectCardAction(P2, grond);
        pass(P2); //dismiss revealed deck cards
        assertEquals(Zone.REMOVED, gimliInDeck.getZone());
        assertEquals(Zone.REMOVED, aragornInDeck.getZone());
    }

    @Test
    public void removeFromTheGame() throws Exception {
        initializeSimplestGame();

        PhysicalCard ourTime = addToZone(createCard(P1, "18_24"), Zone.SUPPORT);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.MANEUVER);
        assertEquals(Zone.REMOVED, ourTime.getZone());
    }
}
