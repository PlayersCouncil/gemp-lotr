package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_245_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("archer", "54_245");

					put("scout", "4_252");      // Southron Scout: [raider] Southron Man minion, cost 1; Ambush (2) and no other text
					put("explorer", "4_250");   // Southron Explorer: [raider] Southron Man minion; Regroup: exert him and remove (5) to take control of a site
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man minion that is no Southron; his text needs 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronArcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Archer
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>.
		 * While you can spot another Southron, the minion archery total is +1 for each site you control.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("archer");

		assertEquals("Southron Archer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedForThreeAndAddsOnlyItselfUntilAnotherSouthronIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var scout = scn.GetShadowCard("scout");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(archer, scout);

		scn.StartGame();
		scn.SkipToSite(4);
		// Every marker is past sites 1 to 3: the Shadow player takes two, the Free Peoples player the third.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Easterling is a [raider] Man but no Southron.
		scn.MoveMinionsToTable(axeman);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		// 10 set + 1 companion + 6 for The Bridge of Khazad-dum
		assertEquals(17, scn.GetTwilight());
		assertEquals(0, scn.GetShadowArcheryTotal());

		// A site-4 minion at site 5: no roaming.
		scn.ShadowPlayCard(archer);
		assertInHand(scout);
		assertInZone(Zone.SHADOW_CHARACTERS, archer);
		assertEquals(14, scn.GetTwilight());
		// The archer alone: he is no "other" Southron, and the Easterling is none at all.
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.ShadowPlayCard(scout);
		// 1 for the archer + 1 for each of the Shadow player's 2 sites; the Free Peoples player's site does not count.
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TotalRisesByOneForEachSiteTheShadowPlayerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();
		// No minion in the Shadow phase, so the game goes on to regroup at site 5.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(archer, explorer);
		scn.SetTwilight(5);

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.ShadowTakeControlOfSite();
		assertEquals(2, scn.GetShadowArcheryTotal());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(explorer));
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(3, scn.GetShadowArcheryTotal());

		// A site the other player controls adds nothing.
		scn.FreepsTakeControlOfSite();
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
