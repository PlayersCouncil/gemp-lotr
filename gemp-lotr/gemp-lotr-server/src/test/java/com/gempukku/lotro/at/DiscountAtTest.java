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

    @Test
    public void playHostOfUdunAtDiscard() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl hostOfUdun = createCard(P2, "7_282");
        moveCardToZone(hostOfUdun, Zone.HAND);

        skipMulligans();

        setTwilightPool(9);
        addThreats(P1, 1);

        // End fellowship phase
        assertEquals(Phase.FELLOWSHIP, getPhase());
        pass(P1);

        assertEquals(Phase.SHADOW, getPhase());
        selectCardAction(P2, hostOfUdun);

        assertEquals(Zone.SHADOW_CHARACTERS, hostOfUdun.getZone());
        assertEquals(0, getTwilightPool());
        assertEquals(0, getThreats());
    }

    @Test
    public void playHostOfUdunAtDiscardCantPlayWithoutThreats() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl hostOfUdun = createCard(P2, "7_282");
        moveCardToZone(hostOfUdun, Zone.HAND);

        skipMulligans();

        setTwilightPool(9);

        // End fellowship phase
        assertEquals(Phase.FELLOWSHIP, getPhase());
        pass(P1);

        assertEquals(Phase.SHADOW, getPhase());
        assertNull(getCardActionId(P2, hostOfUdun));
    }

    @Test
    public void playDefiledByRemovingFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl gimli = createCard(P1, "5_7");
        addToZone(gimli, Zone.FREE_CHARACTERS);
        PhysicalCardImpl goblinRunner = createCard(P2, "1_178");
        addToZone(goblinRunner, Zone.SHADOW_CHARACTERS);


        PhysicalCardImpl defiled = createCard(P2, "13_105");
        addToZone(defiled, Zone.HAND);

        for (int i = 0; i < 4; i++) {
            addToZone(createCard(P2, "13_105"), Zone.DISCARD);
        }

        skipMulligans();

        // End fellowship phase
        assertEquals(Phase.FELLOWSHIP, getPhase());
        pass(P1);

        setTwilightPool(0);

        // End shadow phase
        assertEquals(Phase.SHADOW, getPhase());
        pass(P2);

        // End maneuver phase
        pass(P1);
        pass(P2);

        // End archery phase
        pass(P1);
        pass(P2);

        // End assignment phase
        pass(P1);
        pass(P2);

        // Assign
        playerDecided(P1, gimli.getCardId() + " " + goblinRunner.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(gimli.getCardId()));
        pass(P1);

        assertEquals(Phase.SKIRMISH, getPhase());
        selectCardAction(P2, defiled);
        selectArbitraryCards(P2, "temp0", "temp1", "temp2", "temp3");

        assertEquals(0, getTwilightPool());
        assertEquals(Zone.DISCARD, defiled.getZone());
    }

    @Test
    public void playDefiledByRemovingFromDiscardCantPlayWithoutDiscount() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl gimli = createCard(P1, "5_7");
        addToZone(gimli, Zone.FREE_CHARACTERS);
        PhysicalCardImpl goblinRunner = createCard(P2, "1_178");
        addToZone(goblinRunner, Zone.SHADOW_CHARACTERS);

        PhysicalCardImpl defiled = createCard(P2, "13_105");
        addToZone(defiled, Zone.HAND);

        for (int i = 0; i < 3; i++) {
            addToZone(createCard(P2, "13_105"), Zone.DISCARD);
        }

        skipMulligans();

        // End fellowship phase
        assertEquals(Phase.FELLOWSHIP, getPhase());
        pass(P1);

        setTwilightPool(0);

        // End shadow phase
        assertEquals(Phase.SHADOW, getPhase());
        pass(P2);

        // End maneuver phase
        pass(P1);
        pass(P2);

        // End archery phase
        pass(P1);
        pass(P2);

        // End assignment phase
        pass(P1);
        pass(P2);

        // Assign
        playerDecided(P1, gimli.getCardId() + " " + goblinRunner.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(gimli.getCardId()));
        pass(P1);

        assertEquals(Phase.SKIRMISH, getPhase());
        assertNull(getCardActionId(P2, defiled));
    }
}
