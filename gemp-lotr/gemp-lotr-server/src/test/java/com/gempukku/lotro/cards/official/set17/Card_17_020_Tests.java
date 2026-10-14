package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_020_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gwaihir", "17_20");

					// Gandalf, Friend of the Shirefolk: [gandalf] Wizard, strength 6, +1 for each of Hobbit, Dwarf, Elf
					// and Man in the fellowship: 7 beside Frodo, 8 with Gwaihir's +1.
					put("gandalf", "1_72");
					// Radagast, The Brown: the [gandalf] Wizard companion who is no Gandalf, strength 8. The move limit
					// is +1; his other text answers a move in the regroup phase, which no test makes.
					put("radagast", "9_26");
					// Ent Avenger: a [gandalf] companion who is no Wizard, strength 6. His bonus wants 3 Ents.
					put("avenger", "6_27");

					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else.
					put("savage1", "1_151");
					put("savage2", "1_151");
					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text.
					put("troop", "1_177");
					// Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; its texts are about unbound
					// Hobbits, of whom there are none.
					put("pursuer", "4_190");
					// Saruman, Coldly Still: a [men] Wizard minion, strength 8, vitality 4; "Each [men] minion bearing
					// a possession is strength +2"; his skirmish ability is not used.
					put("saruman", "17_48");
					// The Mouth of Sauron, Messenger of Mordor: a [men] Man minion, vitality 3; his maneuver ability is
					// not used.
					put("mouth", "12_73");
					// Messenger's Mount: on The Mouth of Sauron, who is fierce. "Response: If a [men] minion is about
					// to take a wound, remove (3) to prevent that": offered at a wound and not at an exertion.
					put("mount", "12_72");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GwaihirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Gwaihir, The Windlord
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Follower
		 * Strength: +1
		 * Resistance: +1
		 * Aid: Add a threat.
		 * Game Text: <b>Aid</b> – Add a threat. <helper>(At the start of the maneuver phase, you may add a threat to transfer this to a companion.)</helper><br>If bearer is a [gandalf] Wizard, each time bearer wins a skirmish, you may exert a minion (if that minion is a Wizard, wound it instead).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gwaihir");

		assertEquals("Gwaihir", card.getBlueprint().getTitle());
		assertEquals("The Windlord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void AidAddsAThreatToTransferGwaihirToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(gwaihir);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, gwaihir);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf));
		scn.FreepsChooseCard(gandalf);

		// Aid - add a threat: a threat, and no twilight.
		assertEquals(1, scn.GetThreats());
		assertEquals(pool, scn.GetTwilight());
		assertAttachedTo(gwaihir, gandalf);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhenGandalfBearingGwaihirWinsASkirmishAMinionAnywhereMayBeExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gwaihir);

		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// His Aid is offered for him where he is borne already; he stays with Gandalf.
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// Gandalf 8 beats the Savage 5, who takes his wound.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));
		scn.FreepsAcceptOptionalTrigger();

		// A minion of either culture and race, the one he beat and one in no skirmish; no companion. Exactly one.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningWhenRadagastBearingGwaihirWinsExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(radagast);
		scn.AttachCardsTo(radagast, gwaihir);

		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(radagast, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(radagast);
		scn.PassSkirmishActions();

		// Radagast 9 beats the Savage: a [gandalf] Wizard of another title is a bearer the text takes.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenGandalfBearingGwaihirLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gwaihir);

		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.ShadowDeclineAssignments();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// The Troop 13 beats Gandalf 8 (not by double); the Savage and the Troop could both exert.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gwaihir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnEntAvengerBearingGwaihirWinsNorWhenGandalfNotBearingHimWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.AttachCardsTo(avenger, gwaihir);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{avenger, savage1}, new PhysicalCardImpl[]{gandalf, savage2});

		// His bearer, a [gandalf] companion who is no Wizard, wins (7 against 5).
		scn.FreepsResolveSkirmish(avenger);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gwaihir));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));

		// Gandalf, a [gandalf] Wizard who does not bear him, wins (7 against 5).
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gwaihir));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoMinionCanBeExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gwaihir);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// The Savage, the only minion, is left with 2 wounds on vitality 3: exhausted, and no Wizard.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.IsExhausted(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gwaihir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeGandalfWinsAMinionIsExertedOrAWizardMinionWoundedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gwaihir);

		var pursuer = scn.GetShadowCard("pursuer");
		var saruman = scn.GetShadowCard("saruman");
		var mouth = scn.GetShadowCard("mouth");
		var mount = scn.GetShadowCard("mount");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, saruman, mouth);
		scn.AttachCardsTo(mouth, mount);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.ShadowDeclineAssignments();
		// Enough for the Mount's (3), so that its Response is offered at any wound to a [men] minion.
		scn.SetTwilight(5);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// Gandalf beats the fierce Pursuer a first time.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer, saruman, mouth));
		assertEquals(0, scn.GetWoundsOn(mouth));
		scn.FreepsChooseCard(mouth);

		// The Mouth, a [men] minion who is no Wizard, is exerted: no wound, so the Mount answers nothing.
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// And a second time: offered again.
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsChooseCard(saruman);

		// Saruman is a Wizard: he is about to take a wound, and the Mount may prevent it.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mount));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedWizardMinionMayBeChosenAndIsWoundedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, gwaihir);

		var savage = scn.GetShadowCard("savage1");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, saruman);
		scn.AddWoundsToChar(saruman, 3);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		// Saruman, vitality 4 with 3 wounds, is exhausted and cannot be exerted; as a Wizard he is wounded instead,
		// so he may be chosen beside the Savage, who can be exerted.
		assertTrue(scn.IsExhausted(saruman));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, saruman));
		scn.FreepsChooseCard(saruman);

		// The fourth wound kills him.
		assertInDiscard(saruman);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
