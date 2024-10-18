package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class Card_01_224_Tests
{

    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>()
                {{
                    put("rtim", "1_224");
                    put("enquea", "2_83");
                    put("twk", "1_237");

                    put("guard", "1_7");
                }},
                GenericCardTestHelper.FellowshipSites,
                GenericCardTestHelper.FOTRFrodo,
                GenericCardTestHelper.ATARRing
        );
    }

    @Test
    public void ReturntoItsMasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1
         * Name: Return to Its Master
         * Unique: False
         * Side: Shadow
         * Culture: Wraith
         * Twilight Cost: 0
         * Type: Event
         * Subtype:
         * Game Text: <b>Response:</b> If the Ring-bearer wears The One Ring at the end of a skirmish phase, cancel all remaining assignments and assign a Nazgûl to skirmish the Ring-bearer; The One Ring's game text does not apply during this skirmish.
         */

        var scn = GetScenario();

        var card = scn.GetFreepsCard("rtim");

        assertEquals("Return to Its Master", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertFalse(card.getBlueprint().isUnique());
        assertEquals(Side.SHADOW, card.getBlueprint().getSide());
        assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
        assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasKeyword(card, Keyword.RESPONSE));
        assertEquals(0, card.getBlueprint().getTwilightCost());
    }

    @Test
    public void ReturntoItsMasterPermitsAssigningOfNonFierceNazgulWhenUsedDuringFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var rtim = scn.GetShadowCard("rtim");
        var enquea = scn.GetShadowCard("enquea");
        var twk = scn.GetShadowCard("twk");
        scn.ShadowMoveCardToHand(rtim);
        scn.ShadowMoveCharToTable(enquea, twk);

        var frodo = scn.GetRingBearer();
        var onering = scn.GetOneRing();
        var guard = scn.GetFreepsCard("guard");
        scn.FreepsMoveCharToTable(guard);

        scn.StartGame();

        //Enquea is the twilight version, who is not fierce.  He should still be a valid target
        // when RTIM triggers during a fierce skirmish.

        scn.SkipToAssignments();
        //We will skip the non-fierce skirmishes
        scn.FreepsDeclineAssignments();
        assertEquals(2, scn.ShadowGetShadowAssignmentTargetCount()); // both enquea and TWK
        scn.ShadowDeclineAssignments();

        //Fierce skirmish assignment phase
        scn.PassCurrentPhaseActions();
        scn.FreepsDeclineAssignments();
        assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount()); // TWK, but not enquea
        scn.ShadowAssignToMinions(guard, twk);
        scn.FreepsResolveSkirmish(guard);

        //Need the One Ring to be worn
        scn.FreepsUseCardAction(onering);
        scn.ShadowPassCurrentPhaseAction();
        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.ShadowPlayAvailable(rtim));
        scn.ShadowPlayCard(rtim);

        //Can't quite get the rest

        assertEquals(2, scn.GetShadowCardChoiceCount()); // both enquea and TWK
        scn.ShadowChooseCard(enquea);
    }


    //Legacy AT test
    @Test
    public void returnToItsMaster() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var p1Deck = scn.createSimplestDeck();
        p1Deck.setRing("4_1");
        var p2Deck = scn.createSimplestDeck();

        Map<String, LotroDeck> decks = new HashMap<>();
        decks.put(scn.P1, p1Deck);
        decks.put(scn.P2, p2Deck);

        scn.initializeGameWithDecks(decks);

        scn.skipMulligans();

        PhysicalCardImpl returnToItsMaster = new PhysicalCardImpl(102, "1_224", scn.P2, scn._cardLibrary.getLotroCardBlueprint("1_224"));
        scn._game.getGameState().addCardToZone(scn._game, returnToItsMaster, Zone.HAND);

        PhysicalCardImpl nelya = new PhysicalCardImpl(102, "1_233", scn.P2, scn._cardLibrary.getLotroCardBlueprint("1_233"));
        scn._game.getGameState().addCardToZone(scn._game, nelya, Zone.SHADOW_CHARACTERS);

        PhysicalCardImpl hobbitSword = new PhysicalCardImpl(102, "1_299", scn.P1, scn._cardLibrary.getLotroCardBlueprint("1_299"));
        scn._game.getGameState().attachCard(scn._game, hobbitSword, scn._game.getGameState().getRingBearer(scn.P1));

        // End fellowship
        scn.playerDecided(scn.P1, "");

        // End shadow
        scn.playerDecided(scn.P2, "");

        // End maneuvers phase
        scn.playerDecided(scn.P1, "");
        scn.playerDecided(scn.P2, "");

        // End archery phase
        scn.playerDecided(scn.P1, "");
        scn.playerDecided(scn.P2, "");

        // End assignment phase
        scn.playerDecided(scn.P1, "");
        scn.playerDecided(scn.P2, "");

        // Assign
        scn.playerDecided(scn.P1, scn._game.getGameState().getRingBearer(scn.P1).getCardId() + " " + nelya.getCardId());

        // Choose skirmish to resolve
        scn.playerDecided(scn.P1, "" + scn._game.getGameState().getRingBearer(scn.P1).getCardId());

        // Skirmish phase
        AwaitingDecision skirmishAction = scn._userFeedback.getAwaitingDecision(scn.P1);
        scn.playerDecided(scn.P1, scn.getCardActionId(skirmishAction, "Use The One"));

        assertTrue(scn._game.getGameState().isWearingRing());

        // End skirmish phase
        scn.playerDecided(scn.P2, "");
        scn.playerDecided(scn.P1, "");

        // Don't use Return to Its Master
        scn.playerDecided(scn.P2, "");

        // End assignment phase
        scn.playerDecided(scn.P1, "");
        scn.playerDecided(scn.P2, "");

        // Assign
        scn.playerDecided(scn.P1, scn._game.getGameState().getRingBearer(scn.P1).getCardId() + " " + nelya.getCardId());

        // Choose skirmish to resolve
        scn.playerDecided(scn.P1, "" + scn._game.getGameState().getRingBearer(scn.P1).getCardId());

        // End fierce skirmish phase
        scn.playerDecided(scn.P1, "");
        scn.playerDecided(scn.P2, "");

        AwaitingDecision playeReturnDecision = scn._userFeedback.getAwaitingDecision(scn.P2);
        scn.playerDecided(scn.P2, scn.getCardActionId(playeReturnDecision, "Play Return"));

        // Choose skirmish to resolve
        scn.playerDecided(scn.P1, "" + scn._game.getGameState().getRingBearer(scn.P1).getCardId());

        assertEquals(scn._game.getGameState().getRingBearer(scn.P1), scn._game.getGameState().getSkirmish().getFellowshipCharacter());
        assertEquals(1, scn._game.getGameState().getSkirmish().getShadowCharacters().size());
        assertEquals(nelya, scn._game.getGameState().getSkirmish().getShadowCharacters().iterator().next());
    }

}
