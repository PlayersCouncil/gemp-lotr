package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shaft", "1_22");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability only

					// Cards of different titles to lay the draw deck with; none is played.
					put("armor", "1_8");        // Dwarven Armor
					put("heart", "1_10");       // Dwarven Heart
					put("guard", "1_7");        // Dwarf Guard
					put("farin", "1_11");       // Farin, Dwarven Emissary
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("savage", "1_151");     // Uruk Savage

					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MithrilShaftStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Mithril Shaft
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a Dwarf to discard cards from the top of your draw deck until you choose to stop (limit 5). Add (1) for each card discarded in this way. Take the last card discarded into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shaft");

		assertEquals("Mithril Shaft", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsADwarfToDiscardFromTheTopOfTheDeckUntilHeStopsAndTakesTheLastCardIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaft = scn.GetFreepsCard("shaft");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var guard = scn.GetFreepsCard("guard");
		var farin = scn.GetFreepsCard("farin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(shaft);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetFreepsDrawDeck(armor, heart, guard, farin);
		assertEquals(armor, scn.GetFreepsTopOfDeck());
		assertEquals(4, scn.GetFreepsDeckCount());
		int pool = scn.GetTwilight();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.FreepsPlayAvailable(shaft));

		scn.FreepsPlayCard(shaft);
		// A Dwarf companion and a Dwarf ally may pay; Frodo, no Dwarf, may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		// The top card goes, and (1) with it, before he is asked whether to go on.
		assertInDiscard(armor);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("continue discarding"));

		scn.FreepsChooseYes();
		assertInDiscard(heart);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("continue discarding"));

		scn.FreepsChooseNo();
		// The last card discarded is taken into hand; the first stays in the discard pile.
		assertInHand(heart);
		assertInDiscard(armor, shaft);
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(pool + 2, scn.GetTwilight());
		// The Shadow player's draw deck is not touched.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheFirstCardIsDiscardedUnaskedAndStoppingThereAddsOneAndTakesItBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaft = scn.GetFreepsCard("shaft");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(shaft);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetFreepsDrawDeck(armor, heart, guard);
		assertEquals(armor, scn.GetFreepsTopOfDeck());
		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Gimli is the one Dwarf, and pays unasked.
		scn.FreepsPlayCard(shaft);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInDiscard(armor);
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("continue discarding"));

		scn.FreepsChooseNo();
		assertInHand(armor);
		assertEquals(heart, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AfterTheFifthCardNoMoreIsDiscardedAndTheFifthIsTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaft = scn.GetFreepsCard("shaft");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var guard = scn.GetFreepsCard("guard");
		var farin = scn.GetFreepsCard("farin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetFreepsCard("savage");
		var ownRunner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(shaft);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetFreepsDrawDeck(armor, heart, guard, farin, aragorn, savage, ownRunner);
		assertEquals(armor, scn.GetFreepsTopOfDeck());
		assertEquals(7, scn.GetFreepsDeckCount());
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(shaft);
		assertInDiscard(armor);
		scn.FreepsChooseYes();
		assertInDiscard(heart);
		scn.FreepsChooseYes();
		assertInDiscard(guard);
		scn.FreepsChooseYes();
		assertInDiscard(farin);
		assertEquals(pool + 4, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("continue discarding"));

		// The fifth card is the limit: no sixth is offered.
		scn.FreepsChooseYes();
		assertFalse(scn.FreepsDecisionAvailable("continue discarding"));
		assertInHand(aragorn);
		assertInDiscard(armor, heart, guard, farin);
		assertEquals(savage, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(pool + 5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotPlayableWhenTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaft = scn.GetFreepsCard("shaft");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(shaft);
		// Vitality 3 with 2 wounds: Gimli cannot exert.  Frodo, who could, is no Dwarf.
		scn.AddWoundsToChar(gimli, 2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertFalse(scn.FreepsPlayAvailable(shaft));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckItIsPlayedForTheExertionAndNothingIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaft = scn.GetFreepsCard("shaft");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(shaft);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsPlayAvailable(shaft));

		scn.FreepsPlayCard(shaft);
		assertEquals(1, scn.GetWoundsOn(gimli));
		// No card discarded: nothing added, and nothing taken into hand.
		assertEquals(pool, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInDiscard(shaft);
		assertFalse(scn.FreepsDecisionAvailable("continue discarding"));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WhenTheDeckRunsOutTheLastCardItHadIsTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaft = scn.GetFreepsCard("shaft");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(shaft);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetFreepsDrawDeck(armor, heart);
		assertEquals(2, scn.GetFreepsDeckCount());
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(shaft);
		assertInDiscard(armor);
		assertEquals(heart, scn.GetFreepsTopOfDeck());
		scn.FreepsChooseYes();
		// The deck is empty: there is nothing more to discard, so he is not asked again.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsDecisionAvailable("continue discarding"));
		assertInHand(heart);
		assertInDiscard(armor);
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WhenTheDeckRunsOutAtTheThirdCardTheLastCardItHadIsTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaft = scn.GetFreepsCard("shaft");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(shaft);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetFreepsDrawDeck(armor, heart, guard);
		assertEquals(3, scn.GetFreepsDeckCount());
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(shaft);
		assertInDiscard(armor);
		assertEquals(pool + 1, scn.GetTwilight());
		scn.FreepsChooseYes();
		assertInDiscard(heart);
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertEquals(pool + 2, scn.GetTwilight());
		scn.FreepsChooseYes();
		// The deck is empty: there is nothing more to discard, so he is not asked again.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsDecisionAvailable("continue discarding"));
		assertInHand(guard);
		assertInDiscard(armor, heart);
		assertEquals(pool + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WhenTheDeckRunsOutAtTheFourthCardTheLastCardItHadIsTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shaft = scn.GetFreepsCard("shaft");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var guard = scn.GetFreepsCard("guard");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(shaft);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetFreepsDrawDeck(armor, heart, guard, farin);
		assertEquals(4, scn.GetFreepsDeckCount());
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(shaft);
		assertInDiscard(armor);
		scn.FreepsChooseYes();
		assertInDiscard(heart);
		scn.FreepsChooseYes();
		assertInDiscard(guard);
		assertEquals(farin, scn.GetFreepsTopOfDeck());
		assertEquals(pool + 3, scn.GetTwilight());
		scn.FreepsChooseYes();
		// The deck is empty: there is nothing more to discard, so he is not asked again.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsDecisionAvailable("continue discarding"));
		assertInHand(farin);
		assertInDiscard(armor, heart, guard);
		assertEquals(pool + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
