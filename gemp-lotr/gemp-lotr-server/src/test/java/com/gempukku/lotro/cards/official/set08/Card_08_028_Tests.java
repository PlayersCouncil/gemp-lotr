package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("poison", "8_28");
					put("poison2", "8_28");
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: [gollum] Spider, strength 8, vitality 8; fierce
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: the [gollum] minion who is no Shelob; a regroup ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6, vitality 3
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally who may exert to take part in skirmishes
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its "when you play" add is not met when cheated in
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("pursuer", "4_190");    // Uruk Pursuer: fierce, strength 5, vitality 3; its texts are about unbound Hobbits
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpiderPoisonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Spider Poison
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Remove a threat or a burden to transfer this possession to a companion skirmishing Shelob.
		 * Each time bearer is assigned to a skirmish, bearer must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("poison");

		assertEquals("Spider Poison", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovingAThreatTransfersItToTheCompanionSkirmishingShelobAndToNobodyInAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var poison = scn.GetShadowCard("poison");
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(poison);
		assertInZone(Zone.SUPPORT, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, gollum}, new PhysicalCardImpl[]{frodo, shelob});

		// Boromir fights Gollum first; Frodo is assigned to Shelob and his skirmish has not begun.  No burden is on
		// the Ring-bearer, so the threat is the one way to pay, and it is taken without a choice being asked.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertInZone(Zone.SUPPORT, poison);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(poison));
		scn.ShadowUseCardAction(poison);

		// The threat is paid, and neither the companion fighting Gollum nor the one assigned to Shelob takes it.
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, poison);
		assertNotAttachedTo(poison, boromir);
		assertNotAttachedTo(poison, frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(poison));
		scn.ShadowUseCardAction(poison);

		// In Shelob's own skirmish Frodo is the companion skirmishing her: he bears it now.
		assertEquals(0, scn.GetThreats());
		assertAttachedTo(poison, frodo);
		assertNotInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAThreatAndABurdenTheShadowPlayerChoosesAndABurdenPaysForAGondorManSkirmishingShelob() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var poison = scn.GetShadowCard("poison");
		var poison2 = scn.GetShadowCard("poison2");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(poison, poison2);
		assertInZone(Zone.SUPPORT, poison, poison2);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.AddThreats(1);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, shelob);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.ShadowActionAvailable(poison));
		scn.ShadowUseCardAction(poison);

		// Both can be paid: he is asked which.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("threat"));
		assertTrue(scn.ShadowChoiceAvailable("burden"));
		scn.ShadowChoose("Remove a burden");

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertAttachedTo(poison, boromir);
		// The other copy is not the one transferred.
		assertInZone(Zone.SUPPORT, poison2);
		assertNotAttachedTo(poison2, boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithABurdenAloneItIsPaidButAnAllySkirmishingShelobIsNoCompanionToTakeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var poison = scn.GetShadowCard("poison");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(poison);
		assertInZone(Zone.SUPPORT, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, shelob);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.ShadowActionAvailable(poison));
		scn.ShadowUseCardAction(poison);

		// With no threat the burden is the one way to pay, taken unasked; the ally is no companion.
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, poison);
		assertNotAttachedTo(poison, thrarin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoThreatAndNoBurdenItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		var poison = scn.GetShadowCard("poison");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(poison);
		assertInZone(Zone.SUPPORT, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, shelob);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(poison));
	}

	@Test
	public void EachBearerExertsWhenAssignedAndAnExhaustedBearerIsNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, boromir, arwen);

		var poison = scn.GetShadowCard("poison");
		var poison2 = scn.GetShadowCard("poison2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.AttachCardsTo(aragorn, poison);
		scn.AttachCardsTo(boromir, poison2);
		// Boromir, vitality 3, is exhausted.
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();

		assertAttachedTo(poison, aragorn);
		assertAttachedTo(poison2, boromir);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{boromir, runner2},
				new PhysicalCardImpl[]{arwen, runner3});

		// Aragorn exerts; Boromir, exhausted, cannot, and is not wounded either (he would die); Arwen bears nothing.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void BearerExertsAgainWhenTheShadowPlayerAssignsHimInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var poison = scn.GetShadowCard("poison");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.AttachCardsTo(aragorn, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();

		assertAttachedTo(poison, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsAssignToMinions(aragorn, pursuer);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Aragorn (8) beats the Pursuer (5), who lives through it and is fierce.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		assertTrue(scn.InFierceRound());
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(aragorn, pursuer);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ALeftoverMinionAssignedToTheBearerIsNoSecondAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var poison = scn.GetShadowCard("poison");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.AttachCardsTo(aragorn, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();

		assertAttachedTo(poison, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.ShadowAssignToMinions(aragorn, runner2);

		// CR 4.2 "assignment phase": the assignments take effect together; he is assigned to one skirmish, once.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
