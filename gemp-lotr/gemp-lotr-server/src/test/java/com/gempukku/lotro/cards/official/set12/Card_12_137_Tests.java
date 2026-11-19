package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_137_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("conscript", "12_137");
					put("conscript2", "12_137");

					// Cards to hold in the Shadow hand: a minion, a condition, an event and a Free Peoples card.
					put("murderous", "11_195");   // Murderous Uruk: an [uruk-hai] Uruk-hai; Damage +1 and Muster are its whole text
					put("alliance", "12_141");    // Dark Alliance: an [uruk-hai] condition; a response to a special ability
					put("furor", "11_187");       // Furor: an [uruk-hai] Skirmish event
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Free Peoples companion
					put("savage", "1_151");       // Uruk Savage: the card on top of the draw deck, to be drawn

					put("whitehand", "18_131");   // White Hand Uruk: an [uruk-hai] Uruk-hai; "To play, spot an [uruk-hai] minion", which the Conscript meets
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BreedingPitConscriptStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Breeding Pit Conscript
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper><br>When you play this minion, you may discard a card from hand to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("conscript");

		assertEquals("Breeding Pit Conscript", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.MUSTER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItsOwnerMayDiscardAnyCardFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var conscript = scn.GetShadowCard("conscript");
		var conscript2 = scn.GetShadowCard("conscript2");
		var murderous = scn.GetShadowCard("murderous");
		var alliance = scn.GetShadowCard("alliance");
		var furor = scn.GetShadowCard("furor");
		var aragorn = scn.GetShadowCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(conscript, murderous, alliance, furor, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(conscript2);
		scn.MoveCardsToTopOfDeck(savage);
		assertInZone(Zone.DECK, savage);
		assertInHand(conscript, murderous, alliance, furor, aragorn);
		assertInZone(Zone.SHADOW_CHARACTERS, conscript2);
		assertEquals(savage, scn.GetShadowTopOfDeck());
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(conscript);
		assertEquals(4, scn.GetShadowHandCount());
		// The copy played offers it; the copy already in play does not.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(conscript));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(conscript2));
		scn.ShadowChooseOptionalTrigger(conscript);

		// Any one card from hand: a minion, a condition, an event, or a Free Peoples card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(murderous, alliance, furor, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(aragorn);

		assertInDiscard(aragorn);
		assertInHand(savage, murderous, alliance, furor);
		assertEquals(4, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, conscript, conscript2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheHandAndTheDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var conscript = scn.GetShadowCard("conscript");
		var alliance = scn.GetShadowCard("alliance");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(conscript, alliance);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(savage);
		assertInZone(Zone.DECK, savage);
		assertInHand(conscript, alliance);
		assertEquals(savage, scn.GetShadowTopOfDeck());
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(conscript);
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(conscript));
		scn.ShadowDeclineOptionalTrigger();

		assertInHand(alliance);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(savage, scn.GetShadowTopOfDeck());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherCardInHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var conscript = scn.GetShadowCard("conscript");
		scn.MoveCardsToHand(conscript);

		scn.StartGame();
		assertInHand(conscript);
		assertEquals(1, scn.GetShadowHandCount());
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(conscript);

		// The Conscript itself is in play, and nothing is left in hand to discard.
		assertEquals(0, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(conscript));
		assertInZone(Zone.SHADOW_CHARACTERS, conscript);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherUrukHaiPlayedAfterItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var conscript = scn.GetShadowCard("conscript");
		var whitehand = scn.GetShadowCard("whitehand");
		var alliance = scn.GetShadowCard("alliance");
		scn.MoveCardsToHand(whitehand, alliance);

		scn.StartGame();
		scn.MoveMinionsToTable(conscript);
		assertInHand(whitehand, alliance);
		assertInZone(Zone.SHADOW_CHARACTERS, conscript);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(whitehand);

		// A card is still in hand to pay with, but no Conscript was played: no offer.
		assertInZone(Zone.SHADOW_CHARACTERS, whitehand);
		assertInHand(alliance);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(conscript));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
