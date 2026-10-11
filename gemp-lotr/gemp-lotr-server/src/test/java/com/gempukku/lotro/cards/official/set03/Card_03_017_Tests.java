package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_017_Tests
{

	// The Fellowship-block path with a forest as site 2 and as site 3.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "3_17");
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, vitality 3; a fellowship ability used only on purpose
					put("greenleaf", "1_50");   // Legolas, Greenleaf: Elf companion, vitality 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, vitality 2; a maneuver ability
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_329");      // Breeland Forest: forest; stealth events may not be played
					put("site3", "1_342");      // Rivendell Waterfall: forest and sanctuary; its text needs a ranger there
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path with Ettenmoors, a plains, as site 2; the site after it is still a forest.
	protected VirtualTableScenario GetPlainsAheadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "3_17");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");      // Rivendell Waterfall: forest and sanctuary
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Towers-block path whose site 2 is a forest.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "3_17");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_330");      // Derndingle: forest; its text needs an unbound Hobbit there
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows-block path: the sites have no printed number, and whoever plays the next site chooses it.
	// Fangorn Glade ("site1"), The Angle ("site8") and Caras Galadhon ("site9") are forests.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "3_17");
				}},
				new HashMap<>() {{
					put("site1", "11_239");     // Fangorn Glade: forest
					put("site2", "13_185");     // Abandoned Mine Shaft: underground
					put("site3", "11_234");     // Crags of Emyn Muil: mountain
					put("site4", "17_148");     // Nurn: plains
					put("site5", "18_138");     // Sirannon Ruins: river
					put("site6", "11_230");     // Buckland Homestead: dwelling
					put("site7", "12_187");     // Emyn Muil: mountain
					put("site8", "12_185");     // The Angle: forest
					put("site9", "11_231");     // Caras Galadhon: forest
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}


	@Test
	public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Galadriel, Lady of the Golden Wood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Home: 6
		 * Game Text: At the start of each of your turns, you may heal an Elf.<br><b>Fellowship:</b> Exert Galadriel to play the fellowship's next site if it is a forest (replacing opponent's site if necessary).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("galadriel");

		assertEquals("Galadriel", card.getBlueprint().getTitle());
		assertEquals("Lady of the Golden Wood", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));

	}

	@Test
	public void IsPlayedFromHandForThreeToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveCardsToHand(galadriel);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(galadriel));

		scn.FreepsPlayCard(galadriel);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealAbilityTriggersOnElves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(greenleaf, aragorn);
		scn.MoveAlliesToTable(galadriel, celeborn, thrarin);

		scn.AddWoundsToChar(galadriel, 1);
		scn.AddWoundsToChar(celeborn, 1);
		scn.AddWoundsToChar(greenleaf, 2);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(thrarin, 1);

		scn.StartGame();

		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsAcceptOptionalTrigger();

		// Galadriel herself, an Elf ally and an Elf companion; not the Man, and not the Dwarf ally.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(galadriel, celeborn, greenleaf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, thrarin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(greenleaf);

		// One wound of his two.
		assertEquals(1, scn.GetWoundsOn(greenleaf));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedItHealsNobodyAndIsOfferedAgainAtHerOwnersNextTurnAndNotAtTheOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(galadriel, celeborn);
		scn.AddWoundsToChar(galadriel, 1);
		scn.AddWoundsToChar(celeborn, 2);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The other player's turn begins with his fellowship phase: nobody is offered the heal.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(2, scn.GetWoundsOn(celeborn));

		scn.SkipToOtherPlayersTurn();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(celeborn);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	@Ignore("F-0393: the start-of-turn heal is offered with no wounded Elf (Q-0034); withheld by the engine since Q-0224 (engine pass 26), which this branch does not have yet")
	public void HealIsNotOfferedWhenNoElfIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(greenleaf, aragorn);
		scn.MoveAlliesToTable(galadriel, thrarin);
		// Wounded, but neither is an Elf.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(thrarin, 1);

		scn.StartGame();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(thrarin));
	}

	@Test
	public void FellowshipActionPlaysNextFellowshipForestSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();
		scn.MoveAlliesToTable(galadriel);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(galadriel));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertInZone(Zone.ADVENTURE_DECK, forestSite);
		assertNull(scn.GetSite(2));
		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsUseCardAction(galadriel);

		// Her owner's own Breeland Forest is site 2, where a move would have had the Shadow player lay his; the
		// fellowship has not moved.
		assertSame(forestSite, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, forestSite);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertNull(scn.GetSite(3));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With his own site lying next she may still exert, and nothing is played: he has no other site 2.
		assertTrue(scn.FreepsActionAvailable(galadriel));
		scn.FreepsUseCardAction(galadriel);
		assertSame(forestSite, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertNull(scn.GetSite(3));
		assertEquals(2, scn.GetWoundsOn(galadriel));

		// Vitality 3 with 2 wounds: she cannot exert.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(galadriel));
	}

	@Test
	public void WhenTheNextSiteIsNotAForestGaladrielExertsAndNoSiteIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlainsAheadScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var ettenmoors = scn.GetFreepsSite(2);
		var waterfall = scn.GetFreepsSite(3);

		scn.StartGame();
		scn.MoveAlliesToTable(galadriel);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(galadriel));
		// The condition stands after the "to": the ability is offered, and its cost is paid for nothing.
		assertTrue(scn.FreepsActionAvailable(galadriel));
		scn.FreepsUseCardAction(galadriel);

		// The next site is a plains.  The forest after it is not the next site.
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertNull(scn.GetSite(2));
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, ettenmoors);
		assertInZone(Zone.ADVENTURE_DECK, waterfall);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionReplacesTheOpponentsSiteThatLiesNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var waterfall = scn.GetFreepsSite(3);
		var shadowWaterfall = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveAlliesToTable(galadriel);

		// The other player's Rivendell Waterfall lies at site 3, as it does where he has played his next site
		// himself.  Both players hold the same nine sites here, so the site that replaces it is its twin.
		scn.MoveCardToAdventurePath(shadowWaterfall);
		scn.FreepsRefreshDecision();

		assertSame(shadowWaterfall, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, waterfall);
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsActionAvailable(galadriel));

		scn.FreepsUseCardAction(galadriel);

		assertSame(waterfall, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, waterfall);
		assertInZone(Zone.ADVENTURE_DECK, shadowWaterfall);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionPlaysNextTowersForestSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var forestSite = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();
		scn.MoveAlliesToTable(galadriel);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(galadriel));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertInZone(Zone.ADVENTURE_DECK, forestSite);
		assertNull(scn.GetSite(2));
		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsUseCardAction(galadriel);

		assertSame(forestSite, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionPlaysNextShadowsForestSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var site1 = scn.GetFreepsSite("site1");
		var mineShaft = scn.GetFreepsSite("site2");
		var angle = scn.GetFreepsSite("site8");
		var forestSite = scn.GetFreepsSite("site9");
		var shadowForest = scn.GetShadowSite("site9");

		scn.StartGame(site1);
		scn.MoveAlliesToTable(galadriel);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(galadriel));
		assertInZone(Zone.ADVENTURE_DECK, forestSite);
		assertNull(scn.GetSite(2));
		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsUseCardAction(galadriel);
		assertEquals(1, scn.GetWoundsOn(galadriel));

		// He chooses among the forests left in his adventure deck, and must choose one.
		assertTrue(scn.FreepsDecisionAvailable("Choose site to play"));
		assertEquals(2, scn.FreepsGetBPChoiceCount());
		assertTrue(scn.FreepsHasBPChoice(angle));
		assertTrue(scn.FreepsHasBPChoice(forestSite));
		assertFalse(scn.FreepsHasBPChoice(mineShaft));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(forestSite);

		assertSame(forestSite, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowForest);
		assertInZone(Zone.ADVENTURE_DECK, angle);
		assertNull(scn.GetSite(3));
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
