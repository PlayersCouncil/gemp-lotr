package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lookout", "8_56");
					put("lookout2", "8_56");
					put("brute", "10_38");      // Corsair Brute: another corsair, cost 5, site 4; a start-of-skirmish trigger only

					// The Free Peoples player's allies, of two cultures and races, and a companion beside them.
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose

					// Cards that carry culture tokens; each is given tokens by its own text.
					put("ships", "8_65");       // Ships of Great Draught: [raider] possession in the support area
					put("raiders", "4_242");    // Raiders From the East: [raider] condition in the support area
					put("galley", "8_59");      // Corsair War Galley: [raider] possession in the support area; initiative only at 6 [raider] tokens
					put("rank", "10_96");       // Rank and File: [sauron] condition in the support area, with [sauron] tokens
					put("leaders", "7_112");    // Noble Leaders: the Free Peoples player's [gondor] condition, with [gondor] tokens

					// Cards for the Free Peoples player's hand, which sets who has initiative.
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairLookoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Corsair Lookout
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>.<br>When you play this minion, if you have initiative, you may discard an ally to add 2 [raider] tokens to a card that already has a [raider] token on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lookout");

		assertEquals("Corsair Lookout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithInitiativeDiscardsAnAllyToAddTwoRaiderTokensToACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lookout = scn.GetShadowCard("lookout");
		var barliman = scn.GetFreepsCard("barliman");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ships = scn.GetShadowCard("ships");
		var raiders = scn.GetShadowCard("raiders");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman, bounder);
		scn.MoveCardsToHand(lookout);
		scn.MoveCardsToSupportArea(ships, raiders, galley, rank, leaders);
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));

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
		assertInZone(Zone.SUPPORT, barliman, bounder);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		scn.ShadowPlayCard(lookout);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lookout));
		scn.ShadowAcceptOptionalTrigger();

		// An ally of either culture, not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(barliman, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, scn.GetRingBearer()));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bounder);

		assertInDiscard(bounder);
		assertInZone(Zone.SUPPORT, barliman);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);

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
	public void WithFourCardsInTheFreePeoplesHandTheShadowLacksInitiativeAndNoOfferIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lookout = scn.GetShadowCard("lookout");
		var barliman = scn.GetFreepsCard("barliman");
		var ships = scn.GetShadowCard("ships");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(lookout);
		scn.MoveCardsToSupportArea(ships);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertInZone(Zone.SUPPORT, barliman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasInitiative());
		scn.ShadowPlayCard(lookout);

		assertInZone(Zone.SHADOW_CHARACTERS, lookout);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, barliman);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lookout = scn.GetShadowCard("lookout");
		var barliman = scn.GetFreepsCard("barliman");
		var ships = scn.GetShadowCard("ships");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(lookout);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertInZone(Zone.SUPPORT, barliman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		scn.ShadowPlayCard(lookout);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lookout));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, barliman);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithInitiativeAndNoAllyToDiscardNoOfferIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lookout = scn.GetShadowCard("lookout");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(lookout);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowHasInitiative());
		scn.ShadowPlayCard(lookout);

		assertInZone(Zone.SHADOW_CHARACTERS, lookout);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoRaiderTokenInPlayTheAllyIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lookout = scn.GetShadowCard("lookout");
		var barliman = scn.GetFreepsCard("barliman");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(lookout);
		scn.MoveCardsToSupportArea(galley, rank);

		scn.StartGame();
		scn.AddTokensToCard(rank, 2);
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertInZone(Zone.SUPPORT, barliman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowHasInitiative());
		scn.ShadowPlayCard(lookout);
		// It has a cost, so it is offered with nothing to add tokens to.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lookout));
		scn.ShadowAcceptOptionalTrigger();

		// The one ally is discarded unasked.
		assertInDiscard(barliman);
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherCorsairPlayedBringsNoOfferAndASecondCopyMakesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lookout = scn.GetShadowCard("lookout");
		var lookout2 = scn.GetShadowCard("lookout2");
		var brute = scn.GetShadowCard("brute");
		var barliman = scn.GetFreepsCard("barliman");
		var bounder = scn.GetFreepsCard("bounder");
		var ships = scn.GetShadowCard("ships");
		scn.MoveAlliesToTable(barliman, bounder);
		scn.MoveCardsToHand(brute, lookout2);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(lookout);
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertInZone(Zone.SUPPORT, barliman, bounder);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowHasInitiative());
		scn.ShadowPlayCard(brute);
		assertInZone(Zone.SHADOW_CHARACTERS, brute);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, barliman, bounder);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second Lookout is the one played, and the offer is its own.
		scn.ShadowPlayCard(lookout2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lookout2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lookout));
		scn.ShadowChooseOptionalTrigger(lookout2);
		scn.ShadowChooseCard(barliman);

		// The one card with a [raider] token is chosen unasked; once, and nothing more is asked.
		assertInDiscard(barliman);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(3, scn.GetCultureTokensOn(ships));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
