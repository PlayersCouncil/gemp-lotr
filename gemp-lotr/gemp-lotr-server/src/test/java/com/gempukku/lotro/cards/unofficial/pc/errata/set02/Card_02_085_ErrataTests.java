package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_085_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.Multipath);
	}

	// A format in which a skirmish involving the Ring-bearer may be cancelled.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.Fellowship);
	}

	// Galadriel as Ring-bearer, so that a Frodo can stand in play who is not the Ring-bearer.
	protected VirtualTableScenario GetNonFrodoRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.Multipath, VirtualTableScenario.GaladrielRB);
	}

	protected VirtualTableScenario GetScenario(String format) throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(format, VirtualTableScenario.FOTRFrodo);
	}

	protected VirtualTableScenario GetScenario(String format, String ringBearer) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "52_85");

					put("rider", "12_161");     // Black Rider: [ringwraith] Nazgul, strength 8 +1 for each companion, fierce
					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: a Nazgul who is neither fierce nor twilight; her text only weakens companions bearing a [wraith] condition
					put("return", "1_224");     // Return to Its Master: Shadow response event, at the end of a skirmish phase in which the Ring-bearer wears The One Ring
					put("twilightworld", "1_228");  // The Twilight World: [ringwraith] Shadow response event, when the Ring-bearer puts on The One Ring; another title than Return to Its Master

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("frodo", "1_290");      // Frodo, Son of Drogo: Ring-bound, and a companion who is not the Ring-bearer when Galadriel is
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit companion, strength 3
					put("stealth1", "1_298");   // Hobbit Stealth: Skirmish event, at sites 1 to 5 cancel a skirmish involving a Hobbit
					put("stealth2", "1_298");
				}},
				VirtualTableScenario.FellowshipSites,
				ringBearer,
				VirtualTableScenario.RulingRing,
				format
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: The Witch-king, Lord of the Nazgul
		 * Unique: True
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 8
		 * Type: Minion
		 * Race: Nazgul
		 * Strength: 14
		 * Vitality: 4
		 * Site: 3
		 * Game Text: Twilight.
		 * Return to Its Master cannot be played. Skirmishes involving The Witch-king cannot be canceled.
		 * Each time The Witch-king wins a skirmish, you may exert him to wound the Ring-bearer twice.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("witchking");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Lord of the Nazgul", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysFromHandForEightPlusRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCardsToHand(witchking);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		scn.ShadowPlayCard(witchking);

		// 8, + 2 roaming: a site-3 minion at site 2.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ReturnToItsMasterCannotBePlayedWhileTheWitchkingIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider");
		var returnCard = scn.GetShadowCard("return");
		var twilightWorld = scn.GetShadowCard("twilightworld");
		scn.MoveCardsToHand(returnCard, twilightWorld);
		// Strong enough that the Black Rider does not overwhelm him, and still loses: 7 + 1 for the Ring against 9.
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Frodo"), null, 4));

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, rider);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, rider);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		// Frodo puts the Ring on rather than take the wound.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Another [ringwraith] response event is offered as the Ring goes on, with The Witch-king in play.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(twilightWorld));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.HAND, twilightWorld);

		// The end of the skirmish phase, the Ring-bearer wearing the Ring: Return to Its Master is not offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(returnCard));
		assertInZone(Zone.HAND, returnCard);
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		// The Black Rider is fierce: the game goes on to the fierce assignments.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ReturnToItsMasterCanBePlayedWithoutTheWitchking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider");
		var returnCard = scn.GetShadowCard("return");
		scn.MoveCardsToHand(returnCard, witchking);
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Frodo"), null, 4));

		scn.StartGame();
		scn.MoveMinionsToTable(rider);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, rider);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		// The same moment with The Witch-king in hand and not in play.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(returnCard));
	}

	@Test
	public void HobbitStealthCannotCancelASkirmishInvolvingTheWitchkingButCancelsAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stealth1 = scn.GetFreepsCard("stealth1");
		var stealth2 = scn.GetFreepsCard("stealth2");
		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCompanionsToTable(merry, sam, aragorn);
		scn.MoveCardsToHand(stealth1, stealth2);
		// So that Merry is not overwhelmed, and the skirmish is resolved by strength.
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Merry"), null, 5));

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, rider);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { merry, witchking }, new PhysicalCardImpl[] { sam, rider });

		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, witchking));
		assertTrue(scn.FreepsPlayAvailable(stealth1));
		scn.FreepsPlayCard(stealth1);
		assertInDiscard(stealth1);
		// Site 2, Merry a Hobbit: the event would cancel this skirmish, and does not.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, witchking));
		scn.ShadowPass();
		scn.FreepsPass();
		// 8 against 14: Merry loses and takes a wound. The Witch-king's own trigger is declined.
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(merry));

		// The Black Rider's skirmish, on the same board: cancelled.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, rider));
		scn.FreepsPlayCard(stealth2);
		assertFalse(scn.IsCharSkirmishingAgainst(sam, rider));
		assertEquals(0, scn.GetWoundsOn(sam));
		// The Black Rider is fierce: on to the fierce assignments.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void UnderAFormatThatAllowsItTheRingBearersSkirmishWithTheWitchkingIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var frodo = scn.GetRingBearer();
		var stealth1 = scn.GetFreepsCard("stealth1");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCardsToHand(stealth1);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, witchking);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, witchking));
		scn.FreepsPlayCard(stealth1);
		assertInDiscard(stealth1);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, witchking));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UnderAFormatThatAllowsItTheRingBearersSkirmishWithAnotherNazgulIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var frodo = scn.GetRingBearer();
		var stealth1 = scn.GetFreepsCard("stealth1");
		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCardsToHand(stealth1);

		scn.StartGame();
		// The Witch-king in play and not in this skirmish.
		scn.MoveMinionsToTable(witchking, rider);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, rider);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, rider));
		scn.FreepsPlayCard(stealth1);
		assertFalse(scn.IsCharSkirmishingAgainst(frodo, rider));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WhenTheWitchkingWinsHeMayExertToWoundTheRingBearerTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNonFrodoRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var witchking = scn.GetShadowCard("witchking");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(frodo, aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 14 against 8: The Witch-king wins without overwhelming.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		scn.ShadowAcceptOptionalTrigger();
		// He exerts himself and not the other Nazgul beside him.
		assertEquals(1, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(lemenya));

		// Galadriel is not in a skirmish; the Ruling Ring is offered at each wound all the same, and declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsDeclineOptionalTrigger();

		// The Ring-bearer takes both wounds: not Frodo, who shares the name the Ring-bearer usually has, nor Sam.
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenTheWitchkingLosesOrAnotherNazgulWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Aragorn"), null, 10));
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Merry"), null, 5));

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, rider);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, witchking }, new PhysicalCardImpl[] { merry, rider });

		// The Black Rider, 8 + 3 companions, beats Merry, 3 + 5, without overwhelming him.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(witchking));

		// 18 against 14: The Witch-king loses and takes a wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(witchking));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(witchking));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// The Black Rider is fierce: on to the fierce assignments.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachWinIsOfferedAgainAndDecliningChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCompanionsToTable(aragorn);
		// Fierce, so that he skirmishes, and wins, a second time in the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.name("The Witch-king"), null, Keyword.FIERCE));

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(witchking));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheWitchkingIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(witchking, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(witchking));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheRulingRingMayAnswerTheWitchkingsWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetBurdens());

		// The first wound: the Ring's response is offered and taken; Frodo puts the Ring on and a burden replaces
		// the wound. The second wound finds him wearing it, and is a burden too.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Two burdens and no wound: the second wound was replaced too, the Ring being on by then. (It comes off
		// as the regroup phase starts, which is where the game now stands.)
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
