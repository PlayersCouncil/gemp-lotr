package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DiscountAtTest extends AbstractAtTest {
    @Test
    public void playSauronWithMinionExertDiscount() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl archerMinion = createCard(P2, "4_138");
        moveCardToZone(archerMinion, Zone.SHADOW_CHARACTERS);
        PhysicalCardImpl sauron = createCard(P2, "13_140");
        moveCardToZone(sauron, Zone.HAND);

        skipMulligans();

        setTwilightPool(15);

        // End fellowship phase
        assertEquals(Phase.FELLOWSHIP, getPhase());
        pass(P1);

        assertEquals(Phase.SHADOW, getPhase());
        selectCardAction(P2, sauron);

        assertEquals(Zone.SHADOW_CHARACTERS, sauron.getZone());
        assertEquals(0, getTwilightPool());
        assertEquals(1, getWounds(archerMinion));
    }

    @Test
    public void playSauronWithMinionExertDiscountCantPlayWithoutExert() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl sauron = createCard(P2, "13_140");
        moveCardToZone(sauron, Zone.HAND);

        skipMulligans();

        setTwilightPool(15);

        // End fellowship phase
        assertEquals(Phase.FELLOWSHIP, getPhase());
        pass(P1);

        assertEquals(Phase.SHADOW, getPhase());
        assertNull(getCardActionId(P2, sauron));
    }
}
