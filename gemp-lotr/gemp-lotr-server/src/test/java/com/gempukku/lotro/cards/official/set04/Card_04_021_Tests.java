package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_21");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "4_21");
					put("brigand", "4_10");     // Dunlending Brigand: the other [dunland] Man, with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HillmanBandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hillman Band
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time the fellowship moves, you may spot another [dunland] Man to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Hillman Band", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledTheMoveOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var band = scn.GetShadowCard("band");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(band, brigand);

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledTheMoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var band = scn.GetShadowCard("band");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn.  Site 2 has
		// both markers on it as the move is answered (Q-0184), so there is nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveMinionsToTable(band, brigand);

		assertTrue(scn.FreepsControls(site1));

		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(band));
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.IsSiteControlled(site2));
		assertEquals(3, scn.GetCurrentSiteNumber());
	}
}
