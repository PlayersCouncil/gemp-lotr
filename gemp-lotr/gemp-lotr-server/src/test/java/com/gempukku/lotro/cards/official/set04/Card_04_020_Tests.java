package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chief", "4_20");

					// Goblin Patrol Troop: a minion with no text, only ever in the draw deck here
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HillChiefStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hill Chief
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Shadow:</b> Spot a site you control and exert Hill Chief to draw 3 cards.
		*/

		var scn = GetScenario();

		var chief = scn.GetFreepsCard("chief");

		assertEquals("Hill Chief", chief.getBlueprint().getTitle());
		assertNull(chief.getBlueprint().getSubtitle());
		assertTrue(chief.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, chief.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, chief.getBlueprint().getCulture());
		assertEquals(CardType.MINION, chief.getBlueprint().getCardType());
		assertEquals(Race.MAN, chief.getBlueprint().getRace());
		assertEquals(4, chief.getBlueprint().getTwilightCost());
		assertEquals(10, chief.getBlueprint().getStrength());
		assertEquals(2, chief.getBlueprint().getVitality());
		assertEquals(3, chief.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsHillChiefToDrawThreeCardsUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chief = scn.GetShadowCard("chief");

		scn.StartGame();
		// Both markers are on site 3, so site 1 is behind every marker.  It is the other player's site,
		// controlled by the Shadow player.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(chief);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"),
				scn.GetShadowCard("troop3"), scn.GetShadowCard("troop4"));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(0, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDrawDeck().size());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(chief));
		scn.ShadowUseCardAction(chief);

		assertEquals(1, scn.GetWoundsOn(chief));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDrawDeck().size());

		// Vitality 2 with 1 wound: exhausted, so he can no longer exert, though the site is still his.
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(chief));
	}

	@Test
	public void NotOfferedWhileOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chief = scn.GetShadowCard("chief");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// Site 1 was taken by player 1 on player 2's turn.
		scn.FreepsTakeControlOfSite();
		scn.MoveMinionsToTable(chief);
		scn.SetShadowHand();
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetWoundsOn(chief));
		assertEquals(0, scn.GetShadowHandCount());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(chief));

		// Once a site is his own to spot, the same exertion buys the draw.
		scn.ShadowTakeControlOfSite(site2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowActionAvailable(chief));
		scn.ShadowUseCardAction(chief);
		assertEquals(1, scn.GetWoundsOn(chief));
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
