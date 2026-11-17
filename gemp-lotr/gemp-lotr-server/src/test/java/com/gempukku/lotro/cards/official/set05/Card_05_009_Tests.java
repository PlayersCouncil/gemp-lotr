package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInHand;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("liking", "5_9");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, no text but its own play requirement
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, a Maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a Maneuver ability used only on purpose
					// Here Is Good Rock: unique [dwarven] tale condition in the support area; a Skirmish ability only
					put("rock", "4_50");
					// From the Armory: [dwarven] condition in the support area; a Skirmish ability only
					put("armory", "4_47");
					// I Would Have Gone With You to the End: [gondor] condition in the support area, which stacks its
					// owner's skirmish events; with no Aragorn in play it does nothing (none is laid where it is)
					put("vow", "101_23");
					// Cards to stack, none with text that acts from a stack
					put("guard2", "1_7");       // Dwarf Guard: a Free Peoples companion
					put("axe", "1_9");          // Dwarven Axe: a Free Peoples possession
					put("axe2", "1_9");
					put("strike", "1_3");       // Axe Strike: a Free Peoples skirmish event
					put("strike2", "1_3");
					put("strike3", "1_3");
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion with no text

					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase; its text acts only as it is played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoretoMyLikingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: More to My Liking
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * Maneuver: Stack the top card from your draw deck here (limit once per phase).
		 * Maneuver: Spot a Dwarf and discard a card stacked on a [dwarven] condition to take a Free Peoples card stacked here into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("liking");

		assertEquals("More to My Liking", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ManeuverActionStacksTheTopCardOfTheDrawDeckHereOncePerPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liking = scn.GetFreepsCard("liking");
		var axe = scn.GetFreepsCard("axe");
		var guard2 = scn.GetFreepsCard("guard2");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCardsToSupportArea(liking);

		var runner = scn.GetShadowCard("runner");
		var theirAxe = scn.GetShadowCard("axe");

		scn.StartGame();
		// The Dwarven Axe on top, the Dwarf Guard under it, the Axe Strike under that; the other player's
		// Dwarven Axe on top of his own deck.
		scn.MoveCardsToTopOfDeck(strike, guard2, axe, theirAxe);
		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertSame(guard2, scn.GetFromTopOfFreepsDeck(2));
		assertInZone(Zone.DECK, axe, guard2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertSame(theirAxe, scn.GetShadowTopOfDeck());
		int deck = scn.GetFreepsDeckCount();
		int theirDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetStackedCards(liking).size());
		assertTrue(scn.FreepsActionAvailable(liking, "Stack top card"));

		scn.FreepsUseCardAction(liking, "Stack top card");
		assertEquals(1, scn.GetStackedCards(liking).size());
		assertTrue(scn.GetStackedCards(liking).contains(axe));
		assertInZone(Zone.STACKED, axe);
		assertSame(guard2, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(theirAxe, scn.GetShadowTopOfDeck());
		assertEquals(theirDeck, scn.GetShadowDeckCount());

		// Once in this maneuver phase, though the deck still has cards.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(liking, "Stack top card"));

		// The fellowship moves again in the regroup phase and the minion stays: in the next maneuver phase of the
		// same turn the action is offered again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(liking, "Stack top card"));
		scn.FreepsUseCardAction(liking, "Stack top card");
		assertEquals(2, scn.GetStackedCards(liking).size());
		assertTrue(scn.GetStackedCards(liking).contains(guard2));
		assertSame(strike, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void StackActionIsNotOfferedInTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liking = scn.GetFreepsCard("liking");
		scn.MoveCardsToSupportArea(liking);

		scn.StartGame();

		assertTrue(scn.GetFreepsDeckCount() > 0);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(liking, "Stack top card"));
	}

	@Test
	public void StackActionIsNotOfferedWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liking = scn.GetFreepsCard("liking");
		scn.MoveCardsToSupportArea(liking);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Nothing to stack, and nothing paid: the action would do nothing.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(liking, "Stack top card"));
	}

	@Test
	public void SpottingADwarfCompanionAndDiscardingAStackedCardTakesAFreePeoplesCardStackedHereIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liking = scn.GetFreepsCard("liking");
		var guard = scn.GetFreepsCard("guard");
		var rock = scn.GetFreepsCard("rock");
		var vow = scn.GetFreepsCard("vow");
		var guard2 = scn.GetFreepsCard("guard2");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var strike = scn.GetFreepsCard("strike");
		var strike2 = scn.GetFreepsCard("strike2");
		var strike3 = scn.GetFreepsCard("strike3");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(liking, rock, vow);
		// Free Peoples cards of three types and a Shadow card here; two Free Peoples cards on another [dwarven]
		// condition; a Free Peoples card on a [gondor] condition.
		scn.StackCardsOn(liking, guard2, axe, strike, troop);
		scn.StackCardsOn(rock, strike2, axe2);
		scn.StackCardsOn(vow, strike3);

		// The other player's own [dwarven] condition with a Free Peoples card stacked: his Free Peoples cards are
		// not active on this turn.
		var theirArmory = scn.GetShadowCard("armory");
		var theirAxe = scn.GetShadowCard("axe");
		scn.MoveCardsToSupportArea(theirArmory);
		scn.StackCardsOn(theirArmory, theirAxe);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.STACKED, guard2, axe, strike, troop, strike2, axe2, strike3, theirAxe);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(liking, "Take a Free Peoples card"));
		scn.FreepsUseCardAction(liking, "Take a Free Peoples card");

		// The cost: any card stacked on a [dwarven] condition of his, this one or the Rock; not one on a [gondor]
		// condition, nor one on the other player's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, axe, strike, troop, strike2, axe2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(strike3, theirAxe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(strike2);
		assertInZone(Zone.DISCARD, strike2);

		// The take: a Free Peoples card stacked here; not the Shadow card here, nor the Free Peoples card on the Rock.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, axe, strike));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, axe2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);

		assertInHand(axe);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetStackedCards(liking).size());
		assertEquals(1, scn.GetStackedCards(rock).size());
		assertTrue(scn.GetStackedCards(rock).contains(axe2));
		assertEquals(1, scn.GetStackedCards(vow).size());
		assertEquals(1, scn.GetStackedCards(theirArmory).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ADwarfAllyIsADwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liking = scn.GetFreepsCard("liking");
		var thrarin = scn.GetFreepsCard("thrarin");
		var rock = scn.GetFreepsCard("rock");
		var axe = scn.GetFreepsCard("axe");
		var strike2 = scn.GetFreepsCard("strike2");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(liking, rock);
		scn.StackCardsOn(liking, axe);
		scn.StackCardsOn(rock, strike2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.STACKED, axe, strike2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// The ally is the only Dwarf.
		assertTrue(scn.FreepsActionAvailable(liking, "Take a Free Peoples card"));
		scn.FreepsUseCardAction(liking, "Take a Free Peoples card");
		scn.FreepsChooseCard(strike2);

		// The one card left here is taken with nothing asked.
		assertInZone(Zone.DISCARD, strike2);
		assertInHand(axe);
		assertEquals(0, scn.GetStackedCards(liking).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TakeActionIsNotOfferedWithNoDwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liking = scn.GetFreepsCard("liking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rock = scn.GetFreepsCard("rock");
		var guard2 = scn.GetFreepsCard("guard2");
		var axe = scn.GetFreepsCard("axe");
		// A companion who is no Dwarf, two [dwarven] cards that are no Dwarves, and a Dwarf who is stacked and so
		// not in play.  The cost could be paid.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(liking, rock);
		scn.StackCardsOn(liking, guard2, axe);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.STACKED, guard2, axe);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(liking, "Stack top card"));
		assertFalse(scn.FreepsActionAvailable(liking, "Take a Free Peoples card"));
	}

	@Test
	public void TakeActionIsNotOfferedWithNothingStackedOnYourDwarvenConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liking = scn.GetFreepsCard("liking");
		var guard = scn.GetFreepsCard("guard");
		var rock = scn.GetFreepsCard("rock");
		var vow = scn.GetFreepsCard("vow");
		var strike3 = scn.GetFreepsCard("strike3");
		scn.MoveCompanionsToTable(guard);
		// Cards are stacked only on a [gondor] condition and on the other player's [dwarven] condition.
		scn.MoveCardsToSupportArea(liking, rock, vow);
		scn.StackCardsOn(vow, strike3);

		var theirArmory = scn.GetShadowCard("armory");
		var theirAxe = scn.GetShadowCard("axe");
		scn.MoveCardsToSupportArea(theirArmory);
		scn.StackCardsOn(theirArmory, theirAxe);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.STACKED, strike3, theirAxe);
		assertEquals(0, scn.GetStackedCards(liking).size());
		assertEquals(0, scn.GetStackedCards(rock).size());
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(liking, "Stack top card"));
		assertFalse(scn.FreepsActionAvailable(liking, "Take a Free Peoples card"));
	}

	@Test
	public void WithOnlyAShadowCardStackedHereTheCostIsPaidAndNothingIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var liking = scn.GetFreepsCard("liking");
		var guard = scn.GetFreepsCard("guard");
		var rock = scn.GetFreepsCard("rock");
		var strike2 = scn.GetFreepsCard("strike2");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(liking, rock);
		scn.StackCardsOn(liking, troop);
		scn.StackCardsOn(rock, strike2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.STACKED, troop, strike2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// What the action takes is its target, not a requirement: it is offered, and the cost is paid.
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(liking, "Take a Free Peoples card"));
		scn.FreepsUseCardAction(liking, "Take a Free Peoples card");
		scn.FreepsChooseCard(strike2);

		assertInZone(Zone.DISCARD, strike2);
		assertInZone(Zone.STACKED, troop);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
