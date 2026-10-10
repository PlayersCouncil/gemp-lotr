package com.gempukku.lotro.at.effects;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExchangeCardsAtTest extends AbstractAtTest {
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
