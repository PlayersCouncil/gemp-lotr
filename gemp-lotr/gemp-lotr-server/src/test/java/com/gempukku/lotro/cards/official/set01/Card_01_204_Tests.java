package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_204_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veils", "1_204");
					put("veils2", "1_204");
					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] Nazgul, strength 9; text on companions bearing [wraith] conditions only
					put("nelya", "2_84");       // Ulaire Nelya: a [wraith] twilight Nazgul, strength 10; the rest needs 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12, fierce; the rest needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, strength 5; the rest needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The One Ring, Answer To All Riddles: "Skirmish: Add a burden to wear The One Ring until the regroup phase."
				VirtualTableScenario.ATARRing
		);
	}

	@Test
	public void AllVeilsRemovedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: All Veils Removed
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a Nazgûl strength +2 (or +5 if the Ring-bearer wears The One Ring).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("veils");

		assertEquals("All Veils Removed", card.getBlueprint().getTitle());
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

		var veils = scn.GetShadowCard("veils");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(veils);

		scn.StartGame();
		assertInHand(veils);
		scn.MoveMinionsToTable(lemenya, nelya, khamul, hound);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Not in the assignment phase, the phase before the skirmishes.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(veils));
		scn.ShadowPass();

		// Lemenya fights Aragorn, with the [wraith] Orc in the same skirmish; Khamul is assigned to Gimli, whose
		// skirmish comes later; Nelya is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, khamul});
		scn.ShadowAssignToMinions(aragorn, hound);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.RBWearingOneRing());
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(veils));
		scn.ShadowPlayCard(veils);

		// A Nazgul of either culture, in this skirmish or another or none; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		// The Ring is not worn: 9 + 2.
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(veils);
		// Still +2 at the next decision of the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Lemenya and the Hound (16) overwhelm Aragorn (8).  In Gimli's skirmish, the next skirmish phase, the
		// bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, khamul));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhileTheRingBearerWearsTheRingANazgulInThisSkirmishOrNoneIsPlusFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veils = scn.GetShadowCard("veils");
		var veils2 = scn.GetShadowCard("veils2");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(veils, veils2);

		scn.StartGame();
		assertInHand(veils, veils2);
		scn.MoveMinionsToTable(lemenya, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// The Ring-bearer, in no skirmish, puts The One Ring on by its own ability.
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		scn.FreepsUseCardAction(scn.GetRing());
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());

		assertEquals(9, scn.GetStrength(lemenya));
		scn.ShadowPlayCard(veils);
		scn.ShadowChooseCard(lemenya);

		// 9 + 5, in place of the +2 and not beside it.
		assertEquals(14, scn.GetStrength(lemenya));
		assertEquals(10, scn.GetStrength(nelya));

		scn.FreepsPass();
		scn.ShadowPlayCard(veils2);
		scn.ShadowChooseCard(nelya);

		// Nelya skirmishes nobody: the condition is the Ring-bearer's, whatever the Nazgul is doing.  10 + 5.
		assertEquals(15, scn.GetStrength(nelya));
		assertEquals(14, scn.GetStrength(lemenya));
		assertInDiscard(veils, veils2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheRingPutOnAfterThePlayLeavesTheBonusAtTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veils = scn.GetShadowCard("veils");
		var veils2 = scn.GetShadowCard("veils2");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(veils, veils2);

		scn.StartGame();
		assertInHand(veils, veils2);
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, lemenya);
		scn.FreepsPass();

		assertFalse(scn.RBWearingOneRing());
		assertEquals(9, scn.GetStrength(lemenya));
		scn.ShadowPlayCard(veils);
		// 9 + 2
		assertEquals(11, scn.GetStrength(lemenya));

		scn.FreepsUseCardAction(scn.GetRing());
		assertTrue(scn.RBWearingOneRing());
		// The condition was read as the event was carried out: still 9 + 2.
		assertEquals(11, scn.GetStrength(lemenya));

		scn.ShadowPlayCard(veils2);
		// A copy played now: 11 + 5.
		assertEquals(16, scn.GetStrength(lemenya));
		assertInDiscard(veils, veils2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoNazgulToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veils = scn.GetShadowCard("veils");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(veils);

		scn.StartGame();
		assertInHand(veils);
		scn.MoveMinionsToTable(hound);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, hound);
		scn.FreepsPass();

		// A [wraith] Orc and no Nazgul to choose: the text has no requirement, so the event is offered and
		// played for nothing.
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(veils));
		scn.ShadowPlayCard(veils);

		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(veils);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
