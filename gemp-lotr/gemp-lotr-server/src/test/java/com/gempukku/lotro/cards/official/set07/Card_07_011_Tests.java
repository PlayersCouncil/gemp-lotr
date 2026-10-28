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

public class Card_07_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("darkness", "7_11");

					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no text in play
					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; his skirmish ability is used only on purpose
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, strength 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability only

					put("soldier1", "1_271");     // Orc Soldier: [sauron] Orc, strength 7, vitality 2; his skirmish ability is used only on purpose
					put("soldier2", "1_271");
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OutofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Out of Darkness
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Dwarf strength +2. You may also exert that Dwarf to draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("darkness");

		assertEquals("Out of Darkness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedInASkirmishAndNotInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetFreepsCard("darkness");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(darkness);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertInHand(darkness);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(darkness));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(darkness));
	}

	@Test
	public void MakesADwarfStrengthPlus2ForThisSkirmishOnlyAndThatDwarfMayExertToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetFreepsCard("darkness");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCardsToHand(darkness);
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		assertInHand(darkness);
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier1}, new PhysicalCardImpl[]{gimli, soldier2});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.IsCharSkirmishingAgainst(guard, soldier1));
		assertTrue(scn.IsCharAssignedAgainst(gimli, soldier2));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(darkness);
		// The Dwarf in the skirmish, the Dwarf waiting for his own, and the Dwarf ally; not a companion who is no
		// Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseYes();

		// Gimli, the Dwarf made stronger, exerts; the Guard does not.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertInDiscard(darkness);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The last decision of this skirmish: Gimli is still strengthened.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));

		// The Guard, 4, loses to the Soldier, 7.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(gimli);

		// A new skirmish phase: the bonus went with the last one.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, soldier2));
		assertEquals(6, scn.GetStrength(gimli));
	}

	@Test
	public void DecliningTheExertionDrawsNothingAndTheBonusStands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetFreepsCard("darkness");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(darkness);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertInHand(darkness);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		int deck = scn.GetFreepsDeckCount();

		// Gimli is the one Dwarf, and is chosen unasked.
		scn.FreepsPlayCard(darkness);
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithTheChosenDwarfExhaustedTheExertionIsNotAskedAndTheBonusStands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetFreepsCard("darkness");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var soldier1 = scn.GetShadowCard("soldier1");
		scn.MoveCardsToHand(darkness);
		scn.MoveCompanionsToTable(guard, gimli);

		scn.StartGame();
		// Vitality 2: the Guard is exhausted. Gimli beside him could exert.
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInHand(darkness);
		scn.MoveMinionsToTable(soldier1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, soldier1);

		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(darkness);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli));
		scn.FreepsChooseCard(guard);

		// The Guard cannot exert, so nothing is asked and nothing drawn; another Dwarf does not pay in his place.
		assertEquals(6, scn.GetStrength(guard));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MayBePlayedWithNoDwarfInPlayAndThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetFreepsCard("darkness");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(darkness);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInHand(darkness);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertEquals(8, scn.GetStrength(aragorn));
		int frodoStrength = scn.GetStrength(frodo);
		int deck = scn.GetFreepsDeckCount();

		// An event's target is no requirement: it is played, and finds nobody.
		assertTrue(scn.FreepsPlayAvailable(darkness));
		scn.FreepsPlayCard(darkness);

		assertInDiscard(darkness);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
