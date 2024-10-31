package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PlayCardInOtherPhaseAtTest extends AbstractAtTest {
    @Test
    public void playErkenbrandDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCard erkebrand = addToZone(createCard(P1, "0_59"), Zone.HAND);
        PhysicalCard soldierOfEdoras = addToZone(createCard(P1, "4_262"), Zone.SUPPORT);

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.FELLOWSHIP);

        assertNotNull(getCardActionId(P1, erkebrand));

        passUntil(Phase.ASSIGNMENT);

        // Assign
        pass(P1);
        pass(P2);
        playerDecided(P1, gimli.getCardId() + " " + goblinRunner.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(gimli.getCardId()));
        selectCardAction(P1, erkebrand);
        assertEquals(Zone.FREE_CHARACTERS, erkebrand.getZone());
    }
}
