package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_05_060_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("siege", "5_60");
					put("devilry", "5_49");
					put("ram", "5_44");
					put("ladder", "5_57");

					put("gandalf", "1_364");
					put("sleep", "1_84");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void SiegeEngineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Siege Engine
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Machine</b>. Plays to your support area.<br><b>Shadow:</b> Play an Uruk-hai to place an [isengard] token on a machine.<br><b>Response:</b> If one or more machines are about to be discarded by an opponent, discard this condition to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("siege");

		assertEquals("Siege Engine", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SiegeEngineThwartsSimultaneousDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sleep = scn.GetFreepsCard("sleep");
		scn.FreepsMoveCharToTable("gandalf");
		scn.FreepsMoveCardToHand(sleep);

		var siege = scn.GetShadowCard("siege");
		var devilry = scn.GetShadowCard("devilry");
		var ram = scn.GetShadowCard("ram");
		var ladder = scn.GetShadowCard("ladder");
		scn.ShadowMoveCardToSupportArea(siege, devilry, ram, ladder);

		scn.StartGame();

		assertEquals(Zone.SUPPORT, siege.getZone());
		assertEquals(Zone.SUPPORT, devilry.getZone());
		assertEquals(Zone.SUPPORT, ram.getZone());
		assertEquals(Zone.SUPPORT, ladder.getZone());

		assertTrue(scn.hasKeyword(devilry, Keyword.MACHINE));
		assertTrue(scn.hasKeyword(ram, Keyword.MACHINE));
		assertTrue(scn.hasKeyword(ladder, Keyword.MACHINE));

		scn.FreepsPlayCard(sleep);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.ShadowActionAvailable(siege));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(Zone.DISCARD, siege.getZone());
		assertEquals(Zone.SUPPORT, devilry.getZone());
		assertEquals(Zone.SUPPORT, ram.getZone());
		assertEquals(Zone.SUPPORT, ladder.getZone());

		assertTrue(scn.FreepsAnyDecisionsAvailable());
	}
}
