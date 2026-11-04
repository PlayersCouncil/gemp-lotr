package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stock", "15_8");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, 6/3, damage +1; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, 4/2; "To play, spot a Dwarf" only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, no Dwarf; a maneuver ability used only on purpose
					// Faramir, Captain of Ithilien: [gondor] Man companion, hunter 2: a hunter who is no minion; a maneuver
					// ability used only on purpose
					put("faramir", "18_48");
					put("tracker", "15_171");   // Tracking Uruk: [uruk-hai] Uruk-hai, hunter 1 and no other text
					// Hunting Herdsman: [men] Man, hunter 1; +3 for each follower, and there are none
					put("herdsman", "15_83");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, no hunter, 5/1; its play trigger does nothing when cheated in
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no hunter, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SturdyStockStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Sturdy Stock
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Dwarf strength +2 (or if you can spot a hunter minion, strength +1 and <b>damage +1</b> for each hunter minion you can spot).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stock");

		assertEquals("Sturdy Stock", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoHunterMinionADwarfAnywhereIsStrengthPlus2ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stock = scn.GetFreepsCard("stock");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli, guard, aragorn, faramir);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(stock);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);

		// The assignment phase is no skirmish phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stock));
		scn.PassCurrentPhaseActions();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner}, new PhysicalCardImpl[]{gimli, troop});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(stock));
		scn.FreepsPlayCard(stock);

		// A Dwarf in this skirmish, one whose skirmish is still to come, and an ally; no Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, faramir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// Faramir is a hunter and the minions are not: no hunter minion, so +2 and no damage.
		assertInDiscard(stock);
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gone in his own skirmish, the next phase.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneHunterMinionTheDwarfIsStrengthPlus1AndDamagePlus1InPlaceOfPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stock = scn.GetFreepsCard("stock");
		var gimli = scn.GetFreepsCard("gimli");
		var tracker = scn.GetShadowCard("tracker");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(stock);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, tracker);

		// The Tracking Uruk is left to the Shadow player, who assigns him to nobody.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.FreepsPlayCard(stock);

		assertInDiscard(stock);
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithTwoHunterMinionsTheDwarfIsStrengthPlus2AndDamagePlus2UntilTheEndOfTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stock = scn.GetFreepsCard("stock");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var tracker = scn.GetShadowCard("tracker");
		var herdsman = scn.GetShadowCard("herdsman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToHand(stock);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, herdsman, tracker);

		// Two hunter minions of two cultures and races, the Uruk in no skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner}, new PhysicalCardImpl[]{gimli, herdsman});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.FreepsPlayCard(stock);
		scn.FreepsChooseCard(gimli);

		// +1 and damage +1 for each of the two.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gone in his own skirmish, the next phase.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
