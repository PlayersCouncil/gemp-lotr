package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_284_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bilbo", "1_284");

					// Cards for the discard pile. None of them is played.
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] companion
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("deftness", "1_293");   // Halfling Deftness: a [shire] event
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit, the race without the culture
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Bilbo, Retired Adventurer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fellowship:</b> Exert Bilbo to shuffle a [shire] card from your discard pile into your draw deck.
		*/

		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");

		assertEquals("Bilbo", bilbo.getBlueprint().getTitle());
		assertEquals("Retired Adventurer", bilbo.getBlueprint().getSubtitle());
		assertTrue(bilbo.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, bilbo.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, bilbo.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, bilbo.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, bilbo.getBlueprint().getRace());
		assertEquals(2, bilbo.getBlueprint().getTwilightCost());
		assertEquals(2, bilbo.getBlueprint().getStrength());
		assertEquals(3, bilbo.getBlueprint().getVitality());
		assertTrue(bilbo.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void ExertsBilboToShuffleAChosenShireCardFromHisDiscardPileIntoHisDrawDeckUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var deftness = scn.GetFreepsCard("deftness");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveAlliesToTable(bilbo);
		// [shire] cards of three types, and a Hobbit of another culture beside them.
		scn.MoveCardsToDiscard(merry, sword, deftness, pippin);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertInZone(Zone.DISCARD, merry, sword, deftness, pippin);
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, sword, deftness));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(sword);
		assertInZone(Zone.DECK, sword);
		assertInZone(Zone.DISCARD, merry, deftness, pippin);
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, deftness));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin));
		scn.FreepsChooseCardBPFromSelection(merry);
		assertInZone(Zone.DECK, sword, merry);
		assertInZone(Zone.DISCARD, deftness, pippin);
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());

		// Vitality 3 with 2 wounds: exhausted, with a [shire] card still in the discard pile.
		assertFalse(scn.FreepsActionAvailable(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShireCardInHisDiscardPileBilboExertsAndNoCardLeavesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToDiscard(pippin);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertInZone(Zone.DISCARD, pippin);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		// The exertion can be paid, so the ability is on offer though there is no [shire] card to shuffle in.
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertInZone(Zone.DISCARD, pippin);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		// A shuffle into a draw deck is made though nothing is shuffled in.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
