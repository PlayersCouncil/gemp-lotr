package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_255_Tests
{

	// The Fellowship path, so that Edoras Hall 7_330 (each [rohan] possession costs 1 less) is not under the plays.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "7_255");
					put("sonofthengel", "4_292"); // Théoden, Son of Thengel: another Théoden, cost 2, held in hand only
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, cost 2; Valiant is its whole text
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (a Man only beside three [rohan] companions or
					// Éowyn), vitality 4.  His trigger answers another [rohan] Man exerting: nobody exerts here.
					put("merry", "103_85");
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man who is an ally
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: a [rohan] Man ally; an archery ability that needs a villager
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man and a companion, not [rohan]
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion; a skirmish ability used only on purpose

					put("sword", "4_291");      // Sword of Rohan: [rohan] possession, hand weapon, cost 1, "Bearer must be a [rohan] Man"
					put("sword2", "4_291");
					put("chain", "4_278");      // Heavy Chain: [rohan] possession, armor, cost 1, "Bearer must be a Man"
					put("shield", "1_107");     // Great Shield: a [gondor] possession, cost 1, that a Man may bear
					put("hall", "11_149");      // Protecting the Hall: a [rohan] condition borne by a [rohan] Man
					put("herugrim", "54_280");  // Herugrim: a [rohan] artifact (a hand weapon), "Bearer must be Théoden"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Théoden, Rekindled King
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: While you can spot a [rohan] Man, Théoden's twilight cost is -1.<br><b>Fellowship:</b> If the twilight pool has fewer than 2 twilight tokens, play a [rohan] possession on Théoden and add (2) to heal another [rohan] companion.
		*/

		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", theoden.getBlueprint().getTitle());
		assertEquals("Rekindled King", theoden.getBlueprint().getSubtitle());
		assertTrue(theoden.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, theoden.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, theoden.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, theoden.getBlueprint().getCardType());
		assertEquals(Race.MAN, theoden.getBlueprint().getRace());
		assertEquals(3, theoden.getBlueprint().getTwilightCost());
		assertEquals(7, theoden.getBlueprint().getStrength());
		assertEquals(3, theoden.getBlueprint().getVitality());
		assertEquals(6, theoden.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, theoden.getBlueprint().getSignet());
	}

	@Test
	public void PlayedForThreeBesideAManOfAnotherCultureAndARohanHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(theoden);
		// Merry is a Man only beside three [rohan] companions or Éowyn: here he is a [rohan] Hobbit.
		scn.MoveCompanionsToTable(aragorn, merry);

		scn.StartGame();

		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(theoden));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(theoden));
		scn.FreepsPlayCard(theoden);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CostsOneLessOnceARohanManIsPlayedAndAnotherTheodenInHandDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var sonOfThengel = scn.GetFreepsCard("sonofthengel");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(theoden, sonOfThengel, guard);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertEquals(3, scn.GetTwilightCost(theoden));
		assertEquals(2, scn.GetTwilightCost(sonOfThengel));

		scn.FreepsPlayCard(guard);
		assertEquals(2, scn.GetTwilight());

		// The text names this card: the other Théoden keeps his printed 2.
		assertEquals(2, scn.GetTwilightCost(theoden));
		assertEquals(2, scn.GetTwilightCost(sonOfThengel));

		scn.FreepsPlayCard(theoden);

		// 2 for the Guard + 3 - 1 for Théoden
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		// In play his cost is the number printed on him, though he is a [rohan] Man himself beside another.
		assertEquals(3, scn.GetTwilightCost(theoden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManWhoIsAnAllyIsSpottedForTheLowerCostWhichLeavesThePoolTooFullForHisAbilityUntilItIsBelowTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToHand(theoden, sword);
		scn.MoveAlliesToTable(guma);
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetTwilightCost(theoden));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(theoden);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		// 2 twilight tokens are not fewer than 2.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(theoden));

		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(theoden));
	}

	@Test
	public void FellowshipAbilityPlaysARohanPossessionOnHimAndAddsTwoToHealAnotherRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		var chain = scn.GetFreepsCard("chain");
		var shield = scn.GetFreepsCard("shield");
		var hall = scn.GetFreepsCard("hall");
		var herugrim = scn.GetFreepsCard("herugrim");
		scn.MoveCompanionsToTable(theoden, guard, merry, boromir);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(sword, chain, shield, hall, herugrim);
		// The Guard carries 2 wounds, so that one heal reads 1.
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(theoden, 1);
		scn.AddWoundsToChar(aldor, 1);
		scn.AddWoundsToChar(boromir, 1);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInHand(sword, chain, shield, hall, herugrim);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);

		// "A [rohan] possession": a hand weapon and an armor; not a [gondor] possession, a [rohan] condition or a
		// [rohan] artifact, though Théoden could bear each of them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shield, hall, herugrim));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chain);
		assertAttachedTo(chain, theoden);
		assertEquals(0, scn.GetAttachedCards(guard).size());
		// 1 + 1 for the Chain + 2 added
		assertEquals(4, scn.GetTwilight());

		// "Another [rohan] companion": the Guard and Merry (a Man here, beside three [rohan] companions); not Théoden
		// himself, a [rohan] ally or a companion of [gondor].
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden, aldor, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInHand(sword, shield, hall, herugrim);

		// The pool is no longer below 2.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(theoden));
	}

	@Test
	public void ARohanCompanionWhoIsAHobbitIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var merry = scn.GetFreepsCard("merry");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(theoden, merry);
		scn.MoveCardsToHand(chain);
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();

		// Two [rohan] companions and no Éowyn: Merry is a Hobbit.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertInHand(chain);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(theoden);

		// The one possession, and the one other wounded [rohan] companion, are taken unasked.
		assertAttachedTo(chain, theoden);
		// 0 + 1 for the Chain + 2 added
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoOtherRohanCompanionWoundedTheAbilityIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.MoveCardsToHand(sword);
		// Théoden himself is the only wounded [rohan] companion.
		scn.AddWoundsToChar(theoden, 1);

		scn.StartGame();

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInHand(sword);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);

		// The one possession in hand goes on him unasked.
		assertAttachedTo(sword, theoden);
		// 0 + 1 for the Sword + 2 added
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithNoRohanPossessionInHandThatTheodenCanBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var shield = scn.GetFreepsCard("shield");
		var hall = scn.GetFreepsCard("hall");
		var herugrim = scn.GetFreepsCard("herugrim");
		scn.MoveCompanionsToTable(theoden, guard);
		// He bears one hand weapon already: the second Sword is no possession he can be given.
		scn.AttachCardsTo(theoden, sword);
		scn.MoveCardsToHand(sword2, shield, hall, herugrim);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();

		assertAttachedTo(sword, theoden);
		assertInHand(sword2, shield, hall, herugrim);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(theoden));
	}
}
