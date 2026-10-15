package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_113_Tests
{

	// The Fellowship sites: Ettenmoors at site 2 is a plains, neither a river nor a forest.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("versatility", "1_113");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, vitality 4; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger, vitality 3; strength +3 against a Nazgul only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Buckleberry Ferry, a river, at site 2.
	protected VirtualTableScenario GetRiverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("versatility", "1_113");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, vitality 4; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger, vitality 3; strength +3 against a Nazgul only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion who is no ranger; Knight is his whole text

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, so always exhausted; its play trigger is not met when cheated in
					put("wrath", "4_261");      // Wrath of Harad: [raider] support condition; while a [raider] Man is spotted, each Free Peoples event that spots or exerts a ranger gains the cost "wound a companion"
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, vitality 2; strength +3 and fierce only at 2 burdens
				}},
				new HashMap<>() {{
					put("site1", "1_319");      // Bag End
					put("site2", "1_330");      // Buckleberry Ferry: river; no assignment or skirmish phases while only Hobbits are in the fellowship
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Breeland Forest, a forest, at site 2.
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("versatility", "1_113");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, vitality 4; a maneuver ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion who is no ranger; Knight is his whole text

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 is its whole text
				}},
				new HashMap<>() {{
					put("site1", "1_319");      // Bag End
					put("site2", "1_329");      // Breeland Forest: forest; stealth events may not be played
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ARangersVersatilityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: A Ranger's Versatility
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a ranger at a river or forest to exhaust a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("versatility");

		assertEquals("A Ranger's Versatility", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtARiverARangerOfThePlayersChoiceExertsToExhaustAMinionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverScenario();

		var versatility = scn.GetFreepsCard("versatility");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, arwen, knight);
		scn.MoveCardsToHand(versatility);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(versatility));

		scn.FreepsPlayCard(versatility);
		// The rangers, a [gondor] Man and an [elven] Elf; not the Knight, a [gondor] Man who is no ranger, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Either minion, an Orc or an Uruk-hai of two cultures; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, arwen, knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// Vitality 3: exhausted is two exertions, one vitality left.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetVitality(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertInDiscard(versatility);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtAForestItIsPlayedAsAtARiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var versatility = scn.GetFreepsCard("versatility");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToHand(versatility);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.AddWoundsToChar(savage, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(versatility));

		scn.FreepsPlayCard(versatility);
		// Aragorn, the only ranger, exerts unasked.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsChooseCard(savage);

		// One wound already on a vitality of 3: one exertion more exhausts it.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(versatility);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtASiteThatIsNeitherARiverNorAForestItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var versatility = scn.GetFreepsCard("versatility");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.MoveCardsToHand(versatility);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Ettenmoors is a plains.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		// Both rangers could exert.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(versatility));
	}

	@Test
	public void AtARiverItCannotBePlayedWhileTheOnlyRangerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverScenario();

		var versatility = scn.GetFreepsCard("versatility");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToHand(versatility);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Vitality 4: three wounds exhaust him.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(1, scn.GetVitality(aragorn));
		// The Knight, who is no ranger, could exert.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(versatility));
	}

	@Test
	public void WithTheOnlyMinionAlreadyExhaustedItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverScenario();

		var versatility = scn.GetFreepsCard("versatility");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(versatility);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Vitality 1: the Runner is exhausted already.
		assertEquals(1, scn.GetVitality(runner));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(versatility));

		scn.FreepsPlayCard(versatility);
		// The ranger's exertion is paid, and nothing is exhausted.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInDiscard(versatility);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void UnderWrathOfHaradItAlsoCostsAWoundOnACompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverScenario();

		var versatility = scn.GetFreepsCard("versatility");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToHand(versatility);

		var wrath = scn.GetShadowCard("wrath");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(wrath);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(versatility));

		scn.FreepsPlayCard(versatility);
		// The event exerts a ranger, so Wrath of Harad, with a [raider] Man spotted, adds "wound a companion" to its
		// cost: a cost of the Free Peoples player's event, which he pays and so chooses (CR 4.2 "cost").
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight));
		scn.FreepsChooseCard(knight);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Vitality 2: one exertion exhausts the Axeman.
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertInDiscard(versatility);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
