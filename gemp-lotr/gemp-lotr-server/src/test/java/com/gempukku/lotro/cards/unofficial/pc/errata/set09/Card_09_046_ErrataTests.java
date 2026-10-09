package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_09_046_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrow", "59_46");
					put("guard", "5_83");       // Household Guard: [rohan] Man, vitality 3; Valiant is its whole text
					put("guard2", "5_83");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man; a skirmish ability only while mounted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man; Knight is its whole text
					put("albert", "1_69");      // Albert Dreary: [gandalf] Man, an ally, vitality 3; a maneuver ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man, an ally, vitality 1 (he can never exert)
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("dwarf", "1_7");        // Dwarf Guard: [dwarven] Dwarf, no text in play
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man minion, vitality 2; its text does nothing below 2 burdens
					put("shards", "3_44");      // The Shards of Narsil: an artifact in the support area; fellowship abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheRedArrowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: The Red Arrow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: Fellowship: Discard this and exert a Man to take a Free Peoples Man of a different culture into hand from your draw deck.
		 * 	Regroup: Exert 2 of your Men to discard a Man from your dead pile. Remove this artifact from the game.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arrow");

		assertEquals("The Red Arrow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheRedArrowIsPlayedFromHandForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		scn.MoveCardsToHand(arrow);

		scn.StartGame();
		// Nobody but Frodo is in play: the card asks for nothing to be spotted.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(arrow));

		scn.FreepsPlayCard(arrow);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, arrow);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsTheArrowAndExertsAManToTakeAManOfAnotherCultureIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var arrow = scn.GetFreepsCard("arrow");
		var shards = scn.GetFreepsCard("shards");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var albert = scn.GetFreepsCard("albert");
		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var rider = scn.GetFreepsCard("rider");
		var dwarf = scn.GetFreepsCard("dwarf");
		var axeman = scn.GetFreepsCard("axeman");
		scn.MoveCompanionsToTable(guard, guard2, aragorn, pippin);
		scn.MoveAlliesToTable(albert);
		scn.MoveCardsToSupportArea(arrow, shards);

		scn.StartGame();
		// Vitality 3 with 2 wounds: a Man who cannot exert.
		scn.AddWoundsToChar(guard2, 2);

		assertInZone(Zone.DECK, knight, merchant, rider, dwarf, axeman);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(arrow));

		scn.MarkGameLog();
		scn.FreepsUseCardAction(arrow);

		// One Man, of any culture, companion or ally; not a Hobbit, though he is [gondor], and not a Man who
		// is exhausted.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, aragorn, albert));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard2, pippin, frodo));
		scn.FreepsChooseCard(guard);

		assertInZone(Zone.DISCARD, arrow);
		assertInZone(Zone.SUPPORT, shards);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(albert));

		scn.FreepsDismissRevealedCards();
		// The Household Guard is [rohan]: a [gondor] companion and a [gondor] ally who are Men may be taken.
		// Not a [rohan] Man, a Dwarf, or a [raider] Man who is a Shadow card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rider, dwarf, axeman));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(knight);

		assertInZone(Zone.HAND, knight);
		assertInZone(Zone.DECK, merchant, rider, dwarf, axeman);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// The deck has been looked through, so it is shuffled; the other player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		// Taken by what it is and not revealed: the game log names it, and not a card left in the deck.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(knight));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(merchant));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheManTakenMustDifferInCultureFromTheManExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var albert = scn.GetFreepsCard("albert");
		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var rider = scn.GetFreepsCard("rider");
		var dwarf = scn.GetFreepsCard("dwarf");
		var axeman = scn.GetFreepsCard("axeman");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveCardsToSupportArea(arrow);

		scn.StartGame();
		assertInZone(Zone.DECK, rider, albert, knight, merchant, pippin, dwarf, axeman);

		scn.FreepsUseCardAction(arrow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsDismissRevealedCards();
		// Aragorn is [gondor]: now a [rohan] Man and a [gandalf] Man may be taken, and the two [gondor] Men
		// may not, though the Arrow itself is [rohan].  Nor a [gondor] Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, albert));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, merchant, pippin, dwarf, axeman));
		scn.FreepsChooseCardBPFromSelection(albert);

		assertInZone(Zone.HAND, albert);
		assertInZone(Zone.DECK, rider, knight, merchant);
		assertInZone(Zone.DISCARD, arrow);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakingNothingStillDiscardsTheArrowExertsTheManAndShufflesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(arrow);

		scn.StartGame();
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(guard));

		// The Household Guard is the only Man, and is exerted without a choice.
		scn.FreepsUseCardAction(arrow);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DISCARD, arrow);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.DECK, knight);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithoutAManAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		// A [gondor] Hobbit who could exert, and a Man whose 1 vitality lets him exert never.
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(arrow);
		scn.MoveCardsToHand(guard);

		scn.StartGame();
		assertFalse(scn.FreepsActionAvailable(arrow));

		scn.FreepsPlayCard(guard);
		assertTrue(scn.FreepsActionAvailable(arrow));
		assertInZone(Zone.SUPPORT, arrow);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionExertsTwoOfYourMenToDiscardAManFromYourDeadPileAndRemovesTheArrowFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var arrow = scn.GetFreepsCard("arrow");
		var shards = scn.GetFreepsCard("shards");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var albert = scn.GetFreepsCard("albert");
		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var dwarf = scn.GetFreepsCard("dwarf");
		var theirRider = scn.GetShadowCard("rider");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCompanionsToTable(guard, guard2, aragorn, pippin);
		scn.MoveAlliesToTable(albert);
		scn.MoveCardsToSupportArea(arrow, shards);
		// A Man who was a companion, a Man who was an ally, and a Dwarf; and a Man in the other player's
		// dead pile.
		scn.MoveCardsToDeadPile(knight, merchant, dwarf, theirRider);

		scn.StartGame();
		// Vitality 3 with 2 wounds: a Man who cannot exert.
		scn.AddWoundsToChar(guard2, 2);

		// In the fellowship phase the card offers its other ability only (see the tests of that one).
		scn.SkipToPhase(Phase.REGROUP);
		// A Man who is the other player's, and could exert (vitality 2).
		scn.MoveMinionsToTable(axeman);
		assertTrue(scn.FreepsActionAvailable(arrow));

		scn.FreepsUseCardAction(arrow);
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, aragorn, albert));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard2, pippin, frodo, axeman));
		scn.FreepsChooseCards(guard, albert);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(albert));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwarf, theirRider));
		scn.FreepsChooseCard(merchant);

		assertInZone(Zone.DISCARD, merchant);
		assertInZone(Zone.DEAD, knight, dwarf, theirRider);
		// Removed from the game, not discarded; the other artifact stays.
		assertInZone(Zone.REMOVED, arrow);
		assertInZone(Zone.SUPPORT, shards);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoManInTheDeadPileTheTwoMenAreStillExertedAndTheArrowIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf = scn.GetFreepsCard("dwarf");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveCardsToSupportArea(arrow);
		scn.MoveCardsToDeadPile(dwarf);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(arrow));

		// The only two Men: they are exerted without a choice.
		scn.FreepsUseCardAction(arrow);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DEAD, dwarf);
		assertInZone(Zone.REMOVED, arrow);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithOnlyOneManAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var knight = scn.GetFreepsCard("knight");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCompanionsToTable(guard, guard2, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(arrow);
		scn.MoveCardsToDeadPile(knight);

		scn.StartGame();
		// Vitality 3 with 2 wounds: a Man who cannot exert.
		scn.AddWoundsToChar(guard2, 2);

		scn.SkipToPhase(Phase.REGROUP);
		// A Man able to exert (vitality 2) who is the other player's.
		scn.MoveMinionsToTable(axeman);
		scn.FreepsRefreshDecision();

		// One Household Guard can exert; the other, the Merchant (vitality 1), a Hobbit and the other
		// player's Man do not make a second.
		assertFalse(scn.FreepsActionAvailable(arrow));

		// With one wound fewer the second Household Guard can exert, and that is all it wanted.
		scn.RemoveWoundsFromChar(guard2, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(arrow));
		assertInZone(Zone.SUPPORT, arrow);
		assertInZone(Zone.DEAD, knight);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
