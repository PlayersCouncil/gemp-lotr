package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "7_114");
					put("pippin", "1_306");       // Pippin, Friend to Frodo: [shire] Hobbit companion, strength 3, vitality 4; his text is about [shire] tales
					put("trollslayer", "103_31"); // Pippin, Trollslayer: the [gondor] Pippin, enduring; cheated in past his "To play", his own skirmish ability used only on purpose
					put("merry", "1_302");        // Merry, Friend to Sam: a [shire] Hobbit companion who is no Pippin, vitality 4; a skirmish ability only

					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability only
					put("deadman", "10_27");      // Dead Man of Dunharrow: a [gondor] companion who is a Wraith (cheated in: no threat, no exertions)
					put("merchant", "7_97");      // Gondorian Merchant: [gondor] Man ally; a maneuver ability only, needing initiative
					put("guard", "5_83");         // Household Guard: a [rohan] Man companion; Valiant is its whole text

					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1; its trigger is on being played
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Pippin's Sword
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Pippin.
		 * <b>Skirmish:</b> Exert Pippin twice make him strength +1 for each [gondor] companion you spot.
		*/

		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");

		assertEquals("Pippin's Sword", sword.getBlueprint().getTitle());
		assertNull(sword.getBlueprint().getSubtitle());
		assertTrue(sword.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sword.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, sword.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, sword.getBlueprint().getCardType());
		assertTrue(sword.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, sword.getBlueprint().getPossessionClasses().size());
		assertEquals(1, sword.getBlueprint().getTwilightCost());
		assertEquals(2, sword.getBlueprint().getStrength());
	}

	@Test
	public void IsNotPlayableWithoutPippinAndGoesOnPippinOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(sword);
		assertInHand(sword);

		scn.StartGame();

		// A Hobbit companion of Pippin's culture who is no Pippin, and the Ring-bearer: not playable.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sword));

		scn.MoveCompanionsToTable(pippin);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);

		// Pippin is the one bearer it can go on: nothing is asked.
		assertAttachedTo(sword, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingPippinTwiceMakesHimStrongerByTheNumberOfGondorCompanionsNamedUntilHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var deadman = scn.GetFreepsCard("deadman");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(pippin, merry, boromir, deadman, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(pippin, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		assertAttachedTo(sword, pippin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, runner}, new PhysicalCardImpl[]{boromir, troop});
		scn.FreepsResolveSkirmish(pippin);

		// 3 + 2 for the sword.
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(sword));

		scn.FreepsUseCardAction(sword);
		// Pippin exerts twice; Merry, who could, does not.
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));

		// The [gondor] companions are Boromir (a Man) and the Dead Man (a Wraith): none, or up to those 2.  Not the
		// [gondor] ally, the [rohan] Guard, Merry or Frodo.  It comes filled in with both (F-0561); fewer may be named.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.FreepsGetChoiceDefault());
		assertEquals(5, scn.GetStrength(pippin));
		scn.FreepsChoose("1");

		// One named of two: 5 + 1, not + 2.  Boromir, the companion beside him, is unchanged.
		assertEquals(6, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 6 against 5: Pippin wins.  In the next skirmish, Boromir's, the bonus is gone.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(5, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheGondorPippinBearsItPaysTheExertionsAndCountsHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(trollslayer, merry, boromir);
		scn.MoveCardsToHand(sword);
		assertInHand(sword);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, trollslayer);

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(trollslayer, troop);
		scn.FreepsResolveSkirmish(trollslayer);

		assertEquals(5, scn.GetStrength(trollslayer));
		assertEquals(0, scn.GetWoundsOn(trollslayer));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(sword));

		scn.FreepsUseCardAction(sword);
		assertEquals(2, scn.GetWoundsOn(trollslayer));
		assertEquals(0, scn.GetWoundsOn(merry));
		// Enduring: 3 + 2 for the sword + 2 for each of his two wounds.
		assertEquals(9, scn.GetStrength(trollslayer));

		// He is a [gondor] companion himself: he and Boromir, 2.
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		assertEquals(11, scn.GetStrength(trollslayer));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGondorCompanionToSpotTheExertionsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		// A [gondor] ally and a [rohan] Man companion: neither is a [gondor] companion.
		scn.MoveCompanionsToTable(pippin, merry, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(pippin, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(sword, pippin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, runner);
		scn.FreepsResolveSkirmish(pippin);

		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(sword));

		scn.FreepsUseCardAction(sword);

		// The exertions are paid, no number is asked (0 is the only one), and nothing is added.
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(5, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsUsedByPippinAssignedToNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(pippin, boromir);
		scn.AttachCardsTo(pippin, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertAttachedTo(sword, pippin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);

		// The text names no skirmish of his: offered during Boromir's.
		assertFalse(scn.IsCharAssigned(pippin));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(sword));

		scn.FreepsUseCardAction(sword);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertEquals(6, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenPippinCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(pippin, merry, boromir);
		scn.AttachCardsTo(pippin, sword);
		// Vitality 4 with 2 wounds: one exertion left, not two.
		scn.AddWoundsToChar(pippin, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(sword, pippin);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, runner);
		scn.FreepsResolveSkirmish(pippin);

		// Merry could exert twice, but "Exert Pippin twice" is Pippin's to pay.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sword));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
	}
}
