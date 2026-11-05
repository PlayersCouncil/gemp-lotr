package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_175_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("came", "4_175");

					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("savage1", "1_151");
					put("savage2", "1_151");
					put("savage3", "1_151");
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2; damage +1 only
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; damage +1 only
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, no Uruk-hai, strength 6; a Shadow ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StillTheyCameStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Still They Came
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert an Uruk-hai who is not assigned to a skirmish to make another Uruk-hai strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("came");

		assertEquals("Still They Came", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnUnassignedUrukHaiToMakeAnotherStrengthPlus3UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var came = scn.GetShadowCard("came");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		var sentry = scn.GetShadowCard("sentry");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(came);
		assertInHand(came);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, savage3, sentry, guard, forger);
		scn.SetTwilight(10);
		scn.SkipToAssignments();

		// Aragorn's skirmish is fought first, Boromir's after it; four minions are left unassigned.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{boromir, savage2});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(savage3));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.ShadowPlayAvailable(came));
		scn.ShadowPlayCard(came);

		// To exert: an unassigned Uruk-hai of each culture; not the one in this skirmish, not the one whose
		// skirmish is still to come, not the unassigned Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage3, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage1, savage2, forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(savage3));

		// To strengthen: any other Uruk-hai, assigned or not; not the one exerted, not the Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage1, savage2, savage3, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage2);

		assertEquals(8, scn.GetStrength(savage2));
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage3));
		assertEquals(9, scn.GetStrength(guard));
		assertInDiscard(came);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 beats Savage1's 5; it lives with 1 wound of 3.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage1));

		// Boromir's skirmish is a phase of its own: the +3 is gone.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyUnassignedUrukHaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var came = scn.GetShadowCard("came");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(came);
		assertInHand(came);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, sentry, forger);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(sentry, 1);
		scn.SetTwilight(10);
		assertEquals(1, scn.GetWoundsOn(sentry));
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{boromir, savage2});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The two assigned Uruk-hai could exert, and so could the unassigned Orc; the one unassigned
		// Uruk-hai cannot. The pool could pay the event's 1.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.GetTwilight() >= 1);
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(forger));
		assertInHand(came);
		assertFalse(scn.ShadowPlayAvailable(came));
	}

	@Test
	public void WithNoOtherUrukHaiTheExertionIsPaidAndNothingIsStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var came = scn.GetShadowCard("came");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(came);
		assertInHand(came);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, forger);
		scn.SetTwilight(10);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, forger);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(6, scn.GetStrength(forger));
		assertEquals(8, scn.GetStrength(sentry));
		// An event whose target finds nothing is still playable (F-0665).
		assertTrue(scn.ShadowPlayAvailable(came));
		scn.ShadowPlayCard(came);

		// The Sentry, the one unassigned Uruk-hai, exerts unasked; there is no other Uruk-hai to strengthen.
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(6, scn.GetStrength(forger));
		assertEquals(8, scn.GetStrength(sentry));
		assertInDiscard(came);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnUrukHaiWhoseSkirmishIsOverMayBeExertedInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var came = scn.GetShadowCard("came");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(came);
		assertInHand(came);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, sentry);
		scn.SetTwilight(10);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{boromir, savage2});
		scn.ShadowDeclineAssignments();

		// Aragorn's 8 beats Savage1's 5; it lives with 1 wound of 3, its skirmish over.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage1));
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPlayCard(came);
		// Savage1 fought and is assigned to no skirmish now; Savage2 is in this one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage1, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage2));
		scn.ShadowChooseCard(savage1);
		assertEquals(2, scn.GetWoundsOn(savage1));

		scn.ShadowChooseCard(savage2);
		assertEquals(8, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
