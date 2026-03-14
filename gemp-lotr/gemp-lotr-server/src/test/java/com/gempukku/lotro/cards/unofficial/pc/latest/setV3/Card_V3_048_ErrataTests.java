package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_048_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_48");
					put("initiate", "103_47");   // Desert Wind Initiate - another [raider] minion
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertWindScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desert Wind Scout
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron.</b>  Tracker.  Ambush (2).
		* 	When you play this minion, remove (3) or hinder this minion.
		* 	Each time this minion is hindered, you may hinder or restore another [raider] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Desert Wind Scout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DesertWindScoutCanRestoreAnotherRaiderWhenHindered() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata changes: when hindered, offers choice of hinder OR restore another [raider] minion
		// (original only offered hinder)
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var initiate = scn.GetShadowCard("initiate");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(card, initiate);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// First play the Initiate and hinder it (so we have a hindered raider to restore)
		scn.ShadowPlayCard(initiate);
		scn.ShadowChoose("Hinder");
		assertTrue(scn.IsHindered(initiate));

		// Now play the Scout and choose to hinder self
		scn.ShadowPlayCard(card);
		scn.ShadowChoose("Hinder");

		// The AboutToHinder trigger fires - offering choice of hinder or restore
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Errata: choose to restore the hindered initiate
		scn.ShadowChoose("Restore");

		// Initiate should now be restored
		assertFalse(scn.IsHindered(initiate));
		// Scout should still be hindered
		assertTrue(scn.IsHindered(card));
	}
}
