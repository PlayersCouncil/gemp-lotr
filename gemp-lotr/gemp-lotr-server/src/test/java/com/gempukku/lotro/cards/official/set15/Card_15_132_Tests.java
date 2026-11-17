package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_132_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("days", "15_132");
					put("eored1", "12_113");    // Éored Warrior: [rohan] Man companion, no hunter; strength +3 at a battleground or plains only
					put("eored2", "12_113");
					put("eored3", "12_113");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant, no other text
					// Merry, Master Holbytla: a [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn
					put("merry", "103_85");
					put("worker", "15_135");    // Rohan Worker: [rohan] follower, no requirement to play; text only when transferred or when its bearer wins
					put("eomer", "15_123");     // Éomer, Horsemaster: unique [rohan] Man companion, hunter 2; a skirmish ability used only on purpose
					put("eomer2", "15_123");    // a second copy, left in the draw deck while the first is in play
					put("gamling", "15_126");   // Gamling, The Old: unique [rohan] Man companion, hunter 1; strength +3 beside 3 hunters
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ALLY, no hunter
					put("uruviel", "15_26");    // Uruviel, Woodland Maid: an [elven] follower
					put("sword", "4_291");      // Sword of Rohan: [rohan] possession, bearer must be a [rohan] Man
					put("arrow", "9_46");       // The Red Arrow: [rohan] artifact, support area
					put("supplies", "4_290");   // Supplies of the Mark: [rohan] condition, support area
					put("fight", "4_273");      // Fight for the Villagers: [rohan] Skirmish event
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
				}},
				// Bag End at site 1: no discount on [rohan] possessions, as Edoras Hall would give.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The table of the near misses, apart from the other so that its cards are in no other test's draw deck.
	protected VirtualTableScenario GetNearMissScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("days", "15_132");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant and no hunter
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally, no hunter
					// Faramir, Captain of Ithilien: a hunter Man who is not [rohan] ([gondor]); a Maneuver heal used only on purpose
					put("faramir", "18_48");
					// Merry, Master Holbytla: a [rohan] character who is no Man, with two [rohan] companions and no Eowyn
					put("merry", "103_85");
					put("eored", "12_113");     // Éored Warrior: [rohan] Man companion, left in the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LastDaysofMyHouseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Last Days of My House
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot 2 [rohan] Men (or 1 hunter [rohan] Man) to play a [rohan] companion or a [rohan] follower from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("days");

		assertEquals("Last Days of My House", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoRohanMenToSpotPlaysARohanCompanionFromTheDrawDeckAndReshufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var days = scn.GetFreepsCard("days");
		var eored1 = scn.GetFreepsCard("eored1");
		var eored2 = scn.GetFreepsCard("eored2");
		var eored3 = scn.GetFreepsCard("eored3");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var worker = scn.GetFreepsCard("worker");
		var aldor = scn.GetFreepsCard("aldor");
		var uruviel = scn.GetFreepsCard("uruviel");
		var sword = scn.GetFreepsCard("sword");
		var arrow = scn.GetFreepsCard("arrow");
		var supplies = scn.GetFreepsCard("supplies");
		var fight = scn.GetFreepsCard("fight");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(days);
		scn.MoveCompanionsToTable(eored1, eored2);
		// A [rohan] companion that is not in the draw deck.
		scn.MoveCardsToDiscard(eored3);

		scn.StartGame();

		assertFalse(scn.HasKeyword(eored1, Keyword.HUNTER));
		assertInZone(Zone.DECK, guard, merry, worker, aldor, uruviel, sword, arrow, supplies, fight, aragorn);
		assertInZone(Zone.DISCARD, eored3);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(days));

		scn.FreepsPlayCard(days);
		// Looking through the draw deck shows the whole of it first.
		scn.FreepsDismissRevealedCards();

		// [rohan] companions of two races and a [rohan] follower.  Not a [rohan] ally, possession, artifact, condition
		// or event, not a companion or a follower of another culture, and not the [rohan] companion in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry, worker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aldor, sword, arrow, supplies, fight, aragorn, uruviel, eored3));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertInZone(Zone.DECK, merry, worker);
		assertInDiscard(days);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// Looked through, so reshuffled, and only that deck.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OneHunterRohanManIsEnoughAndARohanFollowerIsPlayedToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var days = scn.GetFreepsCard("days");
		var eomer = scn.GetFreepsCard("eomer");
		var eomer2 = scn.GetFreepsCard("eomer2");
		var worker = scn.GetFreepsCard("worker");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(days);
		scn.MoveCompanionsToTable(eomer);

		scn.StartGame();

		assertTrue(scn.HasKeyword(eomer, Keyword.HUNTER));
		assertInZone(Zone.DECK, worker, guard, eomer2);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// One [rohan] Man, and he is a hunter.
		assertTrue(scn.FreepsPlayAvailable(days));

		scn.FreepsPlayCard(days);
		scn.FreepsDismissRevealedCards();
		// The second Éomer is a [rohan] companion, but his title is in play: the card played from the deck still has
		// to be playable.
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eomer2));
		scn.FreepsChooseCardBPFromSelection(worker);

		assertInZone(Zone.SUPPORT, worker);
		assertInZone(Zone.DECK, guard, eomer2);
		assertInDiscard(days);
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneRohanManWhoIsNoHunterUntilASecondIsPlayedAndThenMayPlayNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNearMissScenario();

		var days = scn.GetFreepsCard("days");
		var guard = scn.GetFreepsCard("guard");
		var aldor = scn.GetFreepsCard("aldor");
		var faramir = scn.GetFreepsCard("faramir");
		var merry = scn.GetFreepsCard("merry");
		var eored = scn.GetFreepsCard("eored");
		scn.MoveCardsToHand(days, aldor);
		scn.MoveCompanionsToTable(guard, faramir, merry);

		scn.StartGame();

		// Near misses: a hunter Man who is not [rohan], a [rohan] character who is no Man, and the one [rohan] Man
		// valiant but no hunter.
		assertTrue(scn.HasKeyword(faramir, Keyword.HUNTER));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(guard, Keyword.HUNTER));
		assertTrue(scn.FreepsPlayAvailable(aldor));
		assertFalse(scn.FreepsPlayAvailable(days));

		// The second [rohan] Man is an ally, so that Merry is still no Man.
		scn.FreepsPlayCard(aldor);
		assertInZone(Zone.SUPPORT, aldor);
		assertFalse(scn.IsRace(merry, Race.MAN));

		// Two [rohan] Men, a companion and an ally.
		assertTrue(scn.FreepsPlayAvailable(days));
		assertInZone(Zone.DECK, eored);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(days);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(eored));
		// "There is no penalty if you don't find (or choose not to play) a card you are looking for in your draw deck."
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, eored);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(days);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWithNothingInTheDrawDeckToPlayAndTheDeckIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var days = scn.GetFreepsCard("days");
		var gamling = scn.GetFreepsCard("gamling");
		var eored1 = scn.GetFreepsCard("eored1");
		var eored2 = scn.GetFreepsCard("eored2");
		var eored3 = scn.GetFreepsCard("eored3");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var worker = scn.GetFreepsCard("worker");
		var eomer = scn.GetFreepsCard("eomer");
		var eomer2 = scn.GetFreepsCard("eomer2");
		var aldor = scn.GetFreepsCard("aldor");
		var uruviel = scn.GetFreepsCard("uruviel");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(days);
		// Gamling, a hunter [rohan] Man of another title than Éomer, alone.
		scn.MoveCompanionsToTable(gamling);
		// Left in the draw deck: the ally, the possession, the artifact, the condition, the event, Aragorn and Uruviel.
		scn.MoveCardsToDiscard(eored1, eored2, eored3, guard, merry, worker, eomer, eomer2);

		scn.StartGame();

		assertTrue(scn.HasKeyword(gamling, Keyword.HUNTER));
		assertInZone(Zone.DECK, aldor, uruviel, aragorn);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(days));

		scn.FreepsPlayCard(days);
		// The deck is still looked through, and there is nothing in it to choose.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aldor, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruviel, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, aldor, uruviel, aragorn);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(days);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
