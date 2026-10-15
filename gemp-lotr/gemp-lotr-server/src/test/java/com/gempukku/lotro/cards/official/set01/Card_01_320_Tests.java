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

public class Card_01_320_Tests
{

	// East Road is site 1; the rest of the path is that of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2; a skirmish ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion, cost 1; a bonus with a weapon only
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, cost 1; a skirmish ability never used
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1; "Bearer must be a Hobbit" only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, cost 6, site 4; no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "1_320");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EastRoadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: East Road
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: Each companion's twilight cost is +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("East Road", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachCompanionAtEastRoadCostsTwoMoreAndAnAllyAndAPossessionTheirPrintedNumbers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToHand(gimli, merry, bounder, sword, troop);
		scn.FreepsRefreshDecision();

		assertEquals("East Road", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		// Two companions of two cultures and races: 2 + 2, 1 + 2
		assertEquals(4, scn.GetTwilightCost(gimli));
		assertEquals(3, scn.GetTwilightCost(merry));
		// The ally and the possession their printed 1; the minion in the Shadow hand its printed 6
		assertEquals(1, scn.GetTwilightCost(bounder));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(6, scn.GetTwilightCost(troop));
		int twilight = scn.GetTwilight();

		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(twilight + 4, scn.GetTwilight());

		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(twilight + 4 + 1, scn.GetTwilight());

		scn.FreepsPlayCard(sword);
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(sword, frodo);
		assertEquals(twilight + 4 + 1 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOtherPlayersCompanionAtEastRoadOnHisOwnTurnCostsTwoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowGimli = scn.GetShadowCard("gimli");

		scn.StartGame();
		// Player 1's fellowship moves on to site 2; player 2's comes to his turn at site 1.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(shadowGimli);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals("East Road", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// 2 + 2
		assertEquals(4, scn.GetTwilightCost(shadowGimli));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(shadowGimli);

		assertInZone(Zone.FREE_CHARACTERS, shadowGimli);
		assertEquals(twilight + 4, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromEastRoadACompanionCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.MoveCardsToHand(gimli);

		// The turn begins with the fellowship at East Road: 2 + 2 while it is there.
		assertEquals("East Road", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(4, scn.GetTwilightCost(gimli));

		scn.SkipToSite(2);
		scn.MoveCardsToHand(gimli);

		// Site 2, Ettenmoors, with East Road behind the fellowship.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(gimli);
		assertEquals(2, scn.GetTwilightCost(gimli));
		int twilight = scn.GetTwilight();

		scn.FreepsPlayCard(gimli);

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
