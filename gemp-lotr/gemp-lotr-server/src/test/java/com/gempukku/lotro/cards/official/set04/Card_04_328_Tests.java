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

public class Card_04_328_Tests
{

	// The Towers path, The Riddermark its site 1, where the game starts.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "4_316");        // Sam, Samwise the Brave: a Ring-bound companion; two responses used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, no companion; an ability used only on purpose
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an unbound [elven] Elf companion; text only against a Nazgul
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound [dwarven] Dwarf companion; an ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: an unbound [gandalf] Wizard companion; a strength bonus only
					put("heir", "4_109");       // Aragorn, Heir of Elendil: a [gondor] Aragorn; defender +1 only
					put("defender", "17_93");   // Aragorn, Defender of Rohan: a [rohan] Aragorn; a keyword for others while the Ring-bearer is assigned
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a [gondor] companion who is no Aragorn; text only about overwhelming
				}},
				new HashMap<>() {{
					put("site1", "4_328");
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
	public void TheRiddermarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: The Riddermark
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Plains</b>. <b>Fellowship:</b> Exert 2 unbound companions to play Aragorn from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("The Riddermark", card.getBlueprint().getTitle());
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
	public void FellowshipActionExertsTwoUnboundCompanionsToPlayAnAragornFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddermark = scn.GetFreepsSite(1);
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var heir = scn.GetFreepsCard("heir");
		var defender = scn.GetFreepsCard("defender");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(sam, legolas, gimli, gandalf);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertSame(riddermark, scn.GetCurrentSite());
		assertInZone(Zone.DECK, heir, defender, boromir);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(riddermark));

		scn.FreepsUseCardAction(riddermark);
		// Two unbound companions: the Ring-bearer and Sam are Ring-bound, and an ally is no companion.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, gimli, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, grimir));
		scn.FreepsChooseCards(legolas, gimli);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// The deck is shown first, and then only the Aragorns of either culture can be taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(heir, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(defender, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, false));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(heir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(defender));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		scn.FreepsChooseCardBPFromSelection(defender);

		assertInZone(Zone.FREE_CHARACTERS, defender);
		assertInZone(Zone.DECK, heir, boromir);
		// One shuffle as the card leaves the deck that was looked through, and the card's own after the play.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilTwoUnboundCompanionsCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddermark = scn.GetFreepsSite(1);
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var heir = scn.GetFreepsCard("heir");
		scn.MoveCompanionsToTable(sam, legolas, gimli);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveCardsToHand(gandalf);
		// Vitality 3 with 2 wounds: Legolas is exhausted, and Gimli is the one unbound companion who can exert.
		scn.AddWoundsToChar(legolas, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.IsExhausted(legolas));
		assertFalse(scn.IsExhausted(gimli));
		assertFalse(scn.IsExhausted(frodo));
		assertFalse(scn.IsExhausted(sam));
		assertFalse(scn.IsExhausted(grimir));
		assertInZone(Zone.DECK, heir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(riddermark));

		scn.FreepsPlayCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(riddermark));
	}

	@Test
	public void WithNoAragornInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riddermark = scn.GetFreepsSite(1);
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var heir = scn.GetFreepsCard("heir");
		var defender = scn.GetFreepsCard("defender");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(legolas, gimli);

		scn.StartGame();
		scn.MoveCardsToDiscard(heir, defender);

		assertInZone(Zone.DISCARD, heir, defender);
		assertInZone(Zone.DECK, boromir);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(riddermark));

		// The two unbound companions are the only two who can pay.
		scn.FreepsUseCardAction(riddermark);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The deck is shown with nothing in it that can be taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DISCARD, heir, defender);
		assertInZone(Zone.DECK, boromir);
		// The card's own shuffle, and no other.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
