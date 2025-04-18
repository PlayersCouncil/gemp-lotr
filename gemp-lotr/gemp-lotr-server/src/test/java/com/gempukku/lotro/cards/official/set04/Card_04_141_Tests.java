package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_141_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "4_141");
					put("uruk", "1_151");
					put("cantea", "1_230");
					put("enquea", "1_231");
					put("gandalf", "1_364");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void BeyondDarkMountainsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Beyond Dark Mountains
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If a companion or ally is killed, exert an [isengard] minion to add a burden (or 2 burdens if Aragorn, Gandalf, or Théoden is killed).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Beyond Dark Mountains", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MayChooseNotToAddBurden() throws DecisionResultInvalidException, CardNotFoundException {
		// Arrange
		GenericCardTestHelper scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var uruk = scn.GetShadowCard("uruk");
		var cantea = scn.GetShadowCard("cantea");
		var enquea = scn.GetShadowCard("enquea");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();

		scn.ShadowMoveCardToHand(card);
		scn.ShadowMoveCardToHand(uruk);
		scn.ShadowMoveCardToHand(cantea);
		scn.ShadowMoveCardToHand(enquea);
		scn.FreepsMoveCardToHand(gandalf);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPlayCard(gandalf);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(uruk);
		scn.ShadowPlayCard(cantea);
		scn.ShadowPlayCard(enquea);
		scn.ShadowPassCurrentPhaseAction();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, uruk);
		scn.ShadowAssignToMinions(gandalf, cantea, enquea);

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPassCurrentPhaseAction();

		assertTrue(scn.ShadowDecisionAvailable(
				"Optional responses"));

		// Act
		scn.ShadowPassCurrentPhaseAction();

		// Assert
		assertEquals(1, scn.GetBurdens());
	}
}
