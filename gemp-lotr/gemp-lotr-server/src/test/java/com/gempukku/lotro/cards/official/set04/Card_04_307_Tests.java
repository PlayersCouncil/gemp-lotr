package com.gempukku.lotro.cards.official.set04;

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

public class Card_04_307_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("impatient", "4_307");
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; a fellowship ability used only on purpose
					// Merry, Friend to Sam: a [shire] Hobbit companion who is no Sam; a skirmish ability used only on purpose
					put("merry", "1_302");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					// Goblin Runner: strength 5, vitality 1; its play trigger is not reached, as it is cheated in
					put("runner", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ImpatientandAngryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Impatient and Angry
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert Sam to make him strength +3. If Sam wins this skirmish, you may remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("impatient");

		assertEquals("Impatient and Angry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsSamToMakeHimStrengthPlus3ForThisSkirmishAndWhenHeWinsABurdenMayBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var impatient = scn.GetFreepsCard("impatient");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(impatient);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner}, new PhysicalCardImpl[]{merry, runner2});
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsPlayAvailable(impatient));
		scn.FreepsPlayCard(impatient);

		// Sam pays, with nothing asked: Merry, a [shire] Hobbit beside him, is no Sam.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(6, scn.GetStrength(sam));
		assertInDiscard(impatient);

		// 6 against the Runner's 5.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());

		// The bonus lasted his skirmish; in the next one he is back to 3.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBurdenRemovalMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var impatient = scn.GetFreepsCard("impatient");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(impatient);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPlayCard(impatient);
		assertEquals(6, scn.GetStrength(sam));

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertInDiscard(runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenSamLosesTheSkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var impatient = scn.GetFreepsCard("impatient");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(impatient);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(runner);
		// The Runner at 10: it beats Sam's 6 without being twice his strength.
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPlayCard(impatient);
		assertEquals(6, scn.GetStrength(sam));
		assertEquals(10, scn.GetStrength(runner));

		scn.PassSkirmishActions();
		// Sam lost and lives, with the exertion and the wound for the loss.
		assertEquals(2, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetBurdens());
	}

	@Test
	public void WhenSamWinsWithNoBurdenToRemoveNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var impatient = scn.GetFreepsCard("impatient");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(impatient);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());

		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPlayCard(impatient);
		assertEquals(6, scn.GetStrength(sam));

		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void CannotBePlayedWhileSamIsExhaustedThoughAnotherHobbitCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var impatient = scn.GetFreepsCard("impatient");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(impatient);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertInHand(impatient);

		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(impatient));
		assertInHand(impatient);
	}

	@Test
	public void PlayedOnSamInNoSkirmishHeIsMadeStrongerAndAnotherCompanionsWinIsAnsweredByNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var impatient = scn.GetFreepsCard("impatient");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(impatient);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		// The card names no skirmish: Sam, assigned to none, may pay and is made stronger.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsPlayAvailable(impatient));
		scn.FreepsPlayCard(impatient);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(6, scn.GetStrength(sam));

		// Aragorn's 8 against 5: the win is not Sam's.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetBurdens());
	}

	@Test
	public void TheBurdenRemovalLapsesWithTheSkirmishItWasPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var impatient = scn.GetFreepsCard("impatient");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(impatient);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(runner, runner2);
		// The second Runner at 2, so that Sam wins his own skirmish at his printed 3.
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner2, null, -3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{sam, runner2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPlayCard(impatient);
		assertEquals(6, scn.GetStrength(sam));

		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Sam's own skirmish, after the one it was played in: he wins it and is offered nothing.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(2, scn.GetStrength(runner2));
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetBurdens());
	}
}
