package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_237_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "7_237");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					// Guma, Plains Farmer and Hlafwine, Village Farmhand: [rohan] Man allies, home 4T; abilities used only on purpose
					put("guma", "4_277");
					put("hlafwine", "4_281");
					// Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions (or Eowyn)
					// can be spotted, which these boards never have
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; a skirmish ability
					// Cards for the Shadow hand (and, his own copies, for the Free Peoples hand): none is played
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("runner", "1_178");     // Goblin Runner: its text is about being played
					put("marksman", "1_176");   // Goblin Marksman: archer and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HisGoldenShieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: His Golden Shield
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot 3 [rohan] Men to make an opponent remove (1) or place a random card from hand beneath his
		 * or her draw deck. Do this once for each card in that player's hand when you play this event.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shield");

		assertEquals("His Golden Shield", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilThreeRohanMenAreSpottedAndDoesNothingAgainstAnEmptyHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(guard, merry, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(shield);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		// Two [rohan] Men (a companion and an ally), a [rohan] Hobbit who is no Man, and a [gondor] Man.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(shield));

		// A second [rohan] Man ally makes three.
		scn.MoveAlliesToTable(hlafwine);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, hlafwine);
		assertTrue(scn.FreepsPlayAvailable(shield));

		// The Shadow hand is empty: nothing is done, and nothing asked.
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPlayCard(shield);
		assertInDiscard(shield);
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerChoosesOnceForEachCardInHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var myTroop = scn.GetFreepsCard("troop");
		var myRunner = scn.GetFreepsCard("runner");
		var myMarksman = scn.GetFreepsCard("marksman");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma, hlafwine);
		scn.MoveCardsToHand(shield, myTroop, myRunner, myMarksman);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToHand(troop, runner);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		assertInHand(troop, runner, shield, myTroop, myRunner, myMarksman);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetTwilight());

		scn.FreepsPlayCard(shield);

		// Two cards in the Shadow hand (three beside the event in the Free Peoples hand): two choices, each the
		// Shadow player's.
		assertEquals(List.of("Remove 1 twilight", "Place a random card from hand beneath draw deck"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Remove 1 twilight");
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());

		assertEquals(List.of("Remove 1 twilight", "Place a random card from hand beneath draw deck"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Place a random card from hand beneath draw deck");

		// Nobody chooses the card: one of the two is at the bottom of his draw deck, and the other is still in hand.
		var placed = scn.GetShadowBottomOfDeck();
		assertTrue(List.of(troop, runner).contains(placed));
		assertInZone(Zone.DECK, placed);
		assertInHand(placed == troop ? runner : troop);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoTwilightEachOfThreeCardsGoesBeneathTheDrawDeckUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma, hlafwine);
		scn.MoveCardsToHand(shield);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToHand(troop, runner, marksman);
		scn.SetTwilight(0);
		scn.FreepsRefreshDecision();
		assertInHand(troop, runner, marksman);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(shield);

		// The number is the hand as the event was played: three, though the hand shrinks as each card goes.
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, troop, runner, marksman);
		assertEquals(Set.of(troop, runner, marksman), Set.of(scn.GetFromBottomOfShadowDeck(1),
				scn.GetFromBottomOfShadowDeck(2), scn.GetFromBottomOfShadowDeck(3)));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
