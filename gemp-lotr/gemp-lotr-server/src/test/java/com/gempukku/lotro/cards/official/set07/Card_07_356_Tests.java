package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_356_Tests
{

	// The King path, Cross Roads its site 8.  Site 7 is Osgiliath Channel (a regroup ability that needs 6
	// companions) and site 9 Dagorlad.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier", "4_254");    // Southron Soldier: [raider] Man, Southron, ambush (2) and nothing else
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, Easterling; a bonus only at 2 burdens
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, no Man; his bonuses need 3 burdens.
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, so that threats can lie
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: a companion; strength +3 only against a Nazgûl
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_336");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "7_356");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrossRoadsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Cross Roads
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 8
		 * Shadow Number: 8
		 * Direction: Right
		 * Game Text: <b>Shadow:</b> Spot 2 [raider] Men and remove a threat to add (2).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Cross Roads", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ShadowActionSpotsTwoRaiderMenAndRemovesAThreatToAddTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCompanionsToTable("aragorn", "arwen");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.RemoveThreats(scn.GetThreats());
		scn.AddThreats(2);
		scn.FreepsPass();

		var crossRoads = scn.GetCurrentSite();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("Cross Roads", crossRoads.getBlueprint().getTitle());
		// Two [raider] Men of two titles, a Southron and an Easterling.
		scn.MoveMinionsToTable(soldier, axeman);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertEquals(5, scn.GetTwilight());

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(crossRoads));
		scn.ShadowUseCardAction(crossRoads);

		assertEquals(1, scn.GetThreats());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneRaiderManBesideARaiderNazgulAndADunlandManUntilASecondRaiderManIsLaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable("aragorn", "arwen");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.RemoveThreats(scn.GetThreats());
		scn.AddThreats(2);
		scn.FreepsPass();

		var crossRoads = scn.GetCurrentSite();
		assertEquals(8, scn.GetCurrentSiteNumber());
		// One [raider] Man, a [raider] minion who is no Man, and a Man who is not [raider].
		scn.MoveMinionsToTable(soldier, khamul, brigand);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(crossRoads));

		scn.MoveMinionsToTable(axeman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(crossRoads));
		scn.ShadowUseCardAction(crossRoads);

		assertEquals(1, scn.GetThreats());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoThreatToRemoveAndIsOnceOneIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCompanionsToTable("aragorn", "arwen");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.RemoveThreats(scn.GetThreats());
		scn.FreepsPass();

		var crossRoads = scn.GetCurrentSite();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(soldier, axeman);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(crossRoads));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(crossRoads));
		scn.ShadowUseCardAction(crossRoads);

		assertEquals(0, scn.GetThreats());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
