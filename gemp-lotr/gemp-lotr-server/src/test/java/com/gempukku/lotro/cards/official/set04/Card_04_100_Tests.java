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

public class Card_04_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "4_100");
					// Gandalf, Friend of the Shirefolk: his text is about his own strength
					put("gandalf", "1_72");
					// Glamdring: a hand weapon, cost 2.  "Bearer must be Gandalf."
					put("glamdring", "1_75");

					// What restores Shadowfax by legal play:
					// Mantle of the White Wizard, [gandalf] condition in the support area: "Each time you
					// play a spell, you may hinder a [gandalf] artifact to restore a Free Peoples card."
					put("mantle", "103_3");
					// Gandalf's Staff: a [gandalf] artifact, a staff and no hand weapon ("The twilight cost of
					// each [gandalf] spell is -1")
					put("staff", "2_22");
					// A New Light: [gandalf] Fellowship event, a spell, cost 1.  To play, spot a [gandalf]
					// Wizard.  It takes a minion from the Shadow player's discard pile, where there is none.
					put("light", "17_15");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "4_100");
					// Gandalf, Friend of the Shirefolk: his text is about his own strength
					put("gandalf", "1_72");
					// Ent Avenger: a [gandalf] companion who is not Gandalf; strength +2 while 3 Ents can be spotted
					put("avenger", "6_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetWeaponScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadowfax", "4_100");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is about his own strength
					put("glamdring", "1_75");   // Glamdring: a hand weapon, "Bearer must be Gandalf"; an ability used only on purpose
					// Gandalf's Staff: a [gandalf] artifact, a staff and no hand weapon ("The twilight cost of each
					// [gandalf] spell is -1"); a skirmish ability never used
					put("staff", "2_22");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("sword", "1_112");      // Ranger's Sword: a hand weapon, "Bearer must be Aragorn"
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
					put("shadowfax", "4_100");
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
					put("shadowfax", "4_100");
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
		 * Set: 4
		 * Name: Shadowfax
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +2
		 * Game Text: Bearer must be Gandalf. Discard any hand weapon he bears. Gandalf may not bear a hand weapon.
		 * At the start of each skirmish involving Gandalf, each minion skirmishing him must exert.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadowfax");

		assertEquals("Shadowfax", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(gandalf, shadowfax);

		scn.StartGame();

		// Frodo and the Ent Avenger, a [gandalf] companion, and no Gandalf: nobody may bear it.
		assertFalse(scn.FreepsPlayAvailable(shadowfax));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(shadowfax));

		// Gandalf is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(shadowfax);
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheHandWeaponBarAndDiscardReachGandalfAloneAndNoItemOfAnotherClass() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeaponScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(shadowfax, staff, sword);

		scn.StartGame();

		assertAttachedTo(glamdring, gandalf);
		scn.FreepsPlayCard(shadowfax);
		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.DISCARD, glamdring);

		// Another companion may still be played a hand weapon, and it stays on him.
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);

		// A staff is no hand weapon: Gandalf may still bear one.
		assertTrue(scn.FreepsPlayAvailable(staff));
		scn.FreepsPlayCard(staff);
		assertAttachedTo(staff, gandalf);
		assertAttachedTo(sword, aragorn);
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
	public void AFierceMinionSkirmishingGandalfExertsAgainAtTheStartOfTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		var destroyer = scn.GetShadowCard("destroyer");

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, destroyer);

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(1, scn.GetWoundsOn(destroyer));
		// Gandalf is strength 9 (6, +1 for the Hobbit in the fellowship, +2 for Shadowfax) and loses to 10.
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
		scn.FreepsPlayCard(horse1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, guard, rider, aragorn));
		scn.FreepsChooseCard(guard);
		assertAttachedTo(horse1, guard);

		scn.AttachCardsTo(gandalf, shadowfax);
		scn.HinderCard(shadowfax);
		scn.FreepsRefreshDecision();
		assertTrue(scn.IsHindered(shadowfax));

		scn.FreepsPlayCard(horse2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, guard));
		scn.FreepsChooseCard(rider);
		assertAttachedTo(horse2, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// "Discard any hand weapon he bears" is a standing discard, apart from "Gandalf may not bear a hand weapon"
	// (Ketura on Q-0223 and Q-0229: two separate effects, and the discard is continuous).  While Shadowfax's text
	// is in force the bar keeps every hand weapon from reaching Gandalf, by a play or by a transfer, so the
	// discard is seen at two moments only: as Shadowfax is played on a Gandalf who bears one, here, and as it is
	// restored, in the two tests after.
	@Test
	public void WhenPlayedDiscardsTheHandWeaponGandalfBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring, staff);
		scn.MoveCardsToHand(shadowfax);

		scn.StartGame();

		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.FreepsPlayAvailable(shadowfax));
		scn.FreepsPlayCard(shadowfax);

		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.DISCARD, glamdring);
		// The Staff is no hand weapon: it stays on him
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder", on a hindered item: "the game text of such items does not apply, so any
	// restrictions that come from the game text of a hindered card are ignored", with Anduril, Flame of the West
	// 7_79 as the example: "Upon being restored, Anduril's game text resumes being applied, upon which Aragorn's
	// Bow would be discarded."  Shadowfax has the same two sentences, "Discard any hand weapon he bears.  Gandalf
	// may not bear a hand weapon".  A card restores it here, and a reconcile in the test after.
	@Test
	public void WhenRestoredDiscardsAHandWeaponGandalfTookUpWhileItWasHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var mantle = scn.GetFreepsCard("mantle");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax, staff);
		scn.MoveCardsToSupportArea(mantle);
		scn.MoveCardsToHand(glamdring, light);

		scn.StartGame();

		assertAttachedTo(shadowfax, gandalf);
		assertAttachedTo(staff, gandalf);
		// "Gandalf may not bear a hand weapon"
		assertFalse(scn.FreepsPlayAvailable(glamdring));

		scn.HinderCard(shadowfax);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(glamdring));
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);

		// The spell is played, and the Mantle hinders the Staff (the one [gandalf] artifact) to restore Shadowfax
		scn.FreepsPlayCard(light);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(staff));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowfax, staff));
		scn.FreepsChooseCard(shadowfax);

		assertFalse(scn.IsHindered(shadowfax));
		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.DISCARD, glamdring);
		// The Staff is no hand weapon: it stays on him
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// The same board with the Free Peoples player's own reconcile doing the restoring, which has no card behind it
	// (Comprehensive Rules 5.0, "reconcile": "When any player reconciles (before they discard any cards from
	// hand), restore all cards owned by that player").
	@Test
	public void WhenRestoredByAReconcileDiscardsAHandWeaponGandalfTookUpWhileItWasHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowfax = scn.GetFreepsCard("shadowfax");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax, staff);
		// The spell stays in hand, so that the reconcile has a discard to ask about
		scn.MoveCardsToHand(glamdring, light);

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.HAND, light);
		scn.HinderCard(shadowfax);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertTrue(scn.IsHindered(shadowfax));

		// No minion: the Shadow phase is followed by the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.IsHindered(shadowfax));
		assertAttachedTo(glamdring, gandalf);

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(shadowfax));
		assertAttachedTo(shadowfax, gandalf);
		assertInZone(Zone.DISCARD, glamdring);
		// The Staff is no hand weapon: it stays on him
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.HAND, light);
	}
}
