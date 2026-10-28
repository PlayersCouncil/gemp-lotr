package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hidden", "7_104");
					put("knight", "5_35");          // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("pippin", "103_31");        // Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); his skirmish ability is used only on purpose
					put("merchant", "7_97");        // Gondorian Merchant: the only [gondor] ally, a character who is no companion
					put("guard1", "5_83");          // Household Guard: a [rohan] Man companion, Valiant and no other text
					put("guard2", "5_83");

					// Garrison of Osgiliath: unique [gondor] fortification condition; its tokens come as it is played (cheated
					// here), its skirmish ability is used only on purpose
					put("garrison", "6_52");
					// A Path Appointed: [gondor] support condition that takes [gondor] tokens; its abilities are used only on purpose
					put("path", "8_42");
					// Noble Leaders: unique [gondor] support condition, here holding no token; its skirmish ability is used only on purpose
					put("leaders", "7_112");
					// Stout and Strong: [dwarven] support condition that takes [dwarven] tokens as Dwarves win skirmishes
					put("stout", "4_57");

					// Cards for the Shadow player's draw deck, read only as Shadow or Free Peoples cards revealed.
					put("soldier", "1_271");        // Orc Soldier: [sauron] minion
					put("fear", "1_278");           // Strength Born of Fear: [sauron] condition
					put("pike", "7_265");           // Besieging Pike: [sauron] possession
					put("threat", "1_197");         // Threat of the Unknown: [moria] event
					put("troop", "1_177");          // Goblin Patrol Troop: [moria] minion
					put("knight2", "5_35");         // Gondorian Knight: a Free Peoples companion
					put("sword", "4_134");          // Sword of Gondor: a Free Peoples possession
					put("valor", "1_103");          // Elendil's Valor: a Free Peoples event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HiddenKnowledgeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Hidden Knowledge
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot a [gondor] companion to reveal the top 5 cards of an opponent's draw deck. For each Shadow card revealed, add a threat. For each Free Peoples card revealed, place a [gondor] token on one of your conditions with a [gondor] token on it. Shuffle that draw deck.
		*/

		var scn = GetScenario();

		var hidden = scn.GetFreepsCard("hidden");

		assertEquals("Hidden Knowledge", hidden.getBlueprint().getTitle());
		assertNull(hidden.getBlueprint().getSubtitle());
		assertFalse(hidden.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, hidden.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, hidden.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, hidden.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(hidden, Timeword.FELLOWSHIP));
		assertEquals(1, hidden.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAGondorCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetFreepsCard("hidden");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard1");
		// A [gondor] character who is no companion, and a companion of another culture.
		scn.MoveAlliesToTable(merchant);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(hidden, knight);
		assertInHand(hidden, knight);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hidden));

		scn.FreepsPlayCard(knight);
		assertInPlay(knight);
		assertTrue(scn.FreepsPlayAvailable(hidden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RevealsTheTopFiveOfTheShadowDeckForTwoThreatsAndThreeTokensThenShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetFreepsCard("hidden");
		var knight = scn.GetFreepsCard("knight");
		var garrison = scn.GetFreepsCard("garrison");
		var path = scn.GetFreepsCard("path");
		var leaders = scn.GetFreepsCard("leaders");
		var stout = scn.GetFreepsCard("stout");
		var ownTop = scn.GetFreepsCard("troop");
		// Three companions, so that a third threat could stand.
		scn.MoveCompanionsToTable(knight, scn.GetFreepsCard("guard1"));
		scn.MoveCardsToSupportArea(garrison, path, leaders, stout);
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(stout, 1);
		scn.MoveCardsToHand(hidden);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(stout));

		scn.StartGame();

		// Top five: Shadow, Free Peoples, Free Peoples, Shadow, Free Peoples; a Shadow card sixth.
		scn.SetShadowDrawDeck(scn.GetShadowCard("soldier"), scn.GetShadowCard("knight2"), scn.GetShadowCard("sword"),
				scn.GetShadowCard("pike"), scn.GetShadowCard("valor"), scn.GetShadowCard("fear"));
		// His own deck holds a Shadow card on top, which would add a threat if it were the deck revealed.
		scn.SetFreepsDrawDeck(ownTop);
		scn.FreepsRefreshDecision();
		assertEquals(6, scn.GetShadowDeckCount());
		assertEquals(ownTop, scn.GetFreepsTopOfDeck());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsPlayAvailable(hidden));
		scn.FreepsPlayCard(hidden);

		// The five are shown to both players, and the sixth is not.
		assertEquals(5, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(5, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// Two Shadow cards: two threats (a sixth card would have made three).
		assertEquals(2, scn.GetThreats());

		// Three Free Peoples cards: three tokens, each on a condition of his already holding a [gondor] token; not on
		// the [gondor] condition holding none, nor the one holding a [dwarven] token.  "For each" lets two go on one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, path));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(leaders, stout));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(garrison);
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, path));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(leaders, stout));
		scn.FreepsChooseCard(garrison);
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, path));
		scn.FreepsChooseCard(path);

		assertEquals(4, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(stout));

		// That deck, and only that deck, is shuffled once; the cards stay in it.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(6, scn.GetShadowDeckCount());
		assertEquals(ownTop, scn.GetFreepsTopOfDeck());
		assertInDiscard(hidden);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAGondorHobbitSpottedFourShadowCardsAddFourThreatsAndOneFreePeoplesCardOneToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetFreepsCard("hidden");
		var pippin = scn.GetFreepsCard("pippin");
		var garrison = scn.GetFreepsCard("garrison");
		var path = scn.GetFreepsCard("path");
		// The only [gondor] companion is a Hobbit; four companions, so that four threats can stand.
		scn.MoveCompanionsToTable(pippin, scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		scn.MoveCardsToSupportArea(garrison, path);
		scn.AddTokensToCard(garrison, 1);
		scn.AddTokensToCard(path, 1);
		scn.MoveCardsToHand(hidden);
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(path));

		scn.StartGame();

		// Top five: a Shadow minion, condition, possession and event, and a Free Peoples card; a Free Peoples card sixth.
		scn.SetShadowDrawDeck(scn.GetShadowCard("soldier"), scn.GetShadowCard("fear"), scn.GetShadowCard("pike"),
				scn.GetShadowCard("threat"), scn.GetShadowCard("sword"), scn.GetShadowCard("valor"));
		scn.FreepsRefreshDecision();
		assertEquals(6, scn.GetShadowDeckCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsPlayAvailable(hidden));
		scn.FreepsPlayCard(hidden);
		scn.DismissRevealedCards();

		assertEquals(4, scn.GetThreats());

		// One token, not two (the sixth card is not counted).
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, path));
		scn.FreepsChooseCard(path);
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(path));

		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertInDiscard(hidden);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoConditionHoldingAGondorTokenTheFreePeoplesCardsRevealedPlaceNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetFreepsCard("hidden");
		var knight = scn.GetFreepsCard("knight");
		var leaders = scn.GetFreepsCard("leaders");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(leaders, stout);
		scn.AddTokensToCard(stout, 1);
		scn.MoveCardsToHand(hidden);
		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(stout));

		scn.StartGame();

		scn.SetShadowDrawDeck(scn.GetShadowCard("troop"), scn.GetShadowCard("knight2"), scn.GetShadowCard("sword"),
				scn.GetShadowCard("valor"), scn.GetShadowCard("soldier"));
		scn.FreepsRefreshDecision();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetThreats());

		// The event is played all the same, with nothing to put its tokens on.
		assertTrue(scn.FreepsPlayAvailable(hidden));
		scn.FreepsPlayCard(hidden);
		scn.DismissRevealedCards();

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertInDiscard(hidden);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
