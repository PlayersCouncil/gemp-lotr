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

public class Card_08_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "8_21");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is about his own strength
					// Ent Avenger: a [gandalf] companion who is not Gandalf; strength +2 while 3 Ents can be spotted
					put("avenger", "6_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "8_21");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is about his own strength
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("troop2", "1_177");
					// White Hand Destroyer: fierce [uruk-hai] Uruk-hai, strength 10, vitality 3; hunter only for each
					// site its owner controls (none)
					put("destroyer", "17_124");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHorseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "8_21");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is about his own strength
					// Horse of Rohan: [rohan] mount, "To play, spot a [rohan] Man.  Bearer must be a Man, Elf, or Wizard";
					// its skirmish text is never reached
					put("horse1", "4_283");
					put("horse2", "4_283");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant and no other text
					put("rider", "4_265");      // Elite Rider: a [rohan] Man companion; his text is about his own cost and strength
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowfaxStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Shadowfax, Greatheart
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be Gandalf.<br>At the start of each skirmish involving Gandalf, each minion skirmishing Gandalf must exert.<br><b>Skirmish:</b> Add a threat to make Gandalf strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadowfax");

		assertEquals("Shadowfax", card.getBlueprint().getTitle());
		assertEquals("Greatheart", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnlyOnGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(gandalf, shadowfax);

		scn.StartGame();

		// Frodo and the Ent Avenger, a [gandalf] companion, and no Gandalf: nobody may bear it.
		assertInHand(shadowfax);
		assertFalse(scn.FreepsPlayAvailable(shadowfax));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(shadowfax));

		// Gandalf is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(shadowfax);
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfGandalfsSkirmishEachMinionSkirmishingHimExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, shadowfax);

		var savage = scn.GetShadowCard("savage");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(savage, troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, savage}, new PhysicalCardImpl[]{aragorn, troop2});
		// The Shadow player puts the minion left over on Gandalf as well.
		scn.ShadowAssignToMinions(gandalf, troop1);

		// Aragorn's skirmish first: it does not involve Gandalf, and nobody exerts.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop1));
		scn.PassSkirmishActions();

		// Gandalf's: an [isengard] Uruk-hai and a [moria] Orc skirmish him, and each exerts; the minion that
		// fought Aragorn does not.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
	}

	@Test
	public void AnExhaustedMinionSkirmishingGandalfIsNeitherExertedNorKilledByIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		var savage = scn.GetShadowCard("savage");
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(savage, troop1);
		// Vitality 3 with 2 wounds: the Goblin Patrol Troop cannot exert, and a wound would kill it.
		scn.AddWoundsToChar(troop1, 2);
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.ShadowAssignToMinions(gandalf, troop1);

		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop1));
	}

	@Test
	public void AFierceMinionSkirmishingGandalfExertsAgainAtTheStartOfTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		var destroyer = scn.GetShadowCard("destroyer");

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(destroyer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, destroyer);

		assertEquals(0, scn.GetWoundsOn(destroyer));
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(1, scn.GetWoundsOn(destroyer));
		// Gandalf is strength 7 (6, +1 for the Hobbit in the fellowship) and loses to 10.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(destroyer));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, destroyer);
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetWoundsOn(destroyer));
	}

	@Test
	public void SkirmishAbilityAddsAThreatToMakeGandalfStrengthPlus1UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, shadowfax);

		var savage = scn.GetShadowCard("savage");
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(savage, troop1);

		// A Skirmish ability: not in the assignment phase before.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shadowfax));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, savage}, new PhysicalCardImpl[]{aragorn, troop1});
		scn.FreepsResolveSkirmish(gandalf);

		// 6, +1 for the Hobbit and +1 for the Man in the fellowship
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(shadowfax));

		scn.FreepsUseCardAction(shadowfax);
		assertEquals(1, scn.GetThreats());
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gandalf's 9 beats the Uruk Savage; in Aragorn's skirmish, the next, Gandalf is 8 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(1, scn.GetThreats());
	}

	@Test
	public void SkirmishAbilityStrengthensAnUnassignedGandalfAndIsNotOfferedAtTheThreatLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, shadowfax);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop1);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(shadowfax));
		scn.FreepsUseCardAction(shadowfax);
		assertEquals(1, scn.GetThreats());
		assertEquals(9, scn.GetStrength(gandalf));
		scn.ShadowPass();

		// Three companions: three threats are the limit, and no more can be added.
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		assertFalse(scn.FreepsActionAvailable(shadowfax));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder": "If the hindered card is an item (possession or artifact), it continues to
	// restrict its bearer from bearing any other cards of the same item class via play or transfer.  Example: If
	// Shadowfax is hindered on Gandalf, you cannot play a Horse of Rohan on Gandalf."
	@Test
	public void HinderedOnGandalfItStillKeepsAHorseOfRohanOffHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHorseScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var horse1 = scn.GetFreepsCard("horse1");
		var horse2 = scn.GetFreepsCard("horse2");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, guard, rider, aragorn);
		scn.MoveCardsToHand(horse1, horse2);

		scn.StartGame();

		// Without Shadowfax, Gandalf is a Wizard the Horse may go on.
		assertInHand(horse1, horse2);
		scn.FreepsPlayCard(horse1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, guard, rider, aragorn));
		scn.FreepsChooseCard(guard);
		assertAttachedTo(horse1, guard);

		scn.AttachCardsTo(gandalf, shadowfax);
		scn.HinderCard(shadowfax);
		scn.FreepsRefreshDecision();
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.IsHindered(shadowfax));

		scn.FreepsPlayCard(horse2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, guard));
		scn.FreepsChooseCard(rider);
		assertAttachedTo(horse2, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
