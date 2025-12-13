package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_047_Tests
{

// ----------------------------------------
// DESERT WIND INITIATE TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("initiate", "103_47");    // Desert Wind Initiate

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertWindInitiateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desert Wind Initiate
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron.</b>  Tracker.  Ambush (3). 
		* 	When you play this minion, remove (3) or hinder this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("initiate");

		assertEquals("Desert Wind Initiate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(3, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}


	@Test
	public void DesertWindInitiateCanPayTwilightTaxToRemainActive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var initiate = scn.GetShadowCard("initiate");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(initiate);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		scn.ShadowPlayCard(initiate);

		// Required trigger - choose to remove (3)
		scn.ShadowChoose("Remove");

		// 1 to play + 3 tax + 2 roaming = 6 total removed
		assertEquals(twilightBefore - 4 - 2, scn.GetTwilight());
		assertFalse(scn.IsHindered(initiate));
	}

	@Test
	public void DesertWindInitiateCanHinderSelfInsteadOfPayingTax() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var initiate = scn.GetShadowCard("initiate");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(initiate);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		scn.ShadowPlayCard(initiate);
		scn.ShadowChoose("Hinder");

		// Only 1 to play (plus roaming), no tax
		assertEquals(twilightBefore - 1 - 2, scn.GetTwilight());
		assertTrue(scn.IsHindered(initiate));
	}

	@Test
	public void DesertWindInitiateMustHinderIfCannotAffordTax() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var initiate = scn.GetShadowCard("initiate");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(initiate);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(1); // Just enough to play, not enough for tax
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(initiate);

		// Should be forced to hinder (can't afford Remove 3)
		// Either auto-hindered or only hinder choice available
		assertTrue(scn.IsHindered(initiate));
	}

}
