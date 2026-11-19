package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_007_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  With no minion in
	// play, turn 1 goes from the Shadow phase straight to the regroup phase at The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arkenstone", "31_7");
					put("emeralds", "31_3");    // Emeralds of Girion: [dwarven] artifact, support area, with a stack of its own; its abilities not used
					put("battle", "30_4");      // Battle of Azanulbizar: a [dwarven] Maneuver event
					put("lore", "30_13");       // Lore of Imladris: a [dwarven] Maneuver event of another title
					put("advice", "33_18");     // Wise Advice: an [elven] Fellowship event
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession, no event
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, vitality 4; a Fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, vitality 3; a Fellowship ability not used
					put("dain", "32_2");        // Dain Ironfoot: a [dwarven] ally; Thorin is strength +1; a start-of-regroup offer that needs a follower
					put("bats", "32_30");       // Dark Bats: [moria] minion; a Shadow ability, not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheArkenstoneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: The Arkenstone
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: <b>Fellowship:</b> Add (1) to take a card stacked here into hand.<br><b>Regroup:</b> Exert a [dwarven] companion to stack a [dwarven] event from your draw deck on the Arkenstone (limit once per phase).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arkenstone");

		assertEquals("The Arkenstone", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityAddsOneToTakeACardStackedOnTheArkenstoneIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arkenstone = scn.GetFreepsCard("arkenstone");
		var emeralds = scn.GetFreepsCard("emeralds");
		var battle = scn.GetFreepsCard("battle");
		var advice = scn.GetFreepsCard("advice");
		var lore = scn.GetFreepsCard("lore");
		scn.MoveCardsToSupportArea(arkenstone, emeralds);
		scn.StackCardsOn(arkenstone, battle, advice);
		scn.StackCardsOn(emeralds, lore);

		scn.StartGame();
		assertInZone(Zone.STACKED, battle, advice, lore);
		assertEquals(2, scn.GetStackedCards(arkenstone).size());
		assertTrue(scn.GetStackedCards(emeralds).contains(lore));
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());

		assertTrue(scn.FreepsActionAvailable(arkenstone));
		scn.FreepsUseCardAction(arkenstone);
		assertEquals(1, scn.GetTwilight());

		// The cards stacked here, of two cultures; not the one stacked on the Emeralds.
		assertTrue(scn.FreepsHasCardChoiceAvailable(battle, advice));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lore));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(advice);

		assertInHand(advice);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.GetStackedCards(arkenstone).contains(battle));
		assertEquals(1, scn.GetStackedCards(arkenstone).size());
		assertTrue(scn.GetStackedCards(emeralds).contains(lore));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingStackedOnTheArkenstoneTheTakeIsPaidForAndTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arkenstone = scn.GetFreepsCard("arkenstone");
		var emeralds = scn.GetFreepsCard("emeralds");
		var lore = scn.GetFreepsCard("lore");
		scn.MoveCardsToSupportArea(arkenstone, emeralds);
		scn.StackCardsOn(emeralds, lore);

		scn.StartGame();
		assertEquals(0, scn.GetStackedCards(arkenstone).size());
		assertTrue(scn.GetStackedCards(emeralds).contains(lore));
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());

		// The ability has a cost, and so is offered with nothing to take.
		assertTrue(scn.FreepsActionAvailable(arkenstone));
		scn.FreepsUseCardAction(arkenstone);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.GetStackedCards(emeralds).contains(lore));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakingIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arkenstone = scn.GetFreepsCard("arkenstone");
		var battle = scn.GetFreepsCard("battle");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToSupportArea(arkenstone);
		scn.StackCardsOn(arkenstone, battle);

		scn.StartGame();
		assertTrue(scn.GetStackedCards(arkenstone).contains(battle));
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arkenstone));
		assertTrue(scn.GetStackedCards(arkenstone).contains(battle));
	}

	@Test
	public void RegroupAbilityExertsADwarvenCompanionToStackADwarvenEventFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arkenstone = scn.GetFreepsCard("arkenstone");
		var battle = scn.GetFreepsCard("battle");
		var lore = scn.GetFreepsCard("lore");
		var advice = scn.GetFreepsCard("advice");
		var axe = scn.GetFreepsCard("axe");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(arkenstone);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		assertInZone(Zone.DECK, battle, lore, advice, axe);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetStackedCards(arkenstone).size());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(arkenstone));
		scn.FreepsUseCardAction(arkenstone);

		// [dwarven] companions of two titles; not Bilbo, a companion of another culture, nor Dain, a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));

		// The deck is shown first; then [dwarven] events of two titles, not a [dwarven] possession or an [elven] event.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(battle, lore));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, advice));
		// A look through a draw deck never forces a card to be taken (F-0049).
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(battle);

		assertInZone(Zone.STACKED, battle);
		assertTrue(scn.GetStackedCards(arkenstone).contains(battle));
		assertEquals(1, scn.GetStackedCards(arkenstone).size());
		assertInZone(Zone.DECK, lore, advice, axe);
		// The deck looked through is shuffled (CR 4.2 "draw deck"); the other player's is not.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedASecondTimeInTheSameRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arkenstone = scn.GetFreepsCard("arkenstone");
		var battle = scn.GetFreepsCard("battle");
		var lore = scn.GetFreepsCard("lore");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCardsToSupportArea(arkenstone);
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		assertInZone(Zone.DECK, battle, lore);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsUseCardAction(arkenstone);
		scn.FreepsChooseCard(balin);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(battle);
		assertTrue(scn.GetStackedCards(arkenstone).contains(battle));
		scn.ShadowPass();

		// Thorin can still exert and Lore of Imladris is still in the deck: the limit alone withholds it.
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertInZone(Zone.DECK, lore);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arkenstone));
	}

	@Test
	public void RegroupAbilityIsOfferedAgainInTheNextRegroupPhaseOfTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arkenstone = scn.GetFreepsCard("arkenstone");
		var battle = scn.GetFreepsCard("battle");
		var lore = scn.GetFreepsCard("lore");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCardsToSupportArea(arkenstone);
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		assertInZone(Zone.DECK, battle, lore);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsUseCardAction(arkenstone);
		scn.FreepsChooseCard(balin);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(battle);
		assertTrue(scn.GetStackedCards(arkenstone).contains(battle));
		assertEquals(1, scn.GetWoundsOn(balin));

		// A second move this turn, to Rivendell (site 3), and with no minion the next regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetCurrentSiteNumber());

		assertTrue(scn.FreepsActionAvailable(arkenstone));
		scn.FreepsUseCardAction(arkenstone);
		scn.FreepsChooseCard(thorin);
		assertEquals(1, scn.GetWoundsOn(thorin));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(lore);
		assertTrue(scn.GetStackedCards(arkenstone).contains(lore));
		assertEquals(2, scn.GetStackedCards(arkenstone).size());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoDwarvenEventInTheDrawDeckTheStackIsPaidForAndStacksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arkenstone = scn.GetFreepsCard("arkenstone");
		var battle = scn.GetFreepsCard("battle");
		var lore = scn.GetFreepsCard("lore");
		var advice = scn.GetFreepsCard("advice");
		var axe = scn.GetFreepsCard("axe");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCardsToSupportArea(arkenstone);
		scn.MoveCompanionsToTable(balin);
		// Every [dwarven] event of the map out of the deck.
		scn.MoveCardsToDiscard(battle, lore);

		scn.StartGame();
		assertInDiscard(battle, lore);
		assertInZone(Zone.DECK, advice, axe);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsActionAvailable(arkenstone));
		scn.FreepsUseCardAction(arkenstone);
		// Balin is the one [dwarven] companion: he exerts with no question.
		assertEquals(1, scn.GetWoundsOn(balin));
		// The deck is shown, with nothing in it to choose.
		scn.FreepsDismissRevealedCards();

		assertEquals(0, scn.GetStackedCards(arkenstone).size());
		assertInZone(Zone.DECK, advice, axe);
		assertInDiscard(battle, lore);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileTheOnlyDwarvenCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arkenstone = scn.GetFreepsCard("arkenstone");
		var battle = scn.GetFreepsCard("battle");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(arkenstone);
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		assertInZone(Zone.DECK, battle);
		scn.SkipToPhase(Phase.REGROUP);
		// Vitality 3 with 2 wounds: Balin cannot exert.  Bilbo, a companion of another culture, can.
		scn.AddWoundsToChar(balin, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arkenstone));
	}
}
