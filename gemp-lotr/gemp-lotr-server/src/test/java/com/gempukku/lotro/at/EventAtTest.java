package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EventAtTest extends AbstractAtTest {
    @Test
    public void cancelEvent() throws Exception {
        initializeSimplestGame();

        PhysicalCard aragorn = addToZone(createCard(P1, "1_89"), Zone.FREE_CHARACTERS);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);
        PhysicalCard strengthOfKings = addToZone(createCard(P1, "1_115"), Zone.HAND);
        PhysicalCard unfamiliarTerritory = addToZone(createCard(P2, "1_201"), Zone.HAND);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, aragorn.getCardId() + " " + goblinRunner.getCardId());
        selectCard(P1, aragorn);
        pass(P1);
        assertEquals(5, getStrength(goblinRunner));
        selectCardAction(P2, unfamiliarTerritory);
        selectCardAction(P1, strengthOfKings);
        assertEquals(5, getStrength(goblinRunner));
    }

    @Test
    public void canPlayNextAction() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard barukKhazad = addToZone(createCard(P1, "5_5"), Zone.HAND);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.MANEUVER);
        selectCardAction(P1, barukKhazad);
        assertNotNull(getAwaitingDecision(P1));
    }
}
