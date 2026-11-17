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

public class Card_04_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("living", "4_105");
					put("living2", "4_105");
					// Gandalf, Friend of the Shirefolk: strength 6, +1 for each of Hobbit, Dwarf, Elf and Man among
					// the companions: 7 beside the Hobbit Ring-bearer.
					put("gandalf", "1_72");
					// Ent Avenger: a [gandalf] companion who is not Gandalf, strength 6; its +2 needs 3 Ents.
					put("avenger", "6_27");
					put("runner1", "1_178");    // Goblin Runner: strength 5; cheated in, so its text never comes up
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UndertheLivingEarthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Under the Living Earth
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make Gandalf strength +1 for each twilight token you spot (limit +5).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("living");

		assertEquals("Under the Living Earth", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesGandalfStrongerByOneForEachTwilightTokenSpottedUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(living);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, runner1}, new PhysicalCardImpl[]{avenger, runner2});
		scn.FreepsResolveSkirmish(gandalf);
		scn.SetTwilight(2);

		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertTrue(scn.FreepsPlayAvailable(living));
		scn.FreepsPlayCard(living);

		// 2 in the pool + 1 for the event's own cost: 3 to spot, filled in with all of them.
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("3");

		// 7 + 3
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertInZone(Zone.DISCARD, living);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gandalf 10 beats the Runner's 5.  In the next skirmish, a phase of its own, the bonus is gone.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(avenger);
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBonusStopsAtPlus5AndASecondCopyGivesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var living2 = scn.GetFreepsCard("living2");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(living, living2);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner1);
		scn.FreepsResolveSkirmish(gandalf);
		scn.SetTwilight(5);

		assertEquals(7, scn.GetStrength(gandalf));
		scn.FreepsPlayCard(living);
		// 5 + 1: six spotted, and the bonus held to +5.
		assertEquals(6, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("6");
		assertEquals(12, scn.GetStrength(gandalf));

		// A limit does not reach another copy (CR 4.2 "limit"): the second gives +5 of its own in the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.FreepsPlayAvailable(living2));
		scn.FreepsPlayCard(living2);
		assertEquals(7, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("7");
		assertEquals(17, scn.GetStrength(gandalf));
		assertInZone(Zone.DISCARD, living, living2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void GandalfGainsTheBonusOutsideTheSkirmishAndAnotherGandalfCompanionDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(living);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		// Gandalf is assigned to nobody; the Ent Avenger, a [gandalf] companion, fights.
		scn.FreepsAssignToMinions(avenger, runner1);
		scn.FreepsResolveSkirmish(avenger);
		scn.SetTwilight(2);

		assertFalse(scn.IsCharSkirmishing(gandalf));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		scn.FreepsPlayCard(living);
		scn.FreepsChoose("3");

		// 7 + 3; the Avenger as he was.
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGandalfInPlayItIsStillPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(living);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, runner1);
		scn.FreepsResolveSkirmish(avenger);
		scn.SetTwilight(2);

		assertEquals(6, scn.GetStrength(avenger));
		assertTrue(scn.FreepsPlayAvailable(living));
		scn.FreepsPlayCard(living);
		scn.FreepsChoose("3");

		assertInZone(Zone.DISCARD, living);
		assertEquals(6, scn.GetStrength(avenger));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(living);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInHand(living);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(living));
	}

	@Test
	public void PlayedWithTheTwilightPoolEmptyGandalfIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(living);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner1);
		scn.FreepsResolveSkirmish(gandalf);
		scn.SetTwilight(0);

		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(gandalf));
		scn.FreepsPlayCard(living);

		// The token added for its own cost is the one there is to spot (CR 4.2, the ruling on this card).
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		assertEquals(8, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
