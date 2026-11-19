package com.gempukku.lotro.cards.official.set13;

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

public class Card_13_149_Tests
{

	// Sam, Bearer of Great Need 13_156 is the Ring-bearer: this Frodo cannot be one, and no Frodo who could is laid
	// beside another Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frodo", "13_149");
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound Hobbit companion, strength 3; a skirmish ability used only on purpose
					put("faramir", "4_117");    // Faramir, Son of Denethor: a Ring-bound Man companion, strength 7; a bar on the opponent's skirmish actions in his skirmishes
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: an ally; a fellowship ability used only on purpose
					put("brigand1", "4_10");    // Dunlending Brigand: strength 8, vitality 1, no text
					put("brigand2", "4_10");
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Frodo, Frenzied Fighter
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Ring-bound</b>.<br>While you can spot more minions than companions, Frodo cannot be overwhelmed unless his strength is tripled.<br><b>Skirmish:</b> If Frodo is not assigned to a skirmish, exert Frodo to have him replace a Ring-bound companion skirmishing a minion.
		*/

		var scn = GetScenario();

		var frodo = scn.GetFreepsCard("frodo");

		assertEquals("Frodo", frodo.getBlueprint().getTitle());
		assertEquals("Frenzied Fighter", frodo.getBlueprint().getSubtitle());
		assertTrue(frodo.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, frodo.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, frodo.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, frodo.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, frodo.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(frodo, Keyword.RING_BOUND));
		assertEquals(2, frodo.getBlueprint().getTwilightCost());
		assertEquals(3, frodo.getBlueprint().getStrength());
		assertEquals(4, frodo.getBlueprint().getVitality());
		assertEquals(8, frodo.getBlueprint().getResistance());
		// CR 4.2's ruling on the card: "This version of Frodo cannot become the Ring-bearer."  A deck may not start
		// with him as its Ring-bearer.
		assertFalse(frodo.getBlueprint().canStartWithRing());
	}

	@Test
	public void WithMoreMinionsThanCompanionsHeIsNotOverwhelmedAtDoubleStrengthAndAnotherCompanionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var merry = scn.GetFreepsCard("merry");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(frodo, merry);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		// Four minions against three companions (Frodo, Sam and Merry); the ally is no companion, and counted as one
		// the two numbers would be equal.
		scn.MoveMinionsToTable(brigand1, brigand2, runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, brigand1},
				new PhysicalCardImpl[]{merry, brigand2});
		scn.ShadowDeclineAssignments();

		// 8 against 3: at least double, short of triple.  Frodo loses and takes his wound.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		// Merry at the same 8 against 3 is overwhelmed.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithMoreMinionsThanCompanionsHeIsOverwhelmedAtTripleStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(frodo, merry);

		scn.StartGame();
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(brigand1, brigand2, runner1, runner2);
		// A strength that only sets the board: 8 + 1 = 9, exactly triple Frodo's 3 (8 is the one short of it, read in
		// the test above).
		scn.ApplyAdHocModifier(new StrengthModifier(null, brigand1, null, 1));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, brigand1);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(frodo);
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(9, scn.GetStrength(brigand1));
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAsManyMinionsAsCompanionsHeIsOverwhelmedAtDoubleStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var merry = scn.GetFreepsCard("merry");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(frodo, merry);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		var runner1 = scn.GetShadowCard("runner1");
		// Three minions against three companions, the ally beside them.
		scn.MoveMinionsToTable(brigand1, brigand2, runner1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, brigand1},
				new PhysicalCardImpl[]{merry, brigand2});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(frodo);
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		// Merry's skirmish is next.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void UnassignedHeExertsToReplaceARingBoundCompanionInEachOfTwoSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var faramir = scn.GetFreepsCard("faramir");
		var sam = scn.GetRingBearer();
		scn.MoveCompanionsToTable(frodo, faramir);

		scn.StartGame();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, runner1},
				new PhysicalCardImpl[]{faramir, runner2});

		// Sam, the Ring-bearer, a Ring-bound Hobbit.  Faramir, assigned and waiting, is not skirmishing.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(frodo));
		scn.FreepsUseCardAction(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner1));
		assertFalse(scn.IsCharSkirmishing(sam));
		assertTrue(scn.IsCharAssignedAgainst(faramir, runner2));
		// CR 4.2 "replace": the skirmish goes on with Frodo.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 3 against 5: Frodo loses and takes a wound.  His skirmish over, he is assigned to nothing.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		// Faramir, a Ring-bound Man of another culture.
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.FreepsActionAvailable(frodo));
		scn.FreepsUseCardAction(frodo);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner2));
		assertFalse(scn.IsCharSkirmishing(faramir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnUnboundCompanionSkirmishingIsNotReplacedAndTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(frodo, merry);

		scn.StartGame();
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(frodo));
		scn.FreepsUseCardAction(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharSkirmishingAgainst(merry, runner1));
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileHeIsAssignedAndOfferedOnceHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var faramir = scn.GetFreepsCard("faramir");
		var sam = scn.GetRingBearer();
		scn.MoveCompanionsToTable(frodo, faramir);

		scn.StartGame();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveMinionsToTable(runner1, runner2, brigand1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, runner1},
				new PhysicalCardImpl[]{frodo, runner2},
				new PhysicalCardImpl[]{faramir, brigand1});

		// Assigned to a skirmish still to come.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(frodo));
		scn.PassSkirmishActions();
		// Sam loses: The Ruling Ring offers to be put on against the wound, declined.
		scn.FreepsDeclineOptionalTrigger();

		// In his own skirmish.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(frodo));
		scn.PassSkirmishActions();

		// His skirmish is over: he is assigned to nothing.
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(frodo));
	}

	@Test
	public void NotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetRingBearer();
		scn.MoveCompanionsToTable(frodo);
		// Vitality 4 with 3 wounds.
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(frodo));
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner1);
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(frodo));
	}
}
