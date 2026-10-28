package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("undaunted", "7_51");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: strength 6, +1 for each race of companion (here Hobbit, and Man with Aragorn); vitality 4
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion who is not Gandalf, vitality 4; strength +2 only with 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; a "when you play" add (2), harmless cheated in
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UndauntedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Undaunted
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert Gandalf to make him strength +1 for each twilight token you spot (limit +8).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("undaunted");

		assertEquals("Undaunted", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfToMakeHimStrongerByTheTwilightTokensNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undaunted = scn.GetFreepsCard("undaunted");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(undaunted);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		// 6, +1 for the Hobbit companion Frodo.
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.FreepsPlayAvailable(undaunted));
		scn.FreepsPlayCard(undaunted);

		// The cost: Gandalf exerts.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// The event's own (2) is in the pool before its effect (CR 4.2 "playing a card"): 3 + 2 to spot, none to all.
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(5, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(5), scn.FreepsGetChoiceDefault());
		assertEquals(7, scn.GetStrength(gandalf));
		scn.FreepsChoose("3");

		// Three named of five: 7 + 3, not + 5.
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(5, scn.GetTwilight());
		assertInDiscard(undaunted);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithGandalfExhaustedThoughAnotherGandalfCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undaunted = scn.GetFreepsCard("undaunted");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(undaunted);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(undaunted);
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		// Vitality 4 with 3 wounds: Gandalf cannot exert, and Ent Avenger, a [gandalf] companion who can, does not pay.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(undaunted));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertInHand(undaunted);
	}

	@Test
	public void GandalfIsMadeStrongerThoughTheSkirmishIsAnotherCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undaunted = scn.GetFreepsCard("undaunted");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, aragorn, avenger);
		scn.MoveCardsToHand(undaunted);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{gandalf, runner2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(1);

		assertEquals(1, scn.GetTwilight());
		// 6, +1 each for the Hobbit and the Man companions.
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsPlayAvailable(undaunted));
		scn.FreepsPlayCard(undaunted);

		// 1 + 2 to spot.
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("3");

		// Gandalf, waiting for his own skirmish, exerts and is the one made stronger: 8 + 3.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(11, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolItsOwnCostIsAllThereIsToSpotAndNoneMayBeNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undaunted = scn.GetFreepsCard("undaunted");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(undaunted);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);
		scn.SetTwilight(0);

		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(undaunted);

		// Only the event's own (2) to spot.
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("0");

		// None named: the exertion is paid all the same, and he is no stronger.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(7, scn.GetStrength(gandalf));
		assertInDiscard(undaunted);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusStopsAtEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undaunted = scn.GetFreepsCard("undaunted");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(undaunted);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);
		scn.SetTwilight(10);

		assertEquals(10, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(gandalf));
		scn.FreepsPlayCard(undaunted);

		// 10 + 2 to spot: the count itself is not capped, and comes filled in with all twelve.
		assertEquals(12, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(12), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("12");

		// Twelve named, limit +8: 7 + 8.
		assertEquals(15, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsToTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undaunted = scn.GetFreepsCard("undaunted");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(undaunted);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, runner1}, new PhysicalCardImpl[]{aragorn, runner2});
		scn.FreepsResolveSkirmish(gandalf);
		scn.SetTwilight(0);

		assertEquals(8, scn.GetStrength(gandalf));
		scn.FreepsPlayCard(undaunted);
		scn.FreepsChoose("1");

		// 8 + 1, still there at the next decision of the same skirmish.
		assertEquals(9, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		scn.FreepsPass();

		// Gandalf won (9 to 5), and the Runner, of vitality 1, is gone.
		assertInDiscard(runner1);
		scn.FreepsResolveSkirmish(aragorn);

		// The next skirmish phase: the +1 is gone.
		assertEquals(8, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
