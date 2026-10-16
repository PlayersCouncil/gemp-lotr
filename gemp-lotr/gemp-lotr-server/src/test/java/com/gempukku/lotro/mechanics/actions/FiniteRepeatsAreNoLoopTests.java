package com.gempukku.lotro.mechanics.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Repeats that are not a loop are never put to the players (CR 4.2 "loops"; Ketura on Q-0329: "We're going to want
 * some extensive tests to ensure that merely large yet finite repeats do not trigger the loop detection").
 *
 * A loop is found only where both players take the same two actions in turn and each round gives the board back as
 * it was (VoluntaryLoopTests).  A pump war spends something each round, exertions, cards, twilight or tokens, and
 * so ends when one side runs dry: it is not a loop however long it runs, and both players are offered their actions
 * as ever to the end.  Nor is one player using his own ability again and again, the other passing in between (Trust
 * Me as You Once Did, her example: "One player using a finite resource 3 times probably isn't a loop").  A pass,
 * or a third card's action, between the rounds of a real loop starts the count again.
 *
 * Every test reads, after each round, that the player who opens the round is offered his actions and not the loop's
 * prompt.  The skirmishes are fought at Ettenmoors 1_331 (site 2), whose "Skirmish: Exert your companion or minion
 * to make that character strength +2" is offered to both players and never used, except where Anduin Banks 7_341
 * is laid as site 4 (as in Card_05_029_Tests).
 */
public class FiniteRepeatsAreNoLoopTests
{
	// Sméagol, Poor Creature 6_45: strength 3, "Skirmish: Discard a card from hand to make Sméagol strength +1."
	// Prowling Orc 11_136: [orc] Orc, strength 6, lurker, "Skirmish: Remove (2) to make an [orc] minion strength +1."
	// Lórien Elf 1_53: no text but "To play, spot an Elf": the cards in hand, and the [elven] characters that
	// Taking the High Ground 12_24 ("Archery: Discard an [elven] character from hand to make the fellowship archery
	// total +1") discards.  Wreathed in Shadow 1_238: "Archery: Remove (1) to make the fellowship archery total -1."
	// Úlairë Nelya 2_84, the minion for the archery phase: nothing below 2 burdens and 2 wounds on the Ring-bearer.
	protected VirtualTableScenario GetCardsAndTwilightScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "6_45");
					put("orc", "11_136");
					put("highground", "12_24");
					put("wreathed", "1_238");
					put("nelya", "2_84");
					for (int i = 1; i <= 11; i++)
						put("elf" + i, "1_53");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	// Sudden Fury 12_132: "Skirmish: Remove a [shire] token from here to make a [shire] companion strength +1"; its
	// trigger answers a [shire] companion losing a skirmish, and none is resolved.  Frodo 1_290 is the one [shire]
	// companion.  Gollum, Old Villain 6_40: "Skirmish: Discard a card from hand to make Gollum strength +1 (or +2 if
	// skirmishing a Ring-bound companion)".  Goblin Runner 1_178: the Shadow cards in hand.  Rank and File 10_96:
	// "Skirmish: Remove 2 [sauron] tokens from here to make a [sauron] minion strength +2"; its other texts answer a
	// wound and a regroup phase.  Mordor Soldier 7_290: a [sauron] Orc, strength 3, whose "Remove a threat" is not
	// offered with no threat.  The Great Ring 19_1 answers no wound while it is not worn.
	protected VirtualTableScenario GetTokensScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "12_132");
					put("gollum", "6_40");
					put("rankandfile", "10_96");
					put("soldier", "7_290");
					for (int i = 1; i <= 11; i++)
						put("runner" + i, "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	// Gimli, Son of Glóin 1_13: vitality 3, "Skirmish: Exert Gimli to make him strength +2."  Moria Axe 1_190 on
	// Goblin Patrol Troop 1_177 (vitality 3, no text): "Skirmish: Exert bearer to make him strength +2."  Trust Me as
	// You Once Did 3_35: "Skirmish: Exert Gandalf to make a companion strength +1 for each companion with the Gandalf
	// signet you can spot (limit +3)", with Gandalf, The Grey Wizard 1_364 (vitality 4).
	protected VirtualTableScenario GetExertionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");
					put("axe", "1_190");
					put("troop", "1_177");
					put("trustme", "3_35");
					put("gandalf", "1_364");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	// The loop of the rules, Sméagol, Slinker 5_29 against Anduin Banks 7_341, laid as site 4.  Uruk Pursuer 4_190
	// (strength 5; its texts are about unbound Hobbits) is the minion, or Uruk Slayer 1_153 ("Skirmish: Remove (1) to
	// make this minion strength +1 (limit +3)"), whose ability is the third card's action.
	protected VirtualTableScenario GetLoopScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "5_29");
					put("pursuer", "4_190");
					put("slayer", "1_153");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "7_341");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElevenRoundsOfCardsAgainstTwilightInASkirmishAreNoLoop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCardsAndTwilightScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var orc = scn.GetShadowCard("orc");
		var elves = new PhysicalCardImpl[11];
		for (int i = 0; i < 11; i++)
			elves[i] = scn.GetFreepsCard("elf" + (i + 1));
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveCardsToHand(elves);
		scn.MoveMinionsToTable(orc);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, orc);
		scn.FreepsResolveSkirmish(smeagol);
		scn.SetTwilight(22);

		assertEquals(11, scn.GetFreepsHandCount());
		for (int round = 1; round <= 10; round++) {
			scn.FreepsUseCardAction(smeagol);
			scn.FreepsChooseCard(elves[round - 1]);
			scn.ShadowUseCardAction(orc);

			assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
			assertTrue(scn.FreepsActionAvailable(smeagol));
			assertEquals(11 - round, scn.GetFreepsHandCount());
			assertEquals(22 - 2 * round, scn.GetTwilight());
			assertEquals(3 + round, scn.GetStrength(smeagol));
			assertEquals(6 + round, scn.GetStrength(orc));
		}

		// The eleventh round: the last card (no choice) and the last twilight.
		scn.FreepsUseCardAction(smeagol);
		assertTrue(scn.ShadowActionAvailable(orc));
		scn.ShadowUseCardAction(orc);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		assertEquals(14, scn.GetStrength(smeagol));
		assertEquals(17, scn.GetStrength(orc));
		assertFalse(scn.FreepsActionAvailable(smeagol));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(orc));
	}

	@Test
	public void ElevenRoundsOfTokensAgainstCardsInASkirmishAreNoLoop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTokensScenario();

		var frodo = scn.GetRingBearer();
		var fury = scn.GetFreepsCard("fury");
		var gollum = scn.GetShadowCard("gollum");
		var runners = new PhysicalCardImpl[11];
		for (int i = 0; i < 11; i++)
			runners[i] = scn.GetShadowCard("runner" + (i + 1));
		scn.MoveCardsToSupportArea(fury);
		scn.AddTokensToCard(fury, 11);

		scn.StartGame();
		scn.MoveCardsToHand(runners);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, gollum);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(11, scn.GetShadowHandCount());
		for (int round = 1; round <= 10; round++) {
			scn.FreepsUseCardAction(fury);
			scn.ShadowUseCardAction(gollum);
			scn.ShadowChooseCard(runners[round - 1]);

			assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
			assertTrue(scn.FreepsActionAvailable(fury));
			assertEquals(11 - round, scn.GetCultureTokensOn(fury));
			assertEquals(11 - round, scn.GetShadowHandCount());
		}

		scn.FreepsUseCardAction(fury);
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(fury));
		assertEquals(0, scn.GetShadowHandCount());
		assertFalse(scn.FreepsActionAvailable(fury));
	}

	@Test
	public void ElevenRoundsOfTokensAgainstTokensInASkirmishAreNoLoop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTokensScenario();

		var frodo = scn.GetRingBearer();
		var fury = scn.GetFreepsCard("fury");
		var rankandfile = scn.GetShadowCard("rankandfile");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(fury, rankandfile);
		scn.AddTokensToCard(fury, 11);
		scn.AddTokensToCard(rankandfile, 22);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, soldier);
		scn.FreepsResolveSkirmish(frodo);

		for (int round = 1; round <= 11; round++) {
			assertTrue(scn.FreepsActionAvailable(fury));
			scn.FreepsUseCardAction(fury);
			assertTrue(scn.ShadowActionAvailable(rankandfile));
			scn.ShadowUseCardAction(rankandfile);

			assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
			assertEquals(11 - round, scn.GetCultureTokensOn(fury));
			assertEquals(22 - 2 * round, scn.GetCultureTokensOn(rankandfile));
			assertEquals(3 + 2 * round, scn.GetStrength(soldier));
		}

		assertFalse(scn.FreepsActionAvailable(fury));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rankandfile));
	}

	@Test
	public void ElevenRoundsOfCardsAgainstTwilightInTheArcheryPhaseAreNoLoop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCardsAndTwilightScenario();

		var highground = scn.GetFreepsCard("highground");
		var wreathed = scn.GetShadowCard("wreathed");
		var nelya = scn.GetShadowCard("nelya");
		var elves = new PhysicalCardImpl[11];
		for (int i = 0; i < 11; i++)
			elves[i] = scn.GetFreepsCard("elf" + (i + 1));
		scn.MoveCardsToSupportArea(highground, wreathed);

		scn.StartGame();
		scn.MoveCardsToHand(elves);
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(11);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		for (int round = 1; round <= 10; round++) {
			scn.FreepsUseCardAction(highground);
			scn.FreepsChooseCard(elves[round - 1]);
			scn.ShadowUseCardAction(wreathed);

			assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
			assertTrue(scn.FreepsActionAvailable(highground));
			assertEquals(11 - round, scn.GetFreepsHandCount());
			assertEquals(11 - round, scn.GetTwilight());
		}

		scn.FreepsUseCardAction(highground);
		assertTrue(scn.ShadowActionAvailable(wreathed));
		scn.ShadowUseCardAction(wreathed);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsActionAvailable(highground));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wreathed));
	}

	@Test
	public void ExertionsAgainstExertionsAreNoLoop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExertionScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetShadowCard("axe");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, axe);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		for (int round = 1; round <= 2; round++) {
			scn.FreepsUseCardAction(gimli);
			scn.ShadowUseCardAction(axe);

			assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
			assertEquals(round, scn.GetWoundsOn(gimli));
			assertEquals(round, scn.GetWoundsOn(troop));
		}

		// Neither can exert again.
		assertFalse(scn.FreepsActionAvailable(gimli));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(axe));
	}

	@Test
	public void OnePlayerUsingTrustMeAsYouOnceDidAgainAndAgainIsNoLoop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExertionScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var trustme = scn.GetFreepsCard("trustme");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(trustme);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		for (int use = 1; use <= 3; use++) {
			assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
			scn.FreepsUseCardAction(trustme);
			scn.FreepsChooseCard(gandalf);
			assertEquals(use, scn.GetWoundsOn(gandalf));

			// The Shadow player passes each time; he is offered the site's ability.
			assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
			scn.ShadowPass();
		}

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(trustme));
	}

	@Test
	public void APassBetweenTheRoundsOfALoopStartsTheCountAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLoopScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(pursuer);
		scn.FreepsPass();

		var banks = scn.GetCurrentSite();
		assertEquals("Anduin Banks", banks.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);

		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetBurdens());

		// The Shadow player passes instead of using the site's ability.
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		// One round since the pass.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		// Two rounds since the pass, the board as at the start of the second: found.
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsDecisionAvailable("LOOP DETECTED"));
	}

	@Test
	public void AThirdCardsActionBetweenTheRoundsOfALoopStartsTheCountAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLoopScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(slayer);
		scn.FreepsPass();

		var banks = scn.GetCurrentSite();
		assertEquals("Anduin Banks", banks.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, slayer);
		scn.FreepsResolveSkirmish(smeagol);
		scn.SetTwilight(1);

		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);

		// In place of the site's ability, the Slayer's own.
		scn.ShadowUseCardAction(slayer);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		// The last two rounds are not the same two actions.
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		// They are now, and the board is as it was at the start of the second: found.
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsDecisionAvailable("LOOP DETECTED"));
	}
}
