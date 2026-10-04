package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_054_Tests
{
	/*
	 * LEDGER  101_54  Sam, Of Bagshot Row
	 * Sources: definition's gametext and printed fields (PC card, no sheet row); CR 4.2 has no individual ruling on
	 *   this card, but its glossary entries "Sam" and "Ring-bound" speak of every version of Sam (R1, R2)
	 *
	 * P1 Play      [+]           Played from hand for (2), as a companion.
	 * C1 Trigger   [+ - F O R]   Each time a companion with the Frodo signet wins a skirmish, you may spot Frodo to
	 *                            heal that companion.
	 * C2 Response  [+ - O]       Response: If Frodo is killed, make Sam the Ring-bearer (resistance 5).
	 * R1 Rule      [+]           CR 4.2 glossary "Sam": Any version of Sam that can become the Ring-bearer if Frodo
	 *                            dies can only become the Ring-bearer if Frodo was the Ring-bearer when he was killed.
	 * R2 Keyword   [K]           CR 4.2 glossary "Ring-bound": All versions of Frodo and Sam are Ring-bound.
	 *
	 * Findings: Q-0008
	 * Notes: C1 has no C: "spot Frodo" is a requirement, not a cost (CR "cost"), and its absence is one of the two -.
	 *   C2 lists O though the text has no "may": declining the response is what loses the game.
	 *   C2's "(resistance 5)": Sam's resistance is asserted only as 5 before and 5 after, with no burdens.  Whether
	 *   the Ring is worn after the succession, and what becomes of burdens and resistance, is Q-0008 and will be a
	 *   mechanic test; nothing here rests on it.
	 *   CR 4.2 Section Three, Ulaire Otsea, Ringwraith in Twilight: "If Frodo dies in a skirmish involving Otsea
	 *   with Sam also in play, The One Ring is transferred to Sam before Blade Tip may be transferred".  That is an
	 *   Interaction and lives in Otsea's file.
	 */

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "101_54");
					// Boromir, Son of Denethor: Frodo signet, strength 7, vitality 3.  His skirmish ability is
					// never used here.
					put("boromir", "1_97");
					put("pippin", "1_306");     // Pippin, Friend to Frodo: Frodo signet, strength 3, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Gandalf signet, strength 8
					// Farin, Dwarven Emissary: no signet, strength 5, and +2 while skirmishing an Orc
					put("farin", "1_11");

					put("runner1", "1_178");    // Goblin Runner: Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: Frodo signet, strength 3, vitality 4
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "101_54");
					put("frodo", "1_290");      // Frodo, Son of Drogo, here only a companion: strength 3
					put("runner1", "1_178");    // Goblin Runner: Orc, strength 5, vitality 1
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	// R2: K
	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Sam, Of Bagshot Row
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Signet: Frodo
		 * Game Text: Each time a companion with the Frodo signet wins a skirmish, you may spot Frodo to heal that
		 * companion.
		 * Response: If Frodo is killed, make Sam the Ring-bearer (resistance 5).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Of Bagshot Row", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
		// Not printed: every version of Sam is Ring-bound by rule.
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
	}

	// P1: +
	@Test
	public void PlaysFromHandAsACompanionForTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(sam);

		scn.StartGame();

		assertInHand(sam);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(sam));

		scn.FreepsPlayCard(sam);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// C1: + O R
	@Test
	public void FrodoSignetCompanionWhoWinsASkirmishMayBeHealedEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(sam, boromir);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(boromir, 1);
		// Sam's printed strength of 3 beats no minion here.  With +3 he beats a Goblin Runner, 6 against 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 3));

		scn.StartGame();

		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, runner1}, new PhysicalCardImpl[]{sam, runner2});

		// Boromir (Frodo signet), 7 against 5.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		// Declined: nothing changes.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(sam));

		// Sam himself (Frodo signet), 6 against 5: the second win of the turn.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishing(sam));
		assertEquals(6, scn.GetStrength(sam));
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		// Accepted: one wound is removed.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// C1: F
	@Test
	public void OnlyAWinnerWithTheFrodoSignetIsOfferedAndOnlyTheWinnerIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCompanionsToTable(sam, boromir, pippin, aragorn, farin);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(farin, 1);

		scn.StartGame();

		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, runner1},
				new PhysicalCardImpl[]{aragorn, runner2},
				new PhysicalCardImpl[]{farin, runner3});

		// Aragorn has the Gandalf signet: 8 against 5, and nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Farin has no signet: 5 + 2 against an Orc's 5, and nothing is offered.
		scn.FreepsResolveSkirmish(farin);
		scn.PassSkirmishActions();
		assertInDiscard(runner3);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(farin));

		// Boromir has the Frodo signet: 7 against 5.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishing(boromir));
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// "That companion": the winner, with no choosing, and not the other wounded bearers of the Frodo signet.
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// C1: -
	@Test
	public void FrodoSignetCompanionWhoLosesASkirmishIsNotOfferedHealing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, boromir);
		scn.AddWoundsToChar(boromir, 1);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(boromir, troop);
		assertEquals(1, scn.GetWoundsOn(boromir));

		// 7 against 13: lost, but not doubled, so Boromir takes one wound and lives.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInPlay(boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// C1: -
	@Test
	public void WinnerIsNotOfferedHealingWhenFrodoCannotBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(sam);
		scn.AddWoundsToChar(sam, 2);
		// Sam's printed strength of 3 beats no minion here.  With +3 he beats a Goblin Runner, 6 against 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 3));

		scn.StartGame();

		// Galadriel bears the Ring, and Frodo is nowhere on the table.
		assertEquals("Galadriel", scn.GetRingBearer().getBlueprint().getTitle());

		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner1);
		assertEquals(2, scn.GetWoundsOn(sam));

		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// C2: +
	@Test
	public void WhenFrodoIsKilledSamCanBecomeTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		assertEquals("Frodo", frodo.getBlueprint().getTitle());
		assertAttachedTo(ring, frodo);
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(5, scn.GetResistance(sam));

		// 3 + 1 for the Ring against 13: Frodo is overwhelmed, which kills him without a wound.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.GameIsFinished());
		// He bears The One Ring.
		assertAttachedTo(ring, sam);
		// 3 + 1 for the Ring
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// C2: O
	@Test
	public void DecliningTheResponseWhenFrodoIsKilledLosesTheGame() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.GameIsFinished());
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertNotSame(sam, scn.GetRingBearer());
		assertTrue(scn.GameIsFinished());
	}

	// C2: -
	@Test
	public void ResponseIsNotOfferedWhenAnotherCompanionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, pippin);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(pippin, troop);
		assertInPlay(pippin);

		// 3 against 13: Pippin is overwhelmed and killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, pippin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(frodo, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// C2: -
	@Test
	public void ResponseIsNotOfferedWhenARingBearerWhoIsNotFrodoIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
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

		// 3 + 1 for the Ring against 13: the Ring-bearer is overwhelmed and killed, but she is not Frodo.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, galadriel);
		assertNotSame(sam, scn.GetRingBearer());
		assertTrue(scn.GameIsFinished());
	}

	// R1: +
	@Test
	public void ResponseIsNotOfferedWhenAFrodoWhoIsNotTheRingBearerIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, frodo);

		scn.StartGame();

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());
		assertEquals("Frodo", frodo.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertInPlay(frodo);

		// 3 against 13: this Frodo is overwhelmed and killed, but he was not the Ring-bearer.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(galadriel, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
