package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destroyer", "17_124");
					// Southron Explorer: [raider] Man, vitality 3, no hunter. "Regroup: Exert this minion and remove
					// (5) to take control of a site."
					put("explorer", "4_250");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text at all, so no hunter
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandDestroyerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Destroyer
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.<br>This minion gains <b>hunter 3</b> for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("destroyer");

		assertEquals("White Hand Destroyer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ItGainsHunter3ForEachSiteItsOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyer = scn.GetShadowCard("destroyer");
		var explorer = scn.GetShadowCard("explorer");
		var troop = scn.GetShadowCard("troop");

		// The first player's third turn: in its regroup phase sites 1 and 2 lie behind both markers.
		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.REGROUP);
		// Laid at the start of the regroup phase, where minions that lived through the skirmishes stand.
		scn.MoveMinionsToTable(destroyer, explorer, troop);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();
		assertEquals(10, scn.GetTwilight());

		// No site controlled: no hunter.
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.HasKeyword(destroyer, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(destroyer, Keyword.HUNTER));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(explorer));
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.HasKeyword(destroyer, Keyword.HUNTER));
		assertEquals(3, scn.GetKeywordCount(destroyer, Keyword.HUNTER));
		// Another minion of the same player gains nothing.
		assertFalse(scn.HasKeyword(troop, Keyword.HUNTER));

		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		// 3 for each of two sites.
		assertEquals(6, scn.GetKeywordCount(destroyer, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(troop, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASiteTheFreePeoplesPlayerControlsAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyer = scn.GetShadowCard("destroyer");

		scn.StartGame();
		scn.SkipToSite(3);
		// Site 1 is the Free Peoples player's, as if taken on one of the other player's turns, and site 2 the
		// Shadow player's.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		// Laid early, as it will stand in play later in the turn (Q-0228).
		scn.MoveMinionsToTable(destroyer);
		scn.FreepsRefreshDecision();

		// The Shadow player's one site only: 3, not 6.
		assertEquals(3, scn.GetKeywordCount(destroyer, Keyword.HUNTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
