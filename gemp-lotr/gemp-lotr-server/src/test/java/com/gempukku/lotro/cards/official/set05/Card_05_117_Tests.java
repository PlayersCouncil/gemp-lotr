package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helpus", "5_117");
					// Sméagol, Poor Creature: [gollum] companion, Ring-bound, no race; to play, add a burden; a skirmish
					// ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol, Ring-bound; to play, add a burden; an assignment
					// ability used only on purpose
					put("oncelikeus", "103_116");
					// Sméagol, Bearer of Great Secrets: a Sméagol of a third blueprint, laid only in the Shadow
					// player's discard pile
					put("greatsecrets", "9_30");
					put("boromir", "1_97");         // Boromir, Son of Denethor: a companion of another title, playable from the pile
					// Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer; his abilities are used only on purpose
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: an unbound Hobbit companion, vitality 4
					put("merry", "1_303");
					// Faramir, Son of Denethor: a Ring-bound Man, vitality 3; his bar is on the opponent's skirmish cards
					put("faramir", "4_117");
					// Bounder: a Hobbit ally (no ally is Ring-bound), vitality 2; a skirmish ability used only on purpose
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: the Ring-bearer, vitality 4
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YouMustHelpUsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: You Must Help Us
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Play Sméagol from your discard pile to heal each Ring-bound Hobbit twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helpus");

		assertEquals("You Must Help Us", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysSmeagolFromTheDiscardPileToHealEachRingBoundHobbitTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helpus = scn.GetFreepsCard("helpus");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var oncelikeus = scn.GetFreepsCard("oncelikeus");
		var boromir = scn.GetFreepsCard("boromir");
		var greatsecrets = scn.GetShadowCard("greatsecrets");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var faramir = scn.GetFreepsCard("faramir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(helpus);
		scn.MoveCompanionsToTable(sam, merry, faramir);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToDiscard(smeagol, oncelikeus, boromir, greatsecrets);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(faramir, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.HAND, helpus);
		assertInZone(Zone.DISCARD, smeagol, oncelikeus, boromir, greatsecrets);
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsPlayAvailable(helpus));
		scn.FreepsPlayCard(helpus);

		// Each Sméagol in his own discard pile; not a companion of another title there, nor the other player's Sméagol in his.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, oncelikeus));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, greatsecrets));
		scn.FreepsChooseCardBPFromSelection(smeagol);

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		// Sméagol's own "To play, add a burden".
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, oncelikeus, helpus);
		// 3 - 2
		assertEquals(1, scn.GetWoundsOn(frodo));
		// Only 1 to heal.
		assertEquals(0, scn.GetWoundsOn(sam));
		// An unbound Hobbit, a Ring-bound Man and a Hobbit ally are not healed.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShireSmeagolPaysAndWithNoRingBoundHobbitWoundedNobodyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helpus = scn.GetFreepsCard("helpus");
		var frodo = scn.GetRingBearer();
		var oncelikeus = scn.GetFreepsCard("oncelikeus");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(helpus);
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToDiscard(oncelikeus);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.HAND, helpus);
		assertInZone(Zone.DISCARD, oncelikeus);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsPlayAvailable(helpus));
		// The one Sméagol in the pile is played unasked.
		scn.FreepsPlayCard(helpus);

		assertInZone(Zone.FREE_CHARACTERS, oncelikeus);
		assertInZone(Zone.DISCARD, helpus);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutASmeagolInHisOwnDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helpus = scn.GetFreepsCard("helpus");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var shadowsmeagol = scn.GetShadowCard("smeagol");
		scn.MoveCardsToHand(helpus);
		// A companion of another title in his pile, and a Sméagol in the other player's.
		scn.MoveCardsToDiscard(boromir, shadowsmeagol);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DISCARD, boromir, shadowsmeagol);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.HAND, helpus);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(helpus));
	}

	@Test
	public void CannotBePlayedWhileTheSmeagolInThePileCannotBePlayedForOneInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helpus = scn.GetFreepsCard("helpus");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var oncelikeus = scn.GetFreepsCard("oncelikeus");
		scn.MoveCardsToHand(helpus);
		scn.MoveCompanionsToTable(oncelikeus);
		scn.MoveCardsToDiscard(smeagol);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		// Sméagol is unique: with one in play, the one in the pile cannot be played.
		assertInZone(Zone.FREE_CHARACTERS, oncelikeus);
		assertInZone(Zone.DISCARD, smeagol);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.HAND, helpus);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(helpus));
	}
}
