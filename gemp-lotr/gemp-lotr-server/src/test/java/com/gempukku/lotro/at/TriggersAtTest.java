package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import static org.junit.Assert.*;

public class TriggersAtTest extends AbstractAtTest {
    @Test
    public void takesOffRing() throws Exception {
        initializeSimplestGame();

        PhysicalCard iSeeYou = addToZone(createCard(P2, "101_46"), Zone.SUPPORT);
        PhysicalCard gollum = addToZone(createCard(P2, "7_58"), Zone.SHADOW_CHARACTERS);

        PhysicalCard ringBearer = getRingBearer(P1);
        PhysicalCard ring = getRing(P1);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, ringBearer.getCardId() + " " + gollum.getCardId());
        selectCard(P1, ringBearer);
        pass(P1);
        pass(P2);
        assertEquals(Zone.SUPPORT, iSeeYou.getZone());
        selectCardAction(P1, ring);
        assertEquals(Zone.DISCARD, iSeeYou.getZone());
    }

    // The card used in this example was reworked
//    @Test
//    public void siteLiberated() throws Exception {
//        initializeSimplestGame();
//
//        PhysicalCard drivenIntoTheHills = addToZone(createCard(P2, "102_1"), Zone.SUPPORT);
//        PhysicalCard theodenSonOfThengel = addToZone(createCard(P1, "4_292"), Zone.FREE_CHARACTERS);
//        PhysicalCard guma = addToZone(createCard(P1, "4_277"), Zone.SUPPORT);
//
//        passUntil(Phase.SHADOW);
//        _game.getGameState().takeControlOfCard(P2, _game, _game.getGameState().getSite(1), Zone.SUPPORT);
//        _game.getGameState().setPlayerPosition(P2, 2);
//
//        passUntil(Phase.REGROUP);
//        assertEquals(Zone.SUPPORT, drivenIntoTheHills.getZone());
//        selectCardAction(P1, theodenSonOfThengel);
//        assertEquals(Zone.DISCARD, drivenIntoTheHills.getZone());
//    }

    @Test
    public void fpDecidedToStay() throws Exception {
        initializeSimplestGame();

        PhysicalCard veryNiceFriends = addToZone(createCard(P1, "7_76"), Zone.HAND);
        PhysicalCard smeagol = addToZone(createCard(P1, "7_71"), Zone.DISCARD);
        PhysicalCard legolas1 = addToZone(createCard(P1, "1_50"), Zone.HAND);

        passUntil(Phase.REGROUP);
        selectCardAction(P1, veryNiceFriends);
        pass(P2);
        pass(P1);
        selectNo(P1);
        assertEquals(Zone.DISCARD, legolas1.getZone());
    }

    @Test
    public void exerted() throws Exception {
        initializeSimplestGame();

        PhysicalCard demandsOfSackvilleBagginses = addToZone(createCard(P2, "2_40"), Zone.SUPPORT);
        PhysicalCard theGaffer = addToZone(createCard(P1, "1_291"), Zone.SUPPORT);

        passUntil(Phase.FELLOWSHIP);
        assertEquals(0, getTwilightPool());
        selectCardAction(P1, theGaffer);
        assertEquals(1, getTwilightPool());
    }
}
