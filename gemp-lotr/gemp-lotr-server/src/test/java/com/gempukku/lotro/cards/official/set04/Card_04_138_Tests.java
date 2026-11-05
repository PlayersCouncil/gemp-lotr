package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_138_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bowmen", "4_138");

					// Southron Explorer: strength 5, vitality 3, no archer. "Regroup: Exert this minion and remove
					// (5) to take control of a site."
					put("explorer", "4_250");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BandofUrukBowmenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Band of Uruk Bowmen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Archer</b>. <b>Damage +1</b>.<br>While you control a site, the minion archery total is +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bowmen");

		assertEquals("Band of Uruk Bowmen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void MinionArcheryTotalIsPlus1OnceItsOwnerTakesControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowmen = scn.GetShadowCard("bowmen");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(explorer);
		// The fellowship moves to site 3; both markers have passed site 1.
		scn.SkipToPhase(Phase.REGROUP);
		// The archer is laid once the archery phase is behind, where minions that lived through it stand.
		scn.MoveMinionsToTable(bowmen);
		scn.SetTwilight(10);

		// The archer's own 1, and no site controlled.
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(site1));
		// 1 for the archer + 1 while a site is controlled
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MinionArcheryTotalIsNotPlus1WhileOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowmen = scn.GetShadowCard("bowmen");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// The Free Peoples player took site 1 on a turn of his as the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.MoveMinionsToTable(bowmen);

		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		// The archer's own 1 alone.
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.FreepsPass();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
