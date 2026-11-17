package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("castamir", "8_51");

					put("brute", "10_38");      // Corsair Brute: corsair, cost 5, site 4; a start-of-skirmish trigger only
					put("boatswain", "10_37");  // Corsair Boatswain: corsair, cost 2, site 4; to play, spot a [raider] Man
					put("southron", "4_254");   // Southron Soldier: [raider] Man, cost 2, no corsair; Southron and ambush only
					put("champion", "13_84");   // Corsair Champion: [men] Man with "Corsair" in the title and not the keyword

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
	public void CastamirofUmbarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Castamir of Umbar
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>. <b>Enduring</b>. <b>Fierce</b>.<br><b>Shadow:</b> Exert Castamir of Umbar and play a corsair to add 2 [raider] tokens to a card that already has a [raider] token on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("castamir");

		assertEquals("Castamir of Umbar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsHimAndPlaysACorsairToAddTwoRaiderTokensToACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		var brute = scn.GetShadowCard("brute");
		var boatswain = scn.GetShadowCard("boatswain");
		var southron = scn.GetShadowCard("southron");
		var champion = scn.GetShadowCard("champion");
		var ships = scn.GetShadowCard("ships");
		var raiders = scn.GetShadowCard("raiders");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCardsToHand(brute, boatswain, southron, champion);
		scn.MoveCardsToSupportArea(ships, raiders, galley, rank, leaders);

		scn.StartGame();
		scn.MoveMinionsToTable(castamir);
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(raiders, 2);
		scn.AddTokensToCard(rank, 2);
		scn.AddTokensToCard(leaders, 2);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		assertInHand(brute, boatswain, southron, champion);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(castamir));
		assertTrue(scn.ShadowActionAvailable(castamir));
		scn.ShadowUseCardAction(castamir);

		assertEquals(1, scn.GetWoundsOn(castamir));
		// A corsair: not the [raider] Man who is none, nor the Man with the word in his title only.
		assertTrue(scn.ShadowHasCardChoiceAvailable(brute, boatswain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(southron, champion));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(brute);

		assertInZone(Zone.SHADOW_CHARACTERS, brute);
		assertInHand(boatswain, southron, champion);

		// A card that has a [raider] token: not the [raider] card with none, nor the cards whose tokens are of
		// another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(galley, rank, leaders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ships);

		assertEquals(3, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileCastamirIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		var brute = scn.GetShadowCard("brute");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(brute);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(castamir);
		scn.AddTokensToCard(ships, 1);
		// Vitality 4, 3 wounds.
		scn.AddWoundsToChar(castamir, 3);
		assertEquals(3, scn.GetWoundsOn(castamir));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(brute));
		assertFalse(scn.ShadowActionAvailable(castamir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithoutACorsairInHandThatCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		var brute = scn.GetShadowCard("brute");
		var boatswain = scn.GetShadowCard("boatswain");
		var southron = scn.GetShadowCard("southron");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(brute, southron);
		scn.MoveCardsToSupportArea(ships);
		// A corsair that could be paid for, in the discard pile and not in hand.
		scn.MoveCardsToDiscard(boatswain);

		scn.StartGame();
		scn.MoveMinionsToTable(castamir);
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertInDiscard(boatswain);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 + 2 for Ettenmoors + 1 for the one companion: 6.  The Southron Soldier (2 + 2 roaming) can be played
		// and is no corsair; the Corsair Brute (5 + 2 roaming) is one short.
		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(castamir));
		assertTrue(scn.ShadowPlayAvailable(southron));
		assertFalse(scn.ShadowPlayAvailable(brute));
		assertFalse(scn.ShadowActionAvailable(castamir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoRaiderTokenInPlayTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castamir = scn.GetShadowCard("castamir");
		var brute = scn.GetShadowCard("brute");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		scn.MoveCardsToHand(brute);
		scn.MoveCardsToSupportArea(galley, rank);

		scn.StartGame();
		scn.MoveMinionsToTable(castamir);
		scn.AddTokensToCard(rank, 2);
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertInHand(brute);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// It has costs, so it is offered with nothing to add tokens to.
		assertTrue(scn.ShadowActionAvailable(castamir));
		scn.ShadowUseCardAction(castamir);

		// The one corsair in hand is played unasked.
		assertEquals(1, scn.GetWoundsOn(castamir));
		assertInZone(Zone.SHADOW_CHARACTERS, brute);
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
