package com.gempukku.lotro.cards.unofficial.pc.errata.set06;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_016_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forearmed", "56_16");
					put("elf1", "1_53");       // Lorien Elf: Elf companion, strength 4, vitality 2, cost 1
					put("elf2", "1_53");
					put("haldir", "1_48");     // Haldir: Elf companion, strength 5, vitality 3; +2 only at sites 6 to 8
					put("silinde", "1_60");    // Silinde: Elf ally, home site 3, strength 5; has site 3's text while it can be spotted
					put("knight", "5_35");     // Gondorian Knight: Man companion, Knight, cost 2
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard", "1_7");       // Dwarf Guard: cost 1, here only as a card on top of the deck
					put("armor", "1_8");       // Dwarven Armor: cost 0, here only as a card on top of the deck
					put("fearing", "2_100");   // Fearing the Worst: the twilight cost of each tracker is +2
					put("assassin", "1_262");  // Orc Assassin: a tracker printed at cost 2, here only on top of the deck

					put("savage", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("footman", "11_82");   // Footman of Dunland: [men] Man, strength 7, vitality 3, cost 3; the site gains plains
					put("troop", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text
					// Goblin Scrabbler: [moria] Orc, strength 4, vitality 1.  Its texts are about the playing of
					// minions (its own, and the roaming penalty of [moria] Orcs); it is cheated in and none is played.
					put("scrabbler", "2_64");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForearmedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Forearmed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Reveal the top card of your draw deck to make a minion skirmishing an Elf strength –X, where X is the twilight cost of the revealed card.  If that minion is now 4 strength or less, discard the revealed card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forearmed");

		assertEquals("Forearmed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RevealsTheTopCardToMakeAMinionSkirmishingAnElfWeakerAndDiscardsTheCardAtFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forearmed = scn.GetFreepsCard("forearmed");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(elf1, elf2);
		scn.MoveCardsToHand(forearmed);

		var savage = scn.GetShadowCard("savage");
		var footman = scn.GetShadowCard("footman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, footman, troop);
		scn.MoveCardsToTopOfDeck(guard);

		// Not in the assignment phase, the last in which the Free Peoples player acts before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(forearmed));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elf1, savage}, new PhysicalCardImpl[]{elf2, troop});
		scn.ShadowAssignToMinions(elf1, footman);
		scn.FreepsResolveSkirmish(elf1);

		int twilight = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.FreepsPlayAvailable(forearmed));
		scn.FreepsPlayCard(forearmed);

		// The top card is shown to both players.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, false));
		scn.DismissRevealedCards();

		// Both minions in the Elf's skirmish; not the one assigned to the other Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(twilight + 1, scn.GetTwilight());
		assertInDiscard(forearmed);
		// Dwarf Guard costs 1: 5 - 1 = 4, so the revealed card is discarded.
		assertEquals(4, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(13, scn.GetStrength(troop));
		assertInDiscard(guard);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARevealedCardCostingNothingLeavesTheMinionAtFiveAndStaysOnTopOfTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forearmed = scn.GetFreepsCard("forearmed");
		var elf1 = scn.GetFreepsCard("elf1");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(elf1);
		scn.MoveCardsToHand(forearmed);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToTopOfDeck(armor);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(elf1, savage);

		int deck = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(forearmed);
		scn.DismissRevealedCards();

		// Dwarven Armor costs 0: the Savage stays at 5, above 4, and the card stays where it was.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(armor, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(forearmed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThePenaltyLastsForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forearmed = scn.GetFreepsCard("forearmed");
		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(haldir, aragorn);
		scn.MoveCardsToHand(forearmed);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.MoveCardsToTopOfDeck(knight);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haldir, savage}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(haldir);

		scn.FreepsPlayCard(forearmed);
		scn.DismissRevealedCards();

		// Gondorian Knight costs 2: 5 - 2 = 3, so the revealed card is discarded.
		assertEquals(3, scn.GetStrength(savage));
		assertInDiscard(knight);
		scn.ShadowPass();
		scn.FreepsPass();

		// Haldir 5 beats 3 without overwhelming it; the Savage survives the wound.
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingAnElfAllyCanBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forearmed = scn.GetFreepsCard("forearmed");
		var silinde = scn.GetFreepsCard("silinde");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(forearmed);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToTopOfDeck(guard);

		// The fellowship moves to site 3, Silinde's home, where she can skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(silinde, savage);

		scn.FreepsPlayCard(forearmed);
		scn.DismissRevealedCards();

		assertEquals(4, scn.GetStrength(savage));
		assertInDiscard(guard);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoMinionMadeWeakerTheRevealedCardIsNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forearmed = scn.GetFreepsCard("forearmed");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(forearmed);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToTopOfDeck(guard);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(knight, savage);

		int deck = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(forearmed);
		scn.DismissRevealedCards();

		// The Knight is no Elf: no minion was made weaker, so there is no "that minion" at 4 or less.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(forearmed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnotherMinionOfTheSkirmishAtFourDoesNotDiscardTheRevealedCardWhileThatMinionIsAtFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forearmed = scn.GetFreepsCard("forearmed");
		var elf1 = scn.GetFreepsCard("elf1");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(elf1);
		scn.MoveCardsToHand(forearmed);

		var footman = scn.GetShadowCard("footman");
		var scrabbler = scn.GetShadowCard("scrabbler");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, scrabbler);
		scn.MoveCardsToTopOfDeck(knight);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf1, footman);
		scn.ShadowAssignToMinions(elf1, scrabbler);
		scn.FreepsResolveSkirmish(elf1);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(4, scn.GetStrength(scrabbler));
		scn.FreepsPlayCard(forearmed);
		scn.DismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(footman, scrabbler));
		scn.FreepsChooseCard(footman);

		// Gondorian Knight costs 2: the Footman is 7 - 2 = 5, above 4.  The Scrabbler beside him in the same
		// skirmish is at 4 and is not "that minion": the revealed card stays where it was.
		assertEquals(5, scn.GetStrength(footman));
		assertEquals(4, scn.GetStrength(scrabbler));
		assertEquals(knight, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(forearmed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forearmed = scn.GetFreepsCard("forearmed");
		var elf1 = scn.GetFreepsCard("elf1");
		scn.MoveCompanionsToTable(elf1);
		scn.MoveCardsToHand(forearmed);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf1, savage);
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetFreepsDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.FreepsResolveSkirmish(elf1);

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetStrength(savage));
		assertFalse(scn.FreepsPlayAvailable(forearmed));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void XIsThePrintedCostOfTheRevealedCardWhateverACardInPlayMakesItCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forearmed = scn.GetFreepsCard("forearmed");
		var elf1 = scn.GetFreepsCard("elf1");
		var fearing = scn.GetFreepsCard("fearing");
		var assassin = scn.GetFreepsCard("assassin");
		scn.MoveCompanionsToTable(elf1);
		scn.MoveCardsToSupportArea(fearing);
		scn.MoveCardsToHand(forearmed);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToTopOfDeck(assassin);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(elf1, savage);

		// Fearing the Worst makes the Assassin cost 4 as the game reads it in the deck; it is printed at 2.
		assertEquals(4, scn.GetTwilightCost(assassin));
		scn.FreepsPlayCard(forearmed);
		scn.DismissRevealedCards();

		// 5 - 2 = 3 (not 5 - 4 = 1); 3 is 4 or less, so the Assassin is discarded.
		assertEquals(3, scn.GetStrength(savage));
		assertInDiscard(assassin);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
