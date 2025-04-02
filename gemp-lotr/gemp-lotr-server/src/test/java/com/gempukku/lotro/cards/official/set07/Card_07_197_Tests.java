package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.at.AbstractAtTest.P1;
import static org.junit.Assert.*;

public class Card_07_197_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "7_197");
					put("enquea", "1_231");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void MorgulRegimentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Regiment
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: For each Nazgûl you can spot, the Free Peoples player must exert a companion to assign this minion to a skirmish.<br><b>Skirmish:</b> Exert this minion to make a Nazgûl or [wraith] Orc strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Morgul Regiment", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void MustExertToAssignMorgulRegiment() throws DecisionResultInvalidException, CardNotFoundException {
		// Arrange
		GenericCardTestHelper scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var enquea = scn.GetShadowCard("enquea");
		var frodo = scn.GetRingBearer();

		scn.ShadowMoveCardToHand(card);
		scn.ShadowMoveCardToHand(enquea);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(card);
		scn.ShadowPlayCard(enquea);
		scn.ShadowPassCurrentPhaseAction();

		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable(
				"Would you like to exert a companion for each Nazgûl you can spot to be able to assign Morgul Regiment to skirmish?"));

		// Act
		scn.playerDecided(P1, "0");

		// Assert
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsCanAssign(card));
	}
}
