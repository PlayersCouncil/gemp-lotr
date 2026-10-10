package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlayCardFromStackedAtTest extends AbstractAtTest {
    @Test
    public void playFromStacked() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard urukSavage = addToZone(createCard(P2, "1_151"), Zone.SHADOW_CHARACTERS);

        PhysicalCard greatestKingdomOfMyPeople = addToZone(createCard(P1, "1_16"), Zone.SUPPORT);
        PhysicalCard slakedThirsts = addToZone(createCard(P1, "7_14"), Zone.STACKED);
        ((PhysicalCardImpl) slakedThirsts).stackOn((PhysicalCardImpl) greatestKingdomOfMyPeople);

        passUntil(Phase.MANEUVER);

        selectCardAction(P1, slakedThirsts);
        assertEquals(2, getWounds(urukSavage));
        assertEquals(Zone.DISCARD, slakedThirsts.getZone());
    }
}
