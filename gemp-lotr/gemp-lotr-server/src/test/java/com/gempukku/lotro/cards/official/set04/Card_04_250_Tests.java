package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_250_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("explorer1", "4_250");
					put("explorer2", "4_250");   // a second copy: not unique
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronExplorerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Explorer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br><b>Regroup:</b> Exert this minion and remove (5) to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("explorer1");

		assertEquals("Southron Explorer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityExertsThisMinionAndRemovesFiveToTakeControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");

		scn.StartGame();
		// Player 1's second turn: both markers on site 2, and a move to site 3 ahead.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(explorer1, explorer2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);

		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetWoundsOn(explorer1));
		assertEquals(0, scn.GetWoundsOn(explorer2));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(explorer1));
		scn.ShadowUseCardAction(explorer1);

		// The copy used exerts, the other copy does not; (5) is removed.
		assertEquals(1, scn.GetWoundsOn(explorer1));
		assertEquals(0, scn.GetWoundsOn(explorer2));
		assertEquals(0, scn.GetTwilight());
		// Site 1, the lowest site every marker has passed, is his.
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var explorer1 = scn.GetShadowCard("explorer1");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer1);
		// The first turn: the fellowship moves to site 2 and the other player's marker stays on site 1.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		var site1 = scn.GetSite(1);

		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertEquals(0, scn.GetWoundsOn(explorer1));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(explorer1));
		scn.ShadowUseCardAction(explorer1);

		// Both costs are paid, and no site is taken.
		assertEquals(1, scn.GetWoundsOn(explorer1));
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(explorer1, explorer2);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(explorer1, 2);
		assertEquals(2, scn.GetWoundsOn(explorer1));
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);

		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(explorer1));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(explorer1));
		// The other copy, able to exert, is offered its own.
		assertTrue(scn.ShadowActionAvailable(explorer2));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithFourTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var explorer1 = scn.GetShadowCard("explorer1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(explorer1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(4);

		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(explorer1));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(explorer1));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(explorer1));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
