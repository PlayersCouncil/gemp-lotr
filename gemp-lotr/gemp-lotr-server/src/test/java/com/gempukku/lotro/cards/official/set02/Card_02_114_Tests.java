package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "2_114");
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3, vitality 4; his text needs a weapon
					put("merry", "1_303");
					put("worker1", "3_62");     // Isengard Worker: strength 5, vitality 3; a Regroup ability used only on purpose
					put("worker2", "3_62");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: [shire], strength 3, vitality 4
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "2_114");
					// Frodo, Frenzied Fighter: the one Frodo who cannot bear the Ring, so the one who may be in play
					// beside another Ring-bearer; strength 3, and his texts want more minions than companions, or a
					// skirmish he is not in
					put("frodo", "13_149");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Sam, Proper Poet
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Signet: Gandalf
		 * Game Text: <b>Maneuver:</b> Exert Sam twice to make him <b>defender +1</b> until the regroup phase.<br><b>Response:</b> If Frodo dies, make Sam the <b>Ring-bearer (resistance 5)</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Proper Poet", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		// Every Sam is Ring-bound by rule (CR 4.2 "Ring-bound"), though this one does not print it.
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void ManeuverAbilityExertsSamTwiceToMakeHimDefenderPlusOneUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var worker1 = scn.GetShadowCard("worker1");
		var worker2 = scn.GetShadowCard("worker2");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();
		// A Maneuver ability: not in the fellowship phase.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sam));

		scn.MoveMinionsToTable(worker1, worker2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(sam, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(merry, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(frodo, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, worker1}, new PhysicalCardImpl[]{sam, worker2});

		// Merry's skirmish first: 3 against 5, he takes a wound.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));

		// In the skirmish that follows one resolved, Sam is still defender +1.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DEFENDER));

		// 3 against 5: Sam takes a third wound, and the regroup phase begins.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(sam, Keyword.DEFENDER));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhenSamCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var worker1 = scn.GetShadowCard("worker1");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 2);
		assertEquals(2, scn.GetWoundsOn(sam));

		scn.MoveMinionsToTable(worker1);
		scn.SkipToPhase(Phase.MANEUVER);

		// Vitality 4 with 2 wounds: he can exert once, not twice.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sam));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertFalse(scn.HasKeyword(sam, Keyword.DEFENDER));
	}

	@Test
	public void WhenFrodoIsOverwhelmedSamCanBecomeTheRingBearerWithNoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		assertSame(frodo, scn.GetRingBearer());
		assertAttachedTo(ring, frodo);
		assertEquals(2, scn.GetBurdens());
		// 5 less 2 burdens
		assertEquals(3, scn.GetResistance(sam));

		// 3 + 1 for the Ring against 13: Frodo is overwhelmed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertAttachedTo(ring, sam);
		// The burdens went with Frodo.
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(sam));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheResponseWhenFrodoDiesLosesTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertFalse(scn.GameIsFinished());

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnotherCompanionDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, troop);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// 3 against 13: Merry is overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(frodo, scn.GetRingBearer());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenARingBearerWhoIsNotFrodoDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(galadriel, troop);
		assertFalse(scn.GameIsFinished());

		// 3 + 1 for the Ring against 13: Galadriel is overwhelmed, and Sam may not take her place.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, galadriel);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void ResponseIsNotOfferedWhenAFrodoWhoIsNotTheRingBearerDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, frodo);

		scn.StartGame();

		assertEquals("Frodo", frodo.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertInZone(Zone.FREE_CHARACTERS, frodo);

		// 3 against 13: this Frodo is overwhelmed, but he was not the Ring-bearer.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(galadriel, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
