package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("plunderer", "8_58");
					put("plunderer2", "8_58");
					put("brute", "10_38");      // Corsair Brute: another corsair, cost 5, site 4; a start-of-skirmish trigger only

					// Cards for the Shadow player's hand: a character, an item and a card that is neither, of three
					// cultures and both sides.
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, no text
					put("fear", "1_278");       // Strength Born of Fear: [sauron] support-area condition
					put("armor", "1_8");        // Dwarven Armor: [dwarven] Free Peoples possession

					// Cards that carry culture tokens; each is given tokens by its own text.
					put("ships", "8_65");       // Ships of Great Draught: [raider] possession in the support area
					put("raiders", "4_242");    // Raiders From the East: [raider] condition in the support area
					put("galley", "8_59");      // Corsair War Galley: [raider] possession in the support area; initiative only at 6 [raider] tokens
					put("rank", "10_96");       // Rank and File: [sauron] condition in the support area, with [sauron] tokens
					put("leaders", "7_112");    // Noble Leaders: the Free Peoples player's [gondor] condition, with [gondor] tokens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairPlundererStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Corsair Plunderer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>.<br>When you play this minion, you may discard 2 cards from hand to add 2 [raider] tokens to a card that already has a [raider] token on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("plunderer");

		assertEquals("Corsair Plunderer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DiscardsTwoCardsFromHandToAddTwoRaiderTokensToACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plunderer = scn.GetShadowCard("plunderer");
		var troop = scn.GetShadowCard("troop");
		var fear = scn.GetShadowCard("fear");
		var armor = scn.GetShadowCard("armor");
		var ships = scn.GetShadowCard("ships");
		var raiders = scn.GetShadowCard("raiders");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCardsToHand(plunderer, troop, fear, armor);
		assertInHand(troop, fear, armor);
		scn.MoveCardsToSupportArea(ships, raiders, galley, rank, leaders);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(raiders, 2);
		scn.AddTokensToCard(rank, 2);
		scn.AddTokensToCard(leaders, 2);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(4, scn.GetShadowHandCount());
		scn.ShadowPlayCard(plunderer);
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(plunderer));
		scn.ShadowAcceptOptionalTrigger();

		// Any card in hand, whatever its type, culture or side; exactly two.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, fear, armor));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop, armor);

		assertInDiscard(troop, armor);
		assertInHand(fear);
		assertEquals(1, scn.GetShadowHandCount());

		// A card that has a [raider] token: not the [raider] card with none, nor the cards whose tokens are of
		// another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(galley, rank, leaders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(raiders);

		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(4, scn.GetCultureTokensOn(raiders));
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plunderer = scn.GetShadowCard("plunderer");
		var troop = scn.GetShadowCard("troop");
		var fear = scn.GetShadowCard("fear");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(plunderer, troop, fear);
		assertInHand(troop, fear);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(plunderer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(plunderer));
		scn.ShadowDeclineOptionalTrigger();

		assertInHand(troop, fear);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyOneOtherCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plunderer = scn.GetShadowCard("plunderer");
		var troop = scn.GetShadowCard("troop");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(plunderer, troop);
		assertInHand(troop);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowHandCount());
		scn.ShadowPlayCard(plunderer);

		// The Plunderer itself is no longer in hand: one card cannot pay for two.
		assertInZone(Zone.SHADOW_CHARACTERS, plunderer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInHand(troop);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoRaiderTokenInPlayTheCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plunderer = scn.GetShadowCard("plunderer");
		var troop = scn.GetShadowCard("troop");
		var fear = scn.GetShadowCard("fear");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		scn.MoveCardsToHand(plunderer, troop, fear);
		assertInHand(troop, fear);
		scn.MoveCardsToSupportArea(galley, rank);

		scn.StartGame();
		scn.AddTokensToCard(rank, 2);
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(plunderer);
		// It has a cost, so it is offered with nothing to add tokens to.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(plunderer));
		scn.ShadowAcceptOptionalTrigger();

		// Two cards in hand, both discarded unasked.
		assertInDiscard(troop, fear);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherCorsairPlayedBringsNoOfferAndASecondCopyMakesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plunderer = scn.GetShadowCard("plunderer");
		var plunderer2 = scn.GetShadowCard("plunderer2");
		var brute = scn.GetShadowCard("brute");
		var troop = scn.GetShadowCard("troop");
		var fear = scn.GetShadowCard("fear");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(brute, plunderer2, troop, fear);
		assertInHand(troop, fear);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(plunderer);
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Three cards are left in hand: the first Plunderer could pay, but it is not its own play.
		scn.ShadowPlayCard(brute);
		assertInZone(Zone.SHADOW_CHARACTERS, brute);
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second Plunderer is the one played, and the offer is its own.
		scn.ShadowPlayCard(plunderer2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(plunderer2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(plunderer));
		scn.ShadowChooseOptionalTrigger(plunderer2);

		// The two cards left in hand go unasked, and the one card with a [raider] token is chosen unasked; once.
		assertInDiscard(troop, fear);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(3, scn.GetCultureTokensOn(ships));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
