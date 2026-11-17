package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInHand;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_06_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dream", "6_20");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; +3 only against a Nazgûl
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion; an archery ability
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally; a fellowship ability used only on purpose
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: [gondor] Man companion, Defender +1 and no other text
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession, no Elf; the minion archery total -1
					put("armor", "1_92");       // Armor: a [gondor] possession, to be revealed
					put("lorienelf", "1_53");   // Lórien Elf: an [elven] companion card, to be revealed
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, to be revealed from the Free Peoples deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MustBeaDreamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Must Be a Dream
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot an Elf to reveal the top card of your draw deck. Heal up to 2 companions whose culture matches the revealed card. You may discard the revealed card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dream");

		assertEquals("Must Be a Dream", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dream = scn.GetFreepsCard("dream");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, cloak);
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToHand(dream);

		scn.StartGame();

		// A Man companion bearing an [elven] possession, and Frodo: no Elf to spot.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInHand(dream);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(dream));
	}

	@Test
	public void ARevealedGondorCardHealsUpToTwoGondorCompanionsAndMayBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dream = scn.GetFreepsCard("dream");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(arwen, aragorn, boromir, pippin);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(pippin, 2);
		scn.MoveCardsToHand(dream);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(armor);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, armor);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(pippin));
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		assertTrue(scn.FreepsPlayAvailable(dream));

		scn.FreepsPlayCard(dream);
		// The top card is shown to both players, and to neither as a choice.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, false));
		scn.DismissRevealedCards();

		// A [gondor] card: the [gondor] companions, a Man and a Hobbit, and not the [elven] Arwen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, pippin);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(arwen));

		assertTrue(scn.FreepsDecisionAvailable("discard"));
		scn.FreepsChooseYes();
		assertInDiscard(armor, dream);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// The Armor and the event itself.
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARevealedElvenCardHealsElfCompanionsAndNotAnElfAllyAndDecliningLeavesItOnTop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dream = scn.GetFreepsCard("dream");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		scn.MoveCompanionsToTable(legolas, arwen, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(celeborn, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToHand(dream);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(lorienelf);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, lorienelf);
		assertSame(lorienelf, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(dream);
		scn.DismissRevealedCards();

		// An [elven] card: the two Elf companions; not the [elven] ally, and not the [gondor] Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, aragorn));
		// Up to 2: one is enough.
		scn.FreepsChooseCards(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsDecisionAvailable("discard"));
		scn.FreepsChooseNo();
		assertSame(lorienelf, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(dream);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingAnElfAllyARevealedShadowCardHealsNobodyAndMayBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dream = scn.GetFreepsCard("dream");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToHand(dream);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(runner);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, runner);
		assertSame(runner, scn.GetFreepsTopOfDeck());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		int deck = scn.GetFreepsDeckCount();
		// The Elf ally is the only Elf.
		assertTrue(scn.FreepsPlayAvailable(dream));

		scn.FreepsPlayCard(dream);
		scn.DismissRevealedCards();

		// No companion is [moria]: nobody is healed, and the discard is still offered.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("discard"));
		scn.FreepsChooseYes();
		assertInDiscard(runner);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheDrawDeckEmptyItIsPlayedForNothingAndNoDiscardIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dream = scn.GetFreepsCard("dream");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AddWoundsToChar(arwen, 1);
		scn.MoveCardsToHand(dream);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(dream));

		scn.FreepsPlayCard(dream);
		// Nothing revealed: no culture to match, and no card to discard.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInDiscard(dream);
		assertFalse(scn.FreepsDecisionAvailable("discard"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
