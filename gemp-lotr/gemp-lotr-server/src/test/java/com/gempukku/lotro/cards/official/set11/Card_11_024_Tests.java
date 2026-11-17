package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("might", "11_24");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion, strength 6; +3 against a Nazgûl only
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an Elf ally; a start-of-turn heal and a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf, strength 8; a maneuver ability only
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					// Cards for the top of the Free Peoples draw deck, read for their printed twilight cost only.
					put("boromir", "1_97");     // Boromir, Son of Denethor: a Free Peoples card of twilight cost 3
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a Free Peoples card of twilight cost 4
					put("fighter", "1_146");    // Uruk Fighter: a Shadow card of twilight cost 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MightoftheElflordsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Might of the Elf-lords
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert an Elf to reveal the top card of your draw deck. If that card's twilight cost is 3 or less, make a minion strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("might");

		assertEquals("Might of the Elf-lords", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACostThreeCardRevealedMakesAMinionInAnotherSkirmishStrengthMinus3ForThisSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(might);

		scn.StartGame();
		// Gandalf lies second, beneath the card to be revealed.
		scn.MoveCardsToTopOfDeck(gandalf);
		scn.MoveCardsToTopOfDeck(boromir);
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage, troop);
		assertEquals(boromir, scn.GetFreepsTopOfDeck());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(arwen);

		var deckCount = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsPlayAvailable(might));

		scn.FreepsPlayCard(might);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(arwen));

		// The top card, and only it, is shown to both players.
		assertTrue(scn.ShadowHasCardChoiceAvailable(boromir, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(gandalf, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(gandalf, false));
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// The Troop, assigned to Aragorn and still waiting for its skirmish, may be chosen as well.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, aragorn, frodo, elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(10, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertInDiscard(might);
		// Revealed, not moved: still the top card, the deck neither smaller nor shuffled.
		assertEquals(boromir, scn.GetFreepsTopOfDeck());
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Arwen 6 against the Savage 5: Arwen wins, and the next skirmish is the Troop's.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACostFourCardRevealedMakesNoMinionWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(might);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(gandalf);
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage, troop);
		assertEquals(gandalf, scn.GetFreepsTopOfDeck());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));

		// Arwen is the only Elf: she pays unasked.
		scn.FreepsPlayCard(might);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, false));
		scn.DismissRevealedCards();

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertInDiscard(might);
		assertEquals(gandalf, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AShadowCardOfCostThreeRevealedCountsToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var arwen = scn.GetFreepsCard("arwen");
		var fighter = scn.GetFreepsCard("fighter");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(might);

		scn.StartGame();
		// The Free Peoples player's own copy of Uruk Fighter, a Shadow card, on top of his own draw deck.
		scn.MoveCardsToTopOfDeck(fighter);
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);
		assertEquals(fighter, scn.GetFreepsTopOfDeck());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.FreepsPlayCard(might);
		assertEquals(1, scn.GetWoundsOn(arwen));
		scn.DismissRevealedCards();

		// The Savage is the only minion: it is chosen unasked.
		assertEquals(2, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheElfExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(might);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(0, scn.GetWoundsOn(arwen));
		// The exertion is a cost: the event is played with nothing to reveal (Q-0163: B).
		assertTrue(scn.FreepsPlayAvailable(might));

		scn.FreepsPlayCard(might);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage));
		assertInDiscard(might);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyElfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var might = scn.GetFreepsCard("might");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(might);
		// Vitality 3 with 2 wounds: Arwen cannot exert.  Aragorn could, but is no Elf.
		scn.AddWoundsToChar(arwen, 2);

		scn.StartGame();
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInHand(might);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInHand(might);
		assertFalse(scn.FreepsPlayAvailable(might));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
