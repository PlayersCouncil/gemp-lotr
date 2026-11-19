package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_015_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("moon", "33_15");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, vitality 4; +1 for each [dwarven] follower he bears
					put("thorin", "30_19");
					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, vitality 3; a fellowship ability only
					put("balin", "30_2");
					// Dain Ironfoot: a Dwarf ally, who could exert; Thorin +1, a regroup trigger with a [dwarven] follower
					put("dain", "32_2");
					// Dwarven Ponies: [dwarven] possession in the support area; a trigger on Bifur, Bofur or Bombur
					// played, and a fellowship ability used only on purpose
					put("ponies", "33_6");
					// Dwarven Axe: [dwarven] possession, hand weapon, on a [dwarven] character; damage +1 at a mountain
					put("axe", "31_1");
					// Emeralds of Girion: a [dwarven] artifact, no possession, in the support area; abilities only
					put("emeralds", "31_3");
					// An Acorn from Beorn's house: a [shire] possession on Bilbo; a regroup ability only
					put("acorn", "31_38");
					// Wise Advice: an [elven] fellowship event
					put("advice", "33_18");
					// The Evil Becomes Stronger: an [elven] archery event
					put("evil", "31_11");
					// Thranduil, Elven King: an [elven] Elf ally
					put("thranduil", "31_12");
					// Vilya: an [elven] artifact
					put("vilya", "33_17");
					// Borders of the Forest: a [mirkwood] condition (a Shadow card)
					put("borders", "33_30");
					// Ancient Grudge: a [mirkwood] condition (a Shadow card)
					put("grudge", "33_29");
					// Lore of Imladris: a [dwarven] event
					put("lore", "30_13");
					// Battle Fury: a [dwarven] event
					put("fury", "30_3");
					// Hatred Rekindled: a [gundabad] condition (a Shadow card)
					put("hatred", "30_34");
					// Elf Army: an [elven] Elf ally, laid in the other player's discard pile
					put("elfarmy", "31_8");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void MoonLettersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Moon-Letters
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Exert a Dwarf companion (or spot a [dwarven] possession) to take into hand an [elven] or [mirkwood] card from your draw deck or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("moon");

		assertEquals("Moon-Letters", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithBothCostsPayableTheChosenDwarfExertsAndACardIsTakenFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var moon = scn.GetFreepsCard("moon");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var ponies = scn.GetFreepsCard("ponies");
		var advice = scn.GetFreepsCard("advice");
		var thranduil = scn.GetFreepsCard("thranduil");
		var borders = scn.GetFreepsCard("borders");
		var lore = scn.GetFreepsCard("lore");
		var hatred = scn.GetFreepsCard("hatred");
		var evil = scn.GetFreepsCard("evil");
		var vilya = scn.GetFreepsCard("vilya");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(ponies);
		scn.MoveCardsToHand(moon);
		scn.SetFreepsDrawDeck(advice, thranduil, borders, vilya, lore, hatred);
		scn.MoveCardsToDiscard(evil);

		scn.StartGame();
		assertInZone(Zone.HAND, moon);
		assertInZone(Zone.DECK, advice, thranduil, borders, vilya, lore, hatred);
		assertInZone(Zone.DISCARD, evil);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.MarkGameLog();
		assertTrue(scn.FreepsPlayAvailable(moon));
		scn.FreepsPlayCard(moon);

		// A Dwarf companion to exert, or a [dwarven] possession to spot: the player is asked which.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Exert");
		// Dwarf companions of two titles; not a Dwarf ally, nor Bilbo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dain, bilbo));
		scn.FreepsChooseCard(balin);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		// Something to take in each pile: the pile is asked.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("draw deck");
		scn.FreepsDismissRevealedCards();
		// [elven] cards of three types (an event, an ally, an artifact) and a [mirkwood] condition; not a [dwarven] event
		// nor a [gundabad] condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(advice, thranduil, vilya, borders));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lore, hatred));
		// A look through a draw deck never forces a card to be taken (F-0049).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(borders);

		assertInZone(Zone.HAND, borders);
		assertInZone(Zone.DECK, advice, thranduil, vilya, lore, hatred);
		assertInZone(Zone.DISCARD, evil, moon);
		// The card taken is named in the game log, and one left in the deck is not.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(borders));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(advice));
		// The deck looked through is shuffled.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheDwarvesExhaustedABorneDwarvenPossessionIsSpottedAndTheDiscardPileUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var moon = scn.GetFreepsCard("moon");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var emeralds = scn.GetFreepsCard("emeralds");
		var acorn = scn.GetFreepsCard("acorn");
		var lore = scn.GetFreepsCard("lore");
		var evil = scn.GetFreepsCard("evil");
		var vilya = scn.GetFreepsCard("vilya");
		var thranduil = scn.GetFreepsCard("thranduil");
		var grudge = scn.GetFreepsCard("grudge");
		var fury = scn.GetFreepsCard("fury");
		var hisElfArmy = scn.GetShadowCard("elfarmy");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, axe);
		scn.AttachCardsTo(bilbo, acorn);
		scn.MoveCardsToSupportArea(emeralds);
		scn.MoveCardsToHand(moon);
		scn.SetFreepsDrawDeck(lore);
		scn.MoveCardsToDiscard(evil, vilya, thranduil, grudge, fury, hisElfArmy);

		scn.StartGame();
		// Thorin (vitality 4) and Balin (vitality 3) exhausted: no Dwarf can exert.
		scn.AddWoundsToChar(thorin, 3);
		scn.AddWoundsToChar(balin, 2);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertAttachedTo(axe, thorin);
		assertInZone(Zone.DECK, lore);
		assertInZone(Zone.DISCARD, evil, vilya, thranduil, grudge, fury, hisElfArmy);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(moon));
		scn.FreepsPlayCard(moon);

		// The Dwarven Axe, which Thorin bears, is spotted; with nothing to take from the draw deck the discard pile
		// is used, and neither choice is asked.
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetWoundsOn(balin));
		// [elven] cards of three types (an event, an artifact, an ally) and a [mirkwood] condition from his own discard
		// pile; not a [dwarven] event, nor the other player's [elven] ally in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(evil, vilya, thranduil, grudge));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(fury, hisElfArmy));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(vilya);

		assertInZone(Zone.HAND, vilya);
		assertInZone(Zone.DISCARD, evil, thranduil, grudge, fury, hisElfArmy, moon);
		assertAttachedTo(axe, thorin);
		// No look at the draw deck.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenPossessionTheOneDwarfExertsUnaskedAndTheDrawDeckIsUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moon = scn.GetFreepsCard("moon");
		var thorin = scn.GetFreepsCard("thorin");
		var advice = scn.GetFreepsCard("advice");
		var thranduil = scn.GetFreepsCard("thranduil");
		var lore = scn.GetFreepsCard("lore");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(moon);
		scn.SetFreepsDrawDeck(advice, thranduil, lore);
		scn.MoveCardsToDiscard(fury);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertInZone(Zone.DECK, advice, thranduil, lore);
		assertInZone(Zone.DISCARD, fury);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(moon);
		assertEquals(1, scn.GetWoundsOn(thorin));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(advice);

		assertInZone(Zone.HAND, advice);
		assertInZone(Zone.DECK, thranduil, lore);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotPlayableUntilADwarfCompanionCanExertOrADwarvenPossessionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var moon = scn.GetFreepsCard("moon");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		var emeralds = scn.GetFreepsCard("emeralds");
		var acorn = scn.GetFreepsCard("acorn");
		var ponies = scn.GetFreepsCard("ponies");
		var evil = scn.GetFreepsCard("evil");
		var lore = scn.GetFreepsCard("lore");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.AttachCardsTo(bilbo, acorn);
		scn.MoveCardsToSupportArea(emeralds);
		scn.MoveCardsToHand(moon);
		scn.SetFreepsDrawDeck(lore);
		scn.MoveCardsToDiscard(evil);

		scn.StartGame();
		scn.AddWoundsToChar(thorin, 3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertInZone(Zone.SUPPORT, emeralds);
		assertAttachedTo(acorn, bilbo);
		assertInZone(Zone.DISCARD, evil);
		assertInZone(Zone.DECK, lore);

		// Thorin exhausted; a Dwarf ally who could exert, a [dwarven] artifact and a [shire] possession pay nothing.
		assertFalse(scn.FreepsPlayAvailable(moon));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [dwarven] possession in the support area can be spotted.
		scn.MoveCardsToSupportArea(ponies);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(moon));
		scn.FreepsPlayCard(moon);
		assertInZone(Zone.HAND, evil);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotPlayableWithNothingElvenOrMirkwoodInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moon = scn.GetFreepsCard("moon");
		var thorin = scn.GetFreepsCard("thorin");
		var lore = scn.GetFreepsCard("lore");
		var hatred = scn.GetFreepsCard("hatred");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(moon);
		scn.SetFreepsDrawDeck(lore, hatred);
		scn.MoveCardsToDiscard(fury);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertInZone(Zone.HAND, moon);
		assertInZone(Zone.DECK, lore, hatred);
		assertInZone(Zone.DISCARD, fury);

		assertFalse(scn.FreepsPlayAvailable(moon));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetWoundsOn(thorin));
	}
}
