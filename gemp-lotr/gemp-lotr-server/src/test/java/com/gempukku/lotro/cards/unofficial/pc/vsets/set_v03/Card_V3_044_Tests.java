package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_044_Tests
{

// ----------------------------------------
// BURN AND PILLAGE TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("burn", "103_44");        // Burn and Pillage
					put("ships", "8_65");         // Ships of Great Draught - token holder
					put("southron1", "4_222");    // Desert Warrior - [Raider] Southron Man
					put("southron2", "4_222");
					put("southron3", "4_222");
					put("southron4", "4_222");
					put("orc", "1_271");          // Orc Soldier - [Sauron], not Raider
					put("fodder1", "1_272");
					put("fodder2", "1_272");
					put("fodder3", "1_272");
					put("fodder4", "1_272");
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
	public void BurnAndPillageHindersRaiderCardsOnlyAndReinforcesTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burn = scn.GetShadowCard("burn");
		var ships = scn.GetShadowCard("ships");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(burn);
		scn.MoveCardsToSupportArea(ships);
		scn.MoveMinionsToTable(southron1, southron2, orc);
		scn.AddTokensToCard(ships, 1); // Need existing token to reinforce

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		int tokensBefore = scn.GetCultureTokensOn(ships);
		assertFalse(scn.IsHindered(southron1));
		assertFalse(scn.IsHindered(southron2));

		scn.ShadowPlayCard(burn);

		// Should only offer Raider cards, not the Orc
		assertTrue(scn.ShadowHasCardChoicesAvailable(southron1, southron2, ships));
		assertFalse(scn.ShadowHasCardChoiceAvailable(orc));

		scn.ShadowChooseCards(southron1, southron2);

		// Choose card to reinforce tokens on
		scn.ShadowChooseCard(ships);

		assertTrue(scn.IsHindered(southron1));
		assertTrue(scn.IsHindered(southron2));
		assertEquals(tokensBefore + 2, scn.GetCultureTokensOn(ships));
	}

	@Test
	public void BurnAndPillageDrawsCardsBasedOnTotalHinderedShadowCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burn = scn.GetShadowCard("burn");
		var ships = scn.GetShadowCard("ships");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var southron4 = scn.GetShadowCard("southron4");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(burn);
		scn.MoveCardsToSupportArea(ships);
		scn.MoveMinionsToTable(southron1, southron2, southron3, southron4, orc);
		scn.AddTokensToCard(ships, 1);
		// Pre-hinder some cards to boost draw count
		scn.HinderCard(orc); // Orc is Shadow, counts for draw even though not Raider

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(burn);
		// Hinder 3 Raider cards
		scn.ShadowChooseCards(southron1, southron2, southron3);
		scn.ShadowChooseCard(ships); // Reinforce target

		// Total hindered Shadow cards: orc (1) + southron1,2,3 (3) = 4
		// 4 / 2 = 2 cards drawn
		assertEquals(2, scn.GetShadowHandCount());
	}

	@Test
	public void BurnAndPillageCanHinderZeroCardsButStillDrawsIfOthersHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burn = scn.GetShadowCard("burn");
		var ships = scn.GetShadowCard("ships");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(burn);
		scn.MoveCardsToSupportArea(ships);
		scn.MoveMinionsToTable(southron1, southron2, orc);
		scn.AddTokensToCard(ships, 1);
		// Pre-hinder 4 Shadow cards for draw
		scn.HinderCard(southron1, southron2, orc, ships);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		int tokensBefore = scn.GetCultureTokensOn(ships);

		scn.ShadowPlayCard(burn);
		// Choose to hinder 0 cards
		scn.ShadowDeclineChoosing();

		// No reinforcement (hindered 0)
		assertEquals(tokensBefore, scn.GetCultureTokensOn(ships));

		// But still draws: 4 hindered / 2 = 2 cards
		assertEquals(2, scn.GetShadowHandCount());
	}

	@Test
	public void BurnAndPillageRoundsDownForCardDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burn = scn.GetShadowCard("burn");
		var ships = scn.GetShadowCard("ships");
		var southron1 = scn.GetShadowCard("southron1");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(burn);
		scn.MoveCardsToSupportArea(ships);
		scn.MoveMinionsToTable(southron1, orc);
		scn.AddTokensToCard(ships, 1);
		// Pre-hinder 1 card
		scn.HinderCard(orc);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(burn);
		// Hinder 2 more Raider cards
		scn.ShadowChooseCards(southron1, ships);
		// No need to choose reinforce target if auto-selected

		// Total hindered: orc (1) + southron1 (1) + ships (1) = 3
		// 3 / 2 = 1 (rounds down)
		assertEquals( 1, scn.GetShadowHandCount());
	}
}
