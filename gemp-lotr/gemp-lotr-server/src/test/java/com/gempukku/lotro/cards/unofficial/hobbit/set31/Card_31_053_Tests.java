package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_053_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  Every test plays in the Shadow phase of turn 1, at The Troll Hoard 30_50 (site 2), where each minion
	// of a higher site number roams (+2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tomnoddy", "31_53");
					put("tomnoddy2", "31_53");
					// Spiders of two cultures.
					put("fat", "31_51");         // Fat Spider: [spider] Spider, cost 3 (-1 for each doubt; none here), site 5; a Regroup ability, not used
					put("shelob", "8_26");       // Shelob, Last Child of Ungoliant: [gollum] Spider, cost 6, site 8
					// Orcs of two cultures.
					put("veteran", "32_43");     // Orkish Veteran: [sauron] Orc, cost 2, site 4; archer at site 9 only, a Regroup ability, not used
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc, cost 2, site 3; an Assignment ability, not used
					// What is neither.
					put("troll", "32_31");       // Demolition Troll: [moria] Troll, cost 5, site 4; a Skirmish ability, not used
					put("river", "31_50");       // Enchanted River: a [spider] condition, cost 2, no Spider; texts that do not reach site 2's Shadow phase
					// An Orc for the other player's own discard pile, of a blueprint the Shadow player's piles do not hold.
					put("underling", "33_50");   // Orc Underling: [sauron] Orc, cost 1
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OldTomnoddyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Old Tomnoddy
		 * Unique: False
		 * Side: Shadow
		 * Culture: Spider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.<br>When you play this minion, you may remove (1) to play a Spider or an Orc from
		 * your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tomnoddy");

		assertEquals("Old Tomnoddy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SPIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedRemoves1ToPlayASpiderFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var fat = scn.GetShadowCard("fat");
		var shelob = scn.GetShadowCard("shelob");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		var troll = scn.GetShadowCard("troll");
		var river = scn.GetShadowCard("river");
		var underling = scn.GetFreepsCard("underling");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToHand(tomnoddy);
		scn.MoveCardsToDiscard(fat, shelob, veteran, aggressor, troll, river, underling);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInHand(tomnoddy);
		assertInZone(Zone.DISCARD, fat, shelob, veteran, aggressor, troll, river, underling);

		scn.ShadowPlayCard(tomnoddy);
		// 4 + 2 roaming
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tomnoddy));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(13, scn.GetTwilight());
		// Spiders of two cultures and Orcs of two cultures; not a Troll, not a [spider] card that is no Spider, and
		// not the other player's Orc in his own discard pile.  The Troll (5 + 2) and the condition (2) could be paid
		// for: only the wording keeps them out.
		assertTrue(scn.ShadowHasCardChoiceAvailable(fat, shelob, veteran, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, river, underling));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(shelob);

		assertInZone(Zone.SHADOW_CHARACTERS, tomnoddy, shelob);
		assertInZone(Zone.DISCARD, fat, veteran, aggressor, troll, river, underling);
		// 13 - (6 + 2 roaming)
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedCanPlayAnOrcFromTheDiscardPileWithExactlyEnoughTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var fat = scn.GetShadowCard("fat");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToHand(tomnoddy);
		scn.MoveCardsToDiscard(fat, aggressor);
		// 6 for Old Tomnoddy, 1 removed, and 2 + 2 roaming for the Orc.  Fat Spider (3 + 2) is then out of reach.
		scn.SetTwilight(11);
		scn.ShadowRefreshDecision();
		assertInHand(tomnoddy);
		assertInZone(Zone.DISCARD, fat, aggressor);

		scn.ShadowPlayCard(tomnoddy);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tomnoddy));
		scn.ShadowAcceptOptionalTrigger();

		// The one card that can be paid for is played unasked.
		assertInZone(Zone.SHADOW_CHARACTERS, tomnoddy, aggressor);
		assertInZone(Zone.DISCARD, fat);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedLeavesEverythingAsItWasAndAnotherSpiderPlayedLaterSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var tomnoddy2 = scn.GetShadowCard("tomnoddy2");
		var fat = scn.GetShadowCard("fat");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToHand(tomnoddy, tomnoddy2, fat);
		scn.MoveCardsToDiscard(aggressor);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInHand(tomnoddy, tomnoddy2, fat);
		assertInZone(Zone.DISCARD, aggressor);

		scn.ShadowPlayCard(tomnoddy);
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tomnoddy));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.DISCARD, aggressor);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played: its own offer, and none from the first.
		scn.ShadowPlayCard(tomnoddy2);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tomnoddy2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tomnoddy));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(8, scn.GetTwilight());

		// Another Spider played: nothing offered.
		scn.ShadowPlayCard(fat);
		// 3 + 2 roaming
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, tomnoddy, tomnoddy2, fat);
		assertInZone(Zone.DISCARD, aggressor);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneTwilightTooFewForTheCheapestCardItCouldPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var aggressor = scn.GetShadowCard("aggressor");
		var troll = scn.GetShadowCard("troll");
		var river = scn.GetShadowCard("river");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToHand(tomnoddy);
		scn.MoveCardsToDiscard(aggressor, troll, river);
		// 6 for Old Tomnoddy leaves 4: the Orc wants 1 + 2 + 2 roaming.  The condition (2) would fit.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(tomnoddy);
		assertInZone(Zone.DISCARD, aggressor, troll, river);

		scn.ShadowPlayCard(tomnoddy);

		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tomnoddy));
		assertInZone(Zone.DISCARD, aggressor, troll, river);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoSpiderOrOrcInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tomnoddy = scn.GetShadowCard("tomnoddy");
		var troll = scn.GetShadowCard("troll");
		var river = scn.GetShadowCard("river");
		var underling = scn.GetFreepsCard("underling");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToHand(tomnoddy);
		scn.MoveCardsToDiscard(troll, river, underling);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInHand(tomnoddy);
		assertInZone(Zone.DISCARD, troll, river, underling);

		scn.ShadowPlayCard(tomnoddy);

		assertEquals(14, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tomnoddy));
		assertInZone(Zone.DISCARD, troll, river, underling);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
