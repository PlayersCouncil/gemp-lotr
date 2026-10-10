package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class EventAtTest extends AbstractAtTest {
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
