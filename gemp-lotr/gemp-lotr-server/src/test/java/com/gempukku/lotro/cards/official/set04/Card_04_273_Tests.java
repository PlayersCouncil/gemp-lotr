package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_04_273_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fight", "4_273");
					put("ceorl", "4_264");      // Ceorl, Weary Horseman: [rohan] Man companion, strength 6; a response to a site taken
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally, strength 3, no villager; an archery ability
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit, no Man beside one other [rohan] companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8; a maneuver ability
					put("herdsman", "7_247");   // Rohirrim Herdsman: a villager ([rohan] Man ally, not unique, strength 4); a regroup ability
					put("guma", "4_277");       // Guma, Plains Farmer: a villager ([rohan] Man ally, unique); a fellowship ability
					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; damage +1 only
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FightfortheVillagersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Fight for the Villagers
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [rohan] Man strength +2 (or +3 if you spot a villager).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fight");

		assertEquals("Fight for the Villagers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesARohanManStrengthPlus2WhileNoVillagerCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fight = scn.GetFreepsCard("fight");
		var ceorl = scn.GetFreepsCard("ceorl");
		var aldor = scn.GetFreepsCard("aldor");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(ceorl, merry, aragorn);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(fight);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(ceorl, savage1);
		scn.FreepsResolveSkirmish(ceorl);

		assertEquals(6, scn.GetStrength(ceorl));
		assertTrue(scn.FreepsPlayAvailable(fight));
		scn.FreepsPlayCard(fight);

		// Aldor is a [rohan] Man but no villager.  The ally in no skirmish may be chosen too (as printed, F-0527).
		assertTrue(scn.FreepsHasCardChoiceAvailable(ceorl, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, frodo, savage1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ceorl);

		assertEquals(8, scn.GetStrength(ceorl));
		assertEquals(3, scn.GetStrength(aldor));
		assertInZone(Zone.DISCARD, fight);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAVillagerSpottedTheRohanManChosenIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fight = scn.GetFreepsCard("fight");
		var ceorl = scn.GetFreepsCard("ceorl");
		var aldor = scn.GetFreepsCard("aldor");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(ceorl);
		scn.MoveAlliesToTable(aldor, herdsman);
		scn.MoveCardsToHand(fight);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(ceorl, savage1);
		scn.FreepsResolveSkirmish(ceorl);

		assertEquals(3, scn.GetStrength(aldor));
		scn.FreepsPlayCard(fight);
		assertTrue(scn.FreepsHasCardChoiceAvailable(ceorl, aldor, herdsman));
		scn.FreepsChooseCard(aldor);

		// The Herdsman is spotted: +3, with nothing asked of who is spotted.
		assertEquals(6, scn.GetStrength(aldor));
		assertEquals(6, scn.GetStrength(ceorl));
		assertInZone(Zone.DISCARD, fight);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsTheSkirmishPhaseItIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fight = scn.GetFreepsCard("fight");
		var ceorl = scn.GetFreepsCard("ceorl");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(ceorl, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(fight);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{ceorl, savage2});

		// In Aragorn's skirmish, Ceorl, whose skirmish is still to come, is made +3 (Guma, a villager, is spotted).
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(ceorl));
		scn.FreepsPlayCard(fight);
		scn.FreepsChooseCard(ceorl);
		assertEquals(9, scn.GetStrength(ceorl));

		scn.PassSkirmishActions();
		// Aragorn's skirmish is over; Ceorl's is a skirmish phase of its own, and the bonus is gone.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(ceorl);
		assertEquals(6, scn.GetStrength(ceorl));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fight = scn.GetFreepsCard("fight");
		var ceorl = scn.GetFreepsCard("ceorl");
		scn.MoveCompanionsToTable(ceorl);
		scn.MoveCardsToHand(fight);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fight));
		assertInZone(Zone.HAND, fight);
	}

	@Test
	public void WithNoRohanManTheEventIsStillPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fight = scn.GetFreepsCard("fight");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(fight);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsPlayAvailable(fight));
		scn.FreepsPlayCard(fight);

		assertInZone(Zone.DISCARD, fight);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
