package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_002_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion, the target of Sent Back
					put("sentback", "9_27");    // Sent Back: "Skirmish: Discard this condition to discard each minion skirmishing a Wizard. Place that Wizard in your dead pile."
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion, Ring-bound and no Ring-bearer, strength 3
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, vitality 2
					put("backstabber", "1_174"); // Goblin Backstabber: a minion, strength 5, assigned against Gandalf
					put("assassin", "2_93");    // Tower Assassin: a minion, strength 11, vitality 3; "Maneuver: Exert Tower Assassin to wound an ally."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_2", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4
					put("slayer", "3_93");      // Morgul Slayer: an Orc minion, vitality 2; "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_2"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Galadriel, Bearer of Wisdom (vitality 3) bears the Ring: a Ring-bearer who is not Frodo.
	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksman", "1_176");   // Goblin Marksman: a minion, vitality 1; Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing,
				"94_2", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_2
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Each time your character is placed in the dead pile (except the Ring-bearer), discard it.
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_2", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-3, mod.getBlueprint().getIntensity());
	}

	@Test
	public void KilledCompanionGoesToDiscardInsteadOfDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		int deadCount = scn.GetFreepsDeadCount();
		int discardCount = scn.GetFreepsDiscardCount();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.DISCARD, aragorn);
		assertEquals(deadCount, scn.GetFreepsDeadCount());
		assertEquals(discardCount + 1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RingBearerIsExemptAndStillGoesToDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();

		int deadCount = scn.GetFreepsDeadCount();

		scn.AddWoundsToChar(frodo, frodo.getBlueprint().getVitality());
		// A single pump is enough to run the death check; a full SkipToPhase can't complete once
		// the Ring-bearer's death has ended the game.
		scn.FreepsPass();

		assertInZone(Zone.DEAD, frodo);
		assertEquals(deadCount + 1, scn.GetFreepsDeadCount());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void ARingBearerWhoIsNotFrodoStaysInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		// Vitality 3 with 2 wounds: the Marksman's arrow kills her.
		scn.AddWoundsToChar(galadriel, 2);

		assertInPlay(galadriel);
		assertEquals(0, scn.GetFreepsDeadCount());

		// She is the only one who can take the arrow, so nobody is asked where it goes.
		scn.SkipToArcheryWounds();

		assertInZone(Zone.DEAD, galadriel);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void SentBackAlsoRedirectsToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gandalf = scn.GetFreepsCard("gandalf");
		var sentback = scn.GetFreepsCard("sentback");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(backstabber);
		scn.MoveCardsToSupportArea(sentback);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, backstabber);

		int deadCount = scn.GetFreepsDeadCount();
		int discardCount = scn.GetFreepsDiscardCount();

		assertTrue(scn.FreepsActionAvailable(sentback));
		scn.FreepsUseCardAction(sentback);

		// Sent Back places the Wizard in the dead pile by its own text, with no wound; the mod
		// sends him on from there to the discard pile too. Sent Back's own cost (discard itself)
		// adds a second card to the discard pile alongside the companion.
		assertInZone(Zone.DISCARD, gandalf);
		assertEquals(deadCount, scn.GetFreepsDeadCount());
		assertEquals(discardCount + 2, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyAndThenSamAreEachDiscardedWhenTheyAreKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		var assassin = scn.GetShadowCard("assassin");

		scn.MoveAlliesToTable(bounder);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The Bounder is the only ally: the Assassin's two wounds are his, and he has 2 vitality.
		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(bounder));
		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);

		assertInZone(Zone.DISCARD, bounder);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(1, scn.GetFreepsDiscardCount());

		// Strength 3 against 11: Sam is overwhelmed and killed.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, assassin);
		assertInPlay(sam);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, sam);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertInPlay(frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutTheModKilledCompanionGoesToDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerTwoHasHisKilledCompanionDiscardedOnHisOwnTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetShadowCard("aragorn");
		var slayer = scn.GetFreepsCard("slayer");

		scn.StartGame();
		// Player 2's turn: he is the Free Peoples player, and the modifier is his.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(slayer);
		// Vitality 4 with 3 wounds: the Slayer's wound kills him.
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhaseInverted(Phase.REGROUP);

		int discardCount = scn.GetShadowDiscardCount();

		scn.ShadowPass();
		scn.FreepsUseCardAction(slayer);
		// Aragorn is the only companion the Slayer may wound, so nobody is asked.

		assertInZone(Zone.DISCARD, aragorn);
		assertEquals(discardCount + 1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ScopingWhenShadowOwnsModFreepsCompanionStillDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.SkipToPhase(Phase.REGROUP);

		// The mod is the other player's, and "your character" is not one of player 1's:
		// his companion goes to the dead pile as the rules have it.
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
