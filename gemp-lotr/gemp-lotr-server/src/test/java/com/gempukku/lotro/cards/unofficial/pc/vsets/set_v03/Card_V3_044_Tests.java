package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_044_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("burn", "103_44");
					put("scout", "4_252");      // Southron Scout: [raider] minion; keywords only
					put("soldier", "4_254");    // Southron Soldier: [raider] minion; keywords only
					put("attacker", "7_141");   // Easterling Attacker: [raider] minion; a skirmish ability never used here
					put("ships", "8_65");       // Ships of Great Draught: [raider] possession that holds [raider] tokens; its abilities are never used here
					put("ships2", "8_65");
					put("galley", "8_59");      // Corsair War Galley: [raider] possession that holds [raider] tokens; initiative at 6 tokens, a regroup ability never used here
					put("galley2", "8_59");
					put("tidings", "7_130");    // Dark Tidings: [raider] condition that holds no tokens; a regroup ability never used here
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, no text at all
					put("towers", "7_281");     // Great Siege-towers: [sauron] condition that holds [sauron] tokens; its abilities are never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("athelas", "1_94");     // Athelas: Free Peoples possession; a fellowship ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BurnandPillageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Burn and Pillage
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Hinder up to 4 [raider] cards to reinforce the same number of [raider] tokens.
		 * 	Draw a card for each 2 hindered Shadow cards you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("burn");

		assertEquals("Burn and Pillage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HindersUpToFourRaiderCardsAndReinforcesThatManyRaiderTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burn = scn.GetShadowCard("burn");
		var scout = scn.GetShadowCard("scout");
		var soldier = scn.GetShadowCard("soldier");
		var attacker = scn.GetShadowCard("attacker");
		var ships = scn.GetShadowCard("ships");
		var hinderedShips = scn.GetShadowCard("ships2");
		var emptyGalley = scn.GetShadowCard("galley");
		var galley = scn.GetShadowCard("galley2");
		var tidings = scn.GetShadowCard("tidings");
		var troop = scn.GetShadowCard("troop");
		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToHand(burn);
		scn.MoveCardsToSupportArea(ships, hinderedShips, emptyGalley, galley, tidings, towers);
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(hinderedShips, 1);
		scn.AddTokensToCard(galley, 1);
		scn.AddTokensToCard(towers, 1);     // a [sauron] token

		scn.StartGame();
		scn.MoveMinionsToTable(scout, soldier, attacker, troop);
		scn.HinderCard(hinderedShips);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowPlayAvailable(burn));

		scn.ShadowPlayCard(burn);

		// Seven [raider] cards could be hindered, of three types; the choice is of none to four of them. Not
		// offered: the [moria] minion, the [sauron] condition, and the [raider] possession already hindered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, soldier, attacker, ships, emptyGalley, galley, tidings));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, towers, hinderedShips));
		assertEquals(7, scn.ShadowGetCardChoiceCount());
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		assertFalse(scn.IsHindered(scout));
		assertFalse(scn.IsHindered(soldier));
		assertFalse(scn.IsHindered(attacker));
		assertFalse(scn.IsHindered(galley));

		scn.ShadowChooseCards(scout, soldier, attacker, galley);

		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.IsHindered(scout));
		assertTrue(scn.IsHindered(soldier));
		assertTrue(scn.IsHindered(attacker));
		assertTrue(scn.IsHindered(galley));
		assertFalse(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(emptyGalley));
		assertFalse(scn.IsHindered(tidings));
		assertFalse(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(towers));

		// Four tokens to reinforce, and one card to take them: the Ships are the only card left that has a
		// [raider] token and is not hindered, so nothing is asked. 1 + 4.
		assertEquals(5, scn.GetCultureTokensOn(ships));
		// Not the Galley just hindered or the Ships hindered before, though each has a token; not the Galley or
		// the condition with no token; not the condition with a [sauron] token.
		assertEquals(1, scn.GetCultureTokensOn(galley));
		assertEquals(1, scn.GetCultureTokensOn(hinderedShips));
		assertEquals(0, scn.GetCultureTokensOn(emptyGalley));
		assertEquals(0, scn.GetCultureTokensOn(tidings));
		assertEquals(1, scn.GetCultureTokensOn(towers));

		// Five hindered Shadow cards (the four and the Ships hindered before): two cards drawn.
		assertInDiscard(burn);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ReinforcesOnEitherUnhinderedCardWithARaiderTokenAndDrawsForHinderedShadowCardsOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burn = scn.GetShadowCard("burn");
		var scout = scn.GetShadowCard("scout");
		var ships = scn.GetShadowCard("ships");
		var galley = scn.GetShadowCard("galley");
		var galley2 = scn.GetShadowCard("galley2");
		var troop = scn.GetShadowCard("troop");
		var towers = scn.GetShadowCard("towers");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCardsToHand(burn);
		scn.MoveCardsToSupportArea(ships, galley, galley2, towers);
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(galley, 1);
		scn.AddTokensToCard(galley2, 1);
		scn.AddTokensToCard(towers, 1);     // a [sauron] token
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// One Shadow card that is not [raider] and two Free Peoples cards are hindered already.
		scn.HinderCard(troop, aragorn, athelas);
		assertTrue(scn.IsHindered(troop));
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(athelas));
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(burn);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, ships, galley, galley2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, towers, aragorn, athelas));
		scn.ShadowChooseCards(galley2);

		assertTrue(scn.IsHindered(galley2));
		assertFalse(scn.IsHindered(scout));

		// One token, on either of the two cards that have a [raider] token and are not hindered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, galley));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(galley2, scout, towers));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(galley);

		assertEquals(2, scn.GetCultureTokensOn(galley));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(galley2));
		assertEquals(1, scn.GetCultureTokensOn(towers));

		// Two hindered Shadow cards, the Galley and the Troop: one card. With the two Free Peoples cards counted
		// it would be two, with only [raider] cards counted none, with the unhindered Shadow cards counted three.
		assertInDiscard(burn);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TokensReinforcedMayBeDividedBetweenCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burn = scn.GetShadowCard("burn");
		var scout = scn.GetShadowCard("scout");
		var soldier = scn.GetShadowCard("soldier");
		var ships = scn.GetShadowCard("ships");
		var galley = scn.GetShadowCard("galley");
		scn.MoveCardsToHand(burn);
		scn.MoveCardsToSupportArea(ships, galley);
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(galley, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(burn);
		scn.ShadowChooseCards(scout, soldier);

		// Two tokens to reinforce: the first on the Ships, the second on the Galley.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, galley));
		scn.ShadowChooseCard(ships);
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(galley));
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, galley));
		scn.ShadowChooseCard(galley);

		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetCultureTokensOn(galley));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void HinderingNoneReinforcesNothingAndStillDrawsForCardsAlreadyHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burn = scn.GetShadowCard("burn");
		var scout = scn.GetShadowCard("scout");
		var soldier = scn.GetShadowCard("soldier");
		var ships = scn.GetShadowCard("ships");
		var tidings = scn.GetShadowCard("tidings");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(burn);
		scn.MoveCardsToSupportArea(ships, tidings);
		scn.AddTokensToCard(ships, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, soldier, troop);
		scn.HinderCard(soldier, tidings, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(13, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(burn);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, ships));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertEquals(12, scn.GetTwilight());
		assertFalse(scn.IsHindered(scout));
		assertFalse(scn.IsHindered(ships));
		assertEquals(2, scn.GetCultureTokensOn(ships));

		// Three hindered Shadow cards: one card, the odd one counting for nothing.
		assertInDiscard(burn);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWithNoRaiderCardToHinderButNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burn = scn.GetShadowCard("burn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(burn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(burn));

		scn.ShadowPass();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(burn));
	}
}
