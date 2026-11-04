package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("begone", "15_27");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, vitality 4; a standing strength text only
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion, vitality 4; +2 only with 3 Ents
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally who is no companion; a Fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a Maneuver ability used only on purpose

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetLoneMinionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("begone", "15_27");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] Man who pays Defend It and Hope
					put("defend", "4_115");     // Defend It and Hope: Maneuver, exert a [gondor] Man to wound a minion once

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, killed by one wound
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeGoneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Be Gone!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>. Exert your [gandalf] companion X times to make a minion strength -2 until the regroup phase, for each time that [gandalf] companion exerted.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("begone");

		assertEquals("Be Gone!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingYourGandalfCompanionOnceMakesAMinionStrengthMinus2UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var begone = scn.GetFreepsCard("begone");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(begone);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, raiders);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(raiders));
		assertTrue(scn.FreepsPlayAvailable(begone));
		scn.FreepsPlayCard(begone);

		// Your [gandalf] companions, a Wizard and an Ent; not the [gandalf] ally, nor companions of other cultures.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, aragorn, frodo));
		scn.FreepsChooseCard(gandalf);

		scn.FreepsChoose("1");
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Any minion, of either culture and race; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, raiders));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, avenger));
		scn.FreepsChooseCard(troop);

		assertEquals(11, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(raiders));
		assertInZone(Zone.DISCARD, begone);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{avenger, raiders});

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(11, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// In a skirmish that follows one already resolved, it still holds.
		scn.FreepsResolveSkirmish(avenger);
		assertEquals(11, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void ExertingTwiceMakesOneMinionStrengthMinus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var begone = scn.GetFreepsCard("begone");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(begone);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, raiders);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.FreepsPlayCard(begone);

		// The lone [gandalf] companion is chosen unasked.  Vitality 4: he can exert at most 3 times.
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(2, scn.GetWoundsOn(avenger));

		scn.FreepsChooseCard(troop);
		scn.FreepsChooseCard(troop);

		// 13 - 2 - 2
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(raiders));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ExertingTwiceMayMakeTwoMinionsStrengthMinus2Each() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var begone = scn.GetFreepsCard("begone");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(begone);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, raiders);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(begone);
		scn.FreepsChoose("2");
		assertEquals(2, scn.GetWoundsOn(gandalf));

		scn.FreepsChooseCard(troop);
		scn.FreepsChooseCard(raiders);

		assertEquals(11, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(raiders));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheNumberOfExertionsStartsAtOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var begone = scn.GetFreepsCard("begone");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(begone);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(begone);

		// A cost of exerting X times cannot be paid by exerting 0 times (CR 4.2 "exert"; Q-0076).
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsDecisionAvailable("Choose how many times to exert"));
	}

	@Test
	public void IsNotPlayableInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var begone = scn.GetFreepsCard("begone");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(begone);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(begone));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(begone));
	}

	@Test
	public void IsNotPlayableWhileYourOnlyGandalfCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var begone = scn.GetFreepsCard("begone");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(begone);

		scn.StartGame();
		// Vitality 4 with 3 wounds; Aragorn, of another culture, could still exert.
		scn.AddWoundsToChar(gandalf, 3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(begone));
	}

	@Test
	public void WithNoMinionLeftItIsStillPlayedAndTheExertionPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLoneMinionScenario();

		var begone = scn.GetFreepsCard("begone");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(begone, defend);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Defend It and Hope kills the only minion.
		scn.FreepsPlayCard(defend);
		assertInZone(Zone.DISCARD, runner);
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(begone));
		scn.FreepsPlayCard(begone);
		scn.FreepsChoose("1");

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.DISCARD, begone);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
