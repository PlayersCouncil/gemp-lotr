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

public class Card_01_335_Tests
{

	// Weatherhills is site 2; the rest of the path is that of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Úlairë Enquëa, Lieutenant of Morgul: [wraith] Nazgul, cost 6, site 3; a maneuver ability used only on purpose
					put("enquea", "1_231");
					// Ulaire Attea, Khamul of the East: [raider] Nazgul, cost 6, site 3; strength and damage for
					// Easterlings at 3 and 5 burdens
					put("khamul", "103_61");
					// Morgul Hound: [wraith] Orc, no Nazgul, cost 2, site 4; stronger at 2 wounds on the Ring-bearer
					put("hound", "7_192");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeatherhillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Weatherhills
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: Each Nazgûl's twilight cost is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Weatherhills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtWeatherhillsEachNazgulCostsOneLessAndAWraithOrcItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(enquea, khamul, hound);

		scn.StartGame();
		scn.SetTwilight(20);

		// At site 1 the printed numbers
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetTwilightCost(enquea));
		assertEquals(6, scn.GetTwilightCost(khamul));
		assertEquals(2, scn.GetTwilightCost(hound));

		scn.FreepsPass();

		assertEquals("Weatherhills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Nazgul of two cultures: 6 - 1 each.  The [wraith] Orc: his printed 2.
		assertEquals(5, scn.GetTwilightCost(enquea));
		assertEquals(5, scn.GetTwilightCost(khamul));
		assertEquals(2, scn.GetTwilightCost(hound));
		// 20 set + 3 for the site + 1 companion
		assertEquals(24, scn.GetTwilight());

		scn.ShadowPlayCard(enquea);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		// 6 - 1 + 2 roaming
		assertEquals(24 - 7, scn.GetTwilight());

		scn.ShadowPlayCard(khamul);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertEquals(24 - 7 - 7, scn.GetTwilight());

		scn.ShadowPlayCard(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		// 2 + 2 roaming
		assertEquals(24 - 7 - 7 - 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromWeatherhillsANazgulCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.FreepsPass();

		assertEquals("Weatherhills", scn.GetCurrentSite().getBlueprint().getTitle());

		// The regroup move takes the fellowship on to site 3, Rivendell Valley.
		scn.SkipToMovementDecision();
		scn.MoveCardsToHand(enquea);
		scn.SetTwilight(10);
		assertInHand(enquea);
		assertEquals(5, scn.GetTwilightCost(enquea));
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(enquea);
		assertEquals(6, scn.GetTwilightCost(enquea));
		// 10 set + 0 for the site + 1 companion
		assertEquals(11, scn.GetTwilight());

		scn.ShadowPlayCard(enquea);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		// His printed 6, and no roaming at site 3
		assertEquals(11 - 6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
