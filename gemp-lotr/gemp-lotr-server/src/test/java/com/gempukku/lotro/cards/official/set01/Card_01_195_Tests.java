package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_195_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("relics", "1_195");
					put("runner", "1_178");
					put("scimitar", "1_180");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void RelicsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Relics of Moria
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 *  Shadow: Remove (2) to play a [MORIA] possession from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("relics");

		assertEquals("Relics of Moria", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityRemoves2ToPlayMoriaPossessionFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var relics = scn.GetShadowCard("relics");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.ShadowMoveCardToSupportArea(relics);
		scn.ShadowMoveCharToTable(runner);
		scn.ShadowMoveCardToDiscard(scimitar);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetTwilight());
		assertEquals(Zone.DISCARD, scimitar.getZone());
		assertTrue(scn.ShadowActionAvailable(relics));
		scn.ShadowUseCardAction(relics);

		assertEquals(1, scn.GetTwilight());
		assertEquals(Zone.ATTACHED, scimitar.getZone());
		assertFalse(scn.ShadowActionAvailable(relics));
	}

	@Test
	public void ShadowAbilityNotAvailableIfNoPossessionsInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var relics = scn.GetShadowCard("relics");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.ShadowMoveCardToSupportArea(relics);
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.DECK, scimitar.getZone());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertFalse(scn.ShadowActionAvailable(relics));
	}

}
