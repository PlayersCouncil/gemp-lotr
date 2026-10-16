package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mouth", "12_73");

					// Lying in Wait: a [men] condition to the support area, cost 0
					put("lying", "13_95");
					// War Trident: a [men] possession a [men] minion bears (here the Mouth), cost 1
					put("trident", "12_77");
					// Fleet of Corsair Ships: a [men] possession to the support area, cost 3
					put("fleet", "18_66");
					// Man of Bree: a [men] card that is neither: a minion, cost 2
					put("bree", "11_90");
					// Spider Poison: a Shadow possession of another culture ([gollum]) to the support area, cost 1
					put("poison", "8_28");
					// Always Threatening: a Shadow condition of another culture ([orc]) to the support area, cost 1
					put("always", "13_103");
					// Harmless: a [men] support-area condition; the Shadow player's copy is laid in his hand
					put("harmless", "13_93");
					// Hemmed In: a [men] support-area condition; the Shadow player's copy is laid in his discard pile
					put("hemmed", "12_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMouthofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: The Mouth of Sauron, Messenger of Mordor
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Maneuver:</b> Exert The Mouth of Sauron to play a [men] condition or [men] possession from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mouth");

		assertEquals("The Mouth of Sauron", card.getBlueprint().getTitle());
		assertEquals("Messenger of Mordor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverActionExertsTheMouthToPlayAMenConditionOrPossessionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var lying = scn.GetShadowCard("lying");
		var trident = scn.GetShadowCard("trident");
		var fleet = scn.GetShadowCard("fleet");
		var bree = scn.GetShadowCard("bree");
		var poison = scn.GetShadowCard("poison");
		var always = scn.GetShadowCard("always");
		var harmless = scn.GetShadowCard("harmless");
		var hemmed = scn.GetShadowCard("hemmed");
		scn.MoveCardsToHand(harmless);
		scn.MoveCardsToDiscard(hemmed);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Enough in the pool for any card of the deck: the Fleet costs 3.
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, lying, trident, fleet, bree, poison, always);
		// The first player's own Harmless and Hemmed In lie in HIS draw deck.
		assertInZone(Zone.DECK, scn.GetFreepsCard("harmless"), scn.GetFreepsCard("hemmed"));
		assertInZone(Zone.HAND, harmless);
		assertInZone(Zone.DISCARD, hemmed);
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		assertEquals(0, scn.GetWoundsOn(mouth));
		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);
		assertEquals(1, scn.GetWoundsOn(mouth));
		scn.ShadowDismissRevealedCards();

		// A [men] condition, and a [men] possession borne and one to the support area.  Not a [men] minion, a
		// possession or a condition of another culture, nor a [men] condition in hand, in the discard pile or in
		// the other player's draw deck (read by blueprint: the Shadow player's own copies are out of his deck).
		assertTrue(scn.ShadowHasCardChoiceAvailable(lying, trident, fleet));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bree, poison, always));
		assertFalse(scn.ShadowHasBPChoiceAvailable("13_93"));
		assertFalse(scn.ShadowHasBPChoiceAvailable("12_70"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(trident);

		assertTrue(scn.GetAttachedCards(mouth).contains(trident));
		assertInZone(Zone.DECK, lying, fleet, bree, poison, always);
		assertInZone(Zone.HAND, harmless);
		assertInZone(Zone.DISCARD, hemmed);
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		// The deck is shuffled as the Trident leaves it, and again when the look is over; the other player's is not.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The archery phase: the Mouth could exert again, and the ability is not offered.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertFalse(scn.ShadowActionAvailable(mouth));
	}

	@Test
	public void HavingLookedThePlayerMayPlayNothingAndOnceTheMouthIsExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var lying = scn.GetShadowCard("lying");
		var trident = scn.GetShadowCard("trident");

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);

		int shuffles = scn.GetShadowDeckShuffleCount();

		// First use: nothing is played, and the exertion stands.
		scn.ShadowUseCardAction(mouth);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(lying, trident));
		scn.ShadowDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertInZone(Zone.DECK, lying, trident);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());

		// Second use: Lying in Wait to the support area.
		scn.FreepsPass();
		scn.ShadowUseCardAction(mouth);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(lying);
		assertInZone(Zone.SUPPORT, lying);
		assertEquals(2, scn.GetWoundsOn(mouth));

		// Vitality 3 with 2 wounds: exhausted, so the Mouth cannot exert, with the Trident still in the deck.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInZone(Zone.DECK, trident);
		assertFalse(scn.ShadowActionAvailable(mouth));
	}

	@Test
	public void WithNoMenConditionOrPossessionInTheDrawDeckItIsStillOfferedAndTheLookFindsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var bree = scn.GetShadowCard("bree");
		var poison = scn.GetShadowCard("poison");
		var always = scn.GetShadowCard("always");
		// Every [men] condition and possession out of the deck; the minion and the two cards of other cultures
		// are left in it.
		scn.MoveCardsToDiscard(scn.GetShadowCard("lying"), scn.GetShadowCard("trident"), scn.GetShadowCard("fleet"),
				scn.GetShadowCard("harmless"), scn.GetShadowCard("hemmed"));

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);

		assertInZone(Zone.DECK, bree, poison, always);
		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);
		assertEquals(1, scn.GetWoundsOn(mouth));
		// The deck is shown, and nothing in it may be chosen.
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.DECK, bree, poison, always);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
