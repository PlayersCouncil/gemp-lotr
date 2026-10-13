package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_136_ErrataTests
{
	// Region 3 is sites 7 to 9, which the turn-skippers reach only on a Fellowship-block path. Mithlond stands
	// in each adventure deck in place of site 9, which no test reaches, and a test puts the Shadow player's
	// copy on the path as the next site the fellowship moves to.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion, vitality 4
					put("gimli", "1_13");         // Gimli, Son of Glóin: a companion, vitality 3
					put("gaffer", "1_291");       // The Gaffer: an ally, vitality 2; a fellowship ability never used here
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					// The game takes an adventure deck whose first site has no site number of its own for a
					// Shadows-block deck, and then asks which site to lay at every move. A map lists its keys
					// in the order of their hashes, and this key comes after all of "site1" to "site8".
					put("Mithlond", "68_136");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MithlondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Mithlond
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>River.</b> When the fellowship moves to this site in region 3, each player heals each
		 * 	of their characters.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("Mithlond");

		assertEquals("Mithlond", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void AsSiteSevenMithlondHealsEachCharacterOfEachPlayerOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(gaffer);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		var mithlond = scn.GetShadowSite("Mithlond");

		// The fellowship moves from site 5 to site 6, the last site of region 2, in the fellowship phase.
		scn.StartGame();
		scn.SkipToSite(5);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(6, scn.GetCurrentSiteNumber());

		// Two minions that have come through the turn's skirmishes, of two cultures and two races.
		scn.MoveMinionsToTable(troop);
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gaffer, 1);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 2);
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Mithlond is the next site, site 7: the first site of region 3.
		scn.MoveCardToAdventurePath(mithlond);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(mithlond, scn.GetCurrentSite());
		assertEquals(7, scn.GetCurrentSiteNumber());
		// One wound comes off each character that has one: the Ring-bearer, a companion, an ally and both minions.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		// Nobody was asked anything on the way.
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AsSiteSixInRegionTwoMithlondHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(gaffer);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		var mithlond = scn.GetShadowSite("Mithlond");

		// The same board one site earlier: the fellowship moves from site 4 to site 5 in the fellowship phase.
		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, scn.GetCurrentSiteNumber());

		scn.MoveMinionsToTable(troop);
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gaffer, 1);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 2);

		// Mithlond is the next site, site 6: the last site of region 2.
		scn.MoveCardToAdventurePath(mithlond);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(mithlond, scn.GetCurrentSite());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AsSiteEightMithlondHealsToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var mithlond = scn.GetShadowSite("Mithlond");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Mithlond is the next site, site 8, and the fellowship moves to it in the fellowship phase.
		scn.MoveCardToAdventurePath(mithlond);
		scn.FreepsPass();

		assertEquals(mithlond, scn.GetCurrentSite());
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
