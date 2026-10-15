package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("light", "2_8");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3F; a fellowship ability only
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, no text but its own play requirement
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a maneuver ability used only on purpose
					put("stairs", "1_24");      // Stairs of Khazad-dum: another Free Peoples support condition; acts only on a Dwarf skirmishing a [moria] minion

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, twilight 1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, twilight 6, no text
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll, no Orc, twilight 10 (-3 only at an underground site)
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, twilight 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoldenLightontheLandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Golden Light on the Land
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Dwarf. Plays to your support area.<br>At a site which is not underground, each [moria] Orc's twilight cost is +2. Discard this condition during the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("light");

		assertEquals("Golden Light on the Land", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayableOnceADwarfAllyIsPlayedAndThenEachMoriaOrcCostsTwoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(light, grimir);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveCardsToHand(runner, troop, troll, soldier);
		scn.FreepsRefreshDecision();

		// Site 1, Bag End, is not underground.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertEquals(1, scn.GetTwilightCost(runner));
		assertEquals(6, scn.GetTwilightCost(troop));
		assertEquals(10, scn.GetTwilightCost(troll));
		assertEquals(2, scn.GetTwilightCost(soldier));

		// Frodo and Aragorn: no Dwarf to spot.
		assertFalse(scn.FreepsPlayAvailable(light));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(grimir);
		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		assertInZone(Zone.SUPPORT, light);
		assertEquals(3, scn.GetTwilightCost(runner));
		assertEquals(8, scn.GetTwilightCost(troop));
		// No Orc, and an Orc of another culture: printed.
		assertEquals(10, scn.GetTwilightCost(troll));
		assertEquals(2, scn.GetTwilightCost(soldier));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayableWithADwarfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(light);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		assertInZone(Zone.SUPPORT, light);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMoriaOrcPlayedAtAPlainsSiteCostsTwoMoreFromThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(light);

		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, light);
		scn.FreepsPass();

		// Site 2, Ettenmoors, is a plains site.  6 to play + 2 for this card + 2 roaming (site 4 at site 2).
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(troop));

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troop));
		scn.ShadowPlayCard(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtASanctuaryTheCostIsUpAndOnceTheFellowshipIsUndergroundItIsPrintedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(runner, troop);

		// Site 3, Rivendell Valley, is a sanctuary and not underground.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(1, scn.GetTwilightCost(runner));
		assertEquals(6, scn.GetTwilightCost(troop));

		scn.MoveCardsToSupportArea(light);
		assertInZone(Zone.SUPPORT, light);
		assertEquals(3, scn.GetTwilightCost(runner));
		assertEquals(8, scn.GetTwilightCost(troop));

		scn.FreepsPass();

		// Site 4, Balin's Tomb, is underground; the condition is still in play.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertInZone(Zone.SUPPORT, light);
		assertEquals(1, scn.GetTwilightCost(runner));
		assertEquals(6, scn.GetTwilightCost(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardedAsTheRegroupPhaseStartsAndNotBefore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var stairs = scn.GetFreepsCard("stairs");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(light, stairs);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, light, stairs);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// The last skirmish of the turn: still in play.
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertInZone(Zone.SUPPORT, light);

		scn.FreepsPass();
		scn.ShadowPass();

		// Aragorn (8) loses to the troop (13) and takes a wound; then the regroup phase.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInDiscard(light);
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
