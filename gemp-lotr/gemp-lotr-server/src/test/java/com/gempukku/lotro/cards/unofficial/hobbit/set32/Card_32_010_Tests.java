package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_010_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The regroup phase of
	// turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("heirlooms", "32_10");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, vitality 3; a fellowship ability only
					// Thorin, Oakenshield: [dwarven] Dwarf companion; +1 for each [dwarven] follower he bears, a fellowship ability
					put("thorin", "30_19");
					put("dain", "32_2");        // Dain Ironfoot: [dwarven] Dwarf ally, vitality 3; +1 to Thorin and a start-of-regroup offer

					// Thráin, Father of Thorin: the [dwarven] follower of that title; an Aid and a trigger on his bearer's win
					put("thrain", "32_9");
					put("thrain2", "32_9");
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, hand weapon; damage +1 at a mountain
					put("emeralds", "31_3");    // Emeralds of Girion: [dwarven] artifact in the support area; fellowship and regroup abilities
					// Cards that are none of the three
					put("bifur", "30_5");       // Bifur, Inarticulate: a [dwarven] follower who is no Thráin
					put("fury", "30_3");        // Battle Fury: a [dwarven] skirmish event
					put("forged", "32_18");     // Iron-Forged Weapon: an [esgaroth] possession a [dwarven] companion may bear
					put("cones", "33_20");      // Burning Fir Cones: a [gandalf] possession in the support area
					put("key", "32_11");        // Thrór's Key: a [dwarven] possession, in the other player's discard pile
					put("card1", "1_177");      // Goblin Patrol Troop: a card with no text, to fill a draw deck
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ThrorsHeirloomsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Thrór's Heirlooms
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Exert a Dwarf companion to play Thráin, a [dwarven] possession or a [dwarven] artifact from your draw deck or discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("heirlooms");

		assertEquals("Thrór's Heirlooms", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsADwarfCompanionToPlayADwarvenPossessionFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirlooms = scn.GetFreepsCard("heirlooms");
		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		var thrain = scn.GetFreepsCard("thrain");
		var axe = scn.GetFreepsCard("axe");
		var emeralds = scn.GetFreepsCard("emeralds");
		var bifur = scn.GetFreepsCard("bifur");
		var fury = scn.GetFreepsCard("fury");
		var forged = scn.GetFreepsCard("forged");
		var cones = scn.GetFreepsCard("cones");
		scn.MoveCompanionsToTable(balin, thorin);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		// Nothing in the discard pile: the draw deck is the one place to play from.
		scn.SetFreepsDrawDeck(thrain, axe, emeralds, bifur, fury, forged, cones, scn.GetFreepsCard("card1"));
		scn.MoveCardsToHand(heirlooms);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, thrain, axe, emeralds, bifur, fury, forged, cones);
		assertInZone(Zone.HAND, heirlooms);
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(heirlooms));
		scn.FreepsPlayCard(heirlooms);
		// Two Dwarf companions may pay; not the Hobbit Ring-bearer, nor a Dwarf ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		scn.FreepsChooseCard(balin);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.FreepsDismissRevealedCards();
		// Thráin, a [dwarven] possession and a [dwarven] artifact; not another [dwarven] follower, a [dwarven]
		// event, or possessions of other cultures.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thrain, axe, emeralds));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bifur, fury, forged, cones));
		// A play from a draw deck is chosen from 0 (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(axe);
		scn.FreepsChooseCard(thorin);

		assertAttachedTo(axe, thorin);
		assertInZone(Zone.DECK, thrain, emeralds, bifur, fury, forged, cones);
		assertInZone(Zone.DISCARD, heirlooms);
		// Shuffled as the card leaves the deck looked through, and again after the play; not the other player's.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void PlaysThrainFromTheDiscardPileWithNoShuffle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirlooms = scn.GetFreepsCard("heirlooms");
		var balin = scn.GetFreepsCard("balin");
		var thrain = scn.GetFreepsCard("thrain");
		var axe = scn.GetFreepsCard("axe");
		var emeralds = scn.GetFreepsCard("emeralds");
		var bifur = scn.GetFreepsCard("bifur");
		var fury = scn.GetFreepsCard("fury");
		var forged = scn.GetFreepsCard("forged");
		var cones = scn.GetFreepsCard("cones");
		var key = scn.GetFreepsCard("key");
		var shadowKey = scn.GetShadowCard("key");
		var thrain2 = scn.GetFreepsCard("thrain2");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SetFreepsDrawDeck(axe, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(thrain, emeralds, bifur, fury, forged, cones);
		// A [dwarven] possession in the other player's discard pile, of a title the player's own piles do not hold.
		scn.MoveCardsToDiscard(shadowKey);
		// The second copy of Thráin is held in hand, out of either pile.
		scn.MoveCardsToHand(heirlooms, key, thrain2);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, axe);
		assertInZone(Zone.DISCARD, thrain, emeralds, bifur, fury, forged, cones, shadowKey);
		assertInZone(Zone.HAND, heirlooms, key, thrain2);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(balin));
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(heirlooms);
		// Balin is the one Dwarf companion: exerted unasked.
		assertEquals(1, scn.GetWoundsOn(balin));
		// The Axe is still in the draw deck, so either pile can be played from.
		assertTrue(scn.FreepsChoiceAvailable("draw deck"));
		assertTrue(scn.FreepsChoiceAvailable("discard pile"));
		scn.FreepsChoose("discard pile");
		assertTrue(scn.FreepsHasCardChoiceAvailable(thrain, emeralds));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bifur, fury, forged, cones, shadowKey));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(thrain);

		assertInZone(Zone.SUPPORT, thrain);
		assertInZone(Zone.DISCARD, emeralds, bifur, fury, forged, cones, shadowKey, heirlooms);
		assertInZone(Zone.DECK, axe);
		// No draw deck was looked through.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNothingToPlayInTheDrawDeckTheDiscardPileIsPlayedFromUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirlooms = scn.GetFreepsCard("heirlooms");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var emeralds = scn.GetFreepsCard("emeralds");
		var bifur = scn.GetFreepsCard("bifur");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SetFreepsDrawDeck(bifur, fury, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(axe, emeralds);
		scn.MoveCardsToHand(heirlooms);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, bifur, fury);
		assertInZone(Zone.DISCARD, axe, emeralds);
		assertInZone(Zone.HAND, heirlooms);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(heirlooms);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, emeralds));
		scn.FreepsChooseCardBPFromSelection(emeralds);

		assertInZone(Zone.SUPPORT, emeralds);
		assertInZone(Zone.DECK, bifur, fury);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNothingToPlayInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirlooms = scn.GetFreepsCard("heirlooms");
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var fury = scn.GetFreepsCard("fury");
		var forged = scn.GetFreepsCard("forged");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		// A [dwarven] follower who is no Thráin and an [esgaroth] possession in the draw deck, a [dwarven] event in
		// the discard pile.
		scn.SetFreepsDrawDeck(bifur, forged, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(fury);
		scn.MoveCardsToHand(heirlooms);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, bifur, forged);
		assertInZone(Zone.DISCARD, fury);
		assertInZone(Zone.HAND, heirlooms);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(heirlooms));
	}

	@Test
	public void CannotBePlayedWhileTheOnlyThrainToPlayIsBarredByTheOneInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirlooms = scn.GetFreepsCard("heirlooms");
		var balin = scn.GetFreepsCard("balin");
		var thrain = scn.GetFreepsCard("thrain");
		var thrain2 = scn.GetFreepsCard("thrain2");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(thrain);

		scn.StartGame();
		scn.SetFreepsDrawDeck(fury, scn.GetFreepsCard("card1"));
		scn.MoveCardsToDiscard(thrain2);
		scn.MoveCardsToHand(heirlooms);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, thrain);
		assertInZone(Zone.DECK, fury);
		assertInZone(Zone.DISCARD, thrain2);
		assertInZone(Zone.HAND, heirlooms);

		scn.SkipToPhase(Phase.REGROUP);
		// Thráin is unique: a second cannot be played while one is in play.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(heirlooms));
	}

	@Test
	public void CannotBePlayedWhileEveryDwarfCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var heirlooms = scn.GetFreepsCard("heirlooms");
		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);
		// Balin, vitality 3, left with one: he cannot exert. Bilbo and Dain can, but are no Dwarf companion.
		scn.AddWoundsToChar(balin, 2);

		scn.StartGame();
		scn.MoveCardsToDiscard(axe);
		scn.MoveCardsToHand(heirlooms);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(balin));
		assertInZone(Zone.DISCARD, axe);
		assertInZone(Zone.HAND, heirlooms);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(heirlooms));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));
	}
}
