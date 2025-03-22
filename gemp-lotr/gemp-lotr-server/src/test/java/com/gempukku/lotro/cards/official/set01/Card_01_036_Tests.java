package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_036_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("feet", "1_36");
					put("arwen", "1_30");

					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("chaff1", "1_3");
					put("chaff2", "1_3");
					put("chaff3", "1_3");
					put("chaff4", "1_3");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void CurseTheirFoulFeetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Curse Their Foul Feet!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Fellowship: Exert an Elf to reveal an opponent's hand. That player discards a card from hand for each Orc revealed.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("feet");

		assertEquals("Curse Their Foul Feet!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FoulFeetExertsAnElfToRevealShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCardToHand(feet, arwen);

		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		var chaff3 = scn.GetShadowCard("chaff3");
		var chaff4 = scn.GetShadowCard("chaff4");
		scn.ShadowMoveCardToHand("runner1","runner2","runner3","runner4",
				"chaff1","chaff2","chaff3","chaff4" );

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(feet));
		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(feet));
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsPlayCard(feet);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(8, scn.FreepsGetCardChoiceCount());

		scn.FreepsDismissRevealedCards();

		assertEquals("4", scn.ShadowGetADParam("min")[0]);
		assertEquals(8, scn.ShadowGetCardChoiceCount());

		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.ShadowChooseCards(chaff1, chaff2, chaff3, chaff4);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDiscardCount());
	}
}
