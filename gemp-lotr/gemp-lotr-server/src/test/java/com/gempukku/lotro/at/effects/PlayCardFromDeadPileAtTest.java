package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlayCardFromDeadPileAtTest extends AbstractAtTest {
    @Test
    public void playCardFromDeadPile() throws Exception {
        initializeSimplestGame();

        PhysicalCard wellMetIndeed = addToZone(createCard(P1, "4_106"), Zone.HAND);
        PhysicalCard aragorn = addToZone(createCard(P1, "1_89"), Zone.FREE_CHARACTERS);
        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard elrond = addToZone(createCard(P1, "1_40"), Zone.SUPPORT);
        PhysicalCard gandalf = addToZone(createCard(P1, "1_72"), Zone.DEAD);

        passUntil(Phase.FELLOWSHIP);

        selectCardAction(P1, wellMetIndeed);
        assertEquals(Zone.FREE_CHARACTERS, gandalf.getZone());
    }
}
