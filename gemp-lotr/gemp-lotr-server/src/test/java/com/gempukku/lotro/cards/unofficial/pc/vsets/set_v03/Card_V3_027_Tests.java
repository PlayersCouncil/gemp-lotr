package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_027_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("king", "103_27");
					// Dead Man of Dunharrow: a [gondor] Wraith, vitality 3; his text is about being played
					put("deadman1", "10_27");
					put("deadman2", "10_27");
					put("oathbreaker", "8_41");  // Oathbreaker: a [gondor] Wraith of another title, vitality 3; a skirmish ability that needs 3 cards in hand
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] companion who is no Wraith; a maneuver ability never used here
					put("knight", "5_35");       // Gondorian Knight: a companion to play from hand for (2), with no requirement and no text but "Knight."

					put("lights", "6_99");       // Corpse Lights: a Wraith who is a [sauron] minion, vitality 2; a skirmish ability only
					// Morgul Slayer: "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					put("slayer1", "3_93");
					put("slayer2", "3_93");
					put("troop", "1_177");       // Goblin Patrol Troop: a minion with no text, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KingoftheDeadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: King of the Dead, Dead Keeper
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: Enduring.  To play, add 2 threats.
		 * 	Each time King of the Dead is killed, heal every Wraith.
		 * 	Assignment: Exert King of the Dead and restore a Wraith to heal that Wraith.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("king");

		assertEquals("King of the Dead", card.getBlueprint().getTitle());
		assertEquals("Dead Keeper", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilThereIsRoomForBothThreatsAndThenCostsFourAndAddsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(king, knight);
		// Frodo and Aragorn: room for 2 threats, and 1 is there already. The King is not yet a companion while
		// his cost is being paid, so he makes no room for himself.
		scn.AddThreats(1);

		scn.StartGame();

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
		assertFalse(scn.FreepsPlayAvailable(king));

		scn.FreepsPlayCard(knight);

		// A third companion: room for 3, so both threats now fit.
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(king));

		scn.FreepsPlayCard(king);

		// 1 + 2
		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, king);
		assertEquals(0, scn.GetWoundsOn(king));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeTheKingIsKilledEveryWraithIsHealedOnceAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lights = scn.GetShadowCard("lights");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(king, deadman1, oathbreaker, aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Minions put down in the regroup phase, so that nothing has been fought.
		scn.MoveMinionsToTable(lights, slayer1, slayer2, troop);
		// Vitality 4 with 3 wounds
		scn.AddWoundsToChar(king, 3);
		scn.AddWoundsToChar(deadman1, 2);
		scn.AddWoundsToChar(oathbreaker, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(lights, 1);
		scn.AddWoundsToChar(troop, 1);
		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(king);
		// The rule that follows a death and the King's own text answer the same wound, and the Free Peoples
		// player says which comes first. With no threats on the dead pile it makes no difference.
		scn.FreepsResolveActionOrder("King of the Dead");

		assertInZone(Zone.DEAD, king);
		// One wound off each Wraith: two [gondor] companions and a [sauron] minion.
		assertEquals(1, scn.GetWoundsOn(deadman1));
		assertEquals(0, scn.GetWoundsOn(oathbreaker));
		assertEquals(0, scn.GetWoundsOn(lights));
		// A [gondor] Man, a Hobbit, and two Orcs, one of them wounded by his own exertion.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The King is put back among the companions by the rig (in play, I Call on You 103_24 plays a Wraith from
		// the dead pile), wounded as before, and killed a second time.
		scn.MoveCompanionsToTable(king);
		scn.AddWoundsToChar(king, 3);
		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(king);
		scn.FreepsResolveActionOrder("King of the Dead");

		assertInZone(Zone.DEAD, king);
		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void KilledWithThreatsOnTheDeadPileHisHealingMayBeTakenBeforeTheirWoundsArePlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var slayer1 = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(king, deadman1, oathbreaker);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer1);
		// Vitality 4 with 3 wounds
		scn.AddWoundsToChar(king, 3);
		scn.AddWoundsToChar(deadman1, 1);
		// The two threats the King adds when he is played.
		scn.AddThreats(2);
		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(king);

		// A companion's death turns the threats into wounds, and the King's death heals: the Free Peoples
		// player says which comes first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(1, scn.GetWoundsOn(deadman1));
		scn.FreepsResolveActionOrder("King of the Dead");

		// Healed first; then two wounds for the two threats, both put on the Dead Man, which nothing heals.
		assertInZone(Zone.DEAD, king);
		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertTrue(scn.FreepsHasCardChoiceAvailable(deadman1, oathbreaker));
		scn.FreepsChooseCard(deadman1);
		scn.FreepsChooseCard(deadman1);

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(deadman1));
		assertEquals(0, scn.GetWoundsOn(oathbreaker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void KilledWithThreatsOnTheDeadPileTheirWoundsMayBePlacedBeforeHisHealing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var slayer1 = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(king, deadman1, oathbreaker);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer1);
		// Vitality 4 with 3 wounds
		scn.AddWoundsToChar(king, 3);
		scn.AddWoundsToChar(deadman1, 1);
		// The two threats the King adds when he is played.
		scn.AddThreats(2);
		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(king);

		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(1, scn.GetWoundsOn(deadman1));
		assertEquals(0, scn.GetWoundsOn(oathbreaker));
		// The rule first: the threats become wounds before anybody is healed.
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DEAD, king);
		assertEquals(1, scn.GetWoundsOn(deadman1));
		assertTrue(scn.FreepsHasCardChoiceAvailable(deadman1, oathbreaker));
		scn.FreepsChooseCard(oathbreaker);
		// One threat wound placed, and the Dead Man is still as he was.
		assertEquals(1, scn.GetWoundsOn(oathbreaker));
		assertEquals(1, scn.GetWoundsOn(deadman1));
		scn.FreepsChooseCard(oathbreaker);

		// Both wounds went on the Oathbreaker, 0 to 2; then the King's healing took one off each Wraith.
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(oathbreaker));
		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherWraithBeingKilledHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var slayer1 = scn.GetShadowCard("slayer1");
		scn.MoveCompanionsToTable(king, deadman1, oathbreaker);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer1);
		scn.AddWoundsToChar(king, 1);
		// Vitality 3 with 2 wounds
		scn.AddWoundsToChar(deadman1, 2);
		scn.AddWoundsToChar(oathbreaker, 1);
		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(deadman1);

		assertInZone(Zone.DEAD, deadman1);
		assertInZone(Zone.FREE_CHARACTERS, king);
		assertEquals(1, scn.GetWoundsOn(king));
		assertEquals(1, scn.GetWoundsOn(oathbreaker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExertsTheKingAndRestoresAHinderedWraithOfEitherSideToHealIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lights = scn.GetShadowCard("lights");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(king, deadman1, deadman2, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, troop);
		scn.AddWoundsToChar(deadman1, 2);
		scn.AddWoundsToChar(deadman2, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(lights, 1);
		// Hindered: a [gondor] Wraith, a Wraith who is a minion, and a companion who is no Wraith. The second
		// Dead Man is wounded and not hindered.
		scn.HinderCard(deadman1, lights, aragorn);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(king));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(king));
		assertTrue(scn.IsHindered(deadman1));
		assertEquals(2, scn.GetWoundsOn(deadman1));
		assertTrue(scn.FreepsActionAvailable(king));

		scn.FreepsUseCardAction(king);

		assertTrue(scn.FreepsHasCardChoiceAvailable(deadman1, lights));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(deadman2, aragorn, king, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(deadman1);

		assertEquals(1, scn.GetWoundsOn(king));
		assertFalse(scn.IsHindered(deadman1));
		assertEquals(1, scn.GetWoundsOn(deadman1));
		// Nobody else is restored or healed.
		assertTrue(scn.IsHindered(lights));
		assertEquals(1, scn.GetWoundsOn(lights));
		assertTrue(scn.IsHindered(aragorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(deadman2));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());

		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(king));
		scn.FreepsUseCardAction(king);

		// The one hindered Wraith left is the minion, taken without asking.
		assertEquals(2, scn.GetWoundsOn(king));
		assertFalse(scn.IsHindered(lights));
		assertEquals(0, scn.GetWoundsOn(lights));

		scn.ShadowPass();

		// The King could exert once more, but the only hindered card left is no Wraith, and the wounded Wraith
		// is not hindered.
		assertFalse(scn.IsExhausted(king));
		assertTrue(scn.IsHindered(aragorn));
		assertEquals(1, scn.GetWoundsOn(deadman2));
		assertFalse(scn.FreepsActionAvailable(king));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnUnwoundedWraithMayBeRestoredAndAnExhaustedKingCannotPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(king, deadman1, oathbreaker);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 4 with 2 wounds: one exertion left
		scn.AddWoundsToChar(king, 2);
		scn.HinderCard(deadman1, oathbreaker);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertTrue(scn.FreepsActionAvailable(king));

		scn.FreepsUseCardAction(king);
		assertTrue(scn.FreepsHasCardChoiceAvailable(deadman1, oathbreaker));
		scn.FreepsChooseCard(deadman1);

		// Restored, with nothing to heal.
		assertEquals(3, scn.GetWoundsOn(king));
		assertFalse(scn.IsHindered(deadman1));
		assertEquals(0, scn.GetWoundsOn(deadman1));
		assertTrue(scn.IsHindered(oathbreaker));

		scn.ShadowPass();

		// A hindered Wraith is still there, and the King cannot exert.
		assertTrue(scn.IsExhausted(king));
		assertFalse(scn.FreepsActionAvailable(king));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
