package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_32_030_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 (resistance 8) as the Ring-bearer. The
	// Shadow phase of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bats", "32_30");
					// A second copy: a [moria] minion that is no Orc
					put("bats2", "32_30");

					// Orcs of two cultures
					put("runner", "31_30");     // Goblin Runner: [moria] Orc; a "when you play" trigger, cheated in
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc; an assignment ability not used
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc with no text

					// Cards that stay in the draw deck, for something to draw
					put("deck1", "1_177");
					put("deck2", "1_177");
					put("deck3", "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DarkBatsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Dark Bats
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Creature
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Shadow:</b> Exert this minion to draw 3 cards. If you cannot spot 3 Orcs, the Free Peoples player may add 2 doubts to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bats");

		assertEquals("Dark Bats", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithThreeOrcsTheBatsExertToDrawThreeCardsAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bats = scn.GetShadowCard("bats");
		var bats2 = scn.GetShadowCard("bats2");

		scn.StartGame();
		scn.MoveMinionsToTable(bats, bats2);
		// Three Orcs, of two cultures
		scn.MoveMinionsToTable("runner", "aggressor", "troop");
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetWoundsOn(bats2));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(bats));

		scn.ShadowUseCardAction(bats);
		// Three Orcs are spotted, so the Free Peoples player has no way to prevent the draw.
		assertFalse(scn.FreepsDecisionAvailable("add 2 doubts"));
		assertEquals(1, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetWoundsOn(bats2));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoOrcsTheFreePeoplesPlayerMayAddTwoDoubtsToPreventTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bats = scn.GetShadowCard("bats");
		var bats2 = scn.GetShadowCard("bats2");

		scn.StartGame();
		// Two Orcs and two minions that are no Orcs: one short of 3 Orcs.
		scn.MoveMinionsToTable(bats, bats2);
		scn.MoveMinionsToTable("runner", "aggressor");
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowUseCardAction(bats);
		assertTrue(scn.FreepsDecisionAvailable("add 2 doubts"));
		scn.FreepsChooseYes();

		assertEquals(2, scn.GetBurdens());
		// The exertion was the cost and stays paid.
		assertEquals(1, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedThePreventionTheShadowPlayerDrawsThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bats = scn.GetShadowCard("bats");
		var bats2 = scn.GetShadowCard("bats2");

		scn.StartGame();
		scn.MoveMinionsToTable(bats, bats2);
		scn.MoveMinionsToTable("runner", "aggressor");
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetWoundsOn(bats2));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetBurdens());

		scn.ShadowUseCardAction(bats);
		assertTrue(scn.FreepsDecisionAvailable("add 2 doubts"));
		scn.FreepsChooseNo();

		assertEquals(0, scn.GetBurdens());
		// This copy exerted, not the other.
		assertEquals(1, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetWoundsOn(bats2));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheBatsAreExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bats = scn.GetShadowCard("bats");
		var bats2 = scn.GetShadowCard("bats2");

		scn.StartGame();
		scn.MoveMinionsToTable(bats, bats2);
		scn.MoveMinionsToTable("runner", "aggressor", "troop");
		// Vitality 2 with 1 wound: this copy cannot exert.
		scn.AddWoundsToChar(bats, 1);
		assertEquals(1, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetWoundsOn(bats2));
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetWoundsOn(bats2));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bats));
		// The other copy, fresh, may use its own.
		assertTrue(scn.ShadowActionAvailable(bats2));
	}

	@Test
	public void WithTheShadowDrawDeckEmptyThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bats = scn.GetShadowCard("bats");
		var bats2 = scn.GetShadowCard("bats2");

		scn.StartGame();
		scn.MoveMinionsToTable(bats, bats2);
		scn.MoveMinionsToTable("runner", "aggressor");
		scn.SetShadowDrawDeck();
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetWoundsOn(bats));
		assertTrue(scn.ShadowActionAvailable(bats));
		scn.ShadowUseCardAction(bats);

		// The cost is paid; with nothing to draw the Free Peoples player is not asked to prevent it.
		assertEquals(1, scn.GetWoundsOn(bats));
		assertFalse(scn.FreepsDecisionAvailable("add 2 doubts"));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
