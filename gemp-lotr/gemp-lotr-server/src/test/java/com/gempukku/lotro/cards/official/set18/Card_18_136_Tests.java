package com.gempukku.lotro.cards.official.set18;

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

public class Card_18_136_Tests
{
	// Mithlond stands in each adventure deck in place of site 9, which no test reaches, and a test puts the Shadow
	// player's copy on the Fellowship-block path as the next site the fellowship moves to.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion, vitality 4
					put("gimli", "1_13");         // Gimli, Son of Glóin: a companion, vitality 3, left unwounded
					put("gaffer", "1_291");       // The Gaffer: an ally, vitality 2; a fellowship ability never used here
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("mithlond", "18_136");
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
		 * Game Text: <b>River</b>. When the fellowship moves to this site, each player heals each of his or her
		 * 	characters.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("mithlond");

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
	public void MovingToMithlondHealsEachCharacterOfEachPlayerOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(gaffer);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		var mithlond = scn.GetShadowSite("mithlond");

		// The fellowship moves to site 2 in the fellowship phase.
		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Two minions that have come through the turn's skirmishes, of two cultures and two races: the Shadow
		// player has minions on the table at a move only in the regroup phase.
		scn.MoveMinionsToTable(troop);
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gaffer, 1);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));

		// Mithlond is the next site, and the fellowship moves to it in the regroup phase.
		scn.MoveCardToAdventurePath(mithlond);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(mithlond, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
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
	public void MithlondHealsAsTheFellowshipMovesToItAndNotAsItMovesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(gaffer);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		var mithlond = scn.GetShadowSite("mithlond");

		scn.StartGame();
		// Mithlond is site 2, and the fellowship moves to it in the fellowship phase.
		scn.MoveCardToAdventurePath(mithlond);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();

		assertEquals(mithlond, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop);
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gaffer, 1);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));

		// The regroup move from Mithlond to site 3 heals nobody.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(mithlond, scn.GetSite(2));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
