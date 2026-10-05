package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_002_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("delvings", "101_2");
					// Ever My Heart Rises: "Fellowship: Spot a Dwarf and discard the top card of your draw deck to
					// take a Free Peoples card stacked here into hand."  Cheated in, it has nothing stacked on it.
					put("everMyHeart", "4_46");
					// Greatest Kingdom of My People: "Each time your opponent plays an Orc, that player discards the
					// top card of his or her draw deck."
					put("kingdom", "1_16");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, vitality 2, no text in play
					put("tiredGuard", "1_7");   // a second Dwarf Guard, to be exhausted
					put("deckGuard", "1_7");    // a third, as a [dwarven] card in the draw deck or stacked
					// Grimir, Dwarven Elder: Dwarf ally, home site 3, vitality 3.  His fellowship ability is never
					// used here.
					put("grimir", "1_17");

					put("strike", "1_3");       // Axe Strike: [dwarven] skirmish event
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] card

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					// Uruk Soldier: cost 2, site 5.  "When you play this minion, you may make the Free Peoples
					// player discard the top card of his draw deck."
					put("soldier", "1_154");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeepestDelvingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Deepest Delvings
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time a [dwarven] card is discarded from the top of your draw deck, you may add (1) to
		 * stack that card here.
		 * Maneuver: Exert a Dwarf to take a card stacked here into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("delvings");

		assertEquals("Deepest Delvings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var delvings = scn.GetFreepsCard("delvings");
		scn.MoveCardsToHand(delvings);

		scn.StartGame();

		assertInHand(delvings);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		// Nothing has to be spotted: only Frodo is in play.
		assertTrue(scn.FreepsPlayAvailable(delvings));

		scn.FreepsPlayCard(delvings);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, delvings);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarvenCardDiscardedFromTopOfDeckMayBeStackedHereForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var delvings = scn.GetFreepsCard("delvings");
		var everMyHeart = scn.GetFreepsCard("everMyHeart");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToSupportArea(delvings, everMyHeart);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(strike);
		assertSame(strike, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetStackedCards(delvings).size());

		// Ever My Heart Rises discards Axe Strike from the top of the deck as its cost.
		scn.FreepsChooseAction("Discard 1 card from the top");
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		// Declined: no twilight, and the card stays discarded.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(strike);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetStackedCards(delvings).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToTopOfDeck(axe);
		assertSame(axe, scn.GetFreepsTopOfDeck());

		scn.FreepsChooseAction("Discard 1 card from the top");
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		// Accepted: (1) is added and the card leaves the discard pile for the stack.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.STACKED, axe);
		assertEquals(1, scn.GetStackedCards(delvings).size());
		assertTrue(scn.GetStackedCards(delvings).contains(axe));
		assertEquals(0, scn.GetStackedCards(everMyHeart).size());
		// Only the declined Axe Strike is in the discard pile.
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EveryDwarvenCardDiscardedFromTopOfDeckIsOfferedAndACardOfAnotherCultureIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var delvings = scn.GetFreepsCard("delvings");
		var everMyHeart = scn.GetFreepsCard("everMyHeart");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var deckGuard = scn.GetFreepsCard("deckGuard");
		scn.MoveCardsToSupportArea(delvings, everMyHeart);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetStackedCards(delvings).size());

		// A [dwarven] event
		scn.MoveCardsToTopOfDeck(strike);
		scn.FreepsChooseAction("Discard 1 card from the top");
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetStackedCards(delvings).size());

		// A [gondor] companion: discarded from the same place by the same action, and nothing is offered.
		scn.MoveCardsToTopOfDeck(aragorn);
		scn.FreepsChooseAction("Discard 1 card from the top");
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInDiscard(aragorn);
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetStackedCards(delvings).size());

		// A [dwarven] possession: the second time
		scn.MoveCardsToTopOfDeck(axe);
		scn.FreepsChooseAction("Discard 1 card from the top");
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetStackedCards(delvings).size());

		// A [dwarven] companion: the third time
		scn.MoveCardsToTopOfDeck(deckGuard);
		scn.FreepsChooseAction("Discard 1 card from the top");
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		// (1) each for Axe Strike, Dwarven Axe and Dwarf Guard
		assertEquals(3, scn.GetTwilight());
		assertEquals(3, scn.GetStackedCards(delvings).size());
		assertTrue(scn.GetStackedCards(delvings).contains(strike));
		assertTrue(scn.GetStackedCards(delvings).contains(axe));
		assertTrue(scn.GetStackedCards(delvings).contains(deckGuard));
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarvenCardDiscardedFromHandIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var delvings = scn.GetFreepsCard("delvings");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCardsToSupportArea(delvings);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(strike);

		scn.StartGame();

		// No minions, so the turn runs straight to the regroup phase and the reconciliation that ends it.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();

		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsDecisionAvailable("reconcile"));
		assertInHand(strike);
		assertEquals(0, scn.GetFreepsDiscardCount());

		// Reconciling lets a player discard a card from hand: a [dwarven] card, but not from the draw deck.
		scn.FreepsChooseCard(strike);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(strike);
		assertEquals(0, scn.GetStackedCards(delvings).size());
		// The turn has ended and the other player's has begun.
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void OpponentsEffectDiscardingFromMyDeckIsOfferedAndADiscardFromTheOpponentsDeckIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var delvings = scn.GetFreepsCard("delvings");
		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var myStrike = scn.GetFreepsCard("strike");
		var theirStrike = scn.GetShadowCard("strike");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(delvings, kingdom);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(troop, soldier);

		scn.StartGame();

		scn.MoveCardsToTopOfDeck(myStrike, theirStrike);
		assertSame(myStrike, scn.GetFreepsTopOfDeck());
		assertSame(theirStrike, scn.GetShadowTopOfDeck());

		// Enough for both minions to be playable when the Shadow player is asked, then an exact pool to count from.
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetTwilight(12);

		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// An Orc is played, so Greatest Kingdom makes the Shadow player discard the top card of his own deck:
		// a [dwarven] card, but not from my draw deck.
		scn.ShadowPlayCard(troop);
		assertInDiscard(theirStrike);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetStackedCards(delvings).size());
		// 12 - (6 to play + 2 roaming at site 2)
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Uruk Soldier lets the Shadow player discard the top card of my deck.
		scn.ShadowPlayCard(soldier);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		// 4 - (2 to play + 2 roaming at site 2)
		assertEquals(0, scn.GetTwilight());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.STACKED, myStrike);
		assertTrue(scn.GetStackedCards(delvings).contains(myStrike));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverActionExertsAChosenDwarfToTakeAChosenStackedCardIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var delvings = scn.GetFreepsCard("delvings");
		var everMyHeart = scn.GetFreepsCard("everMyHeart");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var tiredGuard = scn.GetFreepsCard("tiredGuard");
		var grimir = scn.GetFreepsCard("grimir");
		var strike = scn.GetFreepsCard("strike");
		var axe = scn.GetFreepsCard("axe");
		var deckGuard = scn.GetFreepsCard("deckGuard");
		var troop = scn.GetShadowCard("troop");
		// Grimir is an ally, so he sits in the support area.
		scn.MoveCardsToSupportArea(delvings, everMyHeart, grimir);
		scn.MoveCompanionsToTable(gimli, guard, tiredGuard);
		scn.StackCardsOn(delvings, strike, axe);
		// A [dwarven] card like the others, but stacked somewhere else.
		scn.StackCardsOn(everMyHeart, deckGuard);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(tiredGuard, 1);

		scn.StartGame();

		// Not a fellowship action.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(delvings));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(delvings));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetStackedCards(delvings).size());

		scn.FreepsUseCardAction(delvings);

		// The cost: any Dwarf who can exert, companion or ally, and nobody else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tiredGuard, frodo));
		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));

		// The effect: any card stacked here, and not one stacked elsewhere.
		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(deckGuard));
		scn.FreepsChooseCard(axe);

		assertInZone(Zone.HAND, axe);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetStackedCards(delvings).size());
		assertTrue(scn.GetStackedCards(delvings).contains(strike));
		assertTrue(scn.GetStackedCards(everMyHeart).contains(deckGuard));
		// The Free Peoples player has acted, so the Shadow player is asked next.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionCanTakeAStackedCardThatIsNotDwarven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var delvings = scn.GetFreepsCard("delvings");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stackedTroop = scn.GetFreepsCard("troop");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(delvings);
		scn.MoveCompanionsToTable(gimli);
		// The trigger only ever stacks [dwarven] cards here, but Come Here Lad (4_43) and Suspended Palaces (6_10)
		// stack cards from the top of the draw deck, whatever they are, on a [dwarven] condition that already has
		// a card stacked on it.  So a [gondor] card and a Shadow card of my own deck can lawfully be here; they
		// are put here by cheating.  The text says "a card stacked here", which is any of them.
		scn.StackCardsOn(delvings, strike, aragorn, stackedTroop);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetStackedCards(delvings).size());
		assertEquals(0, scn.GetFreepsHandCount());

		// Gimli is the only Dwarf, so he exerts without being chosen.
		scn.FreepsUseCardAction(delvings);
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, aragorn, stackedTroop));
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.HAND, aragorn);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetStackedCards(delvings).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedOnceNoDwarfCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var delvings = scn.GetFreepsCard("delvings");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var axe = scn.GetFreepsCard("axe");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(delvings);
		scn.MoveCompanionsToTable(gimli);
		scn.StackCardsOn(delvings, strike, axe);
		// Vitality 3 with 1 wound: one exertion left.
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(delvings));

		scn.FreepsUseCardAction(delvings);
		scn.FreepsChooseCard(strike);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.HAND, strike);

		scn.ShadowPassCurrentPhaseAction();

		// Gimli is exhausted.  A card is still stacked here and Frodo could exert, but he is no Dwarf.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(1, scn.GetStackedCards(delvings).size());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsActionAvailable(delvings));
	}
}
