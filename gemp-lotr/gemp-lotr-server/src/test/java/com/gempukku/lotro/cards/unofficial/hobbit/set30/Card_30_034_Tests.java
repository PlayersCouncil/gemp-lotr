package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_034_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hatred", "30_34");
					// For the hand, a card of each kind, and an Orc among them.  A choice from a pile is read by
					// blueprint, so none of these shares one with a card of the discard pile.
					put("footman", "30_42");    // Goblin Footman: [moria] Orc minion; a trigger on his own play
					put("scimitar", "31_31");   // Goblin Scimitar: [moria] hand weapon
					put("anger", "30_32");      // Anger: [gundabad] Skirmish event
					put("borders", "33_30");    // Borders of the Forest: [mirkwood] condition
					// For the discard pile
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2, site 3; an Assignment ability
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, cost 2, site 4; a Regroup ability
					// Bill Ferny Sr.: a [gundabad] minion who is a Man, not an Orc
					put("ferny", "33_22");
					// Goblin Sneak: [moria] Orc; the Free Peoples player's copy lies in his own discard pile
					put("sneak", "31_32");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void HatredRekindledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Hatred Rekindled
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Shadow:</b> Discard 3 cards from hand to play an Orc from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hatred");

		assertEquals("Hatred Rekindled", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowActionDiscards3CardsFromHandToPlayAnOrcThatWasAlreadyInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetShadowCard("hatred");
		var footman = scn.GetShadowCard("footman");
		var scimitar = scn.GetShadowCard("scimitar");
		var anger = scn.GetShadowCard("anger");
		var borders = scn.GetShadowCard("borders");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var ferny = scn.GetShadowCard("ferny");
		var fpSneak = scn.GetFreepsCard("sneak");

		scn.StartGame();
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToHand(footman, scimitar, anger, borders);
		scn.MoveCardsToDiscard(aggressor, veteran, ferny);
		// An Orc in the other player's discard pile, of a title the Shadow player's own discard pile does not hold.
		scn.MoveCardsToDiscard(fpSneak);
		scn.SetTwilight(20);
		assertInZone(Zone.SUPPORT, hatred);
		assertInHand(footman, scimitar, anger, borders);
		assertInDiscard(aggressor, veteran, ferny, fpSneak);
		scn.FreepsPass();

		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(hatred));

		scn.ShadowUseCardAction(hatred);
		// Any 3 cards of the 4: a minion, a possession, an event and a condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, scimitar, anger, borders));
		assertEquals(3, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(footman, scimitar, anger);
		assertInDiscard(footman, scimitar, anger);
		assertInHand(borders);
		assertEquals(1, scn.GetShadowHandCount());

		// Orcs of two cultures that lay in the pile before the cost was paid; not the [gundabad] Man, not the other
		// player's Orc, and not the Goblin Footman that the cost has just put there (Q-0320).
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny, fpSneak, footman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(veteran);

		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertInDiscard(aggressor, ferny, footman, scimitar, anger, fpSneak);
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedWithTwoCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetShadowCard("hatred");
		var anger = scn.GetShadowCard("anger");
		var borders = scn.GetShadowCard("borders");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToHand(anger, borders);
		scn.MoveCardsToDiscard(aggressor);
		scn.SetTwilight(20);
		assertInZone(Zone.SUPPORT, hatred);
		assertInHand(anger, borders);
		assertInDiscard(aggressor);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hatred));
	}

	@Test
	public void ShadowActionIsNotOfferedWithNoOrcInTheDiscardPileThoughOneIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetShadowCard("hatred");
		var footman = scn.GetShadowCard("footman");
		var anger = scn.GetShadowCard("anger");
		var borders = scn.GetShadowCard("borders");
		var ferny = scn.GetShadowCard("ferny");
		var fpSneak = scn.GetFreepsCard("sneak");

		scn.StartGame();
		scn.MoveCardsToSupportArea(hatred);
		// The Orc in hand would be discarded by the cost, and so cannot be the Orc played (Q-0320).
		scn.MoveCardsToHand(footman, anger, borders);
		scn.MoveCardsToDiscard(ferny, fpSneak);
		scn.SetTwilight(20);
		assertInZone(Zone.SUPPORT, hatred);
		assertInHand(footman, anger, borders);
		assertInDiscard(ferny, fpSneak);
		scn.FreepsPass();

		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hatred));
	}

	@Test
	public void ShadowActionIsNotOfferedUntilThePoolCanPayForTheOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hatred = scn.GetShadowCard("hatred");
		var scimitar = scn.GetShadowCard("scimitar");
		var anger = scn.GetShadowCard("anger");
		var borders = scn.GetShadowCard("borders");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToHand(scimitar, anger, borders);
		scn.MoveCardsToDiscard(aggressor);
		assertInZone(Zone.SUPPORT, hatred);
		assertInHand(scimitar, anger, borders);
		assertInDiscard(aggressor);
		scn.FreepsPass();

		// The Orkish Aggressor costs 2, and 2 for roaming (a site 3 minion at site 2): one short.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hatred));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(hatred));
	}
}
