package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halflings", "12_124");
					put("halflings2", "12_124");
					put("aragorn", "11_54");    // Aragorn, Strider: [gondor] Man companion, strength 8, resistance 8; each move adds (1)
					put("scout", "11_18");      // Elven Scout: [elven] Elf companion, strength 6, resistance 7; muster only beside another Elf
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, resistance 6; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit companion, strength 3, resistance 5; a fellowship ability used only on purpose
					put("cloak", "4_303");      // Frodo's Cloak: on Frodo; away from sites 1T to 4T, "Skirmish: discard this possession to remove a burden and heal Frodo"
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its text is on being played
					put("runner2", "1_178");
					put("whelp", "7_202");      // Morgul Whelp: [wraith] Orc, strength 3 while the Ring-bearer has no wound
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges as the Ring-bearer: a Dwarf, so the fellowship need hold no Hobbit.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halflings", "12_124");
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ALLY; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LongLivetheHalflingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Long Live the Halflings
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a Hobbit.<br><b>Skirmish:</b> If your Ring-bearer is assigned to a skirmish, discard this condition to make each companion who has resistance 7 or more strength +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var halflings = scn.GetFreepsCard("halflings");

		assertEquals("Long Live the Halflings", halflings.getBlueprint().getTitle());
		assertNull(halflings.getBlueprint().getSubtitle());
		assertFalse(halflings.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, halflings.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, halflings.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, halflings.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(halflings, Keyword.SUPPORT_AREA));
		assertEquals(2, halflings.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoHobbitToSpotItCannotBePlayedAndWithAGondorHobbitItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var halflings = scn.GetFreepsCard("halflings");
		var smeagol = scn.GetFreepsCard("smeagol");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(halflings);
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();

		// A Dwarf Ring-bearer and a [shire] companion who is no Hobbit.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(halflings));

		scn.MoveCompanionsToTable(pippin);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(halflings));
		scn.FreepsPlayCard(halflings);
		assertInZone(Zone.SUPPORT, halflings);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitAllyIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var halflings = scn.GetFreepsCard("halflings");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(halflings);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(halflings));
		scn.FreepsPlayCard(halflings);
		assertInZone(Zone.SUPPORT, halflings);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void UsedWhileTheRingBearerWaitsItMakesEachCompanionOfResistance7OrMoreStrongerUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halflings = scn.GetFreepsCard("halflings");
		var halflings2 = scn.GetFreepsCard("halflings2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var scout = scn.GetFreepsCard("scout");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn, scout, gimli, sam);
		scn.MoveCardsToSupportArea(halflings, halflings2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, halflings, halflings2);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{frodo, runner2});

		// Aragorn's skirmish first; Frodo is assigned to the one after it.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetBurdens());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(8, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetResistance(scout));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));
		// Frodo is 3, + 1 for The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(halflings));
		scn.FreepsUseCardAction(halflings);

		// The companions of resistance 7 or more, fighting, waiting or in no skirmish; not those of 6 or 5, nor
		// a minion.
		assertInDiscard(halflings);
		assertInZone(Zone.SUPPORT, halflings2);
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(scout));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(runner));

		// Aragorn, 10, beats the Runner; in the next skirmish the bonus holds.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(scout));
		assertEquals(10, scn.GetStrength(aragorn));

		// Frodo, 6, beats the second Runner; at the regroup phase the bonus is gone.
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(scout));
	}

	@Test
	public void WithTheRingBearerUnassignedItIsNotOfferedThoughSamIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halflings = scn.GetFreepsCard("halflings");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(halflings);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, halflings);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{sam, runner2});

		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(halflings));
		scn.PassSkirmishActions();

		// Sam, a Ring-bound companion who is not the Ring-bearer, in his own skirmish.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishing(sam));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(halflings));
		assertInZone(Zone.SUPPORT, halflings);
	}

	@Test
	public void InTheRingBearersOwnSkirmishItIsOfferedAndInOneAfterItNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halflings = scn.GetFreepsCard("halflings");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var whelp = scn.GetShadowCard("whelp");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(halflings);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, halflings);
		scn.MoveMinionsToTable(whelp, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, whelp}, new PhysicalCardImpl[]{aragorn, runner});

		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(halflings));
		// Frodo, 4, beats the Whelp, 3.
		scn.PassSkirmishActions();
		assertInDiscard(whelp);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(halflings));
		assertInZone(Zone.SUPPORT, halflings);
	}

	@Test
	public void ACompanionWhoReachesResistance7AfterItIsUsedIsNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halflings = scn.GetFreepsCard("halflings");
		var frodo = scn.GetRingBearer();
		var cloak = scn.GetFreepsCard("cloak");
		var aragorn = scn.GetFreepsCard("aragorn");
		var scout = scn.GetFreepsCard("scout");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn, scout);
		scn.AttachCardsTo(frodo, cloak);
		scn.MoveCardsToSupportArea(halflings);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(cloak, frodo);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{frodo, runner2});

		scn.FreepsResolveSkirmish(aragorn);
		// One burden: Aragorn 8 - 1, the Scout 7 - 1.
		assertEquals(1, scn.GetBurdens());
		assertEquals(7, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(scout));
		scn.FreepsUseCardAction(halflings);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(scout));

		// The Shadow player passes; Frodo's Cloak takes the burden off, and the Scout is resistance 7 from now on.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(cloak));
		scn.FreepsUseCardAction(cloak);
		assertEquals(0, scn.GetBurdens());
		assertEquals(7, scn.GetResistance(scout));
		// Only the companions there were when it was used are made stronger.
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
