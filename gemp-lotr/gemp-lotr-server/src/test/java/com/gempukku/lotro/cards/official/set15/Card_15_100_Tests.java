package com.gempukku.lotro.cards.official.set15;

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

public class Card_15_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "15_100");
					put("commander2", "15_100");
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("gandalf", "6_30");       // Gandalf, Mithrandir: [gandalf] Wizard companion, strength 10; wounded at each move
					put("glamdring", "1_75");     // Glamdring: strength +2, bearer must be Gandalf
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man companion, strength 7
					put("gimli", "1_13");         // Gimli, Son of Gloin: a fourth companion, for a fourth threat
					put("treebeard", "4_104");    // Treebeard, Oldest Living Thing: an ally of strength 12
					put("thrarin", "1_27");       // Thrarin, Dwarven Smith: an ally of strength 4 who exerts in the maneuver phase to take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackLandCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Black Land Commander
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a companion of strength 12 or more, this minion is strength +5.<br><b>Skirmish:</b> Remove 2 threats to make a character skirmishing this minion strength -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("commander");

		assertEquals("Black Land Commander", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void GandalfMadeStrength12ByGlamdringMakesEachCommanderStrength13AndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(glamdring);

		var commander = scn.GetShadowCard("commander");
		var commander2 = scn.GetShadowCard("commander2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Minions laid early, to be read as a Free Peoples card is played (Q-0228).
		scn.MoveMinionsToTable(commander, commander2, troop);
		scn.FreepsRefreshDecision();

		// An ally of 12 and a minion of 13: no companion of 12 or more.
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(12, scn.GetStrength(treebeard));
		assertEquals(8, scn.GetStrength(commander));
		assertEquals(8, scn.GetStrength(commander2));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);

		// 10 + 2 for Glamdring.
		assertEquals(12, scn.GetStrength(gandalf));
		// 8 + 5, each Commander once, and not the other minion.
		assertEquals(13, scn.GetStrength(commander));
		assertEquals(13, scn.GetStrength(commander2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionOf11IsNotEnoughAndOf12Is() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var commander = scn.GetShadowCard("commander");

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, commander);

		// A strength that only arranges the board: 8 + 3.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(commander));

		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 1));
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(commander));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemoves2ThreatsToMakeTheCompanionItIsSkirmishingStrengthMinus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var commander = scn.GetShadowCard("commander");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(commander, troop);
		// Three companions: room for three threats.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		// A strength that only arranges the board: Aragorn at exactly 12, so that the Commander is 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, commander}, new PhysicalCardImpl[]{boromir, troop});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(commander));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(commander));

		scn.ShadowUseCardAction(commander);
		assertEquals(1, scn.GetThreats());
		assertEquals(10, scn.GetStrength(aragorn));
		// Boromir, skirmishing another minion, is untouched.
		assertEquals(7, scn.GetStrength(boromir));
		// With Aragorn at 10 no companion of 12 or more is left: the Commander's own +5 is gone.
		assertEquals(8, scn.GetStrength(commander));
		// Still in force at the decision that follows the use.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(aragorn));

		scn.PassSkirmishActions();
		// 8 against 10: the Commander loses and is wounded.
		assertEquals(1, scn.GetWoundsOn(commander));
		scn.FreepsResolveSkirmish(boromir);
		// The next skirmish is a phase of its own: Aragorn is 12 again, and so the Commander is 13.
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(commander));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesAnAllyItIsSkirmishingStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		var commander = scn.GetShadowCard("commander");

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		// Two companions: room for two threats.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, commander);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, commander));
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(commander));

		scn.ShadowUseCardAction(commander);
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InAnotherSkirmishItsAbilityIsPaidForAndChangesNobodyWhileTheOtherCopysReachesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		var commander = scn.GetShadowCard("commander");
		var commander2 = scn.GetShadowCard("commander2");

		scn.StartGame();
		scn.MoveMinionsToTable(commander, commander2);
		// Four companions: room for four threats.
		scn.AddThreats(4);
		assertEquals(4, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, commander}, new PhysicalCardImpl[]{boromir, commander2});
		// Boromir fights the second Commander first; Aragorn waits for the first, assigned to it but not yet skirmishing it.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(commander));

		scn.ShadowUseCardAction(commander);
		// Paid for, and nobody made weaker: Boromir skirmishes the other copy, and Aragorn's skirmish has not begun.
		assertEquals(2, scn.GetThreats());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(commander2));
		scn.ShadowUseCardAction(commander2);
		assertEquals(0, scn.GetThreats());
		assertEquals(5, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var commander = scn.GetShadowCard("commander");

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, commander));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(commander));

		// A second threat, and the ability is offered.
		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(commander));
	}
}
