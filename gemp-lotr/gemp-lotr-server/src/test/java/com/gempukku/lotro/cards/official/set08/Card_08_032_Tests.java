package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_032_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("catapult", "8_32");
					put("fodder1", "8_33");
					put("fodder2", "8_34");

					put("runner", "1_178");
					put("shelob", "8_25");
					put("larder", "8_23");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void CatapultStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Catapult
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [gondor] knights.<br><b>Maneuver:</b> Discard 2 cards from hand to reveal the top card of an opponent's draw deck. Choose an opponent who must discard a Shadow card that has a twilight cost that is the same as the twilight cost of the revealed card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("catapult");

		assertEquals("Catapult", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CatapultCantBeUsedWithLessThanTwoCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var catapult = scn.GetFreepsCard("catapult");
		scn.FreepsMoveCardToHand("fodder1");
		scn.FreepsMoveCardToSupportArea(catapult);

		scn.ShadowMoveCharToTable("runner");

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(catapult));
	}

	@Test
	public void CatapultRevealsTopOfOpponentDeckAndDiscardsShadowCardOfSameCost() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var catapult = scn.GetFreepsCard("catapult");
		scn.FreepsMoveCardToHand("fodder1", "fodder2");
		scn.FreepsMoveCardToSupportArea(catapult);

		var runner = scn.GetShadowCard("runner");
		var shelob = scn.GetShadowCard("shelob");
		var larder = scn.GetShadowCard("larder");
		var evilcatapult = scn.GetShadowCard("catapult");
		scn.ShadowMoveCharToTable(runner, shelob);
		scn.ShadowMoveCardToSupportArea(larder);
		scn.ShadowMoveCardsToTopOfDeck(evilcatapult);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(catapult));

		scn.FreepsUseCardAction(catapult);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(evilcatapult));
		scn.DismissRevealedCards();

		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner));
		assertTrue(scn.ShadowHasCardChoiceAvailable(larder));
		assertFalse(scn.ShadowHasCardChoiceAvailable(shelob));

		assertEquals(Zone.SUPPORT, larder.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		scn.ShadowChooseCard(larder);
		assertEquals(Zone.DISCARD, larder.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
	}
}
