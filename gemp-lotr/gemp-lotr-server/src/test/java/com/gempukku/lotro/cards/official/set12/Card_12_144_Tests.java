package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_144_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "12_144");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Saruman, Agent of the Dark Lord
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Skirmish:</b> Exert Saruman to make an [uruk-hai] minion strength +2.<br>Each time a companion is killed, you may heal Saruman.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Agent of the Dark Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void SarumanTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.FreepsMoveCardToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(4, scn.GetTwilight());
	}
}
