package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_001_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("driven", "102_1");
					put("madman", "4_12");
					put("freca", "9_2");
					put("hillman", "15_90");

					put("runner", "1_178");

					//Dwarf guards for companion count
					put("fodder1", "1_7");
					put("fodder2", "1_7");
					put("fodder3", "1_7");
					put("fodder4", "1_7");
					put("fodder5", "1_7");
					put("fodder6", "1_7");
					put("fodder7", "1_7");
					put("fodder8", "1_7");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DrivenintotheHillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Driven into the Hills
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [Dunland] minion.
		* 	For each companion you can spot over 4, the Free Peoples player must have an additional card in hand to have initiative.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("driven");

		assertEquals("Driven into the Hills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DrivenRequiresDunlandMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var runner = scn.GetShadowCard("runner");
		var madman = scn.GetShadowCard("madman");
		scn.ShadowMoveCardToHand(driven, runner, madman);

		scn.StartGame();

		scn.SetTwilight(50);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(driven));
		scn.ShadowPlayCard(runner);
		assertFalse(scn.ShadowPlayAvailable(driven));
		scn.ShadowPlayCard(madman);
		assertTrue(scn.ShadowPlayAvailable(driven));
	}

	@Test
	public void DrivenMakesInitiativeTake5CardsWith5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		scn.ShadowMoveCardToSupportArea(driven);

		scn.FreepsMoveCharToTable("fodder1", "fodder2", "fodder3", "fodder4");
		scn.FreepsMoveCardToHand("driven", "madman", "freca", "hillman");

		scn.StartGame();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		scn.FreepsMoveCardToHand("runner");

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasInitiative());
	}
}
