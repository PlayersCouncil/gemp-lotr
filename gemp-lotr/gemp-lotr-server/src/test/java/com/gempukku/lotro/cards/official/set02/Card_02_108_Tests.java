package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elbereth", "2_108");

					put("nelya", "1_233");
					put("goblinarcher", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.IsildursBaneRing,
				VirtualTableScenario.Fellowship //We need the Ring-bearer's skirmish to be cancelable
		);
	}

	// The default format, multipath, in which a skirmish involving the Ring-bearer may not be cancelled, and a
	// Ring-bearer who is not Frodo, the kind of Ring-bearer the rule was made for.
	protected VirtualTableScenario GetMultipathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elbereth", "2_108");
					// Ulaire Toldea, Messenger of Morgul: a Nazgul of strength 12; fierce, and an assignment
					// ability that needs 4 burdens.
					put("toldea", "1_236");
					// Goblin Marksman: Archer is its whole text.  Its archery wound is what the Ring is put on for.
					put("marksman", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				// Bilbo, Bearer of Things Burgled: strength 3, resistance 8.  "Each time the fellowship moves, if
				// the twilight pool has 2 or fewer twilight tokens, add 2 burdens or add (3)."
				"9_49",
				// Isildur's Bane: "Response: If bearer is about to take a wound, he wears The One Ring until the
				// regroup phase", and that wound is 2 burdens instead.
				VirtualTableScenario.IsildursBaneRing,
				VirtualTableScenario.Multipath
		);
	}

	@Test
	public void OElberethGilthonielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: O Elbereth! Gilthoniel!
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Strength: 1
		 * Game Text: <b>Tale</b>. Bearer must be the Ring-bearer.<br><b>Skirmish:</b> Discard this condition to take off The One Ring or to cancel a skirmish involving the Ring-bearer and a Nazgûl.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elbereth");

		assertEquals("O Elbereth! Gilthoniel!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void OElberethGilthonielGoesOnRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var elbereth = scn.GetFreepsCard("elbereth");
		scn.MoveCardsToHand(elbereth);

		scn.StartGame();
		assertEquals(Zone.HAND, elbereth.getZone());
		scn.FreepsPlayCard(elbereth);
		assertEquals(Zone.ATTACHED, elbereth.getZone());
		assertSame(scn.GetRingBearer(), elbereth.getAttachedTo());
	}

	@Test
	public void SkirmishAbilityCanDiscardToTakeOffRing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var goblinarcher = scn.GetShadowCard("goblinarcher");
		scn.MoveMinionsToTable(goblinarcher);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger(); // IB ring turning archery wound into 2 burdens

		//Assignments
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, goblinarcher);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertTrue(scn.RBWearingOneRing());
		assertEquals(Zone.ATTACHED, elbereth.getZone());

		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertEquals(Zone.DISCARD, elbereth.getZone());
	}

	@Test
	public void SkirmishAbilityCanCancelRBSkirmishIfSkirmishingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var nelya = scn.GetShadowCard("nelya");
		scn.MoveMinionsToTable(nelya);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, nelya);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertEquals(Zone.ATTACHED, elbereth.getZone());

		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(Zone.DISCARD, elbereth.getZone());
	}

	@Test
	public void SkirmishAbilityCantCancelRBSkirmishIfNotSkirmishingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var goblinarcher = scn.GetShadowCard("goblinarcher");
		scn.MoveMinionsToTable(goblinarcher);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger();

		//Assignments
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, goblinarcher);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertEquals(Zone.ATTACHED, elbereth.getZone());

		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertEquals(Zone.DISCARD, elbereth.getZone());
	}

	@Test
	public void InMultipathWearingTheRingItIsTakenOffWithNoQuestionAndASkirmishTheRingBearerIsInIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathScenario();

		var bilbo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		var toldea = scn.GetShadowCard("toldea");
		var marksman = scn.GetShadowCard("marksman");
		scn.AttachCardsTo(bilbo, elbereth);

		scn.StartGame();
		scn.MoveMinionsToTable(toldea, marksman);
		// With 3 in the pool as the fellowship moves, Bilbo's own text asks nothing.
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();
		// The Marksman's wound: Bilbo puts the Ring on for it.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, toldea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, toldea));
		assertTrue(scn.RBWearingOneRing());
		assertAttachedTo(elbereth, bilbo);
		assertTrue(scn.FreepsActionAvailable(elbereth));

		// Of its two things to do only one can be done here, so that one is done and nothing is asked
		// (Comprehensive Rules 4.2, the ruling on this card: "As skirmishes involving the Ring-bearer cannot be
		// cancelled, the skirmish action of this condition can only be used to take off The One Ring").
		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInDiscard(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, toldea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InMultipathIsDiscardedAndDoesNotCancelASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathScenario();

		var bilbo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		var toldea = scn.GetShadowCard("toldea");
		scn.AttachCardsTo(bilbo, elbereth);

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		// With 3 in the pool as the fellowship moves, Bilbo's own text asks nothing.
		scn.SetTwilight(3);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(bilbo, toldea);

		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, toldea));
		assertFalse(scn.RBWearingOneRing());
		assertAttachedTo(elbereth, bilbo);
		// The ability has a cost that can be paid, so it is offered; its cancel is what the format refuses.
		assertTrue(scn.FreepsActionAvailable(elbereth));

		// Neither of its two things can be done: the Ring is not on, and the skirmish may not be cancelled.
		// The condition is discarded for nothing, and nothing is asked.
		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInDiscard(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, toldea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
