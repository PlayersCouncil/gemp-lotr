package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "11_40");
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
					put("shadowfax", "11_40");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is about his own strength
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
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
					put("shadowfax", "11_40");
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
		 * Set: 11
		 * Name: Shadowfax, Unequaled Steed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Resistance: +2
		 * Game Text: Bearer must be Gandalf.<br>Each time Gandalf is about to take a wound, you may add a burden to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadowfax");

		assertEquals("Shadowfax", card.getBlueprint().getTitle());
		assertEquals("Unequaled Steed", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getResistance());
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
	public void EachWoundGandalfIsAboutToTakeMayBePreventedByABurdenAndNoOtherCompanionsIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, shadowfax);

		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(raiders, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, raiders}, new PhysicalCardImpl[]{aragorn, troop});

		// Aragorn's skirmish first: 8 against 13, and his wound is not Gandalf's, so nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());

		// Gandalf is 8 (6, +1 for the Hobbit and +1 for the Man in the fellowship) and loses to 9; damage +1 makes
		// it two wounds, each offered in turn.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(8, scn.GetStrength(gandalf));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// The second wound is offered again; declined, Gandalf takes it and no burden is added.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shadowfax));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
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
