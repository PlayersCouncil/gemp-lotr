package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sauron", "103_92");

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sauron, Lord of All Middle-earth
		 * Unique: true
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 18
		 * Type: Minion
		 * Subtype: Maia
		 * Strength: 24
		 * Vitality: 5
		 * Site Number: 6
		 * Game Text: Fierce. Sauron is twilight cost -1 for each companion and Free Peoples support card you can spot. 
		* 	When you play Sauron, spot 4 other [sauron] cards, discard 3 cards from hand, or hinder him.
		* 	Each time a companion loses a skirmish to Sauron, kill that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sauron");

		assertEquals("Sauron", card.getBlueprint().getTitle());
		assertEquals("Lord of All Middle-earth", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAIA, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(18, card.getBlueprint().getTwilightCost());
		assertEquals(24, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SauronIsDiscounted1PerCompanion() throws DecisionResultInvalidException, CardNotFoundException {

		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		
		scn.SetTwilight(20);
		
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(24, scn.GetTwilight());
		scn.ShadowPlayCard(sauron);

		//24 -18 base -2 roaming +2 discount for both companions
		assertEquals(6, scn.GetTwilight());
	}
}
