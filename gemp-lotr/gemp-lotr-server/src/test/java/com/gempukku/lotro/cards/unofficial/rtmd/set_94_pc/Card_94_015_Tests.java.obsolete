//package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;
//
//import com.gempukku.lotro.common.CardType;
//import com.gempukku.lotro.common.Phase;
//import com.gempukku.lotro.framework.VirtualTableScenario;
//import com.gempukku.lotro.game.CardNotFoundException;
//import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
//import org.junit.Test;
//
//import java.util.HashMap;
//
//import static org.junit.Assert.*;
//
//public class Card_94_015_Tests {
//
//	private final HashMap<String, String> cards = new HashMap<>() {{
//		put("marksman", "1_176");   // Goblin Marksman: Archer, adds 1 to the archery total
//		put("marksman2", "1_176");  // second Archer, so archery produces two separate wound events
//		put("commander", "1_186");  // Guard Commander: strength 7, wins a skirmish against Frodo
//	}};
//
//	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
//		return new VirtualTableScenario(cards,
//				VirtualTableScenario.FellowshipSites,
//				VirtualTableScenario.FOTRFrodo,
//				VirtualTableScenario.RulingRing,
//				"94_15", null
//		);
//	}
//
//	protected VirtualTableScenario GetBaneScenario() throws CardNotFoundException, DecisionResultInvalidException {
//		return new VirtualTableScenario(cards,
//				VirtualTableScenario.FellowshipSites,
//				VirtualTableScenario.FOTRFrodo,
//				VirtualTableScenario.IsildursBaneRing,
//				"94_15", null
//		);
//	}
//
//	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
//		return new VirtualTableScenario(cards,
//				VirtualTableScenario.FellowshipSites,
//				VirtualTableScenario.FOTRFrodo,
//				VirtualTableScenario.RulingRing,
//				null, "94_15"
//		);
//	}
//
//	@Test
//	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
//		/**
//		 * Set: RTMD 94
//		 * Name: Race Text 94_15
//		 * Type: MetaSite
//		 * Game Text: While wearing The One Ring, each time the Ring-bearer is about to take a
//		 * 		wound (except during a skirmish), its owner may add a burden instead.
//		 * 		Response: If the Ring-bearer is about to take a wound, its owner may put on The One Ring
//		 * 		until the regroup phase.
//		 */
//
//		var scn = GetFreepsScenario();
//		var mod = scn.GetFreepsCard("mod");
//		assertEquals("Race Text 94_15", mod.getBlueprint().getTitle());
//		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
//		assertEquals(1, mod.getBlueprint().getIntensity());
//	}
//
//	@Test
//	public void OwnerMayPutOnTheRingInResponseToTheRingBearerAboutToBeWoundedButTheWoundStillHappens() throws DecisionResultInvalidException, CardNotFoundException {
//		var scn = GetFreepsScenario();
//		var mod = scn.GetFreepsCard("mod");
//		var marksman = scn.GetShadowCard("marksman");
//		var frodo = scn.GetRingBearer();
//
//		scn.MoveMinionsToTable(marksman);
//		scn.StartGame();
//
//		scn.SkipToPhase(Phase.ARCHERY);
//		scn.PassCurrentPhaseActions();
//
//		assertFalse(scn.RBWearingOneRing());
//		assertEquals(0, scn.GetWoundsOn(frodo));
//
//		assertTrue(scn.FreepsHasOptionalTriggerAvailable("put on The One Ring"));
//		scn.FreepsAcceptOptionalTrigger();
//
//		assertTrue(scn.RBWearingOneRing());
//		// This clause only puts the ring on - it does not negate the wound. Only the conversion
//		// clause (tested below) can do that, and only outside a skirmish.
//		assertEquals(1, scn.GetWoundsOn(frodo));
//	}
//
//	@Test
//	public void OwnerMayDeclineAndTheWoundStillHappensWithoutTheRingGoingOn() throws DecisionResultInvalidException, CardNotFoundException {
//		var scn = GetFreepsScenario();
//		var mod = scn.GetFreepsCard("mod");
//		var marksman = scn.GetShadowCard("marksman");
//		var frodo = scn.GetRingBearer();
//
//		scn.MoveMinionsToTable(marksman);
//		scn.StartGame();
//
//		scn.SkipToPhase(Phase.ARCHERY);
//		scn.PassCurrentPhaseActions();
//
//		assertTrue(scn.FreepsHasOptionalTriggerAvailable("put on The One Ring"));
//		scn.FreepsDeclineOptionalTrigger();
//
//		assertFalse(scn.RBWearingOneRing());
//		assertEquals(1, scn.GetWoundsOn(frodo));
//	}
//
//	@Test
//	public void RingPutOnByTheResponseComesOffAtRegroup() throws DecisionResultInvalidException, CardNotFoundException {
//		var scn = GetFreepsScenario();
//		var mod = scn.GetFreepsCard("mod");
//		var marksman = scn.GetShadowCard("marksman");
//
//		scn.MoveMinionsToTable(marksman);
//		scn.StartGame();
//
//		scn.SkipToPhase(Phase.ARCHERY);
//		scn.PassCurrentPhaseActions();
//		assertTrue(scn.FreepsHasOptionalTriggerAvailable("put on The One Ring"));
//		scn.FreepsAcceptOptionalTrigger();
//
//		assertTrue(scn.RBWearingOneRing());
//
//		scn.SkipToPhase(Phase.REGROUP);
//		assertFalse(scn.RBWearingOneRing());
//	}
//
//	@Test
//	public void WhileWornOutsideASkirmishOwnerMayConvertTheWoundToABurdenInsteadAndTheWoundIsPrevented() throws DecisionResultInvalidException, CardNotFoundException {
//		var scn = GetFreepsScenario();
//		var mod = scn.GetFreepsCard("mod");
//		var marksman = scn.GetShadowCard("marksman");
//		var marksman2 = scn.GetShadowCard("marksman2");
//		var frodo = scn.GetRingBearer();
//
//		scn.MoveMinionsToTable(marksman, marksman2);
//		scn.StartGame();
//
//		scn.SkipToPhase(Phase.ARCHERY);
//		scn.PassCurrentPhaseActions();
//
//		// Two archers -> archery total 2 -> two separate wound events on the lone Ring-bearer.
//		// First wound: the ring is not on yet, so only the put-on clause applies.
//		int burdens = scn.GetBurdens();
//		assertFalse(scn.RBWearingOneRing());
//		assertTrue(scn.FreepsHasOptionalTriggerAvailable("put on The One Ring"));
//		scn.FreepsAcceptOptionalTrigger();
//		assertTrue(scn.RBWearingOneRing());
//		assertEquals(1, scn.GetWoundsOn(frodo));
//
//		// Second wound: the ring is now on and we are still outside a skirmish (archery), so the
//		// conversion clause is now also available alongside the (redundant) put-on clause.
//		assertTrue(scn.FreepsHasOptionalTriggerAvailable("add a burden instead of the wound"));
//		assertTrue(scn.FreepsHasOptionalTriggerAvailable("put on The One Ring"));
//		scn.FreepsChooseAction("add a burden instead of the wound");
//		if (scn.FreepsHasOptionalTriggerAvailable())
//			scn.FreepsDeclineOptionalTrigger();
//
//		// The wound is prevented (still just 1 on Frodo) and a burden was added instead.
//		assertEquals(1, scn.GetWoundsOn(frodo));
//		assertEquals(burdens + 1, scn.GetBurdens());
//	}
//
//	@Test
//	public void NoConversionDuringASkirmishEvenWhileWearingTheRing() throws DecisionResultInvalidException, CardNotFoundException {
//		var scn = GetBaneScenario();
//		var mod = scn.GetFreepsCard("mod");
//		var marksman = scn.GetShadowCard("marksman");
//		var commander = scn.GetShadowCard("commander");
//		var frodo = scn.GetRingBearer();
//
//		scn.MoveMinionsToTable(marksman, commander);
//		scn.StartGame();
//
//		// Put the ring on during archery (outside a skirmish) via the response clause.
//		scn.SkipToPhase(Phase.ARCHERY);
//		scn.PassCurrentPhaseActions();
//		assertTrue(scn.FreepsHasOptionalTriggerAvailable("put on The One Ring"));
//		scn.FreepsAcceptOptionalTrigger();
//		assertTrue(scn.RBWearingOneRing());
//		assertEquals(1, scn.GetWoundsOn(frodo));
//
//		int burdens = scn.GetBurdens();
//
//		// Frodo (str 3) skirmishes the Guard Commander (str 7) and loses a skirmish, taking a wound.
//		scn.PassAssignmentActions();
//		scn.FreepsAssignAndResolve(frodo, commander);
//		scn.PassSkirmishActions();
//
//		// Our conversion clause explicitly excludes skirmishes - it must not be on offer here.
//		assertFalse(scn.FreepsHasOptionalTriggerAvailable("add a burden instead of the wound"));
//		while (scn.FreepsHasOptionalTriggerAvailable())
//			scn.FreepsDeclineOptionalTrigger();
//
//		// The skirmish wound was still converted to a burden - but by Isildur's Bane own
//		// mandatory, skirmish-only conversion, not by our card: Frodo's wound
//		// count is unchanged from before the skirmish, and 2 burdenx were added independently of ours.
//		assertEquals(1, scn.GetWoundsOn(frodo));
//		assertEquals(burdens + 2, scn.GetBurdens());
//	}
//
//	@Test
//	public void ShadowOwnedModStillOffersBothClausesToItsOwnerForTheSharedRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
//		// No "your" in the text - "its owner" - so the Shadow scenario must positively confirm the
//		// ability is present/active for the Shadow side too, acting on the same (Free Peoples-side)
//		// Ring-bearer.
//		var scn = GetShadowScenario();
//		var mod = scn.GetShadowCard("mod");
//		var marksman = scn.GetShadowCard("marksman");
//		var marksman2 = scn.GetShadowCard("marksman2");
//		var frodo = scn.GetRingBearer();
//
//		scn.MoveMinionsToTable(marksman, marksman2);
//		scn.StartGame();
//
//		scn.SkipToPhase(Phase.ARCHERY);
//		scn.PassCurrentPhaseActions();
//
//		int burdens = scn.GetBurdens();
//		assertFalse(scn.FreepsHasOptionalTriggerAvailable("put on The One Ring"));
//		assertTrue(scn.ShadowHasOptionalTriggerAvailable("put on The One Ring"));
//		scn.ShadowAcceptOptionalTrigger();
//		assertTrue(scn.RBWearingOneRing());
//		assertEquals(1, scn.GetWoundsOn(frodo));
//
//		assertTrue(scn.ShadowHasOptionalTriggerAvailable("add a burden instead of the wound"));
//		scn.ShadowChooseAction("add a burden instead of the wound");
//		if (scn.ShadowHasOptionalTriggerAvailable())
//			scn.ShadowDeclineOptionalTrigger();
//
//		assertEquals(1, scn.GetWoundsOn(frodo));
//		assertEquals(burdens + 1, scn.GetBurdens());
//	}
//}
