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

public class Card_13_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("howling", "13_109");
					put("howling2", "13_109");
					// Scurrying Goblin: [orc] Orc, strength 5, vitality 1; its only text answers its own play.
					put("goblin", "11_137");
					// Half-troll of Far Harad: an [orc] minion who is no Orc, vitality 2; its start-of-assignment
					// offer needs more companions than minions, which no board here has.
					put("halftroll", "14_12");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, damage +1, a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, a skirmish ability only
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: archer; +3 only against a Nazgûl
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; bears the staff
					put("staff", "7_38");       // Gandalf's Staff, Focus of Power: the staff Staff Asunder discards
					// Staff Asunder: Skirmish event: discards a minion Gandalf is skirmishing, with no wound placed.
					put("asunder", "2_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HowlingOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Howling Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>Each time an [orc] minion is killed in a skirmish, you may make this minion strength +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("howling");

		assertEquals("Howling Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachOrcMinionKilledInASkirmishMayMakeThisCopyStrengthPlus2UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howling = scn.GetShadowCard("howling");
		var howling2 = scn.GetShadowCard("howling2");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(howling, howling2, goblin, halftroll, runner);
		// Only so that Gimli beats the Half-troll: 8 - 4.
		scn.ApplyAdHocModifier(new StrengthModifier(null, halftroll, null, -4));

		// Neither Howling Orc is assigned: every kill is in a skirmish that is not his.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, goblin}, new PhysicalCardImpl[]{gimli, halftroll},
				new PhysicalCardImpl[]{aragorn, runner});
		scn.ShadowDeclineAssignments();
		assertEquals(8, scn.GetStrength(howling));
		assertEquals(8, scn.GetStrength(howling2));

		// Boromir 7 beats the Goblin 5, who dies of his one wound: an [orc] Orc killed. Each copy offers its own bonus.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInDiscard(goblin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howling));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howling2));
		scn.ShadowChooseOptionalTrigger(howling);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howling2));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(10, scn.GetStrength(howling));
		// Declined: the other copy is as it was.
		assertEquals(8, scn.GetStrength(howling2));

		// Gimli 6 beats the Half-troll 4 and his damage +1 kills him: an [orc] minion who is no Orc, a second kill.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInDiscard(halftroll);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howling));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(howling2));
		scn.ShadowChooseOptionalTrigger(howling);
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(12, scn.GetStrength(howling));
		assertEquals(8, scn.GetStrength(howling2));

		// Still +4 with a skirmish waiting.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(12, scn.GetStrength(howling));

		// Aragorn 8 beats the Runner 5, who dies: a [moria] Orc, so nothing is offered. The regroup phase follows,
		// and the bonus is gone.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(howling));
		assertEquals(8, scn.GetStrength(howling2));
	}

	@Test
	public void AnOrcMinionKilledByArcheryIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howling = scn.GetShadowCard("howling");
		var goblin = scn.GetShadowCard("goblin");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(howling, goblin);

		// Legolas's archery wound, which the Shadow player places on the Goblin, kills him in the archery phase.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		assertEquals(8, scn.GetStrength(howling));
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		scn.ShadowChooseCard(goblin);

		assertInDiscard(goblin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(8, scn.GetStrength(howling));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnOrcMinionDiscardedFromPlayInASkirmishButNotKilledIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var howling = scn.GetShadowCard("howling");
		var goblin = scn.GetShadowCard("goblin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var asunder = scn.GetFreepsCard("asunder");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(asunder);

		scn.StartGame();
		scn.MoveMinionsToTable(howling, goblin);
		assertAttachedTo(staff, gandalf);
		assertInHand(asunder);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, goblin);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);

		// Staff Asunder discards the Goblin from play, an [orc] minion in a skirmish and no kill: no wound is placed.
		assertEquals(8, scn.GetStrength(howling));
		assertEquals(0, scn.GetWoundsOn(goblin));
		scn.FreepsPlayCard(asunder);
		assertInDiscard(goblin, staff);
		assertEquals(0, scn.GetWoundsOn(goblin));

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(8, scn.GetStrength(howling));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
