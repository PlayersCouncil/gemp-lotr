package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_236_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: [gondor] Man companion, cost 4; a maneuver ability only
					put("aragorn", "1_89");
					// Sam, Son of Hamfast: [shire] Hobbit companion, cost 2; nothing needed to play him
					put("sam", "1_311");
					// Barliman Butterbur: [gandalf] ally, cost 0
					put("barliman", "1_70");
				}},
				// East Road as the site the game starts at, on a Shadows-block path.  Site 2 of that path is
				// Abandoned Mine Shaft 13_185, whose one text acts on a regroup move from it.
				new HashMap<>() {{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site1", "11_236");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void EastRoadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: East Road
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Forest</b>. Each companion is twilight cost +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("East Road");

		assertEquals("East Road", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void AtEastRoadEachCompanionIsTwilightCostTwoMoreAndAnAllyIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var road = scn.GetFreepsSite("East Road");
		scn.MoveCardsToHand(aragorn, sam, barliman);

		scn.StartGame(road);

		assertSame(road, scn.GetCurrentSite());
		assertInZone(Zone.HAND, aragorn, sam, barliman);
		// 4 + 2, 2 + 2, and the ally's printed 0
		assertEquals(6, scn.GetTwilightCost(aragorn));
		assertEquals(4, scn.GetTwilightCost(sam));
		assertEquals(0, scn.GetTwilightCost(barliman));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(aragorn);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(6, scn.GetTwilight());

		scn.FreepsPlayCard(barliman);

		assertInZone(Zone.SUPPORT, barliman);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftEastRoadACompanionCostsItsPrintedNumberAndTheOtherPlayerPaysTwoMoreThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		var road = scn.GetFreepsSite("East Road");
		scn.MoveCardsToHand(aragorn);

		scn.StartGame(road);

		assertEquals(6, scn.GetTwilightCost(aragorn));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));

		// The first player's own fellowship has gone on to site 2: East Road's text no longer applies.
		assertNotSame(road, scn.GetCurrentSite());
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second player's fellowship begins his turn at East Road, the first player's card.
		scn.SkipToOtherPlayersTurn();
		scn.SetShadowHand(shadowAragorn);
		scn.ShadowRefreshDecision();

		assertSame(road, scn.GetCurrentSite());
		assertEquals(6, scn.GetTwilightCost(shadowAragorn));
		int pool = scn.GetTwilight();

		scn.ShadowPlayCard(shadowAragorn);

		assertInZone(Zone.FREE_CHARACTERS, shadowAragorn);
		assertEquals(pool + 6, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
