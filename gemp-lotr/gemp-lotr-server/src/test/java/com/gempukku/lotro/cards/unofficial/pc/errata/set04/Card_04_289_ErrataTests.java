package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_289_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("simbelmyne", "54_289");
					put("eored1", "12_113");    // Éored Warrior: [rohan] Man companion, not valiant, cost 2
					put("eored2", "12_113");
					put("eored3", "12_113");
					put("guard", "5_83");       // Household Guard: valiant [rohan] Man companion, cost 2, no other text
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally, cost 1
					put("sword", "4_291");      // Sword of Rohan: [rohan] possession, cost 1, bearer must be a [rohan] Man
					put("arrow", "9_46");       // The Red Arrow: [rohan] artifact, cost 0, support area
					put("supplies", "4_290");   // Supplies of the Mark: [rohan] condition, cost 0
					put("fight", "4_273");      // Fight for the Villagers: [rohan] Skirmish event
					put("coat", "1_101");       // Coat of Mail: [gondor] possession, cost 1, bearer must be a Man
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
				}},
				// Bag End at site 1: no discount on [rohan] possessions, as Edoras Hall would give.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The table of the near misses.  It is apart from the other so that Merry, a [rohan] character, is in no
	// other test's draw deck to be found there.
	protected VirtualTableScenario GetNearMissScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("simbelmyne", "54_289");
					put("eored1", "12_113");    // Éored Warrior: [rohan] Man companion, not valiant, cost 2
					put("guard", "5_83");       // Household Guard: valiant [rohan] Man companion, cost 2, no other text
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally, not valiant, cost 1
					// Aragorn, Last Hope of Men: the printed valiant Man who is not [rohan] ([gondor]); an optional trigger at the
					// start of each turn beside another valiant companion, so he is put down after StartGame()
					put("lasthope", "102_16");
					// Merry, Master Holbytla: the printed valiant [rohan] character who is no Man, a Hobbit companion.  He is a
					// [rohan] Man as well while 3 [rohan] companions or Eowyn can be spotted; his other texts answer an
					// exertion and a skirmish
					put("merry", "103_85");
				}},
				// Bag End at site 1: no discount on [rohan] possessions, as Edoras Hall would give.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SimbelmyneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Simbelmynë
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot 2 [rohan] Men (or 1 valiant [rohan] Man) to play a [rohan] character or [rohan] item from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("simbelmyne");

		assertEquals("Simbelmynë", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoRohanMenToSpotPlaysARohanCompanionFromTheDrawDeckPayingItsCostAndReshufflesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var simbelmyne = scn.GetFreepsCard("simbelmyne");
		var eored1 = scn.GetFreepsCard("eored1");
		var eored2 = scn.GetFreepsCard("eored2");
		var eored3 = scn.GetFreepsCard("eored3");
		var guard = scn.GetFreepsCard("guard");
		var aldor = scn.GetFreepsCard("aldor");
		var sword = scn.GetFreepsCard("sword");
		var arrow = scn.GetFreepsCard("arrow");
		var supplies = scn.GetFreepsCard("supplies");
		var fight = scn.GetFreepsCard("fight");
		var coat = scn.GetFreepsCard("coat");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(simbelmyne);
		scn.MoveCompanionsToTable(eored1, eored2);
		// A [rohan] Man that is not in the draw deck.
		scn.MoveCardsToDiscard(eored3);

		scn.StartGame();

		assertInZone(Zone.DECK, guard, aldor, sword, arrow, supplies, fight, coat, aragorn);
		assertEquals(0, scn.GetTwilight());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(simbelmyne));

		scn.FreepsPlayCard(simbelmyne);
		// Looking through the draw deck shows the whole of it first.
		scn.FreepsDismissRevealedCards();

		// A [rohan] companion, a [rohan] ally, a [rohan] possession and a [rohan] artifact.  Not a [rohan] condition,
		// not a [rohan] event, not a possession or a Man of another culture, and not the [rohan] Man in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, aldor, sword, arrow));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(supplies, fight, coat, aragorn, eored3));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(guard);

		assertEquals(3, scn.GetTwilight()); // 1 for the event + 2 for Household Guard
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertInZone(Zone.DECK, aldor, sword, arrow);
		assertInDiscard(simbelmyne);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// Looked through, so reshuffled, and only that deck.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysARohanPossessionFromTheDrawDeckOnABearerItCanGoOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var simbelmyne = scn.GetFreepsCard("simbelmyne");
		var eored1 = scn.GetFreepsCard("eored1");
		var eored2 = scn.GetFreepsCard("eored2");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(simbelmyne);
		scn.MoveCompanionsToTable(eored1, eored2);

		scn.StartGame();

		assertInZone(Zone.DECK, sword);
		assertEquals(0, scn.GetTwilight());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(simbelmyne);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(sword);

		// Sword of Rohan's own "Bearer must be a [rohan] Man": either Éored Warrior, not Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eored1, eored2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(eored2);

		assertEquals(2, scn.GetTwilight()); // 1 for the event + 1 for Sword of Rohan
		assertAttachedTo(sword, eored2);
		assertInDiscard(simbelmyne);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// The play onto a bearer shuffles the deck as well, so more than once.
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysARohanArtifactFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var simbelmyne = scn.GetFreepsCard("simbelmyne");
		var eored1 = scn.GetFreepsCard("eored1");
		var eored2 = scn.GetFreepsCard("eored2");
		var arrow = scn.GetFreepsCard("arrow");
		scn.MoveCardsToHand(simbelmyne);
		scn.MoveCompanionsToTable(eored1, eored2);

		scn.StartGame();

		assertInZone(Zone.DECK, arrow);
		assertEquals(0, scn.GetTwilight());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(simbelmyne);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(arrow));
		scn.FreepsChooseCardBPFromSelection(arrow);

		assertEquals(1, scn.GetTwilight()); // 1 for the event + 0 for The Red Arrow
		assertInZone(Zone.SUPPORT, arrow);
		assertInDiscard(simbelmyne);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneRohanManWhoIsNotValiantUntilASecondIsPlayedAndThenMayPlayNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNearMissScenario();

		var simbelmyne = scn.GetFreepsCard("simbelmyne");
		var eored1 = scn.GetFreepsCard("eored1");
		var aldor = scn.GetFreepsCard("aldor");
		var guard = scn.GetFreepsCard("guard");
		var lasthope = scn.GetFreepsCard("lasthope");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(simbelmyne, aldor);
		scn.MoveCompanionsToTable(eored1, merry);

		scn.StartGame();
		// Near misses: a valiant Man who is not [rohan], and a valiant [rohan] character who is not a Man.  With
		// two [rohan] companions on the table Merry is a Hobbit only.
		scn.MoveCompanionsToTable(lasthope);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(lasthope, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(merry, Keyword.VALIANT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertFalse(scn.HasKeyword(eored1, Keyword.VALIANT));
		assertTrue(scn.FreepsPlayAvailable(aldor));
		// One [rohan] Man, and he is not valiant.
		assertFalse(scn.FreepsPlayAvailable(simbelmyne));

		// The second [rohan] Man is an ally, so that Merry is still no Man: a third [rohan] companion would make
		// him one, and a valiant one.
		scn.FreepsPlayCard(aldor);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.IsRace(merry, Race.MAN));

		// Two [rohan] Men.
		assertTrue(scn.FreepsPlayAvailable(simbelmyne));
		assertInZone(Zone.DECK, guard);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(simbelmyne);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard));
		// "There is no penalty if you don't find (or choose not to play) a card you are looking for in your draw deck."
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertEquals(2, scn.GetTwilight()); // 1 for Aldor + 1 for the event, and nothing more
		assertInZone(Zone.DECK, guard);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(simbelmyne);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OneValiantRohanManIsEnoughToPlayItAndARohanAllyCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var simbelmyne = scn.GetFreepsCard("simbelmyne");
		var guard = scn.GetFreepsCard("guard");
		var aldor = scn.GetFreepsCard("aldor");
		scn.MoveCardsToHand(simbelmyne);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		assertInZone(Zone.DECK, aldor);
		assertEquals(0, scn.GetTwilight());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(simbelmyne));

		scn.FreepsPlayCard(simbelmyne);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aldor));
		scn.FreepsChooseCardBPFromSelection(aldor);

		assertEquals(2, scn.GetTwilight()); // 1 for the event + 1 for Aldor
		assertInZone(Zone.SUPPORT, aldor);
		assertInDiscard(simbelmyne);
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManWhoIsAnAllyCountsTowardsTheTwoMenToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var simbelmyne = scn.GetFreepsCard("simbelmyne");
		var eored1 = scn.GetFreepsCard("eored1");
		var aldor = scn.GetFreepsCard("aldor");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(simbelmyne, eored1);
		scn.MoveAlliesToTable(aldor);

		scn.StartGame();

		assertFalse(scn.HasKeyword(aldor, Keyword.VALIANT));
		// One [rohan] Man, an ally and not valiant.
		assertFalse(scn.FreepsPlayAvailable(simbelmyne));

		scn.FreepsPlayCard(eored1);
		assertEquals(2, scn.GetTwilight());

		// A companion and an ally: two [rohan] Men.
		assertTrue(scn.FreepsPlayAvailable(simbelmyne));
		scn.FreepsPlayCard(simbelmyne);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard));
		scn.FreepsDeclineChoosing();

		assertEquals(3, scn.GetTwilight()); // 2 for Éored Warrior + 1 for the event
		assertInDiscard(simbelmyne);
		assertInZone(Zone.DECK, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWithNothingInTheDrawDeckToPlayAndTheDeckIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var simbelmyne = scn.GetFreepsCard("simbelmyne");
		var eored1 = scn.GetFreepsCard("eored1");
		var eored2 = scn.GetFreepsCard("eored2");
		var eored3 = scn.GetFreepsCard("eored3");
		var guard = scn.GetFreepsCard("guard");
		var aldor = scn.GetFreepsCard("aldor");
		var sword = scn.GetFreepsCard("sword");
		var arrow = scn.GetFreepsCard("arrow");
		var supplies = scn.GetFreepsCard("supplies");
		var fight = scn.GetFreepsCard("fight");
		var coat = scn.GetFreepsCard("coat");
		scn.MoveCardsToHand(simbelmyne);
		scn.MoveCompanionsToTable(eored1, eored2);
		// Left in the draw deck: the condition, the Skirmish event, Coat of Mail, Aragorn, Boromir and Legolas.
		scn.MoveCardsToDiscard(eored3, guard, aldor, sword, arrow);

		scn.StartGame();

		assertInZone(Zone.DECK, supplies, fight, coat);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(simbelmyne));

		scn.FreepsPlayCard(simbelmyne);
		// The deck is still looked through, and there is nothing in it to choose.
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetTwilight()); // the event only
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(simbelmyne);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
