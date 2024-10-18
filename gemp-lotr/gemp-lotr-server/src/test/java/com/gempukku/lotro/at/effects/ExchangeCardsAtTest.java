package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExchangeCardsAtTest extends AbstractAtTest {
    @Test
    public void exchangeCardsInHandWithCardsInDeadPile() throws Exception {
        initializeSimplestGame();

        PhysicalCard gandalf = addToZone(createCard(P1, "1_72"), Zone.FREE_CHARACTERS);
        PhysicalCard borneFarAway = addToZone(createCard(P1, "10_14"), Zone.HAND);
        PhysicalCard aragornInDeadPile = addToZone(createCard(P1, "1_89"), Zone.DEAD);
        PhysicalCard gimliInHand = addToZone(createCard(P1, "5_7"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        selectCardAction(P1, borneFarAway);
        assertEquals(Zone.HAND, aragornInDeadPile.getZone());
        assertEquals(Zone.DEAD, gimliInHand.getZone());
    }

    @Test
    public void exchangeCardsInHandWithCardsInDiscard() throws Exception {
        initializeSimplestGame();

        PhysicalCard gandalf = addToZone(createCard(P1, "1_72"), Zone.FREE_CHARACTERS);
        PhysicalCard borneFarAway = addToZone(createCard(P1, "10_14"), Zone.HAND);
        PhysicalCard aragornInDiscard = addToZone(createCard(P1, "1_89"), Zone.DISCARD);
        PhysicalCard gimliInHand = addToZone(createCard(P1, "5_7"), Zone.HAND);

        passUntil(Phase.FELLOWSHIP);
        selectCardAction(P1, borneFarAway);
        assertEquals(Zone.HAND, aragornInDiscard.getZone());
        assertEquals(Zone.DISCARD, gimliInHand.getZone());
    }

    @Test
    public void exchangeCardsInHandWithCardsStacked() throws Exception {
        initializeSimplestGame();

        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        PhysicalCard hallOfOurFathers = addToZone(createCard(P1, "11_11"), Zone.SUPPORT);
        PhysicalCard gandalfStacked = stackOn(createCard(P1, "1_72"), hallOfOurFathers);
        PhysicalCard aragornInHand = addToZone(createCard(P1, "1_89"), Zone.HAND);
        PhysicalCard goblinRunner = addToZone(createCard(P2, "1_178"), Zone.SHADOW_CHARACTERS);

        passUntil(Phase.MANEUVER);
        selectCardAction(P1, hallOfOurFathers);
        assertEquals(Zone.HAND, gandalfStacked.getZone());
        assertEquals(Zone.STACKED, aragornInHand.getZone());
    }
}
