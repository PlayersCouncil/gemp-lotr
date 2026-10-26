package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lurker", "8_76");
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] condition, cost 0, support area, no requirement
					put("filler1", "1_177");    // Goblin Patrol Troop: no text; only held in the Free Peoples hand to make it up
					put("filler2", "1_177");
					put("filler3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulLurkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Morgul Lurker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you have initiative, the minion archery total is +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lurker");

		assertEquals("Morgul Lurker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithFourCardsInTheFreePeoplesHandItAddsNothingAndOnceHeHasThreeTheMinionArcheryTotalIsPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurker = scn.GetShadowCard("lurker");
		var stairs = scn.GetFreepsCard("stairs");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");

		scn.StartGame();
		scn.MoveMinionsToTable(lurker);
		scn.SetFreepsHand(stairs, filler1, filler2, filler3);
		scn.FreepsRefreshDecision();

		// Four cards in hand: the Free Peoples player has initiative (CR 4.2 "initiative").
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		// No archer on either side: the totals are only what cards add.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(stairs);
		// Three cards in hand: the Shadow has initiative.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
