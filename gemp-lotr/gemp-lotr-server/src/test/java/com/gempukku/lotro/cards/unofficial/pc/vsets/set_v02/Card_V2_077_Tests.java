package com.gempukku.lotro.cards.unofficial.pc.errata.setv02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_077_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "102_77");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GandalfsStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Gandalf's Staff, Old Man's Crutch
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Vitality: 1
		 * Game Text: Bearer must be Gandalf.
		* 	Discard any weapon he bears.
		* 	Each time Gandalf is exerted by a Free Peoples card, you may wound a minion (limit once per phase).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gandalf's Staff", card.getBlueprint().getTitle());
		assertEquals("Old Man's Crutch", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GandalfsStaffTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.FreepsMoveCardToHand(card);
		scn.FreepsMoveCharToTable(card);
		scn.FreepsMoveCardToSupportArea(card);
		scn.FreepsMoveCardToDiscard(card);
		scn.FreepsMoveCardsToTopOfDeck(card);

		var card = scn.GetShadowCard("card");
		scn.ShadowMoveCardToHand(card);
		scn.ShadowMoveCharToTable(card);
		scn.ShadowMoveCardToSupportArea(card);
		scn.ShadowMoveCardToDiscard(card);
		scn.ShadowMoveCardsToTopOfDeck(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
