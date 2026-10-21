package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_051_Tests
{

	// Rivers at sites 2 and 3: Buckleberry Ferry and Ford of Bruinen; Bag End at site 1 is none.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goldberry", "9_51");
					put("tom", "9_52");         // Tom Bombadil, The Master: [shire] ally, vitality 9; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; a fellowship ability used only on purpose
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, vitality 3; a fellowship ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				new HashMap<>() {{
					put("site1", "1_319");      // Bag End
					put("site2", "1_330");      // Buckleberry Ferry: river; no assignment or skirmish phases while only Hobbits are in the fellowship
					put("site3", "1_338");      // Ford of Bruinen: river, sanctuary; the first Nazgul played there each turn is -5
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

	// The Fellowship path as it stands: Ettenmoors at site 2 is a plains, Rivendell Valley at site 3 a sanctuary;
	// neither is a river.
	protected VirtualTableScenario GetPlainsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goldberry", "9_51");
					put("tom", "9_52");         // Tom Bombadil, The Master: [shire] ally, vitality 9; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoldberryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Goldberry, River-daughter
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype:
		 * Strength: 4
		 * Vitality: 6
		 * Site Number: 2
		 * Game Text: Each time the fellowship moves to a river, heal Goldberry and Tom Bombadil.<br><b>Skirmish:</b> If the fellowship is at a river, exert Goldberry to make a companion strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goldberry");

		assertEquals("Goldberry", card.getBlueprint().getTitle());
		assertEquals("River-daughter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(6, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 2)));
	}

	@Test
	public void EachMoveToARiverHealsGoldberryAndTomBombadilAndNoOneElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goldberry = scn.GetFreepsCard("goldberry");
		var tom = scn.GetFreepsCard("tom");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(goldberry, tom, bilbo);
		scn.AddWoundsToChar(goldberry, 2);
		scn.AddWoundsToChar(tom, 2);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(bilbo, 1);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(goldberry));
		assertEquals(2, scn.GetWoundsOn(tom));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bilbo));

		scn.FreepsPass();          // move 1 -> 2, to Buckleberry Ferry
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(goldberry));
		assertEquals(1, scn.GetWoundsOn(tom));
		// A wounded companion, and a wounded [shire] ally of another title, are not healed.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Again on the second move of the turn, to the Ford of Bruinen, a river that is also a sanctuary.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(goldberry));
		assertEquals(0, scn.GetWoundsOn(tom));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoveToASiteThatIsNoRiverHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlainsScenario();

		var goldberry = scn.GetFreepsCard("goldberry");
		var tom = scn.GetFreepsCard("tom");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(goldberry, tom);
		scn.AddWoundsToChar(goldberry, 2);
		scn.AddWoundsToChar(tom, 2);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(goldberry));
		assertEquals(2, scn.GetWoundsOn(tom));

		scn.FreepsPass();          // move 1 -> 2, to Ettenmoors, a plains
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(goldberry));
		assertEquals(2, scn.GetWoundsOn(tom));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Nor a move to a sanctuary that is no river (Rivendell Valley).
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(goldberry));
		assertEquals(2, scn.GetWoundsOn(tom));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtARiverGoldberryExertsToMakeACompanionOfTheChoiceStrengthPlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goldberry = scn.GetFreepsCard("goldberry");
		var tom = scn.GetFreepsCard("tom");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(goldberry, tom, bilbo);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();   // at Buckleberry Ferry, a river
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{sam, runner2});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertEquals(0, scn.GetWoundsOn(goldberry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(goldberry));
		scn.FreepsUseCardAction(goldberry);

		assertEquals(1, scn.GetWoundsOn(goldberry));
		// Any companion, in this skirmish or not; no ally and no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sam, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(goldberry, tom, bilbo, runner, runner2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still +1 at the next decision of that skirmish; gone in the next skirmish, a phase of its own.
		scn.ShadowPass();
		assertEquals(9, scn.GetStrength(aragorn));
		scn.FreepsPass();
		assertInDiscard(runner);
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner2));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtTheFordOfBruinenARiverThatIsASanctuaryTheAbilityIsOfferedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goldberry = scn.GetFreepsCard("goldberry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(goldberry);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();   // at the Ford of Bruinen
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertEquals(0, scn.GetWoundsOn(goldberry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(goldberry));
		scn.FreepsUseCardAction(goldberry);
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(goldberry));
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhereTheFellowshipIsAtNoRiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlainsScenario();

		var goldberry = scn.GetFreepsCard("goldberry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(goldberry);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();   // at Ettenmoors, a plains
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(0, scn.GetWoundsOn(goldberry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(goldberry));
	}

	@Test
	public void NotOfferedWhileGoldberryIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goldberry = scn.GetFreepsCard("goldberry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(goldberry);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();   // at Buckleberry Ferry, a river
		// Vitality 6 with 5 wounds, laid after the move that would have healed one.
		scn.AddWoundsToChar(goldberry, 5);
		assertEquals(5, scn.GetWoundsOn(goldberry));
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(5, scn.GetWoundsOn(goldberry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(goldberry));
	}
}
