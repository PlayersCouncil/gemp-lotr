package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
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

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion to skirmish, strength 8

					put("lemenya", "1_232");    // Ulaire Lemenya: [ringwraith] Nazgul, strength 9; her text only touches bearers of [ringwraith] conditions
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: [raider] Nazgul, strength 12; fierce; his text needs 3 burdens
					put("ambusher", "8_74");    // Morgul Ambusher: [ringwraith] Orc, not a Nazgul, strength 8; its text needs 3 threats
					put("marksman", "1_176");   // Goblin Marksman: an archer, to wound the Ring-bearer in the archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// Isildur's Bane: the Ring-bearer may put it on when about to take any wound, an archery wound
				// included; that wound is 2 burdens instead.
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
		 * Strength: +1
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

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(elbereth);

		scn.StartGame();
		assertInHand(elbereth);
		// 3 + 1 for Isildur's Bane
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(elbereth));

		// Frodo is the only bearer there can be, so no choice is asked: Sam, Ring-bound and not the
		// Ring-bearer, is none.
		scn.FreepsPlayCard(elbereth);

		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(elbereth, frodo);
		assertNotAttachedTo(elbereth, sam);
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityCanDiscardToTakeOffRing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, elbereth);

		var marksman = scn.GetShadowCard("marksman");
		var ambusher = scn.GetShadowCard("ambusher");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, ambusher, lemenya);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();
		scn.FreepsChooseCard(frodo);
		assertFalse(scn.RBWearingOneRing());
		scn.FreepsAcceptOptionalTrigger(); // IB ring turning archery wound into 2 burdens
		assertTrue(scn.RBWearingOneRing());

		// The assignment phase, the one before the skirmishes: with the Ring on there is something the
		// ability could do, and it is a Skirmish ability.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(elbereth));

		//Assignments
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, ambusher},
				new PhysicalCardImpl[]{aragorn, lemenya}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertTrue(scn.RBWearingOneRing());
		assertAttachedTo(elbereth, frodo);

		// Frodo is skirmishing a [ringwraith] Orc, and the Nazgul waits for Aragorn: there is no skirmish to
		// cancel, so taking off the Ring is what happens, and no choice is put.
		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertInDiscard(elbereth);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, ambusher));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, lemenya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityCanCancelRBSkirmishIfSkirmishingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, elbereth);

		var lemenya = scn.GetShadowCard("lemenya");
		var ambusher = scn.GetShadowCard("ambusher");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, ambusher);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, lemenya},
				new PhysicalCardImpl[]{aragorn, ambusher}
		);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, lemenya));
		assertFalse(scn.RBWearingOneRing());
		assertAttachedTo(elbereth, frodo);

		// The Ring is not on, so there is nothing to take off: the skirmish is cancelled, and no choice is put.
		scn.FreepsUseCardAction(elbereth);
		assertInDiscard(elbereth);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertFalse(scn.IsCharSkirmishing(lemenya));
		// Nobody won or lost it.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetBurdens());
		// The other skirmish is still to come.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, ambusher));
		assertTrue(scn.FreepsDecisionAvailable("skirmish"));
	}

	@Test
	public void CancelsASkirmishBetweenTheRingBearerAndANazgulOfAnotherCultureWithAnOrcBesideIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, elbereth);

		var attea = scn.GetShadowCard("attea");
		var ambusher = scn.GetShadowCard("ambusher");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(attea, ambusher, lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, attea},
				new PhysicalCardImpl[]{aragorn, lemenya}
		);
		scn.ShadowAssignToMinions(frodo, ambusher);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, attea));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, ambusher));
		assertFalse(scn.RBWearingOneRing());

		scn.FreepsUseCardAction(elbereth);
		assertInDiscard(elbereth);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertFalse(scn.IsCharSkirmishing(attea));
		assertFalse(scn.IsCharSkirmishing(ambusher));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, lemenya));
		assertTrue(scn.FreepsDecisionAvailable("skirmish"));
	}

	@Test
	public void SkirmishAbilityCantCancelRBSkirmishIfNotSkirmishingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, elbereth);

		var ambusher = scn.GetShadowCard("ambusher");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher, lemenya);

		//Assignments
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, ambusher},
				new PhysicalCardImpl[]{aragorn, lemenya}
		);
		scn.FreepsResolveSkirmish(frodo);
		// The ability has a cost that can be paid, so it is offered though it will do nothing.
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, ambusher));
		assertFalse(scn.RBWearingOneRing());
		assertAttachedTo(elbereth, frodo);

		// Frodo is skirmishing a [ringwraith] Orc, and the Nazgul waits for Aragorn; the Ring is not on.
		// Neither thing can be done: the condition is discarded for nothing, and nothing is asked.
		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, ambusher));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, lemenya));
		assertInDiscard(elbereth);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WearingTheRingAgainstANazgulTheSkirmishCanBeCancelledAndTheRingStaysOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, elbereth);

		var attea = scn.GetShadowCard("attea");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(attea, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();
		scn.FreepsChooseCard(frodo);
		// Isildur's Bane, as the archery wound comes to Frodo: he puts the Ring on, for 2 burdens.
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetBurdens());

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, attea},
				new PhysicalCardImpl[]{aragorn, marksman}
		);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, attea));

		// Both can be done: the player chooses.
		scn.FreepsUseCardAction(elbereth);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Cancel");

		assertInDiscard(elbereth);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertFalse(scn.IsCharSkirmishing(attea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		// Aragorn's skirmish is still to come, and the Ring is still on.
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, marksman));
		assertTrue(scn.FreepsDecisionAvailable("skirmish"));
	}

	@Test
	public void WearingTheRingAgainstANazgulTheRingCanBeTakenOffAndTheSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var lemenya = scn.GetShadowCard("lemenya");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, lemenya));

		scn.FreepsUseCardAction(elbereth);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Take off");

		assertInDiscard(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, lemenya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInSamsSkirmishWithANazgulTheRingIsTakenOffAndThatSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, elbereth);

		var marksman = scn.GetShadowCard("marksman");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, lemenya);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();
		scn.FreepsChooseCard(frodo);
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, marksman},
				new PhysicalCardImpl[]{sam, lemenya}
		);
		// Sam, who is Ring-bound but not the Ring-bearer, is the one skirmishing a Nazgul.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, lemenya));
		assertTrue(scn.FreepsActionAvailable(elbereth));

		// The Ring-bearer is not in this skirmish, so it is not one the condition can cancel: only taking off
		// the Ring can be done, and no choice is put.
		scn.FreepsUseCardAction(elbereth);

		assertInDiscard(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.IsCharSkirmishingAgainst(sam, lemenya));
		assertTrue(scn.IsCharAssignedAgainst(frodo, marksman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInSamsSkirmishWithANazgulWithTheRingOffItIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, elbereth);

		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, lemenya);
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, lemenya));
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.FreepsActionAvailable(elbereth));

		// A skirmish with a Nazgul in it, and not the Ring-bearer's; the Ring is not on.  Neither thing can
		// be done: the condition is discarded for nothing, and nothing is asked.
		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInDiscard(elbereth);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, lemenya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
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
