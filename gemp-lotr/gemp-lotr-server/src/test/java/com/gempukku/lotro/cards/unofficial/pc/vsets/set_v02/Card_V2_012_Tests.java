package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_012_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("jetsam", "102_12");
					put("pipe1", "1_285");
					put("pipeweed1", "1_300");
					put("pipe2", "1_74");
					put("pipeweed2", "1_305");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void JetsamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Jetsam
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Pipeweed.
		* 	When you play this possession, you may shuffle a pipe and a pipeweed into your deck from your discard pile. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("jetsam");

		assertEquals("Jetsam", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void JetsamTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam");
		var pipe1 = scn.GetFreepsCard("pipe1");
		var pipeweed1 = scn.GetFreepsCard("pipeweed1");
		var pipe2 = scn.GetFreepsCard("pipe2");
		var pipeweed2 = scn.GetFreepsCard("pipeweed2");

		scn.FreepsMoveCardToHand(jetsam);
		scn.FreepsMoveCardToDiscard(pipe1, pipe2, pipeweed1, pipeweed2);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(jetsam);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetFreepsCardChoiceCount());
		assertEquals(Zone.DISCARD, pipe1.getZone());

		scn.FreepsChooseCardBPFromSelection(pipe1);
		assertEquals(Zone.DECK, pipe1.getZone());

		assertEquals(2, scn.GetFreepsCardChoiceCount());
		assertEquals(Zone.DISCARD, pipeweed1.getZone());

		scn.FreepsChooseCardBPFromSelection(pipeweed1);
		assertEquals(Zone.DECK, pipeweed1.getZone());

		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetTwilight());
	}
}
