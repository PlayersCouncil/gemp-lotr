package com.gempukku.lotro.cards.unofficial.pc.errata.set13;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_188_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<String, String>()
				{{
					put("card", "63_188");
					// put other cards in here as needed for the test case
				}},
				new HashMap<>() {{
					put("parapet", "63_188");
					put("site2", "11_237");
					put("site3", "11_237");
					put("site4", "11_237");
					put("site5", "11_237");
					put("site6", "11_237");
					put("site7", "11_237");
					put("site8", "11_237");
					put("site9", "11_237");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing,
				"open"
		);
	}

	@Test
	public void CourtyardParapetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 13
		* Title: Courtyard Parapet
		* Side: Free Peoples
		* Culture: 
		* Twilight Cost: 0
		* Type: site
		* Subtype: Standard
		* Game Text: <b>Dwelling.</b>When the fellowship moves to this site in region 2, the 
		* 	first Shadow player may discard 2 cards from hand to add a burden.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl parapet = scn.GetFreepsSite("Courtyard Parapet");

		assertFalse(parapet.getBlueprint().isUnique());
		assertEquals(CardType.SITE, parapet.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(parapet, Keyword.DWELLING));
		assertEquals(0, parapet.getBlueprint().getTwilightCost());
		assertEquals(SitesBlock.SHADOWS, parapet.getBlueprint().getSiteBlock());
		assertEquals(0, parapet.getBlueprint().getSiteNumber());
	}

	//@Test
	public void CourtyardParapetTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");
		scn.FreepsMoveCardToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
