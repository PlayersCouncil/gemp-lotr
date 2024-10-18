package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BurdensAtTest extends AbstractAtTest {
    @Test
    public void preventAddingAllBurdens() throws Exception {
        initializeSimplestGame();

        PhysicalCard samwiseTheBrave = addToZone(createCard(P1, "4_316"), Zone.FREE_CHARACTERS);
        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard desperateDefenseOfTheRing = attachTo(createCard(P2, "1_244"), ringBearer);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        assertEquals(1, getBurdens());
        playerDecided(P1, ringBearer.getCardId() + " " + goblinRunner.getCardId());
        selectCardAction(P1, samwiseTheBrave);
        assertEquals(1, getBurdens());
    }
}
