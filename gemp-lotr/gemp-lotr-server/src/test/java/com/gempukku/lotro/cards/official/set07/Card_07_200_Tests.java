package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_200_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "7_200");
					put("enque", "1_231");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void MorgulSpawnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Spawn
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a Nazgûl, the Free Peoples player must exert a companion to assign this minion to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Morgul Spawn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void MorgulSpawnTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var enque = scn.GetShadowCard("enque");

		scn.ShadowMoveCardToHand(card);
		scn.ShadowMoveCardToHand(enque);

		scn.StartGame();

		scn.SetTwilight(20);

		// Fellowship
		scn.FreepsPassCurrentPhaseAction();

		// Shadow
		scn.ShadowPlayCard(card);
		scn.ShadowPlayCard(enque);
		scn.ShadowPassCurrentPhaseAction();

		// Maneuver
		scn.SkipToAssignments();

		// Assignments
		assertTrue(scn.FreepsDecisionAvailable(
				"Would you like to exert a companion to be able to assign Morgul Spawn to skirmish?"));
	}
}
