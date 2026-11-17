package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("skulker", "12_95");
					put("meal", "18_83");       // Gruesome Meal: [orc] Skirmish event, cost 2; a companion skirmishing an [orc] minion is strength -2
					put("brutality", "11_180"); // Brutality: [uruk-hai] Skirmish event, cost 2
					put("choice", "15_119");    // Unreasonable Choice: [orc] Shadow event, cost 1
					// Two-faced: [gollum] event, cost 2, "Shadow or Maneuver or Skirmish"; with no [gollum] card in play it does nothing
					put("twofaced1", "103_15");
					put("twofaced2", "103_15");
					put("blessing", "11_20");   // The Lady's Blessing: [elven] Skirmish event, cost 1, the Free Peoples player's
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcSkulkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Orc Skulker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>Each skirmish event you play is twilight cost -1.
		*/

		var scn = GetScenario();

		var skulker = scn.GetFreepsCard("skulker");

		assertEquals("Orc Skulker", skulker.getBlueprint().getTitle());
		assertNull(skulker.getBlueprint().getSubtitle());
		assertFalse(skulker.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, skulker.getBlueprint().getSide());
		assertEquals(Culture.ORC, skulker.getBlueprint().getCulture());
		assertEquals(CardType.MINION, skulker.getBlueprint().getCardType());
		assertEquals(Race.ORC, skulker.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(skulker, Keyword.LURKER));
		assertEquals(2, skulker.getBlueprint().getTwilightCost());
		assertEquals(5, skulker.getBlueprint().getStrength());
		assertEquals(2, skulker.getBlueprint().getVitality());
		assertEquals(4, skulker.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachSkirmishEventItsOwnerPlaysCostsOneLessAndNoOtherEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var blessing = scn.GetFreepsCard("blessing");
		var skulker = scn.GetShadowCard("skulker");
		var meal = scn.GetShadowCard("meal");
		var brutality = scn.GetShadowCard("brutality");
		var choice = scn.GetShadowCard("choice");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(blessing);
		scn.MoveCardsToHand(meal, brutality, choice);

		scn.StartGame();
		assertEquals(2, scn.GetTwilightCost(meal));
		assertEquals(2, scn.GetTwilightCost(brutality));
		assertEquals(1, scn.GetTwilightCost(choice));
		assertEquals(1, scn.GetTwilightCost(blessing));

		scn.MoveMinionsToTable(skulker);
		// Skirmish events of two cultures, one less each...
		assertEquals(1, scn.GetTwilightCost(meal));
		assertEquals(1, scn.GetTwilightCost(brutality));
		// ...and not a Shadow event, nor the other player's skirmish event.
		assertEquals(1, scn.GetTwilightCost(choice));
		assertEquals(1, scn.GetTwilightCost(blessing));

		scn.SkipToAssignments();
		scn.SetTwilight(5);
		scn.FreepsAssignToMinions(aragorn, skulker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		scn.ShadowPlayCard(meal);
		// 5 - (2 - 1)
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(meal);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnEventPrintedWithSkirmishAndAnotherPhaseCostsOneLessOnlyWhenPlayedInASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var skulker = scn.GetShadowCard("skulker");
		var inManeuver = scn.GetShadowCard("twofaced1");
		var inSkirmish = scn.GetShadowCard("twofaced2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(inManeuver, inSkirmish);

		scn.StartGame();
		scn.MoveMinionsToTable(skulker);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Played in the maneuver phase it is a maneuver event, and costs its full 2.
		assertEquals(2, scn.GetTwilightCost(inManeuver));
		scn.ShadowPlayCard(inManeuver);
		assertEquals(8, scn.GetTwilight());
		assertInDiscard(inManeuver);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, skulker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Played in a skirmish it is a skirmish event: 2 - 1.
		assertEquals(1, scn.GetTwilightCost(inSkirmish));
		scn.ShadowPlayCard(inSkirmish);
		assertEquals(7, scn.GetTwilight());
		assertInDiscard(inSkirmish);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
