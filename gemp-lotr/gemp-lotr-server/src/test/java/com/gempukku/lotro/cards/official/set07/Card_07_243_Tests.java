package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_243_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("came", "7_243");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, Valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally; a fellowship ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, strength 6, Knight and nothing else
					put("merry", "103_85");     // Merry, Master Holbytla: the [rohan] Hobbit, a Man only beside 3 [rohan] companions or Eowyn
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc minion, strength 5; its text answers its own play
					put("runner2", "1_178");
					// The Shadow hand: three different cards, so that which one went is seen.
					put("troop", "1_177");      // Goblin Patrol Troop
					put("savage", "1_151");     // Uruk Savage
					put("wildmen", "4_4");      // Band of Wild Men
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorningCameStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morning Came
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [rohan] Man strength +4. Any Shadow player may place a random card from hand beneath his or her draw deck to make that Man strength +2 instead.
		*/

		var scn = GetScenario();

		var came = scn.GetFreepsCard("came");

		assertEquals("Morning Came", came.getBlueprint().getTitle());
		assertNull(came.getBlueprint().getSubtitle());
		assertFalse(came.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, came.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, came.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, came.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(came, Timeword.SKIRMISH));
		assertEquals(1, came.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesARohanManStrengthPlus4ForTheSkirmishWhenTheShadowPlayerDeclines() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var came = scn.GetFreepsCard("came");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var wildmen = scn.GetShadowCard("wildmen");
		scn.MoveCompanionsToTable(guard, knight, merry);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(came);
		assertInHand(came);

		scn.StartGame();
		scn.SetShadowHand(troop, savage, wildmen);
		assertEquals(3, scn.GetShadowHandCount());
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner1}, new PhysicalCardImpl[]{knight, runner2});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.FreepsPlayAvailable(came));
		scn.FreepsPlayCard(came);

		// A [rohan] Man companion in this skirmish and a [rohan] Man ally in none; not a [gondor] Man, the [rohan]
		// Hobbit (only two [rohan] companions in play, so no Man) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertTrue(scn.ShadowDecisionAvailable("random card"));
		scn.ShadowChooseNo();

		assertEquals(10, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(3, scn.GetShadowHandCount());
		assertInHand(troop, savage, wildmen);
		assertInDiscard(came);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the next decision of the skirmish; gone in the next skirmish.
		scn.ShadowPass();
		assertEquals(10, scn.GetStrength(guard));
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(knight);
		assertInPlay(guard);
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayPlaceARandomCardBeneathHisDeckToMakeItPlus2Instead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var came = scn.GetFreepsCard("came");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var wildmen = scn.GetShadowCard("wildmen");
		scn.MoveCompanionsToTable(guard, knight);
		scn.MoveCardsToHand(came);
		assertInHand(came);

		scn.StartGame();
		scn.SetShadowHand(troop, savage, wildmen);
		assertEquals(3, scn.GetShadowHandCount());
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner1}, new PhysicalCardImpl[]{knight, runner2});
		scn.FreepsResolveSkirmish(guard);

		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(6, scn.GetStrength(guard));
		scn.FreepsPlayCard(came);
		// The Guard is the only [rohan] Man: chosen unasked.
		assertTrue(scn.ShadowDecisionAvailable("random card"));
		scn.ShadowChooseYes();

		// 6 + 4 - 2: +2 in place of +4.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		// The card that went beneath his deck is one of the three, and the other two are still in hand.
		var bottom = scn.GetShadowBottomOfDeck();
		assertTrue(List.of(troop, savage, wildmen).contains(bottom));
		for (var card : new PhysicalCardImpl[]{troop, savage, wildmen}) {
			if (card == bottom)
				assertInZone(Zone.DECK, card);
			else
				assertInHand(card);
		}
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The +2 is gone in the next skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight);
		assertInPlay(guard);
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyShadowHandTheShadowPlayerIsNotAskedAndItIsPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var came = scn.GetFreepsCard("came");
		var guard = scn.GetFreepsCard("guard");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(came);
		assertInHand(came);

		scn.StartGame();
		scn.SetShadowHand();
		assertEquals(0, scn.GetShadowHandCount());
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner1);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		scn.FreepsPlayCard(came);

		assertFalse(scn.ShadowDecisionAvailable("random card"));
		assertEquals(10, scn.GetStrength(guard));
		assertInDiscard(came);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var came = scn.GetFreepsCard("came");
		var guard = scn.GetFreepsCard("guard");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(came);
		assertInHand(came);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(came));
	}

	@Test
	public void WithNoRohanManItIsPlayedForNothingAndTheShadowPlayerMayStillPayForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var came = scn.GetFreepsCard("came");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");
		var runner1 = scn.GetShadowCard("runner1");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var wildmen = scn.GetShadowCard("wildmen");
		// A [gondor] Man and the [rohan] Hobbit: no [rohan] Man.
		scn.MoveCompanionsToTable(knight, merry);
		scn.MoveCardsToHand(came);
		assertInHand(came);

		scn.StartGame();
		scn.SetShadowHand(troop, savage, wildmen);
		assertEquals(3, scn.GetShadowHandCount());
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, runner1);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(6, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsPlayAvailable(came));
		scn.FreepsPlayCard(came);

		// The Shadow player's "may" has a price of its own, so it is put to him whatever it will buy.
		assertTrue(scn.ShadowDecisionAvailable("random card"));
		scn.ShadowChooseYes();
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(came);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
