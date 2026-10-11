package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("freebooter", "8_54");
					put("freebooter2", "8_54");
					put("gunners", "8_55");     // Corsair Gunners: another corsair, cost 5, site 4; a skirmish ability that needs initiative

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
	public void CorsairFreebooterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Corsair Freebooter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>.<br>When you play this minion, you may remove 2 culture tokens to add 2 [raider] tokens to a card that already has a [raider] token on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("freebooter");

		assertEquals("Corsair Freebooter", card.getBlueprint().getTitle());
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
	public void PlayedFromHandForThreeAndTheRoamingPenaltyAndTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freebooter = scn.GetShadowCard("freebooter");
		var ships = scn.GetShadowCard("ships");
		var raiders = scn.GetShadowCard("raiders");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCardsToHand(freebooter);
		scn.MoveCardsToSupportArea(ships, raiders, leaders);

		scn.StartGame();
		scn.AddTokensToCard(ships, 2);
		scn.AddTokensToCard(raiders, 1);
		scn.AddTokensToCard(leaders, 2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(freebooter));
		scn.ShadowPlayCard(freebooter);

		// 3 + 2 for roaming at site 2
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, freebooter);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(freebooter));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesTwoTokensOfAnyCultureFromEitherPlayersCardsToAddTwoRaiderTokensToACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freebooter = scn.GetShadowCard("freebooter");
		var ships = scn.GetShadowCard("ships");
		var raiders = scn.GetShadowCard("raiders");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCardsToHand(freebooter);
		scn.MoveCardsToSupportArea(ships, raiders, galley, rank, leaders);

		scn.StartGame();
		scn.AddTokensToCard(ships, 2);
		scn.AddTokensToCard(raiders, 1);
		scn.AddTokensToCard(rank, 2);
		scn.AddTokensToCard(leaders, 2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(freebooter);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(freebooter));
		scn.ShadowAcceptOptionalTrigger();

		// Any card with a culture token on it: [raider], [sauron], or the Free Peoples player's [gondor].  Not
		// the Galley, which has none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, raiders, rank, leaders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(galley));
		scn.ShadowChooseCard(rank);
		assertEquals(1, scn.GetCultureTokensOn(rank));

		// The second token may come from another card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, raiders, rank, leaders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(galley));
		scn.ShadowChooseCard(leaders);
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(raiders));

		// A card that has a [raider] token: not the [raider] card with none, nor the cards whose tokens are of
		// another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(galley, rank, leaders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(raiders);

		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BothTokensMayComeFromOneRaiderCardWhichMayThenTakeTheTwoAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freebooter = scn.GetShadowCard("freebooter");
		var ships = scn.GetShadowCard("ships");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(freebooter);
		scn.MoveCardsToSupportArea(ships, raiders);

		scn.StartGame();
		scn.AddTokensToCard(ships, 3);
		scn.AddTokensToCard(raiders, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(freebooter);
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, raiders));
		scn.ShadowChooseCard(ships);
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, raiders));
		scn.ShadowChooseCard(ships);
		assertEquals(1, scn.GetCultureTokensOn(ships));

		// With one token left it is still a card that has a [raider] token.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, raiders));
		scn.ShadowChooseCard(ships);

		assertEquals(3, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenTheTokensRemovedWereTheOnlyRaiderTokensNoneAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freebooter = scn.GetShadowCard("freebooter");
		var ships = scn.GetShadowCard("ships");
		var galley = scn.GetShadowCard("galley");
		scn.MoveCardsToHand(freebooter);
		scn.MoveCardsToSupportArea(ships, galley);

		scn.StartGame();
		scn.AddTokensToCard(ships, 2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(freebooter);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(freebooter));
		scn.ShadowAcceptOptionalTrigger();

		// The two tokens on the Ships are the only tokens there are: they pay, and then no card has a [raider]
		// token to add to.
		assertEquals(0, scn.GetCultureTokensOn(ships));
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithOneCultureTokenInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freebooter = scn.GetShadowCard("freebooter");
		var ships = scn.GetShadowCard("ships");
		var galley = scn.GetShadowCard("galley");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCardsToHand(freebooter);
		scn.MoveCardsToSupportArea(ships, galley, leaders);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(freebooter);

		assertInZone(Zone.SHADOW_CHARACTERS, freebooter);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherCorsairPlayedBringsNoOfferAndASecondCopyMakesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freebooter = scn.GetShadowCard("freebooter");
		var freebooter2 = scn.GetShadowCard("freebooter2");
		var gunners = scn.GetShadowCard("gunners");
		var ships = scn.GetShadowCard("ships");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(gunners, freebooter2);
		scn.MoveCardsToSupportArea(ships, raiders);

		scn.StartGame();
		scn.MoveMinionsToTable(freebooter);
		scn.AddTokensToCard(ships, 3);
		scn.AddTokensToCard(raiders, 1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 1 for Frodo + 2 for Ettenmoors; the Gunners cost 5 + 2 for roaming
		assertEquals(23, scn.GetTwilight());
		scn.ShadowPlayCard(gunners);
		assertEquals(16, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, gunners);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second Freebooter is the one played, and the offer is its own.
		scn.ShadowPlayCard(freebooter2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(freebooter2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(freebooter));
		scn.ShadowChooseOptionalTrigger(freebooter2);
		scn.ShadowChooseCard(ships);
		scn.ShadowChooseCard(ships);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.ShadowChooseCard(raiders);

		// Once, and nothing more is asked.
		assertEquals(3, scn.GetCultureTokensOn(raiders));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
