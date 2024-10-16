package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RequirementsAtTest extends AbstractAtTest {
    @Test
    public void canSpotCultureTokens() throws Exception {
        initializeSimplestGame();

        PhysicalCard elvenDefender = addToZone(createCard(P1, "18_9"), Zone.FREE_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);
        assertEquals(5, getStrength(elvenDefender));
        addCultureTokens(elvenDefender, Token.ELVEN, 1);
        assertEquals(7, getStrength(elvenDefender));
    }
}
