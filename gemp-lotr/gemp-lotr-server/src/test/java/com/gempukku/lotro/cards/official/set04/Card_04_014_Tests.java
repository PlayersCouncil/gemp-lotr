package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_14");
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
					put("ransacker", "4_14");
					put("brigand", "4_10");     // Dunlending Brigand: the other [dunland] Man, with no text
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingRansackerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dunlending Ransacker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may spot another [dunland] Man to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Dunlending Ransacker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledWinningASkirmishOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var brigand = scn.GetShadowCard("brigand");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(ransacker, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 7 against 6: the Knight loses, and takes a wound.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledWinningASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var brigand = scn.GetShadowCard("brigand");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(ransacker, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		// The Ransacker has won, beside the other [dunland] Man.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}
}
