package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_010_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armaments", "102_10");
					put("armaments2", "102_10");

					// Valiant Elves, all of vitality 3.  None of their texts does anything in these tests.
					put("defender", "68_9");     // Elven Defender: strength 5, not unique; stronger only beside an [elven] token
					put("balglin", "55_10");     // Balglin, Elven Warrior: strength 5, unique; takes fewer wounds from a wounded minion
					put("guardian", "54_76");    // Lorien Guardian: strength 5, not unique; a regroup ability
					put("erethon", "54_65");     // Erethon, Naith Lieutenant: strength 6, unique; his text needs a ranged weapon
					// Lorien Elf: an Elf companion who is not valiant, vitality 2, no text once in play.
					put("lorienelf", "1_53");
					// Household Guard: a [rohan] Man, vitality 3, whose whole text is "Valiant".
					put("guard", "5_83");

					put("sword", "4_64");        // Elven Sword: a hand weapon, with a skirmish ability
					put("bow", "1_41");          // Elven Bow: a ranged weapon; bearer is an archer
					put("cloak", "1_42");        // Elven Cloak: a cloak; the minion archery total is -1

					// Goblin Runner: strength 5, vitality 1.  Its text is about being played.
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("savage", "1_151");      // Uruk Savage: strength 5, vitality 3, damage +1
					put("troop", "1_177");       // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NaithArmamentsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Naith Armaments
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon / Ranged Weapon
		 * Strength: +2
		 * Vitality: +1
		 * Game Text: Bearer must be a valiant Elf.
		 * Each time bearer wins a skirmish, you may exert bearer to heal another valiant Elf.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armaments");

		assertEquals("Naith Armaments", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getPossessionClasses().size());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedOnAValiantElfWhoIsStrengthPlus2AndVitalityPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var armaments = scn.GetFreepsCard("armaments");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(defender, balglin, lorienelf, guard);
		scn.MoveCardsToHand(armaments);

		scn.StartGame();

		assertEquals(5, scn.GetStrength(defender));
		assertEquals(3, scn.GetVitality(defender));
		assertTrue(scn.FreepsPlayAvailable(armaments));

		scn.FreepsPlayCard(armaments);

		// A valiant Elf who is not unique and one who is; not the Elf who is not valiant, the valiant Man, or
		// Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(defender, balglin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorienelf, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(defender);

		assertAttachedTo(armaments, defender);
		assertEquals(7, scn.GetStrength(defender));
		assertEquals(4, scn.GetVitality(defender));
		assertEquals(5, scn.GetStrength(balglin));
		assertEquals(3, scn.GetVitality(balglin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBeBorneBesideAnotherHandWeaponOrAnotherRangedWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var guardian = scn.GetFreepsCard("guardian");
		var erethon = scn.GetFreepsCard("erethon");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(defender, balglin, guardian, erethon);
		scn.AttachCardsTo(defender, sword);
		scn.AttachCardsTo(balglin, bow);
		scn.AttachCardsTo(guardian, cloak);
		scn.MoveCardsToHand(armaments);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(armaments));

		scn.FreepsPlayCard(armaments);

		// It is a hand weapon and a ranged weapon at once, and a character bears one of each class: the Elf
		// with a sword and the Elf with a bow are both full.  The Elf with a cloak and the Elf with nothing
		// are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian, erethon));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(defender, balglin));
		scn.FreepsChooseCard(guardian);

		assertAttachedTo(armaments, guardian);
		assertAttachedTo(cloak, guardian);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenBearerWinsASkirmishHeMayExertToHealAnotherValiantElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var guardian = scn.GetFreepsCard("guardian");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(defender, balglin, guardian, lorienelf, guard);
		scn.AttachCardsTo(defender, armaments);

		// Everyone but the bearer is wounded, so that only who they are decides who may be healed.
		scn.AddWoundsToChar(balglin, 2);
		scn.AddWoundsToChar(guardian, 1);
		scn.AddWoundsToChar(lorienelf, 1);
		scn.AddWoundsToChar(guard, 1);

		var runner = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();

		// 5 + 2 for the Armaments against the Runner's 5
		scn.FreepsAssignAndResolve(defender, runner);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(defender));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armaments));

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(defender));
		// The other valiant Elves, unique and not unique.  Not the bearer, who now has a wound of his own; not
		// the Elf who is not valiant; not the valiant Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balglin, guardian));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(defender, lorienelf, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balglin);

		// One wound is healed, on the Elf chosen.
		assertEquals(1, scn.GetWoundsOn(balglin));
		assertEquals(1, scn.GetWoundsOn(guardian));
		assertEquals(1, scn.GetWoundsOn(lorienelf));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(defender));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningExertsNobodyAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		scn.MoveCompanionsToTable(defender, balglin);
		scn.AttachCardsTo(defender, armaments);
		scn.AddWoundsToChar(balglin, 1);

		var runner = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(defender, runner);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armaments));

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(1, scn.GetWoundsOn(balglin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		scn.MoveCompanionsToTable(defender, balglin);
		scn.AttachCardsTo(defender, armaments);
		scn.AddWoundsToChar(balglin, 1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();

		// 7 against 13: lost, and not overwhelmed.
		scn.FreepsAssignAndResolve(defender, troop);
		scn.PassSkirmishActions();

		// The wound is the one the skirmish dealt.
		assertEquals(1, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(balglin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachCopyAnswersToItsOwnBearerAndAWinByAnElfWithNoCopyOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var armaments2 = scn.GetFreepsCard("armaments2");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var guardian = scn.GetFreepsCard("guardian");
		var erethon = scn.GetFreepsCard("erethon");
		scn.MoveCompanionsToTable(defender, balglin, guardian, erethon);
		scn.AttachCardsTo(defender, armaments);
		scn.AttachCardsTo(balglin, armaments2);
		scn.AddWoundsToChar(guardian, 1);
		scn.AddWoundsToChar(erethon, 1);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{erethon, runner1},
				new PhysicalCardImpl[]{defender, runner2},
				new PhysicalCardImpl[]{balglin, runner3});

		// Erethon bears neither copy: his 6 beats the Runner's 5 and nothing is offered.
		scn.FreepsResolveSkirmish(erethon);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The Defender wins: his own copy offers, and Balglin's does not.
		scn.FreepsResolveSkirmish(defender);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armaments));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armaments2));

		scn.FreepsChooseOptionalTrigger(armaments);
		assertEquals(1, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(balglin));
		// Balglin is another valiant Elf, but has no wound to heal.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian, erethon));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(defender, balglin));
		scn.FreepsChooseCard(guardian);
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(1, scn.GetWoundsOn(erethon));

		// Balglin wins: now it is his copy that offers, and he who exerts.  To him the Defender is another
		// valiant Elf, with the wound of his own exertion to heal.
		scn.FreepsResolveSkirmish(balglin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner3);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armaments2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(armaments));

		scn.FreepsChooseOptionalTrigger(armaments2);
		assertEquals(1, scn.GetWoundsOn(balglin));
		assertEquals(1, scn.GetWoundsOn(defender));
		assertTrue(scn.FreepsHasCardChoiceAvailable(defender, erethon));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(balglin, guardian));
		scn.FreepsChooseCard(defender);

		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(1, scn.GetWoundsOn(balglin));
		assertEquals(1, scn.GetWoundsOn(erethon));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedToAnExhaustedBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		scn.MoveCompanionsToTable(defender, balglin);
		scn.AttachCardsTo(defender, armaments);
		// Vitality 3 + 1 with 3 wounds: one left, so he cannot exert.
		scn.AddWoundsToChar(defender, 3);
		scn.AddWoundsToChar(balglin, 1);

		var runner = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();

		assertEquals(3, scn.GetWoundsOn(defender));
		assertEquals(1, scn.GetVitality(defender));
		assertTrue(scn.IsExhausted(defender));

		scn.FreepsAssignAndResolve(defender, runner);
		scn.PassSkirmishActions();

		// He won, and there is a wound on another valiant Elf to heal.
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(defender));
		assertEquals(1, scn.GetWoundsOn(balglin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsOfferedAgainWhenBearerWinsASecondSkirmishInTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		scn.MoveCompanionsToTable(defender, balglin);
		scn.AttachCardsTo(defender, armaments);
		scn.AddWoundsToChar(balglin, 2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// A minion the bearer beats without killing, made fierce so that he fights it twice.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();

		// 7 against 5
		scn.FreepsAssignAndResolve(defender, savage);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armaments));
		scn.FreepsAcceptOptionalTrigger();
		// Balglin is the only other valiant Elf, so he is healed without a choice.
		assertEquals(1, scn.GetWoundsOn(defender));
		assertEquals(1, scn.GetWoundsOn(balglin));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(defender, savage);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armaments));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(balglin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsOfferedWhileNoOtherValiantElfHasAWoundAndAcceptedExertsBearerAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armaments = scn.GetFreepsCard("armaments");
		var defender = scn.GetFreepsCard("defender");
		var balglin = scn.GetFreepsCard("balglin");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(defender, balglin, lorienelf, guard);
		scn.AttachCardsTo(defender, armaments);

		// Another valiant Elf with no wound; a wounded Elf who is not valiant; a wounded valiant Man; and the
		// bearer himself, wounded.
		scn.AddWoundsToChar(defender, 1);
		scn.AddWoundsToChar(lorienelf, 1);
		scn.AddWoundsToChar(guard, 1);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// A minion the bearer beats without killing, made fierce so that he wins twice on the one board: the
		// offer is declined the first time and accepted the second.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();

		// 7 against 5
		scn.FreepsAssignAndResolve(defender, savage);
		scn.PassSkirmishActions();

		// The bearer won and can exert.  The exertion would heal nobody, and the offer is made all the same:
		// the text asks only that the cost can be paid.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(balglin));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armaments));

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(balglin));
		assertEquals(1, scn.GetWoundsOn(lorienelf));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(defender, savage);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armaments));

		scn.FreepsAcceptOptionalTrigger();

		// The cost is paid.  A character with no wound cannot be healed, so there is nobody to choose and
		// nothing is asked: not the unwounded valiant Elf, not the Elf who is not valiant, not the valiant Man,
		// and not the bearer, who is not "another".
		assertEquals(2, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(balglin));
		assertEquals(1, scn.GetWoundsOn(lorienelf));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
