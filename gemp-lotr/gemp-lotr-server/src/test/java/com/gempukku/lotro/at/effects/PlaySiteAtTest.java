package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PlaySiteAtTest extends AbstractAtTest {
    @Test
    public void playNextSite() throws Exception {
        initializeSimplestGame();

        PhysicalCard aragorn = addToZone(createCard(P1, "1_89"), Zone.FREE_CHARACTERS);
        PhysicalCard pathfinder = addToZone(createCard(P1, "1_110"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        selectCardAction(P1, pathfinder);
        assertNotNull(getSite(2));
        assertEquals(P1, getSite(2).getOwner());
    }

    @Test
    public void playSite() throws Exception {
        initializeSimplestGame();

        PhysicalCard getOndAndGetAway = addToZone(createCard(P1, "4_304"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        selectCardAction(P1, getOndAndGetAway);
        assertEquals(P1, getSite(2).getOwner());
        assertEquals(P1, getSite(3).getOwner());
    }
}
