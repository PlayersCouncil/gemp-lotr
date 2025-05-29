package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_224_Tests
{

    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>()
                {{
                    put("rtim", "1_224");
                    put("nelya", "1_233");
                    put("enquea", "2_83"); //twilight
                    put("twk", "1_237");

                    put("guard", "1_7");
                    put("sword", "1_299");
                }},
                VirtualTableScenario.FellowshipSites,
                VirtualTableScenario.FOTRFrodo,
                VirtualTableScenario.ATARRing
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
         * Game Text: <b>Response:</b> If the Ring-bearer wears The One Ring at the end of a skirmish phase,
         * cancel all remaining assignments and assign a Nazgûl to skirmish the Ring-bearer;
         * The One Ring's game text does not apply during this skirmish.
         */

        var scn = GetScenario();

        var card = scn.GetFreepsCard("rtim");

        assertEquals("Return to Its Master", card.getBlueprint().getTitle());
        assertNull(card.getBlueprint().getSubtitle());
        assertFalse(card.getBlueprint().isUnique());
        assertEquals(Side.SHADOW, card.getBlueprint().getSide());
        assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
        assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
        assertEquals(0, card.getBlueprint().getTwilightCost());
    }

    @Test
    public void ReturnToItsMasterTriggersDuringNormalSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var ring = scn.GetRing();
        var sword = scn.GetFreepsCard("sword");
        scn.AttachCardsTo(frodo, sword);

        var rtim = scn.GetShadowCard("rtim");
        var nelya = scn.GetShadowCard("nelya");
        scn.MoveCardsToHand(rtim);
        scn.MoveMinionsToTable(nelya);

        scn.StartGame();

        scn.SkipToAssignments();
        scn.FreepsAssignAndResolve(frodo, nelya);
        scn.FreepsUseCardAction(ring);

        assertTrue(scn.RBWearingOneRing());

        scn.ShadowPass();
        scn.FreepsPass();

        assertTrue(scn.ShadowHasOptionalTriggerAvailable());
        scn.ShadowAcceptOptionalTrigger();

        scn.FreepsResolveSkirmish(frodo);

        assertTrue(scn.IsCharSkirmishing(frodo));
        assertTrue(scn.IsCharSkirmishing(nelya));
    }

    //Converted from Legacy AT test
    @Test
    public void ReturnToItsMasterTriggersDuringFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var frodo = scn.GetRingBearer();
        var ring = scn.GetRing();
        var sword = scn.GetFreepsCard("sword");
        scn.AttachCardsTo(frodo, sword);

        var rtim = scn.GetShadowCard("rtim");
        var nelya = scn.GetShadowCard("nelya");
        scn.MoveCardsToHand(rtim);
        scn.MoveMinionsToTable(nelya);

        scn.StartGame();

        scn.SkipToAssignments();
        scn.FreepsAssignAndResolve(frodo, nelya);
        scn.FreepsUseCardAction(ring);

        assertTrue(scn.RBWearingOneRing());

        scn.ShadowPass();
        scn.FreepsPass();

        assertTrue(scn.ShadowHasOptionalTriggerAvailable());
        scn.ShadowDeclineOptionalTrigger();

        scn.PassFierceAssignmentActions();
        scn.FreepsAssignAndResolve(frodo, nelya);

        assertTrue(scn.RBWearingOneRing());
        scn.PassSkirmishActions();

        assertTrue(scn.ShadowHasOptionalTriggerAvailable());
        scn.ShadowAcceptOptionalTrigger();

        scn.FreepsResolveSkirmish(frodo);

        assertTrue(scn.IsCharSkirmishing(frodo));
        assertTrue(scn.IsCharSkirmishing(nelya));
    }

    @Test
    public void ReturntoItsMasterPermitsAssigningOfNonFierceNazgulWhenUsedDuringFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var rtim = scn.GetShadowCard("rtim");
        var enquea = scn.GetShadowCard("enquea");
        var twk = scn.GetShadowCard("twk");
        scn.MoveCardsToHand(rtim);
        scn.MoveMinionsToTable(enquea, twk);

        var frodo = scn.GetRingBearer();
        var onering = scn.GetRing();
        var guard = scn.GetFreepsCard("guard");
        scn.MoveCompanionToTable(guard);

        scn.StartGame();

        //Enquea is the twilight version, who is not fierce.  He should still be a valid target
        // when RTIM triggers during a fierce skirmish.

        scn.SkipToShadowAssignments();
        assertEquals(2, scn.ShadowGetShadowAssignmentTargetCount()); // both enquea and TWK
        //We will skip the non-fierce skirmishes
        scn.ShadowDeclineAssignments();

        //Fierce skirmish assignment phase
        scn.SkipToShadowAssignments();
        assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount()); // TWK, but not enquea
        scn.ShadowAssignToMinions(guard, twk);
        scn.FreepsResolveSkirmish(guard);

        //Need the One Ring to be worn
        scn.FreepsUseCardAction(onering);
        scn.ShadowPassCurrentPhaseAction();
        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.ShadowPlayAvailable(rtim));
        scn.ShadowPlayCard(rtim);

        assertEquals(2, scn.ShadowGetCardChoiceCount()); // both enquea and TWK
        scn.ShadowChooseCard(enquea);

        assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
        assertTrue(scn.IsCharAssigned(enquea));
    }




}
