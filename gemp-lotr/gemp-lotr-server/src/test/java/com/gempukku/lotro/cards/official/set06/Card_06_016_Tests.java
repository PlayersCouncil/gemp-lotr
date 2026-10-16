package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forearmed", "6_16");
					put("elf1", "1_53");       // Lorien Elf: Elf companion, strength 4, vitality 2, cost 1
					put("elf2", "1_53");
					put("haldir", "1_48");     // Haldir: Elf companion, strength 5, vitality 3; +2 only at sites 6 to 8
					put("silinde", "1_60");    // Silinde: Elf ally, home site 3, strength 5; has site 3's text while it can be spotted
					put("knight", "5_35");     // Gondorian Knight: Man companion, Knight, cost 2
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard", "1_7");       // Dwarf Guard: cost 1, here only as a card on top of the deck
					put("fearing", "2_100");   // Fearing the Worst: the twilight cost of each tracker is +2
					put("assassin", "1_262");  // Orc Assassin: a tracker printed at cost 2, here only on top of the deck

					put("savage", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("footman", "11_82");   // Footman of Dunland: [men] Man, strength 7, vitality 3, cost 3; the site gains plains
					put("troop", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text
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
		 * Game Text: <b>Skirmish:</b> Reveal the top card of your draw deck to make a minion skirmishing an Elf strength -X, where X is the twilight cost of the revealed card.
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
	public void RevealsTheTopCardOfHisOwnDeckToMakeAMinionSkirmishingAnElfWeaker() throws DecisionResultInvalidException, CardNotFoundException {
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
		// On top of the other player's deck, a card of another cost: read from his deck, X would be 2.
		var theirKnight = scn.GetShadowCard("knight");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, footman, troop);
		scn.MoveCardsToTopOfDeck(guard, theirKnight);

		// Not in the assignment phase, the last in which the Free Peoples player acts before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(forearmed));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elf1, savage}, new PhysicalCardImpl[]{elf2, troop});
		scn.ShadowAssignToMinions(elf1, footman);
		scn.FreepsResolveSkirmish(elf1);

		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertEquals(theirKnight, scn.GetShadowTopOfDeck());
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsPlayAvailable(forearmed));
		scn.FreepsPlayCard(forearmed);

		// The top card is shown to both players.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, false));
		scn.DismissRevealedCards();

		// Both minions in the Elf's skirmish; not the one assigned to the other Elf, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertInDiscard(forearmed);
		// Dwarf Guard costs 1: 5 - 1 = 4.
		assertEquals(4, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(13, scn.GetStrength(troop));
		// A reveal moves nothing: the Guard is still on top, and neither deck has changed.
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(theirKnight, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
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

		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsPlayCard(forearmed);
		scn.DismissRevealedCards();

		// Gondorian Knight costs 2: 5 - 2 = 3, read at the next decision of the same skirmish.  The Knight stays on top.
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(knight, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
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

		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsPlayCard(forearmed);
		scn.DismissRevealedCards();

		assertEquals(4, scn.GetStrength(savage));
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheSkirmishOfACompanionWhoIsNoElfItIsPlayedAndNoMinionIsMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertEquals(5, scn.GetStrength(savage));
		// An event's target is no requirement: it is played, and its cost paid.
		assertTrue(scn.FreepsPlayAvailable(forearmed));
		scn.FreepsPlayCard(forearmed);
		scn.DismissRevealedCards();

		// The Knight is no Elf: the minion skirmishing him is not made weaker.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(guard, scn.GetFreepsTopOfDeck());
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
		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsPlayCard(forearmed);
		scn.DismissRevealedCards();

		// 5 - 2 = 3, not 5 - 4 = 1.
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(assassin, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
