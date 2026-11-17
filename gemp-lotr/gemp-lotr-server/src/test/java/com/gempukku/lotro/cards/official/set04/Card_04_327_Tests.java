package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_327_Tests
{

	// The Towers path, Plains of Rohan its site 1, where the game starts.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "4_316");        // Sam, Samwise the Brave: a Ring-bound companion; two responses used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, no companion; an ability used only on purpose
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an unbound [elven] Elf companion; text only against a Nazgul
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: an unbound [gondor] Man companion; defender +1 only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: an unbound [gandalf] Wizard companion; a strength bonus only
					put("sonofgloin", "1_13");  // Gimli, Son of Gloin: a Gimli; damage +1 and a skirmish ability used only on purpose
					put("lockbearer", "4_48");  // Gimli, Lockbearer: another Gimli; damage +1 and a strength bonus only
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] Dwarf companion who is no Gimli; "To play, spot a Dwarf" (Grimir is one)
				}},
				new HashMap<>() {{
					put("site1", "4_327");
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
	public void PlainsofRohanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Plains of Rohan
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Plains</b>. <b>Fellowship:</b> Exert 2 unbound companions to play Gimli from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Plains of Rohan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void FellowshipActionExertsTwoUnboundCompanionsToPlayAGimliFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plains = scn.GetFreepsSite(1);
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sonofgloin = scn.GetFreepsCard("sonofgloin");
		var lockbearer = scn.GetFreepsCard("lockbearer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(sam, legolas, aragorn, gandalf);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertSame(plains, scn.GetCurrentSite());
		assertInZone(Zone.DECK, sonofgloin, lockbearer, guard);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(plains));

		scn.FreepsUseCardAction(plains);
		// Two unbound companions: the Ring-bearer and Sam are Ring-bound, and an ally is no companion.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, aragorn, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, grimir));
		scn.FreepsChooseCards(legolas, aragorn);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// The deck is shown first, and then only the Gimlis can be taken: the Dwarf Guard could be played (Grimir is
		// a Dwarf to spot), and is no Gimli.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sonofgloin, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(lockbearer, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, false));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sonofgloin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(lockbearer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard));
		scn.FreepsChooseCardBPFromSelection(lockbearer);

		assertInZone(Zone.FREE_CHARACTERS, lockbearer);
		assertInZone(Zone.DECK, sonofgloin, guard);
		// One shuffle as the card leaves the deck that was looked through, and the card's own after the play.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilTwoUnboundCompanionsCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plains = scn.GetFreepsSite(1);
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sonofgloin = scn.GetFreepsCard("sonofgloin");
		scn.MoveCompanionsToTable(sam, legolas, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveCardsToHand(gandalf);
		// Vitality 3 with 2 wounds: Legolas is exhausted, and Aragorn is the one unbound companion who can exert.
		scn.AddWoundsToChar(legolas, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.IsExhausted(legolas));
		assertFalse(scn.IsExhausted(aragorn));
		assertFalse(scn.IsExhausted(frodo));
		assertFalse(scn.IsExhausted(sam));
		assertFalse(scn.IsExhausted(grimir));
		assertInZone(Zone.DECK, sonofgloin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(plains));

		scn.FreepsPlayCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(plains));
	}

	@Test
	public void WithNoGimliInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plains = scn.GetFreepsSite(1);
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sonofgloin = scn.GetFreepsCard("sonofgloin");
		var lockbearer = scn.GetFreepsCard("lockbearer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();
		scn.MoveCardsToDiscard(sonofgloin, lockbearer);

		assertInZone(Zone.DISCARD, sonofgloin, lockbearer);
		assertInZone(Zone.DECK, guard);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(plains));

		// The two unbound companions are the only two who can pay.
		scn.FreepsUseCardAction(plains);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The deck is shown with nothing in it that can be taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DISCARD, sonofgloin, lockbearer);
		assertInZone(Zone.DECK, guard);
		// The card's own shuffle, and no other.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
