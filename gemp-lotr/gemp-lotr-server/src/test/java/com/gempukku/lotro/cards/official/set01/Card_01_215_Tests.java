package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_215_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("will", "1_215");

					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] Nazgul; text on companions bearing [wraith] conditions only
					put("nelya", "2_84");       // Ulaire Nelya: a [wraith] twilight Nazgul; the rest needs 2 burdens or 2 wounds on the Ring-bearer
					put("cantea", "1_230");     // Ulaire Cantea: a [wraith] Nazgul, fierce; a skirmish ability used only on purpose
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul; the rest needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] minion who is an Orc and no Nazgul; the rest needs 2 wounds on the Ring-bearer

					// Cards for the draw deck.
					put("tip", "1_209");        // Blade Tip: a [wraith] condition
					put("veils", "1_204");      // All Veils Removed: a [wraith] event
					put("runner", "1_178");     // Goblin Runner: a [moria] minion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMastersWillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Master's Will
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Search</b>.<br><b>Shadow:</b> Spot X Nazgûl to reveal the top X cards of your draw deck. Take into your hand all [wraith] cards revealed and discard the rest.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("will");

		assertEquals("The Master's Will", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingTwoNazgulOfEitherCultureRevealsTheTopTwoAndTakesTheWraithCardIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var will = scn.GetShadowCard("will");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var tip = scn.GetShadowCard("tip");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetShadowCard("aragorn");
		scn.MoveCardsToHand(will);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		// Top first: a [wraith] condition and a Free Peoples companion, and a [moria] minion below them.
		scn.SetShadowDrawDeck(tip, aragorn, runner);
		scn.FreepsPass();

		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(will));
		scn.ShadowPlayCard(will);

		// Two Nazgul of two cultures can be spotted; the [wraith] Orc is no Nazgul.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// The top 2 are shown to each player, and the third card is not.
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(tip, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, false));
		scn.DismissRevealedCards();

		// The [wraith] card is taken with no choice asked; the Free Peoples one is discarded.
		assertInHand(tip);
		assertEquals(1, scn.GetShadowHandCount());
		assertInDiscard(aragorn, will);
		assertEquals(2, scn.GetShadowDiscardCount());
		// The third card is still on top of the deck, and the other player's deck is untouched.
		assertEquals(1, scn.GetShadowDeckCount());
		assertSame(runner, scn.GetShadowTopOfDeck());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingThreeNazgulRevealsTheTopThreeTakingEveryWraithCardAndDiscardingANazgulOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var will = scn.GetShadowCard("will");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var cantea = scn.GetShadowCard("cantea");
		var veils = scn.GetShadowCard("veils");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(will);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya, cantea);
		// Top first: a [wraith] event, a [wraith] minion who is no Nazgul, a [raider] Nazgul, and a [moria] minion.
		scn.SetShadowDrawDeck(veils, hound, khamul, runner);
		scn.FreepsPass();

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.ShadowPlayCard(will);

		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");

		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// Every [wraith] card revealed, of either type, is taken; the Nazgul of another culture is discarded.
		assertInHand(veils, hound);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(khamul, will);
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertSame(runner, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThePlayerMaySpotFewerNazgulThanThereAreAndRevealsOnlyThatMany() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var will = scn.GetShadowCard("will");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var tip = scn.GetShadowCard("tip");
		var veils = scn.GetShadowCard("veils");
		scn.MoveCardsToHand(will);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul);
		scn.SetShadowDrawDeck(tip, veils);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(will);

		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// One card revealed and taken; the second [wraith] card is still on top of the deck.
		assertInHand(tip);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertSame(veils, scn.GetShadowTopOfDeck());
		assertInDiscard(will);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayableInTheShadowPhaseAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var will = scn.GetShadowCard("will");
		var lemenya = scn.GetShadowCard("lemenya");
		var tip = scn.GetShadowCard("tip");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(will);

		scn.StartGame();
		assertInHand(will);
		scn.MoveMinionsToTable(lemenya);
		scn.SetShadowDrawDeck(tip, runner);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(will));
		scn.ShadowPass();

		// The next phase in which the Shadow player is asked for actions: the maneuver phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(will));
		assertEquals(2, scn.GetShadowDeckCount());
	}
}
