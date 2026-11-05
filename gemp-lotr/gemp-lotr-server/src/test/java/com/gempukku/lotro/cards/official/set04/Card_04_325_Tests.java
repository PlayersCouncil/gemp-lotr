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

public class Card_04_325_Tests
{

	// The Towers path, Eastemnet Gullies its site 1, where the game starts.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "4_316");        // Sam, Samwise the Brave: a Ring-bound companion; two responses used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, no companion; an ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man companion; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound [dwarven] Dwarf companion; an ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: an unbound [gandalf] Wizard companion; a strength bonus only
					put("greenleaf", "1_50");   // Legolas, Greenleaf: an [elven] Legolas; an archery ability used only on purpose
					put("hunter", "4_73");      // Legolas, Dauntless Hunter: a second Legolas; a cost modifier on Shadow events and conditions
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] Elf companion who is no Legolas; text only against a Nazgul
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EastemnetGulliesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Eastemnet Gullies
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Exert 2 unbound companions to play Legolas from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Eastemnet Gullies", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionExertsTwoUnboundCompanionsToPlayALegolasFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gullies = scn.GetFreepsSite(1);
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var hunter = scn.GetFreepsCard("hunter");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, gandalf);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertSame(gullies, scn.GetCurrentSite());
		assertInZone(Zone.DECK, greenleaf, hunter, arwen);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(gullies));

		scn.FreepsUseCardAction(gullies);
		// Two unbound companions: the Ring-bearer and Sam are Ring-bound, and an ally is no companion.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, grimir));
		scn.FreepsChooseCards(aragorn, gimli);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// The deck is shown first, and then only the Legolases can be taken, not another Elf companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(greenleaf, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(hunter, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, false));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(greenleaf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(hunter));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen));
		scn.FreepsChooseCardBPFromSelection(hunter);

		assertInZone(Zone.FREE_CHARACTERS, hunter);
		assertInZone(Zone.DECK, greenleaf, arwen);
		// One shuffle as the card leaves the deck that was looked through, and the card's own after the play.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilTwoUnboundCompanionsCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gullies = scn.GetFreepsSite(1);
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveCardsToHand(gandalf);
		// Vitality 3 with 2 wounds: Gimli is exhausted, and Aragorn is the one unbound companion who can exert.
		scn.AddWoundsToChar(gimli, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsExhausted(gimli));
		assertFalse(scn.IsExhausted(aragorn));
		assertFalse(scn.IsExhausted(frodo));
		assertFalse(scn.IsExhausted(sam));
		assertFalse(scn.IsExhausted(grimir));
		assertInZone(Zone.DECK, greenleaf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gullies));

		scn.FreepsPlayCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(gullies));
	}

	@Test
	public void WithNoLegolasInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gullies = scn.GetFreepsSite(1);
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var hunter = scn.GetFreepsCard("hunter");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToDiscard(greenleaf, hunter);

		assertInZone(Zone.DISCARD, greenleaf, hunter);
		assertInZone(Zone.DECK, arwen);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(gullies));

		// The two unbound companions are the only two who can pay.
		scn.FreepsUseCardAction(gullies);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The deck is shown with nothing in it that can be taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DISCARD, greenleaf, hunter);
		assertInZone(Zone.DECK, arwen);
		// The card's own shuffle, and no other.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
