package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_126_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trap", "4_126");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability only
					put("boromir", "0_65");       // Boromir, Steward's Heir: [gondor] Man companion, strength 7, vitality 3; keywords only
					// Pippin, Trollslayer: a [gondor] Hobbit, no Man, strength 3, vitality 4; enduring (strength +2 for each
					// wound on him), and a skirmish ability used only on purpose
					put("pippin", "103_31");
					put("guard", "5_83");         // Household Guard: [rohan] Man companion, strength 6; Valiant only
					// A Path Appointed: a [gondor] condition that carries [gondor] tokens of its own; abilities used only on purpose
					put("path", "8_42");

					put("runner1", "1_178");      // Goblin Runner: [moria] Orc, strength 5, vitality 1; harmless when cheated in
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; answers a weapon played on him
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					// Uruk Pursuer: strength 5, vitality 3, printed fierce; its bar and its response reach no Hobbit here
					put("pursuer", "4_190");
					put("troop", "8_106");        // Siege Troop: [sauron] Orc, vitality 4; a regroup ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trap", "4_126");
					// Gondorian Merchant: the one [gondor] ally, a Man, strength 6, vitality 1, home 3T; a maneuver ability
					// that needs initiative
					put("merchant", "7_97");
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IthilienTrapStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ithilien Trap
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * Each time a [gondor] Man wins a skirmish, you may place a [gondor] token here.
		 * <b>Maneuver:</b> Exert a minion for each [gondor] token here (limit 3). Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trap");

		assertEquals("Ithilien Trap", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedAndOnlyAGondorManWinningASkirmishOffersAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetFreepsCard("trap");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(trap);
		scn.MoveCompanionsToTable(aragorn, boromir, pippin, guard);
		// Enduring: 3 + 2 for each of his two wounds is 7, against a Goblin Runner's 5.
		scn.AddWoundsToChar(pippin, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertInZone(Zone.SUPPORT, trap);

		scn.MoveMinionsToTable(runner1, runner2, runner3, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{boromir, chieftain},
				new PhysicalCardImpl[]{pippin, runner2},
				new PhysicalCardImpl[]{guard, runner3});

		// A [gondor] Man loses: 7 against 7 goes to the Shadow side.  Boromir lives with one wound; nothing is offered.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(trap));

		// A [gondor] Hobbit wins: nothing is offered.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(7, scn.GetStrength(pippin));
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(trap));

		// A [rohan] Man wins: nothing is offered.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInDiscard(runner3);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(trap));

		// A [gondor] Man wins.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trap));
		assertEquals(0, scn.GetCultureTokensOn(trap));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(trap));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTokenIsOfferedEachTimeAGondorManWinsAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetFreepsCard("trap");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(trap);
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trap);
		// The Pursuer is fierce, so that Aragorn beats it twice in the turn without killing it.
		scn.MoveMinionsToTable(pursuer, runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, pursuer},
				new PhysicalCardImpl[]{boromir, runner1});

		// Boromir, 7, beats the Runner, 5: offered, and declined.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trap));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(trap));

		// Aragorn, 8, beats the Pursuer, 5: offered again after the refusal, and taken.
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trap));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(trap));

		// The same Man wins the fierce skirmish: a second token.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, pursuer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trap));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(trap));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AGondorManAllyWinningAtHomeOffersAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var trap = scn.GetFreepsCard("trap");
		var merchant = scn.GetFreepsCard("merchant");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(trap);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trap);
		scn.SkipToSite(2);
		// The fellowship moves to site 3, Barrows of Edoras: the Merchant's home.
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merchant, runner);
		scn.FreepsResolveSkirmish(merchant);
		// 6 against 5.
		scn.PassSkirmishActions();

		assertInDiscard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trap));
		assertEquals(0, scn.GetCultureTokensOn(trap));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(trap));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverExertsAMinionForEachTokenHereOneMinionTwiceIfChosenAndThenItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var trap = scn.GetFreepsCard("trap");
		var path = scn.GetFreepsCard("path");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(trap, path);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddTokensToCard(trap, 2);
		// Three [gondor] tokens on another card, which are not "here".
		scn.AddTokensToCard(path, 3);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trap, path);
		assertEquals(2, scn.GetCultureTokensOn(trap));
		assertEquals(3, scn.GetCultureTokensOn(path));
		// Not a fellowship action.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(trap));

		scn.MoveMinionsToTable(savage, chieftain, runner1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(trap));
		scn.FreepsUseCardAction(trap);

		// Minions of two cultures and races; the Runner, with 1 vitality, cannot exert; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, chieftain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner1, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// "For each": the same minion may be chosen again for the second token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, chieftain));
		scn.FreepsChooseCard(savage);

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertInDiscard(trap);
		assertInZone(Zone.SUPPORT, path);
		assertEquals(3, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedInTheArcheryPhaseAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetFreepsCard("trap");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(trap);
		scn.AddTokensToCard(trap, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trap);
		scn.MoveMinionsToTable(savage);
		assertEquals(2, scn.GetCultureTokensOn(trap));
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(trap));

		// The maneuver phase is passed with the ability unused; the archery phase follows it.
		scn.PassCurrentPhaseActions();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(trap));
		assertInZone(Zone.SUPPORT, trap);
		assertEquals(2, scn.GetCultureTokensOn(trap));
		assertEquals(0, scn.GetWoundsOn(savage));
	}

	@Test
	public void WithOneTokenOneMinionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetFreepsCard("trap");
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(trap);
		scn.AddTokensToCard(trap, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trap);
		assertEquals(1, scn.GetCultureTokensOn(trap));
		scn.MoveMinionsToTable(savage, chieftain);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(trap);

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, chieftain));
		scn.FreepsChooseCard(chieftain);

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(trap);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithFourTokensNoMoreThanThreeMinionsAreExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetFreepsCard("trap");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(trap);
		scn.AddTokensToCard(trap, 4);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trap);
		assertEquals(4, scn.GetCultureTokensOn(trap));
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(trap);

		scn.FreepsChooseCard(troop);
		scn.FreepsChooseCard(troop);
		scn.FreepsChooseCard(savage);

		// Three exertions, and no fourth, though both minions could still exert.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(trap);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoTokensItExertsNobodyAndStillDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetFreepsCard("trap");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(trap);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trap);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetCultureTokensOn(trap));
		assertTrue(scn.FreepsActionAvailable(trap));
		scn.FreepsUseCardAction(trap);

		assertInDiscard(trap);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
