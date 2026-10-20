package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_224_Tests
{

	// Answer To All Riddles 4_1: "While wearing The One Ring, the Ring-bearer is strength +2, and each time he is
	// about to take a wound in a skirmish, add a burden instead.  Skirmish: Add a burden to wear The One Ring until
	// the regroup phase."  It is put on in any skirmish, the Ring-bearer's own or another's.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rtim", "1_224");
					put("nelya", "1_233");     // Ulaire Nelya, Lieutenant of Morgul: Nazgul, fierce, strength 10; a Shadow ability used only on purpose
					put("lemenya", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, strength 9, not fierce
					put("enquea", "2_83");     // Ulaire Enquea, Ringwraith in Twilight: Nazgul, strength 11, twilight, not fierce
					put("twk", "1_237");       // The Witch-king, Lord of Angmar: Nazgul, fierce
					put("khamul", "103_61");   // Ulaire Attea, Khamul of the East: a [raider] Nazgul, fierce; his text needs 3 burdens
					put("hound", "7_192");     // Morgul Hound: a [wraith] Orc, strength 5, vitality 2; more only at 2 wounds on the Ring-bearer

					put("guard", "1_7");       // Dwarf Guard
					put("aragorn", "1_89");    // Aragorn, Ranger of the North
					put("sam", "1_311");       // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					put("sword", "1_299");     // Hobbit Sword: strength +2, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.ATARRing
		);
	}

	// The Ruling Ring 1_2: strength +1 (printed).  "Response: If bearer is about to take a wound in a skirmish, he
	// wears The One Ring until the regroup phase.  While wearing The One Ring, each time the Ring-bearer is about to
	// take a wound during a skirmish, add a burden instead."
	protected VirtualTableScenario GetRulingRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rtim", "1_224");
					put("nelya", "1_233");     // Ulaire Nelya, Lieutenant of Morgul: Nazgul, fierce, strength 10
					put("lemenya", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, strength 9, not fierce
					// Morgul Hound: a [wraith] Orc, strength 5, vitality 2.  "While you can spot 2 wounds on the
					// Ring-bearer, this minion is strength +4 and fierce."
					put("hound1", "7_192");
					put("hound2", "7_192");

					put("aragorn", "1_89");    // Aragorn, Ranger of the North
					put("sword", "1_299");     // Hobbit Sword: strength +2, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Isildur's Bane 1_1: strength +1, vitality +1 (printed).  "Response: If bearer is about to take a wound, he wears
	// The One Ring until the regroup phase.  While wearing The One Ring, each time the Ring-bearer is about to take a
	// wound, add 2 burdens instead."
	protected VirtualTableScenario GetIsildursBaneScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rtim", "1_224");
					put("lemenya", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, strength 9, not fierce
					put("marksman", "1_176");  // Goblin Marksman: a [moria] Orc; Archer is its whole text

					put("sword", "1_299");     // Hobbit Sword: strength +2, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.IsildursBaneRing
		);
	}

	// The Great Ring 19_1: "While wearing The One Ring, each time bearer is about to take a wound, add a burden
	// instead.  Maneuver: Wear The One Ring until the regroup phase.  Skirmish: Add a burden to make the Ring-bearer
	// strength +3."
	protected VirtualTableScenario GetGreatRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rtim", "1_224");
					put("lemenya", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, strength 9, not fierce

					put("aragorn", "1_89");    // Aragorn, Ranger of the North
					put("sword", "1_299");     // Hobbit Sword: strength +2, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	// Such a Weight to Carry 7_2: strength +2 (printed).  "Maneuver: Add a burden to wear The One Ring until the
	// regroup phase.  While wearing The One Ring, each time the Ring-bearer is about to take a wound, add a burden
	// instead."
	protected VirtualTableScenario GetSuchAWeightToCarryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rtim", "1_224");
					put("lemenya", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, strength 9, not fierce
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				"7_2"
		);
	}

	// The Binding Ring 9_1: strength +1, vitality +1 (printed).  "Maneuver: Exert bearer to wear The One Ring until
	// the regroup phase.  While wearing The One Ring, each time the Ring-bearer is about to take a wound, add a
	// burden instead."
	protected VirtualTableScenario GetTheBindingRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rtim", "1_224");
					put("lemenya", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, strength 9, not fierce
					// Hobbit Sword: strength +2 and nothing else.  With the Ring's printed +1 Frodo is 6, and loses
					// to 9 without being overwhelmed.
					put("sword", "1_299");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				"9_1"
		);
	}

	// The Ring of Rings 11_1: resistance +2 (printed).  "Response: If the Ring-bearer is about to take a wound, he or
	// she wears The One Ring until the regroup phase.  While the Ring-bearer is wearing The One Ring, each time he or
	// she is about to take a wound, add a burden instead."
	protected VirtualTableScenario GetTheRingOfRingsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rtim", "1_224");
					put("lemenya", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, strength 9, not fierce
					put("marksman", "1_176");  // Goblin Marksman: a [moria] Orc; Archer is its whole text

					// Hobbit Sword: strength +2 and nothing else.  This Ring prints no strength, and Frodo at 3 would
					// be overwhelmed by Lemenya's 9.
					put("sword", "1_299");
					// O Elbereth! Gilthoniel!: [shire] tale on the Ring-bearer, strength +1.  "Skirmish: Discard this
					// condition to take off The One Ring or to cancel a skirmish involving the Ring-bearer and a Nazgul."
					put("elbereth", "2_108");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				"11_1"
		);
	}

	// The Ring of Doom 15_1: "While wearing The One Ring, the Ring-bearer gains hunter 3, and each time he or she
	// is about to take a wound in a skirmish, add a burden instead.  Skirmish: Add a burden to wear The One Ring
	// until the regroup phase."
	protected VirtualTableScenario GetTheRingOfDoomScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rtim", "1_224");
					put("lemenya", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, strength 9, not fierce

					put("sword", "1_299");     // Hobbit Sword: strength +2, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				"15_1"
		);
	}

	@Test
	public void ReturntoItsMasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Return to Its Master
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
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
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCompanionsToTable(aragorn, sam);

		var rtim = scn.GetShadowCard("rtim");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya, hound);
		// Frodo is 3 + 2 for the sword, and one more by hand: at 6 he loses to Nelya (10) without being overwhelmed
		// once the Ring's +2 is gone.
		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 1));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, hound },
				new PhysicalCardImpl[] { frodo, lemenya },
				new PhysicalCardImpl[] { sam, nelya }
		);

		// Aragorn's skirmish is fought first, and in it Frodo puts the Ring on.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(8, scn.GetStrength(frodo));
		scn.ShadowPass();
		// The Ring's own skirmish ability has a cost, and is still on offer while the Ring is worn.
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsPass();

		// Aragorn (8) beat the Hound (5).  Two assignments remain, the Ring-bearer's own among them.
		assertEquals(1, scn.GetWoundsOn(hound));
		assertTrue(scn.IsCharAssignedAgainst(frodo, lemenya));
		assertTrue(scn.IsCharAssignedAgainst(sam, nelya));
		assertInHand(rtim);
		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);

		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(lemenya));
		assertFalse(scn.IsCharAssigned(nelya));

		// Either Nazgul may be chosen; the Hound is a [wraith] minion and no Nazgul.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);

		// Nelya, who had been Sam's, is assigned to the Ring-bearer, and to nobody else.
		assertInDiscard(rtim);
		assertTrue(scn.IsCharAssignedAgainst(frodo, nelya));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(lemenya));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, nelya));
		// The Ring is worn and its text does not apply: no +2, and its skirmish ability is not offered.
		assertTrue(scn.RBWearingOneRing());
		assertEquals(6, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));

		scn.PassSkirmishActions();
		// 6 against 10: the wound for losing is a wound, and no burden is added.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		// The skirmishes that were cancelled are not fought.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(lemenya));

		// That skirmish over, the Ring's text applies again.  Nelya is fierce: her fierce skirmish is still to come.
		assertEquals(8, scn.GetStrength(frodo));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
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

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		// 3 + 2 for the sword + 1 by hand: at 6 Frodo is not overwhelmed by Nelya (10) once the Ring's +2 is gone.
		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 1));

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, nelya);
		scn.FreepsUseCardAction(ring);

		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());

		scn.ShadowPass();
		scn.FreepsPass();

		// 8 against 10, the Ring on: the wound is a burden.
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowDeclineOptionalTrigger();
		assertInHand(rtim);

		// The fierce skirmish is a skirmish phase too: at its end the event is offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(frodo, nelya);

		assertTrue(scn.RBWearingOneRing());
		scn.PassFierceSkirmishActions();
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);
		assertInDiscard(rtim);
		assertTrue(scn.IsCharAssignedAgainst(frodo, nelya));

		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishing(frodo));
		assertTrue(scn.IsCharSkirmishing(nelya));

		// A third skirmish for Nelya, after her regular and her fierce one: 6 against 10, a wound and no burden.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ReturntoItsMasterPermitsAssigningOfNonFierceNazgulWhenUsedDuringFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rtim = scn.GetShadowCard("rtim");
		var enquea = scn.GetShadowCard("enquea");
		var twk = scn.GetShadowCard("twk");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(rtim);

		var frodo = scn.GetRingBearer();
		var onering = scn.GetRing();
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, twk, khamul, hound);

		//Enquea is the twilight version, who is not fierce.  He should still be a valid target
		// when RTIM triggers during a fierce skirmish.

		scn.SkipToShadowAssignments();
		assertEquals(4, scn.ShadowGetShadowAssignmentTargetCount()); // every minion
		//We will skip the non-fierce skirmishes
		scn.ShadowDeclineAssignments();

		//Fierce skirmish assignment phase
		assertTrue(scn.InFierceRound());
		scn.SkipToShadowAssignments();
		assertEquals(2, scn.ShadowGetShadowAssignmentTargetCount()); // TWK and Khamul, but not Enquea or the Hound
		scn.ShadowAssignToMinions(guard, twk);
		scn.FreepsResolveSkirmish(guard);

		//Need the One Ring to be worn
		scn.FreepsUseCardAction(onering);
		assertTrue(scn.RBWearingOneRing());
		scn.ShadowPass();
		scn.FreepsPass();

		// The Witch-king overwhelmed the Dwarf Guard.
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);

		// A Nazgul who is not fierce, the Nazgul who has just fought, and a Nazgul who is not [wraith]; not the
		// [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea, twk, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(enquea);

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharAssignedAgainst(frodo, enquea));
		assertFalse(scn.IsCharAssigned(twk));
		assertFalse(scn.IsCharAssigned(khamul));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheRulingRingTurnsNoWoundIntoABurdenInThatSkirmishAndDoesAgainInTheFierceSkirmishAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRulingRingScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		scn.AttachCardsTo(frodo, sword);

		var rtim = scn.GetShadowCard("rtim");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, nelya);
		// 3 + 1 printed on the Ring + 2 for the sword, against 10.
		assertEquals(6, scn.GetStrength(frodo));
		scn.PassSkirmishActions();

		// The regular skirmish: Frodo loses, puts the Ring on, and the wound is a burden.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);
		// Nelya, who has just fought, is the one Nazgul: she is assigned without a choice.
		assertInDiscard(rtim);
		assertTrue(scn.IsCharAssignedAgainst(frodo, nelya));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The event's skirmish.  The strength printed on the Ring is no game text: Frodo is still 6.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(6, scn.GetStrength(frodo));
		scn.PassSkirmishActions();

		// The wound for losing is a wound: the Ring neither turns it into a burden nor offers its Response.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());

		// Nelya's fierce skirmish is still fought, a third skirmish for her, and in it the Ring's text is back.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(frodo, nelya);
		assertTrue(scn.RBWearingOneRing());
		scn.PassFierceSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsildursBaneWornSinceTheArcheryPhaseAddsNoBurdensInThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIsildursBaneScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		scn.AttachCardsTo(frodo, sword);

		var rtim = scn.GetShadowCard("rtim");
		var lemenya = scn.GetShadowCard("lemenya");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, marksman);

		// The Marksman's archery wound: Frodo puts the Ring on, for 2 burdens.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		// 3 + 1 printed on the Ring + 2 for the sword, against 9.
		assertEquals(6, scn.GetStrength(frodo));
		scn.PassSkirmishActions();
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);
		// Lemenya is the one Nazgul; the Marksman, an Orc who has not fought, is not assigned.
		assertTrue(scn.IsCharAssignedAgainst(frodo, lemenya));
		assertFalse(scn.IsCharAssigned(marksman));

		scn.FreepsResolveSkirmish(frodo);
		assertEquals(6, scn.GetStrength(frodo));
		scn.PassSkirmishActions();

		// A wound, and not 2 burdens; nor is the Ring's Response offered against it.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedAtTheEndOfAnyOtherPhaseAndTheGreatRingDoesNothingInThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGreatRingScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCompanionsToTable(aragorn);

		var rtim = scn.GetShadowCard("rtim");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());

		// The Ring is worn at the end of the maneuver, archery and assignment phases: nothing is offered.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertInHand(rtim);

		// Aragorn's skirmish.  The Ring's skirmish ability is on offer in it.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.PassSkirmishActions();

		// At the end of a skirmish phase it is offered, whoever fought.
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);
		assertTrue(scn.IsCharAssignedAgainst(frodo, lemenya));

		scn.FreepsResolveSkirmish(frodo);
		// 3 + 2 for the sword, against 9.
		assertEquals(5, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SuchAWeightToCarryWornSinceTheManeuverPhaseAddsNoBurdenInThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSuchAWeightToCarryScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();

		var rtim = scn.GetShadowCard("rtim");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.FreepsResolveSkirmish(frodo);
		// 3 + 2 printed on the Ring, against 9: the wound for losing is a burden.
		assertEquals(5, scn.GetStrength(frodo));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);
		assertTrue(scn.IsCharAssignedAgainst(frodo, lemenya));

		scn.FreepsResolveSkirmish(frodo);
		// The Ring's printed +2 is no game text: he is still 5.
		assertEquals(5, scn.GetStrength(frodo));
		scn.PassSkirmishActions();

		// A wound, and no burden.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheBindingRingWornSinceTheManeuverPhaseAddsNoBurdenInThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTheBindingRingScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();

		var sword = scn.GetFreepsCard("sword");
		var rtim = scn.GetShadowCard("rtim");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);

		// The Binding Ring is put on by an exertion of its bearer, which is no wound and adds no burden.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.FreepsResolveSkirmish(frodo);
		// 3 + 1 printed on the Ring + 2 for the Sword, against 9: the wound for losing is a burden.
		assertEquals(6, scn.GetStrength(frodo));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));

		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);
		assertTrue(scn.IsCharAssignedAgainst(frodo, lemenya));

		scn.FreepsResolveSkirmish(frodo);
		// The Ring's printed +1 is no game text: he is still 6.
		assertEquals(6, scn.GetStrength(frodo));
		scn.PassSkirmishActions();

		// A wound, and no burden.
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheRingOfRingsWornSinceTheArcheryPhaseAddsNoBurdenInThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTheRingOfRingsScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		scn.AttachCardsTo(frodo, sword);

		var rtim = scn.GetShadowCard("rtim");
		var lemenya = scn.GetShadowCard("lemenya");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, marksman);

		// The Marksman's archery wound: Frodo puts the Ring on, for a burden.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		// 3 + 2 for the Sword, against 9: the wound for losing is a burden.
		assertEquals(5, scn.GetStrength(frodo));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		// 10 + 2 printed on the Ring - 2 burdens
		assertEquals(10, scn.GetResistance(frodo));

		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);
		// Lemenya is the one Nazgul; the Marksman, an Orc who has not fought, is not assigned.
		assertTrue(scn.IsCharAssignedAgainst(frodo, lemenya));
		assertFalse(scn.IsCharAssigned(marksman));

		scn.FreepsResolveSkirmish(frodo);
		// The Ring's printed resistance +2 is no game text: he is still 10.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetStrength(frodo));
		scn.PassSkirmishActions();

		// A wound, and no burden.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TakenOffInThatSkirmishTheRingOfRingsDoesNotAnswerTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTheRingOfRingsScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, sword, elbereth);

		var rtim = scn.GetShadowCard("rtim");
		var lemenya = scn.GetShadowCard("lemenya");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, marksman);

		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowPlayCard(rtim);
		scn.FreepsResolveSkirmish(frodo);

		// The skirmish of the Ring-bearer cannot be cancelled, so the tale takes the Ring off with nothing asked.
		assertTrue(scn.RBWearingOneRing());
		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 3 + 2 against 9.  Off, this Ring answers a wound in any other skirmish; in this one its text does not
		// apply, and nothing is offered.
		scn.ShadowPass();
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheRingOfDoomGivesNoHunterAndTurnsNoWoundIntoABurdenInThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTheRingOfDoomScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		scn.AttachCardsTo(frodo, sword);

		var rtim = scn.GetShadowCard("rtim");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.FreepsResolveSkirmish(frodo);

		// Frodo is 3 + 2 for the sword, and vitality 4 + 2 printed on this Ring.  He puts the Ring on: hunter 3, and
		// against a Nazgul who is no hunter, 8.
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(6, scn.GetVitality(frodo));
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetKeywordCount(frodo, Keyword.HUNTER));
		assertEquals(8, scn.GetStrength(frodo));
		scn.ShadowPass();
		// The Ring's own skirmish ability has a cost, and is still on offer while the Ring is worn.
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsPass();

		// 8 against 9: the wound for losing is a burden.
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);
		assertTrue(scn.IsCharAssignedAgainst(frodo, lemenya));

		scn.FreepsResolveSkirmish(frodo);
		// The Ring is worn and its text does not apply: no hunter, and its skirmish ability is not offered.  The
		// vitality printed on it is no game text.
		assertTrue(scn.RBWearingOneRing());
		assertFalse(scn.HasKeyword(frodo, Keyword.HUNTER));
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(6, scn.GetVitality(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));

		scn.PassSkirmishActions();
		// 5 against 9: a wound, and no burden.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheRingIsNotWornAtTheEndOfASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRulingRingScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		scn.AttachCardsTo(frodo, sword);

		var rtim = scn.GetShadowCard("rtim");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, lemenya);
		scn.PassSkirmishActions();

		// 6 against 9: Frodo loses, and leaves the Ring off.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertFalse(scn.RBWearingOneRing());
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The skirmish phase ends with a Nazgul in play and the event in hand, and the game goes on.
		assertInHand(rtim);
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoNazgulTheRemainingAssignmentsAreCancelledAndNobodyIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRulingRingScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCompanionsToTable(aragorn);

		var rtim = scn.GetShadowCard("rtim");
		var hound1 = scn.GetShadowCard("hound1");
		var hound2 = scn.GetShadowCard("hound2");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(hound1, hound2);
		// With 2 wounds on the Ring-bearer each Hound is strength 9 and fierce.
		scn.AddWoundsToChar(frodo, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { frodo, hound1 },
				new PhysicalCardImpl[] { aragorn, hound2 }
		);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		// 6 against 9: Frodo puts the Ring on.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.IsCharAssignedAgainst(aragorn, hound2));
		assertTrue(scn.ShadowPlayAvailable(rtim));
		scn.ShadowPlayCard(rtim);

		// The Hounds are [wraith] minions and no Nazgul: Aragorn's assignment is cancelled, nobody is chosen and
		// nobody is assigned.
		assertInDiscard(rtim);
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(hound2));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(hound1));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(hound2));
		// The game has gone on to the Hounds' fierce assignments.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoNazgulAssignedTheRingsTextStillAppliesInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRulingRingScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCompanionsToTable(aragorn);

		var rtim = scn.GetShadowCard("rtim");
		var hound1 = scn.GetShadowCard("hound1");
		var hound2 = scn.GetShadowCard("hound2");
		scn.MoveCardsToHand(rtim);

		scn.StartGame();
		scn.MoveMinionsToTable(hound1, hound2);
		// With 2 wounds on the Ring-bearer each Hound is strength 9 and fierce.
		scn.AddWoundsToChar(frodo, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { frodo, hound1 },
				new PhysicalCardImpl[] { aragorn, hound2 }
		);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());

		// No Nazgul: the event cancels Aragorn's assignment and creates no skirmish.
		scn.ShadowPlayCard(rtim);
		assertInDiscard(rtim);
		assertFalse(scn.IsCharAssigned(frodo));

		// The Hounds' fierce skirmish is none of the event's: the Ring, still worn, turns its wound into a burden.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(frodo, hound1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
