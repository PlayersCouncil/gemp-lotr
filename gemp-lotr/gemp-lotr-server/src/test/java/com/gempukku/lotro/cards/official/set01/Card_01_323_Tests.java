package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_323_Tests
{

	// Green Hill Country is site 1; the rest of the path is that of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, cost 2; a fellowship ability used only on purpose
					put("holbytla", "103_85");  // Merry, Master Holbytla: [rohan] Hobbit companion, cost 2; his trigger answers only a [rohan] Man exerting
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, cost 2; a fellowship ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: [shire] companion who is no Hobbit, cost 1
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2; a skirmish ability used only on purpose
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1, "Hobbit" in its title; "Bearer must be a Hobbit" only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "1_323");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Green Hill Country is site 1, where the first turn of the game begins; the rest is the usual Fellowship path.
	protected VirtualTableScenario GetFirstTurnScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit of cost 1
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit of cost 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: no Hobbit, cost 4
				}},
				new HashMap<>() {{
					put("site1", "1_323");
					put("site2", "1_331");
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
	public void GreenHillCountryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Green Hill Country
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: The twilight cost of each Hobbit is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Green Hill Country", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachHobbitAtGreenHillCountryCostsOneLessAndCardsThatAreNoHobbitsTheirPrintedNumbers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var holbytla = scn.GetFreepsCard("holbytla");
		var bilbo = scn.GetFreepsCard("bilbo");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");

		scn.StartGame();
		scn.MoveCardsToHand(sam, holbytla, bilbo, smeagol, gimli, sword);
		scn.FreepsRefreshDecision();

		assertEquals("Green Hill Country", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		// Hobbit companions of two cultures and a Hobbit ally: 2 - 1 each (a discount of 2 would read 0)
		assertEquals(1, scn.GetTwilightCost(sam));
		assertEquals(1, scn.GetTwilightCost(holbytla));
		assertEquals(1, scn.GetTwilightCost(bilbo));
		// A [shire] companion who is no Hobbit, a companion of another race, a [shire] possession named for
		// Hobbits: their printed numbers
		assertEquals(1, scn.GetTwilightCost(smeagol));
		assertEquals(2, scn.GetTwilightCost(gimli));
		assertEquals(1, scn.GetTwilightCost(sword));
		int twilight = scn.GetTwilight();

		scn.FreepsPlayCard(bilbo);
		assertInZone(Zone.SUPPORT, bilbo);
		assertEquals(twilight + 1, scn.GetTwilight());

		scn.FreepsPlayCard(sword);
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(sword, frodo);
		assertEquals(twilight + 1 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOtherPlayersHobbitAtGreenHillCountryOnHisOwnTurnCostsOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowSam = scn.GetShadowCard("sam");

		scn.StartGame();
		// Player 1's fellowship moves on to site 2; player 2's comes to his turn at site 1.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(shadowSam);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals("Green Hill Country", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// 2 - 1
		assertEquals(1, scn.GetTwilightCost(shadowSam));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(shadowSam);

		assertInZone(Zone.FREE_CHARACTERS, shadowSam);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromGreenHillCountryAHobbitCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");

		scn.StartGame();
		scn.MoveCardsToHand(sam);

		// The turn begins with the fellowship at Green Hill Country: 2 - 1 while it is there.
		assertEquals("Green Hill Country", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetTwilightCost(sam));

		scn.SkipToSite(2);
		scn.MoveCardsToHand(sam);

		// Site 2, Ettenmoors, with Green Hill Country behind the fellowship.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(sam);
		assertEquals(2, scn.GetTwilightCost(sam));
		int twilight = scn.GetTwilight();

		scn.FreepsPlayCard(sam);

		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitPlayedInTheFirstFellowshipPhaseOfTheGameCostsOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFirstTurnScenario();

		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(merry, sam, aragorn);

		scn.StartGame();

		// The first fellowship phase of the game, at the first player's own site 1.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals("Green Hill Country", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(merry));

		scn.FreepsPlayCard(merry);
		// 1 - 1
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, merry);

		scn.FreepsPlayCard(sam);
		// 2 - 1: one less, not nothing
		assertEquals(1, scn.GetTwilight());

		scn.FreepsPlayCard(aragorn);
		// No Hobbit: his 4 in full
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
