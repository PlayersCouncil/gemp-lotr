package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_344_Tests
{

	// The King path, City Gates its site 5.  Site 2 is Rohirrim Camp (a text about roaming minions only), site 4
	// Osgiliath Fallen (a skirmish ability needing a minion) and site 6 Minas Tirith Seventh Circle, whose regroup-move
	// heal reaches [elven] companions only.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: an archery ability only
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_336");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_344");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CityGatesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: City Gates
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 5
		 * Shadow Number: 6
		 * Direction: Left
		 * Game Text: When the fellowship moves to this site, add 3 threats. When the fellowship moves from this site, remove 3 threats.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("City Gates", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void MovingToCityGatesAddsThreeThreatsAndMovingOnFromItRemovesThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowSite(5);
		// Frodo and four companions: room for five threats, so that a fourth threat added could be read.
		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli", "legolas");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AddThreats(1);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetThreats());
		assertEquals(5, scn.GetThreatLimit());

		scn.FreepsPass();

		assertSame(gates, scn.GetCurrentSite());
		// 1 + 3, and none removed on the way in.
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(4, scn.GetThreats());
		scn.FreepsChooseToMove();

		assertEquals(6, scn.GetCurrentSiteNumber());
		// 4 - 3, and none added on the way out.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
