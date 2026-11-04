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

public class Card_15_139_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "15_139");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Aragorn, Last Hope of Men: valiant [gondor] Man companion, vitality 4; a start-of-turn draw (laid
					// after the turn has begun) and a skirmish ability used only on purpose
					put("aragorn", "102_16");
					// Lórien Guardian: valiant [elven] Elf companion, vitality 3; a regroup ability used only on purpose
					put("guardian", "54_76");
					put("boromir", "0_65");     // Boromir, Steward's Heir: [gondor] Man companion, strength 7, not valiant; keywords only
					// Elven Guardian: [elven] Elf companion with Hunter 1; a maneuver ability used only on purpose
					put("elfguard", "17_7");
					// Hunting Herdsman: [men] Man minion, strength 8, vitality 2, Hunter 1; +3 for each follower (none laid)
					put("herdsman", "15_83");
					// Tracking Uruk: [uruk-hai] Uruk-hai minion, strength 14, Hunter 1 is its whole text
					put("uruk", "15_171");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1, no hunter; its only text is on its play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Rohirrim Warrior
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>.<br>At the start of the maneuver phase, you may spot a hunter minion (or exert 2 valiant Men) to make this companion <b>defender +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");

		assertEquals("Rohirrim Warrior", warrior.getBlueprint().getTitle());
		assertNull(warrior.getBlueprint().getSubtitle());
		assertTrue(warrior.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, warrior.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, warrior.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, warrior.getBlueprint().getCardType());
		assertEquals(Race.MAN, warrior.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(warrior, Keyword.VALIANT));
		assertEquals(3, warrior.getBlueprint().getTwilightCost());
		assertEquals(6, warrior.getBlueprint().getStrength());
		assertEquals(3, warrior.getBlueprint().getVitality());
		assertEquals(6, warrior.getBlueprint().getResistance());
	}

	@Test
	public void SpottingAHunterMinionMakesHimDefenderPlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var boromir = scn.GetFreepsCard("boromir");
		var herdsman = scn.GetShadowCard("herdsman");
		var runner = scn.GetShadowCard("runner");
		// The Warrior is the only valiant Man, so the exertion cannot be paid and the spot is the one way.
		scn.MoveCompanionsToTable(warrior, boromir);

		scn.StartGame();

		scn.MoveMinionsToTable(herdsman, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DEFENDER));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(warrior));
		scn.FreepsAcceptOptionalTrigger();

		// Nothing asked and nothing exerted; Boromir, another companion, is not made defender.
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{warrior, herdsman},
				new PhysicalCardImpl[]{boromir, runner});
		// Boromir (7) kills the Runner (5, vitality 1) first.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(warrior);

		// Still in force in the skirmish after one already resolved.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DEFENDER));

		// The Herdsman, 8 + 1 hunter against 6, wins and the Warrior takes a wound.  At the regroup phase it is gone.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DEFENDER));
	}

	@Test
	public void WithNoHunterMinionTwoValiantMenAreExertedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guardian = scn.GetFreepsCard("guardian");
		var boromir = scn.GetFreepsCard("boromir");
		var elfguard = scn.GetFreepsCard("elfguard");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(warrior, guard, guardian, boromir, elfguard);

		scn.StartGame();

		scn.MoveCompanionsToTable(aragorn);
		// A hunter who is no minion, and a minion who is no hunter.
		scn.MoveMinionsToTable(runner);
		scn.FreepsRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(warrior));
		scn.FreepsAcceptOptionalTrigger();

		// Valiant Men of two cultures, the Warrior himself among them; not a valiant Elf, a Man who is not valiant,
		// the hunter companion or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior, guard, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guardian, boromir, elfguard, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(guard, aragorn);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithBothWaysOpenThePlayerChoosesWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var guard = scn.GetFreepsCard("guard");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCompanionsToTable(warrior, guard);

		scn.StartGame();

		scn.MoveMinionsToTable(uruk);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(warrior));
		scn.FreepsAcceptOptionalTrigger();

		// The Tracking Uruk to spot, and two valiant Men able to exert: both ways are offered.
		assertTrue(scn.FreepsChoiceAvailable("Spot a hunter minion"));
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 valiant Men"));
		scn.FreepsChoose("Spot a hunter minion");

		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoHunterMinionAndOneValiantManAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var guard = scn.GetFreepsCard("guard");
		var guardian = scn.GetFreepsCard("guardian");
		var boromir = scn.GetFreepsCard("boromir");
		var elfguard = scn.GetFreepsCard("elfguard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(warrior, guard, guardian, boromir, elfguard);
		// The Guard is exhausted: the Warrior is the only valiant Man able to exert.
		scn.AddWoundsToChar(guard, 2);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(guard));
		// A hunter who is no minion, and a minion who is no hunter.
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(warrior));
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(boromir));
	}

	@Test
	public void DeclinedNothingChangesAndTheStartOfTheArcheryPhaseIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(warrior, guard);

		scn.StartGame();

		scn.MoveMinionsToTable(herdsman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(warrior));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the next phase is no start of the maneuver phase.
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(warrior));
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DEFENDER));
	}

	@Test
	public void ASecondManeuverPhaseInTheSameTurnOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var boromir = scn.GetFreepsCard("boromir");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(warrior, boromir);

		scn.StartGame();

		scn.MoveMinionsToTable(herdsman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(warrior));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DEFENDER));

		// The Herdsman left unassigned stands in the regroup phase, where the defender is gone.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DEFENDER));

		// A second move, and a second maneuver phase with the Herdsman still in play.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SHADOW_CHARACTERS, herdsman);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(warrior));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
