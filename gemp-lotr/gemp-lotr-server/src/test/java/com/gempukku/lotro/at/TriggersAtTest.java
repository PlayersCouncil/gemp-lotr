package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class TriggersAtTest extends AbstractAtTest {
    @Test
    public void replaceSiteNotPossibleWithMountDoom() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl gandalf = new PhysicalCardImpl(100, "1_72", P1, _cardLibrary.getLotroCardBlueprint("1_72"));
        PhysicalCardImpl traveledLeader = new PhysicalCardImpl(101, "12_34", P1, _cardLibrary.getLotroCardBlueprint("12_34"));
        PhysicalCardImpl mountDoom = new PhysicalCardImpl(102, "15_193", P2, _cardLibrary.getLotroCardBlueprint("15_193"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, traveledLeader, Zone.HAND);
        _game.getGameState().addCardToZone(_game, gandalf, Zone.FREE_CHARACTERS);

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().removeCardsFromZone(P2, Collections.singleton(_game.getGameState().getSite(2)));
        mountDoom.setSiteNumber(2);
        _game.getGameState().addCardToZone(_game, mountDoom, Zone.ADVENTURE_PATH);

        // End shadow phase
        playerDecided(P2, "");

        // End regroup phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        playerDecided(P1, "1");

        // Reconcile
        playerDecided(P1, "");

        playerDecided(P2, "");
        playerDecided(P1, "");
        playerDecided(P2, "");
        playerDecided(P1, "");

        // Reconcile
        playerDecided(P1, "");

        playerDecided(P2, "1");

        playerDecided(P1, "");
        playerDecided(P2, "");

        final AwaitingDecision regroupActionDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, regroupActionDecision.getDecisionType());
        validateContents(new String[]{"" + traveledLeader.getCardId()}, (String[]) regroupActionDecision.getDecisionParameters().get("cardId"));

        playerDecided(P1, "0");

        PhysicalCard siteOne = _game.getGameState().getSite(1);

        playerDecided(P1, "" + siteOne.getCardId());

        assertEquals(siteOne, _game.getGameState().getSite(1));
    }

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
    public void siteControlled() throws Exception {
        initializeSimplestGame();

        PhysicalCard youDoNotKnowPain = addToZone(createCard(P2, "17_138"), Zone.SUPPORT);
        PhysicalCard whiteHandIntruder = addToZone(createCard(P2, "17_127"), Zone.SHADOW_CHARACTERS);
        PhysicalCard gimli = addToZone(createCard(P1, "5_7"), Zone.FREE_CHARACTERS);
        for (int i = 0; i < 2; i++) {
            addToZone(createCard(P2, "17_119"), Zone.SHADOW_CHARACTERS);
        }

        passUntil(Phase.SHADOW);
        _game.getGameState().setPlayerPosition(P2, 2);

        passUntil(Phase.ASSIGNMENT);
        pass(P1);
        pass(P2);
        playerDecided(P1, gimli.getCardId() + " " + whiteHandIntruder.getCardId());
        pass(P2);
        selectCard(P1, gimli);

        pass(P1);
        pass(P2);
        selectCardAction(P2, whiteHandIntruder);
        assertEquals(2, getWounds(gimli));
        removeWounds(gimli, 1);
        assertEquals(1, getWounds(gimli));
        selectCardAction(P2, youDoNotKnowPain);
        assertEquals(2, getWounds(gimli));
    }

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
