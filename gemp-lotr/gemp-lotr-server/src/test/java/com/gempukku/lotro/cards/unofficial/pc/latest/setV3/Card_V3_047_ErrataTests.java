package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_047_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_47");
					put("southron1", "4_222");   // Desert Warrior - Southron Man, cost 2
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
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron.</b>  Tracker. Ambush (3).
		* 	Other Southrons are twilight cost -1.
		* 	When you play this minion, remove (3) or hinder this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

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
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DesertWindInitiateReducesOtherSouthronCostBy1() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata adds: Other Southrons are twilight cost -1
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var southron1 = scn.GetShadowCard("southron1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(card);
		scn.MoveCardsToHand(southron1);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		// Desert Warrior (southron1) normally costs 2, with -1 from Initiate = 1
		// Plus 2 for roaming penalty = 3 total
		scn.ShadowPlayCard(southron1);

		assertEquals(twilightBefore - 3, scn.GetTwilight());
	}
}
