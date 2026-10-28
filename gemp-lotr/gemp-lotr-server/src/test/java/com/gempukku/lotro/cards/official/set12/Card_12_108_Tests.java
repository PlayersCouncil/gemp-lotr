package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("castout", "12_108");
					put("castout2", "12_108");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; "Valiant." is his whole text
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, vitality 2; a regroup ability used only on purpose (its play requirement is not reached: cheated in)
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; "Knight." is his whole text
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn
					put("mount", "4_287");      // Rider's Mount: [rohan] mount on a [rohan] Man; its trigger is at the start of a skirmish
					put("sword", "4_291");      // Sword of Rohan: [rohan] hand weapon on a [rohan] Man, no mount
					put("horse", "4_283");      // Horse of Rohan: [rohan] mount any Man may bear; its trigger is at the start of a skirmish

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3; no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, so always exhausted; its play trigger is not reached (cheated in)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CastOutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Cast Out
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Toil 2</b>. <helper>(For each [rohan] character you exert when playing this, its twilight cost is -2.)</helper><br><b>Maneuver:</b> Spot a [rohan] Man and discard this condition to exert a minion.<br><b>Maneuver:</b> Spot a mounted [rohan] Man and discard this condition to return an exhausted minion to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("castout");

		assertEquals("Cast Out", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachRohanCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castout = scn.GetFreepsCard("castout");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, knight);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(castout);

		scn.StartGame();
		assertInHand(castout);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsPlayCard(castout);
		// Each [rohan] character, the ally among them; nobody need be exerted, a Free Peoples card adding its cost.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertInZone(Zone.SUPPORT, castout);
		// 4 less 2 for the one exertion
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingARohanManItIsDiscardedToExertAMinionWhoCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castout = scn.GetFreepsCard("castout");
		var castout2 = scn.GetFreepsCard("castout2");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(castout, castout2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, castout, castout2);

		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(castout, "Exert a minion"));
		// The Guard bears no mount.
		assertFalse(scn.FreepsActionAvailable(castout, "Return exhausted minion"));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsUseCardAction(castout, "Exert a minion");
		assertInZone(Zone.DISCARD, castout);
		assertInZone(Zone.SUPPORT, castout2);

		// Minions of two cultures and races; not the Runner, of vitality 1 and so exhausted, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ARohanHobbitAndAGondorManAreNoRohanManAndARohanManAllyIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castout = scn.GetFreepsCard("castout");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(merry, knight);
		scn.MoveCardsToSupportArea(castout);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, castout);

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(castout, "Exert a minion"));

		scn.MoveAlliesToTable(herdsman);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(castout, "Exert a minion"));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The Savage is the one minion, and is chosen unasked.
		scn.FreepsUseCardAction(castout, "Exert a minion");
		assertInZone(Zone.DISCARD, castout);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithEveryMinionExhaustedTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castout = scn.GetFreepsCard("castout");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(castout);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, castout);

		scn.MoveMinionsToTable(savage, runner);
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(castout, "Exert a minion"));

		scn.FreepsUseCardAction(castout, "Exert a minion");
		assertInZone(Zone.DISCARD, castout);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SpottingAMountedRohanManItIsDiscardedToReturnAnExhaustedMinionToItsOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castout = scn.GetFreepsCard("castout");
		var castout2 = scn.GetFreepsCard("castout2");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard, knight);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToSupportArea(castout, castout2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(guard, mount.getAttachedTo());
		assertInZone(Zone.SUPPORT, castout, castout2);
		scn.AddWoundsToChar(knight, 2);
		assertEquals(2, scn.GetWoundsOn(knight));

		scn.MoveMinionsToTable(savage, troop, runner);
		scn.AddWoundsToChar(savage, 2);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(castout, "Return exhausted minion"));

		scn.FreepsUseCardAction(castout, "Return exhausted minion");
		assertInZone(Zone.DISCARD, castout);
		assertInZone(Zone.SUPPORT, castout2);

		// The exhausted minions, of two cultures and races; not the Troop, wounded and not exhausted, nor the
		// exhausted Knight.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, knight));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertInZone(Zone.HAND, savage);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, runner, troop);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnUnmountedRohanManAndAMountedGondorManDoNotLetItReturnAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castout = scn.GetFreepsCard("castout");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var mount = scn.GetFreepsCard("mount");
		var knight = scn.GetFreepsCard("knight");
		var horse = scn.GetFreepsCard("horse");
		scn.MoveCompanionsToTable(guard, knight);
		scn.AttachCardsTo(guard, sword);
		scn.AttachCardsTo(knight, horse);
		scn.MoveCardsToSupportArea(castout);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertEquals(guard, sword.getAttachedTo());
		assertEquals(knight, horse.getAttachedTo());
		assertInZone(Zone.SUPPORT, castout);

		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// The Guard is a [rohan] Man, so the other ability is offered.
		assertTrue(scn.FreepsActionAvailable(castout, "Exert a minion"));
		assertFalse(scn.FreepsActionAvailable(castout, "Return exhausted minion"));

		scn.AttachCardsTo(guard, mount);
		assertEquals(guard, mount.getAttachedTo());
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(castout, "Return exhausted minion"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AMountedRohanAllyIsSpottedAndWithNoExhaustedMinionNothingIsReturned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var castout = scn.GetFreepsCard("castout");
		var herdsman = scn.GetFreepsCard("herdsman");
		var mount = scn.GetFreepsCard("mount");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(herdsman, mount);
		scn.MoveCardsToSupportArea(castout);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertEquals(herdsman, mount.getAttachedTo());
		assertInZone(Zone.SUPPORT, castout);

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(castout, "Return exhausted minion"));

		scn.FreepsUseCardAction(castout, "Return exhausted minion");
		assertInZone(Zone.DISCARD, castout);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
