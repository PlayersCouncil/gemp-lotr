package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_213_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("moving", "11_213");
					// Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl, site 2; nothing of her text below 2
					// burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl; strength -2 only to a character
					// bearing a [wraith] condition
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a Nazgûl of [raider]; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc, no Nazgûl; its text needs 2 wounds on the Ring-bearer
					put("hound", "7_192");
					put("marksman", "1_176");   // Goblin Marksman: a [moria] archer, the card laid on top of the deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MovingThisWayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Moving This Way
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Shadow:</b> Spot a Nazgûl and reveal a Nazgûl from hand to place the revealed card beneath your draw deck and draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("moving");

		assertEquals("Moving This Way", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RevealsANazgulFromHandToTheFreePeoplesPlayerPlacesItBeneathTheDeckAndDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moving = scn.GetShadowCard("moving");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveCardsToSupportArea(moving);
		scn.MoveMinionsToTable(nelya);
		scn.SetShadowHand(lemenya, khamul, hound);
		scn.MoveCardsToTopOfDeck(marksman);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(marksman, scn.GetShadowTopOfDeck());
		assertNotEquals(lemenya, scn.GetShadowBottomOfDeck());
		assertTrue(scn.ShadowActionAvailable(moving));

		scn.ShadowUseCardAction(moving);
		// The Nazgûl of either culture may be revealed; the Hound is no Nazgûl.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		scn.ShadowChooseCard(lemenya);

		// Shown to the Free Peoples player, and to him alone: a reveal from a hand is no pop-up for its owner.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(khamul, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(lemenya, scn.GetShadowBottomOfDeck());
		assertInHand(marksman, khamul, hound);
		// One gone beneath the deck, one drawn.
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANazgulOfAnotherCultureIsSpottedForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moving = scn.GetShadowCard("moving");
		var khamul = scn.GetShadowCard("khamul");
		var lemenya = scn.GetShadowCard("lemenya");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveCardsToSupportArea(moving);
		// Khamûl, a [raider] Nazgûl, is the only Nazgûl in play.
		scn.MoveMinionsToTable(khamul);
		scn.SetShadowHand(lemenya);
		scn.MoveCardsToTopOfDeck(marksman);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(lemenya);
		assertTrue(scn.ShadowActionAvailable(moving));

		// Lemenya, the one Nazgûl in hand, is revealed unasked.
		scn.ShadowUseCardAction(moving);
		scn.FreepsDismissRevealedCards();

		assertEquals(lemenya, scn.GetShadowBottomOfDeck());
		assertInHand(marksman);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoNazgulInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moving = scn.GetShadowCard("moving");
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveCardsToSupportArea(moving);
		// A [wraith] minion who is no Nazgûl.
		scn.MoveMinionsToTable(hound);
		scn.SetShadowHand(lemenya);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(lemenya);
		assertFalse(scn.ShadowActionAvailable(moving));
	}

	@Test
	public void IsNotOfferedWithNoNazgulInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moving = scn.GetShadowCard("moving");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveCardsToSupportArea(moving);
		scn.MoveMinionsToTable(nelya);
		// A [wraith] card in hand that is no Nazgûl.
		scn.SetShadowHand(hound);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(hound);
		assertFalse(scn.ShadowActionAvailable(moving));
	}
}
