package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("buccaneer", "8_53");
					put("buccaneer2", "8_53");
					put("brute", "10_38");      // Corsair Brute: another corsair, cost 5, site 4; a start-of-skirmish trigger only

					// A second companion, so that the fellowship can hold two threats.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose

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
	public void CorsairBuccaneerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Corsair Buccaneer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>.<br>When you play this minion, you may remove a threat to add 2 [raider] tokens to a card that already has a [raider] token on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("buccaneer");

		assertEquals("Corsair Buccaneer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var buccaneer = scn.GetShadowCard("buccaneer");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(buccaneer);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.AddThreats(1);
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(buccaneer);
		assertInZone(Zone.SHADOW_CHARACTERS, buccaneer);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(buccaneer));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesAThreatToAddTwoRaiderTokensToACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var buccaneer = scn.GetShadowCard("buccaneer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ships = scn.GetShadowCard("ships");
		var raiders = scn.GetShadowCard("raiders");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(buccaneer);
		scn.MoveCardsToSupportArea(ships, raiders, galley, rank, leaders);

		scn.StartGame();
		scn.AddThreats(2);
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(raiders, 2);
		scn.AddTokensToCard(rank, 2);
		scn.AddTokensToCard(leaders, 2);
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(buccaneer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(buccaneer));
		scn.ShadowAcceptOptionalTrigger();

		// One threat, not both.
		assertEquals(1, scn.GetThreats());

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
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoRaiderTokenInPlayTheThreatIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var buccaneer = scn.GetShadowCard("buccaneer");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		scn.MoveCardsToHand(buccaneer);
		scn.MoveCardsToSupportArea(galley, rank);

		scn.StartGame();
		scn.AddThreats(1);
		scn.AddTokensToCard(rank, 2);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(buccaneer);
		// It has a cost, so it is offered with nothing to add tokens to.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(buccaneer));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var buccaneer = scn.GetShadowCard("buccaneer");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(buccaneer);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(buccaneer);

		assertInZone(Zone.SHADOW_CHARACTERS, buccaneer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherCorsairPlayedBringsNoOfferAndASecondCopyMakesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var buccaneer = scn.GetShadowCard("buccaneer");
		var buccaneer2 = scn.GetShadowCard("buccaneer2");
		var brute = scn.GetShadowCard("brute");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(brute, buccaneer2);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(buccaneer);
		scn.AddThreats(2);
		scn.AddTokensToCard(ships, 1);
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(brute);
		assertInZone(Zone.SHADOW_CHARACTERS, brute);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second Buccaneer is the one played, and the offer is its own.
		scn.ShadowPlayCard(buccaneer2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(buccaneer2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(buccaneer));
		scn.ShadowChooseOptionalTrigger(buccaneer2);

		// The one card with a [raider] token is chosen unasked; once, and nothing more is asked.
		assertEquals(1, scn.GetThreats());
		assertEquals(3, scn.GetCultureTokensOn(ships));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
