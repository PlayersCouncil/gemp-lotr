package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_216_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rises", "11_216");
					put("rises2", "11_216");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text, strength 13, vitality 3
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4; a maneuver ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: damage +1, strength 6; a skirmish ability never used here
					put("first", "7_92");       // First Level: Maneuver, exert a [gondor] Man to transfer it from the support area to a minion
					put("third", "7_125");      // Third Level: Skirmish, exert a [gondor] Man to transfer it from the support area to a minion skirmishing a [gondor] Man
					put("homestead", "13_42");  // Traveler's Homestead: Maneuver, discard it from play to discard a Shadow condition from play
					put("phial", "10_13");      // Phial of Galadriel, Star-glass: on a Hobbit; Skirmish: remove the game text of a minion skirmishing bearer until the regroup phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AShadowRisesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: A Shadow Rises
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Maneuver:</b> Remove (5) to make this condition a <b>fierce</b> [wraith] minion until the start of the regroup phase that has 10 strength and 3 vitality, and cannot bear other cards. This card is still a condition.<br>At the start of the regroup phase, remove all tokens from here.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rises");

		assertEquals("A Shadow Rises", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovingFiveMakesItAFierceMinionOfStrength10AndVitality3UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rises = scn.GetShadowCard("rises");
		var rises2 = scn.GetShadowCard("rises2");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(rises, rises2);
		assertInZone(Zone.SUPPORT, rises, rises2);
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, rises, rises2);
		assertFalse(scn.IsType(rises, CardType.MINION));
		assertFalse(scn.HasKeyword(rises, Keyword.FIERCE));
		assertEquals(0, scn.GetStrength(rises));
		assertEquals(0, scn.GetVitality(rises));
		assertTrue(scn.ShadowActionAvailable(rises));
		scn.ShadowUseCardAction(rises);

		assertEquals(0, scn.GetTwilight());
		// Out of the support area, among the minions.
		assertInZone(Zone.SHADOW_CHARACTERS, rises);
		assertTrue(scn.IsType(rises, CardType.MINION));
		assertTrue(scn.HasKeyword(rises, Keyword.FIERCE));
		assertEquals(10, scn.GetStrength(rises));
		assertEquals(3, scn.GetVitality(rises));
		// The other copy is still a plain condition.
		assertInZone(Zone.SUPPORT, rises2);
		assertFalse(scn.IsType(rises2, CardType.MINION));
		assertFalse(scn.HasKeyword(rises2, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{boromir, rises});
		// Aragorn's 8 loses to 13.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The skirmish that follows: still the minion.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, rises));
		assertTrue(scn.IsType(rises, CardType.MINION));
		assertEquals(10, scn.GetStrength(rises));
		// Boromir's 7 loses to 10.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));

		// It is fierce, and is fought in the fierce round.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, rises);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, rises));
		assertTrue(scn.HasKeyword(rises, Keyword.FIERCE));
		assertEquals(10, scn.GetStrength(rises));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The regroup phase has started: back in the support area, a condition and nothing more.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, rises);
		assertFalse(scn.IsType(rises, CardType.MINION));
		assertTrue(scn.IsType(rises, CardType.CONDITION));
		assertFalse(scn.HasKeyword(rises, Keyword.FIERCE));
		assertEquals(0, scn.GetStrength(rises));
		assertEquals(0, scn.GetVitality(rises));
	}

	@Test
	public void NotOfferedWithFourTwilightNorAgainWhileItIsTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rises = scn.GetShadowCard("rises");
		var rises2 = scn.GetShadowCard("rises2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(rises, rises2);
		assertInZone(Zone.SUPPORT, rises, rises2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rises));

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(rises));
		scn.ShadowUseCardAction(rises);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.IsType(rises, CardType.MINION));

		// With (5) still in the pool, the minion is not offered its ability again; the copy in the support area is.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rises));
		assertTrue(scn.ShadowActionAvailable(rises2));
	}

	@Test
	public void AsAMinionInTheManeuverPhaseFirstLevelIsNotTransferredToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rises = scn.GetShadowCard("rises");
		var troop = scn.GetShadowCard("troop");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var first = scn.GetFreepsCard("first");
		scn.MoveCardsToSupportArea(rises, first);
		assertInZone(Zone.SUPPORT, rises, first);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, hound);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowUseCardAction(rises);
		assertTrue(scn.IsType(rises, CardType.MINION));

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(first));
		scn.FreepsUseCardAction(first);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, hound));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rises));
		scn.FreepsChooseCard(troop);

		assertAttachedTo(first, troop);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AsAMinionInASkirmishThirdLevelIsNotTransferredToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rises = scn.GetShadowCard("rises");
		var troop = scn.GetShadowCard("troop");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var third = scn.GetFreepsCard("third");
		scn.MoveCardsToSupportArea(rises, third);
		assertInZone(Zone.SUPPORT, rises, third);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, hound);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowUseCardAction(rises);
		assertTrue(scn.IsType(rises, CardType.MINION));

		// All three minions on Aragorn: the Free Peoples player assigns one, the Shadow player the other two.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rises);
		scn.ShadowAssignToMinions(aragorn, troop, hound);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(third));
		scn.FreepsUseCardAction(third);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, hound));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rises));
		scn.FreepsChooseCard(troop);

		assertAttachedTo(third, troop);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AsAMinionItIsStillAConditionAndIsDiscardedAsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rises = scn.GetShadowCard("rises");
		var rises2 = scn.GetShadowCard("rises2");
		var troop = scn.GetShadowCard("troop");
		var homestead = scn.GetFreepsCard("homestead");
		scn.MoveCardsToSupportArea(rises, rises2, homestead);
		assertInZone(Zone.SUPPORT, rises, rises2, homestead);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowUseCardAction(rises);
		assertTrue(scn.IsType(rises, CardType.MINION));
		assertTrue(scn.IsType(rises, CardType.CONDITION));

		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);
		// A Shadow condition: the minion it now is, and the copy in the support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rises, rises2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		scn.FreepsChooseCard(rises);

		assertInDiscard(rises, homestead);
		assertInZone(Zone.SUPPORT, rises2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtTheStartOfEachRegroupPhaseItsWoundsAreRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rises = scn.GetShadowCard("rises");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(rises);
		assertInZone(Zone.SUPPORT, rises);
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		// Gimli at 11 beats its 10 and, with damage +1, puts two wounds on it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 5));

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowUseCardAction(rises);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gimli, rises});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(rises));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(rises));

		// The fierce round, no regroup phase yet: the wounds stay.  Boromir's 7 loses to 10.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, rises);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(2, scn.GetWoundsOn(rises));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The regroup phase starts: both wounds are gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, rises);
		assertEquals(0, scn.GetWoundsOn(rises));

		// A second move, and again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowUseCardAction(rises);
		// Fresh: 3 vitality, nothing left over from the first time.
		assertEquals(0, scn.GetWoundsOn(rises));
		assertEquals(3, scn.GetVitality(rises));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gimli, rises});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(rises));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, rises);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(2, scn.GetWoundsOn(rises));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(rises));
	}

	@Test
	public void SkirmishingABearerOfThePhialItLosesFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rises = scn.GetShadowCard("rises");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var phial = scn.GetFreepsCard("phial");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(rises);
		assertInZone(Zone.SUPPORT, rises);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, phial);
		assertAttachedTo(phial, frodo);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowUseCardAction(rises);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{frodo, rises});
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.HasKeyword(rises, Keyword.FIERCE));
		assertTrue(scn.FreepsActionAvailable(phial));
		scn.FreepsUseCardAction(phial);

		// The one minion skirmishing Frodo is chosen unasked.
		assertInDiscard(phial);
		assertFalse(scn.HasKeyword(rises, Keyword.FIERCE));
		assertInZone(Zone.SHADOW_CHARACTERS, rises);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
