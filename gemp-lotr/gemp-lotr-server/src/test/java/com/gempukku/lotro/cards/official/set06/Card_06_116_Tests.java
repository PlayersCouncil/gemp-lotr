package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_116_Tests
{

	// The Towers path, Westfold its site 1, where the game starts.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: a [gondor] Aragorn, vitality 4, Gandalf signet; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Aragorn, Defender of Rohan: a [rohan] Aragorn, vitality 4, no signet; a keyword for others while the
					// Ring-bearer is assigned
					put("defender", "17_93");

					// Companions with the Aragorn signet, of three cultures
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven]; strength +3 against a Nazgûl only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor]; text only about overwhelming
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire]; strength +2 while he bears a weapon
					// Companions with another signet or none
					put("gimli", "1_13");       // Gimli, Son of Gloin: Gandalf signet; a skirmish ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: Frodo signet; an archery ability used only on purpose
					put("guard", "5_83");       // Household Guard: no signet; valiant is its whole text
				}},
				new HashMap<>() {{
					put("site1", "6_116");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WestfoldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Westfold
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Exert Aragorn twice to play a companion with the Aragorn signet from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Westfold", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionExertsAragornTwiceToPlayACompanionWithTheAragornSignetFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var westfold = scn.GetFreepsSite(1);
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertSame(westfold, scn.GetCurrentSite());
		assertInZone(Zone.DECK, arwen, boromir, merry, gimli, legolas, guard);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(westfold));

		scn.FreepsUseCardAction(westfold);
		// Aragorn exerts twice; nobody else does.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The deck is shown first, and then only the companions with the Aragorn signet can be taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, false));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard));
		// A play from a draw deck may always find nothing (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(merry);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInZone(Zone.DECK, arwen, boromir, gimli, legolas, guard);
		// One shuffle as the card leaves the deck that was looked through, and the card's own after the play.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRohanAragornMayPayTheExertions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var westfold = scn.GetFreepsSite(1);
		var defender = scn.GetFreepsCard("defender");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(defender);

		scn.StartGame();

		assertInZone(Zone.DECK, arwen, boromir);
		assertEquals(0, scn.GetWoundsOn(defender));
		assertTrue(scn.FreepsActionAvailable(westfold));

		scn.FreepsUseCardAction(westfold);
		assertEquals(2, scn.GetWoundsOn(defender));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, boromir));
		scn.FreepsChooseCardBPFromSelection(arwen);

		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertInZone(Zone.DECK, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAragornCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var westfold = scn.GetFreepsSite(1);
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		// Vitality 4 with 2 wounds: Aragorn can exert once, not twice.  Boromir, no Aragorn, could exert twice.
		scn.AddWoundsToChar(aragorn, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsExhausted(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInZone(Zone.DECK, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(westfold));
	}

	@Test
	public void WithNoCompanionWithTheAragornSignetInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var westfold = scn.GetFreepsSite(1);
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToDiscard(arwen, boromir, merry);

		assertInZone(Zone.DISCARD, arwen, boromir, merry);
		assertInZone(Zone.DECK, gimli);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(westfold));

		scn.FreepsUseCardAction(westfold);
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The deck is shown with nothing in it that can be taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DISCARD, arwen, boromir, merry);
		assertInZone(Zone.DECK, gimli);
		// The card's own shuffle, and no other.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
