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
					put("pipe", "1_285");
					put("pipeweed", "1_305");
					put("pipe2", "1_285");
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

		var jetsam = scn.GetFreepsCard("jetsam");

		assertEquals("Jetsam", jetsam.getBlueprint().getTitle());
		assertNull(jetsam.getBlueprint().getSubtitle());
		assertFalse(jetsam.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, jetsam.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, jetsam.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, jetsam.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(jetsam, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(jetsam, Keyword.SUPPORT_AREA));
		assertEquals(1, jetsam.getBlueprint().getTwilightCost());
	}

	@Test
	public void JetsamTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var jetsam = scn.GetFreepsCard("jetsam");
		var pipe = scn.GetFreepsCard("pipe");
		var pipeweed = scn.GetFreepsCard("pipeweed");
		// var pipe2 = scn.GetFreepsCard("pipe2");
		// var pipeweed2 = scn.GetFreepsCard("pipeweed2");

		scn.FreepsMoveCardToHand(jetsam);
		scn.FreepsMoveCardToDiscard(pipe);
		scn.FreepsMoveCardToDiscard(pipeweed);

		scn.StartGame();


		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetTwilight());


		scn.FreepsPlayCard(jetsam);


		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsDecisionAvailable(""));
		assertEquals(1, scn.GetFreepsCardChoiceCount());
		assertEquals(Zone.DISCARD, pipe.getZone());

		scn.FreepsChooseCardIDFromSelection(pipe);

		assertEquals(Zone.DECK, pipe.getZone());


		assertEquals(1, scn.GetFreepsCardChoiceCount());

		scn.FreepsChooseCardIDFromSelection(pipeweed);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetTwilight());
	}
}
