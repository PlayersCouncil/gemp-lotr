package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_85");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoverofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cover of Darkness, Omen of Dread
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight. To play, hinder 3 copies of Ominous Sky (or 2 copies if you cannot spot another Cover of Darkness).
		* 	Skirmish: Hinder this condition and exert an Orc to make that Orc strength +1 for each burden you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Dread", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void CoverofDarknessTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveCompanionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		//var card = scn.GetShadowCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveMinionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		scn.StartGame();
		
		assertFalse(true);
	}
}
