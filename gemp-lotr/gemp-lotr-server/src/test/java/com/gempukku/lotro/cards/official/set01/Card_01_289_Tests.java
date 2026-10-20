package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_289_Tests
{

	// Frodo, Old Bilbo's Heir is the table's Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: [shire] Hobbit ally, vitality 3, home 1F; a fellowship ability used only on purpose
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, vitality 3, home 3F; a fellowship ability used only on purpose
					put("goldberry", "9_51");   // Goldberry, River-daughter: a [shire] ally who is no Hobbit, vitality 6; her texts need a river
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit companion; strength +2 only with a weapon
				}},
				VirtualTableScenario.FellowshipSites,
				"1_289",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Frodo, Old Bilbo's Heir
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Signet: Gandalf
		 * Game Text: <b>Ring-bearer (resistance 10).</b><br>At the start of each of your turns, you may heal a Hobbit ally.
		*/

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Old Bilbo's Heir", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
		assertTrue(card.getBlueprint().canStartWithRing());
		// Every Frodo is Ring-bound by rule (CR 4.2 "Ring-bound"), though the card does not print it.
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
	}

	@Test
	public void AtTheStartOfHisOwnersTurnHeMayHealOneWoundFromAHobbitAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var maggot = scn.GetFreepsCard("maggot");
		var bilbo = scn.GetFreepsCard("bilbo");
		var goldberry = scn.GetFreepsCard("goldberry");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveAlliesToTable(maggot, bilbo, goldberry);
		scn.MoveCompanionsToTable(merry);
		scn.AddWoundsToChar(maggot, 2);
		scn.AddWoundsToChar(bilbo, 1);
		scn.AddWoundsToChar(goldberry, 1);
		scn.AddWoundsToChar(merry, 1);

		scn.StartGame();

		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsAcceptOptionalTrigger();

		// The two Hobbit allies; not the [shire] ally who is no Hobbit, and not the Hobbit companion.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(maggot, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(goldberry, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(maggot);

		// One wound of his two.
		assertEquals(1, scn.GetWoundsOn(maggot));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(goldberry));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedItHealsNobodyAndIsOfferedAgainAtHisOwnersNextTurnAndNotAtTheOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveAlliesToTable(maggot);
		scn.AddWoundsToChar(maggot, 2);

		scn.StartGame();

		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(maggot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The other player's turn begins with his fellowship phase: nothing is offered and nobody is healed.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(frodo));
		assertEquals(2, scn.GetWoundsOn(maggot));

		// The owner's second turn: offered again, and taken.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsAcceptOptionalTrigger();

		// The one Hobbit ally is healed without a choice being put.
		assertEquals(1, scn.GetWoundsOn(maggot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoHobbitAllyIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var maggot = scn.GetFreepsCard("maggot");
		var goldberry = scn.GetFreepsCard("goldberry");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveAlliesToTable(maggot, goldberry);
		scn.MoveCompanionsToTable(merry);
		// Wounded, but neither is a Hobbit ally.
		scn.AddWoundsToChar(goldberry, 1);
		scn.AddWoundsToChar(merry, 1);

		scn.StartGame();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertEquals(1, scn.GetWoundsOn(goldberry));
		assertEquals(1, scn.GetWoundsOn(merry));
	}
}
