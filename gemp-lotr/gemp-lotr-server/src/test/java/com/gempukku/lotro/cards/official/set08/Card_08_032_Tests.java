package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("catapult", "8_32");
					put("fodder1", "8_33");
					put("fodder2", "8_34");

					put("runner", "1_178");
					put("shelob", "8_25");
					put("larder", "8_23");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
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
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CatapultCantBeUsedWithLessThanTwoCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var catapult = scn.GetFreepsCard("catapult");
		scn.MoveCardsToFreepsHand("fodder1");
		scn.MoveCardsToSupportArea(catapult);

		scn.MoveMinionsToTable("runner");

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
		scn.MoveCardsToFreepsHand("fodder1", "fodder2");
		scn.MoveCardsToSupportArea(catapult);

		var runner = scn.GetShadowCard("runner");
		var shelob = scn.GetShadowCard("shelob");
		var larder = scn.GetShadowCard("larder");
		var evilcatapult = scn.GetShadowCard("catapult");
		scn.MoveMinionsToTable(runner, shelob);
		scn.MoveCardsToSupportArea(larder);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(evilcatapult);
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

	@Test
	public void CatapultBehavesWhenOpponentHas0CardsInDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var catapult = scn.GetFreepsCard("catapult");
		scn.MoveCardsToFreepsHand("fodder1", "fodder2");
		scn.MoveCardsToSupportArea(catapult);

		var runner = scn.GetShadowCard("runner");
		var shelob = scn.GetShadowCard("shelob");
		var larder = scn.GetShadowCard("larder");
		scn.MoveMinionsToTable(runner, shelob);
		scn.MoveCardsToSupportArea(larder);
		scn.MoveCardsToShadowDiscard("catapult", "fodder1", "fodder2");

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsActionAvailable(catapult));

		scn.FreepsUseCardAction(catapult);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());

		//Skipped right to the other player's maneuver action
		assertTrue(scn.ShadowAnyDecisionsAvailable());
	}
}
