package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("avoided", "18_32");
					// Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5; a regroup ability used only on purpose
					put("gollum", "5_24");
					// They Stole It: [gollum] Shadow condition; a skirmish ability used only on purpose
					put("stole", "6_46");
					// Sméagol, Poor Creature: [gollum] Free Peoples companion, Ring-bound, strength 3; a skirmish
					// ability used only on purpose
					put("smeagol", "6_45");
					// Shelob, Last Child of Ungoliant: [gollum] minion, strength 8, vitality 8; enduring, fierce
					put("shelob", "8_26");
					put("runner", "1_178");       // Goblin Runner: [moria] minion; may add (2) when played
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13; no text
					put("hunter", "1_256");       // Morgul Hunter: [sauron] Orc, a card of another culture for a hand
					put("ships", "8_65");         // Ships of Great Draught: [raider] possession, for a hand
					put("sam", "1_311");          // Sam, Son of Hamfast: a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NotEasilyAvoidedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Not Easily Avoided
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Shadow:</b> Remove (3) to take a [gollum] card into hand from your draw deck, then randomly
		 * 	discard a card from your hand.<br><b>Skirmish:</b> Discard a [gollum] card from hand to make a [gollum]
		 * 	minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("avoided");

		assertEquals("Not Easily Avoided", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityTakesAGollumCardFromTheDeckWhichFromAnEmptyHandIsThenTheCardDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avoided = scn.GetShadowCard("avoided");
		var gollum = scn.GetShadowCard("gollum");
		var stole = scn.GetShadowCard("stole");
		var smeagol = scn.GetShadowCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(avoided);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P2, gollum, stole, smeagol, runner);
		scn.FreepsPass();

		// 0 + 1 companion + 2 for site 2
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.ShadowActionAvailable(avoided));
		scn.ShadowUseCardAction(avoided);
		assertEquals(0, scn.GetTwilight());

		// The deck is shown first, every card of it, none to be chosen there.
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		scn.ShadowDismissRevealedCards();
		// A [gollum] minion, a [gollum] condition and a [gollum] Free Peoples companion may be taken; a [moria] minion not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, stole, smeagol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(stole);

		// The one card in hand is then the one discarded at random.
		assertInDiscard(stole);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(3, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, gollum, smeagol, runner);
		// The deck looked through is shuffled once; the other player's is not.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheCardTakenIsNamedInTheGameLogAndOneCardOfTheHandAtRandomIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avoided = scn.GetShadowCard("avoided");
		var gollum = scn.GetShadowCard("gollum");
		var stole = scn.GetShadowCard("stole");
		var runner = scn.GetShadowCard("runner");
		var hunter = scn.GetShadowCard("hunter");
		var ships = scn.GetShadowCard("ships");
		var troop = scn.GetShadowCard("troop");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(avoided);

		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P2, gollum, stole, runner);
		scn.SetHand(VirtualTableScenario.P2, hunter, ships, troop, shelob);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertInHand(hunter, ships, troop, shelob);
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		int freepsHand = scn.GetFreepsHandCount();

		scn.MarkGameLog();
		scn.ShadowUseCardAction(avoided);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(gollum);

		// Taken by its culture with no "reveal" printed: the game log names it, and not a card left in the deck.
		// (Gollum may also be the card then discarded, which names him too: four runs in five he stays in hand.)
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(gollum));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner));

		// One card of the five then in hand is discarded, and only one.
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, stole, runner);
		// Four of the five are still in hand: the one missing is the card discarded.
		assertEquals(4, List.of(gollum, hunter, ships, troop, shelob).stream().filter(card -> scn.GetShadowHand().contains(card)).count());
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoGollumCardInTheDeckTheThreeIsPaidForNothingAndACardIsStillDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avoided = scn.GetShadowCard("avoided");
		var runner = scn.GetShadowCard("runner");
		var hunter = scn.GetShadowCard("hunter");
		var ships = scn.GetShadowCard("ships");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(avoided);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P2, runner, troop);
		scn.SetHand(VirtualTableScenario.P2, hunter, ships);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertInHand(hunter, ships);
		assertEquals(0, scn.GetShadowDiscardCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.ShadowActionAvailable(avoided));
		scn.ShadowUseCardAction(avoided);
		assertEquals(0, scn.GetTwilight());

		// The deck is shown with nothing in it to be chosen; that showing is the only decision.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, false));
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.DECK, runner, troop);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		// One of the two is still in hand: the other is the card discarded.
		assertEquals(1, List.of(hunter, ships).stream().filter(card -> scn.GetShadowHand().contains(card)).count());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithTwoInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avoided = scn.GetShadowCard("avoided");
		var stole = scn.GetShadowCard("stole");
		scn.MoveCardsToSupportArea(avoided);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P2, stole);
		scn.FreepsPass();

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(avoided));
		assertInZone(Zone.DECK, stole);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsAGollumCardFromHandToMakeAGollumMinionStrongerUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avoided = scn.GetShadowCard("avoided");
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		var stole = scn.GetShadowCard("stole");
		var handSmeagol = scn.GetShadowCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(avoided);

		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, smeagol);

		scn.StartGame();
		scn.SetHand(VirtualTableScenario.P2, stole, handSmeagol, runner);
		scn.MoveMinionsToTable(shelob, gollum, troop);
		assertInHand(stole, handSmeagol, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, shelob}, new PhysicalCardImpl[]{smeagol, troop});
		// Gollum is left over, and stays unassigned.
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(gollum));

		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(shelob));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.ShadowActionAvailable(avoided));
		scn.ShadowUseCardAction(avoided);

		// The cost: a [gollum] card of either side from hand; not a [moria] card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stole, handSmeagol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(stole);
		assertInDiscard(stole);
		assertInHand(handSmeagol, runner);

		// The minion: Shelob in this skirmish and Gollum in none; not the [moria] minion, nor the [gollum] companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shelob, gollum));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, smeagol));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(shelob);
		assertEquals(10, scn.GetStrength(shelob));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Sam, 3 against 10, is killed; the next skirmish is Sméagol's, and Shelob's bonus is gone with hers.
		assertInZone(Zone.DEAD, sam);
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityWithNoGollumMinionInPlayIsOfferedAndTheCardIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avoided = scn.GetShadowCard("avoided");
		var troop = scn.GetShadowCard("troop");
		var stole = scn.GetShadowCard("stole");
		var handSmeagol = scn.GetShadowCard("smeagol");
		scn.MoveCardsToSupportArea(avoided);

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SetHand(VirtualTableScenario.P2, stole, handSmeagol);
		scn.MoveMinionsToTable(troop);
		assertInHand(stole, handSmeagol);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, troop);
		scn.FreepsPass();

		// The one minion in play is a [moria] minion: no [gollum] minion for the bonus.
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.ShadowActionAvailable(avoided));
		scn.ShadowUseCardAction(avoided);

		assertTrue(scn.ShadowHasCardChoiceAvailable(stole, handSmeagol));
		scn.ShadowChooseCard(stole);

		// The cost is paid and nothing is strengthened.
		assertInDiscard(stole);
		assertInHand(handSmeagol);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoGollumCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avoided = scn.GetShadowCard("avoided");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(avoided);

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SetHand(VirtualTableScenario.P2, runner);
		scn.MoveMinionsToTable(shelob);
		assertInHand(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, shelob);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(shelob));
		assertFalse(scn.ShadowActionAvailable(avoided));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
