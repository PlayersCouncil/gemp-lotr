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

public class Card_01_328_Tests
{

	// Bree Streets is site 2 of the Fellowship path, Rivendell Valley site 3.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ulaire Lemenya, Lieutenant of Morgul: [wraith] Nazgul, cost 4, site number 3; her text
					// needs a [wraith] condition on a companion
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul, cost 6, site number 3; his text needs
					// burdens
					put("khamul", "103_61");
					// Ulaire Nelya, Ringwraith in Twilight: Nazgul, cost 5, site number 2, so not roaming at
					// site 2; her text needs burdens or wounds on the Ring-bearer
					put("nelya", "2_84");
					// Morgul Hound: a [wraith] minion who is no Nazgul (an Orc), cost 2, site number 4; its text
					// needs wounds on the Ring-bearer
					put("hound", "7_192");
					// Winged Nazgul: a [wraith] Nazgul of two dots, cost 4, site number 4, so roaming at site 3 as
					// well; enduring and fierce, and an assignment ability used only on purpose
					put("winged", "103_81");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_328");
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
	public void BreeStreetsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Bree Streets
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: The roaming penalty for each Nazgûl you play to Bree Streets is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Bree Streets", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ANazgulPlayedToBreeStreetsPaysTwoLessForRoamingAndNoOtherMinionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var winged = scn.GetShadowCard("winged");
		scn.MoveCardsToHand(lemenya, khamul, nelya, hound, winged);

		scn.StartGame();
		scn.SetTwilight(24);
		scn.FreepsPass();

		assertSame(scn.GetShadowSite(2), scn.GetCurrentSite());
		// 24 set + 1 companion + 1 for Bree Streets
		assertEquals(26, scn.GetTwilight());

		scn.ShadowPlayCard(lemenya);
		// 4 to play + 2 roaming - 2.  The penalty is less; the minion is no less roaming.
		assertEquals(22, scn.GetTwilight());
		assertTrue(scn.HasKeyword(lemenya, Keyword.ROAMING));

		scn.ShadowPlayCard(khamul);
		// A [raider] Nazgul: 6 to play + 2 roaming - 2
		assertEquals(16, scn.GetTwilight());
		assertTrue(scn.HasKeyword(khamul, Keyword.ROAMING));

		scn.ShadowPlayCard(winged);
		// A Nazgul who is not unique: 4 to play + 2 roaming - 2
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.HasKeyword(winged, Keyword.ROAMING));

		scn.ShadowPlayCard(hound);
		// A [wraith] Orc and no Nazgul: 2 to play + 2 roaming
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.HasKeyword(hound, Keyword.ROAMING));

		scn.ShadowPlayCard(nelya);
		// A Nazgul with no roaming penalty to make less pays her printed 5: the site takes nothing off her cost.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFirstPlayerPlaysANazgulToTheBreeStreetsTheSecondPlayerLaidAndPaysTwoLessForRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetFreepsCard("lemenya");
		var breeStreets = scn.GetShadowSite(2);

		scn.StartGame();
		scn.FreepsPass();
		// The second player laid his Bree Streets for the first player's move.
		assertSame(breeStreets, scn.GetCurrentSite());

		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(lemenya);
		scn.SetTwilight(10);
		// The second player's fellowship moves to the same site 2, and the first player is the Shadow player.
		scn.ShadowPass();

		assertSame(breeStreets, scn.GetCurrentSite());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		// 10 set + 1 companion + 1 for Bree Streets
		assertEquals(12, scn.GetTwilight());

		scn.FreepsPlayCard(lemenya);
		// 4 to play + 2 roaming - 2: the site is the other player's, and its text is for whoever plays the Nazgul.
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.HasKeyword(lemenya, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void OnceTheFellowshipHasLeftBreeStreetsANazgulPaysTheWholeRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var winged = scn.GetShadowCard("winged");

		scn.StartGame();
		scn.FreepsPass();
		assertSame(scn.GetShadowSite(2), scn.GetCurrentSite());

		// With no minion in play the game goes from the Shadow phase straight to regroup, and the fellowship
		// moves on to Rivendell Valley.
		scn.SkipToMovementDecision();
		scn.MoveCardsToHand(winged);
		scn.SetTwilight(10);
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 10 set + 1 companion + 0 for Rivendell Valley
		assertEquals(11, scn.GetTwilight());

		scn.ShadowPlayCard(winged);
		// 4 to play + 2 roaming: Bree Streets' text does not apply once the fellowship is elsewhere (CR 4.2 "active").
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.HasKeyword(winged, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, winged);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
