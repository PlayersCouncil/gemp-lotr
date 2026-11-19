package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_312_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("siegecraft", "7_312");
					put("inquisitor", "1_268");   // Orc Inquisitor: [sauron] Orc, strength 9, vitality 3; its text acts only as it is played
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc, strength 7, vitality 2, cost 2; a skirmish ability used only on purpose
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					put("sentry", "10_84");       // Cirith Ungol Sentry: a [sauron] minion who is an Uruk-hai, strength 8; Damage +1 is its whole text
					put("runner", "1_178");       // Goblin Runner: a [moria] Orc, strength 5; its text acts only as it is played

					// For a [sauron] Orc who comes into play after the event: A Shadow Fell Over Them ("Assignment: Spot a
					// Nazgûl and remove (4) to play a Nazgûl from your discard pile") plays Úlairë Nertëa, whose "When you
					// play" plays a minion from the discard pile with 5 companions in play.  Úlairë Lemenya is the Nazgûl
					// spotted; her text is about [wraith] conditions on companions.
					put("fell", "15_182");
					put("nertea", "1_234");
					put("lemenya", "1_232");

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Glóin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("merry", "1_303");        // Merry, From O'er the Brandywine: strength 3, vitality 4; his text needs a weapon
					put("sam", "1_311");          // Sam, Son of Hamfast: a fellowship ability used only on purpose
					put("guard", "1_7");          // Dwarf Guard: no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SiegecraftStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Siegecraft
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If your [sauron] Orc wins a skirmish, make all your [sauron] Orcs <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var siegecraft = scn.GetFreepsCard("siegecraft");

		assertEquals("Siegecraft", siegecraft.getBlueprint().getTitle());
		assertNull(siegecraft.getBlueprint().getSubtitle());
		assertFalse(siegecraft.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, siegecraft.getBlueprint().getSide());
		assertEquals(Culture.SAURON, siegecraft.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, siegecraft.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(siegecraft, Timeword.RESPONSE));
		assertEquals(4, siegecraft.getBlueprint().getTwilightCost());
	}

	@Test
	public void ASauronOrcWinningMakesEverySauronOrcFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var siegecraft = scn.GetShadowCard("siegecraft");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(siegecraft);
		assertInHand(siegecraft);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(inquisitor, soldier, chieftain, sentry, runner);
		scn.SkipToAssignments();
		// The Soldier is in a skirmish of his own that has not begun; the Chieftain, the [sauron] Uruk-hai and the
		// [moria] Orc are assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, inquisitor}, new PhysicalCardImpl[]{aragorn, soldier});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		// Nobody has won anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(siegecraft));

		// 9 against 6: the Inquisitor wins.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowPlayAvailable(siegecraft));
		assertFalse(scn.HasKeyword(inquisitor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		scn.ShadowPlayCard(siegecraft);

		assertInDiscard(siegecraft);
		assertTrue(scn.HasKeyword(inquisitor, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));

		// 7 against 8: the Soldier loses his own skirmish to Aragorn and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(soldier));

		// The three [sauron] Orcs are to be defended against a second time, and nobody else.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(inquisitor));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(soldier));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(chieftain));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(sentry));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(runner));
		scn.FreepsAssignToMinions(aragorn, inquisitor);
		scn.ShadowDeclineAssignments();

		// The fierce skirmish is the last thing before the regroup phase: they are fierce through it.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(inquisitor, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));

		// 9 against 8: Aragorn loses.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(inquisitor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
	}

	@Test
	public void IsNotOfferedWhenASauronOrcLosesNorWhenAnOrcOfAnotherCultureOrASauronMinionWhoIsNoOrcWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, gimli, merry);

		var siegecraft = scn.GetShadowCard("siegecraft");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(siegecraft);
		assertInHand(siegecraft);

		scn.StartGame();
		// Enough for the event's cost of 4 throughout.
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(soldier, sentry, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, runner},
				new PhysicalCardImpl[]{gimli, sentry},
				new PhysicalCardImpl[]{aragorn, soldier});

		// 5 against 3: a Goblin Runner, a [moria] Orc, wins.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 8 against 6: a Cirith Ungol Sentry, a [sauron] Uruk-hai, wins, and wounds twice for his damage +1.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 7 against 8: an Orc Soldier, a [sauron] Orc, loses.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInHand(siegecraft);
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASauronOrcWhoComesIntoPlayAfterTheEventIsNotMadeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		// Five companions with Frodo.
		scn.MoveCompanionsToTable(aragorn, gimli, sam, guard);

		var siegecraft = scn.GetShadowCard("siegecraft");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var fell = scn.GetShadowCard("fell");
		var nertea = scn.GetShadowCard("nertea");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(siegecraft);
		scn.MoveCardsToSupportArea(fell);
		scn.MoveCardsToDiscard(nertea, soldier);
		assertInHand(siegecraft);
		assertInZone(Zone.SUPPORT, fell);
		assertInDiscard(nertea, soldier);

		scn.StartGame();
		scn.SetTwilight(25);
		scn.MoveMinionsToTable(chieftain, lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, chieftain);
		scn.ShadowDeclineAssignments();

		// 7 against 6: the Chieftain, a second title of [sauron] Orc, wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowPlayAvailable(siegecraft));
		scn.ShadowPlayCard(siegecraft);
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));

		// The fierce assignment phase: the Shadow player plays Nertëa, and she the Orc Soldier.
		assertTrue(scn.InFierceRound());
		scn.FreepsPass();
		scn.ShadowUseCardAction(fell);
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		// Five companions, one over 4: one minion from the discard pile.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
