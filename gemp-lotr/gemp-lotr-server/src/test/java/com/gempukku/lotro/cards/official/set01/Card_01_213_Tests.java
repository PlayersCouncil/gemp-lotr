package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_213_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frozen", "1_213");
					put("frozen2", "1_213");
					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] Nazgul, strength 9; text on companions bearing [wraith] conditions only
					put("nelya", "2_84");       // Ulaire Nelya: a [wraith] twilight Nazgul, strength 10; the rest needs 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12, fierce; the rest needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, strength 5, vitality 2; the rest needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; his abilities are used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrozenbyFearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Frozen by Fear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a Nazgûl strength +2 (or +3 if the Ring-bearer is assigned to a skirmish that has not resolved).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("frozen");

		assertEquals("Frozen by Fear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedBeforeTheSkirmishesAndMakesANazgulStrengthPlusTwoUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frozen = scn.GetShadowCard("frozen");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(frozen);

		scn.StartGame();
		assertInHand(frozen);
		scn.MoveMinionsToTable(lemenya, nelya, khamul, hound);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Not in the assignment phase, the phase before the skirmishes.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(frozen));
		scn.ShadowPass();

		// Lemenya fights Aragorn, with the [wraith] Orc in the same skirmish; Khamul is assigned to Sam, whose
		// skirmish comes later; Nelya and the Ring-bearer are assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{sam, khamul});
		scn.ShadowAssignToMinions(aragorn, hound);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Sam, Ring-bound but not the Ring-bearer, waits for a skirmish that has not resolved; Frodo waits for none.
		assertTrue(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(frozen));
		scn.ShadowPlayCard(frozen);

		// A Nazgul of either culture, in this skirmish or another or none; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		// The Ring-bearer is assigned to no skirmish: 9 + 2.
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(frozen);
		// Still +2 at the next decision of the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Lemenya and the Hound (16) overwhelm Aragorn (8).  In Sam's skirmish, the next skirmish phase, the bonus
		// is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, khamul));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InTheRingBearersOwnSkirmishANazgulIsPlusThreeAndSoIsOneInNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frozen = scn.GetShadowCard("frozen");
		var frozen2 = scn.GetShadowCard("frozen2");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(frozen, frozen2);

		scn.StartGame();
		assertInHand(frozen, frozen2);
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		// The Ring-bearer's own skirmish is being fought: it has not resolved.
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, lemenya));
		assertEquals(9, scn.GetStrength(lemenya));
		scn.ShadowPlayCard(frozen);
		scn.ShadowChooseCard(lemenya);

		// 9 + 3, in place of the +2 and not beside it.
		assertEquals(12, scn.GetStrength(lemenya));
		assertEquals(10, scn.GetStrength(nelya));

		scn.FreepsPass();
		scn.ShadowPlayCard(frozen2);
		scn.ShadowChooseCard(nelya);

		// Nelya skirmishes nobody: the condition is the Ring-bearer's, whatever the Nazgul is doing.  10 + 3.
		assertEquals(13, scn.GetStrength(nelya));
		assertEquals(12, scn.GetStrength(lemenya));
		assertInDiscard(frozen, frozen2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhileTheRingBearerWaitsForALaterSkirmishANazgulInAnotherIsPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frozen = scn.GetShadowCard("frozen");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(frozen);

		scn.StartGame();
		assertInHand(frozen);
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{frodo, nelya});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Aragorn's skirmish is fought first; the Ring-bearer's has not begun.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, lemenya));
		assertTrue(scn.IsCharAssignedAgainst(frodo, nelya));
		assertEquals(9, scn.GetStrength(lemenya));
		scn.ShadowPlayCard(frozen);
		scn.ShadowChooseCard(lemenya);

		// 9 + 3
		assertEquals(12, scn.GetStrength(lemenya));
		assertEquals(10, scn.GetStrength(nelya));
		assertInDiscard(frozen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnceTheRingBearersSkirmishHasResolvedANazgulInTheNextIsPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frozen = scn.GetShadowCard("frozen");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(frozen);
		// 3 + 1 for The Ruling Ring + 5 = 9, so that Frodo beats the Hound and the game goes on.
		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 5));

		scn.StartGame();
		assertInHand(frozen);
		scn.MoveMinionsToTable(lemenya, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, hound}, new PhysicalCardImpl[]{aragorn, lemenya});

		// The Ring-bearer's skirmish is fought first: 9 against 5, the Hound loses.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(hound));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// His skirmish has resolved, and he is assigned to no other.
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, lemenya));
		assertEquals(9, scn.GetStrength(lemenya));
		scn.ShadowPlayCard(frozen);

		// The one Nazgul is chosen with nothing asked: 9 + 2.
		assertEquals(11, scn.GetStrength(lemenya));
		assertInDiscard(frozen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoNazgulToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frozen = scn.GetShadowCard("frozen");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(frozen);

		scn.StartGame();
		assertInHand(frozen);
		scn.MoveMinionsToTable(hound);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, hound);
		scn.FreepsPass();

		// A [wraith] Orc and no Nazgul to choose, in the Ring-bearer's own skirmish: the text has no requirement,
		// so the event is offered and played for nothing.
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(frozen));
		scn.ShadowPlayCard(frozen);

		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(frozen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
